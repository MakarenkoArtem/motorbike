package com.example.bike.ui.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.alpha
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bike.domain.model.Device
import com.example.bike.domain.repository.IBluetoothRepository
import com.example.bike.model.CurrentColor
import com.example.bike.model.ScreenViewData
import com.example.bike.ui.mapper.updateByBluetoothData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainActivityViewModel(val bluetoothRepository: IBluetoothRepository): ViewModel() {
    private val _screenDataState = MutableStateFlow(ScreenViewData(curColor = CurrentColor()))
    val screenDataState: StateFlow<ScreenViewData> = _screenDataState

    init {
        bluetoothRepository.getDevice()
            .onSuccess {
                _screenDataState.value = _screenDataState.value.copy(
                    connectButtonTitle = it.name
                )
                bluetoothRepository.getColors()
            }
        setType(screenDataState.value.type)
        viewModelScope.launch {
            while (true) {
                var data = bluetoothRepository.getDataFlow()
                while (data.isFailure) {
                    delay(1000)
                    data = bluetoothRepository.getDataFlow()
                }
                val dataState = data.getOrNull() ?: return@launch
                dataState.collect {newData ->
                    if (!newData.connected) { //если сигнал потерян отписываемся от потока
                        return@collect
                    }
                    _screenDataState.value = _screenDataState.value.updateByBluetoothData(newData)
                }
            }
        }
    }

    fun changeCurrentColorButton(index: Int) {
        if (!(index in 0 until screenDataState.value.colors.size)) {
            return
        }
        _screenDataState.value = _screenDataState.value.copy(
            curColor = CurrentColor(
                index, screenDataState.value.colors[index].color
            )
        )
    }

    fun changeActiveStatus(index: Int) {
        _screenDataState.value =
            _screenDataState.value.copy(colors = screenDataState.value.colors.mapIndexed {ind, data ->
                if (ind == index) {
                    _screenDataState.value = _screenDataState.value.copy(
                        curColor = _screenDataState.value.curColor.copy(
                            if (data.enabled) {
                                -1
                            } else {
                                index
                            }
                        )
                    )
                    viewModelScope.launch {
                        if (data.enabled) {
                            bluetoothRepository.colorSend(index=ind, color=Color.Black.toArgb())
                        } else {
                            bluetoothRepository.colorSend(index=ind, color=data.color)
                        }
                    } //setCurrentColor(ind)
                    return@mapIndexed data.copy(enabled = !data.enabled)
                }
                data
            })
    }

    fun connect(device: Device) = kotlin.runCatching {
        bluetoothRepository.connect(device)
            .getOrThrow()
        checkConnection()
    }

    fun checkDevice(): Result<Unit> = bluetoothRepository.getDevice() as Result<Unit>

    fun checkConnection(): Result<Unit> {
        val title = bluetoothRepository.getDevice()
            .getOrNull()?.name ?: "Connect"
        _screenDataState.value = _screenDataState.value.copy(connectButtonTitle = title)
        getColors()
        return checkDevice()
    }


    fun getColors(): Result<List<Int>> = kotlin.runCatching {
        val res = bluetoothRepository.getColors()
            .getOrThrow()

        // теперь значения с блютуз устроства обновляются поточно обновляются
        // здесь просто вызывается запрос на получение цветов, обновленные цвета вернутся в поток
        /*val colors = res.map {ColorButtonViewData(it, it != Color.Black.toArgb())}
        _screenDataState.value = _screenDataState.value.copy(colors = colors)*/
        res
    }

    fun colorPickerSend(pixel: Int) {
        if (screenDataState.value.curColor.index !in 0 until screenDataState.value.colors.size) {
            return
        }
        if (pixel.alpha != 0) {
            screenDataState.value.curColor.color = pixel
            updateColors()
        }
    }

    fun updateColors() {
        val colors = screenDataState.value.colors.toMutableList()
        colors[screenDataState.value.curColor.index] =
            colors[screenDataState.value.curColor.index].copy(color = screenDataState.value.curColor.color)
        _screenDataState.value = _screenDataState.value.copy(colors = colors)
        updateColor(screenDataState.value.curColor.color, screenDataState.value.curColor.index)
    }

    private fun updateColor(
        pixel: Int,
        index: Int
    ) {
        viewModelScope.launch {
            var res = Result.success(Unit)
            repeat(2) { //новый метод отправлять только изменившийся цвет
                res = bluetoothRepository.colorSend(pixel, index)/*if (res.isSuccess) {
                    return@launch
                }*/ //старый метод отправлять все цвета
                res = bluetoothRepository.colorsSend(screenDataState.value.colors.map {it.color})
                if (res.isSuccess) {
                    return@launch
                }
            }
            checkConnection()
        }
    }

    fun setBrightness(brightness: Float) {
        _screenDataState.value = _screenDataState.value.copy(brightness = brightness)
        send("Br:${brightness.toInt()}\n")
    }

    fun setFrequency(frequency: Float) {
        _screenDataState.value = _screenDataState.value.copy(frequency = frequency)
        send("CF:${frequency.toInt()}\n")
    }

    suspend private fun changeStatus(
        status: Boolean,
        active: String,
        passive: String
    ): Result<Unit> = kotlin.runCatching {
        if (status) {
            send(active)
        } else {
            send(passive)
        }
        val message = bluetoothRepository.takeMessage(2, 250)
            .getOrThrow()
        if (message != "OK") {
            throw IllegalStateException("Uncorrected message: '$message'")
        }
    }

    fun setIgnition() {
        viewModelScope.launch {
            val resp = withTimeoutOrNull(5000) {
                changeStatus(!screenDataState.value.ignition, "ON\n", "OFF\n")
            } ?: Result.failure(Exception("Time out"))
            Log.d("Bike.BluetoothClient", resp.toString())
            if (resp.isSuccess) {
                _screenDataState.value =
                    _screenDataState.value.copy(ignition = !screenDataState.value.ignition)
            } else {
                disconnect()
            }
        }
    }

    fun setType(type: Int) {
        val listOfTitles = listOf(
            listOf(
                "База", "Мерцание"
            ), listOf(
                "Вспышки", "Бег", "Столбец"
            ), listOf(
                "Вспышки", "Бег", "Распеделение"
            ), listOf("База")
        )
        if (type !in 0 until listOfTitles.size) {
            return
        }
        _screenDataState.value =
            _screenDataState.value.copy(titlesMode = listOfTitles[type], type = type)
        send("Ty:${screenDataState.value.type + 1}${screenDataState.value.mode + 1}\n")
    }

    fun setMode(mode: Int) {
        _screenDataState.value = _screenDataState.value.copy(mode = mode)
        send("Ty:${screenDataState.value.type + 1}${screenDataState.value.mode + 1}\n")
    }

    fun setHSVStatus(status: Boolean) {
        send(if (status) "OnHSV\n" else "OffHSV\n").onSuccess {
            _screenDataState.value = _screenDataState.value.copy(hsv = status)
        }
    }

    fun setGradientStatus(status: Boolean) {
        send(if (status) "OnGrad\n" else "OffGrad\n").onSuccess {
            _screenDataState.value = _screenDataState.value.copy(gradient = status)
        }
    }

    fun setMovementStatus(status: Boolean) {
        send(if (status) "OnMov\n" else "OffMov\n").onSuccess {
            _screenDataState.value = _screenDataState.value.copy(movement = status)
        }
    }

    fun setSynchronyStatus(status: Boolean) {
        send(if (status) "OnSync\n" else "OffSync\n").onSuccess {
            _screenDataState.value = _screenDataState.value.copy(synchrony = status)
        }
    }

    private fun send(
        message: String,
        repeat: Int = 3,
        timeWait: Long = 100
    ): Result<Unit> = bluetoothRepository.send(message, repeat, timeWait)


    fun setAmplifierStatus() {
        viewModelScope.launch {
            val resp = withTimeoutOrNull(5000) {
                changeStatus(!screenDataState.value.amplifier, "HighAmp\n", "LowAmp\n")
            } ?: Result.failure(Exception("Time out"))
            Log.d("Bike.BluetoothClient", resp.toString())
            if (resp.isSuccess) {
                _screenDataState.value =
                    _screenDataState.value.copy(amplifier = !screenDataState.value.amplifier)
            } else {
                disconnect()
            }
        }
    }

    fun setAudioBTStatus() {
        viewModelScope.launch {
            val resp = withTimeoutOrNull(5000) {
                changeStatus(!screenDataState.value.audioBT, "OnBT\n", "OffBT\n")
            } ?: Result.failure(Exception("Time out"))
            Log.d("Bike.BluetoothClient", resp.toString())
            if (resp.isSuccess) {
                _screenDataState.value =
                    _screenDataState.value.copy(audioBT = !screenDataState.value.audioBT)
            } else {
                disconnect()
            }
        }
    }

    fun updateRed(value: Int) {
        if (!(value in 0..255)) {
            return
        }
        val color = screenDataState.value.curColor.updatePicker(
            value, 'r'
        )
            .getOrElse {return}
        colorPickerSend(color)
    }

    fun updateGreen(value: Int): Unit {
        if (!(value in 0..255)) {
            return
        }
        val color = screenDataState.value.curColor.updatePicker(
            value, 'g'
        )
            .getOrElse {return}
        colorPickerSend(color)
    }

    fun updateBlue(value: Int): Unit {
        if (!(value in 0..255)) {
            return
        }
        val color = screenDataState.value.curColor.updatePicker(
            value, 'b'
        )
            .getOrElse {return}
        colorPickerSend(color)
    }

    fun disconnect() {
        bluetoothRepository.disconnect()
        _screenDataState.value = _screenDataState.value.copy(connectButtonTitle = "Connect")
    }
}