package com.example.timemaster;

import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;

public class Task{
    public int start;                            //当前任务是否执行

    private String taskName;                      //当前任务类型
    private int taskDuration;                   //任务的事时间包长度，毫秒
    private int taskFinishCountDown;            //当前执行的任务时间包倒计时
    private int taskRunTime;                    //
    private int taskFinishNum;                    //完成的任务事件包的个数
    private int taskDestFinishNum;                //预计要完成的任务包
    private int accumulateTime;                  //总共完成的时间
    private int wonderCount;                    //当前任务中的走神次数
    private int wonderSum;                      //走神的总数

    private FreeTime freeTime;                     //未规划的自由支配时间

    public Task(String taskName, int taskDuration, int taskDestFinishNum, FreeTime freeTime) {
        this.taskName = taskName;
        this.taskDuration = taskDuration;
        this.taskFinishCountDown = 0;
        this.taskRunTime = 0;
        this.taskFinishNum = 0;
        this.accumulateTime = 0;
        this.taskDestFinishNum = taskDestFinishNum;
        this.freeTime = freeTime;
        freeTime.borrowTime(taskDuration * taskDestFinishNum);
    }

    public void setTaskFinishNum(int taskFinishNum) {
        this.taskFinishNum = taskFinishNum;
    }

    public int getAccumulateTime() {
        return accumulateTime;
    }

    public void setAccumulateTime(int accumulateTime) {
        this.accumulateTime = accumulateTime;
    }


    public Task(int start) {
        this.start = start;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDuration(){
        return "周期: "+ timeConvertSec(taskDuration);
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String timeConvertSec(int time){
        int hour = time / 60 / 60 / GlobalVariable.timeDuraTion;
        int min = (time - hour * 60 * 60 * GlobalVariable.timeDuraTion ) / 60 / GlobalVariable.timeDuraTion;
        int sec = (time - hour * 60 * 60 * GlobalVariable.timeDuraTion - min * 60 * GlobalVariable.timeDuraTion ) / GlobalVariable.timeDuraTion;

        String timeStr = String.format("%02d",hour)+":"+String.format("%02d",min)+":"+String.format("%02d",sec);
        return timeStr;
    }

    public String getRunTimeStr(){
        double percentage = 100.0d * taskRunTime / taskDuration;
        return  "当前: "+timeConvertSec(taskRunTime) + String.format("  %.2f", percentage)+"%";
    }

    public String getFinishNum(){
       return "完成: " + String.valueOf(taskFinishNum) +"/" + String.valueOf(taskDestFinishNum);
    }

    public String getAccumulateTimeStr(){
        return  timeConvertSec(accumulateTime);
    }

    public void clickStart(){
        if(start == 0) {
            start = 1;
            wonderCount = 0;
            if(taskFinishNum >= taskDestFinishNum){
                //从freeTime借时间
                freeTime.borrowTime(this.taskDuration);
                freeTime.updateView();
            }
        }
        else
            start = 0;
    }

    public void clickFinish(){
        if(start == 0)
            return;
        start = 0;
        taskFinishNum++;
        int leaveTime = taskDuration - taskRunTime;
        freeTime.giveTime(leaveTime);
        freeTime.updateView();
        taskRunTime = 0;
    }

    public void clickCancel(){
        if(start == 0)
            return;
        start = 0;
        freeTime.borrowTime(taskRunTime);
        freeTime.updateView();
        taskRunTime = 0;
    }

    public String getStartStatusString(){
        if(start == 0)
            return "开始";
        else
            return "暂停";
    }

    public void taskTimeInc(){
        if(start == 0)
            return;

        taskRunTime = taskRunTime + GlobalVariable.timeDuraTion;
        accumulateTime = accumulateTime + GlobalVariable.timeDuraTion;
        if(taskRunTime >= taskDuration){
            taskRunTime = 0;
            taskFinishNum = taskFinishNum + 1;
            start = 0;
        }
    }

    public void wonderInc(){
        wonderCount++;
        wonderSum++;
    }

    public void taskTimeAdd(int amount){
        taskRunTime = taskRunTime + amount;
    }

    public void taskTimeDec(){
        taskRunTime = taskRunTime - taskDuration;
    }

    public void taskTimeSub(int amount){
        taskRunTime = taskRunTime - amount;
    }
}


