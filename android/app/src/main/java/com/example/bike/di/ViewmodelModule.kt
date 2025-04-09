package com.example.bike.di

import com.example.bike.domain.repository.IBluetoothRepository
import com.example.bike.ui.viewmodel.ListDeviceDialogViewModel
import com.example.bike.ui.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewmodelModule = module {
    viewModel {MainActivityViewModel(get<IBluetoothRepository>())}
    viewModel {ListDeviceDialogViewModel(get<IBluetoothRepository>())}

}