package io.github.ningwy.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.db.AppLockDBOpenHelper;

/**
 * 程序锁的dao
 * Created by ningwy on 2016/5/23.
 */
public class AppLockDao {

    private AppLockDBOpenHelper helper;
    private Context context;

    public AppLockDao (Context context) {
        helper = new AppLockDBOpenHelper(context);
        this.context = context;
    }

    /**
     * 添加程序锁数据库一个应用
     * @param packageName 应用包名
     */
    public void addAppLock(String packageName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("packagename", packageName);
        db.insert("applock", null, values);
        db.close();
        //当数据库发生变化时，向外发送消息
        Uri uri = Uri.parse("io.github.ningwy.dbchange");
        context.getContentResolver().notifyChange(uri, null);
    }

    /**
     * 从程序锁数据库删除一个应用
     * @param packageName 应用包名
     */
    public void deleteAppLock(String packageName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("applock", "packagename=?", new String[]{packageName});
        db.close();
        //当数据库发生变化时，向外发送消息
        Uri uri = Uri.parse("content://io.github.ningwy.dbchange");
        context.getContentResolver().notifyChange(uri, null);
    }

    /**
     * 得到所有的已加锁的程序的包名
     * @return
     */
    public List<String> queryAllLockedApp() {
        List<String> packageNames = new ArrayList<>();
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("applock", new String[]{"packagename"}, null, null, null , null, null);
        while (cursor.moveToNext()) {
            String packageName = cursor.getString(0);
            packageNames.add(packageName);
        }
        cursor.close();
        db.close();
        return packageNames;
    }

    /**
     * 判断程序锁数据库里是否有某一个应用
     * @param packageName 应用包名
     * @return true:存在, false:不存在
     */
    public boolean isHasAppLock(String packageName) {
        boolean result = false;
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("applock", null, "packagename=?", new String[]{packageName}, null, null, null);
        if (cursor.moveToNext()) {
            result = true;
        }
        return result;
    }

}
