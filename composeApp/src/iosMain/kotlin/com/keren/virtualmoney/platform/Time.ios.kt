package com.keren.virtualmoney.platform

import platform.Foundation.NSDate

/**
 * iOS implementation of getCurrentTimeMillis.
 */
actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
