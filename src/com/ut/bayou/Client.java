package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private int clientId;
    private int port;
    private Socket sock;
    private ObjectOutputStream outstream;
    private ObjectInputStream instream;
    private static Logger logger = Logger.getLogger("Client");
    private Playlist localPlaylist;

    public Client(int clientId, int port){
        this.clientId = clientId;
        this.port = port;
        this.localPlaylist = new Playlist();

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
            outstream = new ObjectOutputStream(sock.getOutputStream());
            instream = new ObjectInputStream(sock.getInputStream());
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
                Object line;
                while((line = instream.readObject()) != null)
                {
                    logger.info("Message received "+line);
                    outstream.writeObject(new String("Ahoy from " + this));
                }
                logger.debug(this+" Exiting");
            }
            catch(SocketException e)
            {
                logger.error("Socket Exception at "+this+ " Exiting...");
                System.exit(0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void executeUserCommand(String [] command){
        if(Constants.ADD.equals(command[2].toUpperCase()))
            addPlaylist(command[3], command[4]);
        else if(Constants.EDIT.equals(command[2].toUpperCase()))
            editPlaylist(command[3], command[4]);
        else if(Constants.DELETE.equals(command[2].toUpperCase()))
            deleteFromPlaylist(command[3]);
        else
            printPlaylist();
    }

    public void addPlaylist(String song, String url){
        localPlaylist.add(song, url);
        try {
            outstream.writeObject(new UserAction(this.clientId, Constants.ADD, song, url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void editPlaylist(String song, String url){
        localPlaylist.edit(song, url);
        try {
            outstream.writeObject(new UserAction(this.clientId, Constants.EDIT, song, url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFromPlaylist(String song) {
        localPlaylist.delete(song);
        try {
            outstream.writeObject(new UserAction(this.clientId, Constants.DELETE, song, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printPlaylist(){
        localPlaylist.printIt();
    }

    public String toString(){
        return "Client "+clientId+" connected to "+port+" ";
    }
}

