package com.example.bike.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class ColorButtonViewData(
        val color:Int= Color.DarkGray.toArgb(),
        val enabled:Boolean=true
)