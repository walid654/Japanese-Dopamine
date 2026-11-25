package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val CDS_WITH_VISUALS = setOf(3, 6, 7, 14, 19, 26, 30, 37, 41, 51, 58, 80)

// White-theme ink & accent palette for crystal-clear textbook worksheets
internal val SheetWhiteBg = Color(0xFFFFFFFF)
internal val SheetSoftGray = Color(0xFFF3F4F6)
internal val SheetBorder = Color(0xFFCBD5E1)
internal val SheetInkBlack = Color(0xFF111827)
internal val SheetInkMuted = Color(0xFF4B5563)
internal val SheetAccentRed = Color(0xFFD32F2F)
internal val SheetAccentBlue = Color(0xFF1D4ED8)
internal val SheetAccentAmber = Color(0xFFB45309)
internal val SheetAccentGreen = Color(0xFF047857)
internal val SheetAccentPink = Color(0xFFBE185D)

@Composable
fun CdListeningVisualSection(
    cdNumber: Int,
    modifier: Modifier = Modifier
) {
    if (cdNumber !in CDS_WITH_VISUALS) return

    var zoomedPanelTitle by remember { mutableStateOf<String?>(null) }
    var zoomedContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SheetWhiteBg,
        border = BorderStroke(1.5.dp, SheetBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = SheetAccentRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ছবি দেখে উত্তর দিন (Picture Exercise)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SheetInkBlack
                    )
                }
                Text(
                    text = "ট্যাপ করে বড় দেখুন",
                    fontSize = 10.5.sp,
                    color = SheetInkMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val onZoom: (String, @Composable () -> Unit) -> Unit = { title, content ->
                zoomedPanelTitle = title
                zoomedContent = content
            }

            when (cdNumber) {
                3 -> Cd03Visuals(onZoom)
                6 -> Cd06Visuals(onZoom)
                7 -> Cd07Visuals(onZoom)
                14 -> Cd14Visuals(onZoom)
                19 -> Cd19Visuals(onZoom)
                26 -> Cd26Visuals(onZoom)
                30 -> Cd30Visuals(onZoom)
                37 -> Cd37Visuals(onZoom)
                41 -> Cd41Visuals(onZoom)
                51 -> Cd51Visuals(onZoom)
                58 -> Cd58Visuals(onZoom)
                80 -> Cd80Visuals(onZoom)
            }
        }
    }

    if (zoomedContent != null && zoomedPanelTitle != null) {
        Dialog(
            onDismissRequest = {
                zoomedContent = null
                zoomedPanelTitle = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = SheetWhiteBg,
                border = BorderStroke(1.5.dp, SheetBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = zoomedPanelTitle!!,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SheetInkBlack
                        )
                        IconButton(
                            onClick = {
                                zoomedContent = null
                                zoomedPanelTitle = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SheetInkBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SheetWhiteBg,
                        border = BorderStroke(1.dp, SheetBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.08f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            zoomedContent!!.invoke()
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun VisualQuestionGroup(
    label: String,
    options: List<Pair<String, @Composable () -> Unit>>,
    onZoom: (String, @Composable () -> Unit) -> Unit,
    highlightedIndex: Int? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEachIndexed { idx, (badge, content) ->
                val isHighlighted = highlightedIndex == idx
                VisualPanelCard(
                    badge = badge,
                    isHighlighted = isHighlighted,
                    onClick = { onZoom("$label  •  $badge", content) },
                    modifier = Modifier.weight(1f),
                    content = content
                )
            }
        }
    }
}

@Composable
internal fun VisualPanelCard(
    badge: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = SheetWhiteBg,
        border = BorderStroke(
            width = if (isHighlighted) 2.dp else 1.2.dp,
            color = if (isHighlighted) SheetAccentRed else SheetBorder
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isHighlighted) SheetAccentRed else SheetSoftGray,
                    border = BorderStroke(1.dp, if (isHighlighted) SheetAccentRed else SheetBorder)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlighted) Color.White else SheetInkBlack,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom",
                    tint = SheetInkMuted.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

internal fun DrawScope.drawClockFace(
    center: Offset,
    radius: Float,
    hour: Int,
    minute: Int = 0,
    faceColor: Color = Color(0xFFFFFFFF),
    ringColor: Color = Color(0xFF111827),
    handColor: Color = Color(0xFFD32F2F)
) {
    drawCircle(color = faceColor, radius = radius, center = center)
    drawCircle(color = ringColor, radius = radius, center = center, style = Stroke(width = 2.4f))

    for (h in 0 until 12) {
        val angle = (h * 30.0 - 90.0) * PI / 180.0
        val inner = radius * if (h % 3 == 0) 0.72f else 0.84f
        val outer = radius * 0.94f
        drawLine(
            color = ringColor.copy(alpha = if (h % 3 == 0) 0.95f else 0.5f),
            start = Offset(center.x + inner * cos(angle).toFloat(), center.y + inner * sin(angle).toFloat()),
            end = Offset(center.x + outer * cos(angle).toFloat(), center.y + outer * sin(angle).toFloat()),
            strokeWidth = if (h % 3 == 0) 2.2f else 1.2f,
            cap = StrokeCap.Round
        )
    }

    val hourAngle = ((hour % 12 + minute / 60f) * 30.0 - 90.0) * PI / 180.0
    drawLine(
        color = handColor,
        start = center,
        end = Offset(
            center.x + radius * 0.54f * cos(hourAngle).toFloat(),
            center.y + radius * 0.54f * sin(hourAngle).toFloat()
        ),
        strokeWidth = 3.2f,
        cap = StrokeCap.Round
    )

    val minAngle = (minute * 6.0 - 90.0) * PI / 180.0
    drawLine(
        color = ringColor,
        start = center,
        end = Offset(
            center.x + radius * 0.78f * cos(minAngle).toFloat(),
            center.y + radius * 0.78f * sin(minAngle).toFloat()
        ),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )
    drawCircle(color = handColor, radius = 2.6f, center = center)
}

internal fun DrawScope.drawStickPerson(
    centerX: Float,
    topY: Float,
    height: Float,
    color: Color = SheetInkBlack,
    accent: Color = Color(0xFF374151),
    mustache: Boolean = false,
    longHair: Boolean = false,
    darkDress: Boolean = false,
    briefcase: Boolean = false
) {
    val headR = height * 0.14f
    val headCenter = Offset(centerX, topY + headR)

    if (longHair) {
        drawArc(
            color = accent,
            startAngle = 160f,
            sweepAngle = 220f,
            useCenter = true,
            topLeft = Offset(headCenter.x - headR * 1.25f, headCenter.y - headR * 1.15f),
            size = Size(headR * 2.5f, headR * 3.1f)
        )
    }

    drawCircle(color = Color.White, radius = headR, center = headCenter)
    drawCircle(color = color, radius = headR, center = headCenter, style = Stroke(width = 2.4f))
    if (mustache) {
        drawLine(
            color = color,
            start = Offset(headCenter.x - headR * 0.45f, headCenter.y + headR * 0.3f),
            end = Offset(headCenter.x + headR * 0.45f, headCenter.y + headR * 0.3f),
            strokeWidth = 2.6f
        )
    }

    val bodyTop = topY + headR * 2.15f
    val bodyBottom = topY + height * 0.72f
    val bodyW = headR * 1.45f

    if (darkDress || longHair) {
        val dress = Path().apply {
            moveTo(centerX - bodyW * 0.45f, bodyTop)
            lineTo(centerX + bodyW * 0.45f, bodyTop)
            lineTo(centerX + bodyW * 0.85f, bodyBottom)
            lineTo(centerX - bodyW * 0.85f, bodyBottom)
            close()
        }
        drawPath(
            path = dress,
            color = if (darkDress) Color(0xFF374151) else Color(0xFFE5E7EB)
        )
        drawPath(path = dress, color = color, style = Stroke(width = 2.2f))
    } else {
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(centerX - bodyW * 0.55f, bodyTop),
            size = Size(bodyW * 1.1f, bodyBottom - bodyTop),
            cornerRadius = CornerRadius(bodyW * 0.45f, bodyW * 0.45f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(centerX - bodyW * 0.55f, bodyTop),
            size = Size(bodyW * 1.1f, bodyBottom - bodyTop),
            cornerRadius = CornerRadius(bodyW * 0.45f, bodyW * 0.45f),
            style = Stroke(width = 2.3f)
        )
    }

    drawLine(
        color = color,
        start = Offset(centerX - bodyW * 0.55f, bodyTop + 4f),
        end = Offset(centerX - bodyW * 1.05f, bodyTop + (bodyBottom - bodyTop) * 0.65f),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(centerX + bodyW * 0.55f, bodyTop + 4f),
        end = Offset(centerX + bodyW * 1.05f, bodyTop + (bodyBottom - bodyTop) * 0.65f),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )

    if (briefcase) {
        drawRoundRect(
            color = SheetInkBlack,
            topLeft = Offset(centerX - bodyW * 1.4f, bodyTop + (bodyBottom - bodyTop) * 0.5f),
            size = Size(bodyW * 0.7f, bodyW * 0.55f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }

    drawLine(
        color = color,
        start = Offset(centerX - bodyW * 0.25f, bodyBottom),
        end = Offset(centerX - bodyW * 0.35f, topY + height),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(centerX + bodyW * 0.25f, bodyBottom),
        end = Offset(centerX + bodyW * 0.35f, topY + height),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )
}

// ======================= CD 03 =======================
@Composable
private fun Cd03Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "例 (Example) — শুভেচ্ছা ও সময়",
        highlightedIndex = 1,
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd03GreetingScene(mode = 0) },
            "②" to { Cd03GreetingScene(mode = 1) },
            "③" to { Cd03GreetingScene(mode = 2) }
        )
    )

    VisualQuestionGroup(
        label = "1) — পরিচয় পর্ব",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd03IntroScene(mode = 0) },
            "②" to { Cd03IntroScene(mode = 1) },
            "③" to { Cd03IntroScene(mode = 2) }
        )
    )

    VisualQuestionGroup(
        label = "2) — নাম ও বয়স",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd03AgeBadgeScene("ワン", "21") },
            "②" to { Cd03AgeBadgeScene("アン", "28") },
            "③" to { Cd03AgeBadgeScene("ワン", "29") }
        )
    )
}

@Composable
private fun Cd03GreetingScene(mode: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            when (mode) {
                0 -> { // Night sky strip + crescent moon + 9:00
                    drawRect(color = Color(0xFF1E293B), size = Size(w, h * 0.50f))
                    drawCircle(color = Color(0xFFFDE047), radius = w * 0.08f, center = Offset(w * 0.42f, h * 0.22f))
                    drawCircle(color = Color(0xFF1E293B), radius = w * 0.065f, center = Offset(w * 0.45f, h * 0.19f))
                    drawCircle(color = Color.White, radius = 2f, center = Offset(w * 0.16f, h * 0.16f))
                    drawCircle(color = Color.White, radius = 2f, center = Offset(w * 0.28f, h * 0.12f))
                    drawClockFace(Offset(w * 0.78f, h * 0.24f), w * 0.14f, hour = 9, minute = 0)
                }
                1 -> { // Sunrise + 7:00
                    drawArc(
                        color = Color(0xFFEA580C),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.32f, h * 0.20f),
                        size = Size(w * 0.30f, h * 0.30f)
                    )
                    drawClockFace(Offset(w * 0.78f, h * 0.24f), w * 0.14f, hour = 7, minute = 0)
                }
                else -> { // Midday Sun + 1:00
                    drawCircle(color = Color(0xFFDC2626), radius = w * 0.09f, center = Offset(w * 0.45f, h * 0.22f))
                    drawClockFace(Offset(w * 0.78f, h * 0.24f), w * 0.14f, hour = 1, minute = 0)
                }
            }
            drawStickPerson(w * 0.30f, h * 0.38f, h * 0.54f, color = SheetInkBlack, briefcase = true)
            drawStickPerson(w * 0.70f, h * 0.38f, h * 0.54f, color = SheetInkBlack)
        }
        Text(
            text = when (mode) {
                0 -> "রাত 9:00"
                1 -> "সকাল 7:00"
                else -> "দুপুর 1:00"
            },
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SheetInkMuted
        )
    }
}

@Composable
private fun Cd03IntroScene(mode: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        when (mode) {
            0 -> {
                drawStickPerson(w * 0.32f, h * 0.10f, h * 0.50f, color = SheetInkBlack)
                drawStickPerson(w * 0.70f, h * 0.10f, h * 0.50f, color = SheetInkBlack, mustache = true)
                for (i in 0..4) {
                    drawCircle(
                        color = SheetInkBlack,
                        radius = w * 0.065f,
                        center = Offset(w * (0.16f + i * 0.17f), h * 0.78f),
                        style = Stroke(width = 2.2f)
                    )
                }
            }
            1 -> {
                drawStickPerson(w * 0.28f, h * 0.16f, h * 0.70f, color = SheetInkBlack)
                drawStickPerson(w * 0.72f, h * 0.16f, h * 0.70f, color = SheetInkBlack, mustache = true)
                drawLine(
                    color = SheetAccentGreen,
                    start = Offset(w * 0.40f, h * 0.46f),
                    end = Offset(w * 0.60f, h * 0.46f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            else -> {
                drawStickPerson(w * 0.22f, h * 0.32f, h * 0.58f, color = SheetInkBlack)
                drawStickPerson(w * 0.50f, h * 0.32f, h * 0.58f, color = SheetInkBlack)
                drawStickPerson(w * 0.80f, h * 0.12f, h * 0.54f, color = SheetAccentRed, mustache = true)
            }
        }
    }
}

@Composable
private fun Cd03AgeBadgeScene(name: String, age: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(30.dp)) {
            drawCircle(
                color = SheetInkBlack,
                radius = size.minDimension * 0.38f,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = 2.4f)
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = SheetSoftGray,
            border = BorderStroke(1.5.dp, SheetInkBlack)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
                Text(age, fontSize = 18.sp, fontWeight = FontWeight.Black, color = SheetAccentRed)
            }
        }
    }
}

// ======================= CD 06 =======================
@Composable
private fun Cd06Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — জিনিস ও নামের মিল (১–৩)",
        onZoom = onZoom,
        options = listOf(
            "1)" to {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text("てちょう", fontSize = 10.sp, color = SheetAccentAmber, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SheetSoftGray,
                        border = BorderStroke(1.dp, SheetAccentBlue)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("大阪城", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
                            Text("ミラーさん", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SheetAccentGreen)
                        }
                    }
                }
            },
            "2)" to {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "あ",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = SheetInkBlack
                    )
                }
            },
            "3)" to {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SheetSoftGray,
                    border = BorderStroke(1.dp, SheetBorder),
                    modifier = Modifier.padding(3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("IMC 大阪支店", fontSize = 8.5.sp, color = SheetAccentAmber, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("マイク・ミラー", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack, textAlign = TextAlign.Center)
                    }
                }
            }
        )
    )

    VisualQuestionGroup(
        label = "1) — জিনিস ও নামের মিল (৪–৫)",
        onZoom = onZoom,
        options = listOf(
            "4)" to {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SheetSoftGray,
                    border = BorderStroke(1.dp, SheetAccentRed.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CAR", fontSize = 15.sp, fontWeight = FontWeight.Black, color = SheetAccentRed)
                        Text("MAGAZINE", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
                    }
                }
            },
            "5)" to {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Canvas(modifier = Modifier.size(44.dp, 32.dp)) {
                        drawRoundRect(
                            color = SheetInkMuted,
                            topLeft = Offset(size.width * 0.25f, 0f),
                            size = Size(size.width * 0.5f, size.height * 0.35f),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 2.4f)
                        )
                        drawRoundRect(
                            color = SheetInkBlack,
                            topLeft = Offset(4f, size.height * 0.25f),
                            size = Size(size.width - 8f, size.height * 0.72f),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 2.4f)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SheetSoftGray,
                        border = BorderStroke(1.dp, SheetAccentGreen)
                    ) {
                        Text(
                            text = "たなか",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SheetAccentGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        )
    )
}

// ======================= CD 07 =======================
@Composable
private fun Cd07Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — অ্যাপার্টমেন্ট ও প্রতিবেশী",
        onZoom = onZoom,
        options = listOf(
            "①" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = RoundedCornerShape(4.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetInkBlack)) {
                        Text("505 ミラー", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.30f, size.height * 0.12f, size.height * 0.78f, longHair = true)
                        drawStickPerson(size.width * 0.70f, size.height * 0.12f, size.height * 0.78f)
                    }
                }
            },
            "②" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetAccentBlue)) {
                            Text("505 ミラー", fontSize = 7.5.sp, color = SheetAccentBlue, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetInkBlack)) {
                            Text("506 たなか", fontSize = 7.5.sp, color = SheetInkBlack, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                        }
                    }
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.28f, size.height * 0.20f, size.height * 0.70f)
                        drawStickPerson(size.width * 0.72f, size.height * 0.12f, size.height * 0.78f, longHair = true)
                    }
                }
            },
            "③" to {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawStickPerson(size.width * 0.22f, size.height * 0.16f, size.height * 0.70f)
                    drawStickPerson(size.width * 0.50f, size.height * 0.18f, size.height * 0.68f, longHair = true)
                    drawStickPerson(size.width * 0.78f, size.height * 0.16f, size.height * 0.70f)
                }
            }
        )
    )

    VisualQuestionGroup(
        label = "2) — উপহার আদান-প্রদান",
        onZoom = onZoom,
        options = listOf(
            "①" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("チョコレートどうぞ", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack, textAlign = TextAlign.Center)
                    Text("ありがとう", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = SheetAccentGreen)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.26f, size.height * 0.12f, size.height * 0.78f)
                        drawRect(color = SheetAccentRed, topLeft = Offset(size.width * 0.44f, size.height * 0.42f), size = Size(size.width * 0.12f, size.width * 0.10f))
                        drawStickPerson(size.width * 0.74f, size.height * 0.12f, size.height * 0.78f, longHair = true)
                    }
                }
            },
            "②" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("দোকান / কাউন্টার", fontSize = 8.5.sp, color = SheetInkMuted, fontWeight = FontWeight.Medium)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.28f, size.height * 0.08f, size.height * 0.72f, longHair = true)
                        drawStickPerson(size.width * 0.74f, size.height * 0.14f, size.height * 0.74f)
                        drawLine(color = SheetInkBlack, start = Offset(size.width * 0.1f, size.height * 0.62f), end = Offset(size.width * 0.64f, size.height * 0.62f), strokeWidth = 3f)
                    }
                }
            },
            "③" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("どうぞ  ？", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SheetAccentPink)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.26f, size.height * 0.14f, size.height * 0.76f, longHair = true)
                        drawRect(color = SheetAccentPink, topLeft = Offset(size.width * 0.44f, size.height * 0.40f), size = Size(size.width * 0.12f, size.width * 0.12f))
                        drawStickPerson(size.width * 0.74f, size.height * 0.14f, size.height * 0.76f)
                    }
                }
            }
        )
    )
}

// ======================= CD 14 =======================
@Composable
private fun Cd14Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — টোকিও ও লন্ডনের সময়",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd14WorldClocks(tokyoHour = 4, londonHour = 7) },
            "②" to { Cd14WorldClocks(tokyoHour = 4, londonHour = 1) },
            "③" to { Cd14WorldClocks(tokyoHour = 1, londonHour = 7) }
        )
    )

    VisualQuestionGroup(
        label = "2) — ঘুম ও জাগার সময়",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd14SleepClocks(fromHour = 6, toHour = 12) },
            "②" to { Cd14SleepClocks(fromHour = 12, toHour = 9) },
            "③" to { Cd14SleepClocks(fromHour = 12, toHour = 6) }
        )
    )
}

@Composable
private fun Cd14WorldClocks(tokyoHour: Int, londonHour: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawClockFace(Offset(size.width / 2f, size.height / 2f), size.minDimension * 0.45f, hour = tokyoHour)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("${tokyoHour}:00", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetAccentRed)
                Text("とうきょう", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetInkBlack)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawClockFace(Offset(size.width / 2f, size.height / 2f), size.minDimension * 0.45f, hour = londonHour)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("${londonHour}:00", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetAccentBlue)
                Text("ロンドン", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetInkBlack)
            }
        }
    }
}

@Composable
private fun Cd14SleepClocks(fromHour: Int, toHour: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(30.dp)) {
                    drawClockFace(Offset(size.width / 2f, size.height / 2f), size.minDimension * 0.45f, hour = fromHour)
                }
                Text("Zzz", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SheetAccentBlue)
                Text("${fromHour}:00", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
            }
            Text("～", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SheetAccentRed)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(30.dp)) {
                    drawClockFace(Offset(size.width / 2f, size.height / 2f), size.minDimension * 0.45f, hour = toHour)
                }
                Text("উঠা", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SheetAccentGreen)
                Text("${toHour}:00", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
            }
        }
    }
}

// ======================= CD 19 =======================
@Composable
private fun Cd19Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — কে কোথায় গিয়েছে",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd19TravelPanel(millerPlace = "→ なら", satoPlace = "→ きょうと") },
            "②" to { Cd19TravelPanel(millerPlace = "→ なら", satoPlace = "🏠 বাসায়") },
            "③" to { Cd19TravelPanel(millerPlace = "🏠 বাসায়", satoPlace = "→ なら") }
        )
    )

    VisualQuestionGroup(
        label = "2) — ক্যালেন্ডার ও তারিখ",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd19CalendarPanel(month = "4月", circledDay = "8", weekHeaders = listOf("日", "月", "火", "水")) },
            "②" to { Cd19CalendarPanel(month = "4月", circledDay = "4", weekHeaders = listOf("月", "火", "水", "金")) },
            "③" to { Cd19CalendarPanel(month = "7月", circledDay = "4", weekHeaders = listOf("日", "月", "火", "水")) }
        )
    )
}

@Composable
private fun Cd19TravelPanel(millerPlace: String, satoPlace: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                Text("ミラー", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetAccentAmber)
                Text(millerPlace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
            }
        }
        Surface(shape = RoundedCornerShape(6.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                Text("さとう", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetAccentGreen)
                Text(satoPlace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
            }
        }
    }
}

@Composable
private fun Cd19CalendarPanel(month: String, circledDay: String, weekHeaders: List<String>) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SheetSoftGray,
        border = BorderStroke(1.dp, SheetBorder),
        modifier = Modifier.fillMaxSize().padding(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(month, fontSize = 14.sp, fontWeight = FontWeight.Black, color = SheetInkBlack)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                weekHeaders.forEach { d ->
                    Text(d, fontSize = 8.sp, fontWeight = FontWeight.Medium, color = SheetInkMuted)
                }
            }
            Surface(
                shape = CircleShape,
                color = SheetAccentRed.copy(alpha = 0.14f),
                border = BorderStroke(1.8.dp, SheetAccentRed)
            ) {
                Text(
                    text = circledDay,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = SheetAccentRed,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

// ======================= CD 26 =======================
@Composable
private fun Cd26Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — কফি শপের দৃশ্য",
        onZoom = onZoom,
        options = listOf(
            "①" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("একসাথে কফি পান", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetAccentAmber)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.24f, size.height * 0.14f, size.height * 0.70f, longHair = true)
                        drawStickPerson(size.width * 0.76f, size.height * 0.14f, size.height * 0.70f)
                        drawOval(color = SheetBorder, topLeft = Offset(size.width * 0.25f, size.height * 0.56f), size = Size(size.width * 0.5f, size.height * 0.24f))
                        drawOval(color = SheetInkBlack, topLeft = Offset(size.width * 0.25f, size.height * 0.56f), size = Size(size.width * 0.5f, size.height * 0.24f), style = Stroke(2f))
                    }
                }
            },
            "②" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ট্রে-তে কফি আনা", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetAccentGreen)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.28f, size.height * 0.10f, size.height * 0.74f, longHair = true)
                        drawLine(color = SheetInkBlack, start = Offset(size.width * 0.42f, size.height * 0.50f), end = Offset(size.width * 0.62f, size.height * 0.50f), strokeWidth = 3f)
                        drawStickPerson(size.width * 0.78f, size.height * 0.18f, size.height * 0.68f)
                    }
                }
            },
            "③" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Surface(shape = RoundedCornerShape(4.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetInkBlack)) {
                        Text("Coffee\nShop", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack, textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                    }
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.38f, size.height * 0.12f, size.height * 0.78f, longHair = true)
                        drawStickPerson(size.width * 0.72f, size.height * 0.12f, size.height * 0.78f)
                    }
                }
            }
        )
    )

    VisualQuestionGroup(
        label = "2) — নেকটাই উপহার",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd26NecktiePanel(giver = "মহিলা", styleLabel = "ডোরা কাটা টাই", tieMode = 0) },
            "②" to { Cd26NecktiePanel(giver = "গোঁফওয়ালা", styleLabel = "ফোঁটা ডিজাইন টাই", tieMode = 1) },
            "③" to { Cd26NecktiePanel(giver = "যুবক →", styleLabel = "গাঢ় রঙের টাই", tieMode = 2) }
        )
    )
}

@Composable
private fun Cd26NecktiePanel(giver: String, styleLabel: String, tieMode: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            val tiePath = Path().apply {
                moveTo(w * 0.45f, h * 0.08f)
                lineTo(w * 0.55f, h * 0.08f)
                lineTo(w * 0.57f, h * 0.42f)
                lineTo(w * 0.50f, h * 0.50f)
                lineTo(w * 0.43f, h * 0.42f)
                close()
            }
            drawPath(
                path = tiePath,
                color = when (tieMode) {
                    0 -> Color(0xFFBFDBFE)
                    1 -> Color(0xFFFDE68A)
                    else -> Color(0xFF334155)
                }
            )
            drawPath(path = tiePath, color = SheetInkBlack, style = Stroke(width = 1.8f))
            drawStickPerson(w * 0.24f, h * 0.28f, h * 0.68f, mustache = (tieMode != 0), longHair = (tieMode == 0))
            drawStickPerson(w * 0.76f, h * 0.28f, h * 0.68f)
        }
        Text("$giver • $styleLabel", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = SheetInkMuted, textAlign = TextAlign.Center, maxLines = 2)
    }
}
