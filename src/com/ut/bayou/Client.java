package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private int clientId;
    private int port;
    private Socket sock;
    private PrintWriter outstream;
    private BufferedReader instream;
    private static Logger logger = Logger.getLogger("Client");

    public Client(int clientId, int port){
        this.clientId = clientId;
        this.port = port;

        initializeClient();
    }

    public void initializeClient(){
        connect();
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

    public void connect(){
        try {
            sock = new Socket("localhost", port);
            outstream = new PrintWriter(sock.getOutputStream(), true);
            instream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startListening(){
        while(true)
        {
            try
            {
                logger.debug(this+" listening for messages");
                String line;
                while((line = instream.readLine()) != null)
                {
                    logger.info("Message received "+line);
                    outstream.println("Ahoy from " + this);
                }
                logger.debug(this+" Exiting");
            }
            catch(SocketException e)
            {
                logger.error("Socket Exception at "+this+ " Exiting...");
                System.exit(0);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public String toString(){
        return "Client "+clientId+" connected to "+port+" ";
    }
}

