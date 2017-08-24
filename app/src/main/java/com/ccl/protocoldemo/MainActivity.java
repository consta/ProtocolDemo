package com.ccl.protocoldemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public interface OnInputDataListener {
        void onSocketDataInput(String data);
    }

    public interface OnConnectListener {
        void onConnect();
    }

    private EditText mEt;
    private TextView mTv;
    private boolean mReceiving;
    private boolean mSending = false;
    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnSendIp;
    private DatagramSocket mSocket;
    private ArrayList<String> mDevs;
    private ArrayAdapter<String> mAdapter;
    private AlertDialog.Builder mBuilder;
    private TcpServer mTcpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEt = (EditText) findViewById(R.id.et);
        mTv = (TextView) findViewById(R.id.tv);
        mBtnStart = (Button) findViewById(R.id.start_receive);
        mBtnStop = (Button) findViewById(R.id.stop_receive);
        mBtnSendIp = (Button) findViewById(R.id.send_ip);
        findViewById(R.id.iv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEt.setText("");
            }
        });
        mTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTv.setText("");
            }
        });
        mBtnStop.setEnabled(false);
        mDevs = new ArrayList<>();

        mBuilder = new AlertDialog.Builder(MainActivity.this);
        mAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mDevs);
        mBuilder.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ip = mDevs.get(which);
                mTv.setText(ip);
                stop(null);
                TcpClient.init(ip.split("ip: ")[1].split(",")[0]);
                TcpClient client = TcpClient.getClient();
                client.setListener(new OnConnectListener() {
                    @Override
                    public void onConnect() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getBaseContext(), TcpConnectActivity.class);
                                intent.putExtra("type", TcpConnectActivity.TYPE_CLIENT);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        });

        mBuilder.setCancelable(true);
        mBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                stop(null);
            }
        });

        mTcpServer = TcpServer.getServer();
    }

    public void send(View v) {
        final String trim = mEt.getText().toString().trim();
        if (!TextUtils.isEmpty(trim)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendData(trim, false);
                }
            }).start();
        } else {
            Toast.makeText(getBaseContext(), "请输入要发送的数据", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendip(View v) {
        if (mSending) {
            mBtnSendIp.setEnabled(false);
            mSending = false;
        } else {
            mBtnSendIp.setEnabled(false);
            final String localIP = "IOTIP:" + android.os.Build.BRAND + "-" + android.os.Build.VERSION.RELEASE;
            if (!TextUtils.isEmpty(localIP)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendData(localIP, true);
                    }
                }).start();
            } else {
                Toast.makeText(getBaseContext(), "请输入要发送的数据", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void start(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                receive();
            }
        }).start();
        mBtnStart.setEnabled(false);
    }

    public void stop(View v) {
        mDevs.clear();
        mReceiving = false;
        mBtnStop.setEnabled(false);
        mBtnStart.setEnabled(true);
        if (mSocket != null && !mSocket.isClosed()) {
            mSocket.close();
        }
    }

    private void receive() {
        mReceiving = true;
        try {
            String localIP = MacAddressUtils.getIpAddress(getBaseContext());
            mSocket = new DatagramSocket(10080);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnStop.setEnabled(true);
                }
            });
            while (mReceiving) {
                byte[] buf = new byte[124000];
                DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
                mSocket.receive(packet);
                Log.e("IPIPIP", "Local ip: " + localIP);
                Log.e("IPIPIP", "ip: " + packet.getAddress().getHostAddress());
                if (!packet.getAddress().getHostAddress().equals(localIP)) {
                    final String data = new String(packet.getData(), 0, packet.getLength()) + "ip: " + packet.getAddress().getHostAddress() + ", port: " + packet.getPort();
                    if (!TextUtils.isEmpty(data)) {
                        if (data.startsWith("IOTIP:")) {
                            String substring = data.substring(6);
                            if (!mDevs.contains(substring)) {
                                mDevs.add(substring);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mDevs.size() == 1) {
                                            mBuilder.show();
                                        }
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTv.setText(data);
                                }
                            });
                        }
                    }
                }
            }
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "接收失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private OnConnectListener mTcpServerListener = new OnConnectListener() {
        @Override
        public void onConnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendip(null);
                    Intent intent = new Intent(getBaseContext(), TcpConnectActivity.class);
                    intent.putExtra("type", TcpConnectActivity.TYPE_SERVER);
                    startActivity(intent);
                }
            });
        }
    };

    private void sendData(String data, boolean loop) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("255.255.255.255");
            byte[] bytes = data.getBytes();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 10080);
            if (loop) {
                mTcpServer.setListener(mTcpServerListener);
                mSending = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnSendIp.setEnabled(true);
                        mBtnSendIp.setText("停止发送IP");
                    }
                });
                while (mSending) {
                    socket.send(packet);
                    SystemClock.sleep(1000);
                }

            } else {
                socket.send(packet);
            }
            socket.close();
            if (loop) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnSendIp.setEnabled(true);
                        mBtnSendIp.setText("发送IP");
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "发送成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (socket != null) {
                socket.close();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "发送失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
