package com.example.bike.ui.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.bike.datasource.remote.model.BluetoothData
import com.example.bike.model.ScreenViewData
import com.example.bike.ui.model.ColorButtonViewData

fun ScreenViewData.updateByBluetoothData(data: BluetoothData): ScreenViewData = this.copy(
    colors = colors.mapIndexed {ind, value ->
        if(ind >= data.colors.size || data.colors[ind] == Color.Black.toArgb()){
            ColorButtonViewData(enabled = false)
        }else{
            ColorButtonViewData(color=data.colors[ind])
        }
    },
    type = data.type,
    mode = data.mode,
    brightness = data.brightness,
    frequency = data.frequency,
    /*ignition = data.ignition,
    amplifier = data.amplifier,
    audioBT = data.audioBT,*/
    hsv = data.hsv,
    gradient = data.gradient,
    movement = data.movement,
    synchrony = data.synchrony
)
