package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerThread extends Thread{
    private Socket sock;
    private Server pServer;
    private static Logger logger = Logger.getLogger("Server");

    public ServerThread(Server server, Socket sock){
        this.sock = sock;
        this.pServer = server;
        start();
    }

    public void run(){
        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(sock.getInputStream()));
            Object line;
            while((line = in.readObject()) != null){
                logger.debug(this+ " message received");
                deliver(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deliver(Object msg){
        if(msg instanceof UserAction){
            logger.debug(this+ " user action received from client "+((UserAction) msg).srcId);
            performPlaylistAction((UserAction) msg);
        }else{

        }
    }

    private void performPlaylistAction(UserAction msg) {
        UserAction ua = (UserAction) msg;
        if(Constants.ADD.equals(ua.action)){
            logger.debug(this+ " adding to playlist");
            pServer.addPlaylist(ua.song, ua.url);}
        else if(Constants.EDIT.equals(ua.action))
            pServer.editPlaylist(ua.song, ua.url);
        else if(Constants.DELETE.equals(ua.action))
            pServer.deleteFromPlaylist(ua.song);
        else
            logger.error("unknown action");
    }

    public String toString(){
        return "Server thread at "+ pServer;
    }

}
