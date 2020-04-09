package com.example.timemaster;

import java.util.ArrayList;
import java.util.List;

public class MessageList {
    public List<Task> msgList = new ArrayList<Task>();
    public int enablePostion = -1;

    public void add(Task m){
        msgList.add(m);
    }

    public Task get(int index){
        if(index < 0)
            return null;
        return msgList.get(index);
    }

    public int size(){
        return msgList.size();
    }

    public Task getCurrentEnableMessage(){
        //确保原子性
        if(enablePostion < 0)
            return null;
        return msgList.get(enablePostion);
    }

}
