package com.ut.bayou;

import java.io.Serializable;
import java.util.HashMap;

public class VersionVector implements Serializable {
    private HashMap<ServerId, Long> vector;

    public VersionVector(){
        vector = new HashMap<ServerId, Long>();
    }

    public void addNewServerEntry(ServerId serverId, long timestamp){
        vector.put(serverId, timestamp);
    }

    public void updateAcceptStamp(ServerId serverId, long newAcceptStamp){
        if(vector.containsKey(serverId)){
            if(newAcceptStamp > vector.get(serverId))
                vector.put(serverId, newAcceptStamp);
        }
        else{
            System.err.println("Something is wrong with version vector.." + serverId.hrNumber);
        }
    }

    public synchronized long getLatestStamp(ServerId sid){
        return vector.get(sid);
    }

    public boolean hasServerId(ServerId sid){
        return vector.containsKey(sid);
    }

    public String strigify(){
        String s = "";
        int k = 0;
        for(ServerId i : vector.keySet()){
            if(k==0)
                s +=  i.hrNumber + Constants.VVSPACE + vector.get(i);
            else
                s +=  Constants.SubDelimiter+""+i.hrNumber + Constants.VVSPACE+ vector.get(i);
            k++;
        }
        return s;
    }

    public String toString(){
        String s = "\nVersion Vector\n------------------\n";
        for(ServerId i : vector.keySet()){
            s+= i.hrNumber + " " + vector.get(i) + "\n";
        }
        return s;
    }
}
