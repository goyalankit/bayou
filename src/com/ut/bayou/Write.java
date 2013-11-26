package com.ut.bayou;

import java.io.Serializable;
import java.util.Scanner;

public class Write implements Comparable, Serializable{
    private long acceptStamp;
    private long csn;
    private int sId;
    private String command;
    private boolean committed;
    private String song;
    private String url;


    public Write(long acceptStamp, long csn, int sId, boolean committed, String command, String song, String url){
        this.acceptStamp = acceptStamp;
        this.csn = csn;
        this.sId = sId;
        this.committed = committed;
        this.command = command;
        this.song = song;
        this.url = url;
    }

    public String stringify(){
        return acceptStamp+Constants.SPACE+csn+Constants.SPACE+sId+Constants.SPACE
                +command+Constants.SPACE+committed+Constants.SPACE+song+Constants.SPACE+url;
    }

    public static Write unStringify(String s){
        Scanner scanner = new Scanner(s);
        return new Write(scanner.nextLong(), scanner.nextLong(),scanner.nextInt(),
                scanner.nextBoolean(), scanner.next(), scanner.next(), scanner.next());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Write)) return false;

        Write write = (Write) o;

        if (acceptStamp != write.acceptStamp) return false;
        if (committed != write.committed) return false;
        if (csn != write.csn) return false;
        if (sId != write.sId) return false;
        if (!command.equals(write.command)) return false;
        if (!song.equals(write.song)) return false;
        if (url != null ? !url.equals(write.url) : write.url != null) return false;

        return true;
    }


    @Override
    public int compareTo(Object second)
    {
        Write secondWrite = (Write)second;
        if(secondWrite.committed && secondWrite.committed)
            return (int) (this.csn - secondWrite.csn);
        if(secondWrite.committed && !this.committed)
            return 1;
        if(!secondWrite.committed && this.committed)
            return -1;
        int diff = (int)(this.acceptStamp - ((Write)second).acceptStamp);
        return diff != 0 ? diff : sId - ((Write)second).sId;
    }
}
