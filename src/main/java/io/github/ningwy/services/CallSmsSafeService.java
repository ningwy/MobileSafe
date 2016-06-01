package io.github.ningwy.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import io.github.ningwy.activities.CallSmsSafeActivity;
import io.github.ningwy.db.dao.BlackNumberDao;

/**
 * 该类用于电话拦截，而短信拦截用内部类实现
 */
public class CallSmsSafeService extends Service {

    private TelephonyManager tm;
    private CallSmsSafeReceiver receiver;
    private BlackNumberDao dao;
    private CallSmsSafeListener listenr;

    public CallSmsSafeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        dao = new BlackNumberDao(this);
        receiver = new CallSmsSafeReceiver();
        listenr = new CallSmsSafeListener();
        //注册短信监听
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(receiver, filter);
        //来电监听
        tm.listen(listenr, PhoneStateListener.LISTEN_CALL_STATE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消注册
        unregisterReceiver(receiver);
        receiver = null;
        //取消来电监听
        tm.listen(listenr, PhoneStateListener.LISTEN_NONE);
        listenr = null;
    }

    private class CallSmsSafeListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                //电话响起时调用
                case TelephonyManager.CALL_STATE_RINGING:
                    if (dao.query(incomingNumber)) {
                        String mode = dao.queryMode(incomingNumber);
                        if ("1".equals(mode) || "2".equals(mode)) {
                            endCall();
//                            deleteCallLog(incomingNumber);
                            /**
                             * 由于聊天记录是异步生成的，所以不能直接删，要注册一个ContentObserver
                             * 来观察通话记录的变化，一旦发生变化，就删除
                             */
                            Uri url = Uri.parse("content://call_log/calls");
                            getContentResolver().registerContentObserver(url, true,
                                    new CallLogContentObserver(new Handler(), incomingNumber));
                        }
                    }
                    break;
            }
        }
    }

    class CallLogContentObserver extends ContentObserver {

        private String incomingNumber;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public CallLogContentObserver(Handler handler, String incomingNumber) {
            super(handler);
            this.incomingNumber = incomingNumber;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            deleteCallLog(incomingNumber);
            //取消注册
            getContentResolver().unregisterContentObserver(this);
        }

    }

    /**
     * 删除通话记录
     */
    private void deleteCallLog(String incomingNumber) {
        ContentResolver resolver = getContentResolver();
        Uri url = Uri.parse("content://call_log/calls");
        resolver.delete(url, "number = ?", new String[]{incomingNumber});
    }

    /**
     * 挂断电话——需调用系统隐藏的API
     */
    private void endCall() {
//        IBinder b = ServiceManager.getService(ALARM_SERVICE);
//        IAlarmManager service = IAlarmManager.Stub.asInterface(b);
        try {
            //1. 由于谷歌把ServiceManager隔离了，不能直接调用，所以通过反射的方式调用
            Class clazz = CallSmsSafeActivity.class.getClassLoader().loadClass("android.os.ServiceManager");
            //2.得到getService()方法
            Method method = clazz.getMethod("getService", String.class);
            //3.调用方法，得到IBinder对象
            IBinder b = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
            //4.得到ITelephony
            ITelephony service = ITelephony.Stub.asInterface(b);
            service.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定义一个短信接收器用于短信拦截
     */
    private class CallSmsSafeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                //得到电话号码
                String number = smsMessage.getOriginatingAddress();
                //得到短信内容
                String body = smsMessage.getMessageBody();
                //1.根据电话号码拦截
                if (dao.query(number)) {
                    Log.e("TAG", "拦截到一条短信");
                    abortBroadcast();
                }
                //2.根据短信内容拦截
                if (body.contains("fapiao")) {
                    Log.e("TAG", "拦截到一条短信2");
                    abortBroadcast();
                }
            }
        }
    }
}
