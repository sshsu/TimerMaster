package com.example.timemaster;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper {
    Context mcontext;
    private static String createTaskName="create table task_name(" +
            "id integer primary key autoincrement," +
            "name varchar(16))";

    private static String createTaskTime="create table task_time(" +
            "id integer primary key autoincrement," +
            "date varchar(16)," +
            "task_id integer," +
            "finish_num integer," +
            "accumulate_time integer)" ;

    private static String createTaskTimeInfo="create table task_time_info(" +
            "id integer primary key autoincrement," +
            "date varchar(16)," +
            "task_id integer," +
            "consume_time integer," +
            "start_time varchar(16)," +
            "end_time varchar(16)," +
            "info varchar(256)," +
            "running integer)" ;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mcontext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTaskName);
        Toast.makeText(mcontext,"task_name数据库已更新",Toast.LENGTH_SHORT).show();
        Log.i("info:", "task_name数据库已创建");
        db.execSQL(createTaskTime);
        Toast.makeText(mcontext,"task_time数据库已更新",Toast.LENGTH_SHORT).show();
        Log.i("info:", "task_time数据库已创建");
        db.execSQL(createTaskTimeInfo);
        Toast.makeText(mcontext,"task_time_info数据库已更新",Toast.LENGTH_SHORT).show();
        Log.i("info:", "task_time_info数据库已创建");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists task_name");
        db.execSQL("drop table if exists task_time");
        db.execSQL("drop table if exists task_time_info");
        onCreate(db);
        Log.i("info:", "task_time_info数据库已创建");
        Toast.makeText(mcontext,"数据库已更新",Toast.LENGTH_SHORT).show();
    }
}
