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
            outstream = new PrintWriter(sock.getOutputStream(), true);
            instream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            outstream.println((new ClientConnectAck(clientId).stringify()));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        outstream.println((new UserAction(this.clientId, Constants.ADD, song, url)).stringify());
    }
    public void editPlaylist(String song, String url){
        localPlaylist.edit(song, url);
        outstream.println(new UserAction(this.clientId, Constants.EDIT, song, url).stringify());
    }

    public void deleteFromPlaylist(String song) {
        localPlaylist.delete(song);
        outstream.println(new UserAction(this.clientId, Constants.DELETE, song, null).stringify());
    }

    public void printPlaylist(){
        localPlaylist.printIt();
    }

    public String toString(){
        return "Client "+clientId+" connected to "+port+" ";
    }
}