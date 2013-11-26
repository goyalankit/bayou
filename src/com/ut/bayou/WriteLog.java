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
