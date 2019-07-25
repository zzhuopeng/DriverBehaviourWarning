package com.cqupt.driverbehaviourwarning.service;

import android.util.Log;

import com.cqupt.driverbehaviourwarning.activity.MyApplication;
import com.cqupt.driverbehaviourwarning.model.WarningMessage;
import com.cqupt.driverbehaviourwarning.utils.ThreadPoolUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;


/**
 * 通过线程池管理：解析Socket Client的Json数据的Runnable
 */
public class OBUClient extends Thread {
    private static final String TAG = "OBUClient";

    private String clientID = "";
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;

    public OBUClient(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try {
            String matches;
            if (null == br) {
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
            }
            if (null == bw) {
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);
            }
            while ((matches = br.readLine()) != null) {
                Log.i(TAG, "从Mk5接受到的信息为：" + matches);
                ParseJson(matches);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (br != null) {
                    br.close();
                }
                if (bw != null) {
                    bw.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 解析Json数据
     */
    private void ParseJson(String jsonString) {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonString);

            //预警信息
            if (jsonObject.has("ID") && jsonObject.getString("ID").equals("0")) {
                //使用Gson直接将Json转对象
                Gson gson = new Gson();
                synchronized (MyApplication.lock) {
                    MyApplication.warningMessage.offer(gson.fromJson(jsonObject.toString(), WarningMessage.class));
                    MyApplication.lock.notifyAll();//通知被锁住的线程，预警驾驶员
                }
            }
            //clientID
            if (jsonObject.has("clientID")) {
                clientID = (String) jsonObject.get("clientID");
                if ("HeartBeat".equals(clientID)) {
                    ThreadPoolUtils.getInstance().getScheduledThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                //回应OBU的心跳数据
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("LIVE", "1");
                                    send(object);
                                    sleep(1000);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送Json消息
     *
     * @param headParam 需要发送的Json消息
     */
    public void send(JSONObject headParam) {
        try {
            if (null == bw) {
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);
            }

            bw.write(headParam.toString());
            bw.flush();
            Log.i(TAG, "socket send: " + headParam.toString());

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (br != null) {
                    br.close();
                }
                if (bw != null) {
                    bw.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}