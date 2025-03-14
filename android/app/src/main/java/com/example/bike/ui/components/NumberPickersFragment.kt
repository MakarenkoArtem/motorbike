package com.example.bike.ui.components

import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.bike.R
import com.example.bike.model.CurrentColor
import com.example.bike.ui.model.MainScreenActionsModel


@Composable
fun NumberPickersFragment(currentColor: CurrentColor, actionsModel: MainScreenActionsModel, modifier: Modifier=Modifier) {
    Row(modifier = modifier
        //.height(110.dp)
        .heightIn(max=120.dp)
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CustomNumberPicker(currentColor.color.red, actionsModel.updateRed)
        CustomNumberPicker(currentColor.color.green, actionsModel.updateGreen)
        CustomNumberPicker(currentColor.color.blue, actionsModel.updateBlue)
    }
}

@Composable
fun CustomNumberPicker(value: Int = 0, updateValue: (Int) -> Unit) {
    val pickerValue = remember {mutableStateOf(value)}
    AndroidView(modifier = Modifier, factory = {context ->
        NumberPicker(context).apply {
            Color.Red
            descendantFocusability =
                NumberPicker.FOCUS_BLOCK_DESCENDANTS            // Блокируем ввод с клавиатуры
            setOnValueChangedListener {numberPicker, oldVal, newVal ->
                val correctVal = stepEvent(oldVal, newVal)
                if (correctVal != pickerValue.value) {
                    pickerValue.value = correctVal
                    updateValue(correctVal)
                }
            }
            this.value = value
            minValue = 0
            maxValue = 255
        }
    }, update = {numberPicker ->
        if (numberPicker.value != value) {
            numberPicker.value = value
            pickerValue.value = value
        }
    })
}/*
@PreviewLightDark
@Composable
fun NumberPickersFragmentPreview() {
    val (r, g, b) = remember {
        listOf(mutableStateOf(45), mutableStateOf(145), mutableStateOf(245))
    }
    NumberPickersFragment(r = r.value,
            g = g.value,
            b = b.value,
            updateRed = {value -> r.value = value},
            updateGreen = {value -> g.value = value},
            updateBlue = {value -> b.value = value})
}*/


private fun stepEvent(oldVal: Int,
                      newVal: Int,
                      step: Int = 5,
                      maxValue: Int = 255,
                      minValue: Int = 0): Int {
    var value = newVal
    var stepPicker = step
    if (stepPicker < 1 || stepPicker > 255) {
        stepPicker = 1
    }
    if (newVal > maxValue - stepPicker && oldVal < minValue + stepPicker) {
        value = maxValue
    } else if (oldVal > maxValue - stepPicker && newVal < minValue + stepPicker) {
        value = minValue
    } else {
        if (newVal > oldVal) {
            value += stepPicker
        } else {
            value -= stepPicker
        }
    }
    value = value / stepPicker * stepPicker
    return value
}


