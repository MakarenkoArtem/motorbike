package com.example.bike.ui.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bike.services.bluetooth.BluetoothViewModel
import com.example.bike.ui.screens.DeviceListScreen
import com.example.bike.ui.viewmodel.ListDeviceDialogViewModel
import com.example.bike.factory.ListDeviceDialogViewModelFactory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListDeviceDialog: ComponentActivity() {
    private lateinit var viewModel: ListDeviceDialogViewModel
    private lateinit var bluetoothViewModel:BluetoothViewModel
    private var bluetoothDevices: StateFlow<List<BluetoothDevice>> =
        MutableStateFlow(emptyList<BluetoothDevice>())
    private val bluetoothActive = MutableStateFlow(false)
    val REQUEST_ENABLE_BT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.onCreateDescription()
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bluetoothViewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
        viewModel = ViewModelProvider(this, ListDeviceDialogViewModelFactory(bluetoothViewModel)
        ).get(ListDeviceDialogViewModel::class.java)

        setFinishOnTouchOutside(true) //закрытие активити когда пользователь нажимает за переделы окна

        lifecycleScope.launch {
            bluetoothViewModel.startBluetoothService()
            bluetoothViewModel.checkBluetoothPermission().onFailure {cancel()}
            bluetoothViewModel.getDevicesFlow().onSuccess {bluetoothDevices = it}
            setContent {
                DeviceListScreen(switchEvent = {newStatus ->
                    if (newStatus) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    }
                    bluetoothActive.value = newStatus
                },
                        switchActiveFlow = bluetoothActive,
                        devicesFlow = bluetoothDevices,
                        selectionFunc = {device ->
                            viewModel.connect(device).onSuccess {client ->
                                val intent = Intent().apply {
                                    putExtra("SELECTED_DEVICE", client)
                                }
                                setResult(RESULT_OK, intent)
                                finish()
                            }
                        })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            bluetoothViewModel.checkBluetoothAdapter()
            bluetoothViewModel.getDevicesFlow()
            bluetoothActive.value = resultCode == RESULT_OK
        }
    }/*
        list!!.setOnItemClickListener { parent, view, position, id ->
            CoroutineScope(Dispatchers.Default).launch {
                val selectedItem = devices[position]
                Log.d("BikeBluetooth", devices.toString())
                if (viewModel.connect(selectedItem).isFailure) {
                    Log.d("BikeBluetooth", getString(R.string.connectionNotEstablished))
                    viewModel.disconnect()
                    withContext(Dispatchers.Main) {
                        Log.d("BikeBluetooth", "1")
                        Toast.makeText(
                        applicationContext,
                        getString(R.string.connectionNotEstablished), Toast.LENGTH_SHORT
                    )
                        .show()
                        Log.d("BikeBluetooth", "2")
                    }
                    return@launch
                }
                val resultIntent = click(viewModel.device!!)
                setResult(Activity.RESULT_OK, resultIntent)
                viewModel.disconnect()
                finish()
            }
        }
    }*/
}
