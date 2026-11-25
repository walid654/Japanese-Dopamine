package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*

enum class ScreenDestination {
    DASHBOARD,
    KANA,
    VOCAB,
    KANJI,
    GRAMMAR,
    INTERVIEW,
    LISTENING,
    COUNTING,
    BOOKMARKS,
    PROFILE
}

@Composable
fun DashboardScreen(
    userProfile: UserProfile?,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentHour by remember {
        mutableIntStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))
    }
    LaunchedEffect(Unit) {
        while (true) {
            currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            kotlinx.coroutines.delay(30000L)
        }
    }
    val greetingText = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Ohayou gozaimasu"
            in 12..17 -> "Konnichiwa"
            else -> "Konbanwa"
        }
    }

    val kanaBorderBrush = remember {
        Brush.horizontalGradient(
            listOf(
                DopamineEmerald.copy(alpha = 0.8f),
                DopamineCyan.copy(alpha = 0.5f)
            )
        )
    }
    val cardGradientBrush = remember {
        Brush.linearGradient(
            listOf(
                DopamineCardElevated,
                DopamineCard
            )
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(DopamineDeepNight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dynamic Time Greeting & Profile Button
        item(key = "header", span = { GridItemSpan(2) }, contentType = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greetingText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DopamineAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "WELCOME TO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "JAPANESE\nDOPAMINE",
                        fontSize = 26.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.Black,
                        color = DopamineCrimson,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = { onNavigate(ScreenDestination.PROFILE) },
                    shape = CircleShape,
                    color = DopamineCardElevated,
                    border = BorderStroke(1.5.dp, DopamineCrimson),
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("profile_avatar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (userProfile?.displayName?.take(1) ?: "G").uppercase(),
                            color = DopamineCrimson,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // Section Title: Study Modules
        item(key = "section_title", span = { GridItemSpan(2) }, contentType = "section_title") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "STUDY MODULES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineTextSecondary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "8 Modules",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DopamineAmber
                )
            }
        }

        // ==========================================
        // 1. Hiragana & Katakana (Hero Banner Card)
        // ==========================================
        item(key = "card_kana", span = { GridItemSpan(2) }, contentType = "hero_card") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(ScreenDestination.KANA) }
                    .testTag("hub_card_Hiragana_Katakana"),
                shape = RoundedCornerShape(22.dp),
                color = DopamineCard,
                border = BorderStroke(1.5.dp, kanaBorderBrush)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardGradientBrush)
                        .padding(18.dp)
                ) {
                    // Kana Brush Watermark
                    Text(
                        text = "あ・ア",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = DopamineEmerald.copy(alpha = 0.12f),
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge icon
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DopamineEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "あ",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = DopamineEmerald
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DopamineEmerald.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "FOUNDATION • 92 LETTERS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DopamineEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hiragana Katakana",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = DopamineTextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = DopamineEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. Vocabulary
        // ==========================================
        item(key = "card_vocab", contentType = "module_card") {
            ModernModuleCard(
                title = "Vocabulary",
                kanjiChar = "語",
                badge = "25 Lessons",
                accentColor = DopamineCrimson,
                onClick = { onNavigate(ScreenDestination.VOCAB) }
            )
        }

        // ==========================================
        // 3. Grammar
        // ==========================================
        item(key = "card_grammar", contentType = "module_card") {
            ModernModuleCard(
                title = "Grammar",
                kanjiChar = "文",
                badge = "21 Lessons",
                accentColor = DopamineCyan,
                onClick = { onNavigate(ScreenDestination.GRAMMAR) }
            )
        }

        // ==========================================
        // 4. Kanji
        // ==========================================
        item(key = "card_kanji", contentType = "module_card") {
            ModernModuleCard(
                title = "Kanji",
                kanjiChar = "漢",
                badge = "110 N5",
                accentColor = DopamineAmber,
                onClick = { onNavigate(ScreenDestination.KANJI) }
            )
        }

        // ==========================================
        // 5. Listening
        // ==========================================
        item(key = "card_listening", contentType = "module_card") {
            ModernModuleCard(
                title = "Listening",
                kanjiChar = "聴",
                badge = "87 CDs",
                accentColor = Color(0xFFB388FF),
                onClick = { onNavigate(ScreenDestination.LISTENING) }
            )
        }

        // ==========================================
        // 6. Interview
        // ==========================================
        item(key = "card_interview", contentType = "module_card") {
            ModernModuleCard(
                title = "Interview",
                kanjiChar = "問",
                badge = "194 Q&A",
                accentColor = DopamineSakura,
                onClick = { onNavigate(ScreenDestination.INTERVIEW) }
            )
        }

        // ==========================================
        // 7. Counting Units
        // ==========================================
        item(key = "card_counting", contentType = "module_card") {
            ModernModuleCard(
                title = "Counting Units",
                kanjiChar = "数",
                badge = "18 Units",
                accentColor = Color(0xFFFF8A65),
                onClick = { onNavigate(ScreenDestination.COUNTING) }
            )
        }

        // ==========================================
        // 8. Problems
        // ==========================================
        item(key = "card_problems", span = { GridItemSpan(2) }, contentType = "problems_card") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(ScreenDestination.BOOKMARKS) }
                    .testTag("hub_card_Problems"),
                shape = RoundedCornerShape(18.dp),
                color = DopamineCard,
                border = BorderStroke(1.dp, DopamineAmber.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DopamineAmber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = DopamineAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Problems",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DopamineTextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = DopamineAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modern Module Card with glowing accents, Kanji watermark, and clean high contrast typography.
 */
@Composable
private fun ModernModuleCard(
    title: String,
    kanjiChar: String,
    badge: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgBrush = remember {
        Brush.linearGradient(
            listOf(
                DopamineCardElevated,
                DopamineCard
            )
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clickable(onClick = onClick)
            .testTag("hub_card_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        color = DopamineCard,
        border = BorderStroke(1.dp, DopamineOutline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(14.dp)
        ) {
            // Kanji watermark on background
            Text(
                text = kanjiChar,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = accentColor.copy(alpha = 0.10f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Kanji Glyph Badge + Category Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = kanjiChar,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Title: Clean, crisp, English only
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineTextPrimary,
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
private fun StatCapsule(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(14.dp),
        color = DopamineCard,
        border = BorderStroke(1.dp, DopamineOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineTextSecondary
                )
            }
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = DopamineTextPrimary
            )
        }
    }
}
