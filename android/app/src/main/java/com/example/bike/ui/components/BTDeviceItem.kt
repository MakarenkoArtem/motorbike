package com.example.bike.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bike.R
import com.example.bike.domain.model.Device
import com.example.bike.domain.model.DeviceStatus

@Composable
fun BTDeviceItem(
    device: Device,
    selectionFunc: (Device) -> (Unit)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {selectionFunc(device)},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .wrapContentHeight(Alignment.CenterVertically), //.padding(vertical = 8.dp)
            textAlign = TextAlign.End, style = TextStyle(
                fontSize = 18.sp
            ), text = device.name
        )
        if (device.status == DeviceStatus.DISCOVERING) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White
            )
        } else if (device.status == DeviceStatus.CONNECTED) {
            Icon(
                painter = painterResource(R.drawable.checkmark),
                contentDescription = "checkMark",
                tint = Color.Unspecified, // Оставляет оригинальные цвета
                modifier = Modifier.size(22.dp)
            )
        }
    }
}