package com.example.bike.ui.components

import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.ViewModelProvider
import com.example.bike.R
import com.example.bike.model.ScreenViewData
import com.example.bike.services.bluetooth.BluetoothViewModel
import com.example.bike.ui.model.MainScreenActionsModel
import com.example.bike.ui.viewmodel.MainActivityViewModel


@Composable
fun PickerAndSliderFragment(screenState: ScreenViewData, actionsModel: MainScreenActionsModel) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (brightSlider, colorPicker, barSlider) = createRefs()
        val imageHeight = remember {mutableStateOf(0.dp)}
        val sliderColors = SliderDefaults.colors(
                thumbColor = Color(167, 184, 255, 255),
                activeTrackColor = Color(116, 135, 255, 255),
        )
        VerticalSlider(value = screenState.brightness,
                onValueChange = actionsModel.setBrightness,
                valueRange = 0f..255f,
                colors = sliderColors,
                modifier = Modifier
                    .width(imageHeight.value)
                    .constrainAs(brightSlider) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(colorPicker.start)
                        bottom.linkTo(parent.bottom)
                    })
        ColorPicker(painter = painterResource(id = R.drawable.color), move=actionsModel.setCurrentColor, moveUp = actionsModel.updateColors, imageHeightDp = imageHeight, modifier = Modifier.constrainAs(colorPicker) {
            width = Dimension.percent(0.7f)
            start.linkTo(brightSlider.end)
            end.linkTo(barSlider.start)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        })

        VerticalSlider(value = screenState.frequency,
                onValueChange = actionsModel.setFrequency,
                valueRange = 0f..255f,
                colors = sliderColors,
                modifier = Modifier
                    .width(imageHeight.value)
                    .constrainAs(barSlider) {
                        start.linkTo(colorPicker.end)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    })
    }
}

@PreviewLightDark
@Composable
fun PickerAndSliderFragmentPreview() {
    val state by remember {mutableStateOf(ScreenViewData())}
    val action = MainScreenActionsModel(MainActivityViewModel(BluetoothViewModel(Application())))
    PickerAndSliderFragment(state, action)

    /*val bright = remember {mutableStateOf(50f)}
    val frequency = remember {mutableStateOf(150f)}
    PickerAndSliderFragment(bright.value,
            frequency.value,
            {newVal -> bright.value = newVal},
            {newVal -> frequency.value = newVal},
            { })*/
}