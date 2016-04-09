package clevermonkey.center;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class RemoteActivity extends AppCompatActivity {

    //UUID.
    //Insecure.
    protected UUID k_uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //Car Name.
    protected String k_carName = "HC-05";

    //唯一的BluetoothAdapter对象。
    protected BluetoothAdapter m_bluetoothAdapter;
    //唯一的BluetoothSocket对象。
    protected BluetoothSocket m_bluetoothSocket;
    //唯一的OutputStream对象。
    protected OutputStream m_outputStream;

    //接口。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        //注册按钮回调。

        //连接按钮。
        ((Button) findViewById(R.id.btn_remote_connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //启动蓝牙。
                m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                m_bluetoothAdapter.enable();

                //设置Bluetooth通知回调。
                final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        // 发现设备。
                        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                            // 从Intent中获取设备对象。
                            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            //验证是否为目标设备。
                            if(bluetoothDevice.getName().equals(k_carName))
                            {
                                //提示。
                                ((TextView) findViewById(R.id.txtView_remote_main)).setText("找到目标设备: "+bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                                unregisterReceiver(this);
                                m_bluetoothAdapter.cancelDiscovery();
                                try {
                                    //连接到设备。
                                    m_bluetoothSocket=bluetoothDevice.createInsecureRfcommSocketToServiceRecord(k_uuid);
                                    m_bluetoothSocket.connect();
                                    m_outputStream = m_bluetoothSocket.getOutputStream();
                                    //提示。
                                    ((TextView) findViewById(R.id.txtView_remote_main)).setText("连接到目标设备: "+bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //提示。
                                    ((TextView) findViewById(R.id.txtView_remote_main)).setText("无法连接到目标设备: "+bluetoothDevice.getName() + " " + bluetoothDevice.getAddress()+"\n请先配对。");
                                }
                            }
                        }
                        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction()))
                        {
                            unregisterReceiver(this);
                            //提示。
                            ((TextView) findViewById(R.id.txtView_remote_main)).setText("找不到目标设备。");
                        }
                    }
                };
                // 注册BroadcastReceiver。
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(broadcastReceiver, intentFilter);

                //开始搜索。
                if (!m_bluetoothAdapter.startDiscovery())
                    ;
            }
        });

        //前进按钮。
        ((Button) findViewById(R.id.btn_remote_ahead)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送信息。
                try {
                    m_outputStream.write(new byte[]{0x1});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //右转按钮。
        ((Button) findViewById(R.id.btn_remote_right)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送信息。
                try {
                    m_outputStream.write(new byte[]{0x2});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //后退按钮。
        ((Button) findViewById(R.id.btn_remote_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送信息。
                try {
                    m_outputStream.write(new byte[]{0x4});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //左转按钮。
        ((Button) findViewById(R.id.btn_remote_left)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送信息。
                try {
                    m_outputStream.write(new byte[]{0x8});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //关闭连接。
        try {
            if(m_bluetoothSocket!=null)
                m_bluetoothSocket.close();
            if(m_outputStream!=null)
                m_outputStream.close();
            if (m_bluetoothAdapter != null)
                m_bluetoothAdapter.disable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
