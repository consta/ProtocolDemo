package com.ccl.protocoldemo;


import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpClient extends BaseTcpSocket {

    private static TcpClient mInstance;
    public static void Init(String ip){
        mInstance = new TcpClient(ip);
    }
    public static TcpClient getTcpClient(){
        return mInstance;
    }

    private String mServerIp;

    private TcpClient(String ip) {
        this.mServerIp = ip;
    }

    @Override
    public void startSocket() {
        ThreadPoolUtils.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("IPIP", "mServerIp: " + mServerIp);
                    mSocket = new Socket(mServerIp, 10081);
                    if (mConnectListener != null) {
                        mBufferedOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
                        mConnectListener.onConnect();
                        startReceive();
                    } else {
                        if (mSocket != null && !mSocket.isClosed()) {
                            try {
                                mSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException ee) {
                    ee.printStackTrace();
                    if (mSocket != null && !mSocket.isClosed()) {
                        try {
                            mSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

}
