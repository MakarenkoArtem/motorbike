package com.example.bike.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

/*@Composable
fun MainScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BigParts()
        }
    }
}
*/
@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun BigParts() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PartWithCircle()
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun PartWithCircle() {
    var sliderPosition by remember { mutableStateOf(5f) }
    Text(text = sliderPosition.toString())
    Button(onClick = { /*TODO*/ }, content={Text("njk")} )
    Slider(
        value = sliderPosition,
        valueRange = 0f..100f,
        onValueChange = {
            println("11")
            sliderPosition = it + 1
        },
        modifier = Modifier
            .width(285.dp) // Высота слайдера
            .rotate(270f),
    )
}


/*
@Preview(backgroundColor = true)
@Composable
fun MainScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ConstraintLayout(
            //modifier = Modifier.fillMaxWidth().weight(0.35f)
        ) {
            val brightnessSlider, colorPicker, barSlider) = createRefs()

            Slider(
                value = 0f,
                onValueChange = {},
                valueRange = 0f..255f,
                modifier = Modifier
                    .height(285.dp)
                    .rotate(270f)
                    .constrainAs(brightnessSlider) {
                        start.linkTo(parent.start)
                        end.linkTo(colorPicker.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.color),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.7f)
                    .constrainAs(colorPicker) {
                        start.linkTo(brightnessSlider.end)
                        end.linkTo(barSlider.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )

            Slider(
                value = 0f,
                onValueChange = {},
                valueRange = 0f..100f,
                modifier = Modifier
                    .height(285.dp)
                    .rotate(270f)
                    .constrainAs(barSlider) {
                        start.linkTo(colorPicker.end)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )

        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { /* TODO */ }) {
                Text(text = "Connect")
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(painterResource(id = R.drawable.bt), contentDescription = null)
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(painterResource(id = R.drawable.mute), contentDescription = null)
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(painterResource(id = R.drawable.start), contentDescription = null)
            }
        }
    }
}
}}*/