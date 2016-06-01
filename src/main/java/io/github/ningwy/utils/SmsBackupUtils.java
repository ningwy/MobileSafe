package io.github.ningwy.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 短信备份工具类
 * Created by ningwy on 2016/5/10.
 */
public class SmsBackupUtils {

    /**
     * 短信备份的回调接口
     */
    public interface SmsBackupCallBack {

        /**
         * 设置进度条的总数量
         * @param count 要备份的短信数量
         */
        void setCount(int count);

        /**
         * 设置进度款进度
         * @param progress 进度
         */
        void setProgress(int progress);
    }

    /**
     * 短信备份的操作方法
     * @param context 上下文
     * @param path 备份保存的路径
     * @param back 备份操作回调的接口
     * @throws Exception
     */
    public static void smsBackup(Context context, String path, SmsBackupCallBack back) throws Exception {
        XmlSerializer xml = Xml.newSerializer();
        File file = new File(path);
        FileOutputStream os = new FileOutputStream(file);
        xml.setOutput(os, "utf-8");
        xml.startDocument("utf-8", true);
        xml.startTag(null, "smss");
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = resolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);
        int count = cursor.getCount();
        back.setCount(count);
        int progress = 0;
        while (cursor.moveToNext()) {
            xml.startTag(null, "sms");

            xml.startTag(null, "address");
            String address = cursor.getString(0);
            xml.text(address);
            xml.endTag(null, "address");

            xml.startTag(null, "date");
            String date = cursor.getString(1);
            xml.text(date);
            xml.endTag(null, "date");

            xml.startTag(null, "type");
            String type = cursor.getString(2);
            xml.text(type);
            xml.endTag(null, "type");

            xml.startTag(null, "body");
            String body = cursor.getString(3);
            xml.text(body);
            xml.endTag(null, "body");

            xml.endTag(null, "sms");
            back.setProgress(progress);
            progress++;
            SystemClock.sleep(100);
        }
        cursor.close();
        xml.endTag(null, "smss");
        xml.endDocument();
    }

}
