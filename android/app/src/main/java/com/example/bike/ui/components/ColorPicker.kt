package com.example.bike.ui.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bike.R


@Composable
fun ColorPicker(painter:Painter, move:(Int)->Unit, moveUp:()->Unit,
                imageHeightDp: MutableState<Dp> = mutableStateOf(0.dp), modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val _imageWidth = remember {mutableStateOf(0)}
    val _imageHeight = remember {mutableStateOf(0)}
    val resources = LocalContext.current.resources
    Image(painter = painter,
            contentDescription = null,
            modifier = modifier
                .aspectRatio(1f) // Картинка квадратная
                .onGloballyPositioned {coordinates -> // Получаем высоту картинки и сохраняем её
                    imageHeightDp.value = with(density) {coordinates.size.height.toDp()}
                    _imageWidth.value = with(density) {coordinates.size.width}
                    _imageHeight.value = with(density) {coordinates.size.height}
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope { // Ждем события касания и следим за движением
                        while (true) {
                            val event = awaitPointerEvent()

                            event.changes.forEach {pointerInputChange ->
                                if (pointerInputChange.isConsumed) return@forEach

                                val x = pointerInputChange.position.x
                                val y = pointerInputChange.position.y
                                val normalizedX = x / _imageWidth.value
                                val normalizedY = y / _imageHeight.value
                                val bitmap =
                                    BitmapFactory.decodeResource(resources, R.drawable.color
                                    )
                                try {
                                val pixel = bitmap.getPixel((normalizedX * bitmap.width).toInt(),
                                        (normalizedY * bitmap.height).toInt()
                                )
                                move(pixel)
                                } catch (e: IllegalArgumentException) {
                                }
                                Log.d("BikeBluetooth", pointerInputChange.toString())
                            }
                            if (event.changes.all {it.changedToUp()}) { // Палец был убран, all потому что касаться экрана может скразу несколько пальцев
                                moveUp()
                                Log.d("BikeBluetooth", "Finger released")
                            }
                        }
                    }
                })
}
