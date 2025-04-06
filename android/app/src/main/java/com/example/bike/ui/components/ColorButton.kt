package com.example.bike.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorButton(color: Color = Color.DarkGray,
                enabled:Boolean=true,
                changeCurrentColorButton: () -> Unit,
                changeActiveStatus: () -> Unit) {
    val bgColor=color.copy(alpha = if (enabled){1f}else{0.5f})
    val borderColor=Color.White.copy(alpha = if (enabled){1f}else{0.5f})
    /*Button(onClick = {},
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(backgroundColor = bgColor),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(3.dp, Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .height(42.dp)
                .heightIn(max = 42.dp)
                .combinedClickable(onClick = changeCurrentColorButton,
                        onLongClick = changeActiveStatus
                )
    ) {
        Text(text = "")
    }*/
    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .height(42.dp)
                .background(bgColor, shape = RoundedCornerShape(20.dp))
                .border(BorderStroke(3.dp, borderColor), shape = RoundedCornerShape(20.dp))
                .combinedClickable(
                        onClick = changeCurrentColorButton,
                        onLongClick = changeActiveStatus
                )
    ) {}
}