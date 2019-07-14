package com.cqupt.driverbehaviourwarning.parallel;

import android.util.Log;

import com.cqupt.driverbehaviourwarning.activity.MyApplication;
import com.cqupt.driverbehaviourwarning.model.WarningMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 解析Json的Runnable
 */
public class ParseJsonRunnable implements Runnable {
    private static final String TAG = "ParseJsonRunnable";

    private String jsonString = null;
    private BufferedWriter bufferedWriter = null;

    public ParseJsonRunnable(BufferedWriter bufferedWriter, final String jsonString) {
        this.jsonString = jsonString;
        this.bufferedWriter = bufferedWriter;
    }

    @Override
    public void run() {
        ParseJson();
    }

    /**
     * 解析Json数据
     */
    private void ParseJson() {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonString);

            if (jsonObject.has("ID") && jsonObject.getString("ID").equals("0")) {
                //使用Gson直接将Json转对象
                Gson gson = new Gson();
                synchronized (MyApplication.lock) {
                    MyApplication.warningMessage.add(gson.fromJson(jsonObject.toString(), WarningMessage.class));
                    MyApplication.lock.notifyAll();//通知被锁住的线程，预警驾驶员
                }
            }

            if (jsonObject.has("LIVE") && jsonObject.getString("LIVE").equals("1")) {
                //异常检测
                if (null == bufferedWriter) {
                    Log.i(TAG, "ParseJson: Please init bufferWriter");
                    return;
                }
                //回应OBU的心跳数据
                JsonObject object = new JsonObject();
                object.addProperty("LIVE", "1");
                bufferedWriter.write(object.toString());
                bufferedWriter.flush();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}