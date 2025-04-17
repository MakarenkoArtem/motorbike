package com.example.bike.model

import android.graphics.Color
import com.example.bike.ui.model.ColorButtonViewData

data class ScreenViewData(
    val colors: List<ColorButtonViewData> = List(5) { ColorButtonViewData(Color.rgb(50, 50, 50), true) },//{ if (it % 2 == 0) Color.BLACK else Color.WHITE }
    val type: Int = 0,
    val format: Int = 0,
    val frequency: Float = 0f,
    val brightness: Float = 0f,
    val ignition: Boolean = true,
    val amplifier: Boolean = false,
    val audioBT: Boolean = true,
    val mode:Int=0,
    val hsv:Boolean=false,
    val gradient:Boolean=true,
    val movement:Boolean=true,
    val synchrony: Boolean = true,
    val curColor: CurrentColor = CurrentColor(),
    val titlesMode:List<String> = listOf(),
    val connectButtonTitle:String = "Connect"
)