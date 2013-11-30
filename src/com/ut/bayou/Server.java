package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Server{
    private int secServerId;
    private ServerId serverId;
    private int port;                         //My Port Id
    private ServerSocket rcvSock;             //Server Socket to receive connections
    private Socket sendSock;                  //Socket to connect to other servers
    private Playlist playlist;                //local server playlist. updated by all threads. needs to be synchronized.

    private HashMap<Integer, Socket> clientSockets; //client port -> socket
    private HashMap<Socket, ObjectOutputStream> outstreams;  //socket -> output stream; all the outstreams. both server and client.
    private HashMap<ServerId, Socket> serverSockets; //server number -> socket
    private Integer totalServersConnectedTo;

    private HashMap<Integer, Write> entropyWrites;
    private Integer numEntropyWrites;
    private int seqNumber;

    private WriteLog tentativeWrites;
    private WriteLog committedWrites;

    private VersionVector versionVector;
    private boolean retired;

    private boolean isPrimary;
    private long largestCSN;

    private boolean canEntropy;
    private HashMap<ServerId, Socket> entropiedWith;
    private static EntropyThread entropyThread;

    private int newPrimary;

    private static Logger logger;
    long nextTimeOut;

    public Server(int serverId, int port) {
        this.secServerId = serverId;
        this.port = port;
        this.totalServersConnectedTo = 0;

        this.playlist = new Playlist();
        this.outstreams = new HashMap<Socket, ObjectOutputStream>();
        this.clientSockets = new HashMap<Integer, Socket>();
        this.serverSockets = new HashMap<ServerId, Socket>();
        this.tentativeWrites = new WriteLog();
        this.committedWrites = new WriteLog();
        this.isPrimary = false;
        this.versionVector = new VersionVector();
        this.largestCSN = 0;
        this.canEntropy = true;
        this.serverId = null;
        this.retired = false;
        this.entropiedWith = new HashMap<ServerId, Socket>();
        logger = Logger.getLogger("Server");
        initializeServer();
        this.newPrimary = 0;
    }

    public void initializeServer(){

        try {
            this.rcvSock = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

         entropyThread = new EntropyThread();

        Runnable listener = new Runnable()
        {
            public void run()
            {
                startListening();
            }
        };
        Thread thread = new Thread(listener);
        thread.start(); //give control to main thread.
    }

    public void startListening(){
        while(!retired){
            Socket socket;
            try {
                logger.info(this+" listening for messages.");
                socket = rcvSock.accept();
                logger.info(this+" connection accepted at "+socket.getPort());
                ObjectOutputStream pout= new ObjectOutputStream(socket.getOutputStream());
                //pout.writeObject(new String("Hello from " + this));
                logger.debug("Sent the message. Will listen now.");
                outstreams.put(socket, pout);

                //Start a Server thread for this client So that more clients can connect.
                new ServerThread(this, socket);

            }catch (SocketException e){
                //e.printStackTrace();
                System.exit(0); //Otherwise it will keep throwing the error.
            }catch (Exception e){
                //e.printStackTrace();
                System.exit(0); //Otherwise it will keep throwing the error.
            }
        }
    }

    //Server Creation
    public synchronized Write addCreationWrite(int svrNum){
        Write creationWrite;
        long acceptTime = System.currentTimeMillis();
        if(isPrimary){
            largestCSN = Math.max(acceptTime, largestCSN+1);
            creationWrite = new Write(acceptTime, largestCSN, serverId, isPrimary, Constants.CREATIONWRITE+":"+svrNum, null,null);
            committedWrites.addToLog(creationWrite);

            ServerId otherId = new ServerId(acceptTime, serverId, svrNum);
            logger.error("ServerId "+otherId);
            versionVector.addNewServerEntry(otherId, acceptTime);
            versionVector.updateAcceptStamp(serverId, acceptTime);
        }
        else{
            creationWrite = new Write(System.currentTimeMillis(), -1, serverId, isPrimary, Constants.CREATIONWRITE+":"+svrNum, null,null);
            tentativeWrites.addToLog(creationWrite);
            versionVector.addNewServerEntry(new ServerId(acceptTime, serverId, serverId.hrNumber), acceptTime);
            versionVector.updateAcceptStamp(serverId, acceptTime);
        }
        return creationWrite;
    }

    public void updateServerIdentity(Write write, int svrNum){
        this.serverId = new ServerId(write.acceptStamp,write.sId,svrNum);
        //tentativeWrites.addToLog(write);
        versionVector.addNewServerEntry(this.serverId, write.acceptStamp);
        //largestCSN = write.acceptStamp + 1;
    }

    public synchronized void addClientSocket(int port, Socket socket){
        logger.debug("adding client socket");
        clientSockets.put(port, socket);
    }

    public synchronized void addServerSocket(ServerId sId, Socket socket){
        logger.debug("adding server socket for " + sId + " in " + secServerId + " and port " + socket.getPort());
        serverSockets.put(sId, socket);
        //versionVector.addNewServerEntry(sId, 0);
    }

    //playlist related methods
    public synchronized void addPlaylist(String song, String url){
        String action = Constants.ADD;
        acceptWrite(song, url, action);
        logger.debug("Adding to playlist");
        playlist.add(song, url);
    }


    public synchronized void editPlaylist(String song, String url){
        String action = Constants.EDIT;
        acceptWrite(song, url, action);
        logger.debug("Editing playlist");
        playlist.edit(song, url);

    }

    public synchronized void deleteFromPlaylist(String song) {
        String action = Constants.DELETE;
        acceptWrite(song, null, action);
        logger.debug("Deleting from playlist");
        playlist.delete(song);
    }

    public synchronized void setServerId(ServerId serverId) {
        if(this.serverId!=null)
            return;

        this.serverId = serverId;
        this.serverId.hrNumber = secServerId;
    }

    public void printServerId(){
        logger.info(this.serverId.stringify());
    }

    private void acceptWrite(String song, String url, String action) {
        if(retired)
            return;
        long acceptStamp = System.currentTimeMillis();

        if(isPrimary){
            largestCSN = Math.max(System.currentTimeMillis(), largestCSN+1);
            Write w = new Write(acceptStamp, largestCSN, serverId, true, action , song, url);
            committedWrites.addToLog(w);
        }else{
            Write w = new Write(acceptStamp, -1, serverId, false, action , song, url);
            tentativeWrites.addToLog(w);
        }
        versionVector.updateAcceptStamp(serverId, acceptStamp);
    }


    public synchronized void startEntropy(){
        if(canEntropy){
            if(entropiedWith.size() == serverSockets.size())
                entropiedWith.clear();

            for(ServerId sid : serverSockets.keySet()){
                if(!entropiedWith.containsKey(sid)){
                    logger.info(this + " starting entropy with server " + sid.hrNumber + " at " + serverSockets.get(sid).getPort());
                    entropiedWith.put(sid,serverSockets.get(sid));
                    sendEntropyRequest(serverSockets.get(sid));
                    break;
                }
            }
        }
    }

    public synchronized void startEntropyWith(ServerId  serverId){
        if(serverSockets.containsKey(serverId))
            sendEntropyRequest(serverSockets.get(serverId));
        else
            logger.error("Not Connected to the requested server");
    }

    public synchronized Socket getServerSockets(ServerId sid) {
        return serverSockets.get(sid);
    }


    //TODO currently sending everything! Send based on accept time stamps

    public synchronized void startSendingEntropyResponse(Socket sock, EntropyReceiverMessage entRcvMsg){
        ObjectOutputStream pout = outstreams.get(sock);
        seqNumber = 0;
        logger.debug(this + " Received CSN " + entRcvMsg.csn + " My csn " + largestCSN);
        if(entRcvMsg.csn < largestCSN){
            logger.debug(this+" Sending committed writes..");
            sendCommitedWrites(pout, entRcvMsg);
        }{
            logger.info(this+" no new committed writes to send");
        }

        sendTentativeWrites(pout, entRcvMsg);

        try {
            pout.writeObject(new EntropyFinished(serverId, seqNumber));
            setCanEntropy(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setCanEntropy(boolean can){
        canEntropy = can;
    }

    public synchronized void closeConnection(ServerId sid){
        try {
            logger.debug(this.serverId.hrNumber+" Removing " + sid.hrNumber);
            Socket temp = serverSockets.remove(sid);
            if(temp != null)
            {
                outstreams.remove(temp).close();
                temp.close();
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }


    public synchronized void closeConnectionForClient(int clientNum){
        try {
            logger.info(this.serverId.hrNumber + " Removing Client " + clientNum + " client sockets size " + clientSockets.size());

            Socket temp = clientSockets.remove(clientNum);
            logger.info(this.serverId.hrNumber+" Removing Client " + clientNum + " client sockets size "+clientSockets.size());
            if(temp != null)
            {
                outstreams.remove(temp).close();
                temp.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void isolate(){
        for(Socket sock : serverSockets.values())
        {
            try {
                outstreams.remove(sock).close();
                sock.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        serverSockets.clear();
    }

    private void sendTentativeWrites(ObjectOutputStream pout, EntropyReceiverMessage entRcvMsg) {
        Iterator<Write> it = tentativeWrites.iterator();
        Write writeToSend;

        while (it.hasNext()){
            writeToSend = it.next();
            if(!entRcvMsg.VV.hasServerId(writeToSend.sId) || (entRcvMsg.VV.hasServerId(writeToSend.sId) && writeToSend.acceptStamp > entRcvMsg.VV.getLatestStamp(writeToSend.sId))){
                sendEntropyWrite(pout, writeToSend);
                seqNumber++;
            }else{

            }
        }
    }

    public synchronized void sendCommitedWrites(ObjectOutputStream pout, EntropyReceiverMessage entRcvMsg){
        Iterator<Write> it = committedWrites.iterator();
        Write writeToSend;

        while(it.hasNext()){
            writeToSend = it.next();
            sendEntropyWrite(pout, writeToSend);
            seqNumber++;
        }
    }

    public synchronized void sendEntropyWrite(ObjectOutputStream pout, Write w){
        try {
            logger.info("Sending entropy write. " + w.command);
            pout.writeObject((new EntropyWriteMessage(serverId, w, seqNumber)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void sendEntropyRequest(Socket socket){
        ObjectOutputStream pout = outstreams.get(socket);
        if(pout == null)
            logger.error(this+" not connected to "+socket.getPort());
        try {
            pout.writeObject((new RequestEntropyMessage(serverId)));

        }catch (SocketException ss){
            logger.debug("Socket Exception: Disconnecting...");
        }
        catch (IOException e) {
         //   e.printStackTrace();
        }
    }

    public void dummy(){
        String s = "";
        for(Socket sock : outstreams.keySet())
            s += " "+sock.getPort();

    }

    public synchronized void respondToEntropyRequest(Socket sock){
        if(canEntropy){

            ObjectOutputStream pout = null;
            try {
                pout = outstreams.get(sock);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(pout == null){
                logger.error("Error outstream is "+serverSockets.get(1));
            }else{
                canEntropy = false;
                try {
                    pout.writeObject((new EntropyReceiverMessage(serverId, versionVector, largestCSN)));
                    //Initialize entropy receive variables
                    entropyWrites = new HashMap<Integer, Write>();
                    numEntropyWrites = Integer.MAX_VALUE;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void processEntropyWriteMessage(EntropyWriteMessage reqEntMsg, Socket sock){
        entropyWrites.put(reqEntMsg.seqNumber, reqEntMsg.write);

        if(entropyWrites.size() == numEntropyWrites){
            notifyAll();
        }
    }

    public synchronized void finalizeEntropySession(EntropyFinished efAck){
        numEntropyWrites = efAck.numOfMessages;

        while(entropyWrites.size() != numEntropyWrites) {
            try {
                wait();
            }
            catch(InterruptedException e) {}
        }


        if(entropyWrites.size() != numEntropyWrites){
            logger.error("Missing some messages. Need to wait and sleep");
        }{
            for (int i = 0; i < efAck.numOfMessages; i++) {

                Write thewrite = entropyWrites.get(i);
                if(thewrite.committed){
                    committedWrites.addToLog(thewrite);
                    tentativeWrites.removeWrite(thewrite);
                    largestCSN = Math.max(thewrite.csn, largestCSN);
                }else{
                    if(isPrimary){
                        largestCSN = Math.max(System.currentTimeMillis(), largestCSN+1);
                        thewrite.committed = true;
                        thewrite.csn = largestCSN;
                        committedWrites.addToLog(thewrite);
                    }else{
                        tentativeWrites.addToLog(thewrite);
                    }
                }

                if(versionVector.hasServerId(thewrite.sId))
                    versionVector.updateAcceptStamp(thewrite.sId, thewrite.acceptStamp);
                else
                    versionVector.addNewServerEntry(thewrite.sId, thewrite.acceptStamp);
            }

            entropyWrites.clear();
            numEntropyWrites = Integer.MAX_VALUE;

            //Inform other server to do its thing
            ObjectOutputStream serOut =  outstreams.get(serverSockets.get(efAck.srcId));
            try {
                serOut.writeObject(new EntropyFinishedAck(serverId));
            } catch (IOException e) {
                e.printStackTrace();
            }
            reCreatePlaylist();
        }

    }

    public synchronized void reCreatePlaylist(){
        synchronized (playlist){
            playlist.clear();

            Iterator<Write> it = committedWrites.iterator();
            while(it.hasNext()){
                Write w = it.next();
                if(w.command.startsWith("CREATION")){
                    processCreationWrite(w);
                }else if(w.command.startsWith(Constants.RETIREMENTWRITE)){
                    processRetirementWrite(w);
                }
                processWrite(w.song, w.url, w.command);
            }

            it = tentativeWrites.iterator();
            while(it.hasNext()){
                Write w = it.next();
                if(w.command.startsWith("CREATION")){
                    processCreationWrite(w);
                }else if(w.command.startsWith(Constants.RETIREMENTWRITE)){
                    processRetirementWrite(w);
                }
                processWrite(w.song, w.url, w.command);
            }
        }
         canEntropy = true;
    }

    private void processRetirementWrite(Write w){
        if(w.command.contains(Constants.PRIMARY)){
            isPrimary = true;

            w.command = Constants.RETIREMENTWRITE+":"+Constants.SECONDARY;
            versionVector.remove(w.sId);
        }else{
            versionVector.remove(w.sId);
        }
    }

    private void processCreationWrite(Write w) {
        int svrNum  =Integer.parseInt(w.command.split(":",2)[1]);
        if(serverId == null && svrNum == secServerId){
            updateServerIdentity(w, secServerId); //set your own server id and update version vector YOUR CREATION.
        }
        else{
            ServerId newServerId = new ServerId(w.acceptStamp,w.sId,svrNum);
            if(!versionVector.hasServerId(newServerId)){
                versionVector.addNewServerEntry(newServerId, w.acceptStamp);
            }
        }
    }

    public void entropyFinishedAck(EntropyFinishedAck entropyFinishedAck){
        if(retired){
            isolate();
            entropyThread.kill();
            logger.debug("The new primary that I chose " + entropyFinishedAck.srcId.hrNumber);
            newPrimary = entropyFinishedAck.srcId.hrNumber;
            Bayou.setPrimaryServer(newPrimary);
        }
    }

    //Simply edit the playlist
    public void processWrite(String song, String url, String command){
        if(command.equals(Constants.ADD))
            playlist.add(song,url);
        else if(command.equals(Constants.EDIT))
            playlist.edit(song, url);
        else if(command.equals(Constants.DELETE))
            playlist.delete(song);
    }

    public void printLog(){
        String logstring = "";
        logstring = this + "$ logs\n";
        logstring+="---------------------------\n";
        logstring += "Tentative Writes number " + tentativeWrites.size();
        logstring += "\n"+tentativeWrites;
        logstring+="\n---------------------------\n";
        logstring += "\nCommited Writes number " + committedWrites.size();
        logstring += "\n"+committedWrites;
        logstring += versionVector;

        logger.info(logstring);
    }

    public void connectToYou(ServerId svrId, int svrPort){
        if(!(serverSockets.containsKey(svrId))){
            try {
                sendSock = new Socket();
                sendSock.connect(new InetSocketAddress(InetAddress.getLocalHost(), svrPort));
                ObjectOutputStream pout = new ObjectOutputStream(sendSock.getOutputStream());
                pout.writeObject((new ServerConnectAck(serverId, new ServerId(System.currentTimeMillis(), serverId, -1))));
                logger.debug("adding server socket for " + svrId + " in " + secServerId + " port number " + sendSock.getPort());
                serverSockets.put(svrId, sendSock);
                outstreams.put(sendSock, pout);
                if(!versionVector.hasServerId(svrId))
                    versionVector.addNewServerEntry(svrId, 0);
                new ServerThread(this, sendSock, svrId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printConnections() {
        logger.info("Server" + serverId.hrNumber + " connections");
        logger.info("Server Connections");
        for(ServerId sID : serverSockets.keySet())
            logger.info(sID.hrNumber + " -> " + " remote port: " + serverSockets.get(sID).getPort() + " local port " + serverSockets.get(sID).getLocalPort());
        logger.info("Client Connections");
        for(Integer sID : clientSockets.keySet())
            logger.info(sID + " -> " + " remote port: " + clientSockets.get(sID).getPort() + " local port " + clientSockets.get(sID).getLocalPort());

    }


    public synchronized void respondToClientDisconnect(ClientDisconnect clDisconnect){
        Socket sock = clientSockets.get(clDisconnect.clientId);
        if(sock!=null){
            ObjectOutputStream oout = outstreams.get(sock);
            try {
                logger.info(this+" responding to client disconnect");
                oout.writeObject(new ClientDisconnectServerResponse(serverId, versionVector.getLatestStamp(serverId)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized int retire() {
        if(serverSockets.size() == 0){
            logger.error("Cannot retire not connected to any server.");
            return -2;
        }
        this.retired = true;

        long acceptStamp = System.currentTimeMillis();
        Write retirementWrite;
        if(isPrimary)
        {
            largestCSN = Math.max(acceptStamp, largestCSN+1);
            retirementWrite = new Write(acceptStamp, largestCSN, serverId, isPrimary, Constants.RETIREMENTWRITE+":"+Constants.PRIMARY, null, null);
            committedWrites.addToLog(retirementWrite);
        }
        else
        {
            logger.info("Adding retirement write to log!");
            printLog();
            retirementWrite = new Write(acceptStamp, largestCSN, serverId, isPrimary, Constants.RETIREMENTWRITE+":"+Constants.SECONDARY, null, null);
            tentativeWrites.addToLog(retirementWrite);
        }
        versionVector.updateAcceptStamp(serverId, acceptStamp);

        startEntropy();

        return newPrimary;
    }

    public synchronized void respondWithStatus(ServerDbStatus sbdStatus){
        logger.info("CLIENT ID/PORT " + sbdStatus.clientId);
        Socket sock = clientSockets.get(sbdStatus.clientId);
        if(sock!=null && sbdStatus.lastAcceptedTimestamp != -1){
            ObjectOutputStream oout = outstreams.get(sock);
            try {
                logger.info(this+" responding to Server Status");
                if(versionVector.getLatestStamp(sbdStatus.lastServer) >= sbdStatus.lastAcceptedTimestamp){
                    oout.writeObject(new ServerDbStatusResponse(serverId, true, playlist));
                }
                else{
                    oout.writeObject(new ServerDbStatusResponse(serverId, false, null));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(sock!=null && sbdStatus.lastAcceptedTimestamp == -1){
                ObjectOutputStream oout = outstreams.get(sock);
            try {
                oout.writeObject(new ServerDbStatusResponse(serverId, true, playlist));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerId getServerId() {
        return serverId;
    }

    public void printServerPlaylist(){
        playlist.printIt();
    }

    //make the first server to join a primary and assign a server id
    public void setPrimary(boolean primary) {
        isPrimary = primary;
        if(this.serverId == null){
            this.serverId = new ServerId(System.currentTimeMillis(), null, 0);
            versionVector.addNewServerEntry(this.serverId, serverId.iSTimestamp);
        }
    }

    public String toString(){
        return "Server "+ secServerId +" at "+port+" : ";
    }

    class EntropyThread extends Thread{
        boolean keepEntropyLive;
        public EntropyThread(){
            logger.info("Entropy thread initialized for "+secServerId);
            nextTimeOut = System.currentTimeMillis() + Constants.EntropyInverval;
            keepEntropyLive = true;
            start();
        }

        public void run() {
            while (keepEntropyLive) {
                Random r = new Random();
                nextTimeOut = System.currentTimeMillis() + Constants.EntropyInverval + r.nextInt(5000) ;
                while (nextTimeOut > System.currentTimeMillis()) {
                    yield();
                }
                try {
                    startEntropy();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        public void kill(){
            keepEntropyLive = false;
        }
    }

}



