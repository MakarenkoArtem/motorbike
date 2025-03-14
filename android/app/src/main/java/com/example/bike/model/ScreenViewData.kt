package com.example.bike.model

import android.graphics.Color
import com.example.bike.services.bluetooth.BluetoothClient
import com.example.bike.ui.model.ColorButtonViewData

data class ScreenViewData(
    val colors: List<ColorButtonViewData> = List(5) { ColorButtonViewData(Color.rgb(50, 50, 50), true) },//{ if (it % 2 == 0) Color.BLACK else Color.WHITE }
    var type: Int = 0,
    var format: Int = 0,
    var frequency: Float = 0f,
    var brightness: Float = 0f,
    var ignition: Boolean = true,
    var amplifier: Boolean = false,
    var audioBT: Boolean = true,
    var client: BluetoothClient? = null,
    var mode:Int=0,
    var hsv:Boolean=false,
    var gradient:Boolean=true,
    var movement:Boolean=true,
    var synchrony: Boolean = true,
    val curColor: CurrentColor = CurrentColor(),
    val titlesMode:List<String> = listOf(),
    val connectButtonTitle:String = "Connect"
)