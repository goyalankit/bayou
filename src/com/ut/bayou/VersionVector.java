package com.ut.bayou;

import java.util.HashMap;
import java.util.Scanner;

public class VersionVector {
    private HashMap<Integer, Long> vector;

    public VersionVector(){
        vector = new HashMap<Integer, Long>();
    }

    public void addNewServerEntry(int serverId, long timestamp){
        vector.put(serverId, timestamp);
    }

    public void updateMyAcceptStamp(int serverId, long newAcceptStamp){
        vector.put(serverId, newAcceptStamp);
    }

    public String strigify(){
        String s = "";
        int k = 0;
        for(Integer i : vector.keySet()){
            if(k==0)
                s +=  i + Constants.SPACE + vector.get(i);
            else
                s +=  Constants.SubDelimiter+""+i + Constants.SPACE + vector.get(i);
            k++;
        }
        return s;
    }

    public static VersionVector unStringify(String s1){
        String s[] = s1.split(Constants.SubDelimiter);
        VersionVector vv = new VersionVector();
        for(String t : s){
        Scanner scanner = new Scanner(t);
            vv.addNewServerEntry(scanner.nextInt(), scanner.nextLong());
        }
        return vv;
    }
}
