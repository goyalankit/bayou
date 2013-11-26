package com.ut.bayou;

import java.util.Scanner;

public class ServerId {
    long iSTimestamp;
    long iSId;
    int hrNumber;

    ServerId(long iSTimestamp, long iSId, int hrNumber){
        this.iSId = iSId;
        this.iSTimestamp = iSTimestamp;
        this.hrNumber = hrNumber;
    }

    public String stringify(){
        return iSTimestamp+Constants.SPACE+iSId+Constants.SPACE+hrNumber;
    }

    public static ServerId unStringify(String s){
        Scanner scanner = new Scanner(s);
        return new ServerId(scanner.nextLong(), scanner.nextLong(),scanner.nextInt());
    }
}
