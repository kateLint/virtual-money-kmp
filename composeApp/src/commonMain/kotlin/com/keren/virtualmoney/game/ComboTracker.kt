package com.keren.virtualmoney.game

import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current combo state.
 */
data class ComboState(
    val count: Int = 0,
    val lastCollectionTime: Long = 0,
    val comboWindow: Long = 2000L, // 2 seconds to maintain combo
    val highestCombo: Int = 0
) {
    /**
     * Check if combo is currently active (within time window).
     */
    fun isActive(): Boolean {
        if (count <= 0) return false
        return (getCurrentTimeMillis() - lastCollectionTime) < comboWindow
    }

    /**
     * Returns the score multiplier based on current combo count.
     */
    fun multiplier(): Float = when {
        count < 3 -> 1.0f    // No multiplier below 3
        count < 5 -> 1.2f    // 3-4 combo: 1.2x
        count < 10 -> 1.5f   // 5-9 combo: 1.5x
        count < 20 -> 2.0f   // 10-19 combo: 2.0x
        else -> 2.5f         // 20+ combo: 2.5x
    }

    /**
     * Returns time remaining in combo window (milliseconds).
     */
    fun timeRemaining(): Long {
        if (!isActive()) return 0
        return maxOf(0, comboWindow - (getCurrentTimeMillis() - lastCollectionTime))
    }

    /**
     * Returns time remaining as a fraction (0.0 to 1.0).
     */
    fun timeRemainingFraction(): Float {
        if (!isActive()) return 0f
        return (timeRemaining().toFloat() / comboWindow).coerceIn(0f, 1f)
    }

    /**
     * Returns true if this is a milestone combo (3, 5, 10, 20, etc).
     */
    fun isMilestone(): Boolean = count in listOf(3, 5, 10, 15, 20, 25, 30, 50)

    /**
     * Returns the combo tier name for display.
     */
    fun tierName(): String? = when {
        count < 3 -> null
        count < 5 -> "COMBO"
        count < 10 -> "GREAT"
        count < 20 -> "AMAZING"
        count < 30 -> "INCREDIBLE"
        else -> "LEGENDARY"
    }
}

/**
 * Tracks combo state throughout a game session.
 * Manages combo counting, multiplier calculation, and state updates.
 */
class ComboTracker {
    private val _state = MutableStateFlow(ComboState())
    val state: StateFlow<ComboState> = _state.asStateFlow()

    /**
     * Called when a coin is collected. Increments combo if within window.
     * @return The combo multiplier to apply to this coin's points
     */
    fun onCoinCollected(): Float {
        val now = getCurrentTimeMillis()
        val current = _state.value

        val newState = if (current.isActive()) {
            // Continue combo
            val newCount = current.count + 1
            current.copy(
                count = newCount,
                lastCollectionTime = now,
                highestCombo = maxOf(current.highestCombo, newCount)
            )
        } else {
            // Start new combo
            current.copy(
                count = 1,
                lastCollectionTime = now,
                highestCombo = maxOf(current.highestCombo, 1)
            )
        }

        _state.value = newState
        return newState.multiplier()
    }

    /**
     * Called when a penalty coin is collected. Breaks the combo.
     */
    fun onPenaltyHit() {
        val current = _state.value
        _state.value = current.copy(
            count = 0,
            lastCollectionTime = 0
        )
    }

    /**
     * Resets the combo tracker for a new game.
     */
    fun reset() {
        _state.value = ComboState()
    }

    /**
     * Returns the current highest combo achieved this game.
     */
    fun getHighestCombo(): Int = _state.value.highestCombo

    /**
     * Updates combo state (call periodically to handle expiration).
     * @return true if combo just expired this tick
     */
    fun tick(): Boolean {
        val current = _state.value
        if (current.count > 0 && !current.isActive()) {
            // Combo expired
            _state.value = current.copy(
                count = 0,
                lastCollectionTime = 0
            )
            return true
        }
        return false
    }
}
