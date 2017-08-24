package com.ccl.protocoldemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TcpConnectActivity extends AppCompatActivity {
    public static final int TYPE_SERVER = 0;
    public static final int TYPE_CLIENT = 1;

    private int mType = -1;
    private SocketInterface mSocket;
    private TextView mTvMessage;
    private EditText mEtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_connect);
        Intent intent = getIntent();
        mType = intent.getIntExtra("type", -1);
        if (mType == TYPE_CLIENT) {
            mSocket = TcpClient.getClient();
        } else if (mType == TYPE_SERVER) {
            mSocket = TcpServer.getServer();
        } else {
            finish();
            return;
        }

        if (mSocket == null) {
            finish();
        } else {
            mTvMessage = (TextView) findViewById(R.id.tv_message);
            mEtMessage = (EditText) findViewById(R.id.et_message);
            mSocket.setListener(new MainActivity.OnInputDataListener() {
                @Override
                public void onSocketDataInput(String data) {
                    final String s = mTvMessage.getText().toString() + "\r\n其他: " + data;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvMessage.setText(s);
                        }
                    });
                }
            });
        }
    }

    public void sendMessage(View v) {
        final String trim = mEtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(trim)) {
            Toast.makeText(getBaseContext(), "请输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        mEtMessage.setText("");
        Log.e("sendMessage", "trim: " + trim);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSocket.sendData(trim);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSocket.stopSocket();
            }
        }).start();
    }
}
