package com.example.bike.ui.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.alpha
import androidx.lifecycle.ViewModel
import com.example.bike.MainActivity.MainActivity
import com.example.bike.model.CurrentColor
import com.example.bike.model.ScreenViewData
import com.example.bike.services.bluetooth.BluetoothClient
import com.example.bike.services.bluetooth.BluetoothViewModel
import com.example.bike.ui.model.ColorButtonViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivityViewModel(val bluetoothViewModel: BluetoothViewModel): ViewModel() {
    private val _screenDataState = MutableStateFlow(ScreenViewData(curColor = CurrentColor()))
    val screenDataState: StateFlow<ScreenViewData> = _screenDataState

    init {
        setTypeColors(screenDataState.value.type)
    }

    fun changeCurrentColorButton(index: Int) {
        if (!(index in 0 until screenDataState.value.colors.size)) {
            return
        } //screenDataState.value.curColor.setColorButton(index, button, screenDataState.value.colors[index])

        Log.d("BikeBluetooth", index.toString())
        _screenDataState.value = _screenDataState.value.copy(curColor = CurrentColor(index,
                screenDataState.value.colors[index].color
        )
        )
    }

    fun changeActiveStatus(index: Int) {
        _screenDataState.value =
            _screenDataState.value.copy(colors = screenDataState.value.colors.mapIndexed {ind, data ->
                if (ind == index) {
                    return@mapIndexed data.copy(enabled = !data.enabled)
                }
                data
            })
        if (screenDataState.value.colors[index].enabled) {
            changeCurrentColorButton(index)
        } else if (screenDataState.value.curColor.index == index) {
            _screenDataState.value =
                _screenDataState.value.copy(curColor = CurrentColor(-1, Color.Black.toArgb()))
        }
    }

    fun connect() {
        if (screenDataState.value.client != null && connect(screenDataState.value.client!!).isSuccess) {
            _screenDataState.value =
                _screenDataState.value.copy(connectButtonTitle = screenDataState.value.client!!.name)
        }
    }

    fun connect(client: BluetoothClient): Result<Unit> {
        client.connect(true).onFailure {
            client.disconnect()
            _screenDataState.value =
                _screenDataState.value.copy(connectButtonTitle = "", client = null)
            return Result.failure(Exception("Connection failed"))
        }
        _screenDataState.value =
            _screenDataState.value.copy(connectButtonTitle = client.name, client = client)
        return getColors() as Result<Unit>
    }

    fun checkDevice(): Result<Unit> {
        if (_screenDataState.value.client == null) {
            return Result.failure(IllegalStateException(""))
        }
        Log.d("BikeBluetooth", "Device exist")
        return Result.success(Unit)

    }

    fun checkConnection(): Result<Unit> {
        val checkingDevice =
            checkDevice().getOrElse {return Result.failure(it)} //val res = screenDataState.value.client!!.connect(check = false)
        /*if (res.isFailure) {
            _screenDataState.value = _screenDataState.value.copy(client = null)
            return res
        }*/
        return Result.success(Unit)
    }


    fun getColors(): Result<List<Int>> {
        if (checkDevice().isFailure) {
            return Result.success(emptyList())
        }
        val res = _screenDataState.value.client!!.getColors()
        Log.d("BikeBluetoothCheck", res.toString())
        val colors =
            res.getOrElse {return res}.map {ColorButtonViewData(it, it != Color.Black.toArgb())}
        Log.d("BikeBluetooth", "Colors: $colors")
        _screenDataState.value = _screenDataState.value.copy(colors = colors)
        return res
    }

    fun setCurrentColor(pixel: Int) {
        if (pixel.alpha != 0 && screenDataState.value.curColor.index in 0 until screenDataState.value.colors.size) {
            screenDataState.value.curColor.color = pixel
            val colors = screenDataState.value.colors.toMutableList()
            colors[screenDataState.value.curColor.index] =
                colors[screenDataState.value.curColor.index].copy(pixel)
            _screenDataState.value =
                screenDataState.value.copy(colors = colors) //screenDataState.value.curColor.activeButton?.setBackgroundColor(pixel)
        }
    }

    fun updateColors() {
        updateColor(screenDataState.value.curColor.color, screenDataState.value.curColor.index)
    }

    private fun updateColor(pixel: Int, index: Int): Result<Unit> {
        return runCatching { //val colors = screenDataState.value.colors.toMutableList()
            //colors[index] = pixel
            //_screenDataState.value = _screenDataState.value.copy(colors = colors)
            var res = Result.success(Unit)
            repeat(2) {
                res = screenDataState.value.client?.colorSend(pixel, index) ?: res
                if (res.isSuccess) {
                    return res
                }
                res =
                    screenDataState.value.client?.colorsSend(screenDataState.value.colors.map {it.color})
                        ?: res
                if (res.isSuccess) {
                    return res
                }
            }
            return checkConnection()
        }
    }

    fun colorPickerSend(pixel: Int, index: Int): Result<Unit> {
        return updateColor(pixel, index)
    }

    fun setBrightness(brightness: Float) {
        _screenDataState.value = _screenDataState.value.copy(brightness = brightness)
        send("Br:${brightness.toInt()}\n")
    }

    fun setFrequency(frequency: Float) {
        _screenDataState.value = _screenDataState.value.copy(frequency = frequency)
        send("CF:${frequency.toInt()}\n")
    }

    private fun changeStatus(status: Boolean, active: String, passive: String): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse {return Result.failure(it)}
        if (status) {
            screenDataState.value.client!!.sendMessage(active)
        } else {
            screenDataState.value.client!!.sendMessage(passive)
        }
        val resp = screenDataState.value.client!!.takeMessage(2, 250)
        val mes = resp.getOrElse {return Result.failure(it)}
        if (mes == "OK") {
            return Result.success(Unit)
        }
        return Result.failure(IllegalStateException(""))
    }

    fun setIgnition() {
        val resp = changeStatus(!screenDataState.value.ignition, "ON\n", "OFF\n")
        if (resp.isSuccess) {
            _screenDataState.value =
                _screenDataState.value.copy(ignition = !screenDataState.value.ignition)
        } else {
            disconnect()
        }
    }

    fun setIgnition(status: Boolean): Result<Unit> {
        val resp = changeStatus(status, "ON\n", "OFF\n")
        if (resp.isSuccess) {
            _screenDataState.value = _screenDataState.value.copy(ignition = status)
        } else {
            disconnect()
        }
        return resp
    }

    fun setTypeColors(index: Int) {
        val listOfTitles = listOf(listOf("База", "Мерцание"
        ), listOf("Вспышки", "Бег", "Столбец"
        ), listOf("Вспышки", "Бег", "Распеделение"
        ), listOf("База")
        )
        if (!(index in 0 until listOfTitles.size)) {
            return
        }
        _screenDataState.value =
            _screenDataState.value.copy(titlesMode = listOfTitles[index], type = index)
        send("Ty:${screenDataState.value.type + 1}${screenDataState.value.mode + 1}\n")
    }

    fun setModeColors(index: Int) {
        _screenDataState.value = _screenDataState.value.copy(mode = index)
        send("Ty:${screenDataState.value.type + 1}${screenDataState.value.mode + 1}\n")
    }

    fun setHSVStatus(status: Boolean) {
        _screenDataState.value = _screenDataState.value.copy(hsv = status)
        send(if (status) "OnHSV\n" else "OffHSV\n").onFailure {
            _screenDataState.value = _screenDataState.value.copy(hsv = !status)
        }
    }

    fun setGradientStatus(status: Boolean) {
        _screenDataState.value = _screenDataState.value.copy(gradient = status)
        send(if (status) "OnGrad\n" else "OffGrad\n").onFailure {
            _screenDataState.value = _screenDataState.value.copy(gradient = !status)
        }
    }

    fun setMovementStatus(status: Boolean) {
        _screenDataState.value = _screenDataState.value.copy(movement = status)
        send(if (status) "OnMov\n" else "OffMov\n").onFailure {
            _screenDataState.value = _screenDataState.value.copy(movement = !status)
        }
    }

    fun setSynchronyStatus(status: Boolean) {
        _screenDataState.value = _screenDataState.value.copy(synchrony = status)
        send(if (status) "OnSync\n" else "OffSync\n").onFailure {
            _screenDataState.value = _screenDataState.value.copy(synchrony = !status)
        }
    }

    private fun send(message: String, repeat: Int = 3, timeWait: Long = 100): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse {return Result.failure(it)}
        return screenDataState.value.client!!.sendMessage(message, repeat, timeWait
        )
    }

    fun setAmplifierStatus() {
        val resp = changeStatus(!screenDataState.value.amplifier, "HighAmp\n", "LowAmp\n")
        if (resp.isSuccess) {
            _screenDataState.value =
                _screenDataState.value.copy(amplifier = !screenDataState.value.amplifier)
        } else {
            disconnect()
        }
    }

    fun setAmplifierStatus(status: Boolean): Result<Unit> {
        val resp = changeStatus(status, "HighAmp\n", "LowAmp\n")
        if (resp.isSuccess) {
            _screenDataState.value = _screenDataState.value.copy(amplifier = status)
        } else {
            disconnect()
        }
        return resp
    }

    fun setAudioBTStatus() {
        val resp = changeStatus(!screenDataState.value.audioBT, "OnBT\n", "OffBT\n")
        if (resp.isSuccess) {
            _screenDataState.value =
                _screenDataState.value.copy(audioBT = !screenDataState.value.audioBT)
        } else {
            disconnect()
        }
    }


    fun setAudioBTStatus(status: Boolean): Result<Unit> {
        val resp = changeStatus(status, "OnBT\n", "OffBT\n")
        if (resp.isSuccess) {
            _screenDataState.value = _screenDataState.value.copy(audioBT = status)
        } else {
            disconnect()
        }
        return resp
    }

    fun updateRed(value: Int) {
        if (!(value in 0..255)) {
            return
        }
        val color=screenDataState.value.curColor.updatePicker(value,
                'r'
        ).getOrElse {return}
        setCurrentColor(color)
    }

    fun updateGreen(value: Int): Unit {
        if (!(value in 0..255)) {
            return
        }
        val color=screenDataState.value.curColor.updatePicker(value,
                'g'
        ).getOrElse {return}
        setCurrentColor(color)
    }

    fun updateBlue(value: Int): Unit {
        if (!(value in 0..255)) {
            return
        }
        val color=screenDataState.value.curColor.updatePicker(value,
                'b'
        ).getOrElse {return}
        setCurrentColor(color)
    }

    fun disconnect() {
        screenDataState.value.client?.disconnect()
        _screenDataState.value =
            _screenDataState.value.copy(connectButtonTitle = "Connect", client = null)
    }
}