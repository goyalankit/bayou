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
    private boolean forClient = false;
    private ServerId connectedServerId;
    private int clientId;
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
                } catch (SocketException se) {
                    break;
                }
                catch (SocketTimeoutException s) {
                    break;
                } catch (IOException e) {
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if(forClient)
                cleanUpAfterClient();
            else
                cleanUpAfterServer();
            logger.error("SOCKET EXCEPTION");
        }
    }

    public void cleanUpAfterClient(){
        pServer.closeConnectionForClient(sock.getPort());
        try {
            in.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanUpAfterServer(){
        pServer.closeConnection(connectedServerId);
        try {
            in.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
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
            clientId = cca.clientId;
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

        } else if(msg instanceof EntropyFinished){
            EntropyFinished efack = (EntropyFinished) msg;
            pServer.finalizeEntropySession(efack);
        } else if(msg instanceof ClientDisconnect){
            logger.info("Client Disconnect Request Received");
            ClientDisconnect clientDisconnect = (ClientDisconnect) msg;
            pServer.respondToClientDisconnect(clientDisconnect);
        } else if(msg instanceof ServerDbStatus){
            logger.info("Client Requesting status");
            ServerDbStatus sdstatus = (ServerDbStatus) msg;
            pServer.respondWithStatus(sdstatus);
        } else if(msg instanceof EntropyFinishedAck){
            EntropyFinishedAck entFinAck = (EntropyFinishedAck) msg;
            pServer.entropyFinishedAck(entFinAck);
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
