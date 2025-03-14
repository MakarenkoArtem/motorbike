package com.example.bike.ui.model

import com.example.bike.ui.viewmodel.MainActivityViewModel

data class MainScreenActionsModel(private val viewModel: MainActivityViewModel) {
    val changeCurrentColorButton:(Int)->Unit = viewModel::changeCurrentColorButton
    val changeActiveStatus:(Int)->Unit = viewModel::changeActiveStatus
    val setCurrentColor = viewModel::setCurrentColor
    val setBrightness = viewModel::setBrightness
    val setFrequency = viewModel::setFrequency
    val setIgnition:()->Unit = viewModel::setIgnition
    val setTypeColors:(Int)->Unit = viewModel::setTypeColors
    val setModeColors:(Int)->Unit = viewModel::setModeColors
    val setHSVStatus = viewModel::setHSVStatus
    val setGradientStatus = viewModel::setGradientStatus
    val setMovementStatus = viewModel::setMovementStatus
    val setSynchronyStatus = viewModel::setSynchronyStatus
    val setAmplifierStatus:()->Unit = viewModel::setAmplifierStatus
    val setAudioBTStatus:()->Unit = viewModel::setAudioBTStatus
    val updateRed = viewModel::updateRed
    val updateGreen = viewModel::updateGreen
    val updateBlue = viewModel::updateBlue
    val connect:()->Unit=viewModel::connect
    val updateColors=viewModel::updateColors

}