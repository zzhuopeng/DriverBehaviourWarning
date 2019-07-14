package com.cqupt.driverbehaviourwarning.service;

import android.util.Log;

import com.cqupt.driverbehaviourwarning.parallel.ParseJsonRunnable;
import com.cqupt.driverbehaviourwarning.utils.ThreadPoolUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


/**
 * 通过线程池管理：解析Socket Client的Json数据的Runnable
 */
public class OBUClient extends Thread {
    private static final String TAG = "OBUClient";

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
                ThreadPoolUtils.getInstance().getScheduledThreadPool().execute(new ParseJsonRunnable(bw, matches));
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