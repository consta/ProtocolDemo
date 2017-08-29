package com.ccl.protocoldemo;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class BaseTcpSocket {

    protected boolean mInputRun;

    protected Socket mSocket;
    protected BufferedInputStream mBufferedInputStream;
    protected BufferedOutputStream mBufferedOutputStream;
    protected OnConnectListener mConnectListener;
    protected OnInputDataListener mInputDataListener;

    public interface OnInputDataListener {
        void onSocketDataInput(String data);
    }

    public interface OnConnectListener {
        void onConnect();
    }

    public abstract void startSocket();

    public void setListener(OnInputDataListener l) {
        mInputDataListener = l;
    }

    public void setListener(OnConnectListener l) {
        if ((mConnectListener = l) != null) {
            startSocket();
        }
    }

    public void sendData(String data) {
        if (mBufferedOutputStream != null && mSocket != null && !mSocket.isOutputShutdown()) {
            try {
                byte[] bytes = data.getBytes();
                mBufferedOutputStream.write(Utils.intToByteArray1(bytes.length));
                mBufferedOutputStream.flush();
                mBufferedOutputStream.write(bytes);
                mBufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void startReceive() {
        ThreadPoolUtils.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                mInputRun = true;
                try {
                    mBufferedInputStream = new BufferedInputStream(mSocket.getInputStream());
                    byte[] head = new byte[4];
                    while (mInputRun) {
                        if (mSocket != null && !mSocket.isClosed() && mBufferedInputStream != null) {
                            mBufferedInputStream.read(head);
                            int headLen = Utils.byteArrayToInt(head);
                            byte[] content = new byte[headLen];
                            mBufferedInputStream.read(content);
                            String data = new String(content).trim();
                            if (mInputDataListener != null ) {
                                if("exit".equals(data)){
                                    mInputRun = false;
                                }
                                mInputDataListener.onSocketDataInput(data);
                            }
                        } else {
                            mInputRun = false;
                        }
                    }
                } catch (IOException eee) {
                    eee.printStackTrace();
                    if (mBufferedOutputStream != null) {
                        try {
                            mBufferedOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mBufferedInputStream != null) {
                        try {
                            mBufferedInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void stopSocket() {
        mInputRun = false;
        sendData("exit");
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.shutdownOutput();
                mSocket.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mBufferedOutputStream != null) {
            try {
                mBufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mBufferedInputStream != null) {
            try {
                mBufferedInputStream.close();
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