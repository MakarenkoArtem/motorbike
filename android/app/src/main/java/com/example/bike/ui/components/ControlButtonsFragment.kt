package com.example.bike.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bike.R
import com.example.bike.model.ScreenViewData
import com.example.bike.ui.model.MainScreenActionsModel

@Composable
fun ControlButtonFragment(screenState: ScreenViewData,
                          actionModel: MainScreenActionsModel,
                          openDialog: () -> Unit,
                          modifier: Modifier=Modifier) {
    Row(modifier = modifier
        .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = actionModel.setIgnition) {
            Icon(painter = painterResource(if (screenState.ignition) {
                R.drawable.stop
            } else {
                R.drawable.start
            }
            ),
                    contentDescription = "Bike Off",
                    tint = Color.Unspecified, // Оставляет оригинальные цвета
                    modifier = Modifier.size(60.dp)
            )
        }
        ConnectButton(screenState.connectButtonTitle, openDialog)
        IconButton(onClick = actionModel.setAudioBTStatus) {
            Icon(painter = painterResource(if (screenState.audioBT) {
                R.drawable.aux_pic
            } else {
                R.drawable.bt
            }
            ),
                    contentDescription = "Audio BT",
                    tint = Color.Unspecified, // Оставляет оригинальные цвета
                    modifier = Modifier.size(60.dp)
            )
        }

        IconButton(onClick = actionModel.setAmplifierStatus) {
            Icon(painter = painterResource(if (screenState.amplifier) {
                R.drawable.mute
            } else {
                R.drawable.sound
            }
            ),
                    contentDescription = "Amplifier",
                    tint = Color.Unspecified, // Оставляет оригинальные цвета
                    modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
fun ConnectButton(title: String, onClick: () -> Unit) {
    Button(onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(51, 97, 255, 255), // Цвет фона кнопки
                    contentColor = Color.White    // Цвет текста и иконок
            ),
            modifier = Modifier
                .width(138.dp)
                .height(64.dp),
            shape = RoundedCornerShape(32.dp) // Скругление углов
    ) {
        Text(text = title, color=Color.White, fontSize = 16.sp)
    }
}