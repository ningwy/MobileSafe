package io.github.ningwy.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 从联系人归属地数据库查询归属地的dao
 * Created by ningwy on 2016/4/21.
 */
public class QueryAddressDao {

    /**
     * 从数据库里查询归属地
     * @param number 要查询的号码
     * @return 归属地
     */
    public static String queryAddressFromDB(String number) {
        //要查询得到的地址
        String address = number;
        String path = "/data/data/io.github.ningwy/files/address.db";
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        //电话号码的正则表达式
        String regx = "^1[345678]\\d{9}$";
        if (number.matches(regx)) {
            String sql = "select location from data2 where id = (select outkey from data1 where id = ?)";
            //根据号码查询只需前面七位即可
            String[] selectionArgs = new String[]{number.substring(0, 7)};
            Cursor cursor = sqLiteDatabase.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                address = cursor.getString(0);
            }
            cursor.close();
        } else {
            switch (number.length()) {
                //3位为警察电话，110,120
                case 3:
                    address = "国家机关";
                    break;
                //4位为模拟器电话
                case 4:
                    address = "模拟器";
                    break;
                //5位为服务热线，如10086
                case 5:
                    address = "服务热线";
                    break;
                //什么都不是则为本地号码如 0759-xxxx
                default:
                    String sql = "select location from data2 where area = ?";
                    //当本地号码为如010-xxxxxx时
                    String[] selectionArgs = new String[]{number.substring(1, 3)};
                    Cursor cursor = sqLiteDatabase.rawQuery(sql, selectionArgs);
                    while (cursor.moveToNext()) {
                        address = cursor.getString(0);
                    }
                    //当本地号码为如0855-xxxx时
                    selectionArgs = new String[]{number.substring(1, 4)};
                    cursor = sqLiteDatabase.rawQuery(sql, selectionArgs);
                    while (cursor.moveToNext()) {
                        address = cursor.getString(0);
                    }
                    cursor.close();
                    break;
            }
        }
        sqLiteDatabase.close();
        return address;
    }

}
