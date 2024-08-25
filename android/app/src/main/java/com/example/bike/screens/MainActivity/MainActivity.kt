package com.example.bike.screens.MainActivity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bi.ListDeviceDialog
import com.example.bike.R
import com.example.bike.model.ScreenViewData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var screenData: StateFlow<ScreenViewData>
    lateinit var connectButton: Button
    lateinit var colorPicker: ImageView
    lateinit var brightness: SeekBar
    lateinit var bar: SeekBar


    lateinit var type1: RadioButton
    lateinit var type2: RadioButton
    lateinit var type3: RadioButton
    lateinit var type4: RadioButton
    lateinit var mode1: RadioButton
    lateinit var mode2: RadioButton
    lateinit var mode3: RadioButton
    lateinit var mode4: RadioButton
    lateinit var mode5: RadioButton
    lateinit var synchronously: RadioButton
    lateinit var modes: Array<RadioButton>
    var texts: Array<String> =
        arrayOf("Движение", "1 цвет", "2 цвета", "3 цвета", "6 цвета", "Асинхронно")

    lateinit var soundButton: ImageButton
    lateinit var ignitionButton: ImageButton


    var activButton: Button? = null
    lateinit var colorButton0: Button
    lateinit var colorButton50: Button
    lateinit var colorButton100: Button
    lateinit var colorButton150: Button
    lateinit var colorButton200: Button
    lateinit var colorButton250: Button
    lateinit var colorButtons: Array<Button>

    val getResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val res = result.data?.getParcelableExtra<BluetoothDevice>("SELECTED_DEVICE")
                    ?: return@registerForActivityResult
                viewModel.connect(device = res)
                connectButton.text = viewModel.screenDataState.value.device?.name ?: ""
                Toast.makeText(
                    applicationContext,
                    getString(R.string.connectionEstablished), Toast.LENGTH_SHORT
                )
                    .show()
            }
        }


    val connectListener = object : OnClickListener {
        override fun onClick(p0: View?) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                ActivityCompat.requestPermissions(this@MainActivity, permissions, 0)
                Log.d("BikeBluetooth", "permissions")
            }
            if (screenData.value.device != null) {
                connectButton.text = getString(R.string.connect)
                viewModel.disconnect()
                return
            }
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
                ActivityCompat.requestPermissions(this@MainActivity, permissions, 0)
            }
            //lifecycleScope.launch {
            val pairedDevices = viewModel.getPairedDevices()
            val intent = Intent(applicationContext, ListDeviceDialog::class.java)
            intent.putParcelableArrayListExtra("DEVICE_NAMES", ArrayList(pairedDevices))
            getResult.launch(intent)
        }
    }

    val colorPickerTouch: OnTouchListener = OnTouchListener { view, event ->
        if (activButton != null) {
            val imageView = view as ImageView
            val bitmap = imageView.drawable.toBitmap()
            val verticalBufferZone =
                (imageView.height - min(imageView.width, imageView.height)) / 2
            val horizontalBufferZone =
                (imageView.width - min(imageView.width, imageView.height)) / 2
            var x = (event.x - horizontalBufferZone) / min(imageView.width, imageView.height)
            var y = (event.y - verticalBufferZone) / min(imageView.width, imageView.height)
            /*Log.d(
                "BikeBluetooth",
                "x:${x} event:${event.x} buffer:${horizontalBufferZone} width:${imageView.width}"
            )
            Log.d(
                "BikeBluetooth",
                "y:${y} event:${event.y} buffer:${verticalBufferZone} height:${imageView.height}"
            )*/
            runCatching {
                val pixel = bitmap.getPixel(
                    (x * bitmap.width).toInt(),
                    (y * bitmap.height).toInt()
                )
                if (pixel.alpha == 0) {
                    return@OnTouchListener true
                }
                activButton!!.backgroundTintList = ColorStateList.valueOf(pixel)
                if(event.action == MotionEvent.ACTION_UP) {
                    CoroutineScope(Dispatchers.Default).launch {
                        viewModel.colorPickerSend(pixel, colorButtons.indexOf(activButton))
                        //screenData.value.device?.sendMessage("END\n", 1, 0)
                    }
                }
            }
        }
        true
    }

    val brightSeekBarListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            lifecycleScope.launch {
                viewModel.setBrightness(seekBar.progress)
            }
        }
    }

    val fastSeekBarListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            lifecycleScope.launch {
                viewModel.setFrequency(seekBar.progress)
            }
        }
    }

    val ignitionListener = OnClickListener {
        lifecycleScope.launch {                    //CoroutineScope(Dispatchers.IO)
            viewModel.setIgnition(!screenData.value.ignition).getOrElse {
                viewModel.disconnect()
                return@launch
            }
        }
    }

    val soundListener = OnClickListener {
        lifecycleScope.launch {
            viewModel.setSound(!screenData.value.sound).getOrElse {
                viewModel.disconnect()
                return@launch
            }
        }
    }

    val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        //как только будут заданы все размеры вызовется эта функция
        override fun onGlobalLayout() {
            var layoutParams = brightness.layoutParams
            layoutParams.width = colorPicker.height  // Изменяем ширину
            brightness.layoutParams = layoutParams  // Применяем новые параметры
            layoutParams = bar.layoutParams
            layoutParams.width = colorPicker.height  // Изменяем ширину
            bar.layoutParams = layoutParams  // Применяем новые параметры

            // Удаляем слушателя после первого выполнения
            colorPicker.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }

    private val changeMode = OnClickListener { view ->
        val radioButton = view as RadioButton
        val num = modes.indexOf(radioButton)
        if (num == -1) {
            return@OnClickListener
        }
        viewModel.setModeColors(num + 1)
    }
    private val changeType = OnClickListener { view ->
        val radioButton = view as RadioButton
        val num = when (radioButton) {
            type1 -> {
                texts = arrayOf("Движение", "1 цвет", "2 цвета", "3 цвета", "6 цвета", "Асинхронно")
                0
            }

            type2 -> {
                texts = arrayOf("1 цвет", "2 цвета", "3 цвета", "6 цвета", "", "Без градиента")
                1
            }

            type3 -> {
                texts = arrayOf("1 цвет", "2 цвета", "3 цвета", "6 цвета", "", "Без градиента")
                2
            }

            type4 -> {
                texts = arrayOf("Тип 1", "Тип 2", "Тип 3", "Тип 4", "", "Асинхронно")
                3
            }
            else -> {
                return@OnClickListener
            }
        }
        viewModel.setTypeColors(num+1)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)//MODE_NIGHT_FOLLOW_SYSTEM
        setContentView(R.layout.activity_main)
        //viewModel = MainActivityViewModel(context = applicationContext, activity = this)
        val factory =
            MainActivityViewModelFactory(context = applicationContext, activity = this)
        viewModel = ViewModelProvider(this, factory).get(MainActivityViewModel::class.java)
        screenData = viewModel.screenDataState

        connectButton = findViewById(R.id.Connect)
        connectButton.setOnClickListener(connectListener)

        colorPicker = findViewById(R.id.colorPicker)
        colorPicker.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        //colorPicker.setImageDrawable()
        //colorPicker.setDrawingCacheEnabled(true)
        //colorPicker.buildDrawingCache(true)
        colorPicker.setOnTouchListener(colorPickerTouch)

        brightness = findViewById(R.id.brightness)
        brightness.setOnSeekBarChangeListener(brightSeekBarListener)

        bar = findViewById(R.id.bar)
        bar.setOnSeekBarChangeListener(fastSeekBarListener)

        ignitionButton = findViewById(R.id.bike_off_button)
        ignitionButton.setOnClickListener(ignitionListener)

        soundButton = findViewById(R.id.sound_button)
        soundButton.setOnClickListener(soundListener)

        colorButton0 = findViewById(R.id.buttonColor0)
        colorButton50 = findViewById(R.id.buttonColor50)
        colorButton100 = findViewById(R.id.buttonColor100)
        colorButton150 = findViewById(R.id.buttonColor150)
        colorButton200 = findViewById(R.id.buttonColor200)
        colorButton250 = findViewById(R.id.buttonColor250)
        colorButtons = arrayOf<Button>(
            colorButton0,
            colorButton50,
            colorButton100,
            colorButton150,
            colorButton200,
            colorButton250
        )
        for (but in colorButtons) {
            but.setOnClickListener { view ->
                activButton = view as Button
            }
        }

        type1 = findViewById(R.id.radioButType1)
        type1.setOnClickListener(changeType)
        type2 = findViewById(R.id.radioButType2)
        type2.setOnClickListener(changeType)
        type3 = findViewById(R.id.radioButType3)
        type3.setOnClickListener(changeType)
        type4 = findViewById(R.id.radioButType4)
        type4.setOnClickListener(changeType)


        mode1 = findViewById(R.id.radioButMode1)
        mode2 = findViewById(R.id.radioButMode2)
        mode3 = findViewById(R.id.radioButMode3)
        mode4 = findViewById(R.id.radioButMode4)
        mode5 = findViewById(R.id.radioButMode5)
        synchronously = findViewById(R.id.synchronously)
        modes = arrayOf(mode1, mode2, mode3, mode4, mode5, synchronously)
        for (i in 0..4) {
            modes[i].setOnClickListener(changeMode)
        }
        synchronously.setOnClickListener(View.OnClickListener {
            viewModel.setSynchron(!screenData.value.synchron)
        })


        lifecycleScope.launch {
            viewModel.screenDataState.collect { data -> // Обновление в зависимости от значения screenData
                connectButton.text = data.device?.name ?: "Connect"
                synchronously.setChecked(screenData.value.synchron)
                if (screenData.value.ignition) {
                    ignitionButton.setImageResource(R.drawable.start)
                } else {
                    ignitionButton.setImageResource(R.drawable.stop)
                }
                if (screenData.value.sound) {
                    soundButton.setImageResource(R.drawable.sound)
                } else {
                    soundButton.setImageResource(R.drawable.mute)
                }
                for (i in 0..colorButtons.size - 1) {
                    colorButtons[i].backgroundTintList =
                        ColorStateList.valueOf(data.colors[i])
                }
                for (i in 0..5) {
                    modes[i].isClickable = texts[i] != ""
                    modes[i].visibility = if (texts[i] != "") View.VISIBLE else View.INVISIBLE/*if texts[i].equals(""){
                types_[i].setClickable(false);
            }else{types_[i].setClickable(true);}*/
                    modes[i].text = texts[i]
                }
            }
        }
    }
}