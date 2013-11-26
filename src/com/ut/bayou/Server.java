package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;

public class Server{
    private int serverId;
    private int port;                         //My Port Id
    private ServerSocket rcvSock;             //Server Socket to receive connections
    private Socket sendSock;                  //Socket to connect to other servers
    private Playlist playlist;                //local server playlist. updated by all threads. needs to be synchronized.

    private HashMap<Integer, Socket> clientSockets; //client port -> socket
    private HashMap<Socket, ObjectOutputStream> outstreams;  //socket -> output stream; all the outstreams. both server and client.
    private HashMap<Integer, Socket> serverSockets; //server number -> socket

    private WriteLog tentativeWrites;
    private WriteLog committedWrites;

    private VersionVector versionVector;

    private boolean isPrimary;
    private int largestUnusedCsn;

    private boolean canEntropy;
    private HashMap<Integer, Socket> entropiedWith;

    private static Logger logger;

    public Server(int serverId, int port) {
        this.serverId = serverId;
        this.port = port;
        this.playlist = new Playlist();
        this.outstreams = new HashMap<Socket, ObjectOutputStream>();
        this.clientSockets = new HashMap<Integer, Socket>();
        this.serverSockets = new HashMap<Integer, Socket>();
        this.tentativeWrites = new WriteLog();
        this.committedWrites = new WriteLog();
        this.isPrimary = false;
        this.versionVector = new VersionVector();
        this.largestUnusedCsn = 0;
        this.canEntropy = true;
        this.entropiedWith = new HashMap<Integer, Socket>();
        versionVector.addNewServerEntry(serverId, 0); //add your own entry to vector first.
        logger = Logger.getLogger("Server");
        initializeServer();
    }

    public void initializeServer(){

        try {
            this.rcvSock = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        while(true){
            Socket socket;
            try {
                logger.info(this+" listening for messages.");
                socket = rcvSock.accept();
                logger.info(this+" connection accepted at "+socket.getPort());
                ObjectOutputStream pout= new ObjectOutputStream(socket.getOutputStream());
                pout.writeObject(new String("Hello from " + this));
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

    public synchronized void addClientSocket(int port, Socket socket){
        logger.debug("adding client socket");
        clientSockets.put(port, socket);
    }

    public synchronized void addServerSocket(int sId, Socket socket){
        logger.debug("adding server socket for "+sId +" in "+serverId + " and port " + socket.getPort());
        serverSockets.put(sId, socket);
        versionVector.addNewServerEntry(sId, 0);
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

    private void acceptWrite(String song, String url, String action) {
        long acceptStamp = System.currentTimeMillis();

        if(isPrimary){
            Write w = new Write(acceptStamp, largestUnusedCsn++, serverId, true, action , song, url);
            committedWrites.addToLog(w);
        }else{
            Write w = new Write(acceptStamp, -1, serverId, false, action , song, url);
            tentativeWrites.addToLog(w);
        }
        versionVector.updateMyAcceptStamp(serverId, acceptStamp);
    }


    public synchronized void startEntropy(){
        if(canEntropy){
            if(entropiedWith.size() == serverSockets.size())
                entropiedWith.clear();
            for(Integer sid : serverSockets.keySet()){
                if(!entropiedWith.containsKey(sid)){
                    logger.info(this + " starting entropy with server " + sid + " at " + serverSockets.get(sid).getPort());
                    sendEntropyRequest(serverSockets.get(sid));
                }
            }
        }
    }


    //TODO currently sending everything! Send based on accept time stamps

    public synchronized void startSendingEntropyResponse(Socket sock, EntropyReceiverMessage entRcvMsg){
        ObjectOutputStream pout = outstreams.get(sock);
        if(entRcvMsg.csn < largestUnusedCsn-1)
            sendCommitedWrites(pout, entRcvMsg);

        sendTentativeWrites(pout, entRcvMsg);

    }

    private void sendTentativeWrites(ObjectOutputStream pout, EntropyReceiverMessage entRcvMsg) {
        Iterator<Write> it = tentativeWrites.iterator();
        Write writeToSend;

        while (it.hasNext()){
            writeToSend = it.next();
            sendEntropyWrite(pout, writeToSend);
        }
    }

    public synchronized void sendCommitedWrites(ObjectOutputStream pout, EntropyReceiverMessage entRcvMsg){
        Iterator<Write> it = committedWrites.iterator();
        Write writeToSend;

        while(it.hasNext()){
            writeToSend = it.next();
            sendEntropyWrite(pout, writeToSend);
        }
    }

    public synchronized void sendEntropyWrite(ObjectOutputStream pout, Write w){
        try {
            pout.writeObject((new EntropyWriteMessage(serverId, w)));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public synchronized void sendEntropyRequest(Socket socket){
        ObjectOutputStream pout = outstreams.get(socket);
        try {
            pout.writeObject((new RequestEntropyMessage(serverId)));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public synchronized void respondToEntropyRequest(Socket sock){
        if(canEntropy){

            ObjectOutputStream pout = null;
            try {
                pout = outstreams.get(sock);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if(pout == null){
                logger.error("Error outstream is "+serverSockets.get(1));
            }else{
                canEntropy = false;
                try {
                    pout.writeObject((new EntropyReceiverMessage(serverId, versionVector, largestUnusedCsn - 1)));
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public void printLog(){
        String logstring = "";
        logstring = this + "$ logs\n"+
                "---------------------------\n"+
                "Tentative Writes number " + tentativeWrites.size()+
                "\nCommited Writes number " + committedWrites.size()+"\n"+
                "Version Vector: "+versionVector.strigify()+"C Writes: "+committedWrites + "\n T writes: "+tentativeWrites;
        logger.info(logstring);
    }

    public void connectToYou(int svrNum, int svrPort){
        if(!(clientSockets.containsKey(svrNum) || clientSockets.containsKey(svrPort))){
            sendSock = new Socket();
            try {
                sendSock.connect(new InetSocketAddress(InetAddress.getLocalHost(), svrPort));
                ObjectOutputStream pout = new ObjectOutputStream(sendSock.getOutputStream());
                pout.writeObject((new ServerConnectAck(serverId)));
                logger.debug("adding server socket for " + svrNum + " in " + serverId + " port number " + sendSock.getPort());
                serverSockets.put(svrNum, sendSock);
                outstreams.put(sendSock, pout);
                versionVector.addNewServerEntry(svrNum, 0);
                new ServerThread(this, sendSock);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public String toString(){
        return "Server "+serverId+" at "+port+" : ";
    }
}



