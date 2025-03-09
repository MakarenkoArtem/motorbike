package com.example.bike.model

import android.graphics.Color
import com.example.bike.services.bluetooth.BluetoothClient

data class ScreenViewData(
    val colors: List<Int> = List(6) { Color.rgb(50, 50, 50) },//{ if (it % 2 == 0) Color.BLACK else Color.WHITE }
    var type: Int = 0,
    var format: Int = 0,
    var frequency: Int = 0,
    var brightness: Int = 0,
    var ignition: Boolean = true,
    var amplifier: Boolean = false,
    var audioBT: Boolean = true,
    var client: BluetoothClient? = null,
    var mode:Int=1,
    var hsv:Boolean=false,
    var gradient:Boolean=true,
    var movement:Boolean=true,
    var synchrony: Boolean = true,
    var curColor: CurrentColor
)