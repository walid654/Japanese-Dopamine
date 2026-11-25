package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val IntroBrushFont = FontFamily(
    Font(R.font.kaushan_script, FontWeight.Normal)
)

private val IntroBgBlack = Color(0xFF0D0D0F)
private val IntroCrimsonInk = Color(0xFFF21349)

private data class InkSplatterSpec(
    val lineIndex: Int, // 0 = Japanese, 1 = Dopamine
    val triggerProgress: Float,
    val xFraction: Float,
    val yOffsetFactor: Float,
    val driftX: Float,
    val driftY: Float,
    val radiusDp: Float,
    val maxAlpha: Float
)

// Fine crisp crimson ink droplets drifting slowly upward-right along the calligraphy flow
private val IntroSplatters = listOf(
    // Line 1 ("Japanese") splatters
    InkSplatterSpec(0, 0.08f, 0.14f, -0.32f, -4f, -14f, 1.8f, 0.92f),
    InkSplatterSpec(0, 0.14f, 0.19f, -0.46f, 6f, -20f, 1.3f, 0.85f),
    InkSplatterSpec(0, 0.22f, 0.25f, -0.38f, 4f, -16f, 2.2f, 0.95f),
    InkSplatterSpec(0, 0.31f, 0.33f, -0.52f, 8f, -22f, 1.4f, 0.80f),
    InkSplatterSpec(0, 0.40f, 0.41f, -0.28f, -5f, -15f, 1.6f, 0.88f),
    InkSplatterSpec(0, 0.50f, 0.51f, -0.48f, 10f, -24f, 2.0f, 0.92f),
    InkSplatterSpec(0, 0.58f, 0.59f, -0.36f, 6f, -18f, 1.2f, 0.78f),
    InkSplatterSpec(0, 0.68f, 0.69f, -0.50f, 12f, -26f, 2.3f, 0.94f),
    InkSplatterSpec(0, 0.77f, 0.78f, -0.30f, 9f, -17f, 1.5f, 0.86f),
    InkSplatterSpec(0, 0.86f, 0.87f, -0.44f, 14f, -22f, 2.0f, 0.90f),
    InkSplatterSpec(0, 0.93f, 0.93f, -0.22f, 16f, -14f, 1.4f, 0.82f),

    // Line 2 ("Dopamine") splatters
    InkSplatterSpec(1, 0.08f, 0.11f, -0.30f, -5f, -14f, 2.0f, 0.92f),
    InkSplatterSpec(1, 0.17f, 0.18f, -0.44f, 6f, -20f, 1.4f, 0.84f),
    InkSplatterSpec(1, 0.27f, 0.28f, -0.35f, 4f, -17f, 2.1f, 0.95f),
    InkSplatterSpec(1, 0.37f, 0.38f, -0.52f, 10f, -25f, 2.4f, 0.92f),
    InkSplatterSpec(1, 0.47f, 0.49f, -0.26f, 6f, -15f, 1.3f, 0.80f),
    InkSplatterSpec(1, 0.57f, 0.58f, -0.46f, 11f, -22f, 1.9f, 0.90f),
    InkSplatterSpec(1, 0.67f, 0.68f, -0.34f, 8f, -18f, 1.5f, 0.85f),
    InkSplatterSpec(1, 0.78f, 0.79f, -0.48f, 14f, -26f, 2.2f, 0.94f),
    InkSplatterSpec(1, 0.87f, 0.88f, -0.32f, 12f, -16f, 1.6f, 0.88f),
    InkSplatterSpec(1, 0.94f, 0.94f, -0.18f, 16f, -13f, 1.4f, 0.82f)
)

@Composable
fun IntroSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Deliberate, unhurried timings matching the 4.5-5s reference video flow
    val line1Progress = remember { Animatable(0f) }
    val line2Progress = remember { Animatable(0f) }
    val swashProgress = remember { Animatable(0f) }

    // Slow organic "jump / float" wave & settle motion
    val line1Jump = remember { Animatable(0f) }
    val line2Jump = remember { Animatable(0f) }
    val masterScale = remember { Animatable(0.93f) }
    val masterFloatY = remember { Animatable(10f) }
    val masterTilt = remember { Animatable(-8.2f) }
    val dropletDriftProgress = remember { Animatable(0f) }

    val screenAlpha = remember { Animatable(1f) }
    val exitZoom = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Gentle opening pause on deep matte dark canvas
        delay(220L)

        // Slow continuous droplet drift & whole-logo floating settle running smoothly alongside
        launch {
            dropletDriftProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 4800, easing = CubicBezierEasing(0.22f, 0.0f, 0.35f, 1f))
            )
        }
        launch {
            masterScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 4200, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f))
            )
        }
        launch {
            masterFloatY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 4000, easing = CubicBezierEasing(0.22f, 0.1f, 0.28f, 1f))
            )
        }
        launch {
            masterTilt.animateTo(
                targetValue = -6f,
                animationSpec = tween(durationMillis = 4100, easing = CubicBezierEasing(0.28f, 0.05f, 0.30f, 1f))
            )
        }

        // Phase 1: Smooth unhurried brush flow for "Japanese" (~1.85s)
        val job1 = launch {
            line1Progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1850,
                    easing = CubicBezierEasing(0.36f, 0.02f, 0.20f, 1f)
                )
            )
        }
        // Slow gentle vertical jump/bounce wave on "Japanese" while flowing
        launch {
            delay(300L)
            line1Jump.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.58f,
                    stiffness = 75f
                )
            )
        }

        // Start "Dopamine" smoothly as the brush finishes the tail of "Japanese"
        delay(1600L)

        // Phase 2: Smooth unhurried brush flow for "Dopamine" (~1.95s)
        val job2 = launch {
            line2Progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1950,
                    easing = CubicBezierEasing(0.34f, 0.02f, 0.18f, 1f)
                )
            )
        }
        // Slow gentle vertical jump/bounce wave on "Dopamine" while flowing
        launch {
            delay(320L)
            line2Jump.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.56f,
                    stiffness = 72f
                )
            )
        }

        job1.join()
        // Wait until "Dopamine" is nearing its flourish tail before sweeping the swash
        delay(1350L)

        // Phase 3: Tapered calligraphic underline swash (~0.85s)
        val swashJob = launch {
            swashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 850,
                    easing = CubicBezierEasing(0.28f, 0.05f, 0.20f, 1f)
                )
            )
        }
        job2.join()
        swashJob.join()

        // Calm cinematic hold so the completed typography breathes before entering the app
        delay(900L)

        // Silky fade out with subtle forward float into the app
        launch {
            exitZoom.animateTo(
                targetValue = 1.035f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    val p1 = line1Progress.value
    val p2 = line2Progress.value
    val swashP = swashProgress.value
    val driftP = dropletDriftProgress.value

    // Smooth slow up-and-settle jump curve derived from progress + spring
    val line1BounceOffsetDp = (-sin(line1Jump.value * PI).toFloat() * 7.5f)
    val line2BounceOffsetDp = (-sin(line2Jump.value * PI).toFloat() * 8.5f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(IntroBgBlack)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    rotationZ = masterTilt.value
                    scaleX = masterScale.value * exitZoom.value
                    scaleY = masterScale.value * exitZoom.value
                    translationY = masterFloatY.value.dp.toPx()
                },
            contentAlignment = Alignment.Center
        ) {
            val isCompactWidth = maxWidth < 330.dp
            val topFontSize = if (isCompactWidth) 42.sp else 50.sp
            val bottomFontSize = if (isCompactWidth) 46.sp else 54.sp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top Line: Japanese
                Box(
                    modifier = Modifier.offset(y = line1BounceOffsetDp.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Japanese",
                        fontFamily = IntroBrushFont,
                        fontSize = topFontSize,
                        color = IntroCrimsonInk,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .drawWithContent {
                                if (p1 > 0f) {
                                    // Slanted calligraphic nib clip edge so strokes emerge naturally
                                    val slant = size.height * 0.24f
                                    val edgeX = (size.width + slant) * p1
                                    val revealPath = Path().apply {
                                        moveTo(-slant, 0f)
                                        lineTo(edgeX, 0f)
                                        lineTo(edgeX - slant, size.height)
                                        lineTo(-slant, size.height)
                                        close()
                                    }
                                    clipPath(revealPath) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                            }
                    )

                    // Ink splatters + delicate brush tip following the slow calligraphy flow
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        if (p1 > 0.01f && p1 < 0.985f) {
                            val tipFade = when {
                                p1 < 0.08f -> p1 / 0.08f
                                p1 > 0.90f -> (1f - p1) / 0.10f
                                else -> 1f
                            }.coerceIn(0f, 1f)
                            val tipX = w * p1
                            val tipY = h * 0.48f + sin(p1 * 15f) * h * 0.07f
                            drawCircle(
                                color = IntroCrimsonInk.copy(alpha = 0.85f * tipFade),
                                radius = 2.8.dp.toPx(),
                                center = Offset(tipX, tipY)
                            )
                        }

                        IntroSplatters.filter { it.lineIndex == 0 }.forEach { sp ->
                            if (p1 >= sp.triggerProgress) {
                                val localAge = ((p1 - sp.triggerProgress) / 0.45f).coerceIn(0f, 1f)
                                val slowTravel = (localAge * 0.55f + driftP * 0.45f).coerceIn(0f, 1f)
                                val fade = (sp.maxAlpha * (1f - driftP * 0.55f)).coerceIn(0.22f, 1f)
                                val px = w * sp.xFraction + sp.driftX.dp.toPx() * slowTravel
                                val py = h * (0.5f + sp.yOffsetFactor) + sp.driftY.dp.toPx() * slowTravel
                                drawCircle(
                                    color = IntroCrimsonInk.copy(alpha = fade),
                                    radius = sp.radiusDp.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }

                // Bottom Line: Dopamine (with smooth vertical jump + proximity to top line)
                Box(
                    modifier = Modifier.offset(y = (-8f + line2BounceOffsetDp).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dopamine",
                        fontFamily = IntroBrushFont,
                        fontSize = bottomFontSize,
                        color = IntroCrimsonInk,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .drawWithContent {
                                if (p2 > 0f) {
                                    val slant = size.height * 0.24f
                                    val edgeX = (size.width + slant) * p2
                                    val revealPath = Path().apply {
                                        moveTo(-slant, 0f)
                                        lineTo(edgeX, 0f)
                                        lineTo(edgeX - slant, size.height)
                                        lineTo(-slant, size.height)
                                        close()
                                    }
                                    clipPath(revealPath) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                            }
                    )

                    // Ink splatters + delicate brush tip for "Dopamine"
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        if (p2 > 0.01f && p2 < 0.985f) {
                            val tipFade = when {
                                p2 < 0.08f -> p2 / 0.08f
                                p2 > 0.90f -> (1f - p2) / 0.10f
                                else -> 1f
                            }.coerceIn(0f, 1f)
                            val tipX = w * p2
                            val tipY = h * 0.50f + cos(p2 * 14f) * h * 0.07f
                            drawCircle(
                                color = IntroCrimsonInk.copy(alpha = 0.85f * tipFade),
                                radius = 3.0.dp.toPx(),
                                center = Offset(tipX, tipY)
                            )
                        }

                        IntroSplatters.filter { it.lineIndex == 1 }.forEach { sp ->
                            if (p2 >= sp.triggerProgress) {
                                val localAge = ((p2 - sp.triggerProgress) / 0.45f).coerceIn(0f, 1f)
                                val slowTravel = (localAge * 0.55f + driftP * 0.45f).coerceIn(0f, 1f)
                                val fade = (sp.maxAlpha * (1f - driftP * 0.55f)).coerceIn(0.24f, 1f)
                                val px = w * sp.xFraction + sp.driftX.dp.toPx() * slowTravel
                                val py = h * (0.5f + sp.yOffsetFactor) + sp.driftY.dp.toPx() * slowTravel
                                drawCircle(
                                    color = IntroCrimsonInk.copy(alpha = fade),
                                    radius = sp.radiusDp.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }

                // Tapered variable-width calligraphic underline swash matching the reference stroke
                Canvas(
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .width(248.dp)
                        .height(24.dp)
                ) {
                    if (swashP > 0f) {
                        val w = size.width
                        val h = size.height
                        val steps = 36
                        val maxStep = (steps * swashP).toInt().coerceAtLeast(1)

                        val topPoints = ArrayList<Offset>(maxStep + 1)
                        val bottomPoints = ArrayList<Offset>(maxStep + 1)

                        for (i in 0..maxStep) {
                            val t = (i.toFloat() / steps).coerceAtMost(swashP)
                            val inv = 1f - t
                            // Smooth cubic centerline arching gently from left to right
                            val p0x = w * 0.06f; val p0y = h * 0.72f
                            val p1x = w * 0.34f; val p1y = h * 0.24f
                            val p2x = w * 0.70f; val p2y = h * 0.18f
                            val p3x = w * 0.95f; val p3y = h * 0.40f

                            val cx = inv * inv * inv * p0x +
                                3f * inv * inv * t * p1x +
                                3f * inv * t * t * p2x +
                                t * t * t * p3x
                            val cy = inv * inv * inv * p0y +
                                3f * inv * inv * t * p1y +
                                3f * inv * t * t * p2y +
                                t * t * t * p3y

                            // Tapered brush thickness: pointed ends, fuller belly around t = 0.45
                            val halfThickness = (sin(t * PI).toFloat() * 2.8f + 0.35f).dp.toPx()
                            topPoints.add(Offset(cx, cy - halfThickness))
                            bottomPoints.add(Offset(cx, cy + halfThickness))
                        }

                        if (topPoints.size >= 2) {
                            val ribbon = Path().apply {
                                moveTo(topPoints.first().x, topPoints.first().y)
                                for (i in 1 until topPoints.size) {
                                    lineTo(topPoints[i].x, topPoints[i].y)
                                }
                                for (i in bottomPoints.size - 1 downTo 0) {
                                    lineTo(bottomPoints[i].x, bottomPoints[i].y)
                                }
                                close()
                            }
                            drawPath(
                                path = ribbon,
                                color = IntroCrimsonInk
                            )
                        }
                    }
                }
            }
        }
    }
}
