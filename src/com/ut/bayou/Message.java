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

class RequestEntropyMessage extends Message{ //Message to send a request to perform anti-entropy.
    RequestEntropyMessage(int srcId) {
        this.srcId = srcId;
    }

    public String stringify(){
        return  "RequestEntropyMessage"+Constants.Delimiter+""+srcId;
    }

    public static RequestEntropyMessage unStringify(String s){
        String [] topLevel = s.split(Constants.Delimiter,2);
        if(!topLevel[0].equals(Constants.RequestEntropyMessage)){
            throw new ClassCastException();
        }
        return new RequestEntropyMessage(Integer.parseInt(topLevel[1]));
    }
}

class EntropyWriteMessage extends Message{ //Writes sent by sender to receiver in anti-entropy
    Write write;
    EntropyWriteMessage(int srcId, Write w){
        this.srcId = srcId;
        this.write = w;
    }

    public String stringify(){
        return  "EntropyWriteMessage"+Constants.Delimiter+srcId+Constants.SPACE+write.stringify();
    }

    public static EntropyWriteMessage unstringify(String s){
        String [] topLevel = s.split(Constants.Delimiter,2);
        EntropyWriteMessage ewm = new EntropyWriteMessage(-1, null);
        if(!topLevel[0].equals(Constants.EntropyWriteMessage)){
            throw new ClassCastException();
        }

        Scanner scanner = new Scanner(topLevel[1]);
        ewm.srcId = scanner.nextInt();
        ewm.write = Write.unStringify(scanner.next());
        return ewm;
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

    public String stringify(){
        String s = "";
        s += "EntropyReceiverMessage"+Constants.Delimiter+srcId
                +Constants.SPACE+ VV.strigify()+Constants.SPACE+csn ;
        return s;
    }

    public static EntropyReceiverMessage unStringify(String s){
        String [] topLevel = s.split(Constants.Delimiter,2);
        EntropyReceiverMessage erm = new EntropyReceiverMessage(-1, null, -1);
        if(!topLevel[0].equals(Constants.EntropyReceiverMessage)){
            throw new ClassCastException();
        }
        Scanner scanner = new Scanner(topLevel[1]);
        erm.srcId = scanner.nextInt();
        erm.VV = VersionVector.unStringify(scanner.next());
        erm.csn = scanner.nextInt();
        return erm;
    }

}


class ServerConnectAck extends Message{  //Server acknowledges to client after connecting
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

class ClientConnectAck extends Message{ //Client acknowledges to server after connecting
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