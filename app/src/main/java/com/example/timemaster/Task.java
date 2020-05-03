package com.example.timemaster;

import android.os.Vibrator;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Timer;

import static java.lang.Math.abs;

public class Task{
    public int start;                                //当前任务是否执行

    public String taskName;                         //当前任务类型
    public int taskDuration;                        //任务的事时间包长度，毫秒
    public int taskFinishCountDown;                  //当前执行的任务时间包倒计时
    public int taskRunTime;                         //
    public int taskFinishNum;                       //完成的任务事件包的个数
    public int taskDestFinishNum;                   //预计要完成的任务包
    public int accumulateTime;                      //总共完成的时间
    public int wonderCount;                         //当前任务中的走神次数
    public int wonderSum;                           //走神的总数

    private FreeTime freeTime;                      //未规划的自由支配时间

    public int task_time_info_id = -1;             //当前的taskinfo的id
    public int task_id  = -1;                             //在task_name中的id
    public int task_time_id = -1;

    public int last_update_time_info_time = 0;          //上次更新last_update_time_info的时间

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

            //判断id 是否为负数，如果是表明还未创建该条记录
            if(this.task_time_info_id < 0) {
                //不存在则创建
                //插入一個time_info記錄
                //INSERT INTO task_time_info(date, task_id, consume_time, start, end, info)
                //VALUES("2020-09-12", $task_id, 0, freeTimeCurrentUsed, 15:03:00, 0, 不詳);

                String start_time =   GlobalVariable.getTimeYYMMDDHHMMSS();;

                this.task_time_info_id = GlobalVariable.DBhelpr.insert_task_time_info(this, start_time, "NULL");
            }

            GlobalVariable.DBhelpr.update_task_time_info(this.task_time_info_id, ""+this.taskRunTime,
                    null, "true");
        }
        else {
            //做一个update
            GlobalVariable.DBhelpr.update_task_time_info(this.task_time_info_id, ""+this.taskRunTime,
                                                null, "false");
            start = 0;
        }
    }

    public void clickFinish(){
        if(start == 0)
            return;

        //將end更新到task_time_info表上
        String end_time = GlobalVariable.getTimeYYMMDDHHMMSS();
        GlobalVariable.DBhelpr.update_task_time_info(this.task_time_info_id, ""+this.taskRunTime,
                end_time, "false");
        this.task_time_info_id = -1;


        start = 0;
        taskFinishNum++;
        int leaveTime = taskDuration - taskRunTime;
        freeTime.giveTime(leaveTime);   //将多余时间给freetime
        freeTime.updateView();
        taskRunTime = 0;

        //更新task_time的accumulate_time
        GlobalVariable.DBhelpr.update_task_time(this.task_time_id, this.taskFinishNum, this.accumulateTime);

    }

    public void clickCancel(){
        if(start == 0)
            return;
        start = 0;
        freeTime.borrowTime(taskRunTime);
        freeTime.updateView();
        taskRunTime = 0;

        if(task_time_info_id >= 0) {
            GlobalVariable.DBhelpr.delete_task_time_info(task_time_info_id);
            this.task_time_info_id = -1;
        }
    }

    public String getStartStatusString(){
        if(start == 0)
            return "开始";
        else
            return "暂停";
    }

    public void taskTimeInc(int delta){
        if(start == 0)
            return;

        taskRunTime = taskRunTime + delta;
        accumulateTime = accumulateTime + delta;

        //3秒刷一次task_time_info库
        int now = GlobalVariable.getTimeSec();
        if(abs(now - last_update_time_info_time) > 3) {
            GlobalVariable.DBhelpr.update_task_time_info(this.task_time_info_id,
                    "" + this.taskRunTime, null, "true");
            last_update_time_info_time = now;
        }

        if(taskRunTime >= taskDuration){
            String end_time = GlobalVariable.getTimeYYMMDDHHMMSS();
            GlobalVariable.DBhelpr.update_task_time_info(this.task_time_info_id,
                    "" + this.taskRunTime, end_time, "false");
            this.task_time_info_id = -1;
            last_update_time_info_time = now;

            taskFinishNum = taskFinishNum + 1;
            GlobalVariable.DBhelpr.update_task_time(task_id, taskFinishNum, accumulateTime);

            taskRunTime = 0;
            start = 0;
            //震动
            GlobalVariable.soud_player.startVideoAndVibrator();
        }
    }

    public void wonderInc(){
        wonderCount++;
        wonderSum++;
    }

    public void update(int taskRunTime, int taskFinishNum, int accumulateTime){
        this.taskRunTime = taskRunTime;
        this.taskFinishNum = taskFinishNum;
        this.accumulateTime = accumulateTime;
    }

    public void reset(){

        //更新task_time_info
        if(this.task_time_info_id >= 0) {
            String end_time = GlobalVariable.getTimeYYMMDDHHMMSS();
            GlobalVariable.DBhelpr.update_task_time_info(task_time_info_id, ""+taskRunTime,
                                                         end_time,"false");
        }

        //更新task_time
        //如果完成大于0.6则已算达成个数
        if(this.taskRunTime * 0.6 >= taskDuration)
            taskFinishNum++;
        GlobalVariable.DBhelpr.update_task_time(this.task_time_id, taskFinishNum, accumulateTime);

        //重设数据
        this.taskRunTime = 0;
        this.taskFinishNum = 0;
        this.accumulateTime = 0;
        this.start = 0;
        this.task_time_info_id = -1;
        this.task_time_id = -1;
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


