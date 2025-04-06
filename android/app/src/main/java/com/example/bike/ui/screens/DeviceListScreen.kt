package com.example.bike.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bike.R
import com.example.bike.domain.model.Device
import com.example.bike.model.ListDeviceViewData
import com.example.bike.ui.components.BTDeviceItem
import com.example.bike.ui.components.BluetoothHeaderFragment

@Composable
fun DeviceListScreen(
    screenState: ListDeviceViewData,
    switchEvent: (Boolean) -> Unit,
    selectionFunc: (Device) -> (Unit)
) {
    MaterialTheme(
        colors = Colors(
            primary = Color.DarkGray,
            background = Color.DarkGray,
            onBackground = Color.Blue,
            error = Color.Red,
            isLight = false,
            onError = Color.Red,
            onPrimary = Color.White,
            secondary = Color.DarkGray,
            onSecondary = Color.Gray,
            primaryVariant = Color.DarkGray,
            secondaryVariant = Color.Black,
            surface = Color(40, 40, 40, 240),
            onSurface = Color.White,
        )
    ) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                BluetoothHeaderFragment(screenState.bluetoothStatus) {newState ->
                    switchEvent(
                        newState
                    )
                }
                if (screenState.bluetoothStatus) {
                    if (screenState.devices.isEmpty()) {
                        EmptyBody(stringResource(id = R.string.emptyDeviceList))
                    } else {
                        Body(screenState.devices, selectionFunc)
                    }
                } else {
                    EmptyBody(stringResource(id = R.string.bluetoothLocked))
                }

            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Body(
    devices: List<Device>,
    selectionFunc: (Device) -> (Unit)
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        items(devices) {device -> BTDeviceItem(device, selectionFunc)}
    }

}

@Composable
fun EmptyBody(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp), textAlign = TextAlign.Center, style = TextStyle(
            fontSize = 18.sp, fontWeight = FontWeight.Bold
        ), text = text
    )

}/*
@PreviewLightDark
@Preview(showBackground = true)
@Composable
fun DeviceListScreenPreview() {
    val active = MutableStateFlow(false)
    val devices = MutableStateFlow(emptyList<Device>())
    DeviceListScreen(switchEvent = {newStatus -> active.value = newStatus},
        switchActiveFlow = active,
        devicesFlow = devices,
        {_ ->})
}
*/