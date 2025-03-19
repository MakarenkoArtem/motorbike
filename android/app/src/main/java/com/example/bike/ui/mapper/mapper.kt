package com.example.bike.ui.mapper

import com.example.bike.datasource.remote.model.BluetoothData
import com.example.bike.model.ScreenViewData

fun ScreenViewData.updateByBluetoothData(data: BluetoothData): ScreenViewData = this.copy(
    colors = colors.mapIndexed {ind, value ->
        if (value.enabled) {
            value.copy(color = data.colors[ind])
        }else{
            value
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
