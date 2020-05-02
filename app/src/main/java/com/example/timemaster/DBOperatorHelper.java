package com.example.timemaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import androidx.lifecycle.GenericLifecycleObserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DBOperatorHelper {
    public String dbName = "TimeMaster.db";
    public int dbVersion = 5;

    public DBHelper dbHelper;
    public SQLiteDatabase db;

    DBOperatorHelper(Context context){
        dbHelper = new DBHelper(context, dbName, null, dbVersion);
        db = dbHelper.getWritableDatabase();
    }

    //查询task_name是否完成初始化，不存在的就创建
    void taskNameInit(MessageList messageList){

        //遍历messageList的所有task_name
        List<Task> msgList = messageList.msgList;

        for(int i = 0;i < msgList.size(); ++i) {
            Task task = msgList.get(i);
            String task_name = task.getTaskName();
            Cursor cursor = db.query("task_name", null, "name = ?", new String[]{task_name},
                    null, null, null, null);

            if (cursor.moveToFirst() == false) {
                //该列名还未存在，创建
                ContentValues values = new ContentValues();
                values.put("name", task_name);
                db.insert("task_name", null, values);

                cursor = db.query("task_name", null, "name = ?", new String[]{task_name},
                        null, null, null, null);
                cursor.moveToFirst();

            }

            task.task_id = cursor.getInt(cursor.getColumnIndex("id"));
        }
     }

    //查询task_time是否完成今日的task记录的初始化，不存在的就创建, 必须先做taskNameInit
    void taskTimeInit(MessageList messageList){
        List<Task> msgList = messageList.msgList;
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
        int task_id;

        for(int i = 0;i < msgList.size(); ++i) {
            Task task = msgList.get(i);
            if(task.task_id < 0){
                continue;
            }

            task_id = task.task_id;


            //去task_time表查，是否有，
            Cursor cursor = db.query("task_time", null, "date = ? and task_id = ?",
                    new String[]{today, ""+task_id}, null, null, null, null);

            if (cursor.moveToFirst() == false) {
                //如果沒有insert
                ContentValues values = new ContentValues();
                values.put("date", today);
                values.put("task_id", task_id);
                values.put("finish_num", 0);
                values.put("accumulate_time", 0);
                if(db.insert("task_time", null, values) == -1)
                    Log.i("info:", "error insert");

                cursor = db.query("task_time", null, "date = ? and task_id = ?",
                        new String[]{today, ""+task_id}, null, null, null, null);
                cursor.moveToFirst();

            }
            task.task_time_id = cursor.getInt(cursor.getColumnIndex("id"));
        }
    }

    //将task_time_info消耗时间进行汇总到task_time，同时根据统计消息重设messageList
    void updateTaskTimeAndListTime(MessageList messageList){
            List<Task> msgList = messageList.msgList;
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
            int task_id;

            for(int i = 0;i < msgList.size(); ++i) {
                Task task = msgList.get(i);
                String task_name = task.getTaskName();

                //找到task的ID
                task_id = task.task_id;

                //去task_time_info表查，是否有，
                Cursor cursor = db.query("task_time_info", null, "date = ? and task_id = ?",
                        new String[]{today, "" + task_id}, null, null, null, null);

                //遍历所有的task_time_info，统计个数和总consume_time
                int consume_time_all = 0;
                int amount = 0;
                int running_time = 0, running_task = 0;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    consume_time_all = consume_time_all + cursor.getInt(cursor.getColumnIndex("consume_time"));
                    amount = amount + 1;

                    //找到当前运行的活动
                    if(cursor.getInt(cursor.getColumnIndex("running")) == 1) {
                        //将状态恢复到当前的task
                        running_time = cursor.getInt(cursor.getColumnIndex("consume_time"));
                        task.task_time_info_id = cursor.getInt(cursor.getColumnIndex("id"));
                        task.start = 1;
                        running_task = 1;
                        messageList.enablePostion = i;  //设置当前的运行的活动
                    }
                }

                //将accumulate_time和finish_num更新到task_time表上
                int finish_num = amount - running_task;
                int accumulate_time = consume_time_all - running_time;
                ContentValues values = new ContentValues();
                values.put("finish_num", finish_num);
                values.put("accumulate_time", accumulate_time);
                db.update("task_time", values,"date = ? and task_id = ?", new String[]{today, ""+task_id});

                //将consume_time_all和amount更新到当前的list上
                task.update(running_time, finish_num, accumulate_time + running_time);
            }
    }



    //插入一条task_time_info，并返回id
    public int insert_task_time_info(Task task, String startTime, String info){
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();

        Cursor cursor = db.query("task_time_info", null, "date = ? and task_id = ? and start_time = ?",
                new String[]{today, "" + task.task_id, startTime}, null, null, null, null);

        int id = 0;
        if(cursor.moveToFirst() == true){
            id = cursor.getInt(cursor.getColumnIndex("id"));
            return id;
        }

        ContentValues values = new ContentValues();
        values.put("date", today);
        values.put("task_id", task.task_id);
        values.put("start_time", startTime);
        values.put("running", 0);
        db.insert("task_time_info", null, values);

        cursor = db.query("task_time_info", null, "date = ? and task_id = ? and start_time = ?",
                new String[]{today, "" + task.task_id, startTime}, null, null, null, null);

        cursor.moveToFirst();
        id = cursor.getInt(cursor.getColumnIndex("id"));
        return id;
    }

    public void  delete_task_time_info(int id){
        db.delete("task_time_info", "id=?", new String[]{" "+id});
    }

    public void  update_task_time_info(int id, String consume_time, String end_time, String running) {
        Cursor cursor = db.query("task_time_info", null, "id=?", new String[]{""+id},
                null,null,null,null);
        if(cursor.moveToFirst() != true)
            return;
        String date =  cursor.getString(cursor.getColumnIndex("date"));

        ContentValues values = new ContentValues();

        if (consume_time != null)
            values.put("consume_time", Integer.parseInt(consume_time));

        if (end_time != null)
            values.put("end_time", end_time);

        if (running != null) {
            if (running.equals("true")) {
                values.put("running", 1);

                //将别的活动的running状态关闭
                cursor = db.query("task_time_info", null, "date = ? and running =?",
                        new String[]{date, ""+1}, null, null, null, null);
                if(cursor.moveToFirst() == true){
                    int queryed_id = cursor.getInt(cursor.getColumnIndex("id"));
                    update_task_time_info(queryed_id, null,null, "false");
                }
            }
            else
                values.put("running", 0);
        }
        db.update("task_time_info", values, "id=?", new String[]{" " + id});
    }

    public void update_task_time(int task_id, int finish_num, int accumulate_time){
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
        ContentValues values = new ContentValues();

        values.put("finish_num", finish_num);
        values.put("accumulate_time", accumulate_time);
        db.update("task_time", values, "date=? and task_id=?", new String[]{today, ""+task_id});
    }
}
