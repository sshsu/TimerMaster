package com.example.timemaster;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
    //上下文
    MessageList messageList;
    public ListViewAttr listViewAttr;
    private LayoutInflater mInflater;
    int i = 0;

    public MyAdapter(Context context, MessageList messageList, ListViewAttr listViewAttr) {
        this.messageList = messageList;
        this.mInflater = LayoutInflater.from(context);
        this.listViewAttr = listViewAttr;
    }

    //返回多少条记录
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        Log.i("info:", "messageList 的队列的长度: "+messageList.size());
        return messageList.size();
    }
    //每一个item项，返回一次界面
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //TODO lock updateViewLock 为了防止timeupdate更新数据时而此处异步更新了区间

        //布局不变，数据变
        View view = null;
        MyListener myListener=null;
        Task m = messageList.get(position);

        if(convertView == null){
            //如果缓存view为空，我们生成新的布局作为1个item
            //LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            //因为getView()返回的对象，adapter会自动赋给ListView
            view = mInflater.inflate(R.layout.listviewitems, null);
            ++listViewAttr.viewCount;

            Log.i("info:", "没有缓存，重新生成"+position+"对象的hash code: "+ view.hashCode()+" 当前对象数: "+i);

            myListener = new MyListener(position);

            view.setTag(myListener);
        }else{
            Log.i("info:", "有缓存，不需要重新生成"+position+" 对象的hash code: "+convertView.hashCode());
            myListener = (MyListener)convertView.getTag();
            myListener.mPosition = position;
            view = convertView;
        }

        //获取组件
        TextView tv_taskName = (TextView)view.findViewById(R.id.taskName);
        TextView tv_taskDuration = (TextView)view.findViewById(R.id.taskDuration);
        TextView tv_taskRunTime = (TextView)view.findViewById(R.id.taskRunTime);
        TextView tv_taskFinishNum = (TextView)view.findViewById(R.id.taskFinishNum);
        TextView tv_accumulateTime = (TextView)view.findViewById(R.id.accumulateTime);
        Button startButton = (Button)view.findViewById(R.id.startButton);
        Button finishButton = (Button)view.findViewById(R.id.finishButton);
        Button cancelButton = (Button)view.findViewById(R.id.cancelButton);


        //更新数据
        tv_taskName.setText( m.getTaskName() );
        tv_taskDuration.setText(m.getTaskDuration());
        tv_taskRunTime.setText(m.getRunTimeStr());
        tv_taskFinishNum.setText(m.getFinishNum());
        tv_accumulateTime.setText(m.getAccumulateTimeStr());
        startButton.setText(m.getStartStatusString());
        startButton.setOnClickListener(myListener);
        finishButton.setOnClickListener(myListener);
        cancelButton.setOnClickListener(myListener);

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

       private class MyListener implements View.OnClickListener {
          int mPosition;


          public MyListener(int inPosition){
              mPosition= inPosition;
          }
          @Override
          public void onClick(View v) {
              Task m = messageList.get(mPosition);
              if(v.getId() == R.id.startButton) {
                  Log.i("info:", "click start");
                  m.clickStart();
                  if (messageList.enablePostion < 0) {
                      messageList.enablePostion = mPosition;
                  } else {
                      if (messageList.enablePostion == mPosition) {
                          messageList.enablePostion = -1;
                      } else {
                          Task oldEnableMessage = messageList.get(messageList.enablePostion);
                          oldEnableMessage.clickStart();
                          messageList.enablePostion = mPosition;
                      }
                  }
              }
              else if(v.getId() == R.id.finishButton){
                    m.clickFinish();

              }
              else if(v.getId() == R.id.cancelButton){
                    m.clickCancel();

              }
              notifyDataSetChanged();
          }
      }
}

