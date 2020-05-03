package com.example.timemaster;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ListView;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {

    private SimpleAdapter sa;
    private ListView lv;
    private MessageList messageList = new MessageList();
    private List<Map<String,Object> > messageList2 = new ArrayList<Map<String,Object> >();
    private List<Task> messageList3;//用于ORMLite 的演示
    private ListViewAttr listViewAttr = new ListViewAttr();
    public FreeTime freeTime;
    private MyAdapter myAdapter;

    private DBOperatorHelper DBhelper;
    private String today;
    private long last_unix_timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.weichatlayout);

        //初始化播放音频
        GlobalVariable.soud_player = SoundPoolUtils.getInstance(this, R.raw.finish);

        //初始化任务
        initTaskAndTime();

        //初始化数据库
        InitDataBaseAndTime();

        //设置listView的adapter
        lv = (ListView) findViewById(R.id.listView1);
        myAdapter = new MyAdapter(this, messageList, listViewAttr);
        lv.setAdapter(myAdapter);

        Log.i("info:", "enable task index is " + messageList.enablePostion);

        //开启update线程计时
        last_unix_timestamp = GlobalVariable.getUnixStamp();
        today = GlobalVariable.getTimeYYMMDD();
        UpdateThread updateTime = new UpdateThread();
        updateTime.start();

    }

    public void initTaskAndTime(){
        TextView freeTimeView = findViewById(R.id.FreeTimeTv);
        int timeDuration = GlobalVariable.timeDuraTion;
        freeTime = new FreeTime(this, 24 * 60 * 60 * GlobalVariable.timeDuraTion, 0);

        Task m1 = new Task("启动", 15 * 60 * timeDuration , 0, freeTime);
        Task m2 = new Task("读书", 30 * 60 * timeDuration , 2, freeTime);
        Task m3 = new Task("工作", 45 * 60 * timeDuration , 6, freeTime);
        Task m4 = new Task("探索", 45 * 60 * timeDuration , 2, freeTime);
        Task m5 = new Task("休息", 15 * 60 * timeDuration , 4, freeTime);
        Task m6 = new Task("复健", 30 * 60 * timeDuration , 1, freeTime);
        Task m7 = new Task("学习", 30 * 60 * timeDuration , 3, freeTime);
        Task m8 = new Task("爱好", 45 * 60 * timeDuration , 1, freeTime);
        Task m9 = new Task("冥想", 20 * 60 * timeDuration , 1, freeTime);
        Task m10 = new Task("玩手机", 15 * 60 * timeDuration , 0, freeTime);
        Task m11 = new Task("others", 15 * 60 * timeDuration , 0, freeTime);

        messageList.add(m1);
        messageList.add(m2);
        messageList.add(m3);
        messageList.add(m4);
        messageList.add(m5);
        messageList.add(m6);
        messageList.add(m7);
        messageList.add(m8);
        messageList.add(m9);
        messageList.add(m10);
        messageList.add(m11);

    }

    public void InitDataBaseAndTime(){
        //初始化数据库
        DBhelper = new DBOperatorHelper(this);
        GlobalVariable.DBhelpr = DBhelper;
        updateDBAndList();
    }

    public void updateDBAndList(){
        //查询task_name是否完成初始化，不存在的就创建
        DBhelper.taskNameInit(messageList);

        //查询task_time是否完成今日的task记录的初始化，不存在的就创建
        DBhelper.taskTimeInit(messageList);

        //将task_time_info消耗时间进行汇总到task_time，同时根据统计消息重设messageList
        DBhelper.updateTaskTimeAndListTime(messageList);

        //获取当前的时间和零点的时间差, 代表当前已经使用的时间
        UpdataFreeTime(freeTime, messageList);

    }

    //根据messageList更新freeTime
    public void UpdataFreeTime(FreeTime freeTime, MessageList messageList){
        //已用时间
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int min = now.get(Calendar.MINUTE);
        int sec = now.get(Calendar.SECOND);
        int now_dura = hour * 60 * 60 * GlobalVariable.timeDuraTion
                + min * 60 * GlobalVariable.timeDuraTion
                + sec * GlobalVariable.timeDuraTion;

        List<Task> msgList = messageList.msgList;
        int left_task_time = 0; //剩余任务时间
        int used_task_time = 0; //已完成的任务使用时间
        int borrow_time = 0;
        for(int i = 0;i < msgList.size(); ++i) {
            Task task = msgList.get(i);
            if(task.taskFinishNum < task.taskDestFinishNum){
                left_task_time = left_task_time + (task.taskDestFinishNum - task.taskFinishNum) * task.taskDuration;
            }
            else{
                if(task.taskRunTime > 0){
                    borrow_time = borrow_time + task.taskDuration;
                }
            }
            used_task_time = used_task_time + task.accumulateTime;
        }

        //剩余的free_time = 24 - 已过去的时间 - 还要花在任务上的时间 - 已借出的时间
        int free_time = 24 * 60 * 60 * GlobalVariable.timeDuraTion - now_dura - left_task_time - borrow_time;
        //使用过的freeTime等于 已过去的时间 - 已花在任务上的时间
        int freeTimeAllUsed = now_dura - used_task_time;
        freeTime.update(free_time, freeTimeAllUsed);
    }

    public void OnClickWonder(){
        Task task = messageList.get(messageList.enablePostion);
        task.wonderInc();
        myAdapter.notifyDataSetChanged();
    }


    class UpdateThread extends Thread{
        //实时计时更新线程
        public void run() {
            while (true) {
                try {
                    Thread.sleep(GlobalVariable.timeDuraTion );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        //Log.i("info:", "start to run update thread");
                            //跨日检测
                            String now = GlobalVariable.getTimeYYMMDD();
                            long unix_time_stamp = GlobalVariable.getUnixStamp();
                            int delta = (int)(unix_time_stamp - last_unix_timestamp);
                            last_unix_timestamp = unix_time_stamp;
                            if(today.equals(now) == false){
                                //如果跨日了，要重新初始化messageList和db
                                //msg reset
                                messageList.reset();
                                //db reset
                                updateDBAndList();

                                myAdapter.notifyDataSetChanged();
                                today = now;
                            }

                            if (messageList.enablePostion < 0) {
                                //总时间--
                                freeTime.startInit();
                                freeTime.timeDec(delta);
                                freeTime.updateView();
                                //update 总时间
                            } else {
                                freeTime.stop();
                                //启动了某个任务
                                Task m = messageList.get(messageList.enablePostion);
                                m.taskTimeInc(delta);
                                if(m.start == 0) {
                                    //该任务结束了
                                    messageList.enablePostion = -1;

                                }
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                });
            }
        }
    }
}
