package cn.edu.bupt.intelligentsound.utils;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/12/23.
 * 在启动的时候不能使用
 */
@Component
public class HttpUtil {

    public static String getAllDevices_Service_DeviceAttr(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        System.out.println(response);
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    //
    public static String sendControl(String url, JSONObject jsonStr) throws IOException {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
        String str = jsonStr.toString();
        RequestBody body = RequestBody.create(JSON, str);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = null;

        response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }
}
