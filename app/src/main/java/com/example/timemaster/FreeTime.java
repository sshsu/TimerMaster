package com.example.timemaster;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;

public class FreeTime {
    public int freeTime;
    public int freeTimeAllUsed;    //没有记录的freeTime的使用时间
    public int freeTimeCurrentUsed;     //当前一个中断下使用的Time
    public int duration;
    private TextView freeTimeview;
    private MainActivity mainActivity;
    public int start;
    public int currentTaskInfoRecordId;

    public int play_gap;

    FreeTime(MainActivity mainActivity, int freeTime, int freeTimeAllUsed){
        this.mainActivity = mainActivity;
        this.freeTime = freeTime;
        this.freeTimeAllUsed = freeTimeAllUsed;
        this.duration =  15 * 60 *GlobalVariable.timeDuraTion;
    }

    public String timeConvertSec(int time){
        int hour = time / 60 / 60 / GlobalVariable.timeDuraTion;
        int min = (time - hour * 60 * 60 * GlobalVariable.timeDuraTion ) / 60 / GlobalVariable.timeDuraTion;
        int sec = (time - hour * 60 * 60 * GlobalVariable.timeDuraTion - min * 60 * GlobalVariable.timeDuraTion ) / GlobalVariable.timeDuraTion;
        String timeStr = String.format("%02d",hour)+":"+String.format("%02d",min)+":"+String.format("%02d",sec);
        return timeStr;
    }

    public void update(int freeTime, int freeTimeAllUsed){
        this.freeTimeCurrentUsed = 0;
        this.freeTime = freeTime;
        this.freeTimeAllUsed = freeTimeAllUsed;
        updateView();
    }

    public void updateView(){

        TextView freeTimeTextView = mainActivity.findViewById(R.id.FreeTimeTv);
        TextView currentUsedTimeTextView = mainActivity.findViewById(R.id.CurrentUsedTime);
        TextView allUsedTimeTextView = mainActivity.findViewById(R.id.AllUsedTime);

        String freeTimeStr = timeConvertSec(freeTime);
        String currentUsedTimeStr = timeConvertSec(freeTimeCurrentUsed);
        String allUsedTimeStr = timeConvertSec(freeTimeAllUsed);


        freeTimeTextView.setText(freeTimeStr);
        currentUsedTimeTextView.setText(currentUsedTimeStr);
        allUsedTimeTextView.setText(allUsedTimeStr);

    }

    public void startInit(){
        if(start == 1)
            return;

        start = 1;
        freeTimeCurrentUsed = 0;
        play_gap = 0;

        //插入一個time_info記錄
        //INSERT INTO task_time_info(date, task_id, consume_time, start, end, info)
        //VALUES("2020-09-12", $task_id, 0, freeTimeCurrentUsed, 15:03:00, 0, 不詳);

        //查詢剛剛insert的id
        //SELECT id from task_time_info where date = "2020-09-12" and task_id = task_id and start = 15:03:00
        //currentTaskInfoRecordId = id;
    }

    public void stop(){
        if(start == 0)
            return;

        //將end更新到task_info表上
        //UPDATE task_time_info SET end = "15:06:00" WHERE task_time_info.id = currentTaskInfoRecordId
        //更新task_time的accumulate_time
        //
        start = 0;
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

    public void timeDec(int delta){
        this.freeTime -= delta;
        this.freeTimeAllUsed += delta;
        this.freeTimeCurrentUsed += delta;
        //freeTime 每使用15min提醒一次
        this.play_gap += delta;
        if(play_gap > 15 * 60 * GlobalVariable.timeDuraTion){
            GlobalVariable.soud_player.startVideoAndVibrator();
            play_gap = 0;
        }
    }

    public void reset(){
        this.freeTime = 0;
        this.freeTimeAllUsed = 0;    //没有记录的freeTime的使用时间
        this.freeTimeCurrentUsed = 0;     //当前一个中断下使用的Time
        this.start = 0;
        this.play_gap = 0;
    }

}
