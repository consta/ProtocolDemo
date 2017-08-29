package com.ccl.protocoldemo;


import android.content.Context;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPSocketReceive {
    private boolean mReceiving = false;
    private Context mContext;
    private DatagramSocket mSocket;
    private OnPacketReceiveListener mOnPacketReceiveListener;

    public interface OnPacketReceiveListener{
        void OnPacketReceive(DatagramPacket packet);
    }

    public UDPSocketReceive(Context context){
        mContext = context;
    }

    public void setListener(OnPacketReceiveListener l){
        mOnPacketReceiveListener = l;
        ThreadPoolUtils.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                receive();
            }
        });
    }

    public void stopReceive(){
        mReceiving = false;
        if (mSocket != null && !mSocket.isClosed()) {
            mSocket.close();
        }
    }

    public void receive() {
        mReceiving = true;
        try {
            String localIP = MacAddressUtils.getIpAddress(mContext);
            mSocket = new DatagramSocket(10080);
            while (mReceiving) {
                byte[] buf = new byte[1240];
                DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
                mSocket.receive(packet);
                if (!packet.getAddress().getHostAddress().equals(localIP)) {
                    if(mOnPacketReceiveListener != null){
                        mOnPacketReceiveListener.OnPacketReceive(packet);
                    }
                }
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
