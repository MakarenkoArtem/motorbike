package com.example.bike.di

import com.example.bike.services.bluetooth.BluetoothService
import org.koin.core.qualifier.named
import org.koin.dsl.module

val datasourceModule = module {
    single<BluetoothService>(named(name = "bluetoothService")) {
        BluetoothService()
    }
}