package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class Server{
    private int serverId;
    private int port;                                               //My Port Id
    private ServerSocket rcvSock;                                   //Server Socket to receive connections

    private HashMap<Integer, Socket> socks;
    private HashMap<Socket, PrintWriter> ostreams;

    private static Logger logger;

    public Server(int serverId, int port) {
        this.serverId = serverId;
        this.port = port;
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
        thread.start(); //send back the control to main.
    }

    public void startListening(){
        while(true){
            Socket socket;
            try {
                logger.info(this+" listening for messages.");
                socket = rcvSock.accept();
                logger.info(this+" connection accepted.");
                PrintWriter pout= new PrintWriter(socket.getOutputStream(), true);
                pout.println("Ahoy! from "+this);
                logger.debug("Sent the message. Will listen now.");

                //Start a Server thread for this client So that more clients can connect.
                new ServerThread(this, socket);

            }catch (SocketException e){
                System.exit(0); //Otherwise it will keep throwing the error.
            }catch (Exception e){
                e.printStackTrace();
                System.exit(0); //Otherwise it will keep throwing the error.
            }
        }
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
        catch(SocketException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}



