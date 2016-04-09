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

    //鍞竴鐨凚luetoothAdapter瀵硅薄銆
    protected BluetoothAdapter m_bluetoothAdapter;
    //鍞竴鐨凚luetoothSocket瀵硅薄銆
    protected BluetoothSocket m_bluetoothSocket;
    //鍞竴鐨凮utputStream瀵硅薄銆
    protected OutputStream m_outputStream;

    //鎺ュ彛銆

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        //娉ㄥ唽鎸夐挳鍥炶皟銆

        //杩炴帴鎸夐挳銆
        ((Button) findViewById(R.id.btn_remote_connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //鍚姩钃濈墮銆
                m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                m_bluetoothAdapter.enable();

                //璁剧疆Bluetooth閫氱煡鍥炶皟銆
                final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        // 鍙戠幇璁惧銆
                        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                            // 浠嶪ntent涓幏鍙栬澶囧璞°€
                            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            //楠岃瘉鏄惁涓虹洰鏍囪澶囥€
                            if(bluetoothDevice.getName().equals(k_carName))
                            {
                                //鎻愮ず銆
                                ((TextView) findViewById(R.id.txtView_remote_main)).setText("鎵惧埌鐩爣璁惧: "+bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                                unregisterReceiver(this);
                                m_bluetoothAdapter.cancelDiscovery();
                                try {
                                    //杩炴帴鍒拌澶囥€
                                    m_bluetoothSocket=bluetoothDevice.createInsecureRfcommSocketToServiceRecord(k_uuid);
                                    m_bluetoothSocket.connect();
                                    m_outputStream = m_bluetoothSocket.getOutputStream();
                                    //鎻愮ず銆
                                    ((TextView) findViewById(R.id.txtView_remote_main)).setText("杩炴帴鍒扮洰鏍囪澶: "+bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //鎻愮ず銆
                                    ((TextView) findViewById(R.id.txtView_remote_main)).setText("鏃犳硶杩炴帴鍒扮洰鏍囪澶: "+bluetoothDevice.getName() + " " + bluetoothDevice.getAddress()+"\n璇峰厛閰嶅銆");
                                }
                            }
                        }
                        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction()))
                        {
                            unregisterReceiver(this);
                            //鎻愮ず銆
                            ((TextView) findViewById(R.id.txtView_remote_main)).setText("鎵句笉鍒扮洰鏍囪澶囥€");
                        }
                    }
                };
                // 娉ㄥ唽BroadcastReceiver銆
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(broadcastReceiver, intentFilter);

                //寮€濮嬫悳绱€
                if (!m_bluetoothAdapter.startDiscovery())
                    ;
            }
        });

        //鍓嶈繘鎸夐挳銆
        ((Button) findViewById(R.id.btn_remote_ahead)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //鍙戦€佷俊鎭€
                try {
                    m_outputStream.write(new byte[]{0x1});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //鍙宠浆鎸夐挳銆
        ((Button) findViewById(R.id.btn_remote_right)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //鍙戦€佷俊鎭€
                try {
                    m_outputStream.write(new byte[]{0x2});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //鍚庨€€鎸夐挳銆
        ((Button) findViewById(R.id.btn_remote_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //鍙戦€佷俊鎭€
                try {
                    m_outputStream.write(new byte[]{0x4});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //宸﹁浆鎸夐挳銆
        ((Button) findViewById(R.id.btn_remote_left)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //鍙戦€佷俊鎭€
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

        //鍏抽棴杩炴帴銆
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
