package com.cqupt.driverbehaviourwarning.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server Socket：接收Client Socket连接
 */
public class TCPService extends Service {
    private final static String TAG = "TCPService";

    //接收Client Socket连接线程
    ServiceThread serviceThread;

    //Server Socket
    private ServerSocket serversocket;

    //开启socket线程的次数
    private int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: 创建socket 端口6003");
        try {
            serversocket = new ServerSocket(6003);
            Log.i(TAG, "TCPService->>>>onCreate: new ServerSocket success");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "TCPService->>>>onCreate: new ServerSocket occur exception");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: 等待连接线程");
        if (serviceThread == null) {
            serviceThread = new ServiceThread();
            serviceThread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: 关闭socket连接");

        if (serversocket != null) {
            try {
                serversocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    /**
     * 接收Client连接线程
     */
    class ServiceThread extends Thread {

        @Override
        public void run() {

            while (true) {
                if (null == serversocket) {
                    Log.w(TAG, "ServiceThread--->>>serversocket can not be null.");
                    return;
                }
                //如果serversocket已经关闭，则关闭本线程
                if (serversocket.isClosed()) {
                    Log.e(TAG, "ServiceThread--->>>serversocket is closed.");
                    break;
                }
                try {
                    count++;
                    Socket socket = serversocket.accept();//阻塞
                    OBUClient client = new OBUClient(socket);
                    client.start();
                    Log.i(TAG, "----------有客户端连接，客户端为" + count + "号-----------");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
