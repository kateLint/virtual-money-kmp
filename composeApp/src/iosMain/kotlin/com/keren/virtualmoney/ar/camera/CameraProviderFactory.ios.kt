package com.keren.virtualmoney.ar.camera

/**
 * iOS implementation of CameraProviderFactory.
 *
 * iOS CameraProvider doesn't require any special context or initialization parameters.
 */
actual object CameraProviderFactory {
    /**
     * Create a CameraProvider instance for iOS.
     * @return CameraProvider configured for iOS
     */
    actual fun create(): CameraProvider {
        return CameraProvider()
    }
}
