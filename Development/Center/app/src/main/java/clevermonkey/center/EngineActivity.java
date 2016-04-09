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

    //指令发送间隔（毫秒）。
    protected int k_stepTime = 200;
    //预设的Monitor连接端口。
    protected final int k_port = 35555;

    //唯一的Camera对象。
    protected Camera m_camera;
    //预览原始数据缓冲区。
    protected byte[] m_previewByte = new byte[1280 * 720 * 3 / 2];
    //预览原始数据格式转换中间图像矩形。
    protected Rect m_previewRect = new Rect(0, 0, 1280, 720);
    //数据处理控制标志。
    protected boolean isOn = false;
    //唯一的Monitor的网络接口。
    protected Socket m_socket;
    //唯一的Monitor输入流。
    protected InputStream m_inputStream;
    //唯一的Monitor输出流。
    protected OutputStream m_outputStream;
    //唯一的消息处理器。
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
    //唯一的网络线程对象。
    protected Thread m_netThread;
    //唯一的TimerTask对象。用于控制数据处理间隔。
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

        //设置SurfaceView属性。
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView_engine);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //当Surface初始化后启用预览画面。
                try {
                    m_camera.setPreviewDisplay(holder);
                    m_camera.startPreview();
                    //因为工作时对焦距离不变，所以仅需一次自动对焦即可。
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

        //启动Camera。
        m_camera = Camera.open();
        //设置Camera参数。
        Camera.Parameters parameters = m_camera.getParameters();
        parameters.setPreviewSize(1280, 720);
        parameters.setPreviewFpsRange(25000, 30000);
        m_camera.setParameters(parameters);
        m_camera.setDisplayOrientation(90);
        m_camera.addCallbackBuffer(m_previewByte);
        m_camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            //处理每个预览帧的回调。
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    //根据标志控制是否进行数据处理，减少资源消耗。
                    if (isOn) {
                        //转换为RGB格式。
                        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, 1280, 720, null);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(m_previewByte.length);
                        yuvImage.compressToJpeg(m_previewRect, 30, byteArrayOutputStream);
                        byte[] tmp = byteArrayOutputStream.toByteArray();
                        m_previewBmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);

                        isOn = false;
                        //将图像输入跟踪器。
                        //显示跟踪器图像。
                        ((ImageView) findViewById(R.id.imageView_engine_alpha)).setImageBitmap(m_previewBmp);

                        //发送到远程Monitor。
                        if (m_outputStream != null)
                            UpdateMonitor();
                    }
                    //重新将已使用完毕的缓冲数组加入回队列。
                    m_camera.addCallbackBuffer(m_previewByte);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //设置""按钮回调。
        //启动工作过程。
        ((Button) findViewById(R.id.button_engine_start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设定定时器。
                new Timer().schedule(m_timerTask, 5000, k_stepTime);
            }
        });

        //初始化消息处理例程。
        m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                ((TextView) findViewById(R.id.textView_engine)).setText((String) msg.obj);
            }
        };

        //初始化Server。
        //不能在主线程执行网络操作。需启动新线程异步连接。
        m_netThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(k_port);
                    //仅接受一个客户端连接。
                    m_socket = serverSocket.accept();
                    serverSocket.close();
                    m_inputStream = m_socket.getInputStream();
                    m_outputStream = m_socket.getOutputStream();
                    //提示。
                    Message msg = new Message();
                    msg.obj = "远程Monitor已连接。";
                    m_handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        m_netThread.start();

        //提示。
        //获取wifi服务.。
        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAdress = wifiInfo.getIpAddress();
        String ipString = (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
        ((TextView) findViewById(R.id.textView_engine)).setText("Monitor未连接。\n本机IP: " + ipString);

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
        //构造实时画面位图。
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
