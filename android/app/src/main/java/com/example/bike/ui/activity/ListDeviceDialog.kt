package com.example.bike.ui.activity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.bike.ui.screens.DeviceListScreen
import com.example.bike.ui.viewmodel.ListDeviceDialogViewModel
import org.koin.androidx.compose.koinViewModel

class ListDeviceDialog: ComponentActivity() {
    private lateinit var viewModel: ListDeviceDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.onCreateDescription()
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setFinishOnTouchOutside(true) //закрытие активити когда пользователь нажимает за переделы окна
        /* Заменил провайдера на koinViewModel
           bluetoothViewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
                viewModel = ViewModelProvider(this, ListDeviceDialogViewModelFactory(bluetoothViewModel)
                ).get(ListDeviceDialogViewModel::class.java)*/

        val enableBtLauncher =
            this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("ListDeviceDialog", "RESULT_OK")
                    viewModel.switchEvent(viewModel.getStatus().isSuccess)
                } else { // Пользователь отклонил запрос на включение Bluetooth
                    Log.d("ListDeviceDialog", "RESULT_FAIL")
                    viewModel.switchEvent(false)
                }
            }

        setContent {
            viewModel = koinViewModel()
            LaunchedEffect(Unit) {
                viewModel.screenDataState.collect {device ->
                    if (device.connectedDevice != null) {
                        val intent = Intent().putExtra("SELECTED_DEVICE", device.connectedDevice!!)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
            val screenState by viewModel.screenDataState.collectAsState()

            DeviceListScreen(screenState = screenState, switchEvent = {newStatus ->
                viewModel.bluetoothRepository.checkBluetoothPermission()
                if (newStatus && viewModel.bluetoothRepository.checkBluetoothPermission().isSuccess) { //                    bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBtLauncher.launch(enableBtIntent)
                } else {
                    viewModel.switchEvent(false)
                }
            }, selectionFunc = {device -> viewModel.connect(device)})
        }
    }

    override fun onDestroy() {
        viewModel.disconnect()
        super.onDestroy()
    }
}