package io.github.ningwy.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.db.BlackNumberDBOpenHelper;
import io.github.ningwy.domain.BlackNumberInfo;

/**
 * 黑名单的dao
 * Created by ningwy on 2016/4/27.
 */
public class BlackNumberDao {

    private BlackNumberDBOpenHelper helper;

    public BlackNumberDao(Context context) {
        helper = new BlackNumberDBOpenHelper(context);
    }

    /**
     * 增加黑名单
     *
     * @param number 黑名单号码
     * @param mode   模式：0-短信拦截，1-电话拦截，2-全部拦截
     */
    public void add(String number, String mode) {
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("number", number);
        values.put("mode", mode);
        database.insert("blacknumber", null, values);
        //记得关闭数据库连接
        database.close();
    }

    /**
     * 删除黑名单
     *
     * @param number 要删除的号码
     */
    public void delete(String number) {
        SQLiteDatabase database = helper.getWritableDatabase();
        database.delete("blacknumber", "number=?", new String[]{number});
        database.close();
    }

    /**
     * 修改黑名单号码的拦截模式
     *
     * @param number  要修改的号码
     * @param newMode 新的拦截模式
     */
    public void update(String number, String newMode) {
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mode", newMode);
        database.update("blacknumber", values, "number=?", new String[]{number});
        database.close();
    }

    /**
     * 查询黑名单是否存在
     *
     * @param number 要查询的号码
     * @return
     */
    public boolean query(String number) {
        boolean result = false;
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query("blacknumber", null, "number=?", new String[]{number}, null, null, null);
        if (cursor.moveToNext()) {
            result = true;
        }
        cursor.close();
        database.close();
        return result;
    }

    /**
     * 查询黑名单号码的拦截模式
     *
     * @param number 要查询的号码
     * @return
     */
    public String queryMode(String number) {
        String mode = null;
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query("blacknumber", new String[]{"mode"}, "number=?",
                new String[]{number}, null, null, null);
        while (cursor.moveToNext()) {
            mode = cursor.getString(0);
        }
        cursor.close();
        database.close();
        return mode;
    }

    /**
     * 查询全部的黑名单
     * @return
     */
    public List<BlackNumberInfo> queryAll() {
        SystemClock.sleep(3000);
        List<BlackNumberInfo> infoList = new ArrayList<>();
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query("blacknumber", new String[]{"number", "mode"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String number = cursor.getString(0);
            String mode = cursor.getString(1);
            BlackNumberInfo info = new BlackNumberInfo(number, mode);
            infoList.add(info);
        }
        cursor.close();
        database.close();
        return infoList;
    }

    /**
     * 部分查询--->用于ListView的分页加载
     * @param index 从index开始加载，每次加载20条
     * @return
     */
    public List<BlackNumberInfo> queryPart(int index) {
        SystemClock.sleep(600);
        List<BlackNumberInfo> infoList = new ArrayList<>();
        SQLiteDatabase database = helper.getWritableDatabase();
        String sql = "select number, mode from blacknumber order by _id desc limit 20 offset ?";
        Cursor cursor = database.rawQuery(sql, new String[]{index + ""});
        while (cursor.moveToNext()) {
            String number = cursor.getString(0);
            String mode = cursor.getString(1);
            BlackNumberInfo info = new BlackNumberInfo(number, mode);
            infoList.add(info);
        }
        cursor.close();
        database.close();
        return infoList;
    }

    /**
     *得到数据库中黑名单的总的记录数
     * @return
     */
    public int getCount() {
        int count = 0;
        SQLiteDatabase database = helper.getWritableDatabase();
        String sql = "select count(*) from blacknumber";
        Cursor cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        database.close();
        return count;
    }

}
