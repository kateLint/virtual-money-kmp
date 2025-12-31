package com.keren.virtualmoney.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Displays live camera preview as background for AR mode.
 * Expect/actual pattern for platform-specific camera implementation.
 */
@Composable
expect fun CameraPreview(modifier: Modifier = Modifier)
