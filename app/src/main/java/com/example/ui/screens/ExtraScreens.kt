package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bookmark
import com.example.data.model.LearningItem
import com.example.data.model.UserProfile
import com.example.data.repository.JapaneseContentRepository
import com.example.data.repository.UserFirestoreRepository
import com.example.ui.components.AmbientBottomGradientBackground
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.DopamineTopBar
import com.example.ui.theme.*
import com.example.util.TtsManager
import kotlinx.coroutines.launch

// --- COUNTING UNITS SCREEN ---

@Composable
fun CountingScreen(
    repository: JapaneseContentRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val items = remember { repository.getCountingItems() }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    var activeSpeakingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) activeSpeakingId = null
    }

    LaunchedEffect(Unit) {
        if (items.isNotEmpty()) {
            ttsManager.preCachePhrases(items.map { it.audioPhrase ?: it.japanese })
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBottomGradientBackground()

        Scaffold(
            topBar = {
                DopamineTopBar(
                    title = "Counting Units",
                    subtitle = "18 Essential JLPT N5 Counters & Quantifiers",
                    onBackClick = onBack
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = items,
                key = { it.id },
                contentType = { "counting_row" }
            ) { item ->
                SimpleAudioItemCard(
                    japanese = item.japanese,
                    romaji = item.romaji,
                    bangla = item.bangla,
                    accentColor = Color(0xFFFF8A65),
                    isSpeaking = isSpeaking && activeSpeakingId == item.id,
                    onSpeak = remember(item.id) {
                        {
                            activeSpeakingId = item.id
                            ttsManager.speak(item.audioPhrase ?: item.japanese)
                        }
                    }
                )
            }
        }
        }
    }
}

// --- PROBLEMS SCREEN (Flashcard Problem & Quiz Problem) ---

@Composable
fun BookmarksScreen(
    repository: JapaneseContentRepository,
    firestoreRepository: UserFirestoreRepository,
    userId: String,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allProblems by firestoreRepository.observeBookmarks(userId).collectAsState(initial = emptyList())
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    var activeSpeakingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) activeSpeakingId = null
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Flashcard Problem, 1 = Quiz Problem
    var practiceMode by remember { mutableStateOf<String?>(null) } // null, "FLASHCARD", "QUIZ"

    val flashcardProblems = remember(allProblems) {
        allProblems.filter { !it.bookmarkId.startsWith("qp_") && !it.category.startsWith("QZ_PROBLEM") }
    }
    val quizProblems = remember(allProblems) {
        allProblems.filter { it.bookmarkId.startsWith("qp_") || it.category.startsWith("QZ_PROBLEM") }
    }
    val activeList = if (selectedTab == 0) flashcardProblems else quizProblems

    val allVocab = remember { repository.getVocabItems() }
    val flashcardItemIds = remember(flashcardProblems) { flashcardProblems.map { it.itemId }.toSet() }

    when (practiceMode) {
        "FLASHCARD" -> {
            val practiceItems = remember(flashcardProblems) {
                flashcardProblems.map {
                    LearningItem(
                        id = it.itemId,
                        japanese = it.japanese,
                        romaji = it.romaji,
                        bangla = it.bangla,
                        category = it.category.removePrefix("FC_PROBLEM:"),
                        lesson = 1,
                        audioPhrase = it.japanese
                    )
                }
            }
            FlashcardViewer(
                title = "Flashcard Problems",
                items = practiceItems,
                ttsManager = ttsManager,
                onToggleBookmark = { item ->
                    coroutineScope.launch {
                        firestoreRepository.toggleBookmark(
                            Bookmark(
                                bookmarkId = "fp_${item.id}",
                                userId = userId,
                                itemId = item.id,
                                category = "FC_PROBLEM:${item.category}",
                                japanese = item.japanese,
                                romaji = item.romaji,
                                bangla = item.bangla
                            )
                        )
                    }
                },
                bookmarkedIds = flashcardItemIds,
                onClose = { practiceMode = null }
            )
        }
        "QUIZ" -> {
            val practiceItems = remember(quizProblems) {
                quizProblems.map {
                    LearningItem(
                        id = it.itemId,
                        japanese = it.japanese,
                        romaji = it.romaji,
                        bangla = it.bangla,
                        category = it.category.removePrefix("QZ_PROBLEM:"),
                        lesson = 1,
                        audioPhrase = it.japanese
                    )
                }
            }
            InteractiveQuizGame(
                category = "PROBLEM",
                lesson = 1,
                quizItems = practiceItems,
                allItems = (allVocab + practiceItems).distinctBy { it.id },
                modeJpToBn = true,
                hardMode = false,
                userId = userId,
                ttsManager = ttsManager,
                onSaveQuizResult = { history ->
                    firestoreRepository.recordQuizResult(history)
                },
                onClose = { practiceMode = null },
                maxQuestions = null
            )
        }
        else -> {
            BackHandler { onBack() }
            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "Problems",
                            subtitle = "${allProblems.size} Items for Practice",
                            onBackClick = onBack,
                            centerTitle = true
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Two Options: Flashcard Problem & Quiz Problem
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = DopamineCard,
                            contentColor = DopamineTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "Flashcard Problem (${flashcardProblems.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 0) DopamineAmber else DopamineTextSecondary
                                    )
                                },
                                modifier = Modifier.testTag("tab_flashcard_problem")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        text = "Quiz Problem (${quizProblems.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 1) DopamineCrimson else DopamineTextSecondary
                                    )
                                },
                                modifier = Modifier.testTag("tab_quiz_problem")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (activeList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.Style else Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = DopamineTextSecondary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (selectedTab == 0) {
                                            "কোনো ফ্ল্যাশ কার্ড প্রবলেম নেই"
                                        } else {
                                            "কোনো কুইজ প্রবলেম নেই"
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DopamineTextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (selectedTab == 0) {
                                            "ফ্ল্যাশ কার্ডে স্টার (★) বা 'কঠিন' সিলেক্ট করলে সেগুলো এখানে জমা হবে।"
                                        } else {
                                            "কুইজ দেওয়ার সময় কোনো উত্তর ভুল হলে সেগুলো স্বয়ংক্রিয়ভাবে এখানে জমা হবে।"
                                        },
                                        fontSize = 12.5.sp,
                                        color = DopamineTextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item(key = "practice_button_$selectedTab", contentType = "practice_button") {
                                    Button(
                                        onClick = {
                                            practiceMode = if (selectedTab == 0) "FLASHCARD" else "QUIZ"
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedTab == 0) DopamineAmber else DopamineCrimson,
                                            contentColor = if (selectedTab == 0) Color.Black else Color.White
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .testTag("practice_problems_button")
                                    ) {
                                        Icon(
                                            imageVector = if (selectedTab == 0) Icons.Default.Style else Icons.Default.Quiz,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (selectedTab == 0) {
                                                "Practice Flashcard Problems (${activeList.size})"
                                            } else {
                                                "Practice Quiz Problems (${activeList.size})"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                    }
                                }

                                items(
                                    items = activeList,
                                    key = { it.bookmarkId },
                                    contentType = { "problem_row" }
                                ) { bm ->
                                    val cleanCat = bm.category
                                        .removePrefix("FC_PROBLEM:")
                                        .removePrefix("QZ_PROBLEM:")
                                    val isRowSpeaking = isSpeaking && activeSpeakingId == bm.bookmarkId
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = DopamineCard,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selectedTab == 0) DopamineAmber.copy(alpha = 0.3f)
                                            else DopamineCrimson.copy(alpha = 0.35f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                if (cleanCat.isNotBlank()) {
                                                    Text(
                                                        text = cleanCat,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedTab == 0) DopamineAmber else DopamineCrimson
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }
                                                Text(
                                                    text = bm.japanese,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DopamineTextPrimary
                                                )
                                                if (bm.romaji.isNotBlank()) {
                                                    Text(
                                                        text = bm.romaji,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = DopamineAmber
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = bm.bangla,
                                                    fontSize = 13.5.sp,
                                                    color = DopamineEmerald
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AudioSpeakerButton(
                                                    onClick = {
                                                        activeSpeakingId = bm.bookmarkId
                                                        ttsManager.speak(bm.japanese)
                                                    },
                                                    isSpeaking = isRowSpeaking,
                                                    size = 38
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            firestoreRepository.removeProblem(bm)
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove Problem",
                                                        tint = DopamineCrimson
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PROFILE SCREEN ---

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    firestoreRepository: UserFirestoreRepository,
    userId: String,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val studySecs = userProfile?.studySeconds ?: 0L
    val studyMins = studySecs / 60
    val studyHours = studyMins / 60
    val studyTimeString = if (studyHours > 0) "${studyHours}h ${studyMins % 60}m" else "${studyMins}m"

    val quizCount = userProfile?.quizCount ?: 0L
    val quizCorrect = userProfile?.quizCorrect ?: 0L
    val accuracy = if (quizCount > 0) "${(quizCorrect * 100 / (quizCount * 10)).coerceIn(0, 100)}%" else "--"

    Scaffold(
        topBar = {
            DopamineTopBar(
                title = "Learner Profile",
                onBackClick = onBack,
                centerTitle = true
            )
        },
        containerColor = DopamineDeepNight,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DopamineCard,
                    border = BorderStroke(1.dp, DopamineOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(DopamineCrimson.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (userProfile?.displayName?.take(1) ?: "G").uppercase(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = DopamineCrimson
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userProfile?.displayName ?: "Guest Learner",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DopamineTextPrimary
                        )

                        Text(
                            text = userProfile?.email ?: if (userId.startsWith("guest_")) "Guest Account" else "Synced Account",
                            fontSize = 13.sp,
                            color = if (userId.startsWith("guest_")) DopamineAmber else DopamineTextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(studyTimeString, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DopamineCyan)
                                Text("Study Time", fontSize = 11.sp, color = DopamineTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$quizCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DopamineAmber)
                                Text("Quizzes", fontSize = 11.sp, color = DopamineTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(accuracy, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DopamineEmerald)
                                Text("Accuracy", fontSize = 11.sp, color = DopamineTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DopamineCrimson),
                            border = BorderStroke(1.dp, DopamineCrimson),
                            modifier = Modifier.fillMaxWidth().testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userId.startsWith("guest_")) "Log Out / Sign In" else "Sign Out",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleAudioItemCard(
    japanese: String,
    romaji: String,
    bangla: String,
    accentColor: Color,
    isSpeaking: Boolean,
    onSpeak: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DopamineCard,
        border = BorderStroke(1.dp, DopamineOutline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = japanese,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineTextPrimary
                )
                Text(
                    text = romaji,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bangla,
                    fontSize = 13.sp,
                    color = DopamineEmerald
                )
            }
            AudioSpeakerButton(
                onClick = onSpeak,
                isSpeaking = isSpeaking,
                size = 38
            )
        }
    }
}
