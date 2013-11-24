package com.ut.bayou;

import java.io.Serializable;

public class Message implements Serializable{
    Integer srcId;
}

class BeginEntropyMessage extends Message{
    BeginEntropyMessage(Integer srcId) {
        this.srcId = srcId;
    }
}


class AckBeginEntropy extends Message{

}

class UserAction extends Message{
    String action;
    String song;
    String url;

    UserAction(int clientId, String action, String song, String url) {
        this.srcId = clientId;
        this.action = action;
        this.song = song;
        this.url = url;
    }
}

class ConnectionAcceptAck extends Message{
    ConnectionAcceptAck(Integer sId) {
        this.srcId = sId;
    }
}