package io.github.ningwy.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.R;
import io.github.ningwy.domain.TaskInfo;
import io.github.ningwy.engine.TaskInfoProvider;
import io.github.ningwy.utils.SystemInfoUtils;

public class TaskManagerActivity extends AppCompatActivity {

    private TextView tv_process_count;
    private TextView tv_ram;
    private TextView tv_task_status;
    private LinearLayout ll_task_loading;
    private ListView lv_task;

    private List<TaskInfo> taskInfoList;
    private List<TaskInfo> userTaskList;
    private List<TaskInfo> systemTaskList;

    private TaskInfo taskInfo;

    /**
     * 运行中进程个数
     */
    private int runningProcessCount;
    /**
     * 可用内存
      */
    private long availRam;
    /**
     * 总内存
     */
    private long totalRam;

    private TaskManagerAdapter adapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter = new TaskManagerAdapter();
            lv_task.setAdapter(adapter);
            ll_task_loading.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager);

        init();
        fillData();

        lv_task.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = lv_task.getItemAtPosition(position);
                if (obj != null) {
                    taskInfo = (TaskInfo) obj;
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_task_status);
                    if (taskInfo.isChecked()) {
                        taskInfo.setChecked(false);
                        checkBox.setChecked(false);
                    } else {
                        taskInfo.setChecked(true);
                        checkBox.setChecked(true);
                    }
                }
            }
        });

        lv_task.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (userTaskList == null || systemTaskList == null) {
                    return;
                }
                if (firstVisibleItem <= userTaskList.size()) {
                    tv_task_status.setText("用户进程(" + userTaskList.size() + ")");
                } else {
                    tv_task_status.setText("系统进程(" + systemTaskList.size() + ")");
                }
            }
        });

    }

    /**
     * 获取数据
     */
    private void fillData() {
        ll_task_loading.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                super.run();
                taskInfoList = TaskInfoProvider.getAllTask(TaskManagerActivity.this);
                userTaskList = new ArrayList<>();
                systemTaskList = new ArrayList<>();
                for (TaskInfo taskInfo : taskInfoList) {
                    if (taskInfo.isUser()) {
                        userTaskList.add(taskInfo);
                    } else {
                        systemTaskList.add(taskInfo);
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * 初始化
     */
    private void init() {
        tv_process_count = (TextView) findViewById(R.id.tv_process_count);
        tv_ram = (TextView) findViewById(R.id.tv_ram);
        tv_task_status = (TextView) findViewById(R.id.tv_task_status);
        ll_task_loading = (LinearLayout) findViewById(R.id.ll_task_loading);
        lv_task = (ListView) findViewById(R.id.lv_task);

        //给TextView填上希望显示的数据和文字
        runningProcessCount = SystemInfoUtils.getRunningProcessCount(this);
        availRam = SystemInfoUtils.getAvailRam(this);
        totalRam = SystemInfoUtils.getTotalRam(this);
        String availRamStr = Formatter.formatFileSize(this, availRam);
        String totalRamStr = Formatter.formatFileSize(this, totalRam);
        tv_process_count.setText("进程数:" + runningProcessCount);
        tv_ram.setText("剩余/总内存:" + availRamStr + "/" + totalRamStr);

        taskInfoList = new ArrayList<>();
    }

    private class TaskManagerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
            boolean showSystemProcess = sp.getBoolean("showSystemProcess", true);
            if (showSystemProcess) {
                return userTaskList.size() + 1 + systemTaskList.size() + 1;
            } else {
                return userTaskList.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            TaskInfo taskInfo;
            if (position == 0) {
                return null;
            } else if (position == userTaskList.size() + 1) {
                return null;
            } else if (position <= userTaskList.size()) {
                taskInfo = userTaskList.get(position - 1);
            } else {
                taskInfo = systemTaskList.get(position - userTaskList.size() - 2);
            }
            return taskInfo;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView != null && convertView instanceof LinearLayout) {
                viewHolder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(TaskManagerActivity.this, R.layout.task_manager_item, null);
                viewHolder = new ViewHolder();
                viewHolder.iv_task_icon = (ImageView) convertView.findViewById(R.id.iv_task_icon);
                viewHolder.tv_task_name = (TextView) convertView.findViewById(R.id.tv_task_name);
                viewHolder.tv_mem_size = (TextView) convertView.findViewById(R.id.tv_mem_size);
                viewHolder.cb_task_status = (CheckBox) convertView.findViewById(R.id.cb_task_status);
                convertView.setTag(viewHolder);
            }

            TaskInfo taskInfo;
            if (position == 0) {
                TextView tv = new TextView(TaskManagerActivity.this);
                tv.setText("用户进程(" + userTaskList.size() + ")");
                tv.setTextSize(16);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                return tv;
            } else if (position == userTaskList.size() + 1) {
                TextView tv = new TextView(TaskManagerActivity.this);
                tv.setText("系统进程(" + systemTaskList.size() + ")");
                tv.setTextSize(16);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                return tv;
            } else if (position <= userTaskList.size()) {
                taskInfo = userTaskList.get(position - 1);
            } else {
                taskInfo = systemTaskList.get(position - userTaskList.size() - 2);
            }

            if (getPackageName().equals(taskInfo.getPackageName())) {
                //说明该item是手机卫士，把CheckBox去掉
                viewHolder.cb_task_status.setVisibility(View.GONE);
            }

            viewHolder.iv_task_icon.setImageDrawable(taskInfo.getIcon());
            viewHolder.tv_task_name.setText(taskInfo.getTaskName());
            viewHolder.tv_mem_size.setText(Formatter.formatFileSize(TaskManagerActivity.this, taskInfo.getMemSize()));
            viewHolder.cb_task_status.setChecked(taskInfo.isChecked());

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView iv_task_icon;
        TextView tv_task_name;
        TextView tv_mem_size;
        CheckBox cb_task_status;
    }

    //Button的点击事件——全选
    public void selectAll(View view) {
        for (TaskInfo taskInfo : userTaskList) {
            taskInfo.setChecked(true);
        }
        for (TaskInfo taskInfo : systemTaskList) {
            taskInfo.setChecked(true);
        }
        adapter.notifyDataSetChanged();
    }

    //Button的点击事件——反选
    public void invertSelect(View view) {
        for (TaskInfo taskInfo : userTaskList) {
//            if (taskInfo.isChecked()) {
//                taskInfo.setChecked(false);
//            } else {
//                taskInfo.setChecked(true);
//            }
            taskInfo.setChecked(!taskInfo.isChecked());
        }
        for (TaskInfo taskInfo : systemTaskList) {
//            if (taskInfo.isChecked()) {
//                taskInfo.setChecked(false);
//            } else {
//                taskInfo.setChecked(true);
//            }
            taskInfo.setChecked(!taskInfo.isChecked());//很明显这个方法要优雅很多
        }
        adapter.notifyDataSetChanged();//重新调用BaseAdapter中的getCount和getView方法
    }

    //Button的点击事件——一键清理
    public void clearAll(View view) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        int killCount = 0;//杀死的进程数
        List<TaskInfo> killTaskInfos = new ArrayList<>();//杀死的进程组成的List
        long spareRam = 0;//杀死进程释放的内存

        //遍历用户进程
        for (TaskInfo taskInfo : userTaskList) {

            if (getPackageName().equals(taskInfo.getPackageName())) {
                //说明是手机卫士，直接跳出，继续循环下去
                continue;
            }

            if (taskInfo.isChecked()) {
                am.killBackgroundProcesses(taskInfo.getPackageName());
                killCount ++;
                killTaskInfos.add(taskInfo);
                spareRam += taskInfo.getMemSize();
            }
        }
        //遍历系统进程
        for (TaskInfo taskInfo : systemTaskList) {
            if (taskInfo.isChecked()) {
                am.killBackgroundProcesses(taskInfo.getPackageName());
                killCount ++;
                killTaskInfos.add(taskInfo);
                spareRam += taskInfo.getMemSize();
            }
        }

        for (TaskInfo taskInfo : killTaskInfos) {
            if (taskInfo.isUser()) {
                userTaskList.remove(taskInfo);
            } else {
                systemTaskList.remove(taskInfo);
            }
        }

        runningProcessCount -= killCount;
        availRam += spareRam;
        tv_process_count.setText("进程数:" + runningProcessCount);
        tv_ram.setText("剩余/总内存:" + Formatter.formatFileSize(this, availRam) + "/" + Formatter.formatFileSize(this, totalRam));

        adapter.notifyDataSetChanged();

//        fillData();
    }

    //Button的点击事件——设置
    public void setting(View view) {
        Intent intent = new Intent(this, TaskManagerSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.notifyDataSetChanged();
    }
}
