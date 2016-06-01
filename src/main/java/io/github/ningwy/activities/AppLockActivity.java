package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.R;
import io.github.ningwy.db.dao.AppLockDao;
import io.github.ningwy.domain.AppInfo;
import io.github.ningwy.engine.AppInfoProdiver;

public class AppLockActivity extends AppCompatActivity implements View.OnClickListener {

    private AppLockDao dao;

    private SharedPreferences sp;

    private TextView tv_left_unlock;
    private TextView tv_right_locked;
    private TextView tv_applock_status;
    private ListView lv_unlock;
    private ListView lv_locked;

    private List<AppInfo> appInfos;
    private List<AppInfo> unlockApps;
    private List<AppInfo> lockedApps;

    private AppLockAdapter unlockAdapter;
    private AppLockAdapter lockedAdapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            tv_applock_status.setText("未加锁软件:" + unlockApps.size());

            unlockAdapter = new AppLockAdapter(false);
            lockedAdapter = new AppLockAdapter(true);
            lv_unlock.setAdapter(unlockAdapter);
            lv_locked.setAdapter(lockedAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        init();

        tv_left_unlock.setOnClickListener(this);
        tv_right_locked.setOnClickListener(this);

        fillData();

    }

    /**
     * 填充数据
     */
    private void fillData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                appInfos = AppInfoProdiver.getAllAppInfo(AppLockActivity.this);
                for (AppInfo appInfo : appInfos) {
                    if (dao.isHasAppLock(appInfo.getPackageName())) {
                        lockedApps.add(appInfo);
                    } else {
                        unlockApps.add(appInfo);
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
        sp = getSharedPreferences("password", MODE_PRIVATE);
        dao = new AppLockDao(this);
        tv_left_unlock = (TextView) findViewById(R.id.tv_left_unlock);
        tv_right_locked = (TextView) findViewById(R.id.tv_right_locked);
        tv_applock_status = (TextView) findViewById(R.id.tv_applock_status);
        lv_unlock = (ListView) findViewById(R.id.lv_unlock);
        lv_locked = (ListView) findViewById(R.id.lv_locked);

        appInfos = new ArrayList<>();
        unlockApps = new ArrayList<>();
        lockedApps = new ArrayList<>();
    }

    private class AppLockAdapter extends BaseAdapter {

        private boolean flag = false;

        public AppLockAdapter(boolean flag) {
            this.flag = flag;
        }

        @Override
        public int getCount() {
            if (flag) {
                //已加锁
                return lockedApps.size();
            } else {
                //未加锁
                return unlockApps.size();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            ViewHolder viewHolder;
            if (convertView != null) {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(AppLockActivity.this, R.layout.app_lock_item, null);
                viewHolder = new ViewHolder();
                viewHolder.iv_app_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
                viewHolder.tv_app_name = (TextView) view.findViewById(R.id.tv_app_name);
                viewHolder.ll_app_lock = (LinearLayout) view.findViewById(R.id.ll_app_lock);
                viewHolder.iv_unlock = (ImageView) view.findViewById(R.id.iv_unlock);
                viewHolder.tv_unlock = (TextView) view.findViewById(R.id.tv_unlock);

                view.setTag(viewHolder);
            }
            final AppInfo appInfo;
            if (flag) {
                //已加锁
                appInfo = lockedApps.get(position);
                viewHolder.iv_unlock.setImageResource(R.drawable.locked);
                viewHolder.tv_unlock.setText("解锁");
            } else {
                //未加锁
                appInfo = unlockApps.get(position);
                viewHolder.iv_unlock.setImageResource(R.drawable.unlocked);
                viewHolder.tv_unlock.setText("加锁");
            }
            viewHolder.iv_app_icon.setImageDrawable(appInfo.getIcon());
            viewHolder.tv_app_name.setText(appInfo.getAppName());

            viewHolder.ll_app_lock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flag) {
                        //在已加锁ListView中
                        //添加平移动画
                        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, -1.0F,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0);
                        animation.setDuration(500);
                        view.startAnimation(animation);
                        /**
                         * 由于为view添加了动画，所以要延迟执行更新UI，否则动画则会显示下一个view
                         * 在执行动画。测试过SystemClock.sleep(500)来睡眠500毫秒延迟执行，不成功，
                         * 具体原因有待考究。
                         *
                         * 注意:该postDelayed方法中的线程，是附属于Handler中的，
                         * 也即是Handler在什么线程中，方法中的线程就在什么线程中。
                         * 该例子中因为Handler是在主线程中，故方法中的线程也在主线程中运行，
                         * 所以可以更新UI
                         *
                         * 下同
                         */
                        new Handler().postDelayed(new Thread(){
                            @Override
                            public void run() {
                                dao.deleteAppLock(appInfo.getPackageName());
                                lockedApps.remove(appInfo);
                                unlockApps.add(appInfo);
                                tv_applock_status.setText("已加锁软件:" + lockedApps.size());
                                lockedAdapter.notifyDataSetChanged();
                                unlockAdapter.notifyDataSetChanged();
                            }
                        }, 500);
                    } else {
                        //在未加锁ListView中
                        //添加平移动画
                        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 1.0F,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0);
                        animation.setDuration(500);
                        view.startAnimation(animation);
                        new Handler().postDelayed(new Thread(){
                            @Override
                            public void run() {
                                dao.addAppLock(appInfo.getPackageName());
                                unlockApps.remove(appInfo);
                                lockedApps.add(appInfo);
                                tv_applock_status.setText("未加锁软件:" + unlockApps.size());
                                lockedAdapter.notifyDataSetChanged();
                                unlockAdapter.notifyDataSetChanged();
                            }
                        }, 500);
                    }
                }
            });

            return view;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    static class ViewHolder {
        ImageView iv_app_icon;
        TextView tv_app_name;
        LinearLayout ll_app_lock;
        ImageView iv_unlock;
        TextView tv_unlock;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //未加锁
            case R.id.tv_left_unlock:
                tv_left_unlock.setBackgroundResource(R.drawable.tab_left_pressed);
                tv_right_locked.setBackgroundResource(R.drawable.tab_right_default);
                tv_applock_status.setText("未加锁软件:" + unlockApps.size());
                lv_unlock.setVisibility(View.VISIBLE);
                unlockAdapter.notifyDataSetChanged();
                break;
            //已加锁
            case R.id.tv_right_locked:
                tv_left_unlock.setBackgroundResource(R.drawable.tab_left_default);
                tv_right_locked.setBackgroundResource(R.drawable.tab_right_pressed);
                tv_applock_status.setText("已加锁软件:" + lockedApps.size());
                lv_unlock.setVisibility(View.GONE);
                lockedAdapter.notifyDataSetChanged();
                break;
        }
    }

    //重新设置密码
    public void reSetupPsd(View view) {
        Intent intent = new Intent(this, ResetPsdActivity.class);
        intent.putExtra("resetpsd", "applockpsd");
        startActivity(intent);
    }
}
