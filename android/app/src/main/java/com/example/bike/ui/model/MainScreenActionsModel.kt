package com.example.bike.ui.model

import com.example.bike.ui.viewmodel.MainActivityViewModel

data class MainScreenActionsModel(private val viewModel: MainActivityViewModel) {
    val changeCurrentColorButton: (Int) -> Unit = viewModel::changeCurrentColorButton
    val changeActiveStatus: (Int) -> Unit = viewModel::changeActiveStatus
    val colorPickerSend = viewModel::colorPickerSend
    val setBrightness = viewModel::setBrightness
    val setFrequency = viewModel::setFrequency
    val setIgnition = viewModel::setIgnition
    val setTypeColors: (Int) -> Unit = viewModel::setType
    val setModeColors: (Int) -> Unit = viewModel::setMode
    val setHSVStatus = viewModel::setHSVStatus
    val setGradientStatus = viewModel::setGradientStatus
    val setMovementStatus = viewModel::setMovementStatus
    val setSynchronyStatus = viewModel::setSynchronyStatus
    val setAmplifierStatus: () -> Unit = viewModel::setAmplifierStatus
    val setAudioBTStatus: () -> Unit = viewModel::setAudioBTStatus
    val updateRed = viewModel::updateRed
    val updateGreen = viewModel::updateGreen
    val updateBlue = viewModel::updateBlue
    val checkConnection: () -> Result<Unit> = viewModel::checkConnection
    val updateColors = viewModel::updateColors

}