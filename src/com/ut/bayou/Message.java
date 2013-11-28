package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.Serializable;

public class Message implements Serializable{
    ServerId srcId;
    static Logger logger = Logger.getLogger("Message");
    public String toString(){
        return "srcId="+srcId;
    }
}

class RequestEntropyMessage extends Message{ //Message to send a request to perform anti-entropy.
    RequestEntropyMessage(ServerId srcId) {
        this.srcId = srcId;
    }

    public String toString(){
        return  "RequestEntropyMessage"+Constants.Delimiter+""+srcId;
    }
}

class EntropyWriteMessage extends Message{ //Writes sent by sender to receiver in anti-entropy
    Write write;
    int seqNumber;
    EntropyWriteMessage(ServerId srcId, Write w, int seqNumber){
        this.srcId = srcId;
        this.write = w;
        this.seqNumber = seqNumber;
    }

    public String toString(){
        return  "EntropyWriteMessage"+Constants.Delimiter+srcId+Constants.SPACE+write.stringify();
    }

}

class EntropyReceiverMessage extends Message{ //Send your version vector after entropy request.
    VersionVector VV;
    long csn;

    EntropyReceiverMessage(ServerId sId, VersionVector v, long csn){
        this.srcId = sId;
        this.VV = v;
        this.csn = csn;
    }

    public String toString(){
        String s = "";
        s += "EntropyReceiverMessage"+Constants.Delimiter+srcId
                +Constants.SPACE+ VV.strigify()+Constants.SPACE+csn ;
        return s;
    }
}

class EntropyFinishedAck extends Message{
    int numOfMessages;

    EntropyFinishedAck(ServerId sId, int numOfMessages) {
        this.srcId = sId;
        this.numOfMessages = numOfMessages;
    }
}

class ServerConnectAck extends Message{  //Server acknowledges to client after connecting
    ServerId serverId;
    ServerConnectAck(ServerId sId, ServerId serverId) {
        this.srcId = sId;
        this.serverId = serverId;
    }

    public String toString(){
        return  "ServerConnectAck"+Constants.Delimiter+""+srcId;
    }
}

class ClientConnectAck extends Message{ //Client acknowledges to server after connecting
    int clientId;
    ClientConnectAck(int clientId) {
        this.clientId = clientId;
    }

    public String toString(){
        return  "ClientConnectAck"+Constants.Delimiter+""+srcId;
    }
}

class ClientDisconnect extends Message{
    int clientId;
    ClientDisconnect(int clientId){
        this.clientId = clientId;
    }
}

class ClientDisconnectServerResponse extends Message{
    long currentAcceptTime;
    ClientDisconnectServerResponse(ServerId sid, long currentAcceptTime){
        this.srcId = sid;
        this.currentAcceptTime = currentAcceptTime;
    }

}

class ServerDbStatus extends Message{
    int clientId;
    ServerId lastServer;
    long lastAcceptedTimestamp;

    ServerDbStatus (int clientId, ServerId lastServer, long lastAcceptedTimestamp){
        this.clientId = clientId;
        this.lastServer = lastServer;
        this.lastAcceptedTimestamp = lastAcceptedTimestamp;
    }
}

class ServerDbStatusResponse extends Message{
    boolean updated;
    Playlist playlist;
    ServerDbStatusResponse (ServerId serverId, boolean updated, Playlist playlist){
        this.srcId = serverId;
        this.updated = updated;
        this.playlist = playlist;
    }
}


class UserAction extends Message{ //Client to server user action propagation message.
    String action;
    String song;
    String url;
    int ClientSrc;

    UserAction(int clientId, String action, String song, String url) {
        this.ClientSrc = clientId;
        this.srcId = null;
        this.action = action;
        this.song = song;
        this.url = url;
    }

    public String toString(){
        String s = "";
        s = "UserAction"+Constants.Delimiter +
                srcId +Constants.SPACE+
                action +Constants.SPACE+
                song +Constants.SPACE+url;

        return s;
    }
}