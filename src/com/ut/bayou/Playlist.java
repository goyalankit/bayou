package com.ut.bayou;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Playlist {
    private Map<String, String> collection;

    public Playlist(Map<String, String> map) {
        this.collection = Collections.synchronizedMap(new HashMap<String, String>());
    }

    public void add(String song, String url){
        collection.put(song, url);
    }

    public void edit(String song, String url) {
        collection.put(song, url);
    }

    public void delete(String song){
        collection.remove(song);
    }
}
