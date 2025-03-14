package com.example.bike.services.bluetooth

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    var service: BluetoothService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as BluetoothService.LocalBinder
            service = localBinder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    init{
        viewModelScope.launch{bindBluetoothService()}
    }

    suspend fun waitForService(timeLimit:Long=5000):Result<Unit> {
        val end= LocalTime.now().plus(timeLimit, ChronoUnit.MILLIS)
        while (service == null && LocalTime.now()<end) {
            delay(50)
        }
        return if(service==null){
            Result.failure(Exception("Bluetooth service didn't connect"))
        }else{
            Result.success(Unit)
        }
    }


    suspend fun bindBluetoothService():Result<Unit> {
        val intent = Intent(getApplication(), BluetoothService::class.java)
        getApplication<Application>().bindService(
            intent, serviceConnection, Context.BIND_AUTO_CREATE
        )
        return waitForService()
    }

    fun getRequiredPermissions() = service?.getRequiredPermissions() ?: emptyArray()

    fun checkBluetoothPermission() = service?.checkBluetoothPermission()
        ?: Result.failure(Exception("BluetoothService does not bound"))

    fun isConnected() = service?.checkAdapter()
        ?: Result.failure(Exception("BluetoothService does not bound"))

    fun getDevicesFlow(): Result<StateFlow<List<BluetoothDevice>>> {
        val devices = service?.getDevicesFlow() ?: return Result.failure(Exception(""))
        return Result.success(devices)
    }

    fun getClient(device: BluetoothDevice) =
        service?.getClient(device) ?: Result.failure(Exception(""))
}