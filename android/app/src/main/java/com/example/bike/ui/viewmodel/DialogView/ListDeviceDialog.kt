package com.example.bi

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bike.BT.BTClient
import com.example.bike.R
import com.example.bike.ui.screens.EmptyDeviceListScreen
import com.example.bike.ui.viewmodel.DialogView.ListDeviceDialogViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListDeviceDialog : AppCompatActivity() {
    lateinit var viewModel: ListDeviceDialogViewModel
    var btSwitch: Switch?=null
    var list: ListView? = null
    //private lateinit var btLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.onCreateDescription()
        viewModel = ListDeviceDialogViewModel(context = applicationContext, activity = this)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)// убираем заголовок
        setFinishOnTouchOutside(true)//закрытие активити когда пользователь нажимает за переделы окна
        /*setContentView(R.layout.empty_list_devices_dialog)
        lifecycleScope.launch {
            viewModel.devices.collect { devices ->
                Log.d("BikeBluetooth", "collect $devices")
                updateList(devices)
            }
        }*/
        //val devices by viewModel.listDevicesState.collect(r->)
        /*val devices =
            intent.getParcelableArrayListExtra<BluetoothDevice>("DEVICE_NAMES") ?: emptyList()*/
        val devices = viewModel.devices
        f(devices)
        //registerBtLauncher()
        if (viewModel.btService.btAdapter != null) {
            btSwitch?.isChecked = viewModel.btService.btAdapter.isEnabled
        }else{
            btSwitch?.isChecked=false
        }
    }

    fun f(devices:List<BluetoothDevice>){
        if (devices.isEmpty()) {
            setContent{
                EmptyDeviceListScreen()
            }
        } else {
            setContentView(R.layout.list_devices_dialog)
            list = findViewById<ListView>(R.id.listDevices)
            list?.adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, devices.map { it ->
                    try {
                        it.name
                    } catch (e: SecurityException) {
                    }
                })

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
        }
    }

    //private fun registerBtLauncher(){
        var btLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            if (viewModel.btService.btAdapter != null) {
                btSwitch?.isChecked = viewModel.btService.btAdapter.isEnabled
                onCreate(Bundle.EMPTY)
            }
        }
    //}
/*
    fun updateList(devices: List<BluetoothDevice>) {
        try {
            if(devices.isEmpty()){
                list=null
                setContentView(R.layout.empty_list_devices_dialog)
                return
            }
            setContentView(R.layout.list_devices_dialog)
            list = findViewById<ListView>(R.id.listDevices)
            list?.adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, devices.map { it ->
                    it.name
                })
            list!!.setOnItemClickListener { parent, view, position, id ->
                val selectedItem = devices[position]
                Log.d("myLog", devices.toString())
                val resultIntent = click(selectedItem)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        } catch (e: SecurityException) {
        }
    }*/
}

fun click(device: BTClient): Intent {
    Log.d("BikeBluetooth", device.device.toString())
    val resultIntent = Intent()
    resultIntent.putExtra("SELECTED_DEVICE", device.device)
    return resultIntent
}