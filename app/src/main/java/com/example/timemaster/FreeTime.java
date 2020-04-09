package com.example.timemaster;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class FreeTime {
    public int freeTime;
    public int freeTimeUsed;    //没有记录的freeTime的使用时间
    public int duration;
    private TextView freeTimeview;

    FreeTime(TextView freeTimeview, int freeTime, int freeTimeUsed){
        this.freeTimeview = freeTimeview;
        this.freeTime = freeTime;
        this.freeTimeUsed = freeTimeUsed;
    }

    public String timeConvertSec(int time){
        int hour = time / 60 / 60 / GlobalVariable.timeDuraTion;
        int min = (time - hour * 60 * 60 * GlobalVariable.timeDuraTion ) / 60 / GlobalVariable.timeDuraTion;
        int sec = (time - hour * 60 * 60 * GlobalVariable.timeDuraTion - min * 60 * GlobalVariable.timeDuraTion ) / GlobalVariable.timeDuraTion;
        String timeStr = String.format("%02d",hour)+":"+String.format("%02d",min)+":"+String.format("%02d",sec);
        return timeStr;
    }


    public void updateView(){
        String freeTimeStr = timeConvertSec(freeTime);
        freeTimeview.setText(freeTimeStr);
    }

    //
    public void borrowTime(int amount){
        this.freeTime -= amount;
    }

    public void giveTime(int amount){
        this.freeTime += amount;
    }

    public void timeInc(){
        this.freeTime += GlobalVariable.timeDuraTion;
    }

    public void timeDec(){
        this.freeTime -= GlobalVariable.timeDuraTion;
    }

}
