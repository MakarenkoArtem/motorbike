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
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
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
import androidx.lifecycle.lifecycleScope
import com.example.bi.ListDeviceDialog
import com.example.bike.R
import com.example.bike.model.CurrentColor
import com.example.bike.model.ScreenViewData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var screenData: StateFlow<ScreenViewData>
    private lateinit var curColor: CurrentColor
    lateinit var connectButton: Button
    lateinit var colorPicker: ImageView
    lateinit var brightness: SeekBar
    lateinit var bar: SeekBar

    private lateinit var types: List<RadioButton>
    private lateinit var modes: List<RadioButton>
    private lateinit var synchronously: RadioButton
    private var texts: List<String> =
        listOf("Движение", "1 цвет", "2 цвета", "3 цвета", "6 цвета", "Асинхронно")

    private lateinit var soundButton: ImageButton
    private lateinit var ignitionButton: ImageButton
    private lateinit var colorButtons: List<Button>

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


    private val connectListener = object : OnClickListener {
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

    private fun fixationColorChange(event: MotionEvent) {
        curColor.activButton?.backgroundTintList = ColorStateList.valueOf(curColor.color)
        if (event.action == MotionEvent.ACTION_UP) {
            lifecycleScope.launch {
                val resp =
                    viewModel.colorPickerSend(
                        curColor.color,
                        colorButtons.indexOf(curColor.activButton)
                    )
                if (resp.isFailure) {
                    viewModel.checkConnection()
                }
            }
        }
    }

    private val colorPickerTouch = OnTouchListener { view, event ->
        if (curColor.activButton != null) {
            val imageView = view as ImageView
            val bitmap = imageView.drawable.toBitmap()
            val verticalBufferZone =
                (imageView.height - min(imageView.width, imageView.height)) / 2
            val horizontalBufferZone =
                (imageView.width - min(imageView.width, imageView.height)) / 2
            val x = (event.x - horizontalBufferZone) / min(imageView.width, imageView.height)
            val y = (event.y - verticalBufferZone) / min(imageView.width, imageView.height)
            Log.d(
                "BikeBluetooth",
                "x:${x} event:${event.x} buffer:${horizontalBufferZone} width:${imageView.width}"
            )
            Log.d(
                "BikeBluetooth",
                "y:${y} event:${event.y} buffer:${verticalBufferZone} height:${imageView.height}"
            )
            runCatching {
                try {
                    val pix = bitmap.getPixel(
                        (x * bitmap.width).toInt(),
                        (y * bitmap.height).toInt()
                    )
                    if (pix.alpha != 0) {
                        curColor.color = pix
                    }
                } catch (e: IllegalArgumentException) {
                }
                curColor.updatePickers()
                fixationColorChange(event)
            }
        }
        true
    }

    private val brightSeekBarListener = object : OnSeekBarChangeListener {
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

    private val fastSeekBarListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            lifecycleScope.launch {
                viewModel.setFrequency(seekBar.progress)
            }
        }
    }

    private val ignitionListener = OnClickListener {
        lifecycleScope.launch {
            viewModel.setIgnition(!screenData.value.ignition).getOrElse {
                viewModel.disconnect()
                return@launch
            }
        }
    }

    private val soundListener = OnClickListener {
        lifecycleScope.launch {
            viewModel.setSound(!screenData.value.sound).getOrElse {
                viewModel.disconnect()
                return@launch
            }
        }
    }

    private val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
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

    private fun changeActiveStatus(butt: Button, status: Boolean) {
        if (status) {
            butt.isEnabled = true
            butt.alpha = 1f

        } else {
            butt.isEnabled = false
            butt.alpha = 0.5f
        }
    }

    private fun changeActiveStatus(butts: List<Button>, status: Boolean) {
        butts.forEach { it -> changeActiveStatus(it, status) }
    }

    private val changeNumPickerVal = OnTouchListener { view, event ->
        curColor.updateByPickers()
        fixationColorChange(event)
        false//обязательно false, иначе не будет дальнейшей обратоки события и не будет изменения значений
    }

    private val changeMode = OnClickListener { view ->
        val radioButton = view as RadioButton
        val num = modes.indexOf(radioButton)
        if (num == -1) {
            return@OnClickListener
        }
        changeActiveStatus(colorButtons, true)
        curColor.activButton = colorButtons[0]
        when (texts[num]) {
            getString(R.string.oneColor) -> {
                changeActiveStatus(colorButtons.drop(1), false)
            }

            getString(R.string.twoColors) -> {
                changeActiveStatus(colorButtons.slice(1..4), false)
            }

            getString(R.string.threeColors) -> {
                changeActiveStatus(colorButtons[1], false)
                changeActiveStatus(colorButtons.slice(3..4), false)
            }
        }
        viewModel.setModeColors(num + 1)
    }

    private val changeType = OnClickListener { view ->
        val radioButton = view as RadioButton
        val listOfTexts = listOf(
            listOf(
                "Движение",
                getString(R.string.oneColor),
                getString(R.string.twoColors),
                getString(R.string.threeColors),
                getString(R.string.sixColors),
                "Асинхронно"
            ),
            listOf(
                "Движение",
                getString(R.string.threeColors),
                "",
                "",
                "",
                "Без градиента"
            ),
            listOf(
                getString(R.string.oneColor),
                getString(R.string.twoColors),
                getString(R.string.threeColors),
                getString(R.string.sixColors),
                "",
                "Без градиента"
            ),
            listOf("HSV", "Градиент", "", "", "", "Асинхронно")
        )
        val num = types.indexOf(radioButton)
        if (num == -1) {
            return@OnClickListener
        }
        texts = listOfTexts[num]
        viewModel.setTypeColors(num + 1)
    }


    private fun initColorButs() = listOf(
        R.id.buttonColor0,
        R.id.buttonColor50,
        R.id.buttonColor100,
        R.id.buttonColor150,
        R.id.buttonColor200,
        R.id.buttonColor250
    ).map {
        val but = findViewById<Button>(it)
        but.setOnClickListener { view ->
            curColor.activButton = view as Button
        }
        return@map but
    }

    private fun initTypes(changeType: OnClickListener) = arrayOf(
        R.id.radioButType1,
        R.id.radioButType2,
        R.id.radioButType3,
        R.id.radioButType4
    ).map {
        val but = findViewById<RadioButton>(it)
        but.setOnClickListener(changeType)
        return@map but
    }

    private fun initModes(changeMode: OnClickListener): List<RadioButton> {
        val modeList = listOf(
            R.id.radioButMode1,
            R.id.radioButMode2,
            R.id.radioButMode3,
            R.id.radioButMode4,
            R.id.radioButMode5,
            R.id.synchronously
        ).map {
            val but = findViewById<RadioButton>(it)
            but.setOnClickListener(changeMode)
            but
        }
        modeList[5].setOnClickListener({
            viewModel.setSynchron(!screenData.value.synchrony)
        })
        return modeList
    }

    private fun checkScreenData() = lifecycleScope.launch {
        viewModel.screenDataState.collect { data -> // Обновление в зависимости от значения screenData
            connectButton.text = data.device?.name ?: "Connect"
            synchronously.isChecked = screenData.value.synchrony
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
            for (i in 0..<colorButtons.size) {
                colorButtons[i].backgroundTintList =
                    ColorStateList.valueOf(data.colors[i])
            }
            for (i in 0..5) {
                modes[i].isClickable = texts[i] != ""
                modes[i].visibility = if (texts[i] != "") View.VISIBLE else View.INVISIBLE
                modes[i].text = texts[i]
            }
        }
    }

    private val stepEvent = OnValueChangeListener { picker, oldVal, newVal ->
        var value = oldVal
        if (newVal > picker.maxValue - curColor.stepPicker && oldVal < picker.minValue + curColor.stepPicker) {
            value = picker.maxValue
        } else if (oldVal > picker.maxValue - curColor.stepPicker && newVal < picker.minValue + curColor.stepPicker) {
            value = picker.minValue
        } else {
            if (newVal > oldVal) {
                value += curColor.stepPicker
            } else {
                value -= curColor.stepPicker
            }
        }
        picker.value = value/curColor.stepPicker*curColor.stepPicker
        false
    }

    private fun initNumPickers() {
        curColor.setPickers(
            listOf(
                findViewById<NumberPicker>(R.id.numPickerRed),
                findViewById<NumberPicker>(R.id.numPickerGreen),
                findViewById<NumberPicker>(R.id.numPickerBlue)
            ), changeNumPickerVal, stepEvent
        )
        val r = findViewById<NumberPicker>(R.id.numPickerRed)
        r.setOnValueChangedListener(stepEvent)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)//MODE_NIGHT_FOLLOW_SYSTEM
        setContentView(R.layout.activity_main)
        val factory =
            MainActivityViewModelFactory(
                context = applicationContext,
                activity = this
            )
        viewModel = factory.create(MainActivityViewModel::class.java)
        screenData = viewModel.screenDataState
        curColor=screenData.value.curColor

        connectButton = findViewById(R.id.Connect)
        connectButton.setOnClickListener(connectListener)

        colorPicker = findViewById(R.id.colorPicker)
        colorPicker.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        colorPicker.setOnTouchListener(colorPickerTouch)

        initNumPickers()

        brightness = findViewById(R.id.brightness)
        brightness.setOnSeekBarChangeListener(brightSeekBarListener)

        bar = findViewById(R.id.bar)
        bar.setOnSeekBarChangeListener(fastSeekBarListener)

        ignitionButton = findViewById(R.id.bike_off_button)
        ignitionButton.setOnClickListener(ignitionListener)

        soundButton = findViewById(R.id.sound_button)
        soundButton.setOnClickListener(soundListener)

        colorButtons = initColorButs()

        types = initTypes(changeType)

        modes = initModes(changeMode)
        synchronously = modes[5]

        checkScreenData()
    }
}