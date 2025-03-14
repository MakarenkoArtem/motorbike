package com.example.bike.model

import android.graphics.Color
import android.widget.Button
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

data class CurrentColor(var index:Int=-1,
    var color: Int = Color.argb(0, 0, 0, 0), var activeButton: Button? = null,/*var redPicker: NumberPicker? = null,
                   var greenPicker: NumberPicker? = null,
                   var bluePicker: NumberPicker? = null,*/
                   var stepPicker: Int = 5) {

    /*fun setPickers(pickers: List<NumberPicker>,
                   onTouchLis: OnTouchListener,
                   onStepEvent: OnValueChangeListener,
                   minValue: Int = 0,
                   maxValue: Int = 255) = runCatching {
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
    }*/

    fun updatePicker(value: Int, colorChar: Char): Result<Int> {
        if (!(value in 0..255)) {
            return Result.failure(Exception("Out of range"))
        }
        var (r, g, b) = listOf(color.red, color.green, color.blue)
        when (colorChar) {
            'r' -> {
                r = value
            }
            'g' -> {
                g = value
            }
            'b' -> {
                b = value
            }
        }
        color = Color.rgb(r, g, b)
        return Result.success(color)
    }/*
    fun updateByPickers() = runCatching {
        color = Color.rgb(redPicker!!.value, greenPicker!!.value, bluePicker!!.value
        )
    /

    fun updatePickers() = runCatching {
        redPicker?.value = color.red
        greenPicker?.value = color.green
        bluePicker?.value = color.blue
    }*/
}