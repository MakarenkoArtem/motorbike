package com.example.bike.ui.screens

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bike.data.repository.BluetoothRepository
import com.example.bike.model.ScreenViewData
import com.example.bike.ui.components.ControlButtonFragment
import com.example.bike.ui.components.NumberPickersFragment
import com.example.bike.ui.components.PickerAndSliderFragment
import com.example.bike.ui.components.SectionWithRegimsFragment
import com.example.bike.ui.model.MainScreenActionsModel
import com.example.bike.ui.viewmodel.MainActivityViewModel


@Composable
fun MainScreen(
    mainActivityViewModel: MainActivityViewModel,
    openDialog: () -> Unit
) {
    val screenState by mainActivityViewModel.screenDataState.collectAsState()
    val actionsModel = MainScreenActionsModel(mainActivityViewModel)
    MainScreenContent(screenState, actionsModel, openDialog)
}

@Composable
fun MainScreenContent(
    screenState: ScreenViewData,
    actionsModel: MainScreenActionsModel,
    openDialog: () -> Unit
) {
    MaterialTheme(
        colors = darkColors()
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PickerAndSliderFragment(screenState, actionsModel)
                NumberPickersFragment(
                    screenState.curColor,
                    actionsModel,
                    modifier = Modifier.weight(1f, fill = true)
                )
                SectionWithRegimsFragment(screenState, actionsModel)
                ControlButtonFragment(
                    screenState,
                    actionsModel,
                    openDialog,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenContentPreview() {
    val viewModel by remember {mutableStateOf(MainActivityViewModel(BluetoothRepository(Activity())))}
    MainScreen(viewModel, {})
}/*
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenContentPreview() {
    val brightness = remember {mutableStateOf(100f)}
    val bar = remember {mutableStateOf(200f)}
    val (r, g, b) = remember {listOf(mutableStateOf(200), mutableStateOf(200), mutableStateOf(200))}

    val (indexType, indexMode) = remember {List(2, {mutableStateOf(0)})}
    val (gradient, HSV, movement, synchrony) = remember {List(4, {mutableStateOf(false)})}
    MainScreenContent(
            brightness.value,
            bar.value,
            {value -> brightness.value = value},
            {value -> bar.value = value},
            r.value,
            g.value,
            b.value,
            {value -> r.value = value},
            {value -> g.value = value},
            {value -> b.value = value},
            listOf(Color.Red, Color.White, Color.Blue),
            ColorButtonEvent(-1, {_ ->}, {_ ->}),
            listOf("Основной", "Цвето-\nмузыка", "Цв.муз\n(частоты)", "Стробоскоп"),
            indexType.value,
            {value -> indexType.value = value},
            listOf("База", "Мерцание"),
            indexMode.value,
            {value -> indexMode.value = value},
            gradient.value,
            {value -> gradient.value = value},
            HSV.value,
            {value -> HSV.value = value},
            movement.value,
            {value -> movement.value = value},
            synchrony.value,
            {value -> synchrony.value = value},
    )
}*/