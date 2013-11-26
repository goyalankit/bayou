package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.Serializable;

public class Message implements Serializable{
    int srcId;
    static Logger logger = Logger.getLogger("Message");
    public String toString(){
        return "srcId="+srcId;
    }
}

class RequestEntropyMessage extends Message{ //Message to send a request to perform anti-entropy.
    RequestEntropyMessage(int srcId) {
        this.srcId = srcId;
    }

    public String toString(){
        return  "RequestEntropyMessage"+Constants.Delimiter+""+srcId;
    }
}

class EntropyWriteMessage extends Message{ //Writes sent by sender to receiver in anti-entropy
    Write write;
    EntropyWriteMessage(int srcId, Write w){
        this.srcId = srcId;
        this.write = w;
    }

    public String toString(){
        return  "EntropyWriteMessage"+Constants.Delimiter+srcId+Constants.SPACE+write.stringify();
    }

}

class EntropyReceiverMessage extends Message{ //Send your version vector after entropy request.
    VersionVector VV;
    int csn;

    EntropyReceiverMessage(int sId, VersionVector v, int csn){
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


class ServerConnectAck extends Message{  //Server acknowledges to client after connecting
    ServerConnectAck(int sId) {
        this.srcId = sId;
    }

    public String toString(){
        return  "ServerConnectAck"+Constants.Delimiter+""+srcId;
    }
}

class ClientConnectAck extends Message{ //Client acknowledges to server after connecting
    ClientConnectAck(int sId) {
        this.srcId = sId;
    }

    public String toString(){
        return  "ClientConnectAck"+Constants.Delimiter+""+srcId;
    }
}

class UserAction extends Message{ //Client to server user action propagation message.
    String action;
    String song;
    String url;

    UserAction(int clientId, String action, String song, String url) {
        this.srcId = clientId;
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