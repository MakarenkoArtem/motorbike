package com.example.bike.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.magnifier
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BluetoothHeader(active: Boolean, func: (Boolean) -> Unit) {
    Column() {
        Row(
                modifier = Modifier
                    .wrapContentHeight() // Высота по содержимому
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold
            ), text = "Bluetooth"
            )
            Switch(checked = active,
                    onCheckedChange = {newState -> func(newState)},
                    colors = SwitchDefaults.colors(uncheckedThumbColor = Color.White,
                            checkedThumbColor = Color(30, 30, 253, 255)
                    )
            )
        }
        Divider(color = Color.Cyan, thickness = 2.dp, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LogViewer(logs: List<String>, modifier: Modifier = Modifier
    .fillMaxSize()
    .padding(16.dp)) {
    Column(modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(logs) {log ->
                Text(log, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@PreviewLightDark
@Composable
fun BluetoothHeaderViewer() {
    val active by remember {mutableStateOf(false)}
    BluetoothHeader(active = active) {_ ->}
}
