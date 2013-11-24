package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class Server{
    private int serverId;
    private int port;                         //My Port Id
    private ServerSocket rcvSock;             //Server Socket to receive connections
    private Playlist playlist;                //local server playlist. updated by all threads. needs to be synchronized.

    private HashMap<Integer, Socket> idSockets; //server -> socket
    private HashMap<Socket, ObjectOutputStream> outstreams;  //socket -> output stream

    private static Logger logger;

    public Server(int serverId, int port) {
        this.serverId = serverId;
        this.port = port;
        this.playlist = new Playlist();
        this.outstreams = new HashMap<Socket, ObjectOutputStream>();
        this.idSockets = new HashMap<Integer, Socket>();
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
                logger.info(this+" connection accepted.");
                ObjectOutputStream obout= new ObjectOutputStream(socket.getOutputStream());
                obout.writeObject(new String("Ahoy! from "+this));
                logger.debug("Sent the message. Will listen now.");
                outstreams.put(socket, obout);
                idSockets.put(socket.getPort(), socket);
                //Start a Server thread for this client So that more clients can connect.
                new ServerThread(this, socket);

            }catch (SocketException e){
                e.printStackTrace();
                System.exit(0); //Otherwise it will keep throwing the error.
            }catch (Exception e){
                e.printStackTrace();
                System.exit(0); //Otherwise it will keep throwing the error.
            }
        }
    }



    //playlist related methods
    public void addPlaylist(String song, String url){
        logger.debug("Adding to server playlist");
        playlist.add(song, url);
    }
    public void editPlaylist(String song, String url){
        playlist.edit(song, url);
    }

    public void deleteFromPlaylist(String song) {
        playlist.delete(song);
    }

    public void printPlaylist(){
        playlist.printIt();
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



