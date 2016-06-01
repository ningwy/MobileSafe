package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.github.ningwy.R;
import io.github.ningwy.utils.MD5Utils;

public class EnterPwdActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_applock_psd;
    private Button btn_applock_confirm;
    private Button btn_applock_cancel;
    private ImageView iv_psd_icon;
    private TextView tv_psd_name;

    private SharedPreferences sp;
    private PackageManager pm;

    /**
     * 启动该页面传达而来的intent
     */
    private Intent intentMsg;

    /**
     * 启动该页面的activity所在应用的包名
     */
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pwd);
        init();

        btn_applock_confirm.setOnClickListener(this);
        btn_applock_cancel.setOnClickListener(this);
    }

    /**
     * 初始化
     */
    private void init() {
        et_applock_psd = (EditText) findViewById(R.id.et_applock_psd);
        btn_applock_confirm = (Button) findViewById(R.id.btn_applock_confirm);
        btn_applock_cancel = (Button) findViewById(R.id.btn_applock_cancel);
        iv_psd_icon = (ImageView) findViewById(R.id.iv_psd_icon);
        tv_psd_name = (TextView) findViewById(R.id.tv_psd_name);

        sp = getSharedPreferences("password", MODE_PRIVATE);
        pm = getPackageManager();

        intentMsg = getIntent();
        packageName = intentMsg.getStringExtra("packageName");

        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            iv_psd_icon.setImageDrawable(applicationInfo.loadIcon(pm));
            tv_psd_name.setText(applicationInfo.loadLabel(pm).toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AppLockActivity.class);
        switch (v.getId()) {
            case R.id.btn_applock_confirm:
                String applockpsd = sp.getString("applockpsd", "");
                String a_psd = et_applock_psd.getText().toString().trim();
                if (TextUtils.isEmpty(a_psd)) {//第一个密码为空时
                    Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!applockpsd.equals(MD5Utils.getMD5Secret(a_psd))) {
                    Toast.makeText(getApplicationContext(), "密码不正确", Toast.LENGTH_SHORT).show();
                } else if (applockpsd.equals(MD5Utils.getMD5Secret(a_psd))) {//密码正确
                    //进入应用后发送一个广播让WatchDogService停止监听该应用启动，否则会一直启动输密码界面
                    Intent stopIntent = new Intent();
                    stopIntent.setAction("io.github.ningwy.stopprotection");
                    //携带上包名，以便做判断
                    stopIntent.putExtra("packageName", packageName);
                    sendBroadcast(stopIntent);
                    //关闭输入密码页面
                    finish();
                }
                break;
            case R.id.btn_applock_cancel:
                comeBackHome();
                finish();
                break;
        }
    }

    //当页面看不见的时候把activity关闭
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        //当点击返回键的时候回到桌面
        comeBackHome();
    }

    /**
     * 回到桌面
     */
    public void comeBackHome() {
        Intent homeIntent = new Intent();
        homeIntent.setAction("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.HOME");
        startActivity(homeIntent);
    }


}
