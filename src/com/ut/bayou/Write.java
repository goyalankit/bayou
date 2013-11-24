package com.ut.bayou;

public class Write implements Comparable{
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
