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
                s +=  i + Constants.VVSPACE + vector.get(i);
            else
                s +=  Constants.SubDelimiter+""+i + Constants.VVSPACE+ vector.get(i);
            k++;
        }
        return s;
    }

    public String toString(){
        String s = "\nVersion Vector\n------------------\n";
        for(Integer i : vector.keySet()){
            s+= i + " " + vector.get(i) + "\n";
        }
        return s;
    }

    public static VersionVector unStringify(String s1){
        s1 = s1.replaceAll("_"," ");        ;
        String s[] = s1.split(Constants.SubDelimiter);
        VersionVector vv = new VersionVector();

        for(String t : s){
        Scanner scanner = new Scanner(t);
            if(scanner.hasNext())
                vv.addNewServerEntry(Integer.parseInt(scanner.next()), scanner.nextLong());
        }
        return vv;
    }
}
