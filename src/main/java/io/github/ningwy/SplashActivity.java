package io.github.ningwy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.github.ningwy.activities.HomeActivity;
import io.github.ningwy.utils.StreamTools;

public class SplashActivity extends AppCompatActivity {

    private static final int ENTER_HOME = 0;
    private static final int SHOW_UPDATE_DIALOG = 1;
    private static final int URL_ERROR = 2;
    private static final int NETWORK_ERROR = 3;
    private static final int JSON_ERROR = 4;
    private TextView tv_splash_version;
    private TextView tv_splash_download;
    private SharedPreferences sp;
    /**
     * 升级的URL地址
     */
    private String apkUrl;
    /**
     * 升级的版本描述
     */
    private String description;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENTER_HOME:
                    enterHome();
                    break;
                case SHOW_UPDATE_DIALOG:
                    showUpdateDialog();
                    break;
                case URL_ERROR:
                    Toast.makeText(getApplicationContext(), "URL连接错误", Toast.LENGTH_SHORT).show();
                    if (sp.getBoolean("update", false)) {
                        showUpdateDialog();
                    } else {
                        enterHome();
                    }
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getApplicationContext(), "网络连接错误", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case JSON_ERROR:
                    Toast.makeText(getApplicationContext(), "Json解析错误", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                default:

                    break;
            }
        }
    };

    /**
     * 显示下载更新对话框
     */
    private void showUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("更新下载")
                .setMessage(description)
                .setPositiveButton("立马更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /**
                         *因服务器不可用，所以只能放一个Toast
                         *downloadAPK();
                         */
                        downloadAPK();
                        Toast.makeText(getApplicationContext(), "下载中...", Toast.LENGTH_SHORT).show();
                        enterHome();
                    }
                })
                .setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        enterHome();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        enterHome();
                    }
                })
                .show();
    }

    /**
     * 下载apk
     */
    private void downloadAPK() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            FinalHttp finalHttp = new FinalHttp();
            finalHttp.download(apkUrl, Environment.getExternalStorageDirectory() + "/mobilesafe.apk", new AjaxCallBack<File>() {
                @Override
                public void onLoading(long count, long current) {
                    super.onLoading(count, current);
                    tv_splash_download.setVisibility(View.VISIBLE);
                    int progress = (int) (current / count * 100);
                    tv_splash_download.setText("当前下载进度为" + progress + "%");
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    super.onFailure(t, errorNo, strMsg);
                    t.printStackTrace();
                    Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(File file) {
                    super.onSuccess(file);
                    installAPK(file);
                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "SD卡不可用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 安装APK
     */
    private void installAPK(File file) {
        /**
         *这是APK安装应用的action和type
         *
         <action android:name="android.intent.action.VIEW" />
         <category android:name="android.intent.category.DEFAULT" />
         <data android:scheme="content" />
         <data android:scheme="file" />
         <data android:mimeType="application/vnd.android.package-archive" />
         */
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initView();

        updateApplication();

        copyDB("address.db");
        copyDB("commonnum.db");
        copyDB("antivirus.db");

        createShortCut();

    }

    /**
     * 创建快捷方式
     */
    private void createShortCut() {
        if (sp.getBoolean("shortcut", false)) {
            return;
        }

        Intent intent = new Intent();
        //Launcher2应用的创建快捷方式的action
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        //图标
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        //名称
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机卫士");
        //动作
        Intent toDoIntent = new Intent();
        toDoIntent.setAction("io.github.ningwy.home");

        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, toDoIntent);
        sendBroadcast(intent);
        //保存
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("shortcut", true);
        editor.commit();
    }

    /**
     * 因为SQLite数据库不能够读取到assets目录下的东西，所以将
     * 拷贝assets目录下的号码归属地数据库到/data/data/packageName/files/xxx.db目录下
     */
    private void copyDB(String dbName) {
        File file = new File(getFilesDir(), dbName);
        if (!file.exists()) {
            try {
                AssetManager assets = getAssets();
                InputStream is = assets.open(dbName);
                FileOutputStream fos = new FileOutputStream(file);
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0 , len);
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 版本升级
     */
    private void updateApplication() {
        final Message msg = new Message();
        //联网操作，放在分线程中执行
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(getString(R.string.serverurl));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(4000);
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream is = connection.getInputStream();
                        String result = StreamTools.readFromStream(is);
                        JSONObject json = new JSONObject(result);
                        apkUrl = json.getString("apkUrl");
                        description = json.getString("description");
                        String version = json.getString("version");
                        if (getVersion().equals(version)) {
                            msg.what = ENTER_HOME;
                        } else {
                            msg.what = SHOW_UPDATE_DIALOG;
                        }
                    }
                } catch (MalformedURLException e) {
                    //URL连接错误
                    msg.what = URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    //网络连接错误
                    msg.what = NETWORK_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    //Json解析错误
                    msg.what = JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    handler.sendMessageDelayed(msg, 1500);
                }
            }
        }.start();
    }

    /**
     * 进入主页
     */
    private void enterHome() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        //进入主页后关闭当前页面
        finish();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
        tv_splash_download = (TextView) findViewById(R.id.tv_splash_download);
        tv_splash_version.setText("版本号：" + getVersion());
        sp = getSharedPreferences("config", MODE_PRIVATE);
    }

    /**
     * 获得版本号
     */
    private String getVersion() {

        try {
            /**
             * 先获得包管理器，再利用包管理器对象获得包信息，在包信息里面就有版本号
             */
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }
}
