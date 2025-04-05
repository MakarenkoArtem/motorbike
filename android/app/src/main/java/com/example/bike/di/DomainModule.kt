package com.example.bike.di

import com.example.bike.data.repository.BluetoothRepository
import com.example.bike.domain.repository.IBluetoothRepository
import org.koin.dsl.module

val domainModule = module {
    factory<IBluetoothRepository> {
        BluetoothRepository(
            context = get()
        )
    }
}