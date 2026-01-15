package com.keren.virtualmoney.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Standard animation durations used throughout the app.
 */
object AnimationDurations {
    const val INSTANT = 100
    const val FAST = 200
    const val NORMAL = 300
    const val SLOW = 500
    const val EXTRA_SLOW = 800
}

/**
 * Standard easing curves.
 */
object GameEasing {
    val bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    val gentle = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessLow
    )
}

/**
 * Screen transition specs.
 */
object ScreenTransitions {
    fun slideInFromRight(): EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(AnimationDurations.NORMAL))

    fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(AnimationDurations.NORMAL))

    fun slideInFromLeft(): EnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(AnimationDurations.NORMAL))

    fun slideOutToRight(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(AnimationDurations.NORMAL))

    fun scaleIn(): EnterTransition = scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(AnimationDurations.NORMAL))

    fun scaleOut(): ExitTransition = scaleOut(
        targetScale = 1.1f,
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(AnimationDurations.NORMAL))

    fun slideUpEnter(): EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeIn()

    fun slideDownExit(): ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(AnimationDurations.NORMAL, easing = FastOutSlowInEasing)
    ) + fadeOut()
}

/**
 * Coin collection animation.
 */
@Composable
fun CoinCollectAnimation(
    isCollecting: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var animationComplete by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isCollecting) 1.5f else 1f,
        animationSpec = if (isCollecting) {
            tween(150, easing = FastOutSlowInEasing)
        } else {
            spring()
        },
        finishedListener = {
            if (isCollecting && !animationComplete) {
                animationComplete = true
            }
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isCollecting && animationComplete) 0f else 1f,
        animationSpec = tween(100),
        finishedListener = {
            if (isCollecting && animationComplete) {
                onAnimationEnd()
            }
        }
    )

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}

/**
 * Score pop animation.
 */
@Composable
fun ScorePopAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.5f,
            transformOrigin = TransformOrigin(0.5f, 1f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        ) + fadeIn(),
        exit = scaleOut(
            targetScale = 1.2f,
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Pulse animation modifier.
 */
@Composable
fun Modifier.pulse(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMs: Int = 600
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs),
            repeatMode = RepeatMode.Reverse
        )
    )

    return this.scale(scale)
}

/**
 * Shake animation modifier.
 */
@Composable
fun Modifier.shake(
    enabled: Boolean = false,
    shakeIntensity: Float = 10f
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -shakeIntensity,
        targetValue = shakeIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        )
    )

    return this.graphicsLayer {
        translationX = offsetX
    }
}

/**
 * Float animation modifier (up and down).
 */
@Composable
fun Modifier.floatAnimation(
    enabled: Boolean = true,
    offsetY: Float = 10f,
    durationMs: Int = 2000
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = -offsetY,
        targetValue = offsetY,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    return this.graphicsLayer {
        translationY = offset
    }
}

/**
 * Glow animation (alpha pulsing).
 */
@Composable
fun Modifier.glow(
    enabled: Boolean = true,
    minAlpha: Float = 0.6f,
    maxAlpha: Float = 1f,
    durationMs: Int = 1000
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs),
            repeatMode = RepeatMode.Reverse
        )
    )

    return this.alpha(alpha)
}

/**
 * Spin animation modifier.
 */
@Composable
fun Modifier.spin(
    enabled: Boolean = true,
    durationMs: Int = 3000
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing)
        )
    )

    return this.graphicsLayer {
        rotationZ = rotation
    }
}

/**
 * Bounce in animation.
 */
@Composable
fun BounceIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMs: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ).let { if (delayMs > 0) it else it }
        ) + fadeIn(animationSpec = tween(delayMillis = delayMs)),
        exit = scaleOut(
            targetScale = 0f,
            animationSpec = tween(AnimationDurations.FAST)
        ) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Staggered animation helper.
 */
@Composable
fun StaggeredList(
    items: List<@Composable () -> Unit>,
    staggerDelayMs: Int = 50,
    modifier: Modifier = Modifier
) {
    items.forEachIndexed { index, item ->
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay((index * staggerDelayMs).toLong())
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + fadeIn(),
            modifier = modifier
        ) {
            item()
        }
    }
}

/**
 * Crossfade with custom animation.
 */
@Composable
fun <T> GameCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(AnimationDurations.NORMAL),
    content: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        label = "game_crossfade"
    ) { state ->
        content(state)
    }
}

