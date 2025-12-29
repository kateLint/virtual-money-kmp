package com.keren.virtualmoney.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific camera view.
 * Each platform will provide its own implementation using native camera APIs.
 */
@Composable
expect fun CameraView(modifier: Modifier = Modifier)
