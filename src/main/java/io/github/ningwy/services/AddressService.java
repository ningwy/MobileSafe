package io.github.ningwy.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import io.github.ningwy.R;
import io.github.ningwy.db.dao.QueryAddressDao;

public class AddressService extends Service {

    /**
     * 电话管理者
     */
    private TelephonyManager tm;
    private MyPhoneStateListener listener;
    private CallListenerReceiver callListenerReceiver;
    private SharedPreferences sp;

    private WindowManager wm;
    /**
     * 来电显示的自定义View
     */
    private View view;
    /**
     * 用于定义view状态位置的参数
     */
    private WindowManager.LayoutParams params;

    public AddressService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        //添加来电监听
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        //注册去电监听
        callListenerReceiver = new CallListenerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(callListenerReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消来电监听
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;
        //取消去电监听
        unregisterReceiver(callListenerReceiver);
        callListenerReceiver = null;
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        //当电话状态改变时
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                //来电，铃声响起时调用
                case TelephonyManager.CALL_STATE_RINGING :
                    String address = QueryAddressDao.queryAddressFromDB(incomingNumber);
                    MyToast(address);
                    break;

                //挂断时调用
                case TelephonyManager.CALL_STATE_IDLE:
                    //当来电监听没有启动时，view可能为null，所以需要判断一下
                    if (view != null) {
                        wm.removeView(view);
                        view = null;
                    }
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 自定义吐司
     * @param address 要显示的来电号码归属地
     */
    private void MyToast(String address) {
        view = View.inflate(this, R.layout.caller_indentification, null);
        //"半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿"
        int imgs[] = {
                R.drawable.call_locate_white, R.drawable.call_locate_orange,
                R.drawable.call_locate_blue, R.drawable.call_locate_gray,
                R.drawable.call_locate_green
        };
        int index = sp.getInt("style", 0);
        //设置背景色
        view.setBackgroundResource(imgs[index]);
        //设置触摸事件
        view.setOnTouchListener(new View.OnTouchListener() {
            int eventX;
            int eventY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN :
                        eventX = (int) event.getRawX();
                        eventY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE :
                        //计算偏移量
                        int dx = (int) (event.getRawX() - eventX);
                        int dy = (int) (event.getRawY() - eventY);
                        //得到移动后view的坐标
                        params.x += dx;
                        params.y += dy;
                        //屏蔽非法拖动——在windowManager中，坐标是没有负数的，而在一般的View中，
                        //对坐标是有负数要求
                        if (params.x < 0) {
                            params.x = 0;
                        }
                        if (params.y < 0) {
                            params.y = 0;
                        }
                        if (params.x > wm.getDefaultDisplay().getWidth() - view.getWidth()) {
                            params.x = wm.getDefaultDisplay().getWidth() - view.getWidth();
                        }
                        if (params.y > wm.getDefaultDisplay().getHeight() - view.getHeight()) {
                            params.y = wm.getDefaultDisplay().getHeight() - view.getHeight();
                        }
                        //布局view
                        wm.updateViewLayout(view, params);
                        //重新测量记录eventX和eventY
                        eventX = (int) event.getRawX();
                        eventY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP :
                        //保存view的位置
//                        int left = view.getLeft();
//                        int top = view.getTop();
                        SharedPreferences.Editor editor = sp.edit();
                        /**
                         * 视图从创建到显示的生命周期不熟悉，以致保存的时候保存了left和top，
                         * 殊不知两者都为0
                         * editor.putInt("lastX", left);
                           editor.putInt("lastY", top);
                         */
                        editor.putInt("lastX", params.x);
                        editor.putInt("lastY", params.y);
                        editor.commit();
                        break;
                }
                return true;
            }
        });
        TextView tv_address = (TextView) view.findViewById(R.id.tv_address);
        //设置号码归属地
        tv_address.setText(address);

        //以下都是设置一些列参数
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        /**
         * 该参数使得显示框能够对触摸事件作出回应
         * 不过需要权限——SYSTEM_ALERT_WINDOW，否则会报错：
         * android.view.WindowManager$BadTokenException：Unable to add window
         * android.view.ViewRootImpl$W@1865c45 -- permission denied for this window type
         */
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        //设置view的位置
        int lastX = sp.getInt("lastX", 0);
        int lastY = sp.getInt("lastY", 0);
        params.x = lastX;
        params.y = lastY;
        //重新设置params的gravity参数，使其以左上顶点为原点(默认以中点为原点)
        params.gravity = Gravity.LEFT + Gravity.TOP;

        wm.addView(view, params);
    }

    /**
     * 去电监听，为一个广播接收器
     */
    private class CallListenerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //得到拨打的电话号码
            String number = getResultData();
            String address = QueryAddressDao.queryAddressFromDB(number);
            MyToast(address);
        }
    }
}
