package com.example.bike.MainActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bike.R
import com.example.bike.factory.MainActivityViewModelFactory
import com.example.bike.model.CurrentColor
import com.example.bike.model.ScreenViewData
import com.example.bike.services.bluetooth.BluetoothClient
import com.example.bike.services.bluetooth.BluetoothService
import com.example.bike.services.bluetooth.BluetoothViewModel
import com.example.bike.ui.activity.ListDeviceDialog
import com.example.bike.ui.screens.MainScreen
import com.example.bike.ui.viewmodel.MainActivityViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity: AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var screenData: StateFlow<ScreenViewData>
    private lateinit var curColor: CurrentColor

    private lateinit var bluetoothViewModel: BluetoothViewModel

    val getResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if (result.resultCode == RESULT_OK) {
                val res = result.data?.getParcelableExtra<BluetoothClient>("SELECTED_DEVICE")
                    ?: return@registerForActivityResult
                viewModel.connect(res).onFailure {return@registerForActivityResult}
                Toast.makeText(applicationContext,
                        getString(R.string.connectionEstablished),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun openDialog() {
        if (viewModel.screenDataState.value.client != null) {
            viewModel.disconnect()
            return
        }
        lifecycleScope.launch {
            bluetoothViewModel.waitForService()
            if (bluetoothViewModel.checkBluetoothPermission().isFailure) {
                val permissions = bluetoothViewModel.getRequiredPermissions()
                Log.d("BikeBluetooth", permissions.toString())
                ActivityCompat.requestPermissions(this@MainActivity, permissions, 0)
            }
            val intent = Intent(applicationContext, ListDeviceDialog::class.java)
            getResult.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) //MODE_NIGHT_FOLLOW_SYSTEM

        val intent = Intent(getApplication(), BluetoothService::class.java)
        getApplication().startService(intent) // Запуск  блютуз сервиса
        bluetoothViewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
        viewModel = ViewModelProvider(this, MainActivityViewModelFactory(bluetoothViewModel)).get(
                MainActivityViewModel::class.java
        )
        screenData = viewModel.screenDataState
        curColor = screenData.value.curColor
        setContent {MainScreen(mainActivityViewModel = viewModel, openDialog = {openDialog()})}
    }
}