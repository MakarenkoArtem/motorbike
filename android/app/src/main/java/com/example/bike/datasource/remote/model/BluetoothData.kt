package com.example.bike.datasource.remote.model

import android.graphics.Color

data class BluetoothData(
    val active:Boolean= false,
    val colors: List<Int> = List(5) {Color.BLACK},
    val type: Int = 0,
    val mode: Int = 0,
    val frequency: Float = 0f,
    val brightness: Float = 0f,
    val ignition: Boolean = true,
    val amplifier: Boolean = false,
    val audioBT: Boolean = true,
    val hsv: Boolean = false,
    val gradient: Boolean = true,
    val movement: Boolean = true,
    val synchrony: Boolean = true,
    val connected:Boolean = true
)