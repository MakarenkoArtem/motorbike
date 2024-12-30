package com.example.bike.screens.MainActivity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bike.BT.BTClient
import com.example.bike.BT.BTService
import com.example.bike.model.CurrentColor
import com.example.bike.model.ScreenViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivityViewModel(
    context: Context,
    activity: Activity
) : ViewModel() {
    private val _screenDataState = MutableStateFlow(ScreenViewData(curColor = CurrentColor()))
    val screenDataState: StateFlow<ScreenViewData> = _screenDataState
    val btService: BTService = BTService(context = context, activity = activity)

    fun connect(device: BluetoothDevice): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(device = BTClient(device))
        val res = checkConnection()
        Log.d("BikeBluetoothCheck", res.toString())
        if (res.isFailure) {
            return res
        }
        return getColors() as Result<Unit>
    }

    fun checkDevice(): Result<Unit> {
        if (_screenDataState.value.device == null) {
            return Result.failure(IllegalStateException(""))
        }
        Log.d("BikeBluetooth", "00!!!connect")
        return Result.success(Unit)

    }

    fun checkConnection(): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse { return Result.failure(it) }
        val res = screenDataState.value.device!!.connect(check = false)
        if (res.isFailure) {
            _screenDataState.value = _screenDataState.value.copy(device = null)
            return res
        }
        return Result.success(Unit)
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        val pairedDevices = btService.getPairedDevices()
        Log.d("myLog", pairedDevices.toString())
        return pairedDevices
    }

    fun getColors(): Result<List<Int>> {
        if (checkDevice().isFailure) {
            return Result.success(emptyList())
        }
        val res = _screenDataState.value.device!!.getColors()
        Log.d("BikeBluetooth", "$res")
        val colors = res.getOrElse { return res }
        Log.d("BikeBluetooth", "Colors: $colors")
        _screenDataState.value = _screenDataState.value.copy(colors = colors)
        return res
    }

    fun colorPickerSend(pixel: Int, index: Int): Result<Unit> {
        return runCatching {
            val colors = screenDataState.value.colors.toMutableList()
            colors[index] = pixel
            _screenDataState.value = _screenDataState.value.copy(colors = colors)
            var res = Result.success(Unit)
            repeat(2) {
                res = screenDataState.value.device?.colorsSend(screenDataState.value.colors)
                    ?: res
                if (res.isSuccess) {
                    return res
                }
            }
            return checkConnection()
        }
    }

    fun setBrightness(brightness: Int) {
        _screenDataState.value = _screenDataState.value.copy(brightness = brightness)
        screenDataState.value.device?.sendMessage("Br:$brightness\n", 3, 100)
            ?: return
                .getOrElse { return }
    }

    fun setFrequency(frequency: Int) {
        _screenDataState.value = _screenDataState.value.copy(frequency = frequency)
        screenDataState.value.device?.sendMessage("CF:$frequency\n", 3, 100)
            ?: return
                .getOrElse { return }
    }

    fun setIgnition(ignition: Boolean): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse { return Result.failure(it) }
        Log.d("BikeBluetooth Device:", screenDataState.value.device!!.name)
        if (ignition) {
            screenDataState.value.device!!.sendMessage("ON\n")
        } else {
            screenDataState.value.device!!.sendMessage("OFF\n")
        }
        val resp = screenDataState.value.device!!.takeMessage(2, 250)
        val mes = resp.getOrElse { return Result.failure(it) }
        Log.d("BikeBluetooth", "$mes ${mes.length} ${mes == "OK"}")
        if (mes == "OK") {
            _screenDataState.value = _screenDataState.value.copy(ignition = ignition)
            return Result.success(Unit)
        }
        return Result.failure(IllegalStateException(""))
    }

    fun setTypeColors(index: Int): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(type = index)
        return sendType()
    }

    fun setModeColors(index: Int): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(mode = index)
        return sendType()
    }

    fun setSynchron(synchrony: Boolean): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(synchrony = synchrony)
        return sendType()
    }

    fun sendType(): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse { return Result.failure(it) }
        return screenDataState.value.device!!.sendMessage(
            "Ty:${if (screenDataState.value.synchrony) 1 else 0}${screenDataState.value.type}${screenDataState.value.mode}\n",
            3,
            100
        )
    }

    fun setSound(sound: Boolean): Result<Unit> {
        val checkingDevice = checkDevice().getOrElse { return Result.failure(it) }
        if (sound) {
            screenDataState.value.device!!.sendMessage("HIGH\n")
        } else {
            screenDataState.value.device!!.sendMessage("LOW\n")
        }
        val resp = screenDataState.value.device!!.takeMessage(2, 250)
            .getOrElse { return Result.failure(it) }
        _screenDataState.value = _screenDataState.value.copy(sound = sound)
        return Result.success(Unit)
    }


    fun disconnect() {
        runCatching { screenDataState.value.device?.disconnect() }
        _screenDataState.value = _screenDataState.value.copy(device = null)
    }
}