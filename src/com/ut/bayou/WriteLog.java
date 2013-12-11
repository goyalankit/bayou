package com.ut.bayou;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class WriteLog {
    private SortedSet<Write> writes;

    public WriteLog(){
        writes = Collections.synchronizedSortedSet(new TreeSet<Write>());
    }

    public void addToLog(Write w){
        writes.add(w);
    }

    public Iterator<Write> iterator() {
        return writes.iterator();
    }

    public void removeWrite(Write w){
        Iterator<Write> it = writes.iterator();
        Write w1;
        while(it.hasNext()){
            w1 = it.next();
            if(w1.acceptStamp == w.acceptStamp && w1.sId.equals(w.sId)){
                System.out.println("INFO removing write from the writes list");
                it.remove();
            }
        }
    }

    @Override
    public String toString(){
        String s = "CSN          |AcceptStamp   | Command | Song | URL | ServerId";
        Iterator<Write> it = writes.iterator();
        while(it.hasNext()){
            Write w = it.next();
            s += "\n"+ w.csn + " "+ w.acceptStamp + " " + w.command + " " + w.song + " " + w.url + " " + w.sId.hrNumber;
        }
        return s;
    }

    public int size(){
        return writes.size();
    }
}
