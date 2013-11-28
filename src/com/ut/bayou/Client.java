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
    private Server previousServer;
    private Server currentServer;
    private long tsLastServer;
    private boolean serverUpdateStatus;
    boolean reConnected;

    public Client(int clientId, int port, Server server){
        this.clientId = clientId;
        this.port = port;
        this.localPlaylist = new Playlist();
        this.currentServer = server;
        this.reConnected = false;
        connect();
    }

    public void connect(){
        try {
            sock = new Socket("localhost", port);
            outstream = new ObjectOutputStream(sock.getOutputStream());
            instream = new ObjectInputStream(sock.getInputStream());
            outstream.writeObject((new ClientConnectAck(clientId)));
            //instream.readObject();

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

    public void disconnect() {
        previousServer = currentServer;
        currentServer  = null;

        try {
            outstream.writeObject(new ClientDisconnect(sock.getLocalPort()));
            ClientDisconnectServerResponse cds = (ClientDisconnectServerResponse) instream.readObject();
            tsLastServer = cds.currentAcceptTime;
            serverUpdateStatus = false;
            logger.info("Setting timestamp of last server = " + tsLastServer);
            outstream.close();
            instream.close();
            sock.close();
            sock = null;
            outstream = null;
            instream = null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e)
        {
            System.out.println("Disconnection error in client " + clientId);
        } catch (ClassNotFoundException c){}
    }

    public void reconnect(int port, Server server)
    {
        if(sock != null)
        {
            System.out.println("Request failed, client already connected.");
        }
        else
        {
            try {
                sock = new Socket("localhost", port);
                currentServer = server;
                outstream = new ObjectOutputStream(sock.getOutputStream());
                instream = new ObjectInputStream(sock.getInputStream());
                outstream.writeObject((new ClientConnectAck(clientId)));
                reConnected = true;
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void addPlaylist(String song, String url) throws IOException {
        localPlaylist.add(song, url);
        outstream.writeObject((new UserAction(this.clientId, Constants.ADD, song, url)));
        checkServerStatus();
    }
    public void editPlaylist(String song, String url) throws IOException{
        localPlaylist.edit(song, url);
        outstream.writeObject(new UserAction(this.clientId, Constants.EDIT, song, url));
        checkServerStatus();
    }

    public void deleteFromPlaylist(String song) throws IOException{
        localPlaylist.delete(song);
        outstream.writeObject(new UserAction(this.clientId, Constants.DELETE, song, null));
        checkServerStatus();
    }

    public void checkServerStatus(){
        try {
            if(reConnected){
                outstream.writeObject(new ServerDbStatus(sock.getLocalPort(), previousServer.getServerId(), tsLastServer));
                ServerDbStatusResponse sdbRes = (ServerDbStatusResponse)instream.readObject();
                if(sdbRes.updated){
                    logger.info("Received playlist");sdbRes.playlist.printIt();
                    localPlaylist = sdbRes.playlist;
                    reConnected = false;
                    serverUpdateStatus = true;
                }else {
                    logger.info(" $ Server not updated. Information may be stale");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException c){}
    }

    public void printPlaylist(){
        localPlaylist.printIt();
        checkServerStatus();
    }

    public String toString(){
        return "Client "+clientId+" connected to "+port+" ";
    }
}