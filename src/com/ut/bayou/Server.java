package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;

public class Server{
    private int serverId;
    private int port;                         //My Port Id
    private ServerSocket rcvSock;             //Server Socket to receive connections
    private Socket sendSock;             //Server Socket to receive connections
    private Playlist playlist;                //local server playlist. updated by all threads. needs to be synchronized.

    private HashMap<Integer, Socket> clientSockets; //server -> socket
    private HashMap<Socket, PrintWriter> outstreams;  //socket -> output stream
    private HashMap<Integer, Socket> serverSockets; //server -> socket

    private WriteLog tentativeWrites;
    private WriteLog committedWrites;

    private VersionVector versionVector;

    private boolean isPrimary;
    private int largestUnusedCsn;

    private static Logger logger;

    public Server(int serverId, int port) {
        this.serverId = serverId;
        this.port = port;
        this.playlist = new Playlist();
        this.outstreams = new HashMap<Socket, PrintWriter>();
        this.clientSockets = new HashMap<Integer, Socket>();
        this.serverSockets = new HashMap<Integer, Socket>();
        this.tentativeWrites = new WriteLog();
        this.committedWrites = new WriteLog();
        this.isPrimary = false;
        this.versionVector = new VersionVector();
        this.largestUnusedCsn = 0;
        versionVector.addNewServerEntry(serverId, 0); //add your own entry to vector first.
        logger = Logger.getLogger("Server");
        initializeServer();
    }

    public void initializeServer(){

        try {
            this.rcvSock = new ServerSocket(port);
            addShutdownHooks(this); //ensures that the ports are released.
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
                PrintWriter pout= new PrintWriter(socket.getOutputStream(), true);
                pout.println("Hello from "+this);
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
        logger.debug("adding server socket");
        serverSockets.put(sId, socket);
    }

    //playlist related methods
    public void addPlaylist(String song, String url){
        String action = Constants.ADD;
        acceptWrite(song, url, action);
        logger.debug("Adding to playlist");
        playlist.add(song, url);
    }


    public void editPlaylist(String song, String url){
        String action = Constants.EDIT;
        acceptWrite(song, url, action);
        logger.debug("Editing playlist");
        playlist.edit(song, url);

    }

    public void deleteFromPlaylist(String song) {
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


    public void printLog(){
        String logstring = "";
        logstring = this + "$ logs\n"+
                "---------------------------\n"+
                "Tentative Writes number " + tentativeWrites.size()+
                "\nCommited Writes number " + committedWrites.size()+"\n"+
                "Version Vector: "+versionVector.strigify();
        ;
        logger.info(logstring);
    }

    public void connectToYou(int svrNum, int svrPort){
        if(!(clientSockets.containsKey(svrNum) || clientSockets.containsKey(svrPort))){
            sendSock = new Socket();
            try {
                sendSock.connect(new InetSocketAddress(InetAddress.getLocalHost(), svrPort));
                PrintWriter pout = new PrintWriter(sendSock.getOutputStream());
                pout.println((new ServerConnectAck(serverId).stringify()));
                serverSockets.put(svrNum, sendSock);
                outstreams.put(sendSock, pout);
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

    //To allow the graceful closing of socket connections.
    private void addShutdownHooks(final Server server){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            public void run()
            {
                server.shutdown();
            }
        }));
        logger.debug(this+"Shutdown hooks attached");
    }

    private void shutdown(){
        try
        {
            logger.info("Shutting down server " + this);
            rcvSock.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}



