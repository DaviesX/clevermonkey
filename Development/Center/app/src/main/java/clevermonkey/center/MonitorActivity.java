package clevermonkey.center;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


public class MonitorActivity extends AppCompatActivity {

    //
    Socket m_socket;
    InputStream m_inputStream;
    OutputStream m_outputStream;
    byte[] m_byte = new byte[1280 * 720 * 4];
    int m_byteRead=0;
    Handler m_handler;

    //
    final int k_port = 35555;
    final static int k_msgTypeText=1;
    final static int k_msgTypeImg=2;
    final static int k_msgTypeControl=3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置为准备界面。
        setContentView(R.layout.activity_monitor_pre);

        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        //提示。
        //显示当前wifi信息。
        String msg;
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            msg = wifiInfo.getBSSID() == null ? "WIFI未连接到热点。\n请点击 Connect WIFI 按钮连接到目标热点。"
                    : "WIFI已连接到：\nSSID: " + wifiInfo.getSSID() + "\nBSSID: " + wifiInfo.getBSSID();
        } else {
            msg = "WIFI未开启。";
        }
        ((TextView) findViewById(R.id.textView_monitor_pre)).setText(msg);

        //设置消息处理例程。
        m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case k_msgTypeText:
                        ((TextView)findViewById(R.id.textView_monitor)).setText((String)msg.obj);
                        break;

                    case k_msgTypeControl:
                        setContentView(R.layout.activity_monitor);
                        break;

                    case k_msgTypeImg:
                        try {
                            Bitmap bmp=BitmapFactory.decodeByteArray(m_byte, 0, m_byteRead);
                            ((ImageView) findViewById(R.id.imageView_monitor)).setImageBitmap(bmp);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        break;
                }

            }
        };

        //注册"Connect wifi"按钮回调。
        ((Button) findViewById(R.id.btn_monitor_pre_connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //启动系统设置程序连接到目标热点。
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                //提示。
                //显示当前wifi信息。
                String msg;
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    msg = wifiInfo.getBSSID() == null ? "WIFI未连接到热点。\n请点击 Connect WIFI 按钮连接到目标热点。"
                            : "WIFI已连接到：\nSSID: " + wifiInfo.getSSID() + "\nBSSID: " + wifiInfo.getBSSID();
                } else {
                    msg = "WIFI未开启。";
                }
                ((TextView) findViewById(R.id.textView_monitor_pre)).setText(msg);
            }
        });

        //注册"Connect Server"按钮回调。
        ((Button) findViewById(R.id.btn_monitor_pre_sever)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不能在主线程执行网络操作。需启动新线程异步连接。
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //建立连接。
                        try {
                            m_socket = new Socket(((EditText) findViewById(R.id.editText_monitor_pre)).getText().toString(), k_port);
                            m_inputStream = m_socket.getInputStream();
                            m_outputStream = m_socket.getOutputStream();
                            //连接到目标后进入工作界面。
                            Message msg = Message.obtain();
                            msg.what=k_msgTypeControl;
                            msg.obj = R.layout.activity_monitor;
                            m_handler.sendMessage(msg);
                            //提示。
                            msg = Message.obtain();
                            msg.what=k_msgTypeText;
                            msg.obj ="远程Monitor已连接。";
                            m_handler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        //启用定时器定时读取输入流并刷新画面。
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (m_inputStream != null && m_inputStream.available() > 0) {
                        m_byteRead=m_inputStream.read(m_byte);
                        //通知UI线程刷新。
                        Message msg = Message.obtain();
                        msg.what=k_msgTypeImg;
                        m_handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000, 80);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            //清理连接。
            if (m_socket != null)
                m_socket.close();
            if (m_inputStream != null)
                m_inputStream.close();
            if (m_outputStream != null)
                m_outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
