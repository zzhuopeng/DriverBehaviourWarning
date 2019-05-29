package com.cqupt.driverbehaviourwarning.parallel;

import com.cqupt.driverbehaviourwarning.MyApplication;
import com.cqupt.driverbehaviourwarning.model.WarningMessage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析Json的Runnable
 */
public class ParseJsonRunnable implements Runnable {
    private static final String TAG = "ParseJsonRunnable";

    private String jsonString = null;

    public ParseJsonRunnable(final String jsonString) {
        this.jsonString = jsonString;
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

            if (jsonObject.getString("ID").equals("0")) {
                //使用Gson直接将Json转对象
                Gson gson = new Gson();
                synchronized (MyApplication.lock) {
                    MyApplication.warningMessage = gson.fromJson(jsonObject.toString(), WarningMessage.class);
                    MyApplication.lock.notifyAll();//通知被锁住的线程，预警驾驶员
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}