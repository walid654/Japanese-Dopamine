package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bookmark
import com.example.data.model.LearningItem
import com.example.data.model.QuizHistory
import com.example.ui.components.AmbientBottomGradientBackground
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.BookmarkStarButton
import com.example.ui.components.DopamineTopBar
import com.example.ui.theme.*
import com.example.util.TtsManager
import kotlinx.coroutines.launch


// Note: FlashcardViewer has been upgraded and moved to dedicated FlashcardViewer.kt

@Composable
fun InteractiveQuizGame(
    category: String,
    lesson: Int,
    quizItems: List<LearningItem>,
    allItems: List<LearningItem>,
    modeJpToBn: Boolean,
    hardMode: Boolean, // false = 4 options, true = 6 options
    userId: String,
    ttsManager: TtsManager,
    onSaveQuizResult: suspend (QuizHistory) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    maxQuestions: Int? = 10,
    onWrongAnswer: ((LearningItem) -> Unit)? = null
) {
    BackHandler { onClose() }

    val coroutineScope = rememberCoroutineScope()
    val questions = remember(quizItems, maxQuestions) {
        val shuffled = quizItems.shuffled()
        if (maxQuestions != null) shuffled.take(maxQuestions) else shuffled
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().background(DopamineDeepNight), contentAlignment = Alignment.Center) {
            Text("Not enough items to generate a quiz.", color = DopamineTextSecondary)
        }
        return
    }

    val currentQuestion = questions[currentIndex.coerceIn(0, questions.lastIndex)]
    val optionsCount = if (hardMode) 6 else 4

    // Generate multiple choice options
    val currentOptions = remember(currentIndex, currentQuestion.id) {
        val correctAnswer = if (modeJpToBn) currentQuestion.bangla else currentQuestion.japanese
        val pool = allItems
            .filter { it.id != currentQuestion.id }
            .map { if (modeJpToBn) it.bangla else it.japanese }
            .distinct()
            .shuffled()
            .take(optionsCount - 1)
        (pool + correctAnswer).shuffled()
    }

    val correctAnswerText = if (modeJpToBn) currentQuestion.bangla else currentQuestion.japanese

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBottomGradientBackground()

        Scaffold(
            topBar = {
                DopamineTopBar(
                    title = "$category Quiz (${if (hardMode) "6-Choice" else "4-Choice"})",
                    subtitle = "Question ${currentIndex + 1} of ${questions.size} • Score: $score",
                    onBackClick = onClose
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = DopamineCrimson,
                trackColor = DopamineCardElevated
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question Display Box
            Surface(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(20.dp),
                color = DopamineCard,
                border = BorderStroke(1.dp, DopamineOutline)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (modeJpToBn) "What is the meaning of:" else "Select the Japanese for:",
                        fontSize = 12.sp,
                        color = DopamineAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (modeJpToBn) currentQuestion.japanese else currentQuestion.bangla,
                        fontSize = if (modeJpToBn) 36.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = DopamineTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (modeJpToBn) {
                        Text(
                            text = currentQuestion.romaji,
                            fontSize = 16.sp,
                            color = DopamineTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentOptions.forEachIndexed { idx, optionText ->
                    val isSelected = selectedOptionIndex == idx
                    val isCorrect = optionText == correctAnswerText

                    val backgroundColor = when {
                        !isAnswerSubmitted -> if (isSelected) DopamineCardElevated else DopamineCard
                        isCorrect -> DopamineEmerald.copy(alpha = 0.25f)
                        isSelected && !isCorrect -> DopamineCrimson.copy(alpha = 0.25f)
                        else -> DopamineCard
                    }

                    val borderColor = when {
                        !isAnswerSubmitted -> if (isSelected) DopamineCrimson else DopamineOutline
                        isCorrect -> DopamineEmerald
                        isSelected && !isCorrect -> DopamineCrimson
                        else -> DopamineOutline
                    }

                    Surface(
                        onClick = {
                            if (!isAnswerSubmitted) {
                                selectedOptionIndex = idx
                                isAnswerSubmitted = true
                                val wasCorrect = (optionText == correctAnswerText)
                                if (wasCorrect) {
                                    score++
                                } else {
                                    onWrongAnswer?.invoke(currentQuestion)
                                }
                                if (modeJpToBn) {
                                    ttsManager.speak(currentQuestion.audioPhrase ?: currentQuestion.japanese)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = backgroundColor,
                        border = BorderStroke(1.5.dp, borderColor),
                        modifier = Modifier.fillMaxWidth().testTag("quiz_option_$idx")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DopamineAmber
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = optionText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = DopamineTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isAnswerSubmitted) {
                                if (isCorrect) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = DopamineEmerald)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = DopamineCrimson)
                                }
                            }
                        }
                    }
                }
            }

            // Next / Finish Button
            if (isAnswerSubmitted) {
                Button(
                    onClick = {
                        if (currentIndex < questions.lastIndex) {
                            currentIndex++
                            selectedOptionIndex = null
                            isAnswerSubmitted = false
                        } else {
                            isQuizCompleted = true
                            // Save result to Firestore
                            coroutineScope.launch {
                                val history = QuizHistory(
                                    quizId = "quiz_${System.currentTimeMillis()}",
                                    userId = userId,
                                    category = category,
                                    lesson = lesson.toLong(),
                                    score = score.toLong(),
                                    totalQuestions = questions.size.toLong(),
                                    percentage = ((score * 100) / questions.size).toLong()
                                )
                                onSaveQuizResult(history)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("quiz_next_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DopamineCrimson)
                ) {
                    Text(
                        text = if (currentIndex < questions.lastIndex) "Next Question" else "Complete Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
        }
    }

    // Results Dialog
    if (isQuizCompleted) {
        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = DopamineCrimson)
                ) {
                    Text("Return to Hub")
                }
            },
            title = {
                Text(
                    text = "Quiz Completed! 🎉",
                    fontWeight = FontWeight.Bold,
                    color = DopamineTextPrimary
                )
            },
            text = {
                Column {
                    val pct = (score * 100) / questions.size.coerceAtLeast(1)
                    Text(
                        text = "Score: $score / ${questions.size} ($pct%)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DopamineAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (pct >= 80) "Sugoi! Excellent mastery of JLPT N5 items!" else "Keep practicing to reinforce your memory!",
                        fontSize = 14.sp,
                        color = DopamineTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your results have been synced to your cloud profile.",
                        fontSize = 12.sp,
                        color = DopamineEmerald
                    )
                }
            },
            containerColor = DopamineCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
