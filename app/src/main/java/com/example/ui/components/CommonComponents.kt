package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DopamineTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    centerTitle: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val barColors = TopAppBarDefaults.topAppBarColors(
        containerColor = DopamineDeepNight,
        titleContentColor = DopamineTextPrimary
    )
    val navIcon: @Composable () -> Unit = {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DopamineTextPrimary
                )
            }
        }
    }
    val titleContent: @Composable () -> Unit = {
        Column(
            horizontalAlignment = if (centerTitle) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DopamineTextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = DopamineAmber
                )
            }
        }
    }

    if (centerTitle) {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navIcon,
            actions = actions,
            colors = barColors
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navIcon,
            actions = actions,
            colors = barColors
        )
    }
}

@Composable
fun StackedModuleBox(
    title: String,
    badge: String,
    iconText: String? = null,
    icon: ImageVector? = null,
    frontColor: Color = DopamineCrimson,
    backColor: Color = DopamineCrimson,
    tiltDirection: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDimmed: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = DopamineCard,
        border = BorderStroke(1.dp, DopamineOutline),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDimmed) DopamineTextPrimary.copy(alpha = 0.88f) else DopamineTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            } else if (!iconText.isNullOrBlank()) {
                Text(
                    text = iconText,
                    fontSize = when {
                        iconText.length > 3 -> 16.sp
                        iconText.length > 2 -> 19.sp
                        else -> 23.sp
                    },
                    fontWeight = FontWeight.Black,
                    color = DopamineTextPrimary,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DopamineCrimson,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = badge,
                fontSize = 10.sp,
                color = DopamineTextSecondary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AmbientBottomGradientBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientBottomAura")
    val phaseState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auraPhase"
    )

    val leftColors = remember {
        listOf(
            Color(0xFF7B2CBF), // Purple/Violet
            Color(0xFFFF6D00), // Orange
            Color(0xFF00E676)  // Green
        )
    }
    val rightColors = remember {
        listOf(
            Color(0xFFFF1EB8), // Magenta/Pink
            Color(0xFFAEEA00), // Lime/Yellow-Green
            Color(0xFF00B8D4)  // Cyan/Teal
        )
    }

    // Cache radial gradient brushes once per size change and read animation state ONLY in draw phase
    // (zero per-frame recompositions and zero per-frame Brush/Shader allocations while scrolling).
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DopamineDeepNight)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val glowRadius = maxOf(w, h) * 0.68f
                val leftCenter = Offset(x = w * 0.15f, y = h * 0.96f)
                val rightCenter = Offset(x = w * 0.85f, y = h * 0.96f)

                val leftBrushes = leftColors.map { c ->
                    Brush.radialGradient(
                        colors = listOf(
                            c.copy(alpha = 0.34f),
                            c.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = leftCenter,
                        radius = glowRadius
                    )
                }
                val rightBrushes = rightColors.map { c ->
                    Brush.radialGradient(
                        colors = listOf(
                            c.copy(alpha = 0.34f),
                            c.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = rightCenter,
                        radius = glowRadius
                    )
                }

                onDrawBehind {
                    val phase = phaseState.value
                    val idx = phase.toInt().coerceIn(0, 2)
                    val nextIdx = (idx + 1) % 3
                    val fraction = (phase - idx).coerceIn(0f, 1f)
                    val curAlpha = 1f - fraction

                    if (curAlpha > 0.005f) {
                        drawRect(brush = leftBrushes[idx], alpha = curAlpha)
                        drawRect(brush = rightBrushes[idx], alpha = curAlpha)
                    }
                    if (fraction > 0.005f) {
                        drawRect(brush = leftBrushes[nextIdx], alpha = fraction)
                        drawRect(brush = rightBrushes[nextIdx], alpha = fraction)
                    }
                }
            }
    )
}

@Composable
fun AudioSpeakerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    size: Int = 40
) {
    val scaleModifier = if (isSpeaking) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScaleState = infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.22f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        Modifier.graphicsLayer {
            val s = pulseScaleState.value
            scaleX = s
            scaleY = s
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .then(scaleModifier)
            .clip(CircleShape)
            .background(if (isSpeaking) DopamineCrimson else DopamineCardElevated)
            .clickable(onClick = onClick)
            .testTag("audio_speaker_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Pronounce Audio",
            tint = if (isSpeaking) Color.White else DopamineAmber,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun BookmarkStarButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(DopamineCardElevated)
            .clickable(onClick = onToggle)
            .testTag("bookmark_toggle_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
            tint = if (isBookmarked) DopamineAmber else DopamineTextSecondary,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) DopamineCrimson else DopamineCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) DopamineCrimson else DopamineOutline
        ),
        modifier = modifier.testTag("filter_chip_${label.replace(" ", "_")}")
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else DopamineTextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun DopamineSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = DopamineTextSecondary, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = DopamineTextSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = DopamineTextSecondary
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DopamineCard,
            unfocusedContainerColor = DopamineCard,
            focusedBorderColor = DopamineCrimson,
            unfocusedBorderColor = DopamineOutline,
            focusedTextColor = DopamineTextPrimary,
            unfocusedTextColor = DopamineTextPrimary
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_text_field")
    )
}
