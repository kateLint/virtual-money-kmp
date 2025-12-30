package com.keren.virtualmoney.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keren.virtualmoney.game.GameEngine
import com.keren.virtualmoney.platform.createHapticFeedback
import com.keren.virtualmoney.platform.createHighScoreStorage
import com.keren.virtualmoney.platform.createSoundPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel that manages the GameEngine and platform-specific dependencies.
 * This is in commonMain to demonstrate shared logic, but platform-specific
 * instances are injected via expect/actual factory functions.
 */
class GameViewModel : ViewModel() {
    private val hapticFeedback = createHapticFeedback()
    private val soundPlayer = createSoundPlayer()
    private val storage = createHighScoreStorage()

    val gameEngine = GameEngine(
        coroutineScope = viewModelScope,
        onCoinCollected = {
            hapticFeedback.performLight()
            soundPlayer.playCoinSound()
        },
        getHighScore = { storage.getHighScore() },
        saveHighScore = { score -> storage.saveHighScore(score) }
    )

    // AR mode toggle - default to true for AR mode
    private val _isARMode = MutableStateFlow(true)
    val isARMode: StateFlow<Boolean> = _isARMode.asStateFlow()

    /**
     * Toggle between AR and 2D mode.
     */
    fun toggleARMode() {
        _isARMode.value = !_isARMode.value
    }

    /**
     * Set AR mode explicitly.
     */
    fun setARMode(enabled: Boolean) {
        _isARMode.value = enabled
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.release()
    }
}
