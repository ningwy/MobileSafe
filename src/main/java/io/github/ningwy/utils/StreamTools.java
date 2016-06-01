package io.github.ningwy.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ningwy on 2016/4/13.
 */
public class StreamTools {

    /**
     *
     * @param inputStream 输入流
     * @return 从输入流中获取到的数据并转换为String
     * @throws IOException
     */
    public static String readFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        String result = baos.toString();
        inputStream.close();
        baos.close();
        return result;
    }

}
