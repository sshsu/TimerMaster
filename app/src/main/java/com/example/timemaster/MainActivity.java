package com.example.timemaster;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {

    private SimpleAdapter sa;
    private ListView lv;
    private List<messages> messageList = new ArrayList<messages>();
    private List<Map<String,Object> > messageList2 = new ArrayList<Map<String,Object> >();
    private List<messages> messageList3;//用于ORMLite 的演示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weichatlayout);
        for (int i = 1; i < 101; i++) {

            //添加数据
            messages m = new messages();
            m.setTou1(""+i);
            m.setUserName("TT"+i);
            m.setLastMessage( "一起去旅游");
            m.setDatetime("10月1日");
            messageList.add(m);//上周

        }

        //简单理解为VC绑在一起

// baseAdapter
        lv = (ListView)findViewById(R.id.listView1);

        lv.setAdapter( new BaseAdapter(){
            //返回多少条记录
            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return messageList.size();
            }
            //每一个item项，返回一次界面
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = null;
                //布局不变，数据变

                //如果缓存为空，我们生成新的布局作为1个item
                if(convertView==null){
                    Log.i("info:", "没有缓存，重新生成"+position);
                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                    //因为getView()返回的对象，adapter会自动赋给ListView
                    view = inflater.inflate(R.layout.listviewitems, null);
                    // view = inflater.inflate(R.layout.listview_item_layout, null);
                }else{
                    Log.i("info:", "有缓存，不需要重新生成"+position);
                    view = convertView;
                }
                messages m = messageList.get(position);
                TextView tv_userName = (TextView)view.findViewById(R.id.tv_userName);
                tv_userName.setText(  m.getUserName()  );
                tv_userName.setTextSize(15);

                TextView tv_lastMessage = (TextView)view.findViewById(R.id.tv_lastMessage);
                tv_lastMessage.setText(  m.getLastMessage()  );
                tv_lastMessage.setTextSize(12);

                TextView tv_datetime = (TextView)view.findViewById(R.id.tv_datetime);
                tv_datetime.setText( m.getDatetime()  );
                tv_datetime.setTextSize(12);

                return view;
            }

            @Override
            public Object getItem(int position) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getItemId(int position) {
                // TODO Auto-generated method stub
                return 0;
            }

        } );
    }

}
