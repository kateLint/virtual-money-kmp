package com.keren.virtualmoney

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keren.virtualmoney.ui.GameScreen
import com.keren.virtualmoney.ui.GameViewModel
import com.keren.virtualmoney.platform.CameraView

@Composable
fun App() {
    MaterialTheme {
        val viewModel: GameViewModel = viewModel { GameViewModel() }

        GameScreen(
            gameEngine = viewModel.gameEngine,
            cameraBackground = { CameraView() }
        )
    }
}