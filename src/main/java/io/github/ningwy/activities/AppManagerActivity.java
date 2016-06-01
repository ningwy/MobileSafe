package io.github.ningwy.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.R;
import io.github.ningwy.domain.AppInfo;
import io.github.ningwy.engine.AppInfoProdiver;

public class AppManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_app_rom;
    private TextView tv_app_sdcard;
    private TextView tv_status;
    private LinearLayout ll_app_loading;
    private ListView lv_app;
    private List<AppInfo> appInfoList;
    private List<AppInfo> userAppList;
    private List<AppInfo> systemAppList;
    private PopupWindow window;
    private WindowManager wm;
    private AppInfo appInfo;
    private LinearLayout ll_app_uninstall;
    private LinearLayout ll_app_start;
    private LinearLayout ll_app_share;
    private AppManagerAdapter adapter;
    //应用缓存大小
    private long cacheSize;
    //应用数据大小
    private long dataSize;
    //应用自身大小(代码量)
    private long codeSize;
    //应用总的大小
    private long totalSize;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                lv_app.setAdapter(adapter);
                ll_app_loading.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        init();
        fillData();
        lv_app.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (userAppList == null || systemAppList == null) {
                    return;
                }
                if (firstVisibleItem <= userAppList.size()) {
                    tv_status.setText("用户程序(" + userAppList.size() + ")");
                } else {
                    tv_status.setText("系统应用(" + systemAppList.size() + ")");
                }

                //PopupWindow如果存在，则消掉
                dismissPopupWindow();
            }
        });

        lv_app.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View contentView = View.inflate(AppManagerActivity.this, R.layout.app_manager_popupwindow, null);
                ll_app_uninstall = (LinearLayout) contentView.findViewById(R.id.ll_app_uninstall);
                ll_app_start = (LinearLayout) contentView.findViewById(R.id.ll_app_start);
                ll_app_share = (LinearLayout) contentView.findViewById(R.id.ll_app_share);
                //设置点击监听
                ll_app_uninstall.setOnClickListener(AppManagerActivity.this);
                ll_app_start.setOnClickListener(AppManagerActivity.this);
                ll_app_share.setOnClickListener(AppManagerActivity.this);
                Object obj = lv_app.getItemAtPosition(position);
                if (obj != null) {
                    appInfo = (AppInfo) obj;
                    if (window == null) {
                        window = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        int[] location = new int[2];
                        view.getLocationInWindow(location);
                        window.showAtLocation(parent, Gravity.LEFT + Gravity.TOP, (wm.getDefaultDisplay().getWidth() / 2) - 120, location[1]);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f);
                        contentView.startAnimation(scaleAnimation);
                        scaleAnimation.setDuration(500);
                    }
                }
            }
        });

    }

    /**
     * 消掉PopupWindow
     */
    private void dismissPopupWindow() {
        if (window != null) {
            window.dismiss();
            window = null;
        }
    }

    /**
     * 填充数据
     */
    private void fillData() {
        //填充状态栏的关于可用空间的TextView
        String dataPath = Environment.getDataDirectory().getAbsolutePath();
        String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        tv_app_rom.setText("内存可用:" + getSpace(dataPath));
        tv_app_sdcard.setText("sd卡可用:" + getSpace(sdCardPath));

        //为appInfoList填充数据--需在分线程中进行
        ll_app_loading.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                super.run();
                appInfoList = AppInfoProdiver.getAllAppInfo(AppManagerActivity.this);
                //将appInfoList分为用户应用和系统应用
                userAppList = new ArrayList<>();
                systemAppList = new ArrayList<>();
                for (AppInfo appInfo : appInfoList) {
                    if (appInfo.isUser()) {
                        //用户程序
                        userAppList.add(appInfo);
                    } else {
                        //系统程序
                        systemAppList.add(appInfo);
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //卸载
            case R.id.ll_app_uninstall:
                dismissPopupWindow();
                uninstallApp();
                break;

            //启动
            case R.id.ll_app_start:
                dismissPopupWindow();
                startApp();
                break;

            //分享
            case R.id.ll_app_share:
                dismissPopupWindow();
                shareApp();
                break;
        }
    }

    /**
     * 启动app
     */
    private void startApp() {
        PackageManager pm = getPackageManager();
        String packageName = appInfo.getPackageName();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            ResolveInfo resolveInfo = AppInfoProdiver.findActivitiesForPackage(pm, packageName);
            if (resolveInfo != null) {
                intent = new Intent();
                intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
            }
        }
        startActivity(intent);
    }

    /**
     * 分享app——短信、微信、微博等分享
     */
    private void shareApp() {
        /**
         * <action android:name="android.intent.action.SEND" />
         <category android:name="android.intent.category.DEFAULT" />
         <data android:mimeType="text/plain" />
         */
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "我正在使用" + appInfo.getAppName() +
                "，快来下载啦,下载地址：http://apk.hiapk.com/appinfo/"
                + appInfo.getPackageName());
        startActivity(intent);
    }

    /**
     * 卸载app
     */
    private void uninstallApp() {
        /**
         * <intent-filter>
         <action android:name="android.intent.action.VIEW" />
         <action android:name="android.intent.action.DELETE" />
         <category android:name="android.intent.category.DEFAULT" />
         <data android:scheme="package" />
         </intent-filter>
         */
        if (appInfo.isUser()) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.DELETE");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(getApplicationContext(), "卸载系统应用需要root权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fillData();
    }

    private class MyIPackageStatsObserver extends IPackageStatsObserver.Stub {

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            cacheSize = pStats.cacheSize;//缓存大小
            dataSize = pStats.dataSize;//数据大小
            codeSize = pStats.codeSize;//应用程序大小
            totalSize = cacheSize + dataSize + codeSize;//总大小

        }
    }

    /**
     * 初始化
     */
    private void init() {
        tv_app_rom = (TextView) findViewById(R.id.tv_app_rom);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_app_sdcard = (TextView) findViewById(R.id.tv_app_sdcard);
        ll_app_loading = (LinearLayout) findViewById(R.id.ll_app_loading);
        lv_app = (ListView) findViewById(R.id.lv_app);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        adapter = new AppManagerAdapter();
    }

    /**
     * 得到指定目录下的存储空间大小
     *
     * @param path 路径
     * @return 返回格式化的空间大小
     */
    private String getSpace(String path) {

        //StatFs用于检索文件系统所有的空间信息
        StatFs sf = new StatFs(path);
        //得到可用的内存区块
        long blocks = sf.getAvailableBlocks();
        //得到每块内存区块的大小
        long size = sf.getBlockSize();

        return Formatter.formatFileSize(this, blocks * size);
    }

    private class AppManagerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userAppList.size() + systemAppList.size() + 1 + 1;
        }

        @Override
        public Object getItem(int position) {
            AppInfo appInfo;
            if (position == 0) {
                return null;
            } else if (position == (userAppList.size() + 1)) {
                return null;
            } else if (position <= userAppList.size()) {
                appInfo = userAppList.get(position - 1);
            } else {
                appInfo = systemAppList.get(position - userAppList.size() - 2);
            }
            return appInfo;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            AppInfo appInfo;
            /**
             * 注意：如果下面不加 convertView instanceof LinearLayout 判断条件，在
             * 制作复杂ListView的时候，很容易造成空指针异常，其中LinearLayout是ListView
             * 的item布局文件的根节点(视不同的布局文件而不同)
             */
            if (convertView != null && convertView instanceof LinearLayout) {
                viewHolder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(AppManagerActivity.this, R.layout.app_manager_item, null);
                viewHolder = new ViewHolder();
                viewHolder.iv_app_icon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
                viewHolder.tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
                viewHolder.tv_app_location = (TextView) convertView.findViewById(R.id.tv_app_location);
                viewHolder.tv_app_size = (TextView) convertView.findViewById(R.id.tv_app_size);
                convertView.setTag(viewHolder);
            }

            if (position == 0) {
                TextView tv = new TextView(AppManagerActivity.this);
                tv.setText("用户程序(" + userAppList.size() + ")");
                tv.setTextSize(16);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                return tv;
            } else if (position == (userAppList.size() + 1)) {
                TextView tv = new TextView(AppManagerActivity.this);
                tv.setText("系统应用(" + systemAppList.size() + ")");
                tv.setTextSize(16);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                return tv;
            } else if (position <= userAppList.size()) {
                appInfo = userAppList.get(position - 1);
            } else {
                appInfo = systemAppList.get(position - userAppList.size() - 2);
            }
            viewHolder.iv_app_icon.setImageDrawable(appInfo.getIcon());
            viewHolder.tv_app_name.setText(appInfo.getAppName());
            viewHolder.tv_app_size.setText(appInfo.getAppSize());
            if (appInfo.isRom()) {
                viewHolder.tv_app_location.setText("手机内部");
            } else {
                viewHolder.tv_app_location.setText("外部存储");
            }

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView iv_app_icon;
        TextView tv_app_name;
        TextView tv_app_location;
        TextView tv_app_size;
    }
}
