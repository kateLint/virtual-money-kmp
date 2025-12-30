package com.keren.virtualmoney

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keren.virtualmoney.ar.camera.CameraProviderFactory
import com.keren.virtualmoney.ui.GameScreen
import com.keren.virtualmoney.ui.GameViewModel
import com.keren.virtualmoney.platform.CameraView

@Composable
fun App() {
    MaterialTheme {
        val viewModel: GameViewModel = viewModel { GameViewModel() }

        // Create CameraProvider for AR mode
        val cameraProvider = remember { CameraProviderFactory.create() }
        val isARMode by viewModel.isARMode.collectAsState()

        // Start/stop AR session based on mode
        LaunchedEffect(isARMode) {
            if (isARMode) {
                cameraProvider.startSession()
            } else {
                cameraProvider.stopSession()
            }
        }

        // Clean up on disposal
        DisposableEffect(Unit) {
            onDispose {
                cameraProvider.stopSession()
            }
        }

        GameScreen(
            gameEngine = viewModel.gameEngine,
            cameraBackground = { CameraView() },
            cameraProvider = cameraProvider,
            isARMode = isARMode,
            onToggleARMode = { viewModel.toggleARMode() }
        )
    }
}