package com.keren.virtualmoney.ar.camera

import com.keren.virtualmoney.util.ContextProvider

/**
 * Android implementation of CameraProviderFactory.
 *
 * Uses ContextProvider to get the application context needed for ARCore initialization.
 * This avoids passing Context through the entire composable hierarchy.
 */
actual object CameraProviderFactory {
    /**
     * Create a CameraProvider instance using the application context.
     * @return CameraProvider configured with Android context
     */
    actual fun create(): CameraProvider {
        return CameraProvider(ContextProvider.applicationContext)
    }
}
