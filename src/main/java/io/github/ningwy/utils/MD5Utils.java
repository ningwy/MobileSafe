package io.github.ningwy.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5工具类，用于将明文生成md5类型值
 * Created by ningwy on 2016/4/16.
 */
public class MD5Utils {

    /**
     * 由明文密码生成MD5密码
     * @param password 明文密码
     * @return 生成的MD5密码
     */
    public static String getMD5Secret(String password) {
        //用于存储得到的MD5密码
        StringBuffer buffer = new StringBuffer();
        try {
        //1.得到消息摘要器
            //指明由MD5算法得到的消息摘要器
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //2.用消息摘要器将明文密码转换为一个字节数组
            byte[] bytes = digest.digest(password.getBytes());
            //3.遍历得到的字节数组，将每个字节与8个二进制位(0xff或255或11111111)进行与运算，
            // 得到一个int类型的数字
            for (byte b : bytes) {
                int number = b & 0xff;
                //4.将得到的int类型的数字转换为16进制，因为转换为16进制时有可能只有一个16进制位，
                String numberStr = Integer.toHexString(number);
                // 而一个16进制位只相当于4个二进制位，不足8个二进制位，因此需要在不足的16进制位前面加0
                if (numberStr.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(numberStr);
            }
            //5.返回得到的16进制串
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //若发生异常，返回空串
            return "";
        }
    }
}
