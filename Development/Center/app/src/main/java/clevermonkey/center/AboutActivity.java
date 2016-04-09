package clevermonkey.center;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;

public class AboutActivity extends AppCompatActivity {

    UsbDeviceConnection connection;
    UsbEndpoint outEndpoint;

    byte[] data = new byte[1];

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    UsbManager usbManager;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            //call method to set up device communication
                            try {


                                UsbInterface usbInterface = usbDevice.getInterface(0);

                                UsbEndpoint usbEndpoint1 = usbInterface.getEndpoint(0);
                                UsbEndpoint usbEndpoint2 = usbInterface.getEndpoint(1);
                                UsbEndpoint usbEndpoint3 = usbInterface.getEndpoint(2);

                                outEndpoint = usbEndpoint2;

                                ((TextView) findViewById(R.id.textView)).setText(
                                        "EC:" + usbInterface.getEndpointCount() +
                                                "\nE1:" + usbEndpoint1.getType() +
                                                "\nE2:" + usbEndpoint2.getType() +
                                                "\nE3:" + usbEndpoint3.getType() +
                                                "\nE1:" + usbEndpoint1.getDirection() +
                                                "\nE2:" + usbEndpoint2.getDirection() +
                                                "\nE3:" + usbEndpoint3.getDirection()
                                );

                                connection = usbManager.openDevice(usbDevice);
                                connection.claimInterface(usbInterface, true);

                            } catch (Exception e) {
                                ((TextView) findViewById(R.id.textView)).setText("Error " + e.getMessage());
                            }
                        }
                    } else {
                        // Log.d("CMonkey", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        data[0]=(byte)8;

        ((TextView) findViewById(R.id.textView)).setText("UsbDevice List Start.");

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> hashMap = usbManager.getDeviceList();
        Iterator<UsbDevice> iterator = hashMap.values().iterator();

        if (iterator.hasNext()) {
            UsbDevice usbDevice = iterator.next();

            registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(usbDevice, mPermissionIntent);
        }

        ((Button) findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i=connection.bulkTransfer(outEndpoint, data, data.length, 0);
                if (i >= 0)
                    ((TextView) findViewById(R.id.textView)).setText("Set Ok. "+i);
                else
                    ((TextView) findViewById(R.id.textView)).setText("Set Fail. "); //do in another thread
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //
    }
}
