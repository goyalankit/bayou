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
        String s = "";
        Iterator<Write> it = writes.iterator();
        while(it.hasNext()){
            s += ""+it.next().stringify()+"\n"; //Todo add proper formatting here for viewing purposes.
        }
        return s;
    }

    public int size(){
        return writes.size();
    }
}
