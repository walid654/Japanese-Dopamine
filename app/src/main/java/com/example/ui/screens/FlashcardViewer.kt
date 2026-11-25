package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningItem
import com.example.ui.components.AmbientBottomGradientBackground
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.BookmarkStarButton
import com.example.ui.components.DopamineTopBar
import com.example.ui.theme.*
import com.example.util.TtsManager
import kotlinx.coroutines.delay

/**
 * Premium Flashcard UI with interactive 3D card flipping, audio playback,
 * bidirectional study modes (JP ➔ BN and BN ➔ JP), auto-play audio toggle,
 * swipe gesture support, and mastery feedback tracking.
 */
@Composable
fun FlashcardViewer(
    title: String,
    items: List<LearningItem>,
    ttsManager: TtsManager,
    onToggleBookmark: (LearningItem) -> Unit,
    bookmarkedIds: Set<String>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onMarkHard: ((LearningItem) -> Unit)? = null
) {
    BackHandler { onClose() }

    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DopamineDeepNight),
            contentAlignment = Alignment.Center
        ) {
            Text("No flashcard items found for this set.", color = DopamineTextSecondary)
        }
        return
    }

    // Active deck state (supports shuffle)
    var studyList by remember(items) { mutableStateOf(items) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isReverseMode by remember { mutableStateOf(false) } // false: JP -> BN, true: BN -> JP
    var autoPlayAudio by remember { mutableStateOf(true) }

    // Mastery counters
    val masteredIds = remember { mutableStateListOf<String>() }
    val needReviewIds = remember { mutableStateListOf<String>() }

    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    val currentSpeakingText by ttsManager.currentSpeakingText.collectAsState()

    val currentItem = studyList[currentIndex.coerceIn(0, studyList.lastIndex)]
    val isBookmarked = bookmarkedIds.contains(currentItem.id)
    val isMarkedHard = needReviewIds.contains(currentItem.id) || isBookmarked

    // Trigger auto-play audio when navigating to a card
    LaunchedEffect(currentIndex, isReverseMode) {
        if (autoPlayAudio && !isReverseMode) {
            delay(150)
            ttsManager.speak(currentItem.audioPhrase ?: currentItem.japanese)
        }
    }

    // Smooth 3D flip animation with spring physics
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardFlip3D"
    )

    // Animated glow border color based on flip face
    val cardBorderColor by animateColorAsState(
        targetValue = if (isFlipped) DopamineEmerald else DopamineCrimson,
        animationSpec = tween(300),
        label = "cardBorderColor"
    )

    fun flipCard() {
        val willBeFlipped = !isFlipped
        isFlipped = willBeFlipped
        // If flipped to back (and in reverse mode), speak the Japanese word
        if (willBeFlipped && isReverseMode && autoPlayAudio) {
            ttsManager.speak(currentItem.audioPhrase ?: currentItem.japanese)
        }
    }

    fun nextCard() {
        if (currentIndex < studyList.lastIndex) {
            currentIndex++
            isFlipped = false
        }
    }

    fun prevCard() {
        if (currentIndex > 0) {
            currentIndex--
            isFlipped = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBottomGradientBackground()

        Scaffold(
            topBar = {
                DopamineTopBar(
                    title = title,
                    subtitle = "Card ${currentIndex + 1} of ${studyList.size} • ${((currentIndex + 1) * 100) / studyList.size}%",
                    onBackClick = onClose,
                    actions = {
                        // Reverse Mode Toggle (JP ➔ BN vs BN ➔ JP)
                        IconButton(
                            onClick = {
                                isReverseMode = !isReverseMode
                                isFlipped = false
                            },
                            modifier = Modifier.testTag("toggle_reverse_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Study Direction",
                                tint = if (isReverseMode) DopamineAmber else DopamineTextSecondary
                            )
                        }

                        // Shuffle Button
                        IconButton(
                            onClick = {
                                studyList = studyList.shuffled()
                                currentIndex = 0
                                isFlipped = false
                            },
                            modifier = Modifier.testTag("shuffle_flashcards_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle Cards",
                                tint = DopamineTextSecondary
                            )
                        }

                        // Bookmark Star
                        BookmarkStarButton(
                            isBookmarked = isBookmarked,
                            onToggle = { onToggleBookmark(currentItem) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Linear Progress Indicator & Stats Strip
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / studyList.size.coerceAtLeast(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = DopamineCrimson,
                    trackColor = DopamineOutline
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isReverseMode) "বাংলা ➔ জাপানি (Active Recall)" else "জাপানি ➔ বাংলা (Recognition)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isReverseMode) DopamineAmber else DopamineCyan
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DopamineEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${masteredIds.size}",
                            fontSize = 11.5.sp,
                            color = DopamineEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = DopamineCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${needReviewIds.size}",
                            fontSize = 11.5.sp,
                            color = DopamineCrimson,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Flipping Flashcard Box with Swipe Gesture
            var totalDrag by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 16f * density
                    }
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp), spotColor = cardBorderColor)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DopamineCardElevated,
                                DopamineCard
                            )
                        )
                    )
                    .clickable { flipCard() }
                    .pointerInput(currentIndex) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (totalDrag > 80) prevCard()
                                else if (totalDrag < -80) nextCard()
                                totalDrag = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    }
                    .testTag("flashcard_container"),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // FRONT FACE
                    FlashcardFrontView(
                        item = currentItem,
                        isReverseMode = isReverseMode,
                        onAudioClick = { ttsManager.speak(currentItem.audioPhrase ?: currentItem.japanese) },
                        isSpeaking = isSpeaking,
                        onFlip = { flipCard() }
                    )
                } else {
                    // BACK FACE (Rotated 180 so text is normal)
                    FlashcardBackView(
                        item = currentItem,
                        isReverseMode = isReverseMode,
                        onAudioClick = { ttsManager.speak(currentItem.audioPhrase ?: currentItem.japanese) },
                        isSpeaking = isSpeaking,
                        onFlipBack = { flipCard() },
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Strip: Auto-Play Audio Switch & Study Feedback Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto-Play Audio Toggle Pill
                Surface(
                    onClick = { autoPlayAudio = !autoPlayAudio },
                    shape = RoundedCornerShape(12.dp),
                    color = if (autoPlayAudio) DopamineCrimson.copy(alpha = 0.15f) else DopamineCardElevated,
                    border = BorderStroke(1.dp, if (autoPlayAudio) DopamineCrimson else DopamineOutline),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (autoPlayAudio) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = if (autoPlayAudio) DopamineCrimson else DopamineTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (autoPlayAudio) "অটো-ভয়েস ON" else "অটো-ভয়েস OFF",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (autoPlayAudio) DopamineCrimson else DopamineTextSecondary
                        )
                    }
                }

                // Easy / Hard Problem Feedback Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // কঠিন (Hard) Button -> ticks as problem & saves to Problems -> Flashcard Problem
                    Button(
                        onClick = {
                            if (!needReviewIds.contains(currentItem.id)) needReviewIds.add(currentItem.id)
                            masteredIds.remove(currentItem.id)
                            if (onMarkHard != null) {
                                onMarkHard(currentItem)
                            } else if (!isBookmarked) {
                                onToggleBookmark(currentItem)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMarkedHard) DopamineCrimson else DopamineCardElevated,
                            contentColor = if (isMarkedHard) Color.White else DopamineCrimson
                        ),
                        border = BorderStroke(1.dp, DopamineCrimson.copy(alpha = if (isMarkedHard) 1f else 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp).testTag("card_review_button")
                    ) {
                        Icon(
                            imageVector = if (isMarkedHard) Icons.Default.CheckCircle else Icons.Default.StarOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMarkedHard) "কঠিন ✓" else "কঠিন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // সহজ (Easy) Button -> skips directly to next card
                    Button(
                        onClick = {
                            if (!masteredIds.contains(currentItem.id)) masteredIds.add(currentItem.id)
                            needReviewIds.remove(currentItem.id)
                            nextCard()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DopamineEmerald, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp).testTag("card_mastered_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সহজ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Controls: Prev, Flip Action, Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev Card Button
                IconButton(
                    onClick = { prevCard() },
                    enabled = currentIndex > 0,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (currentIndex > 0) DopamineCardElevated else DopamineCardElevated.copy(alpha = 0.4f))
                        .testTag("prev_card_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Card",
                        tint = if (currentIndex > 0) DopamineTextPrimary else DopamineTextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Dedicated Flip / Audio Playback Pill
                FilledTonalButton(
                    onClick = { flipCard() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DopamineCardElevated,
                        contentColor = DopamineAmber
                    ),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("flip_card_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFlipped) "মূল শব্দে ফিরুন (Flip Back)" else "অর্থ দেখুন (Flip Meaning)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Next Card Button
                IconButton(
                    onClick = { nextCard() },
                    enabled = currentIndex < studyList.lastIndex,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (currentIndex < studyList.lastIndex) DopamineCrimson else DopamineCardElevated.copy(alpha = 0.4f))
                        .testTag("next_card_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Card",
                        tint = if (currentIndex < studyList.lastIndex) Color.White else DopamineTextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        }
    }
}

/**
 * Front face of the flashcard:
 * Shows Japanese Kanji/Kana or Bengali prompt with Audio Speaker and Flip prompt.
 */
@Composable
private fun FlashcardFrontView(
    item: LearningItem,
    isReverseMode: Boolean,
    onAudioClick: () -> Unit,
    isSpeaking: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Card Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = DopamineCrimson.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DopamineCrimson.copy(alpha = 0.3f))
            ) {
                Text(
                    text = if (isReverseMode) "PROMPT • বাংলা" else "LESSON ${item.lesson} • ${item.category}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineCrimson,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "QUESTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DopamineTextSecondary.copy(alpha = 0.6f)
            )
        }

        // Center Content: Large Japanese Typography or Bengali Prompt
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isReverseMode) {
                Text(
                    text = item.japanese,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = DopamineTextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 52.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.romaji,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DopamineAmber,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = item.bangla,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineEmerald,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                if (!item.english.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.english,
                        fontSize = 15.sp,
                        color = DopamineTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Pronounce Speaker Button with Animated Pulse
            AudioSpeakerButton(
                onClick = onAudioClick,
                isSpeaking = isSpeaking,
                size = 54
            )
        }

        // Bottom Tap Hint
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DopamineCard,
            border = BorderStroke(1.dp, DopamineOutline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = DopamineTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "কার্ডে চাপ দিয়ে উত্তর দেখুন (Tap to flip)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = DopamineTextSecondary
                )
            }
        }
    }
}

/**
 * Back face of the flashcard:
 * Shows the translation answer, full pronunciation, and speaker button.
 */
@Composable
private fun FlashcardBackView(
    item: LearningItem,
    isReverseMode: Boolean,
    onAudioClick: () -> Unit,
    isSpeaking: Boolean,
    onFlipBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Card Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = DopamineEmerald.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DopamineEmerald.copy(alpha = 0.3f))
            ) {
                Text(
                    text = if (isReverseMode) "JAPANESE ANSWER" else "MEANING • অর্থ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineEmerald,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "ANSWER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DopamineEmerald.copy(alpha = 0.8f)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isReverseMode) {
                Text(
                    text = item.bangla,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineEmerald,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                if (!item.english.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.english,
                        fontSize = 15.sp,
                        color = DopamineTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Original Japanese reference
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DopamineCard,
                    border = BorderStroke(1.dp, DopamineOutline)
                ) {
                    Text(
                        text = "${item.japanese} (${item.romaji})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DopamineTextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Text(
                    text = item.japanese,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = DopamineTextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 52.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.romaji,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DopamineAmber,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio Playback Button on the Back Face
            AudioSpeakerButton(
                onClick = onAudioClick,
                isSpeaking = isSpeaking,
                size = 52
            )
        }

        // Bottom Flip Back Hint
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DopamineCard,
            border = BorderStroke(1.dp, DopamineOutline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = null,
                    tint = DopamineEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "প্রশ্ন দেখতে আবার চাপুন (Tap to flip back)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = DopamineEmerald
                )
            }
        }
    }
}
