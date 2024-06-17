package com.example.bike

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.util.UUID
import kotlin.collections.mutableListOf

class MainActivity : AppCompatActivity() {
    var timerBT: Long = 0
    lateinit var type1: RadioButton
    lateinit var type2: RadioButton
    lateinit var type3: RadioButton
    lateinit var type4: RadioButton
    lateinit var type_1: RadioButton
    lateinit var type_2: RadioButton
    lateinit var type_3: RadioButton
    lateinit var type_4: RadioButton
    lateinit var type_5: RadioButton
    lateinit var synchronously: RadioButton
    lateinit var types_: Array<RadioButton>
    lateinit var sound_button: ImageButton
    lateinit var bike_off_button: ImageButton
    var activButton: Button? = null
    var colorButton0: Button? = null
    var colorButton50: Button? = null
    var colorButton100: Button? = null
    var colorButton150: Button? = null
    var colorButton200: Button? = null
    var colorButton250: Button? = null
    lateinit var colorButtons: Array<Button?>
    lateinit var connectButton: Button
    lateinit var colorPicker: ImageView
    lateinit var bitmap: Bitmap
    lateinit var brightness: SeekBar
    lateinit var bar: SeekBar

    //ConnectedThread BT;
    var BTAdapter: BluetoothAdapter? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    private val BTdevice: BluetoothDevice? = null
    lateinit var bTSocket: BluetoothSocket
    private val myUUID: UUID? = null
    var colors: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
    var message: String? = null
    lateinit var buffer: ByteArray
    var answer: ArrayList<*>? = null
    var type: String = "1"
    var type_: String = "1"
    var synch: String = "0"
    lateinit var texts: Array<String>
    var bike_start: String = "ON\n"
    var sound_val: String = "LOW\n"

    fun init(): Int {
        //String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;10001000
        //startActivityForResult(new Intent(enableBT), 0);
        BTAdapter = BluetoothAdapter.getDefaultAdapter()
        try {
            //Устройство с данным адресом - наш Bluetooth Bee
            //Адрес опредеяется следующим образом: установите соединение
            //между ПК и модулем (пин: 2005), а затем посмотрите в настройках
            //соединения адрес модуля. Скорее всего он будет аналогичным.
            var device = BTAdapter?.getRemoteDevice("98:DA:60:06:6B:BA")

            //Инициируем соединение с устройством
            val m = device?.javaClass?.getMethod(
                "createRfcommSocket", *arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType
                )
            )

            bTSocket = m?.invoke(device, 1) as BluetoothSocket
            bTSocket.connect()
            //В случае появления любых ошибок, выводим в лог сообщение
        } catch (e: IOException) {
            Log.d("myLogs", "1")
            Log.d("BLUETOOTH", e.message!!)
            return 1
        } catch (e: SecurityException) {
            Log.d("myLogs", "2")
            Log.d("BLUETOOTH", e.message!!)
            return 2
        } catch (e: NoSuchMethodException) {
            Log.d("myLogs", "3")
            Log.d("BLUETOOTH", e.message!!)
            return 3
        } catch (e: IllegalArgumentException) {
            Log.d("myLogs", "4")
            Log.d("BLUETOOTH", e.message!!)
            return 4
        } catch (e: IllegalAccessException) {
            Log.d("myLogs", "5")
            Log.d("BLUETOOTH", e.message!!)
            return 5
        } catch (e: InvocationTargetException) {
            Log.d("myLogs", "6")
            //Log.d("BLUETOOTH", e.getMessage());
            return 6
        }

        try {
            inputStream = bTSocket.inputStream
            outputStream = bTSocket.outputStream
        } catch (e: IOException) {
        }
        val answer = BTSend("Con\n", 6, 300, true)
        //ArrayList answer = BTSend("P\n", true);
        //Log.d("myLogs", String.valueOf(answer.get(0)));
        if (!(answer[0] as Boolean)) {
            return 1
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
        val data = answer[1].toString().split(", ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (i in 0..5) {
            val r = data[i * 4 + 1].toInt()
            val g = data[i * 4 + 2].toInt()
            val b = data[i * 4 + 3].toInt()
            val pixels = Color.argb(255, r, g, b)
            colors[i] = pixels
            colorButtons[i]!!.backgroundTintList = ColorStateList.valueOf(pixels)
        }
        Log.d("myLogs", message!!)
        Toast.makeText(applicationContext, "CONNECTED", Toast.LENGTH_SHORT).show()
        connectButton!!.text = "BIKE(HC-06)"
        /*break;
            } catch (Exception e) {
                Log.d("myLogs", e.toString());
                e.printStackTrace();
                break;
            }
        }*/
        bike_off_button!!.isClickable = true
        sound_button!!.isClickable = true
        return 0
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)

        colorPicker = findViewById(R.id.colorPicker)
        //colorPicker.setImageDrawable()
        //colorPicker.setDrawingCacheEnabled(true)
        //colorPicker.buildDrawingCache(true)
        colorPicker.setOnTouchListener(colorPickerTouch)

        brightness = findViewById(R.id.brightness)
        brightness.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                BTSend("Br:" + seekBar.progress.toString() + "\n", 10, 100, true)
            }
        })

        bar = findViewById(R.id.bar)
        bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                BTSend("CF:" + seekBar.progress.toString() + "\n", 10, 100, true)
            }
        })
        resize()


        connectButton = findViewById(R.id.Connect)
        connectButton.setOnClickListener(View.OnClickListener {
            if (init() != 0) {
                disconnect()
            }
        })


        colorButton0 = findViewById(R.id.buttonColor0)
        /*colorButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activButton = (Button) view;
            }
        });*/
        colorButton50 = findViewById(R.id.buttonColor50)
        colorButton100 = findViewById(R.id.buttonColor100)
        colorButton150 = findViewById(R.id.buttonColor150)
        colorButton200 = findViewById(R.id.buttonColor200)
        colorButton250 = findViewById(R.id.buttonColor250)
        colorButtons = arrayOf<Button?>(
            colorButton0,
            colorButton50,
            colorButton100,
            colorButton150,
            colorButton200,
            colorButton250
        )
        for (i in 0..5) {
            colorButtons[i]!!.setOnClickListener { view ->
                activButton = view as Button
            }
        }


        bike_off_button = findViewById(R.id.bike_off_button)
        bike_off_button.setOnClickListener(View.OnClickListener {
            if (bike_start === "ON\n") {
                bike_start = "OFF\n"
                bike_off_button.setImageResource(R.drawable.start)
            } else {
                bike_start = "ON\n"
                bike_off_button.setImageResource(R.drawable.stop)
            }
            val answer = BTSend(bike_start, 5, 200, true)
            if (answer[0] as Boolean) {
                return@OnClickListener
            }
            if (bike_start === "ON\n") {
                bike_start = "OFF\n"
                bike_off_button.setImageResource(R.drawable.start)
            } else {
                bike_start = "ON\n"
                bike_off_button.setImageResource(R.drawable.stop)
            }
        })


        sound_button = findViewById(R.id.sound_button)
        sound_button.setOnClickListener(View.OnClickListener {
            if (sound_val === "HIGH\n") {
                sound_val = "LOW\n"
                sound_button.setImageResource(R.drawable.sound)
            } else {
                sound_val = "HIGH\n"
                sound_button.setImageResource(R.drawable.mute)
            }
            val answer = BTSend(sound_val, 5, 200, true)
            if (answer[0] as Boolean) {
                return@OnClickListener
            }
            if (sound_val === "HIGH\n") {
                sound_val = "LOW\n"
                sound_button.setImageResource(R.drawable.sound)
            } else {
                sound_val = "HIGH\n"
                bike_off_button.setImageResource(R.drawable.mute)
            }
        })


        type1 = findViewById(R.id.radioButton1)
        type1.setOnClickListener(changeType)
        type2 = findViewById(R.id.radioButton2)
        type2.setOnClickListener(changeType)
        type3 = findViewById(R.id.radioButton3)
        type3.setOnClickListener(changeType)
        type4 = findViewById(R.id.radioButton4)
        type4.setOnClickListener(changeType)


        type_1 = findViewById(R.id.radioButton_1)
        type_2 = findViewById(R.id.radioButton_2)
        type_3 = findViewById(R.id.radioButton_3)
        type_4 = findViewById(R.id.radioButton_4)
        type_5 = findViewById(R.id.radioButton_5)
        synchronously = findViewById(R.id.synchronously)
        types_ = arrayOf(type_1, type_2, type_3, type_4, type_5, synchronously)
        for (i in 0..4) {
            types_[i]!!.setOnClickListener(changeType_)
        }
        synchronously.setOnClickListener(View.OnClickListener {
            Log.d("myLogs", synch)
            if (synch === "0") {
                synch = "1"
            } else {
                synch = "0"
                synchronously.setChecked(false)
            }
            //if (((RadioButton) view).isChecked()) {synch = "1";}
        })
    }

    fun resize() {
        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        val screenWidth = displaymetrics.widthPixels
        val screenHeight = displaymetrics.heightPixels
        var params = colorPicker!!.layoutParams as ConstraintLayout.LayoutParams
        val height = (params.matchConstraintPercentHeight * screenHeight).toInt()
        params = brightness!!.layoutParams as ConstraintLayout.LayoutParams
        params.width = (height * 0.95).toInt()
        params.marginStart = (-params.width * 0.4).toInt()
        brightness!!.layoutParams = params
        params = bar!!.layoutParams as ConstraintLayout.LayoutParams
        params.width = (height * 0.95).toInt()
        params.marginEnd = (-params.width * 0.4).toInt()
        bar!!.layoutParams = params
    }

    var changeType_: View.OnClickListener = View.OnClickListener { v ->
        val rb = v as RadioButton
        message = "Ty:"
        if (type_1 === rb) {
            type_ = "1"
        } else if (type_2 === rb) {
            type_ = "2"
        } else if (type_3 === rb) {
            type_ = "3"
        } else if (type_4 === rb) {
            type_ = "4"
        } else if (type_5 === rb) {
            type_ = "5"
        }
        message += synch + type + type_ + "\n"
        BTSend(message, 6, 100, true)
    }
    var changeType: View.OnClickListener = View.OnClickListener { v ->
        val rb = v as RadioButton
        message = "Type:"
        if (type1 === rb) {
            type = "1"
            texts = arrayOf("Движение", "1 цвет", "2 цвета", "3 цвета", "6 цвета", "Асинхронно")
        } else if (type2 === rb) {
            type = "2"
            texts = arrayOf("1 цвет", "2 цвета", "3 цвета", "6 цвета", "", "Без градиента")
        } else if (type3 === rb) {
            type = "3"
            texts = arrayOf("1 цвет", "2 цвета", "3 цвета", "6 цвета", "", "Без градиента")
        } else if (type4 === rb) {
            type = "4"
            texts = arrayOf("Тип 1", "Тип 2", "Тип 3", "Тип 4", "", "Асинхронно")
        }
        for (i in 0..5) {
            types_[i].isClickable = texts[i] != ""
            types_[i].visibility = if (texts[i] != "") View.VISIBLE else View.INVISIBLE
            /*if texts[i].equals(""){
                    types_[i].setClickable(false);
                }else{types_[i].setClickable(true);}*/
            types_[i].text = texts[i]
        }
        type_1.toggle()
        message = "Ty:"
        type_ = "1"
        synch = "0"
        synchronously.isChecked = false
        message += synch + type + type_ + "\n"
        BTSend(message, 6, 100, true)
    }
    var colorPickerTouch: OnTouchListener = OnTouchListener { view, event ->
        if ((activButton != null) && (event.action == MotionEvent.ACTION_UP)) {
            colorPickerSend(event)
            BTSend(message, 3, 100, false)
            BTSend("END\n", 1, 0, false)
        }
        true
    }

    fun colorPickerSend(event: MotionEvent) {
        bitmap = colorPicker!!.drawingCache
        try {
            val pixels = bitmap.getPixel(event.x.toInt(), event.y.toInt())
            if (pixels == 0) {
                return
            }
            val a = Color.alpha(pixels)
            if (a < 255) {
                return
            }
            val r = Color.red(pixels)
            val g = Color.green(pixels)
            val b = Color.blue(pixels)
            //int colorInt = getResources().getColor(R.color.colorButton0);
            //ColorStateList c = ColorStateList.valueOf(colorInt);
            //Log.d("myLogs", "!" + Integer.toString(pixels) + "!");
            //Log.d("myLogs", "!" + Integer.toHexString(pixels) + "!");
            activButton!!.backgroundTintList = ColorStateList.valueOf(pixels)
            message = "Co:"
            //message = "C";
            for (i in 0..5) {
                if (activButton === colorButtons[i]) {
                    colors[i] = pixels
                }
                val r_ = String.format("%03d", Color.red(colors[i]))
                val g_ = String.format("%03d", Color.green(colors[i]))
                val b_ = String.format("%03d", Color.blue(colors[i]))
                message += String.format(
                    "%03d",
                    (51 * i).toString() + "," + r_ + "," + g_ + "," + b_ + ","
                )
            }
            message += "\n"
            Log.d("myLogs", message!!)
            /*timerBT = System.currentTimeMillis();
                BTSend(message, 1, 0,false);*/
            //outputStream.write(message.getBytes());
            //SystemClock.sleep(1000);
        } catch (e: Exception) {
            println("Error " + e.message)
        }
    }


    fun BTSend(mes: String?, count: Int, time: Int, wait_answer: Boolean): MutableList<Any> {
        var mes = mes
        var answer = MutableList<Any>(2,{true; ""})
        if (inputStream == null) {
            answer.set(0, false)
            return answer
        }
        try {
            outputStream?.write("READY?\n".toByteArray())
            if (inputStream?.available() != 0) {
                buffer = ByteArray(1024) // буферный массив
                val size = inputStream?.read(buffer) ?:0
                message = String(buffer, 0, size)
            }
        } catch (e: IOException) {
        }
        for (i in 0 until count) {
            try {
                buffer = ByteArray(1024) // буферный массив
                outputStream?.write(mes!!.toByteArray())
                Log.d("myLogs", mes!!)
                SystemClock.sleep(time.toLong())
                if (!wait_answer) {
                    continue
                }
                SystemClock.sleep(150)
                if (inputStream?.available() == 0) {
                    continue
                }
                val size = inputStream?.read(buffer)?:0
                message = String(buffer, 0, size)
                Log.d("myLogs", message!!)
                if (message?.substring(0, 2) == "OK") {
                    try {
                        answer.set(1, message!!.substring(3))
                        return answer
                    } catch (e: StringIndexOutOfBoundsException) {
                    }
                    return answer
                }
            } catch (e: Exception) {
                Log.d("myLogs", e.message!!)
            }
        }
        if (!wait_answer) {
            return answer
        }
        mes = "Con\n"
        for (i in 0..2) {
            try {
                buffer = ByteArray(1024) // буферный массив
                outputStream!!.write(mes.toByteArray())
                SystemClock.sleep(500)
                if (inputStream!!.available() == 0) {
                    continue
                }
                val size = inputStream?.read(buffer)?:0
                message = String(buffer, 0, size)
                if (message?.substring(0, 2) == "OK") {
                    answer.set(1, message!!.substring(2))
                    return answer
                }
            } catch (e: Exception) {
                Log.d("myLogs", e.message!!)
            }
        }
        answer.set(0, false)
        disconnect()
        return answer
    }

    fun disconnect() {
        Log.d("myLogs", "11")
        try {
            bTSocket.close()
            inputStream = null
            outputStream = null
        } catch (e: Exception) {
        }
        Toast.makeText(applicationContext, "DISCONNECTED", Toast.LENGTH_SHORT).show()
        connectButton.text = "Connect"
        sound_button.isClickable = false
        bike_off_button.isClickable = false
    }
}