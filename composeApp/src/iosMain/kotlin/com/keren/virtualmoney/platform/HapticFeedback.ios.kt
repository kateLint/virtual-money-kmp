package com.keren.virtualmoney.platform

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

private class IOSHapticFeedback : HapticFeedback {
    private val impactGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)

    init {
        impactGenerator.prepare()
    }

    override fun performLight() {
        impactGenerator.impactOccurred()
    }
}

actual fun createHapticFeedback(): HapticFeedback {
    return IOSHapticFeedback()
}
