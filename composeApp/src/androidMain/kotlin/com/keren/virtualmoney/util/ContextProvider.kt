package com.keren.virtualmoney.util

import android.annotation.SuppressLint
import android.content.Context

/**
 * Global application context provider.
 * This is initialized in MainActivity and used by platform-specific implementations.
 */
@SuppressLint("StaticFieldLeak")
object ContextProvider {
    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}

val applicationContext: Context
    get() = ContextProvider.applicationContext
