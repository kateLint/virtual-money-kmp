package com.keren.virtualmoney.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.CoreGraphics.CGRect
import platform.Foundation.NSNotificationCenter
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraView(modifier: Modifier) {
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (authStatus) {
            AVAuthorizationStatusAuthorized -> {
                permissionGranted = true
            }
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    permissionGranted = granted
                }
            }
            else -> {
                permissionGranted = false
            }
        }
    }

    if (permissionGranted) {
        UIKitView(
            factory = {
                val cameraView = UIView()

                // Setup camera session
                val captureSession = AVCaptureSession()
                captureSession.sessionPreset = AVCaptureSessionPresetHigh

                // Get back camera
                val videoDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

                if (videoDevice != null) {
                    try {
                        val videoInput = AVCaptureDeviceInput.deviceInputWithDevice(videoDevice, null) as AVCaptureDeviceInput

                        if (captureSession.canAddInput(videoInput)) {
                            captureSession.addInput(videoInput)
                        }

                        // Create preview layer
                        val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)
                        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                        previewLayer.setFrame(cameraView.bounds)

                        cameraView.layer.addSublayer(previewLayer)

                        // Start session
                        captureSession.startRunning()

                        // Store session for cleanup
                        cameraView.layer.session = captureSession
                        cameraView.layer.previewLayer = previewLayer

                    } catch (e: Exception) {
                        println("Camera setup error: ${e.message}")
                    }
                }

                cameraView
            },
            modifier = modifier.fillMaxSize(),
            update = { view ->
                // Update preview layer frame when view size changes
                CATransaction.begin()
                CATransaction.setValue(true, kCATransactionDisableActions)
                (view.layer.previewLayer as? AVCaptureVideoPreviewLayer)?.setFrame(view.bounds)
                CATransaction.commit()
            }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera permission required",
                color = Color.White
            )
        }
    }
}

// Extension properties to store session and layer
@OptIn(ExperimentalForeignApi::class)
private var platform.QuartzCore.CALayer.session: AVCaptureSession?
    get() = null
    set(_) {}

@OptIn(ExperimentalForeignApi::class)
private var platform.QuartzCore.CALayer.previewLayer: AVCaptureVideoPreviewLayer?
    get() = null
    set(_) {}
