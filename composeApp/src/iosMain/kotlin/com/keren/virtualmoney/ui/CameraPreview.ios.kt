package com.keren.virtualmoney.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * iOS implementation of camera preview (stub - not implemented).
 * Shows black background as placeholder.
 */
@Composable
actual fun CameraPreview(modifier: Modifier) {
    Box(
        modifier = modifier.background(Color.Black)
    )
}
