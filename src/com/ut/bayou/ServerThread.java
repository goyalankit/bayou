package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ServerThread extends Thread {
    private Socket sock;
    private Server pServer;
    private static Logger logger = Logger.getLogger("Server");
    private static boolean forClient = false;
    private int connectedServerId;

    public ServerThread(Server server, Socket sock) {
        this.sock = sock;
        this.pServer = server;
        start();
    }

    public void run() {
        logger.debug("Server thread started by " + pServer);
        while (true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    logger.debug(line + " Message received");
                    deliver(line);
                    setTimeoutForEntropy();
                }
            } catch (SocketTimeoutException s) {
                pServer.startEntropy();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deliver(String msg) {
        if (msg.startsWith(Constants.UserAction)) {
            UserAction ua = UserAction.unStringify(msg);
            logger.debug(pServer + " User ACTION RECEIVED");
            performPlaylistAction(ua);
        } else if (msg.startsWith(Constants.ClientConnectAck)) {
            logger.debug(pServer + " Client ACK RECEIVED");
            ClientConnectAck cca = ClientConnectAck.unStringify(msg);
            pServer.addClientSocket(sock.getPort(), sock);
            forClient = true;
        } else if (msg.startsWith(Constants.ServerConnectAck)) {
            logger.debug(pServer + " Server ACK RECEIVED");
            ServerConnectAck cca = ServerConnectAck.unStringify(msg);
            pServer.addServerSocket(cca.srcId, sock);
            connectedServerId = cca.srcId;
            forClient = false;
        } else if(msg.startsWith(Constants.RequestEntropyMessage)){
            RequestEntropyMessage reqEntMsg = RequestEntropyMessage.unStringify(msg);
            logger.debug(pServer + " Entropy Request Received from server "+ reqEntMsg.srcId);
            pServer.respondToEntropyRequest(sock);
        }else if(msg.startsWith(Constants.EntropyReceiverMessage)){
            logger.debug(pServer + " Entropy Response Received");
            EntropyReceiverMessage reqEntMsg = EntropyReceiverMessage.unStringify(msg);
            pServer.startSendingEntropyResponse(sock, reqEntMsg);
        }else if(msg.startsWith(Constants.EntropyWriteMessage)){
            EntropyWriteMessage reqEntMsg = EntropyWriteMessage.unstringify(msg);
            //pServer.startSendingEntropyResponse(sock, reqEntMsg);

        }else{
            logger.info(msg+" received");
        }

    }

    private void setTimeoutForEntropy() {
        try {
            sock.setSoTimeout(Constants.EntropyInverval);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void performPlaylistAction(UserAction ua) {
        if (Constants.ADD.equals(ua.action)) {
            logger.debug(this + " ADDING TO PLAYLIST");
            pServer.addPlaylist(ua.song, ua.url);
        } else if (Constants.EDIT.equals(ua.action))
            pServer.editPlaylist(ua.song, ua.url);
        else if (Constants.DELETE.equals(ua.action))
            pServer.deleteFromPlaylist(ua.song);
        else
            logger.error("unknown action");
    }
}
