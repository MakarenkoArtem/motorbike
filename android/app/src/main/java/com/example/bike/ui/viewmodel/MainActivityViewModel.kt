package com.example.bike.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bike.model.CurrentColor
import com.example.bike.model.ScreenViewData
import com.example.bike.services.bluetooth.BluetoothClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivityViewModel: ViewModel() {
    private val _screenDataState = MutableStateFlow(ScreenViewData(curColor = CurrentColor()))
    val screenDataState: StateFlow<ScreenViewData> = _screenDataState

    fun connect(client:BluetoothClient): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(client = client)
        val res = checkConnection()
        Log.d("BikeBluetoothCheck", res.toString())
        if (res.isFailure) {
            return res
        }
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
        val checkingDevice = checkDevice().getOrElse { return Result.failure(it) }
        val res = screenDataState.value.client!!.connect(check = false)
        if (res.isFailure) {
            _screenDataState.value = _screenDataState.value.copy(client = null)
            return res
        }
        return Result.success(Unit)
    }


    fun getColors(): Result<List<Int>> {
        if (checkDevice().isFailure) {
            return Result.success(emptyList())
        }
        val res = _screenDataState.value.client!!.getColors()
        Log.d("BikeBluetooth", "$res")
        val colors = res.getOrElse { return res }
        Log.d("BikeBluetooth", "Colors: $colors")
        _screenDataState.value = _screenDataState.value.copy(colors = colors)
        return res
    }

    fun updateColor(pixel: Int, index: Int): Result<Unit> {
        return runCatching {
            val colors = screenDataState.value.colors.toMutableList()
            colors[index] = pixel
            _screenDataState.value = _screenDataState.value.copy(colors = colors)
            var res = Result.success(Unit)
            repeat(2) {
                res = screenDataState.value.client?.colorSend(pixel, index) ?: res
                if (res.isSuccess) {
                    return res
                }
                res = screenDataState.value.client?.colorsSend(screenDataState.value.colors) ?: res
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

    fun setBrightness(brightness: Int) {
        _screenDataState.value = _screenDataState.value.copy(brightness = brightness)
        send("Br:$brightness\n")
    }

    fun setFrequency(frequency: Int) {
        _screenDataState.value = _screenDataState.value.copy(frequency = frequency)
        send("CF:$frequency\n")
    }

    private fun changeStatus(status: Boolean, active: String, passive: String): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse { return Result.failure(it) }
        if (status) {
            screenDataState.value.client!!.sendMessage(active)
        } else {
            screenDataState.value.client!!.sendMessage(passive)
        }
        val resp = screenDataState.value.client!!.takeMessage(2, 250)
        val mes = resp.getOrElse { return Result.failure(it) }
        if (mes == "OK") {
            return Result.success(Unit)
        }
        return Result.failure(IllegalStateException(""))
    }

    fun setIgnition(status: Boolean): Result<Unit> {
        val resp = changeStatus(status, "ON\n", "OFF\n")
        if (resp.isSuccess) {
            _screenDataState.value = _screenDataState.value.copy(ignition = status)
        }
        return resp
    }

    fun setTypeColors(index: Int): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(type = index)
        return send("Ty:${screenDataState.value.type}${screenDataState.value.mode}\n")
    }

    fun setModeColors(index: Int): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(mode = index)
        return send("Ty:${screenDataState.value.type}${screenDataState.value.mode}\n")
    }

    fun setHSVStatus(status: Boolean): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(hsv = status)
        return send(if (status) "OnHSV\n" else "OffHSV\n")
    }

    fun setGradientStatus(status: Boolean): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(gradient = status)
        return send(if (status) "OnGrad\n" else "OffGrad\n")
    }

    fun setMovementStatus(status: Boolean): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(movement = status)
        return send(if (status) "OnMov\n" else "OffMov\n")
    }

    fun setSynchronStatus(status: Boolean): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(synchrony = status)
        return send(if (status) "OnSync\n" else "OffSync\n")
    }

    private fun send(message: String, repeat: Int = 3, timeWait: Long = 100): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse { return Result.failure(it) }
        return screenDataState.value.client!!.sendMessage(
            message,
            repeat,
            timeWait
        )
    }


    fun setAmplifierStatus(status: Boolean): Result<Unit> {
        val resp = changeStatus(status, "HighAmp\n", "LowAmp\n")
        if (resp.isSuccess) {
            _screenDataState.value = _screenDataState.value.copy(amplifier = status)
        }
        return resp
    }


    fun setAudioBTStatus(status: Boolean): Result<Unit> {
        val resp = changeStatus(status, "OnBT\n", "OffBT\n")
        if (resp.isSuccess) {
            _screenDataState.value = _screenDataState.value.copy(audioBT = status)
        }
        return resp
    }


    fun disconnect() {
        runCatching { screenDataState.value.client?.disconnect() }
        _screenDataState.value = _screenDataState.value.copy(client = null)
    }
}