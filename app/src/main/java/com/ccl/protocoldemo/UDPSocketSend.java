package com.ccl.protocoldemo;


import android.os.SystemClock;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSocketSend {
    private boolean mSending = false;
    private String mData;
    private DatagramSocket mSocket;

    public void sendData(String data) {
        if (mData == null) {
            if (mSending) {
                stopSend();
            }
            mData = data;
            ThreadPoolUtils.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    startSend();
                }
            });
        }else{
            mData = data;
        }
    }

    public void stopSend() {
        mSending = false;
        if (mSocket != null && !mSocket.isClosed()) {
            mSocket.close();
        }
    }

    public void startSend() {
        mSocket = null;
        try {
            mSocket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("255.255.255.255");
            String data = "CCLID:" + "111" + "AGENT:" + android.os.Build.BRAND + android.os.Build.VERSION.RELEASE;
            byte[] bytes = data.getBytes();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 10080);
            mSending = true;
            while (mSending) {
                mSocket.send(packet);
                SystemClock.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
            }
        }
    }


}
