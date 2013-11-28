package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Playlist implements Serializable{
    private Map<String, String> collection;
    private static Logger logger = Logger.getLogger("Playlist");

    public Playlist() {
        this.collection = Collections.synchronizedMap(new HashMap<String, String>());
    }

    public void add(String song, String url){
        collection.put(song, url);
    }

    public void edit(String song, String url) {
        if(collection.containsKey(song))
            collection.put(song, url);
        else
            logger.error("Entry doesn't exist");
    }

    public void delete(String song){
        if(collection.containsKey(song))
            collection.remove(song);
        else
            logger.error("Entry doesn't exist");
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
