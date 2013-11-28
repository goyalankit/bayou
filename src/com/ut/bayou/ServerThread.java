package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;

public class ServerThread extends Thread {
    private Socket sock;
    private Server pServer;
    private static Logger logger = Logger.getLogger("Server");
    private static boolean forClient = false;
    private ServerId connectedServerId;
    ObjectInputStream in = null;

    public ServerThread(Server server, Socket sock) {
        this.sock = sock;
        this.pServer = server;
        start();
    }

    public ServerThread(Server server, Socket sock, ServerId connectedServerId) {
        this.sock = sock;
        this.pServer = server;
        this.connectedServerId = connectedServerId;
        start();
    }

    public void run() {
        logger.debug("Server thread started by " + pServer);



        try {
            in = new ObjectInputStream(sock.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                try {
                    Object line;
                    while ((line = in.readObject()) != null) {
                        logger.debug(line + " Message received");
                        deliver(line);
                    }
                } catch (SocketTimeoutException s) {
                    pServer.startEntropy(); //START ENTROPY
                    setTimeoutForEntropy();
                } catch (SocketException se) {
                    //se.printStackTrace();
                    break;
                } catch (IOException e) {
                    //e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            cleanUpAfterServer();
            logger.error("SOCKET EXCEPTION");
        }
    }

    public void cleanUpAfterServer(){
        pServer.closeConnection(connectedServerId);
        try {
            in.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            setTimeoutForEntropy();
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
            pServer.setCanEntropy(false);
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

            Random r = new Random();
            int timeout = r.nextInt(5000) + Constants.EntropyInverval;
            logger.info(pServer + "Setting socket timeout to be " + timeout);
            sock.setSoTimeout(timeout);
        } catch (SocketException e) {
            logger.error("Socket exception");
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
