package com.example.bike

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView

class ListDeviceDialog: Activity() {

    override fun onCreate(savedInstanceState: Bundle?,){
        super.onCreate(savedInstanceState)
        super.onCreateDescription()
        setContentView(R.layout.list_devices_dialog)
        setFinishOnTouchOutside(true)//закрытие активити когда пользователь нажимает за переделы окна
        val list=findViewById<ListView>(R.id.listDevices)
        val devices=intent.getStringArrayListExtra("DEVICE_NAMES")?: emptyList()
        list.adapter= ArrayAdapter(this, android.R.layout.simple_list_item_1, devices)
        list.setOnItemClickListener{parent, view, position, id->
            val selectedItem=devices[position]
            val resultIntent=click(selectedItem)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
fun click(device:String):Intent{
    Log.d("myLogs",device)
    val resultIntent = Intent()
    resultIntent.putExtra("SELECTED_DEVICE", device)
    return resultIntent
}