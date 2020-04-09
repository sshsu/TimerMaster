package com.example.timemaster;
import android.app.Activity;
import android.util.Log;
import android.widget.ListView;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weichatlayout);

        //初始化任务
        initTaskAndTime();

        //设置listView的adapter
        lv = (ListView) findViewById(R.id.listView1);
        myAdapter = new MyAdapter(this, messageList, listViewAttr);
        lv.setAdapter(myAdapter);

        //开启update线程计时
        UpdateThread updateTime = new UpdateThread();
        updateTime.start();

    }

    public void initTaskAndTime(){
        TextView freeTimeView = findViewById(R.id.FreeTimeTv);
        int timeDuration = GlobalVariable.timeDuraTion;
        //获取当前的时间和零点的时间差, 代表当前已经使用的时间


        //查询所有的消耗的时间的总和

        //已使用的时间减去消耗的时间，有未记录的时间，消耗到freeTime

        freeTime = new FreeTime(freeTimeView, 24 * 60 * 60 * timeDuration, 0);

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

        messageList.enablePostion = -1;
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
                    Thread.sleep(GlobalVariable.timeDuraTion);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.i("info:", "start to run update thread");

                            if (messageList.enablePostion < 0) {
                                //总时间--
                                freeTime.timeDec();
                                freeTime.updateView();
                                //update 总时间
                            } else {
                                //启动了某个任务
                                Task m = messageList.get(messageList.enablePostion);
                                m.taskTimeInc();
                                if(m.start == 0)//该任务结束了
                                    messageList.enablePostion = -1;
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                });
            }
        }
    }
}
