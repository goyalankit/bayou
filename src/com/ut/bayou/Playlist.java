package com.ut.bayou;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Playlist {
    private Map<String, String> collection;
    private static Logger logger = Logger.getLogger("Playlist");

    public Playlist() {
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

    public void clear(){
        collection.clear();
    }

    public void printIt(){
        String s = "Playlist\n------------------";
        int counter = 1;
        for(String key:collection.keySet()){
            s += "\n" + counter + " " + collection.get(key);
            counter++;
        }
        logger.info(s);
    }

}
