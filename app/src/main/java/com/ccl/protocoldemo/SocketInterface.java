package com.ccl.protocoldemo;


public interface SocketInterface {
    void setListener(MainActivity.OnInputDataListener l);
    void setListener(MainActivity.OnConnectListener l);
    void sendData(String data);
    void stopSocket();
}
