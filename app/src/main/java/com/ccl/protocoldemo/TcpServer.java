package com.ccl.protocoldemo;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class TcpServer extends BaseTcpSocket {

    private static TcpServer mInstance;

    public static void Init(){
        mInstance = new TcpServer();
    }
    public static TcpServer getTcpServer(){
        return mInstance;
    }

    private TcpServer() {
    }

    @Override
    public void startSocket() {
        ThreadPoolUtils.getInstance().addTask(new Runnable() {
            @Override
            public void run() {

                try {
                    ServerSocket serverSocket = new ServerSocket(10081, 1);
                    mSocket = serverSocket.accept();
                    serverSocket.close();
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
                } catch (Exception ee) {
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
