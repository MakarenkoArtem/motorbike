package com.example.bike.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bike.R

@Preview(showBackground = true)
@Composable
fun EmptyDeviceListScreen() {
    MaterialTheme(
        colors = Colors(
            primary = Color.DarkGray,
            background = Color.DarkGray,
            error = Color.Red,
            isLight = false,
            onBackground = Color.Blue,
            onError = Color.Red,
            onPrimary = Color.White,
            onSecondary = Color.Gray,
            onSurface = Color.Black,
            primaryVariant = Color.DarkGray,
            secondaryVariant = Color.Black,
            secondary = Color.DarkGray,
            surface = Color.DarkGray
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
            ) {
                Header()
                Body()
            }
        }
    }
}

@Composable
fun Header() {
    var switchVal by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .wrapContentHeight() // Высота по содержимому
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            text = "Bluetooth"
        )
        Switch(
            checked = switchVal, onCheckedChange = { switchVal = !switchVal },
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = Color.White,
                checkedThumbColor = Color.Green
            )
        )
    }
    Divider(
        color = Color.White,
        thickness = 2.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun Body() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold, // Жирный текст
        ),
        text = stringResource(id = R.string.emptyDeviceList),
    )

}