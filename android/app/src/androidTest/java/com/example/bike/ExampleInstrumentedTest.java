/*package com.example.bike;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
/*@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.bike", appContext.getPackageName());
    }
}*/
package com.example.bike;
import android.annotation.SuppressLint;
import android.bluetooth.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.MotionEvent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    InputStream inputStream;
    OutputStream outputStream;
    BluetoothAdapter BTAdapter;
    private BluetoothDevice BTdevice;
    private BluetoothSocket bTSocket;
    private UUID myUUID;
    public Button activButton = null;
    public Button colorButton0;
    public Button colorButton50;
    public Button colorButton100;
    public Button colorButton150;
    public Button colorButton200;
    public Button colorButton250;
    public Button connectButton;
    public ImageView colorPicker;
    Bitmap bitmap;
    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectButton = findViewById(R.id.Connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                BTAdapter = BluetoothAdapter.getDefaultAdapter();
                if (BTAdapter == null) {
                    connectButton.setText("Not support BT");
                    return;
                }
                //Intent enableBT = null;
                if (!BTAdapter.isEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, 11);
                    BTAdapter.enable();
                }
                BluetoothDevice device = BTAdapter.getRemoteDevice("98:DA:60:06:6B:BA");
                Method m = null;
                try {
                    m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                try {
                    bTSocket = (BluetoothSocket) m.invoke(device, 1);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                try {
                    bTSocket.connect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();

                try {
                    outputStream = bTSocket.getOutputStream();
                    outputStream.write("Connecting\n".getBytes());
                    inputStream = bTSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    inputStream.read(buffer);
                    String s = new String(buffer, StandardCharsets.UTF_8);
                    Log.d("myLogs", s);
                    connectButton.setText("BIKE(HC-06)");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                /*
                if (checkCoarseLocationPermission()) {
                    Toast.makeText(view.getContext(), "-Bluetooth", Toast.LENGTH_SHORT).show();
                    BTAdapter.startDiscovery();
                    Log.d("myLogs", "-3");
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }/*
                if (BTAdapter.isDiscovering()){
                    Log.d("myLogs", "Discovering...");
                }*/
                /*Toast.makeText(view.getContext(), "Discovering", Toast.LENGTH_SHORT).show();

                //BTAdapter.startDiscovery();
                Toast.makeText(view.getContext(), "Bluetooth", Toast.LENGTH_SHORT).show();
                Log.d("myLogs", "-2");
                Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
                Log.d("myLogs", "-1");
                boolean t = true;
                for (BluetoothDevice device : pairedDevices) {
                    Log.d("myLogs", device.getName());
                    if (device.getName() == "BIKE(HC-06)") {
                        if (BluetoothDevice.ACTION_FOUND.equals(device)) {
                            try {
                                connect(device);
                            } catch (IOException e) {
                                return;
                            }
                            t = false;
                            break;
                        }
                    }
                }
                if (t) {
                    return;
                }


                //enableBT.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                //DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), “false”);
                // Add it to our adapter
                //mAdapter.add(newDevice);
                //BTAdapter.startDiscovery();
                Log.d("myLogs", "0");
                try {
                    bTSocket.connect();
                } catch (IOException e) {
                    Log.d("CONNECTTHREAD", "Could not connect: " + e.toString());
                    try {
                        bTSocket.close();
                    } catch (IOException close) {
                        Log.d("CONNECTTHREAD", "Could not close connection:" + e.toString());
                        return;
                    }
                }
                try {
                    outputStream = bTSocket.getOutputStream();
                    outputStream.write("Connecting".getBytes());
                } catch (IOException e) {
                    try {
                        bTSocket.close();
                    } catch (IOException ex) {
                        return;
                    }
                    return;
                }
                try {
                    inputStream = bTSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    inputStream.read(buffer);
                    Log.d("myLogs", buffer.toString());
                } catch (IOException e) {
                    try {
                        bTSocket.close();
                    } catch (IOException ex) {
                        return;
                    }
                    return;
                }
                connectButton.setText(BTdevice.getName());
            }*/
            }
        });

        colorButton0 = findViewById(R.id.buttonColor0);
        colorButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });

        colorButton50 = findViewById(R.id.buttonColor50);
        colorButton50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });

        colorButton100 = findViewById(R.id.buttonColor100);
        colorButton100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });

        colorButton150 = findViewById(R.id.buttonColor150);
        colorButton150.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });

        colorButton200 = findViewById(R.id.buttonColor200);
        colorButton200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });

        colorButton250 = findViewById(R.id.buttonColor250);
        colorButton250.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });

        colorPicker = findViewById(R.id.colorPicker);
        colorPicker.setDrawingCacheEnabled(true);
        colorPicker.buildDrawingCache(true);
        colorPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if ((activButton != null) && (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN)) {
                    bitmap = colorPicker.getDrawingCache();
                    try {
                        int pixels = bitmap.getPixel((int) event.getX(), (int) event.getY());
                        if (pixels == 0) {
                            return true;
                        }
                        int a = Color.alpha(pixels);
                        if (a < 255) {
                            return true;
                        }
                        int r = Color.red(pixels);
                        int g = Color.green(pixels);
                        int b = Color.blue(pixels);
                        int colorInt = getResources().getColor(R.color.colorButton0);
                        ColorStateList c = ColorStateList.valueOf(colorInt);
                        Log.d("myLogs", "!" + Integer.toHexString(pixels) + "!");
                        activButton.setBackgroundTintList(ColorStateList.valueOf(pixels));
                        outputStream.write("!{}".getBytes());
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
                    /*
                    if (colorButton0.equals(activButton)) {
                        color = colorButton0;
                    } else if (colorButton50.equals(activButton)) {
                        color = R.color.colorButton50;
                    } else if (colorButton100.equals(activButton)) {
                        color = R.color.colorButton100;
                    } else if (colorButton150.equals(activButton)) {
                        color = R.color.colorButton150;
                    } else if (colorButton200.equals(activButton)) {
                        color = R.color.colorButton200;
                    } else if (colorButton250.equals(activButton)) {
                        color = R.color.colorButton250;
                    }*/
                }
                return true;
            }
        });
        //bTSocket.getConnectionType();
    }

    private boolean checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return false;
        } else {
            return true;
        }
    }


    @SuppressLint("MissingPermission")
    protected void connect(BluetoothDevice device) throws IOException {
        try {
            //Create a Socket connection: need the server's UUID number of registered

            bTSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));


            bTSocket.connect();
            Log.d("EF-BTBee", ">>Client connectted");

            InputStream inputStream = bTSocket.getInputStream();
            OutputStream outputStream = bTSocket.getOutputStream();
            outputStream.write("Connect".getBytes());

            byte[] buffer = new byte[1024];
            inputStream.read(buffer);
            Log.d("myLogs", buffer.toString());


            new Thread() {
                public void run() {
                    while (true) {
                        try {
                            Log.d("EF-BTBee", ">>Send data thread!");
                            OutputStream outputStream = bTSocket.getOutputStream();
                            outputStream.write(new byte[]{(byte) 0xa2, 0, 7, 16, 0, 4, 0});
                        } catch (IOException e) {
                            Log.e("EF-BTBee", "", e);
                        }
                    }
                }
            }.start();

        } catch (IOException e) {
            Log.e("EF-BTBee", "", e);
        } finally {
            if (bTSocket != null) {
                try {
                    Log.d("EF-BTBee", ">>Client Close");
                    bTSocket.close();
                    finish();
                    return;
                } catch (IOException e) {
                    Log.e("EF-BTBee", "", e);
                }
            }
        }
    }
}
