package com.example.bike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.util.Base64DataException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public long timerBT = 0;
    public RadioButton type1;
    public RadioButton type2;
    public RadioButton type3;
    public RadioButton type4;
    public RadioButton type_1;
    public RadioButton type_2;
    public RadioButton type_3;
    public RadioButton type_4;
    public RadioButton type_5;
    public RadioButton synchronously;
    public RadioButton[] types_;
    public SwitchCompat bike_off_switch;
    public Button activButton = null;
    public Button colorButton0;
    public Button colorButton50;
    public Button colorButton100;
    public Button colorButton150;
    public Button colorButton200;
    public Button colorButton250;
    public Button[] colorButtons;
    public Button connectButton;
    public ImageView colorPicker;
    Bitmap bitmap;
    SeekBar brightness;
    SeekBar bar;
    //ConnectedThread BT;
    InputStream inputStream;
    OutputStream outputStream;
    BluetoothAdapter BTAdapter;
    private BluetoothDevice BTdevice;
    private BluetoothSocket bTSocket;
    private UUID myUUID;
    int[] colors = {0, 0, 0, 0, 0, 0};
    String message;
    byte[] buffer;
    ArrayList answer;
    String type="1";
    String type_="1";
    String synch = "0";
    String[] texts;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        colorPicker = findViewById(R.id.colorPicker);
        colorPicker.setDrawingCacheEnabled(true);
        colorPicker.buildDrawingCache(true);
        colorPicker.setOnTouchListener(colorPickerTouch);

        brightness = findViewById(R.id.brightness);
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                BTSend("Br:"+Integer.toString(seekBar.getProgress())+"\n", 10,100,true);
            }
        });

        bar = findViewById(R.id.bar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                BTSend("Ot:"+Integer.toString(seekBar.getProgress())+"\n", 10,100,true);
            }
        });
        resize();


        connectButton = findViewById(R.id.Connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (init()!=0){disconnect();}
            }
        });


        colorButton0 = findViewById(R.id.buttonColor0);
        /*colorButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });*/
        colorButton50 = findViewById(R.id.buttonColor50);
        colorButton100 = findViewById(R.id.buttonColor100);
        colorButton150 = findViewById(R.id.buttonColor150);
        colorButton200 = findViewById(R.id.buttonColor200);
        colorButton250 = findViewById(R.id.buttonColor250);
        colorButtons = new Button[]{colorButton0, colorButton50, colorButton100, colorButton150, colorButton200, colorButton250};
        for (int i = 0; i < 6; ++i) {
            colorButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activButton = (Button) view;
                }
            });
        }


        bike_off_switch = findViewById(R.id.bike_off_switch);
        bike_off_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton swi, boolean isChecked) {
                message = "OFF\n";
                if (isChecked){message = "ON\n";}
                ArrayList answer = BTSend(message, 5,200, true);
                if ((boolean) answer.get(0)) {
                    return;
                }
                swi.setChecked(!isChecked);
            }
        });


        type1 = findViewById(R.id.radioButton1);
        type1.setOnClickListener(changeType);
        type2 = findViewById(R.id.radioButton2);
        type2.setOnClickListener(changeType);
        type3 = findViewById(R.id.radioButton3);
        type3.setOnClickListener(changeType);
        type4 = findViewById(R.id.radioButton4);
        type4.setOnClickListener(changeType);


        type_1 = findViewById(R.id.radioButton_1);
        type_2 = findViewById(R.id.radioButton_2);
        type_3 = findViewById(R.id.radioButton_3);
        type_4 = findViewById(R.id.radioButton_4);
        type_5 = findViewById(R.id.radioButton_5);
        synchronously   = findViewById(R.id.synchronously);
        types_ = new RadioButton[]{type_1,type_2,type_3,type_4,type_5, synchronously};
        for (int i = 0; i < 5; ++i) {types_[i].setOnClickListener(changeType_);}
        synchronously.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("myLogs", synch);
                if (synch == "0"){
                    synch = "1";
                }else{synch = "0";
                    synchronously.setChecked(false);
                }
                //if (((RadioButton) view).isChecked()) {synch = "1";}
            }});
    };
    public void resize(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        int screenHeight = displaymetrics.heightPixels;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) colorPicker.getLayoutParams();
        int height = (int)(params.matchConstraintPercentHeight*screenHeight);
        params = (ConstraintLayout.LayoutParams) brightness.getLayoutParams();
        params.width = (int) (height*0.95);
        params.setMarginStart((int) (-params.width*0.4));
        brightness.setLayoutParams(params);
        params = (ConstraintLayout.LayoutParams) bar.getLayoutParams();
        params.width = (int) (height*0.95);
        params.setMarginEnd((int) (-params.width*0.4));
        bar.setLayoutParams(params);
    }
    View.OnClickListener changeType_ = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton rb = (RadioButton)v;
            message = "Ty:";
            if (type_1 == rb) {type_ = "1";
            } else if (type_2 == rb) {type_ = "2";
            } else if (type_3 == rb) {type_ = "3";
            } else if (type_4 == rb) {type_ = "4";
            } else if (type_5 == rb) {type_ = "5";}
            message += synch + type + type_ + "\n";
            BTSend(message,6,100,true);
        }
    };
    View.OnClickListener changeType = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton rb = (RadioButton)v;
            message = "Type:";
            if (type1 == rb) {type = "1";
                texts = new String[]{"Выкл", "1 цвет", "2 цвета", "3 цвета", "6 цвета", ""};
            } else if (type2 == rb) {type = "2";
                texts = new String[]{"1 цвет", "2 цвета", "3 цвета", "6 цвета", "", ""};
            } else if (type3 == rb) {type = "3";
                texts = new String[]{"1 цвет", "2 цвета", "3 цвета", "6 цвета", "", ""};
            } else if (type4 == rb) {type = "4";
                texts = new String[]{"Тип 1", "Тип 2", "Тип 3", "Тип 4", "","Синхронно"};}
            for (int i=0;i<6;i++){
                types_[i].setClickable(!texts[i].equals(""));
                types_[i].setVisibility(!texts[i].equals("") ? View.VISIBLE : View.INVISIBLE);
                /*if texts[i].equals(""){
                    types_[i].setClickable(false);
                }else{types_[i].setClickable(true);}*/
                types_[i].setText(texts[i]);
            }
            type_1.toggle();
            message = "Ty:";
            type_ = "1";
            message += synch + type + type_ + "\n";
            BTSend(message,6,100,true);
        }
    };
    View.OnTouchListener colorPickerTouch =new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if ((activButton != null) && (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP)) {
                colorPickerSend(event);
                if (event.getAction() == MotionEvent.ACTION_UP){
                    BTSend(message, 3, 100,false);
                    BTSend("END\n", 1, 0,false);
                }
            }
            return true;}
    };
    public void colorPickerSend(MotionEvent event){
        bitmap = colorPicker.getDrawingCache();
        try {
            int pixels = bitmap.getPixel((int) event.getX(), (int) event.getY());
            if (pixels == 0) {return;}
            int a = Color.alpha(pixels);
            if (a < 255) {return;}
            int r = Color.red(pixels);
            int g = Color.green(pixels);
            int b = Color.blue(pixels);
            //int colorInt = getResources().getColor(R.color.colorButton0);
            //ColorStateList c = ColorStateList.valueOf(colorInt);
            //Log.d("myLogs", "!" + Integer.toString(pixels) + "!");
            //Log.d("myLogs", "!" + Integer.toHexString(pixels) + "!");
            activButton.setBackgroundTintList(ColorStateList.valueOf(pixels));
            if (System.currentTimeMillis() - timerBT > 500){
                message = "Co:";
                //message = "C";
                for (int i = 0; i < 6; ++i) {
                    if (activButton == colorButtons[i]) {
                        colors[i] = pixels;
                    }
                    String r_ = String.format("%03d", Color.red(colors[i]));
                    String g_ = String.format("%03d", Color.green(colors[i]));
                    String b_ = String.format("%03d", Color.blue(colors[i]));
                    message += String.format("%03d", 51 * i) + "," + r_ + "," + g_ + "," + b_ + ",";
                }
                message += "\n";
                Log.d("myLogs", message);
                timerBT = System.currentTimeMillis();
                BTSend(message, 1, 0,false);
                //outputStream.write(message.getBytes());
                //SystemClock.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }
    public int init(){
        //String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;10001000
        //startActivityForResult(new Intent(enableBT), 0);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        inputStream=null;
        outputStream=null;
        try{
            //Устройство с данным адресом - наш Bluetooth Bee
            //Адрес опредеяется следующим образом: установите соединение
            //между ПК и модулем (пин: 2005), а затем посмотрите в настройках
            //соединения адрес модуля. Скорее всего он будет аналогичным.
            BluetoothDevice device = BTAdapter.getRemoteDevice("98:DA:60:06:6B:BA");

            //Инициируем соединение с устройством
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});

            bTSocket = (BluetoothSocket) m.invoke(device, 1);
            bTSocket.connect();
            //В случае появления любых ошибок, выводим в лог сообщение
        } catch (IOException e) {
            Log.d("myLogs", "1");
            Log.d("BLUETOOTH", e.getMessage());
            return 1;
        } catch (SecurityException e) {
            Log.d("myLogs", "2");
            Log.d("BLUETOOTH", e.getMessage());
            return 2;
        } catch (NoSuchMethodException e) {
            Log.d("myLogs", "3");
            Log.d("BLUETOOTH", e.getMessage());
            return 3;
        } catch (IllegalArgumentException e) {
            Log.d("myLogs", "4");
            Log.d("BLUETOOTH", e.getMessage());
            return 4;
        } catch (IllegalAccessException e) {
            Log.d("myLogs", "5");
            Log.d("BLUETOOTH", e.getMessage());
            return 5;
        } catch (InvocationTargetException e) {
            Log.d("myLogs", "6");
            //Log.d("BLUETOOTH", e.getMessage());
            return 6;
        }

        try{
            inputStream= bTSocket.getInputStream();
            outputStream= bTSocket.getOutputStream();
        } catch(IOException e){}
        ArrayList answer = BTSend("Con\n", 6, 300, true);
        //ArrayList answer = BTSend("P\n", true);
        //Log.d("myLogs", String.valueOf(answer.get(0)));
        if (!(boolean) answer.get(0)) {
            return 1;
        }
        /*buffer=new byte[1024];// буферный массив
        int bytes;// bytes returned from read()
        try{outputStream.write("Connecting\n".getBytes());
        } catch (IOException e) {
            connectButton.setText("Connect");
            return 1;
        }
        for(int j=0;j<10;++j){
            try {
                if( inputStream.available() == 0 ){
                    SystemClock.sleep(150);
                    continue;}
                int size=inputStream.read(buffer);
                message=new String(buffer, 0, size);*/
                String[] data = String.valueOf(answer.get(1)).split(", ");
                for (int i=0; i<6;++i) {
                    int r = Integer.parseInt(data[i * 4 + 1]);
                    int g = Integer.parseInt(data[i * 4 + 2]);
                    int b = Integer.parseInt(data[i * 4 + 3]);
                    int pixels = Color.argb(255, r, g, b);
                    colors[i]=pixels;
                    colorButtons[i].setBackgroundTintList(ColorStateList.valueOf(pixels));
                }
                Log.d("myLogs", message);
                Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_SHORT).show();
                connectButton.setText("BIKE(HC-06)");
                /*break;
            } catch (Exception e) {
                Log.d("myLogs", e.toString());
                e.printStackTrace();
                break;
            }
        }*/
        bike_off_switch.setClickable(true);
        return 0;
    }
    public ArrayList BTSend(String mes, int count, int time, boolean wait_answer){
        answer = new ArrayList<>(Arrays.asList(true, ""));
        if (inputStream == null){
            answer.set(0, false);
            return answer;
        }
        try {
            outputStream.write("READY?\n".getBytes());
            if (inputStream.available()!=0){
                buffer=new byte[1024];// буферный массив
                int size=inputStream.read(buffer);
                message=new String(buffer, 0, size);}
        } catch (IOException e) {}
        for (int i =0;i<count;i++){
            try {
                buffer=new byte[1024];// буферный массив
                outputStream.write(mes.getBytes());
                Log.d("myLogs", mes);
                SystemClock.sleep(time);
                if (!wait_answer){
                    continue;
                }
                SystemClock.sleep(150);
                if (inputStream.available()==0){continue;}
                int size=inputStream.read(buffer);
                message=new String(buffer, 0, size);
                Log.d("myLogs", message);
                if (Objects.equals(message.substring(0,2), "OK")){
                    try {
                        answer.set(1, message.substring(3));
                        return answer;
                    }catch (StringIndexOutOfBoundsException e){}
                    return answer;}
            } catch (Exception e) {
                Log.d("myLogs", e.getMessage());
            }
        }
        if (!wait_answer){
            return answer;
        }
        mes = "Con\n";
        for (int i =0;i<3;i++){
            try {
                buffer=new byte[1024];// буферный массив
                outputStream.write(mes.getBytes());
                SystemClock.sleep(500);
                if (inputStream.available()==0){continue;}
                int size=inputStream.read(buffer);
                message=new String(buffer, 0, size);
                if (Objects.equals(message.substring(0,2), "OK")){
                    answer.set(1, message.substring(2));
                    return answer;}
            } catch (Exception e) {
                Log.d("myLogs", e.getMessage());
            }
        }
        answer.set(0, false);
        disconnect();
        return answer;
    }
    public void disconnect(){
        Log.d("myLogs", "11");
        try {
            bTSocket.close();
            inputStream = null;
            outputStream = null;
        } catch (Exception e){}
        Toast.makeText(getApplicationContext(), "DISCONNECTED", Toast.LENGTH_SHORT).show();
        connectButton.setText("Connect");
        bike_off_switch.setClickable(false);
    }
}