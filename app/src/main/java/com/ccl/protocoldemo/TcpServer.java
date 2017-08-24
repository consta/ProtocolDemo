package com.ccl.protocoldemo;


import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements SocketInterface {

    private ServerSocket mServerSocket;
    private Socket mSocket;
    private BufferedReader mBufferedReader;
    private BufferedWriter mBufferedWriter;

    private MainActivity.OnConnectListener mConnectListener;
    private MainActivity.OnInputDataListener mInputDataListener;

    private static TcpServer mInstance;
    private boolean mInputRun;

    public static TcpServer getServer() {
        if (mInstance == null) {
            mInstance = new TcpServer();
        }
        return mInstance;
    }

    public final Object LOCK = new Object();

    private TcpServer() {
    }

    @Override
    public void setListener(MainActivity.OnConnectListener l) {
        synchronized (LOCK) {
            if ((mConnectListener = l) != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startServer();
                    }
                }).start();
            }
        }
    }

    @Override
    public void setListener(MainActivity.OnInputDataListener l) {
        mInputDataListener = l;
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

    private void startServer() {
        try {
            mServerSocket = new ServerSocket(10081, 1);
            mSocket = mServerSocket.accept();
            if (mConnectListener != null) {
                mBufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(mSocket.getOutputStream()));
                mConnectListener.onConnect();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mInputRun = true;
                        try {
                            while (mInputRun) {
                                mBufferedReader = new BufferedReader(
                                        new InputStreamReader(mSocket.getInputStream()));
                                if (mSocket != null && !mSocket.isClosed() && mBufferedReader != null) {
                                    String data = "";
                                    String len;
                                    while ((len = mBufferedReader.readLine()) != null) {
                                        data += len;
                                    }
                                    if (mInputDataListener != null && !TextUtils.isEmpty(data)) {
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
                if (mServerSocket != null && !mServerSocket.isClosed()) {
                    try {
                        mServerSocket.close();
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
            if (mServerSocket != null && !mServerSocket.isClosed()) {
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        if (mServerSocket != null && !mServerSocket.isClosed()) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
