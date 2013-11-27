package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private int clientId;
    private int port;
    private Socket sock;
    private ObjectOutputStream outstream;
    private ObjectInputStream instream;
    private static Logger logger = Logger.getLogger("Client");
    private Playlist localPlaylist; //to ensure read your write property. How to make sure that it is updated?

    public Client(int clientId, int port){
        this.clientId = clientId;
        this.port = port;
        this.localPlaylist = new Playlist();
        connect();
    }

    public void connect(){
        try {
            sock = new Socket("localhost", port);
            outstream = new ObjectOutputStream(sock.getOutputStream());
            instream = new ObjectInputStream(sock.getInputStream());
            outstream.writeObject((new ClientConnectAck()));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executeUserCommand(String [] command) throws IOException {
        if(Constants.ADD.equals(command[2].toUpperCase()))
            addPlaylist(command[3], command[4]);
        else if(Constants.EDIT.equals(command[2].toUpperCase()))
            editPlaylist(command[3], command[4]);
        else if(Constants.DELETE.equals(command[2].toUpperCase()))
            deleteFromPlaylist(command[3]);
        else
            printPlaylist();
    }

    public void addPlaylist(String song, String url) throws IOException {
        localPlaylist.add(song, url);
        outstream.writeObject((new UserAction(this.clientId, Constants.ADD, song, url)));

    }
    public void editPlaylist(String song, String url) throws IOException{
        localPlaylist.edit(song, url);
        outstream.writeObject(new UserAction(this.clientId, Constants.EDIT, song, url));
    }

    public void deleteFromPlaylist(String song) throws IOException{
        localPlaylist.delete(song);
        outstream.writeObject(new UserAction(this.clientId, Constants.DELETE, song, null));
    }

    public void printPlaylist(){
        localPlaylist.printIt();
    }

    public String toString(){
        return "Client "+clientId+" connected to "+port+" ";
    }
}