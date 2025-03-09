package com.example.bike.services.bluetooth

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothService: Service() {
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val _flow: MutableStateFlow<List<BluetoothDevice>> = MutableStateFlow(emptyList())
    private val flow: StateFlow<List<BluetoothDevice>> = _flow

    override fun onCreate() {
        super.onCreate()
        updateBondedDevices().onFailure {
            Log.d("BikeBluetooth", it.message ?: it.toString())
            Toast.makeText(this, it.message ?: it.toString(), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateService() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
    }

    private val binder = LocalBinder()

    inner class LocalBinder: Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(Manifest.permission.BLUETOOTH_CONNECT,
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

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION
            ) // Android 10-11 (API 29-30)

            else -> emptyArray() // Для Android 9 и ниже разрешения не нужны
        }
    }

    fun checkBluetoothPermission(): Result<Unit> {
        val permissions = getRequiredPermissions()
        val answer = permissions.map {permission ->
            ContextCompat.checkSelfPermission(this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (answer.all {true}) {
            return Result.success(Unit)
        }
        return Result.failure(Exception("Does not have permissions: " + answer.mapIndexed {ind, it ->
            if (it) {
                ""
            } else {
                permissions[ind]
            }
        }.filter {it != ""}.joinToString(", ")))
    }

    fun checkAdapter(): Result<Unit> {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            updateService()
        }
        if (bluetoothAdapter == null) {
            return Result.failure(IllegalStateException("The device does not have Bluetooth"))
        }
        if (!bluetoothAdapter!!.isEnabled) {
            return Result.failure(IllegalStateException("Bluetooth is turned off"))
        }
        return Result.success(Unit)
    }



    fun getDevicesFlow(): StateFlow<List<BluetoothDevice>> {
        updateBondedDevices()
        return flow
    }

    fun updateBondedDevices(): Result<Unit> {
        checkAdapter().onFailure {
            Log.d("BikeBluetooth", it.toString())
            return Result.failure(it)
        }
        checkBluetoothPermission().onFailure {return Result.failure(it)}
        val bondedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        bondedDevices.forEach() {item ->
            Log.d("BikeBluetooth", "${item.name}: ${item.address}")
        }
        _flow.value = bondedDevices.toList()
        return Result.success(Unit)
    }

    fun getClient(device: BluetoothDevice): Result<BluetoothClient> {
        return Result.success(BluetoothClient(device = device))
    }/*val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(
                "BikeBluetooth", intent?.action ?: ""
            )
            checkBluetoothPermission().onFailure { return }
            val device: BluetoothDevice =
                intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
                    ?: return
            val list = _flow.value.toMutableList()
            list.add(device)
            _flow.value = list.toList()
            Log.d(
                "BikeBluetooth",
                "Найдено устройство: ${device.name}, Адрес: ${device.address}"
            )

        }
    }

    fun discoveryDevices(period: Long = 10000) {
        checkBluetoothPermission().onFailure {
            Log.d(
                "BikeBluetooth", it.message ?: ""
            )
            return
        }
        bluetoothAdapter?.startDiscovery() ?: return
        this.registerReceiver(receiver, filter)
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0..period / 100) {
                Log.d(
                    "BikeBluetooth", "Discovering"
                )
                delay(100)
            }
            this@BluetoothService.unregisterReceiver(receiver)
        }
    }*/


}