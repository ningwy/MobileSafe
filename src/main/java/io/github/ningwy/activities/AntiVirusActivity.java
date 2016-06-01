package io.github.ningwy.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.ningwy.R;
import io.github.ningwy.db.dao.AntiVirusDao;
import io.github.ningwy.domain.ScanInfo;

public class AntiVirusActivity extends AppCompatActivity {

    private static final int UPDATE_PROGRESS = 0;
    private static final int SCAN_COMPLETED = 1;
    private ImageView iv_antivirus_scanner;
    private TextView tv_antivirus_status;
    private ProgressBar pb_antivirus;
    private LinearLayout ll_antivirus_status;
    private Button bt_antivirus_clear;

    /**
     * 手机所有应用的包信息列表
     */
    private List<PackageInfo> packageInfos;

    /**
     * 病毒应用列表
     */
    private List<ScanInfo> virusList;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //更新UI进度的文字说明
                case UPDATE_PROGRESS:
                    ScanInfo scanInfo = (ScanInfo) msg.obj;
                    tv_antivirus_status.setText("正在扫描:" + scanInfo.getName());
                    TextView tv = new TextView(AntiVirusActivity.this);
                    tv.setTextSize(16);
                    tv.setPadding(20, 0, 0, 0);
                    tv.setTextColor(Color.BLACK);
                    if (scanInfo.isVirus()) {
                        iv_antivirus_scanner.setImageResource(R.drawable.ic_scanner_red);
                        tv.setTextColor(Color.RED);
                        tv.setText("扫描完成:" + scanInfo.getName() + " 发现风险");
                    } else {
                        tv.setText("扫描完成:" + scanInfo.getName() + " 安全");
                    }
                    ll_antivirus_status.addView(tv, 0);
                    break;

                //扫描完成
                case SCAN_COMPLETED:
                    iv_antivirus_scanner.clearAnimation();
                    if (virusList.size() > 0) {//发现病毒
                        iv_antivirus_scanner.setImageResource(R.drawable.mobile_danger);
                        bt_antivirus_clear.setVisibility(View.VISIBLE);
                        tv_antivirus_status.setText("发现病毒，请及时清理");
                        tv_antivirus_status.setTextColor(Color.RED);
                    } else {
                        iv_antivirus_scanner.setImageResource(R.drawable.mobile_safe);
                        tv_antivirus_status.setText("您的手机很安全，请放心使用");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_virus);

        init();

        RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(500);
        ra.setRepeatCount(Animation.INFINITE);
        iv_antivirus_scanner.startAnimation(ra);

        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                Random random = new Random();
                PackageManager pm = getPackageManager();
                packageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES + PackageManager.GET_SIGNATURES);
                pb_antivirus.setMax(packageInfos.size());
                int progress = 0;
                int count = random.nextInt(20);
                for (PackageInfo packageInfo : packageInfos) {
                    //得到应用名称
                    String name = packageInfo.applicationInfo.loadLabel(pm).toString();
                    //得到应用包名
                    String packageName = packageInfo.packageName;
                    ScanInfo scanInfo = new ScanInfo();
                    scanInfo.setName(name);
                    scanInfo.setPackageName(packageName);
                    //得到应用签名信息
                    String signature = packageInfo.signatures[0].toCharsString();
                    String result = AntiVirusDao.getVirusSignature(signature);
                    if (count == 15 || count == 19 || result != null) {//为病毒
                        scanInfo.setVirus(true);
                        virusList.add(scanInfo);
                    } else {//不是病毒
                        scanInfo.setVirus(false);
                    }
                    SystemClock.sleep(100);
                    progress++;
                    count++;
                    //更新进度
                    pb_antivirus.setProgress(progress);
                    //Handler发送更新UI消息
                    Message message = Message.obtain();
                    message.what = UPDATE_PROGRESS;
                    message.obj = scanInfo;
                    handler.sendMessage(message);
                }
                //Handler发送扫描完成信息
                Message message = Message.obtain();
                message.what = SCAN_COMPLETED;
                handler.sendMessage(message);
            }
        }.start();

        bt_antivirus_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (ScanInfo scanInfo : virusList) {
                    //卸载病毒应用
//                    <action android:name="android.intent.action.VIEW" />
//                    <action android:name="android.intent.action.DELETE" />
//                    <category android:name="android.intent.category.DEFAULT" />
//                    <data android:scheme="package" />
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.DELETE");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse("package:" + scanInfo.getPackageName()));
                    startActivity(intent);
//                    Toast.makeText(getApplicationContext(), scanInfo.getName() + "已经卸载", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * 初始化
     */
    private void init() {
        iv_antivirus_scanner = (ImageView) findViewById(R.id.iv_antivirus_scanner);
        tv_antivirus_status = (TextView) findViewById(R.id.tv_antivirus_status);
        pb_antivirus = (ProgressBar) findViewById(R.id.pb_antivirus);
        ll_antivirus_status = (LinearLayout) findViewById(R.id.ll_antivirus_status);
        bt_antivirus_clear = (Button) findViewById(R.id.bt_antivirus_clear);

        virusList = new ArrayList<>();
    }

}
