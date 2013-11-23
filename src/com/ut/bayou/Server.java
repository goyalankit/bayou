package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server{
    private int serverId;
    private int port;
    private ServerSocket rcvSock;
    private static Logger logger = Logger.getLogger("Server");

    public Server(int serverId, int port) {
        this.serverId = serverId;
        this.port = port;
        try {
            this.rcvSock = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initializeServer();
    }


    public void initializeServer(){
        Runnable listener = new Runnable()
        {
            public void run()
            {
                startListening();
            }
        };
        Thread thread = new Thread(listener);
        thread.start();
    }

    public void startListening(){
        while(true){
            logger.debug("i am here");
            Socket socket;
            try {
                logger.info(this+" listening for messages.");
                socket = rcvSock.accept();
                logger.info(this+" connection accepted.");
                PrintWriter pout= new PrintWriter(socket.getOutputStream(), true);
                pout.println("Ahoy! from "+this);
                logger.debug("Sent the message. Will listen now.");

                //Start a thread.
                ServerThread s = new ServerThread(socket);

            }catch (SocketException e){
                e.printStackTrace();
                System.exit(0); //Otherwise it will keep throwing the error.
            }catch (Exception e){
                e.printStackTrace();
                System.exit(0); //Otherwise it will keep throwing the error.
            }
        }
    }



    public String toString(){
        return "Server "+serverId+" at "+port+" ";
    }

}

