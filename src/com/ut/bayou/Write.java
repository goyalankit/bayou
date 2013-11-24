package com.ut.bayou;

public class Write {
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

}
