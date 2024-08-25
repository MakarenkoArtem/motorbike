package com.example.bike.screens.MainActivity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bike.BT.BTClient
import com.example.bike.BT.BTService
import com.example.bike.model.ScreenViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivityViewModel(
    val context: Context,
    val activity: Activity
) : ViewModel() {
    //var screenData = ScreenViewData()
    private val _screenDataState = MutableStateFlow(ScreenViewData())
    val screenDataState: StateFlow<ScreenViewData> = _screenDataState
    val btService: BTService = BTService(context = context, activity = activity)

    fun connect(device: BluetoothDevice): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(device = BTClient(device))
        val res = screenDataState.value.device!!.connect()
        if (res.isFailure) {
            _screenDataState.value = _screenDataState.value.copy(device = null)
            return res
        }
        getColors()
        return Result.success(Unit)
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        val pairedDevices = btService.getPairedDevices()
        Log.d("myLog", pairedDevices.toString())
        return pairedDevices
    }

    fun getColors(): Result<List<Int>> {
        if (_screenDataState.value.device == null) {
            return Result.success(emptyList())
        }
        var res = _screenDataState.value.device!!.getColors()
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
            Log.d("BikeBluetooth", screenDataState.value.device?.colorsSend(screenDataState.value.colors).toString())
        }
    }

    fun setBrightness(brightness: Int) {
        _screenDataState.value = _screenDataState.value.copy(brightness = brightness)
        screenDataState.value.device?.sendMessage("Br:$brightness\n", 3, 100)
            ?: return
                .getOrElse { return }
        //screenDataState.value.device!!.takeMessage(5, 100)
    }

    fun setFrequency(frequency: Int) {
        _screenDataState.value = _screenDataState.value.copy(frequency = frequency)
        screenDataState.value.device?.sendMessage("CF:$frequency\n", 3, 100)
            ?: return
                .getOrElse { return }
        //screenDataState.value.device!!.takeMessage(5, 100)
    }

    fun setIgnition(ignition: Boolean): Result<Unit> {
        Log.d("BikeBluetooth", screenDataState.value.device?.name ?: "null")
        if (screenDataState.value.device == null) {
            return Result.failure(IllegalStateException(""))
        }
        if (ignition) {
            screenDataState.value.device!!.sendMessage("ON\n")
        } else {
            screenDataState.value.device!!.sendMessage("OFF\n")
        }
        val mes =
            screenDataState.value.device!!.takeMessage(2).getOrElse { return Result.failure(it) }
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

    fun setSynchron(synchron: Boolean): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(synchron = synchron)
        /*if (screenDataState.value.synchron) {
            screenDataState.value.device!!.sendMessage("ON")
        } else {
            screenDataState.value.device!!.sendMessage("OFF")
        }
        val mes =
            screenDataState.value.device!!.takeMessage(2).getOrElse { return Result.failure(it) }
        Log.d("BikeBluetooth", "$mes ${mes.length} ${mes == "OK"}")
        if (mes == "OK") {
            _screenDataState.value = _screenDataState.value.copy(ignition = ignition)
            return Result.success(Unit)
        }
        return Result.failure(IllegalStateException(""))*/
        return sendType()
    }

    fun sendType(): Result<Unit> {
        if (screenDataState.value.device == null) {
            return Result.failure(IllegalStateException(""))
        }
        return screenDataState.value.device?.sendMessage(
            "Ty:${if(screenDataState.value.synchron)1 else 0}${screenDataState.value.type}${screenDataState.value.mode}\n",
            3,
            100
        ) ?: Result.success(Unit)
    }

    fun setSound(sound: Boolean): Result<Unit> {
        if (screenDataState.value.device == null) {
            return Result.failure(IllegalStateException(""))
        }
        if (sound) {
            screenDataState.value.device!!.sendMessage("HIGH\n")
        } else {
            screenDataState.value.device!!.sendMessage("LOW\n")
        }
        screenDataState.value.device!!.takeMessage(2).getOrElse { return Result.failure(it) }
        _screenDataState.value = _screenDataState.value.copy(sound = sound)
        return Result.success(Unit)
    }


    fun disconnect() {
        runCatching { screenDataState.value.device?.disconnect() }
        _screenDataState.value = _screenDataState.value.copy(device = null)
    }
}