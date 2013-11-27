package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ServerThread extends Thread {
    private Socket sock;
    private Server pServer;
    private static Logger logger = Logger.getLogger("Server");
    private static boolean forClient = false;
    private ServerId connectedServerId;

    public ServerThread(Server server, Socket sock) {
        this.sock = sock;
        this.pServer = server;
        start();
    }

    public void run() {
        logger.debug("Server thread started by " + pServer);


        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(sock.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Object line;
                while ((line = in.readObject()) != null) {
                    logger.debug(line + " Message received");
                    deliver(line);
                    //setTimeoutForEntropy();
                }
            } catch (SocketTimeoutException s) {
                pServer.startEntropy();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void deliver(Object msg) {
        if (msg instanceof UserAction) {
            UserAction ua = (UserAction) msg;
            logger.debug(pServer + " User ACTION RECEIVED");
            performPlaylistAction(ua);
        } else if (msg instanceof ClientConnectAck) {
            logger.debug(pServer + " Client ACK RECEIVED");
            ClientConnectAck cca = (ClientConnectAck) msg;
            pServer.addClientSocket(sock.getPort(), sock);
            forClient = true;
        } else if (msg instanceof ServerConnectAck) {
            logger.debug(pServer + " Server ACK RECEIVED");
            ServerConnectAck cca = (ServerConnectAck) msg;
            pServer.addServerSocket(cca.srcId, sock);
            pServer.setServerId(cca.serverId);
            connectedServerId = cca.srcId;
            forClient = false;
        } else if (msg instanceof RequestEntropyMessage) {
            RequestEntropyMessage reqEntMsg = (RequestEntropyMessage) msg;
            logger.debug(pServer + " Entropy Request Received from server " + reqEntMsg.srcId);
            pServer.respondToEntropyRequest(sock);
        } else if (msg instanceof EntropyReceiverMessage) {
            logger.debug(pServer + " Entropy Response Received");
            EntropyReceiverMessage reqEntMsg = (EntropyReceiverMessage) msg;
            pServer.startSendingEntropyResponse(sock, reqEntMsg);
        } else if (msg instanceof EntropyWriteMessage) {
            EntropyWriteMessage reqEntMsg = (EntropyWriteMessage) msg;
            pServer.processEntropyWriteMessage(reqEntMsg, sock);

        } else if(msg instanceof EntropyFinishedAck){
            EntropyFinishedAck efack = (EntropyFinishedAck) msg;
            pServer.finalizeEntropySession(efack);
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
