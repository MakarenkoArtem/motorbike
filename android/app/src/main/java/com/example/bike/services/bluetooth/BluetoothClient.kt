package com.example.bike.services.bluetooth

import android.bluetooth.BluetoothSocket
import android.graphics.Color
import android.util.Log
import com.example.bike.datasource.remote.model.BluetoothData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class BluetoothClient(
    var address: String,
    private val bTSocket: BluetoothSocket,
    var name: String = ""
) {
    private val inputStream: InputStream
    private val outputStream: OutputStream
    private val _bluetoothData: MutableStateFlow<BluetoothData> = MutableStateFlow(BluetoothData())
    val bluetoothData: StateFlow<BluetoothData> = _bluetoothData
    private val clientScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        inputStream = bTSocket.inputStream
        outputStream = bTSocket.outputStream
    }

    fun getDataFlow() = bluetoothData

    fun connect(check: Boolean = true): Result<StateFlow<BluetoothData>> {
        if (check) {
            clientScope.launch {
                _bluetoothData.value = _bluetoothData.value.copy(
                    active = connectRequest(5, 500, true).isSuccess
                )
                Log.d("Bike.BluetoothClient", "connect()")
                disconnect()
            }
        } else {
            _bluetoothData.value = _bluetoothData.value.copy(active = true)
        }
        return Result.success(bluetoothData)
    }

    suspend private fun connectRequest(
        count: Int = 1,
        time: Long = 0,
        waitAnswer: Boolean = false
    ): Result<Unit> = kotlin.runCatching {
        var message = ""
        for (i in 0..count) {
            sendMessage("Con\n")
            if (waitAnswer) {
                message = takeMessage().getOrNull() ?: ""
                Log.d("Bike.BluetoothClient", message)
                if (message.substring(0, 2) == "OK") {
                    Log.d("Bike.BluetoothClient", "Connect")
                    return@runCatching
                }
            }
            delay(time)
        }
        throw Exception("Connection lost")
    }


    fun getColors(timeOut: Int = 500): Result<List<Int>> = kotlin.runCatching {
        outputStream.write("GC\n".toByteArray())
        outputStream.flush()
        Log.d("Bike.BluetoothClient", "GC")
        clientScope.launch {
            var time = 0
            while (timeOut <= time && inputStream.available() == 0) {
                delay(100)
                time += 100
            }
            val buffer = ByteArray(1024)
            val size = inputStream.read(buffer)
            val message = String(buffer, 0, size)
            Log.d("BikeBluetooth", "$size: $message")
            if (size == 0) { // Result.failure(IllegalStateException("Нет данных"))
                return@launch
            }
            val data = message.split(",") //Regex(","))
                .dropLastWhile {it.isEmpty()}
            if(data.size<20){return@launch}
            Log.d("Bike.BluetoothClient", "${data.size} ${data.toString()}")
            val colors = MutableList(5, {Color.BLACK})
            for (i in 0..4) {
                val r = data[i * 4 + 1].toInt()
                val g = data[i * 4 + 2].toInt()
                val b = data[i * 4 + 3].toInt()
                val pixels = Color.argb(255, r, g, b)
                colors[i] = pixels
            }
            Log.d("Bike.BluetoothClient", bluetoothData.value.colors.toString())
            _bluetoothData.value = _bluetoothData.value.copy(colors = colors)
        }
        bluetoothData.value.colors
    }


    fun sendMessage(
        message: String,
        repeat: Int = 1,
        timeWait: Long = 100
    ): Result<Unit> {
        clientScope.launch {
            Log.d("BikeBluetooth", message)
            outputStream.write(message.toByteArray())
            repeat(repeat - 1) {
                delay(timeWait)
                outputStream.write(message.toByteArray())
            }
        }
        return Result.success(Unit)
    }

    suspend fun takeMessage(
        repeat: Int = 3,
        timeWait: Long = 50
    ): Result<String> {
        val buffer = ByteArray(1024) // буферный массив
        val executor = Executors.newSingleThreadExecutor() // создаем executor
        return runCatching {
            repeat(repeat) {
                val future = executor.submit<String> {
                    inputStream.read(buffer)
                        .let {size ->
                            if (size > 0) {
                                String(buffer, 0, size)
                            } else {
                                null
                            }
                        }
                }
                try {
                    var result = future.get(timeWait, TimeUnit.MILLISECONDS)
                    if (result != null) {
                        if("OK" in result){
                            result = "OK"
                        }
                        return@runCatching result
                    }
                } catch (e: TimeoutException) { //throw IllegalStateException("Время ожидания истекло")
                }
            }
            throw IllegalStateException("Время ожидания истекло")
        }.also {
            executor.shutdown()
        }
    }

    suspend fun colorSend(
        color: Int,
        index: Int
    ): Result<Unit> = runCatching {
        val message = String.format(
            "Cr:%03d,%03d,%03d,%03d,\n",
            51 * index + 26,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        sendMessage(message).getOrThrow()
        clientScope.launch {
            if ((takeMessage().getOrNull() ?: "") == "Damaged message") {
                connect(true)
            }
        }
    }

    suspend fun colorsSend(colors: List<Int>): Result<Unit> {
        var message = "Co:"
        return runCatching {
            for (i in 0..4) {
                message += String.format(
                    "%03d,%03d,%03d,%03d,",
                    51 * i + 26,
                    Color.red(colors[i]),
                    Color.green(colors[i]),
                    Color.blue(colors[i])
                )
            }
            message += "\n"
            sendMessage(message).getOrThrow()
            clientScope.launch {
                if ((takeMessage().getOrNull() ?: "") == "Damaged message") {
                    connect(true)
                }
            }
        }
    }

    fun disconnect(): Result<Unit> = kotlin.runCatching {
        _bluetoothData.value = _bluetoothData.value.copy(connected = false)
        clientScope.cancel()
        Log.d("Bike.BluetoothClient", "disconnect")
        bTSocket.close()
    }
}