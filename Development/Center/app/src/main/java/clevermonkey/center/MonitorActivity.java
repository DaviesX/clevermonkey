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

        //璁剧疆涓哄噯澶囩晫闈€
        setContentView(R.layout.activity_monitor_pre);

        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        //鎻愮ず銆
        //鏄剧ず褰撳墠wifi淇℃伅銆
        String msg;
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            msg = wifiInfo.getBSSID() == null ? "WIFI鏈繛鎺ュ埌鐑偣銆俓n璇风偣鍑 Connect WIFI 鎸夐挳杩炴帴鍒扮洰鏍囩儹鐐广€"
                    : "WIFI宸茶繛鎺ュ埌锛歕nSSID: " + wifiInfo.getSSID() + "\nBSSID: " + wifiInfo.getBSSID();
        } else {
            msg = "WIFI鏈紑鍚€";
        }
        ((TextView) findViewById(R.id.textView_monitor_pre)).setText(msg);

        //璁剧疆娑堟伅澶勭悊渚嬬▼銆
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

        //娉ㄥ唽"Connect wifi"鎸夐挳鍥炶皟銆
        ((Button) findViewById(R.id.btn_monitor_pre_connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //鍚姩绯荤粺璁剧疆绋嬪簭杩炴帴鍒扮洰鏍囩儹鐐广€
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                //鎻愮ず銆
                //鏄剧ず褰撳墠wifi淇℃伅銆
                String msg;
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    msg = wifiInfo.getBSSID() == null ? "WIFI鏈繛鎺ュ埌鐑偣銆俓n璇风偣鍑 Connect WIFI 鎸夐挳杩炴帴鍒扮洰鏍囩儹鐐广€"
                            : "WIFI宸茶繛鎺ュ埌锛歕nSSID: " + wifiInfo.getSSID() + "\nBSSID: " + wifiInfo.getBSSID();
                } else {
                    msg = "WIFI鏈紑鍚€";
                }
                ((TextView) findViewById(R.id.textView_monitor_pre)).setText(msg);
            }
        });

        //娉ㄥ唽"Connect Server"鎸夐挳鍥炶皟銆
        ((Button) findViewById(R.id.btn_monitor_pre_sever)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //涓嶈兘鍦ㄤ富绾跨▼鎵ц缃戠粶鎿嶄綔銆傞渶鍚姩鏂扮嚎绋嬪紓姝ヨ繛鎺ャ€
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //寤虹珛杩炴帴銆
                        try {
                            m_socket = new Socket(((EditText) findViewById(R.id.editText_monitor_pre)).getText().toString(), k_port);
                            m_inputStream = m_socket.getInputStream();
                            m_outputStream = m_socket.getOutputStream();
                            //杩炴帴鍒扮洰鏍囧悗杩涘叆宸ヤ綔鐣岄潰銆
                            Message msg = Message.obtain();
                            msg.what=k_msgTypeControl;
                            msg.obj = R.layout.activity_monitor;
                            m_handler.sendMessage(msg);
                            //鎻愮ず銆
                            msg = Message.obtain();
                            msg.what=k_msgTypeText;
                            msg.obj ="杩滅▼Monitor宸茶繛鎺ャ€";
                            m_handler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        //鍚敤瀹氭椂鍣ㄥ畾鏃惰鍙栬緭鍏ユ祦骞跺埛鏂扮敾闈€
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (m_inputStream != null && m_inputStream.available() > 0) {
                        m_byteRead=m_inputStream.read(m_byte);
                        //閫氱煡UI绾跨▼鍒锋柊銆
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
            //娓呯悊杩炴帴銆
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
