package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThread extends Thread{
    private Socket sock;
    private static Logger logger = Logger.getLogger("Server");

    public ServerThread(Socket sock){
        this.sock = sock;
        start();
    }

    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            while((line = in.readLine()) != null){
                logger.info(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
