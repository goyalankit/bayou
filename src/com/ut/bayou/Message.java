package com.ut.bayou;

public class Message {
    int srcId;
}

class BeginEntropyMessage extends Message{
    BeginEntropyMessage(int srcId) {
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