package com.ut.bayou;

import org.apache.log4j.Logger;

import java.util.Scanner;

public class Message {
    int srcId;
    static Logger logger = Logger.getLogger("Message");
    public String toString(){
        return "srcId="+srcId;
    }
}

class BeginEntropyMessage extends Message{
    BeginEntropyMessage(int srcId) {
        this.srcId = srcId;
    }
}


class ServerConnectAck extends Message{
    ServerConnectAck(int sId) {
        this.srcId = sId;
    }

    public String stringify(){
        return  "ServerConnectAck"+Constants.Delimiter+""+srcId;
    }

    public static ServerConnectAck unStringify(String s){
        String [] topLevel = s.split(Constants.Delimiter,2);
        if(!topLevel[0].equals(Constants.ServerConnectAck)){
            throw new ClassCastException();
        }
        return new ServerConnectAck(Integer.parseInt(topLevel[1]));
    }
}

class ClientConnectAck extends Message{
    ClientConnectAck(int sId) {
        this.srcId = sId;
    }

    public String stringify(){
        return  "ClientConnectAck"+Constants.Delimiter+""+srcId;
    }

    public static ClientConnectAck unStringify(String s){
        String [] topLevel = s.split(Constants.Delimiter,2);
        if(!topLevel[0].equals(Constants.ClientConnectAck)){
            throw new ClassCastException();
        }
        return new ClientConnectAck(Integer.parseInt(topLevel[1]));
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

    public String stringify(){
        String s = "";
        s = "UserAction"+Constants.Delimiter +
                srcId +Constants.SPACE+
                action +Constants.SPACE+
                song +Constants.SPACE+url;

        return s;
    }

    public static UserAction unStringify(String s){
        String [] topLevel = s.split(Constants.Delimiter,2);
        UserAction ua = new UserAction(-1, null, null, null);
        if(!topLevel[0].equals(Constants.UserAction)){
            throw new ClassCastException();
        }
        Scanner scanner = new Scanner(topLevel[1]);
        ua.srcId = scanner.nextInt();
        ua.action = scanner.next();
        ua.song = scanner.next();
        ua.url = scanner.next();

        return ua;
    }
}