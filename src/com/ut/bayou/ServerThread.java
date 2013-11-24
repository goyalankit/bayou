package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            Object line;
            while((line = in.readLine()) != null){
                logger.debug(this+ " Message received");
                deliver(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deliver(Object msg){
        if(msg instanceof UserAction){
            logger.debug(this+ " USER ACTION RECEIVED");
            performPlaylistAction((UserAction) msg);
        }else{

        }
    }

    private void performPlaylistAction(UserAction msg) {
        UserAction ua = (UserAction) msg;
        if(Constants.ADD.equals(ua.action)){
            logger.debug(this+ " ADDING TO PLAYLIST");
            pServer.addPlaylist(ua.song, ua.url);}
        else if(Constants.EDIT.equals(ua.action))
            pServer.editPlaylist(ua.song, ua.url);
        else if(Constants.DELETE.equals(ua.action))
            pServer.deleteFromPlaylist(ua.song);
        else
            logger.error("unknown action");
    }
}
