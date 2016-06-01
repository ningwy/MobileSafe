package io.github.ningwy.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.github.ningwy.utils.MD5Utils;

/**
 * 病毒库的dao
 * Created by ningwy on 2016/4/27.
 */
public class AntiVirusDao {

    /**
     *得到病毒的描述信息
     * @param signature 签名信息
     * @return 病毒的描述信息,并且有返回值说明是病毒，返回null则为正常软件
     */
    public static String getVirusSignature(String signature) {
        //要查询得到的地址
        String result = null;
        String path = "/data/data/io.github.ningwy/files/antivirus.db";
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        String sql = "select desc from datable where md5 = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{MD5Utils.getMD5Secret(signature)});
        while (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        cursor.close();
        sqLiteDatabase.close();
        return result;
    }

}
