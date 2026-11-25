package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ======================= CD 30 =======================
@Composable
internal fun Cd30Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — শার্ট ও দাম",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd30ShirtCard(price = "3,500えん", isBlack = false, hasBowCollar = false) },
            "②" to { Cd30ShirtCard(price = "1,500えん", isBlack = false, hasBowCollar = true) },
            "③" to { Cd30ShirtCard(price = "3,500えん", isBlack = true, hasBowCollar = false) }
        )
    )

    VisualQuestionGroup(
        label = "2) — বাসায় অতিথি বরণ",
        onZoom = onZoom,
        options = listOf(
            "①" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("いらっしゃい！", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SheetAccentAmber)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.28f, size.height * 0.16f, size.height * 0.72f, briefcase = true)
                        drawStickPerson(size.width * 0.72f, size.height * 0.10f, size.height * 0.74f)
                    }
                }
            },
            "②" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("টেবিলে চা পান", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetAccentGreen)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.22f, size.height * 0.14f, size.height * 0.70f)
                        drawStickPerson(size.width * 0.50f, size.height * 0.08f, size.height * 0.64f)
                        drawStickPerson(size.width * 0.78f, size.height * 0.14f, size.height * 0.70f)
                    }
                }
            },
            "③" to {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("দরজায় স্বাগতম", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetAccentBlue)
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        drawStickPerson(size.width * 0.24f, size.height * 0.16f, size.height * 0.72f)
                        drawStickPerson(size.width * 0.62f, size.height * 0.10f, size.height * 0.74f)
                        drawStickPerson(size.width * 0.84f, size.height * 0.10f, size.height * 0.74f, longHair = true)
                    }
                }
            }
        )
    )
}

@Composable
private fun Cd30ShirtCard(price: String, isBlack: Boolean, hasBowCollar: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            val shirtFill = if (isBlack) Color(0xFF1F2937) else Color(0xFFFFFFFF)
            val outline = SheetInkBlack

            val body = Path().apply {
                moveTo(w * 0.34f, h * 0.12f)
                lineTo(w * 0.66f, h * 0.12f)
                lineTo(w * 0.84f, h * 0.30f)
                lineTo(w * 0.76f, h * 0.86f)
                lineTo(w * 0.24f, h * 0.86f)
                lineTo(w * 0.16f, h * 0.30f)
                close()
            }
            drawPath(body, color = shirtFill)
            drawPath(body, color = outline, style = Stroke(width = 2.2f))

            drawRect(
                color = Color.White,
                topLeft = Offset(w * 0.38f, h * 0.10f),
                size = Size(w * 0.24f, h * 0.10f)
            )
            drawRect(
                color = outline,
                topLeft = Offset(w * 0.38f, h * 0.10f),
                size = Size(w * 0.24f, h * 0.10f),
                style = Stroke(width = 1.5f)
            )
            if (hasBowCollar) {
                drawCircle(color = SheetAccentPink, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.24f))
            }
        }
        Text(
            text = price,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = SheetInkBlack
        )
    }
}

// ======================= CD 37 =======================
@Composable
internal fun Cd37Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — সিঁড়ি ও টয়লেটের অবস্থান",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd37StairsToilet(position = "সিঁড়ির পেছনে") },
            "②" to { Cd37StairsToilet(position = "সিঁড়ির বামে") },
            "③" to { Cd37StairsToilet(position = "সিঁড়ির ডানে") }
        )
    )

    VisualQuestionGroup(
        label = "2) — ফোন ও ফ্যাক্স কোথায় আছে",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd37DeskScene(mode = 0) },
            "②" to { Cd37DeskScene(mode = 1) },
            "③" to { Cd37DeskScene(mode = 2) }
        )
    )

    VisualQuestionGroup(
        label = "3) — স্টেশন (えき) ও ফোন",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd37StationScene(label = "えき-র নিচে ফোন", tag = "えき") },
            "②" to { Cd37StationScene(label = "বইয়ের দোকানের পাশে", tag = "本 • えき") },
            "③" to { Cd37StationScene(label = "えき → さとう-এর বাসা", tag = "えき → さとう") }
        )
    )
}

@Composable
private fun Cd37StairsToilet(position: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetAccentBlue)) {
            Text("トイレ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SheetAccentBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
        }
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            for (s in 0..3) {
                val inset = s * w * 0.06f
                drawRect(
                    color = SheetSoftGray,
                    topLeft = Offset(w * 0.25f + inset, h * (0.70f - s * 0.16f)),
                    size = Size(w * 0.50f - inset * 2f, h * 0.14f)
                )
                drawRect(
                    color = SheetInkBlack,
                    topLeft = Offset(w * 0.25f + inset, h * (0.70f - s * 0.16f)),
                    size = Size(w * 0.50f - inset * 2f, h * 0.14f),
                    style = Stroke(width = 1.6f)
                )
            }
        }
        Text(position, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetAccentAmber, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Cd37DeskScene(mode: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(3.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Surface(shape = RoundedCornerShape(4.dp), color = SheetSoftGray, border = BorderStroke(0.8.dp, SheetBorder)) {
                Text(
                    text = if (mode == 2) "FAX ⬆" else "ড্রয়ার",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (mode == 2) SheetAccentRed else SheetInkMuted,
                    modifier = Modifier.padding(3.dp)
                )
            }
            Surface(shape = RoundedCornerShape(4.dp), color = SheetSoftGray, border = BorderStroke(0.8.dp, SheetBorder)) {
                Text(
                    text = when (mode) {
                        0 -> "☎  |  FAX"
                        1 -> "FAX  |  ☎"
                        else -> "টেবিলে ☎"
                    },
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SheetAccentGreen,
                    modifier = Modifier.padding(3.dp)
                )
            }
        }
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            drawRect(color = SheetSoftGray, topLeft = Offset(w * 0.08f, h * 0.25f), size = Size(w * 0.30f, h * 0.65f))
            drawRect(color = SheetInkBlack, topLeft = Offset(w * 0.08f, h * 0.25f), size = Size(w * 0.30f, h * 0.65f), style = Stroke(width = 2f))
            drawLine(color = SheetInkBlack, start = Offset(w * 0.40f, h * 0.56f), end = Offset(w * 0.94f, h * 0.56f), strokeWidth = 3f)
        }
        Text(
            text = when (mode) {
                0 -> "ফোনের ডানে ফ্যাক্স"
                1 -> "ফোনের বামে ফ্যাক্স"
                else -> "ড্রয়ারের উপরে ফ্যাক্স"
            },
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = SheetAccentAmber,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Cd37StationScene(label: String, tag: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetInkBlack)) {
            Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
        }
        Text(label, fontSize = 8.5.sp, fontWeight = FontWeight.Medium, color = SheetInkMuted, textAlign = TextAlign.Center)
    }
}

// ======================= CD 41 =======================
@Composable
internal fun Cd41Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — পরিবারের সদস্য",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd41FamilyPanel(childrenText = "২ ছেলে", note = "বাবা + মা") },
            "②" to { Cd41FamilyPanel(childrenText = "১ ছেলে • ১ মেয়ে", note = "বাবা + মা") },
            "③" to { Cd41FamilyPanel(childrenText = "১ মেয়ে", note = "দম্পতি") }
        )
    )

    VisualQuestionGroup(
        label = "2) — কফি ও আইসক্রিম অর্ডার",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd41OrderPanel(coffees = 2, iceCreams = 2) },
            "②" to { Cd41OrderPanel(coffees = 2, iceCreams = 1) },
            "③" to { Cd41OrderPanel(coffees = 1, iceCreams = 2) }
        )
    )
}

@Composable
private fun Cd41FamilyPanel(childrenText: String, note: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(note, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = SheetInkMuted)
        Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
            val w = size.width
            val h = size.height
            drawCircle(color = SheetInkBlack, radius = 9f, center = Offset(w * 0.30f, h * 0.30f), style = Stroke(2f))
            drawCircle(color = SheetInkBlack, radius = 9f, center = Offset(w * 0.70f, h * 0.30f), style = Stroke(2f))
            drawLine(color = SheetAccentRed, start = Offset(w * 0.39f, h * 0.30f), end = Offset(w * 0.61f, h * 0.30f), strokeWidth = 2.2f)
            drawLine(color = SheetAccentRed, start = Offset(w * 0.50f, h * 0.30f), end = Offset(w * 0.50f, h * 0.85f), strokeWidth = 2.2f)
        }
        Surface(shape = RoundedCornerShape(6.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetAccentGreen)) {
            Text(childrenText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SheetAccentGreen, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
        }
    }
}

@Composable
private fun Cd41OrderPanel(coffees: Int, iceCreams: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "☕".repeat(coffees),
            fontSize = 18.sp
        )
        Text(
            text = "🍨".repeat(iceCreams),
            fontSize = 18.sp
        )
        Text(
            text = "কফি $coffees • আইসক্রিম $iceCreams",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            textAlign = TextAlign.Center
        )
    }
}

// ======================= CD 51 =======================
@Composable
internal fun Cd51Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — কপি মেশিন (2F / 1F)",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd51FloorPanel(floor = "2F", action = "কপি করছেন") },
            "②" to { Cd51FloorPanel(floor = "1F", action = "কপি করছেন") },
            "③" to { Cd51FloorPanel(floor = "1F", action = "কফি খাচ্ছেন") }
        )
    )

    VisualQuestionGroup(
        label = "2) — রাস্তার মোড় ও দিক",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd51IntersectionPanel(turnRight = false, tallBuilding = false) },
            "②" to { Cd51IntersectionPanel(turnRight = true, tallBuilding = true) },
            "③" to { Cd51IntersectionPanel(turnRight = false, tallBuilding = true) }
        )
    )
}

@Composable
private fun Cd51FloorPanel(floor: String, action: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = SheetSoftGray, border = BorderStroke(1.2.dp, SheetInkBlack)) {
            Text(floor, fontSize = 14.sp, fontWeight = FontWeight.Black, color = SheetInkBlack, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
        }
        Text(action, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = SheetInkMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Cd51IntersectionPanel(turnRight: Boolean, tallBuilding: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            drawRect(color = Color.White, size = Size(w, h))
            drawRect(color = Color(0xFFE5E7EB), topLeft = Offset(w * 0.36f, 0f), size = Size(w * 0.28f, h))
            drawRect(color = Color(0xFFE5E7EB), topLeft = Offset(0f, h * 0.36f), size = Size(w, h * 0.28f))

            val path = Path().apply {
                moveTo(w * 0.50f, h * 0.88f)
                lineTo(w * 0.50f, h * 0.50f)
                lineTo(if (turnRight) w * 0.86f else w * 0.14f, h * 0.50f)
            }
            drawPath(
                path = path,
                color = SheetAccentRed,
                style = Stroke(width = 3.2f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
            )

            val bx = if (turnRight) w * 0.70f else w * 0.10f
            val bh = if (tallBuilding) h * 0.28f else h * 0.16f
            drawRect(color = SheetAccentGreen, topLeft = Offset(bx, h * 0.32f - bh), size = Size(w * 0.20f, bh))
            drawRect(color = SheetInkBlack, topLeft = Offset(bx, h * 0.32f - bh), size = Size(w * 0.20f, bh), style = Stroke(1.5f))
        }
        Text(
            text = "${if (turnRight) "ডানে →" else "← বামে"} (${if (tallBuilding) "বড় ভবন" else "ছোট ঘর"})",
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = SheetInkMuted,
            textAlign = TextAlign.Center
        )
    }
}

// ======================= CD 58 =======================
@Composable
internal fun Cd58Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — যাতায়াতের মাধ্যম",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd58CommutePanel(leg1 = "JR", leg2 = "ちかてつ + হাঁটা") },
            "②" to { Cd58CommutePanel(leg1 = "JR", leg2 = "バス + হাঁটা") },
            "③" to { Cd58CommutePanel(leg1 = "হাঁটা", leg2 = "ちかてつ + সাইকেল") }
        )
    )

    VisualQuestionGroup(
        label = "2) — মেয়েটির চেহারা ও চুল",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd58GirlPanel(tall = false, longHair = false, darkDress = true, desc = "খাটো • ছোট চুল") },
            "②" to { Cd58GirlPanel(tall = true, longHair = true, darkDress = false, desc = "লম্বা • লম্বা চুল") },
            "③" to { Cd58GirlPanel(tall = true, longHair = false, darkDress = true, desc = "লম্বা • ছোট চুল") }
        )
    )
}

@Composable
private fun Cd58CommutePanel(leg1: String, leg2: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("うち → $leg1", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SheetAccentAmber, textAlign = TextAlign.Center)
        Surface(shape = RoundedCornerShape(4.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetAccentBlue)) {
            Text("おおさか", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SheetAccentBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Text("→ $leg2 → かいしゃ", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = SheetAccentGreen, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Cd58GirlPanel(tall: Boolean, longHair: Boolean, darkDress: Boolean, desc: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val hRatio = if (tall) 0.84f else 0.56f
            drawStickPerson(
                centerX = size.width * 0.50f,
                topY = size.height * (1f - hRatio) - 4f,
                height = size.height * hRatio,
                longHair = longHair,
                darkDress = darkDress
            )
        }
        Text(desc, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = SheetInkBlack, textAlign = TextAlign.Center)
    }
}

// ======================= CD 80 =======================
@Composable
internal fun Cd80Visuals(onZoom: (String, @Composable () -> Unit) -> Unit) {
    VisualQuestionGroup(
        label = "1) — আন্তর্জাতিক ফোন নম্বর",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd80PhoneKeypadPanel(codeTop = "1-001", codeBottom = "-XXXX", pressTopLeft = true) },
            "②" to { Cd80PhoneKeypadPanel(codeTop = "001-1", codeBottom = "-XXXX", pressTopLeft = false) },
            "③" to { Cd80PhoneKeypadPanel(codeTop = "001-XXXX", codeBottom = "-1", pressTopLeft = false) }
        )
    )

    VisualQuestionGroup(
        label = "2) — সেতু ও ১০০ মিটার ম্যাপ",
        onZoom = onZoom,
        options = listOf(
            "①" to { Cd80MapPanel(sideRoadLeft = false, blockRight = false, caption = "সেতুর পর বামে 100m") },
            "②" to { Cd80MapPanel(sideRoadLeft = true, blockRight = false, caption = "বামের রাস্তায় 100m") },
            "③" to { Cd80MapPanel(sideRoadLeft = false, blockRight = true, caption = "সেতুর পর ডানে 100m") }
        )
    )
}

@Composable
private fun Cd80PhoneKeypadPanel(codeTop: String, codeBottom: String, pressTopLeft: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Canvas(modifier = Modifier.size(44.dp, 32.dp)) {
            val cols = 3
            val rows = 4
            val dx = size.width / cols
            val dy = size.height / rows
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val isPressed = if (pressTopLeft) (r == 0 && c == 0) else (r == 3 && c == 1)
                    drawCircle(
                        color = if (isPressed) SheetAccentRed else SheetBorder,
                        radius = 3.8f,
                        center = Offset(dx * (c + 0.5f), dy * (r + 0.5f))
                    )
                    drawCircle(
                        color = SheetInkBlack,
                        radius = 3.8f,
                        center = Offset(dx * (c + 0.5f), dy * (r + 0.5f)),
                        style = Stroke(1f)
                    )
                }
            }
        }
        Surface(shape = RoundedCornerShape(6.dp), color = SheetSoftGray, border = BorderStroke(1.dp, SheetBorder)) {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(codeTop, fontSize = 10.sp, fontWeight = FontWeight.Black, color = SheetAccentRed)
                Text(codeBottom, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack)
            }
        }
    }
}

@Composable
private fun Cd80MapPanel(sideRoadLeft: Boolean, blockRight: Boolean, caption: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            drawRect(color = Color.White, size = Size(w, h))
            // River
            drawRect(color = Color(0xFFBFDBFE), topLeft = Offset(0f, h * 0.42f), size = Size(w, h * 0.16f))
            // Rail track bottom
            drawLine(color = SheetInkBlack, start = Offset(0f, h * 0.80f), end = Offset(w, h * 0.80f), strokeWidth = 3.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
            // Vertical road
            drawRect(color = Color(0xFFE5E7EB), topLeft = Offset(w * 0.42f, 0f), size = Size(w * 0.16f, h))
            drawLine(color = SheetInkBlack, start = Offset(w * 0.42f, 0f), end = Offset(w * 0.42f, h), strokeWidth = 1.4f)
            drawLine(color = SheetInkBlack, start = Offset(w * 0.58f, 0f), end = Offset(w * 0.58f, h), strokeWidth = 1.4f)
            if (sideRoadLeft) {
                drawRect(color = Color(0xFFE5E7EB), topLeft = Offset(0f, h * 0.16f), size = Size(w * 0.42f, h * 0.12f))
                drawLine(color = SheetInkBlack, start = Offset(0f, h * 0.16f), end = Offset(w * 0.42f, h * 0.16f), strokeWidth = 1.4f)
                drawLine(color = SheetInkBlack, start = Offset(0f, h * 0.28f), end = Offset(w * 0.42f, h * 0.28f), strokeWidth = 1.4f)
            }
            // Black building square (■)
            val bx = if (blockRight) w * 0.64f else w * 0.22f
            drawRect(color = SheetInkBlack, topLeft = Offset(bx, h * 0.04f), size = Size(w * 0.14f, h * 0.14f))
        }
        Text(caption, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SheetInkBlack, textAlign = TextAlign.Center)
    }
}
