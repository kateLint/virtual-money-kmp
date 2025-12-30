package com.keren.virtualmoney.ar.camera

/**
 * Factory for creating platform-specific CameraProvider instances.
 *
 * This factory abstracts away the platform-specific initialization
 * requirements (like Android Context) from the common code.
 *
 * Platform implementations:
 * - Android: Uses ContextProvider to get application context
 * - iOS: No special initialization needed
 */
expect object CameraProviderFactory {
    /**
     * Create a platform-specific CameraProvider instance.
     * @return CameraProvider configured for the current platform
     */
    fun create(): CameraProvider
}
