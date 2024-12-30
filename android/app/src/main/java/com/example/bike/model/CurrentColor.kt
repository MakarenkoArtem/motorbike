package com.example.bike.model

import android.graphics.Color
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

class CurrentColor(
    var color: Int = Color.argb(0, 0, 0, 0),
    var activButton: Button? = null,
    var redPicker: NumberPicker? = null,
    var greenPicker: NumberPicker? = null,
    var bluePicker: NumberPicker? = null,
    var stepPicker: Int=5
) {
    fun setPickers(
        pickers: List<NumberPicker>,
        onTouchLis: OnTouchListener,
        onStepEvent:OnValueChangeListener,
        minValue: Int = 0,
        maxValue: Int = 255
    ) = runCatching {
        if (pickers.size != 3) {
            throw IllegalStateException("Incorrect amount NumberPickers")
        }
        val pics = mutableListOf(redPicker, greenPicker, bluePicker)
        for (i in 0..<3) {
            pics[i] = pickers[i]
            pics[i]?.minValue = minValue
            pics[i]?.maxValue = maxValue
            pics[i]?.maxValue = maxValue
            pics[i]?.setOnTouchListener(onTouchLis)
            pics[i]?.setOnValueChangedListener(onStepEvent)
        }
        redPicker = pickers[0]
        greenPicker = pickers[1]
        bluePicker = pickers[2]
    }

    fun updateByPickers() = runCatching {
        color = Color.rgb(
            redPicker!!.value,
            greenPicker!!.value,
            bluePicker!!.value
        )
    }

    fun updatePickers() = runCatching {
        redPicker?.value = color.red
        greenPicker?.value = color.green
        bluePicker?.value = color.blue
    }
}