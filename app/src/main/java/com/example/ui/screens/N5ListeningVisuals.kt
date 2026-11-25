package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R

internal fun detectN5QuestionVisualKey(speakerLabel: String): String? {
    if (!speakerLabel.contains("（しつもん）")) return null
    return when {
        speakerLabel.contains("もんだい 1 • 1ばん") -> "m1_q1"
        speakerLabel.contains("もんだい 1 • 2ばん") -> "m1_q2"
        speakerLabel.contains("もんだい 1 • 3ばん") -> "m1_q3"
        speakerLabel.contains("もんだい 1 • 4ばん") -> "m1_q4"
        speakerLabel.contains("もんだい 1 • 5ばん") -> "m1_q5"
        speakerLabel.contains("もんだい 2 • 1ばん") -> "m2_q1"
        speakerLabel.contains("もんだい 3 • 1ばん") -> "m3_q1"
        speakerLabel.contains("もんだい 3 • 2ばん") -> "m3_q2"
        speakerLabel.contains("もんだい 3 • 3ばん") -> "m3_q3"
        else -> null
    }
}

private data class N5VisualOption(
    val number: Int,
    val content: @Composable () -> Unit
)

private data class N5VisualSpec(
    val title: String,
    val correctNumber: Int,
    val options: List<N5VisualOption>
)

@Composable
fun N5QuestionVisualCard(
    questionKey: String,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (questionKey == "m1_q1") {
        N5ImageWorksheetCard(
            title = "もんだい 1 • 1ばん（きっさてん）",
            drawableResId = R.drawable.n5_m1_q1,
            optionCount = 4,
            correctNumber = 4,
            selectedOption = selectedOption,
            onSelectOption = onSelectOption,
            modifier = modifier
        )
        return
    }

    val spec = remember(questionKey) { getN5VisualSpec(questionKey) } ?: return
    var zoomedOption by remember { mutableStateOf<N5VisualOption?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SheetWhiteBg,
        border = BorderStroke(1.5.dp, SheetBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = SheetAccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = spec.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SheetInkBlack
                    )
                }
                Text(
                    text = if (selectedOption != null) "সিলেক্ট: $selectedOption" else "ট্যাপ করে বাছাই করুন",
                    fontSize = 10.5.sp,
                    fontWeight = if (selectedOption != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedOption != null) SheetAccentRed else SheetInkMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spec.options.forEach { opt ->
                    val isSelected = selectedOption == opt.number
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.88f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectOption(opt.number) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFFFF1F2) else SheetWhiteBg,
                        border = BorderStroke(
                            width = if (isSelected) 2.2.dp else 1.2.dp,
                            color = if (isSelected) SheetAccentRed else SheetBorder
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                opt.content()
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) SheetAccentRed else SheetSoftGray,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) SheetAccentRed else SheetBorder
                                    )
                                ) {
                                    Text(
                                        text = "${opt.number}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else SheetInkBlack,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .clickable { zoomedOption = opt },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Zoom option ${opt.number}",
                                        tint = SheetInkMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectedOption != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val isCorrect = selectedOption == spec.correctNumber
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    border = BorderStroke(
                        1.dp,
                        if (isCorrect) SheetAccentGreen.copy(alpha = 0.45f) else SheetAccentRed.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isCorrect) SheetAccentGreen else SheetAccentRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCorrect) "সঠিক বাছাই! (${optNumberText(selectedOption)})" else "আবার শুনে চেষ্টা করুন (${optNumberText(selectedOption)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCorrect) SheetAccentGreen else SheetAccentRed
                        )
                    }
                }
            }
        }
    }

    zoomedOption?.let { opt ->
        Dialog(
            onDismissRequest = { zoomedOption = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
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
                            text = "${spec.title} • অপশন ${opt.number}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SheetInkBlack
                        )
                        IconButton(onClick = { zoomedOption = null }) {
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
                            .aspectRatio(1.1f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            opt.content()
                        }
                    }
                }
            }
        }
    }
}

private fun optNumberText(num: Int): String = "$num"

@Composable
private fun N5ImageWorksheetCard(
    title: String,
    drawableResId: Int,
    optionCount: Int,
    correctNumber: Int,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showZoomDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SheetWhiteBg,
        border = BorderStroke(1.5.dp, SheetBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = SheetAccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SheetInkBlack
                    )
                }
                Text(
                    text = if (selectedOption != null) "সিলেক্ট: $selectedOption" else "ট্যাপ করে বড় দেখুন",
                    fontSize = 10.5.sp,
                    fontWeight = if (selectedOption != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedOption != null) SheetAccentRed else SheetInkMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showZoomDialog = true }
            ) {
                Image(
                    painter = painterResource(id = drawableResId),
                    contentDescription = title,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
                Surface(
                    shape = CircleShape,
                    color = SheetWhiteBg.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, SheetBorder),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom worksheet image",
                        tint = SheetInkBlack,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (num in 1..optionCount) {
                    val isSelected = selectedOption == num
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectOption(num) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SheetAccentRed else SheetSoftGray,
                        border = BorderStroke(
                            width = if (isSelected) 1.8.dp else 1.dp,
                            color = if (isSelected) SheetAccentRed else SheetBorder
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$num",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else SheetInkBlack
                            )
                        }
                    }
                }
            }

            if (selectedOption != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val isCorrect = selectedOption == correctNumber
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    border = BorderStroke(
                        1.dp,
                        if (isCorrect) SheetAccentGreen.copy(alpha = 0.45f) else SheetAccentRed.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isCorrect) SheetAccentGreen else SheetAccentRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCorrect) "সঠিক বাছাই! ($selectedOption)" else "আবার শুনে চেষ্টা করুন ($selectedOption)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCorrect) SheetAccentGreen else SheetAccentRed
                        )
                    }
                }
            }
        }
    }

    if (showZoomDialog) {
        Dialog(
            onDismissRequest = { showZoomDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(20.dp),
                color = SheetWhiteBg,
                border = BorderStroke(1.5.dp, SheetBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SheetInkBlack
                        )
                        IconButton(onClick = { showZoomDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SheetInkBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = title,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

private fun getN5VisualSpec(key: String): N5VisualSpec? = when (key) {
    "m1_q1" -> N5VisualSpec(
        title = "もんだい 1 • 1ばん（きっさてん）",
        correctNumber = 4,
        options = listOf(
            N5VisualOption(1) { DrinkPanel(isJuice = true, isCold = true, label = "ジュース") },
            N5VisualOption(2) { DrinkPanel(isJuice = false, isCold = true, label = "アイス") },
            N5VisualOption(3) { DrinkPanel(isJuice = false, isCold = true, isTea = true, label = "アイスティー") },
            N5VisualOption(4) { DrinkPanel(isJuice = false, isCold = false, label = "あたたかい") }
        )
    )
    "m1_q2" -> N5VisualSpec(
        title = "もんだい 1 • 2ばん（きって・はがき）",
        correctNumber = 2,
        options = listOf(
            N5VisualOption(1) { PriceReceiptPanel("820 えん") },
            N5VisualOption(2) { PriceReceiptPanel("872 えん") },
            N5VisualOption(3) { PriceReceiptPanel("902 えん") },
            N5VisualOption(4) { PriceReceiptPanel("1340 えん") }
        )
    )
    "m1_q3" -> N5VisualSpec(
        title = "もんだい 1 • 3ばん（カバン）",
        correctNumber = 4,
        options = listOf(
            N5VisualOption(1) { BagPanel(isBlack = false, isLarge = true) },
            N5VisualOption(2) { BagPanel(isBlack = false, isLarge = false) },
            N5VisualOption(3) { BagPanel(isBlack = true, isLarge = true) },
            N5VisualOption(4) { BagPanel(isBlack = true, isLarge = false) }
        )
    )
    "m1_q4" -> N5VisualSpec(
        title = "もんだい 1 • 4ばん（いもうと）",
        correctNumber = 2,
        options = listOf(
            N5VisualOption(1) { SisterPortraitPanel(longHair = true, hasGlasses = false) },
            N5VisualOption(2) { SisterPortraitPanel(longHair = false, hasGlasses = true) },
            N5VisualOption(3) { SisterPortraitPanel(longHair = true, hasGlasses = true) },
            N5VisualOption(4) { SisterPortraitPanel(longHair = false, hasGlasses = false) }
        )
    )
    "m1_q5" -> N5VisualSpec(
        title = "もんだい 1 • 5ばん（パーティーの へや）",
        correctNumber = 2,
        options = listOf(
            N5VisualOption(1) { PartyTablePanel(hasCake = false, hasCups = false, hasFlower = false) },
            N5VisualOption(2) { PartyTablePanel(hasCake = true, hasCups = false, hasFlower = false) },
            N5VisualOption(3) { PartyTablePanel(hasCake = true, hasCups = true, hasFlower = false) },
            N5VisualOption(4) { PartyTablePanel(hasCake = true, hasCups = true, hasFlower = true) }
        )
    )
    "m2_q1" -> N5VisualSpec(
        title = "もんだい 2 • 1ばん（よるの あいさつ）",
        correctNumber = 2,
        options = listOf(
            N5VisualOption(1) { HallwayGreetingPanel(phrase = "おやすみ", moon = true) },
            N5VisualOption(2) { HallwayGreetingPanel(phrase = "こんばんは", moon = true) },
            N5VisualOption(3) { HallwayGreetingPanel(phrase = "おつかれ", moon = false) }
        )
    )
    "m3_q1" -> N5VisualSpec(
        title = "もんだい 3 • 1ばん（にもつ）",
        correctNumber = 2,
        options = listOf(
            N5VisualOption(1) { CarryingBoxesPanel(mode = 1) },
            N5VisualOption(2) { CarryingBoxesPanel(mode = 2) },
            N5VisualOption(3) { CarryingBoxesPanel(mode = 3) }
        )
    )
    "m3_q2" -> N5VisualSpec(
        title = "もんだい 3 • 2ばん（せんせいの へや）",
        correctNumber = 1,
        options = listOf(
            N5VisualOption(1) { EnteringRoomPanel(phrase = "しつれいします") },
            N5VisualOption(2) { EnteringRoomPanel(phrase = "こんにちは") },
            N5VisualOption(3) { EnteringRoomPanel(phrase = "おじゃまします") }
        )
    )
    "m3_q3" -> N5VisualSpec(
        title = "もんだい 3 • 3ばん（おみまい）",
        correctNumber = 2,
        options = listOf(
            N5VisualOption(1) { HospitalVisitPanel(phrase = "さようなら") },
            N5VisualOption(2) { HospitalVisitPanel(phrase = "おだいじに") },
            N5VisualOption(3) { HospitalVisitPanel(phrase = "だいじょうぶ?") }
        )
    )
    else -> null
}

@Composable
private fun DrinkPanel(
    isJuice: Boolean,
    isCold: Boolean,
    isTea: Boolean = false,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(46.dp)) {
            val w = size.width
            val h = size.height
            if (isCold) {
                // Tall glass with straw and ice cubes
                drawLine(SheetAccentRed, Offset(w * 0.62f, h * 0.08f), Offset(w * 0.48f, h * 0.55f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawRect(
                    color = when {
                        isJuice -> Color(0xFFF59E0B)
                        isTea -> Color(0xFFB45309)
                        else -> Color(0xFF451A03)
                    },
                    topLeft = Offset(w * 0.28f, h * 0.36f),
                    size = Size(w * 0.44f, h * 0.50f)
                )
                drawRect(
                    color = SheetInkBlack,
                    topLeft = Offset(w * 0.26f, h * 0.26f),
                    size = Size(w * 0.48f, h * 0.62f),
                    style = Stroke(width = 2.2f)
                )
                // Ice cubes
                drawRect(Color.White.copy(alpha = 0.8f), Offset(w * 0.33f, h * 0.40f), Size(w * 0.14f, w * 0.14f))
                drawRect(Color.White.copy(alpha = 0.8f), Offset(w * 0.50f, h * 0.44f), Size(w * 0.13f, w * 0.13f))
            } else {
                // Warm steaming mug + saucer
                val steamStroke = Stroke(width = 2f, cap = StrokeCap.Round)
                drawPath(
                    Path().apply {
                        moveTo(w * 0.35f, h * 0.34f)
                        cubicTo(w * 0.30f, h * 0.24f, w * 0.42f, h * 0.18f, w * 0.36f, h * 0.08f)
                        moveTo(w * 0.52f, h * 0.34f)
                        cubicTo(w * 0.47f, h * 0.22f, w * 0.58f, h * 0.16f, w * 0.53f, h * 0.08f)
                    },
                    color = SheetAccentRed,
                    style = steamStroke
                )
                drawRoundRect(
                    color = Color(0xFF78350F),
                    topLeft = Offset(w * 0.24f, h * 0.42f),
                    size = Size(w * 0.46f, h * 0.36f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawArc(
                    color = SheetInkBlack,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.64f, h * 0.47f),
                    size = Size(w * 0.18f, h * 0.24f),
                    style = Stroke(width = 2.4f)
                )
                drawLine(SheetInkBlack, Offset(w * 0.16f, h * 0.82f), Offset(w * 0.82f, h * 0.82f), strokeWidth = 2.6f, cap = StrokeCap.Round)
            }
        }
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            maxLines = 1
        )
    }
}

@Composable
private fun PriceReceiptPanel(priceText: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(34.dp)) {
            val w = size.width
            val h = size.height
            // Stamp + postcard sketch
            drawRect(Color(0xFFDBEAFE), Offset(w * 0.10f, h * 0.18f), Size(w * 0.36f, h * 0.42f))
            drawRect(SheetInkBlack, Offset(w * 0.10f, h * 0.18f), Size(w * 0.36f, h * 0.42f), style = Stroke(2f))
            drawRect(Color(0xFFFEF3C7), Offset(w * 0.44f, h * 0.38f), Size(w * 0.46f, h * 0.46f))
            drawRect(SheetInkBlack, Offset(w * 0.44f, h * 0.38f), Size(w * 0.46f, h * 0.46f), style = Stroke(2f))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = priceText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BagPanel(isBlack: Boolean, isLarge: Boolean) {
    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val scale = if (isLarge) 1f else 0.64f
        val bw = w * 0.62f * scale
        val bh = h * 0.50f * scale
        val left = (w - bw) / 2f
        val top = h * (if (isLarge) 0.34f else 0.46f)

        // Handle
        drawArc(
            color = SheetInkBlack,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(left + bw * 0.22f, top - bh * 0.32f),
            size = Size(bw * 0.56f, bh * 0.56f),
            style = Stroke(width = 2.6f)
        )
        // Body
        drawRoundRect(
            color = if (isBlack) SheetInkBlack else Color.White,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = SheetInkBlack,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2.4f)
        )
    }
}

@Composable
private fun SisterPortraitPanel(longHair: Boolean, hasGlasses: Boolean) {
    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val headY = h * 0.38f
        val r = w * 0.22f

        // Hair silhouette
        if (longHair) {
            drawRoundRect(
                color = SheetInkBlack,
                topLeft = Offset(cx - r * 1.25f, headY - r * 1.15f),
                size = Size(r * 2.5f, h * 0.56f),
                cornerRadius = CornerRadius(14f, 14f)
            )
        } else {
            drawCircle(
                color = SheetInkBlack,
                radius = r * 1.18f,
                center = Offset(cx, headY - r * 0.1f)
            )
        }

        // Face
        drawCircle(
            color = Color(0xFFFFEDD5),
            radius = r,
            center = Offset(cx, headY)
        )
        drawCircle(
            color = SheetInkBlack,
            radius = r,
            center = Offset(cx, headY),
            style = Stroke(1.8f)
        )

        // Eyes / Glasses
        val eyeY = headY - r * 0.05f
        if (hasGlasses) {
            drawCircle(SheetInkBlack, radius = r * 0.28f, center = Offset(cx - r * 0.36f, eyeY), style = Stroke(2f))
            drawCircle(SheetInkBlack, radius = r * 0.28f, center = Offset(cx + r * 0.36f, eyeY), style = Stroke(2f))
            drawLine(SheetInkBlack, Offset(cx - r * 0.08f, eyeY), Offset(cx + r * 0.08f, eyeY), strokeWidth = 2f)
        } else {
            drawCircle(SheetInkBlack, radius = 2.2f, center = Offset(cx - r * 0.34f, eyeY))
            drawCircle(SheetInkBlack, radius = 2.2f, center = Offset(cx + r * 0.34f, eyeY))
        }

        // Smile & shoulders
        drawArc(
            color = SheetInkBlack,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.3f, headY + r * 0.25f),
            size = Size(r * 0.6f, r * 0.35f),
            style = Stroke(1.8f)
        )
        drawArc(
            color = SheetAccentBlue,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - w * 0.28f, h * 0.66f),
            size = Size(w * 0.56f, h * 0.34f)
        )
    }
}

@Composable
private fun PartyTablePanel(hasCake: Boolean, hasCups: Boolean, hasFlower: Boolean) {
    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val tableY = h * 0.66f
        // Table
        drawLine(SheetInkBlack, Offset(w * 0.10f, tableY), Offset(w * 0.90f, tableY), strokeWidth = 3f)
        drawLine(SheetInkBlack, Offset(w * 0.20f, tableY), Offset(w * 0.20f, h * 0.90f), strokeWidth = 2.4f)
        drawLine(SheetInkBlack, Offset(w * 0.80f, tableY), Offset(w * 0.80f, h * 0.90f), strokeWidth = 2.4f)

        if (hasCake) {
            // Cake in middle
            drawRect(Color(0xFFFDE68A), Offset(w * 0.35f, tableY - h * 0.22f), Size(w * 0.28f, h * 0.22f))
            drawRect(SheetInkBlack, Offset(w * 0.35f, tableY - h * 0.22f), Size(w * 0.28f, h * 0.22f), style = Stroke(1.8f))
            drawLine(SheetAccentRed, Offset(w * 0.49f, tableY - h * 0.22f), Offset(w * 0.49f, tableY - h * 0.36f), strokeWidth = 2.2f)
        }
        if (hasCups) {
            drawRect(Color(0xFFBAE6FD), Offset(w * 0.14f, tableY - h * 0.18f), Size(w * 0.13f, h * 0.18f))
            drawRect(SheetInkBlack, Offset(w * 0.14f, tableY - h * 0.18f), Size(w * 0.13f, h * 0.18f), style = Stroke(1.6f))
        }
        if (hasFlower) {
            drawRect(Color(0xFFA7F3D0), Offset(w * 0.71f, tableY - h * 0.18f), Size(w * 0.12f, h * 0.18f))
            drawCircle(SheetAccentPink, radius = 4.5f, center = Offset(w * 0.77f, tableY - h * 0.28f))
        }
    }
}

@Composable
private fun HallwayGreetingPanel(phrase: String, moon: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(38.dp)) {
            val w = size.width
            val h = size.height
            if (moon) {
                drawCircle(Color(0xFFFACC15), radius = 6f, center = Offset(w * 0.80f, h * 0.22f))
            }
            // Two small figures greeting
            drawCircle(SheetInkBlack, radius = 5f, center = Offset(w * 0.30f, h * 0.44f), style = Stroke(2f))
            drawLine(SheetInkBlack, Offset(w * 0.30f, h * 0.56f), Offset(w * 0.30f, h * 0.86f), strokeWidth = 2.2f)
            drawCircle(SheetInkBlack, radius = 5f, center = Offset(w * 0.70f, h * 0.44f), style = Stroke(2f))
            drawLine(SheetInkBlack, Offset(w * 0.70f, h * 0.56f), Offset(w * 0.70f, h * 0.86f), strokeWidth = 2.2f)
        }
        Text(
            text = phrase,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun CarryingBoxesPanel(mode: Int) {
    val caption = when (mode) {
        1 -> "もちませんか"
        2 -> "もちましょうか"
        else -> "もってください"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(38.dp)) {
            val w = size.width
            val h = size.height
            // Friend with stacked boxes
            drawCircle(SheetInkBlack, radius = 4.5f, center = Offset(w * 0.26f, h * 0.36f), style = Stroke(1.8f))
            drawLine(SheetInkBlack, Offset(w * 0.26f, h * 0.48f), Offset(w * 0.26f, h * 0.82f), strokeWidth = 2f)
            drawRect(Color(0xFFFDE68A), Offset(w * 0.36f, h * 0.44f), Size(w * 0.20f, h * 0.16f))
            drawRect(SheetInkBlack, Offset(w * 0.36f, h * 0.44f), Size(w * 0.20f, h * 0.16f), style = Stroke(1.6f))
            drawRect(Color(0xFFFDE68A), Offset(w * 0.36f, h * 0.26f), Size(w * 0.20f, h * 0.16f))
            drawRect(SheetInkBlack, Offset(w * 0.36f, h * 0.26f), Size(w * 0.20f, h * 0.16f), style = Stroke(1.6f))

            // Second person + arrow indicator
            drawCircle(
                color = if (mode == 2) SheetAccentRed else SheetInkBlack,
                radius = 4.5f,
                center = Offset(w * 0.76f, h * 0.42f),
                style = Stroke(2f)
            )
            drawLine(
                color = if (mode == 2) SheetAccentRed else SheetInkBlack,
                start = Offset(w * 0.76f, h * 0.54f),
                end = Offset(w * 0.76f, h * 0.84f),
                strokeWidth = 2.2f
            )
            if (mode == 2) {
                // Reaching hands offering to carry one
                drawLine(SheetAccentRed, Offset(w * 0.76f, h * 0.62f), Offset(w * 0.58f, h * 0.46f), strokeWidth = 2f)
            }
        }
        Text(
            text = caption,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            maxLines = 1
        )
    }
}

@Composable
private fun EnteringRoomPanel(phrase: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(38.dp)) {
            val w = size.width
            val h = size.height
            // Doorframe
            drawRect(SheetSoftGray, Offset(w * 0.18f, h * 0.16f), Size(w * 0.42f, h * 0.70f))
            drawRect(SheetInkBlack, Offset(w * 0.18f, h * 0.16f), Size(w * 0.42f, h * 0.70f), style = Stroke(2f))
            // Student bowing at door
            drawCircle(SheetInkBlack, radius = 4.5f, center = Offset(w * 0.72f, h * 0.46f), style = Stroke(2f))
            drawLine(SheetInkBlack, Offset(w * 0.72f, h * 0.58f), Offset(w * 0.76f, h * 0.86f), strokeWidth = 2.2f)
        }
        Text(
            text = phrase,
            fontSize = 7.8.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            maxLines = 1
        )
    }
}

@Composable
private fun HospitalVisitPanel(phrase: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(38.dp)) {
            val w = size.width
            val h = size.height
            // Bed & sick friend
            drawLine(SheetInkBlack, Offset(w * 0.10f, h * 0.72f), Offset(w * 0.55f, h * 0.72f), strokeWidth = 2.4f)
            drawCircle(SheetInkBlack, radius = 4f, center = Offset(w * 0.22f, h * 0.58f), style = Stroke(1.8f))
            // Visitor leaving & bowing
            drawCircle(SheetAccentRed, radius = 4.5f, center = Offset(w * 0.76f, h * 0.46f), style = Stroke(2f))
            drawLine(SheetAccentRed, Offset(w * 0.76f, h * 0.58f), Offset(w * 0.76f, h * 0.84f), strokeWidth = 2.2f)
        }
        Text(
            text = phrase,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = SheetInkBlack,
            maxLines = 1
        )
    }
}
