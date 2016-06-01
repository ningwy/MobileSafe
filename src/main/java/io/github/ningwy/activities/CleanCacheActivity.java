package io.github.ningwy.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.R;

public class CleanCacheActivity extends AppCompatActivity {

    private static final int UPDATE_PROGRESS = 0;
    private static final int FINISHED = 1;
    private static final int GET_CACHE_INFO = 2;
    private LinearLayout ll_cache_bg;
    private ImageView iv_cache_scanner;
    private TextView tv_cache_status;
    private ProgressBar pb_cache;
    private LinearLayout ll_cache_status;
    private Button bt_cache_clear;

    private PackageManager pm;

    /**
     * 有缓存的应用列表
     */
    private List<CacheInfo> cacheInfos;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    String name = (String) msg.obj;
                    tv_cache_status.setText("正在扫描:" + name);
                    break;

                case FINISHED:
                    if (cacheInfos.size() > 0) {
                        tv_cache_status.setText("扫描完成,下面应用具有缓存:");
                        bt_cache_clear.setVisibility(View.VISIBLE);
                    } else {
                        tv_cache_status.setText("您的手机很干净，没有缓存");
                    }
                    iv_cache_scanner.clearAnimation();
                    ll_cache_bg.setVisibility(View.GONE);
                    pb_cache.setVisibility(View.GONE);
                    break;

                case GET_CACHE_INFO:
                    CacheInfo cacheInfo = (CacheInfo) msg.obj;
                    showCache(cacheInfo);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_cache);

        init();

        scanCache();

    }

    /**
     * 初始化
     */
    private void init() {
        ll_cache_bg = (LinearLayout) findViewById(R.id.ll_cache_bg);
        iv_cache_scanner = (ImageView) findViewById(R.id.iv_cache_scanner);
        tv_cache_status = (TextView) findViewById(R.id.tv_cache_status);
        pb_cache = (ProgressBar) findViewById(R.id.pb_cache);
        ll_cache_status = (LinearLayout) findViewById(R.id.ll_cache_status);
        bt_cache_clear = (Button) findViewById(R.id.bt_cache_clear);

        RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(500);
        ra.setRepeatCount(Animation.INFINITE);
        iv_cache_scanner.startAnimation(ra);
        pm = getPackageManager();

        cacheInfos = new ArrayList<>();
    }

    /**
     * 显示缓存信息
     */
    private void showCache(final CacheInfo cacheInfo) {
        View view = View.inflate(CleanCacheActivity.this, R.layout.clean_cache_item, null);
        ImageView iv_cache_icon = (ImageView) view.findViewById(R.id.iv_cache_icon);
        TextView tv_cache_size = (TextView) view.findViewById(R.id.tv_cache_size);
        TextView tv_cache_name = (TextView) view.findViewById(R.id.tv_cache_name);
        ImageView iv_cache_clean = (ImageView) view.findViewById(R.id.iv_cache_clean);

        iv_cache_icon.setImageDrawable(cacheInfo.icon);
        tv_cache_name.setText(cacheInfo.name);
        tv_cache_size.setText(Formatter.formatFileSize(getApplicationContext(), cacheInfo.cacheSize));
        iv_cache_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.parse("package:"+cacheInfo.packname));
                startActivityForResult(intent, cacheInfos.indexOf(cacheInfo));
            }
        });
        ll_cache_status.addView(view, cacheInfos.indexOf(cacheInfo));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ll_cache_status.removeViewAt(requestCode);
    }

    /**
     * 扫描缓存
     */
    private void scanCache() {
        new Thread() {
            @Override
            public void run() {
                List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
                pb_cache.setMax(packageInfos.size());
                int progress = 0;
                for (PackageInfo packageInfo : packageInfos) {
                    SystemClock.sleep(100);
                    //得到名字
                    String name = packageInfo.applicationInfo.loadLabel(pm).toString();
                    //得到包名
                    String packageName = packageInfo.packageName;
                    //设置进度
                    progress++;
                    pb_cache.setProgress(progress);
                    //发送更新消息
                    Message msg = Message.obtain();
                    msg.obj = name;
                    msg.what = UPDATE_PROGRESS;
                    handler.sendMessage(msg);

                    //用反射得到 getPackageSizeInfo()方法，利用该方法才能得到缓存
                    try {
                        Method method = PackageManager.class.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                        method.invoke(pm, packageName, new MyIPackageStatsObserver());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //发送完成消息
                Message msg = Message.obtain();
                msg.what = FINISHED;
                handler.sendMessage(msg);
            }
        }.start();
    }

    class CacheInfo {
        long cacheSize;
        String name;
        String packname;
        Drawable icon;
    }

    private class MyIPackageStatsObserver extends IPackageStatsObserver.Stub {

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            long cacheSize = pStats.cacheSize;
            if (cacheSize > 12 * 1024) {
                try {
                    CacheInfo cacheInfo = new CacheInfo();
                    cacheInfo.cacheSize = cacheSize;
                    cacheInfo.packname = pStats.packageName;
                    ApplicationInfo info = pm.getApplicationInfo(cacheInfo.packname, 0);
                    cacheInfo.name = info.loadLabel(pm).toString();
                    cacheInfo.icon = info.loadIcon(pm);

                    //添加到缓存应用列表中
                    cacheInfos.add(cacheInfo);

                    Message message = Message.obtain();
                    message.obj = cacheInfo;
                    message.what = GET_CACHE_INFO;
                    handler.sendMessage(message);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * bt_clean_cache的点击事件——清理全部缓存
     *
     * @param view
     */
    public void cleanCache(View view) {
        try {
            Method method = pm.getClass().getMethod("freeStorageAndNotify", long.class, IPackageDataObserver.class);
            method.invoke(pm, Long.MAX_VALUE, new IPackageDataObserver.Stub(){
                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
//                    Log.e("TAG", "succeeded:" + succeeded);
                }
            });
            Log.e("TAG", "cacheInfos.size() = " + cacheInfos.size());
//            for (int i = 0; i < cacheInfos.size(); i++) {
//                Log.e("TAG", i + "");
//                ll_cache_status.removeViewAt(i);
//            }
            ll_cache_status.removeAllViews();
            tv_cache_status.setText("清除完成，您的手机很干净");
            bt_cache_clear.setClickable(false);
            bt_cache_clear.setBackgroundColor(getResources().getColor(R.color.lightGray));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
