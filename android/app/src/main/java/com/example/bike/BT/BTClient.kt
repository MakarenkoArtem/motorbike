package com.example.bike.BT

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class BTClient(
    val device: BluetoothDevice,
) {
    var name: String = ""
    var bTSocket: BluetoothSocket? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null


    init {
        try {
            name = device.name
        } catch (e: SecurityException) {
        }
    }

    fun connect(check: Boolean = true): Result<Unit> {
        try {//Инициируем соединение с устройством
            Log.d("BikeBluetooth", "!!!connect")
            /*bTSocket =
                device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")) //защищенное соединение*/
            bTSocket =
                device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")) //незащищенное соединение
            bTSocket!!.connect()
            inputStream = bTSocket!!.inputStream
            outputStream = bTSocket!!.outputStream
            Log.d("BikeBluetooth", "!!!$outputStream $inputStream")
        } catch (e: SecurityException) {
            Log.d("BikeBluetooth", e.message!!)
            return Result.failure(e)
        } catch (closeException: IOException) {
            Log.d("BikeBluetooth", "Could not close the client socket $closeException")
            return Result.failure(closeException)
        } catch (e: Exception) {
            Log.d("BikeBluetooth", e.message!!)
            return Result.failure(e)
        }
        if (check) {
            return connectRequest(5, 500, true)
        } else {
            return Result.success(Unit)
        }
    }

    fun connectRequest(
        count: Int = 1,
        time: Long = 0,
        wait_answer: Boolean = false
    ): Result<Unit> {
        Log.d("BikeBluetooth", "BTSend")
        var message = ""
        return runCatching {
            for (i in 0..count) {
                sendMessage("Con\n")
                SystemClock.sleep(time)
                if (wait_answer) {
                    message = takeMessage().getOrNull() ?: ""
                    Log.d("BikeBluetooth", message)
                    if (message.substring(0, 2) == "OK") {
                        Log.d("BikeBluetooth", "Connect")
                        return Result.success(Unit)
                    }
                }
            }
        }
    }

    fun getColors(
        timeOut: Int = 500
    ): Result<List<Int>> {
        Log.d("BikeBluetooth", "$outputStream $inputStream")
        outputStream?.write("GC\n".toByteArray())
        var time = 0
        while (timeOut <= time && inputStream?.available() == 0) {
            SystemClock.sleep(100)
            time += 100
        }
        val buffer = ByteArray(1024)// буферный массив
        val size = inputStream?.read(buffer) ?: 0
        val message = String(buffer, 0, size)
        Log.d("BikeBluetooth", "$size: $message")
        if (size == 0) {
            return Result.failure(IllegalStateException("Нет данных"))
        }
        val data = message.split(Regex(",")).dropLastWhile { it.isEmpty() }
        return runCatching {
            val colors = Array(5, { Color.BLACK })
            for (i in 0..4) {
                val r = data[i * 4 + 1].toInt()
                val g = data[i * 4 + 2].toInt()
                val b = data[i * 4 + 3].toInt()
                val pixels = Color.argb(255, r, g, b)
                colors[i] = pixels
                //colorButtons[i]!!.backgroundTintList = ColorStateList.valueOf(pixels)
            }
            Log.d("BikeBluetooth", colors.toString())
            colors.toList()
        }
    }


    fun sendMessage(
        message: String,
        repeat: Int = 1,
        timeWait: Long = 100
    ): Result<Unit> {
        return runCatching {
            Log.d("BikeBluetooth", message)
            outputStream?.write(message.toByteArray())
            repeat(repeat - 1) {
                SystemClock.sleep(timeWait)
                outputStream?.write(message.toByteArray())
            }
        }
    }

    fun takeMessage(
        repeat: Int = 1,
        timeWait: Long = 200
    ): Result<String> {
        val buffer = ByteArray(1024) // буферный массив
        val executor = Executors.newSingleThreadExecutor() // создаем executor
        return runCatching {
            for (i in 0 until repeat) {
                val future = executor.submit<String> {
                    inputStream?.read(buffer)?.let { size ->
                        if (size > 0) {
                            String(buffer, 0, size)
                        } else {
                            null
                        }
                    } ?: throw IllegalStateException("Нет входного потока")
                }
                try {
                    // Ждем ответа с тайм-аутом
                    val result = future.get(timeWait, TimeUnit.MILLISECONDS)
                    if (result != null) {
                        return@runCatching result
                    }
                } catch (e: TimeoutException) {
                    Log.d("BikeBluetooth", "Ошибка: ${e.message}")
                    //throw IllegalStateException("Время ожидания истекло")
                } catch (e: Exception) {
                    // Обработка других исключений, если они возникают
                    Log.d("BikeBluetooth", "Ошибка: ${e.message}")
                }
                /*
                // Задержка перед следующим чтением, если это не последний раз
                if (i != repeat - 1) {
                    SystemClock.sleep(timeWait)
                }*/
            }
            throw IllegalStateException("Время ожидания истекло")
        }.also {
            executor.shutdown() // Закрываем executor
        }
    }

    fun colorSend(color: Int, index: Int): Result<Unit> {
        return runCatching {
            val message = "Cr:" + String.format(
                "%03d,%03d,%03d,%03d,",
                51 * index + 26, Color.red(color), Color.green(color), Color.blue(color)
            ) + "\n"
            sendMessage(message)
            if ((takeMessage().getOrNull() ?: "") == "Damaged message") {
                throw IllegalStateException("Damaged message")
            }
        }
    }

    fun colorsSend(colors: List<Int>): Result<Unit> {
        var message = "Co:"
        return runCatching {
            for (i in 0..4) {
                message += String.format(
                    "%03d,%03d,%03d,%03d,",
                    51 * i + 26, Color.red(colors[i]), Color.green(colors[i]), Color.blue(colors[i])
                )
            }
            message += "\n"
            sendMessage(message)
            if ((takeMessage().getOrNull() ?: "") == "Damaged message") {
                throw IllegalStateException("Damaged message")
            }
        }
    }

    fun disconnect(): Result<Unit> {
        Log.d("BikeBluetooth", "disconnect")
        try {
            bTSocket!!.close()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
}