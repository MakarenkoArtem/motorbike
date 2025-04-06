package com.example.bike

import android.app.Application
import com.example.bike.di.datasourceModule
import com.example.bike.di.domainModule
import com.example.bike.di.viewmodelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BikeApp:Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@BikeApp)
            modules(datasourceModule, domainModule, viewmodelModule)
        }
    }
}