package com.example.bike.ui.components

import android.widget.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.RadioButton
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColor
import com.example.bike.R
import com.example.bike.model.ScreenViewData
import com.example.bike.ui.model.MainScreenActionsModel

@Composable
fun SectionWithRegimsFragment(screenState: ScreenViewData, actionsModel: MainScreenActionsModel) {
    var centerColWidth by remember {mutableStateOf(0.dp)}
    var rightColWidth by remember {mutableStateOf(0.dp)}
    val density = LocalDensity.current
    Row(modifier = Modifier
        .wrapContentHeight()) {
        Row(modifier = Modifier) {
            Column(modifier = Modifier
                .width(75.dp)
                .wrapContentHeight()
                .padding(vertical = 0.dp)
            ) {
                screenState.colors.forEachIndexed {index, color ->
                    ColorButton(Color(color.color), color.enabled, { -> actionsModel.changeCurrentColorButton(index)},
                            {actionsModel.changeActiveStatus(index)})
                }
            }
        }
        Column {
            Row {
                RadioGroupCustom(listOf("Основной", "Цветомузыка", "Цв.муз(частоты)", "Стробоскоп"),
                        screenState.type,
                        actionsModel.setTypeColors,
                        modifier = Modifier
                            .weight(1f, true)
                            .onGloballyPositioned {coordinates ->
                                centerColWidth = with(density) {coordinates.size.width.toDp()}
                            })
                RadioGroupCustom(screenState.titlesMode,
                        screenState.mode,
                        actionsModel.setModeColors,
                        modifier = Modifier
                            .weight(1f, true)
                            .onGloballyPositioned {coordinates ->
                                rightColWidth = with(density) {coordinates.size.width.toDp()}
                            })
            }
            Divider(color = Color.Cyan,
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
            )
            Row() {
                GradientHSVButtons(screenState.gradient,
                        actionsModel.setGradientStatus,
                        screenState.hsv,
                        actionsModel.setHSVStatus,
                        modifier = Modifier.width(centerColWidth)
                )
                MovementSynchronyButtons(screenState.movement,
                        actionsModel.setMovementStatus,
                        screenState.synchrony,
                        actionsModel.setSynchronyStatus,
                        modifier = Modifier.width(rightColWidth)
                )
            }
        }
    }
}


@Composable
fun GradientHSVButtons(gradient: Boolean = true,
                       updateGradient: (Boolean) -> Unit = {_ ->},
                       HSV: Boolean = false,
                       updateHSV: (Boolean) -> Unit = {_ ->},
                       modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .padding(0.dp)
        .widthIn(max = 148.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start, // Выравниваем элементы в начале
                modifier = Modifier
                    .clickable {updateGradient(!gradient)}
                    .padding(0.dp)
                    .fillMaxWidth()) {
            RadioButton(selected = gradient, onClick = {}, modifier = Modifier.padding(0.dp)
            )
            Text(text = stringResource(R.string.gradient),
                    color = Color.White,
                    modifier = Modifier.padding(0.dp)
            )

        }
        Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start, // Выравниваем элементы в начале
                modifier = Modifier
                    .clickable {updateHSV(!HSV)}
                    .padding(0.dp)
                    .fillMaxWidth()) {
            RadioButton(selected = HSV, onClick = {}, modifier = Modifier.padding(0.dp)
            )
            Text(text = stringResource(R.string.hsv),
                    color = Color.White,
                    modifier = Modifier.padding(0.dp)
            )

        }

    }
}

@Composable
fun MovementSynchronyButtons(movement: Boolean = true,
                             updateMovement: (Boolean) -> Unit = {_ ->},
                             synchrony: Boolean = true,
                             updateSynchrony: (Boolean) -> Unit = {_ ->},
                             modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .padding(0.dp)
        .widthIn(min = 184.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .clickable {updateMovement(!movement)}
                    .padding(0.dp)
                    .fillMaxWidth()) {
            RadioButton(selected = movement, onClick = {}, modifier = Modifier.padding(0.dp)
            )
            Text(text = stringResource(R.string.movement),
                    color = Color.White,
                    modifier = Modifier.padding(0.dp)
            )

        }
        Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .clickable {updateSynchrony(!synchrony)}
                    .padding(0.dp)
                    .fillMaxWidth()) {
            RadioButton(selected = synchrony, onClick = {}, modifier = Modifier.padding(0.dp)
            )
            Text(text = stringResource(R.string.synchrony),
                    color = Color.White,
                    modifier = Modifier.padding(0.dp)
            )

        }

    }
}


@Composable
fun RadioGroupCustom(titles: List<String>,
                     indexSelect: Int = 0,
                     onSelected: (Int) -> Unit,
                     modifier: Modifier = Modifier) {
    var selectedOption = indexSelect
    if (indexSelect >= titles.size || selectedOption < 0) {
        selectedOption = 0
    }
    Column(modifier = modifier.padding(0.dp)
    ) {
        titles.forEachIndexed {index, title ->
            Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .clickable {onSelected(index)}
                        .padding(0.dp)
                        .fillMaxWidth(1f)) {
                RadioButton(selected = selectedOption == index,
                        onClick = {onSelected(index)},
                        modifier = Modifier.padding(0.dp)
                )
                Text(text = title, color = Color.White, modifier = Modifier.padding(0.dp)
                )

            }
        }
    }

}

/*
@PreviewLightDark
@Composable
fun SectionWithRegimsFragmentPerview() {
    val (indexType, indexMode) = remember {List(2, {mutableStateOf(0)})}
    val (gradient, HSV, movement, synchrony) = remember {List(4, {mutableStateOf(false)})}
    SectionWithRegimsFragment(
            listOf(Color.Red, Color.White, Color.Blue),
            ColorButtonEvent(-1, {_ ->}, {_ ->}),
            listOf("Основной", "Цветомузыка", "Цв.муз(частоты)", "Стробоскоп"),
            indexType.value,
            {value -> indexType.value = value},
            listOf("База", "Мерцание"),
            indexMode.value,
            {value -> indexMode.value = value},
            gradient.value,
            {value -> gradient.value = value},
            HSV.value,
            {value -> HSV.value = value},
            movement.value,
            {value -> movement.value = value},
            synchrony.value,
            {value -> synchrony.value = value},
    )
}


@PreviewLightDark
@Composable
fun RadioGroupCustomPreview() {
    val indexSelect = remember {
        mutableStateOf(0)
    }
    RadioGroupCustom(listOf("Основной", "Цветомузыка", "Цв.муз(частоты)", "Стробоскоп"),
            indexSelect.value,
            {value -> indexSelect.value = value})
}*/