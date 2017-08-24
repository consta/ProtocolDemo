package com.ccl.protocoldemo;


import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TcpClient implements SocketInterface {

    private static TcpClient mInstance;

    public static void init(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            mInstance = new TcpClient(ip);
        }
    }

    public static TcpClient getClient() {
        return mInstance;
    }

    private String mServerIp;
    private boolean mInputRun;

    private Socket mSocket;
    private BufferedReader mBufferedReader;
    private BufferedWriter mBufferedWriter;
    private MainActivity.OnConnectListener mConnectListener;
    private MainActivity.OnInputDataListener mInputDataListener;

    private TcpClient(String ip) {
        this.mServerIp = ip;
    }

    private void startConnect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Log.e("IPIP", "mServerIp: " + mServerIp);
                    mSocket = new Socket(mServerIp, 10081);
                    if (mConnectListener != null) {
                        mBufferedWriter = new BufferedWriter(
                                new OutputStreamWriter(mSocket.getOutputStream()));
                        mConnectListener.onConnect();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mInputRun = true;
                                try {
                                    mBufferedReader = new BufferedReader(
                                            new InputStreamReader(mSocket.getInputStream()));
                                    while (mInputRun) {
                                        if (mSocket != null && !mSocket.isClosed() && mBufferedReader != null) {
                                            String data = "";
                                            String len;
                                            while ((len = mBufferedReader.readLine()) != null) {
                                                data += len;
                                            }
                                            if (mInputDataListener != null) {
                                                mInputDataListener.onSocketDataInput(data);
                                            }
                                        } else {
                                            mInputRun = false;
                                        }
                                    }
                                } catch (IOException eee) {
                                    eee.printStackTrace();
                                    if (mBufferedWriter != null) {
                                        try {
                                            mBufferedWriter.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (mBufferedReader != null) {
                                        try {
                                            mBufferedReader.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }).start();
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
        }).start();
    }

    @Override
    public void setListener(MainActivity.OnInputDataListener l) {
        mInputDataListener = l;
    }

    @Override
    public void setListener(MainActivity.OnConnectListener l) {
        mConnectListener = l;
        if (!TextUtils.isEmpty(mServerIp) && mConnectListener != null) {
            startConnect();
        }
    }

    @Override
    public void sendData(String data) {
        Log.e("sendMessage", "sendData 1");
        if (mBufferedWriter != null) {
            Log.e("sendMessage", "sendData 2");
            try {
                Log.e("sendMessage", "sendData 3");
                mBufferedWriter.write(data);
                mBufferedWriter.flush();
                mSocket.shutdownOutput();
                mBufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(mSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void stopSocket() {
        mInputRun = false;
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
