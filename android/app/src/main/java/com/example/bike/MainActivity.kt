package com.example.bike.MainActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bike.ui.activity.ListDeviceDialog
import com.example.bike.R
import com.example.bike.model.CurrentColor
import com.example.bike.model.ScreenViewData
import com.example.bike.services.bluetooth.BluetoothClient
import com.example.bike.services.bluetooth.BluetoothService
import com.example.bike.services.bluetooth.BluetoothViewModel
import com.example.bike.ui.viewmodel.MainActivityViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

@Suppress("DEPRECATION")
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
    private lateinit var extraParams: List<RadioButton>
    private lateinit var hsv: RadioButton
    private lateinit var gradient: RadioButton
    private lateinit var movement: RadioButton
    private lateinit var synchronously: RadioButton
    private var texts: List<String> = mutableListOf("База", "Мерцание", "", "", "", "")

    private lateinit var amplifierButton: ImageButton
    private lateinit var ignitionButton: ImageButton
    private lateinit var audioBTButton: ImageButton
    private lateinit var colorButtons: List<Button>

    private var actuallyColorsButtons: MutableList<Int> =
        mutableListOf(Color.GRAY, Color.GRAY, Color.GRAY, Color.GRAY, Color.GRAY)


    private lateinit var bluetoothViewModel: BluetoothViewModel


    private val connectListener = object : OnClickListener {
        override fun onClick(p0: View?) {
            lifecycleScope.launch {
                bluetoothViewModel.waitForService()
                if (bluetoothViewModel.checkBluetoothPermission().isFailure) {
                    val permissions = bluetoothViewModel.getRequiredPermissions()
                    Log.d("BikeBluetooth", permissions.toString())
                    ActivityCompat.requestPermissions(this@MainActivity, permissions, 0)
                }
                val intent = Intent(applicationContext, ListDeviceDialog::class.java)
                getResult.launch(intent)
            }
        }
    }

    val getResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val res = result.data?.getParcelableExtra<BluetoothClient>("SELECTED_DEVICE")
                    ?: return@registerForActivityResult
                viewModel.connect(res)
                connectButton.text = viewModel.screenDataState.value.client?.name ?: ""
                Toast.makeText(
                    applicationContext,
                    getString(R.string.connectionEstablished),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun fixationColorChange(event: MotionEvent) {
        curColor.activButton?.backgroundTintList = ColorStateList.valueOf(curColor.color)
        if (event.action == MotionEvent.ACTION_UP) {
            val index = colorButtons.indexOf(curColor.activButton)
            actuallyColorsButtons[index] = curColor.color
            lifecycleScope.launch {
                val resp = viewModel.colorPickerSend(
                    curColor.color, index
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
            val verticalBufferZone = (imageView.height - min(imageView.width, imageView.height)) / 2
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
                        (x * bitmap.width).toInt(), (y * bitmap.height).toInt()
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

    private val amplifierListener = OnClickListener {
        lifecycleScope.launch {
            viewModel.setAmplifierStatus(!screenData.value.amplifier).getOrElse {
                viewModel.disconnect()
                return@launch
            }
        }
    }
    private val audioBTListener = OnClickListener {
        lifecycleScope.launch {
            viewModel.setAudioBTStatus(!screenData.value.audioBT).getOrElse {
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
            butt.alpha = 1f
            val index = colorButtons.indexOf(butt)
            viewModel.updateColor(actuallyColorsButtons[index], index)
        } else {
            butt.alpha = 0.5f
            viewModel.updateColor(Color.BLACK, colorButtons.indexOf(butt))
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
        /*changeActiveStatus(colorButtons, true)
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
        }*/
        viewModel.setModeColors(num + 1)
    }

    private val changeType = OnClickListener { view ->
        val radioButton = view as RadioButton
        val listOfTexts = listOf(
            listOf(
                "База",
                "Мерцание",
                "",
                "",
                "",
                ""
            ), listOf(
                "Вспышки", "Бег", "Столбец", "", "", ""
            ), listOf(
                "Вспышки",
                "Бег",
                "Распеделение",
                "",
                "",
                ""
            ), listOf("База", "", "", "", "", "")
        )
        val num = types.indexOf(radioButton)
        if (num == -1) {
            return@OnClickListener
        }
        texts = listOfTexts[num]
        viewModel.setTypeColors(num + 1)
    }

    private val changeExtraParam = OnClickListener { view ->
        val radioButton = view as RadioButton
        when (radioButton) {
            hsv -> {
                viewModel.setHSVStatus(!screenData.value.hsv)
            }
            gradient -> {
                viewModel.setGradientStatus(!screenData.value.gradient)
            }
            movement -> {
                viewModel.setMovementStatus(!screenData.value.movement)
            }
            synchronously -> {
                viewModel.setSynchronStatus(!screenData.value.synchrony)
            }
        }
    }

    private fun initColorButs() = listOf(
        R.id.buttonColor0,
        R.id.buttonColor1,
        R.id.buttonColor2,
        R.id.buttonColor3,
        R.id.buttonColor4
    ).map {
        val but = findViewById<Button>(it)
        but.setOnLongClickListener { view ->
            changeActiveStatus(but, but.alpha != 1f)
            true
        }
        but.setOnClickListener { view ->
            if (but.alpha == 1f) {
                curColor.activButton = but
            } else if (curColor.activButton == but) {
                curColor.activButton = null
            }
        }
        return@map but
    }

    private fun initTypes(changeType: OnClickListener) = arrayOf(
        R.id.radioButType1, R.id.radioButType2, R.id.radioButType3, R.id.radioButType4
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
        ).map {
            val but = findViewById<RadioButton>(it)
            but.setOnClickListener(changeMode)
            but
        }
        return modeList
    }

    private fun initParams(changeExtraParam: OnClickListener): List<RadioButton> {
        val paramsList = listOf(
            R.id.hsv,
            R.id.gradient,
            R.id.movement,
            R.id.synchronously,
        ).map {
            val but = findViewById<RadioButton>(it)
            but.setOnClickListener(changeExtraParam)
            but
        }
        return paramsList
    }

    private fun checkScreenData() = lifecycleScope.launch {
        viewModel.screenDataState.collect { data -> // Обновление в зависимости от значения screenData
            connectButton.text = data.client?.name ?: "Connect"
            hsv.isChecked = screenData.value.hsv
            gradient.isChecked = screenData.value.gradient
            movement.isChecked = screenData.value.movement
            synchronously.isChecked = screenData.value.synchrony
            if (screenData.value.ignition) {
                ignitionButton.setImageResource(R.drawable.start)
            } else {
                ignitionButton.setImageResource(R.drawable.stop)
            }
            if (screenData.value.amplifier) {
                amplifierButton.setImageResource(R.drawable.sound)
            } else {
                amplifierButton.setImageResource(R.drawable.mute)
            }
            if (screenData.value.audioBT) {
                audioBTButton.setImageResource(R.drawable.bt)
            } else {
                audioBTButton.setImageResource(R.drawable.aux_pic)
            }
            for (i in 0..<colorButtons.size) {
                if (data.colors[i] != Color.BLACK) {
                    colorButtons[i].backgroundTintList = ColorStateList.valueOf(data.colors[i])
                    actuallyColorsButtons[i] = data.colors[i]
                }
            }
            for (i in 0..3) {
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
        picker.value = value / curColor.stepPicker * curColor.stepPicker
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
        val intent = Intent(getApplication(), BluetoothService::class.java)
        getApplication().startService(intent) // Запуск сервиса

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        bluetoothViewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
        screenData = viewModel.screenDataState
        curColor = screenData.value.curColor

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

        amplifierButton = findViewById(R.id.amplifierButton)
        amplifierButton.setOnClickListener(amplifierListener)

        audioBTButton = findViewById(R.id.audioBTButton)
        audioBTButton.setOnClickListener(audioBTListener)

        colorButtons = initColorButs()

        types = initTypes(changeType)

        modes = initModes(changeMode)

        extraParams = initParams(changeExtraParam)
        hsv = extraParams[0]
        gradient = extraParams[1]
        movement = extraParams[2]
        synchronously = extraParams[3]

        checkScreenData()
    }
}