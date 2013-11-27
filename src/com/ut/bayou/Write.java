package com.ut.bayou;

import java.io.Serializable;

public class Write implements Comparable, Serializable{
    long acceptStamp;
    long csn;
    ServerId sId;
    String command;
    boolean committed;
    String song;
    String url;


    public Write(long acceptStamp, long csn, ServerId sId, boolean committed, String command, String song, String url){
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
        return diff != 0 ? diff : (sId.hrNumber - ((Write)second).sId.hrNumber);
    }
}
