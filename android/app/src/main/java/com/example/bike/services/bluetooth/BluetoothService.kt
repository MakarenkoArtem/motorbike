package com.example.bike.services.bluetooth

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.bike.domain.model.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BluetoothService: Service() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val _bluetoothDevicesFlow: MutableStateFlow<List<BluetoothDevice>> =
        MutableStateFlow(emptyList())
    private val _devicesFlow: MutableStateFlow<List<Device>> = MutableStateFlow(emptyList())
    val devicesFlow: StateFlow<List<Device>> = _devicesFlow

    override fun onCreate() {
        super.onCreate()
        getStatus()
    }

    private val binder = LocalBinder()

    inner class LocalBinder: Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun getStatus(): Result<Unit> = kotlin.runCatching {
        checkAdapter().getOrThrow()
        updateBondedDevices().getOrThrow()
    }

    private fun updateAdapter(): Result<Unit> = kotlin.runCatching {
        bluetoothAdapter = getSystemService(BluetoothManager::class.java)?.adapter
        if (bluetoothAdapter == null) {
            throw Exception("Bluetooth adapter not found")
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) // Android 14+ (API 34+)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) // Android 12-13 (API 31-33)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) // Android 10-11 (API 29-30)

            else -> emptyArray() // Для Android 9 и ниже разрешения не нужны
        }
    }

    fun checkBluetoothPermission(): Result<Unit> = kotlin.runCatching {
        val permissions = getRequiredPermissions()
        val answer = permissions.map {permission ->
            ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (answer.any {false}) {
            throw Exception("Does not have permissions: " + answer.mapIndexedNotNull {ind, it ->
                if (it) {
                    null
                } else {
                    permissions[ind]
                }
            }
                .joinToString(", "))
        }
    }

    fun checkAdapter(): Result<Unit> = kotlin.runCatching {
        if (bluetoothAdapter == null) {
            updateAdapter().getOrThrow()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            throw IllegalStateException("Bluetooth is turned off")
        }
    }

    fun getDevicesFlow(): StateFlow<List<Device>> {
        updateBondedDevices()
        return devicesFlow
    }

    private fun updateBondedDevices(): Result<Unit> = kotlin.runCatching {
        checkAdapter().getOrThrow()
        checkBluetoothPermission().getOrThrow()
        val bondedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        _bluetoothDevicesFlow.value = bondedDevices.toList()
        _devicesFlow.value = bondedDevices.mapNotNull {it -> getDevice(it).getOrNull()}
    }

    fun getClient(device: Device): Result<BluetoothClient> = kotlin.runCatching {
        checkBluetoothPermission().getOrThrow()
        val device = _bluetoothDevicesFlow.value.filter {getDevice(it).getOrNull() == device}
            .getOrNull(0) ?: throw Exception("Device not found")
        val bTSocket: BluetoothSocket =
            device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")) //незащищенное соединение
        bTSocket.connect()
        val client = BluetoothClient(
            address = device.address,
            bTSocket = bTSocket,
            name = device.name
        )
        return@runCatching client
    }

    private fun getDevice(device: BluetoothDevice): Result<Device> = kotlin.runCatching {
        checkBluetoothPermission().getOrThrow()
        return@runCatching Device(address = device.address, name = device.name)
    }
}