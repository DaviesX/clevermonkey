package clevermonkey.center;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class EngineActivity extends AppCompatActivity {

    //鎸囦护鍙戦€侀棿闅旓紙姣锛夈€
    protected int k_stepTime = 200;
    //棰勮鐨凪onitor杩炴帴绔彛銆
    protected final int k_port = 35555;

    //鍞竴鐨凜amera瀵硅薄銆
    protected Camera m_camera;
    //棰勮鍘熷鏁版嵁缂撳啿鍖恒€
    protected byte[] m_previewByte = new byte[1280 * 720 * 3 / 2];
    //棰勮鍘熷鏁版嵁鏍煎紡杞崲涓棿鍥惧儚鐭╁舰銆
    protected Rect m_previewRect = new Rect(0, 0, 1280, 720);
    //鏁版嵁澶勭悊鎺у埗鏍囧織銆
    protected boolean isOn = false;
    //鍞竴鐨凪onitor鐨勭綉缁滄帴鍙ｃ€
    protected Socket m_socket;
    //鍞竴鐨凪onitor杈撳叆娴併€
    protected InputStream m_inputStream;
    //鍞竴鐨凪onitor杈撳嚭娴併€
    protected OutputStream m_outputStream;
    //鍞竴鐨勬秷鎭鐞嗗櫒銆
    protected Handler m_handler;
    //
    protected  Bitmap m_previewBmp;
    protected    Bitmap m_betaBmp;
    //
    protected Rect m_cameraDetRect = new Rect(90, 0, 630, 960);
    protected Rect m_alphaDetRect = new Rect(90, 960, 270, 1280);
    protected Rect m_betaDetRect = new Rect(450, 960, 640, 1280);
    protected Rect m_cameraSrcRect = new Rect(0, 0, 720, 1280);
    protected Rect m_alphaSrcRect = new Rect(0, 0, 720, 1280);
    protected Rect m_betaSrcRect;
    //鍞竴鐨勭綉缁滅嚎绋嬪璞°€
    protected Thread m_netThread;
    //鍞竴鐨凾imerTask瀵硅薄銆傜敤浜庢帶鍒舵暟鎹鐞嗛棿闅斻€
    protected TimerTask m_timerTask = new TimerTask() {
        @Override
        public void run() {
            isOn = true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_engine);

        //璁剧疆SurfaceView灞炴€с€
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView_engine);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //褰揝urface鍒濆鍖栧悗鍚敤棰勮鐢婚潰銆
                try {
                    m_camera.setPreviewDisplay(holder);
                    m_camera.startPreview();
                    //鍥犱负宸ヤ綔鏃跺鐒﹁窛绂讳笉鍙橈紝鎵€浠ヤ粎闇€涓€娆¤嚜鍔ㄥ鐒﹀嵆鍙€
                    m_camera.autoFocus(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                m_camera.stopPreview();
            }
        });

        //鍚姩Camera銆
        m_camera = Camera.open();
        //璁剧疆Camera鍙傛暟銆
        Camera.Parameters parameters = m_camera.getParameters();
        parameters.setPreviewSize(1280, 720);
        parameters.setPreviewFpsRange(25000, 30000);
        m_camera.setParameters(parameters);
        m_camera.setDisplayOrientation(90);
        m_camera.addCallbackBuffer(m_previewByte);
        m_camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            //澶勭悊姣忎釜棰勮甯х殑鍥炶皟銆
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    //鏍规嵁鏍囧織鎺у埗鏄惁杩涜鏁版嵁澶勭悊锛屽噺灏戣祫婧愭秷鑰椼€
                    if (isOn) {
                        //杞崲涓篟GB鏍煎紡銆
                        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, 1280, 720, null);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(m_previewByte.length);
                        yuvImage.compressToJpeg(m_previewRect, 30, byteArrayOutputStream);
                        byte[] tmp = byteArrayOutputStream.toByteArray();
                        m_previewBmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);

                        isOn = false;
                        //灏嗗浘鍍忚緭鍏ヨ窡韪櫒銆
                        //鏄剧ず璺熻釜鍣ㄥ浘鍍忋€
                        ((ImageView) findViewById(R.id.imageView_engine_alpha)).setImageBitmap(m_previewBmp);

                        //鍙戦€佸埌杩滅▼Monitor銆
                        if (m_outputStream != null)
                            UpdateMonitor();
                    }
                    //閲嶆柊灏嗗凡浣跨敤瀹屾瘯鐨勭紦鍐叉暟缁勫姞鍏ュ洖闃熷垪銆
                    m_camera.addCallbackBuffer(m_previewByte);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //璁剧疆""鎸夐挳鍥炶皟銆
        //鍚姩宸ヤ綔杩囩▼銆
        ((Button) findViewById(R.id.button_engine_start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //璁惧畾瀹氭椂鍣ㄣ€
                new Timer().schedule(m_timerTask, 5000, k_stepTime);
            }
        });

        //鍒濆鍖栨秷鎭鐞嗕緥绋嬨€
        m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                ((TextView) findViewById(R.id.textView_engine)).setText((String) msg.obj);
            }
        };

        //鍒濆鍖朣erver銆
        //涓嶈兘鍦ㄤ富绾跨▼鎵ц缃戠粶鎿嶄綔銆傞渶鍚姩鏂扮嚎绋嬪紓姝ヨ繛鎺ャ€
        m_netThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(k_port);
                    //浠呮帴鍙椾竴涓鎴风杩炴帴銆
                    m_socket = serverSocket.accept();
                    serverSocket.close();
                    m_inputStream = m_socket.getInputStream();
                    m_outputStream = m_socket.getOutputStream();
                    //鎻愮ず銆
                    Message msg = new Message();
                    msg.obj = "杩滅▼Monitor宸茶繛鎺ャ€";
                    m_handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        m_netThread.start();

        //鎻愮ず銆
        //鑾峰彇wifi鏈嶅姟.銆
        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAdress = wifiInfo.getIpAddress();
        String ipString = (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
        ((TextView) findViewById(R.id.textView_engine)).setText("Monitor鏈繛鎺ャ€俓n鏈満IP: " + ipString);

    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            m_camera.release();
            m_timerTask.cancel();
            if(m_socket!=null)
                m_socket.close();
            if(m_inputStream!=null)
                m_inputStream.close();
            if(m_outputStream!=null)
                m_outputStream.close();
            if(m_netThread.isAlive())
                m_netThread.destroy();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    boolean UpdateMonitor() {
        //鏋勯€犲疄鏃剁敾闈綅鍥俱€
        //Bitmap toSendBmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
       // Canvas canvas = new Canvas(toSendBmp);
       // canvas.drawBitmap(m_previewBmp, m_cameraSrcRect, m_cameraDetRect, null);
       // if (m_alphaBmp != null)
        //    canvas.drawBitmap(m_alphaBmp, m_alphaSrcRect, m_alphaDetRect, null);
        //if (m_betaBmp != null)
       //     canvas.drawBitmap(m_betaBmp, m_betaSrcRect, m_betaDetRect, null);
        //Bitmap to byte array.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //toSendBmp.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        m_previewBmp.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        try {
            //Send.
            m_outputStream.write(byteArrayOutputStream.toByteArray());
            m_outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
