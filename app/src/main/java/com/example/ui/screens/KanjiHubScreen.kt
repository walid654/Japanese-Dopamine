package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bookmark
import com.example.data.model.LearningItem
import com.example.data.repository.JapaneseContentRepository
import com.example.data.repository.UserFirestoreRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.TtsManager
import kotlinx.coroutines.launch

@Composable
fun KanjiHubScreen(
    repository: JapaneseContentRepository,
    firestoreRepository: UserFirestoreRepository,
    userId: String,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val coroutineScope = rememberCoroutineScope()
    var currentSubMode by remember { mutableStateOf("LIST") } // "LIST", "FLASHCARD", "QUIZ"

    val bookmarks by firestoreRepository.observeBookmarks(userId).collectAsState(initial = emptyList())
    val bookmarkedIds = remember(bookmarks) {
        bookmarks
            .filter { !it.bookmarkId.startsWith("qp_") && !it.category.startsWith("QZ_PROBLEM") }
            .map { it.itemId }
            .toSet()
    }

    val allKanjiItems = remember { repository.getKanjiItems() }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    var activeSpeakingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) activeSpeakingId = null
    }

    LaunchedEffect(Unit) {
        if (allKanjiItems.isNotEmpty()) {
            ttsManager.preCachePhrases(allKanjiItems.map { it.audioPhrase ?: it.japanese })
        }
    }

    when (currentSubMode) {
        "FLASHCARD" -> {
            FlashcardViewer(
                title = "Kanji Flashcards",
                items = allKanjiItems,
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
                onMarkHard = { item ->
                    coroutineScope.launch {
                        firestoreRepository.saveProblem(
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
                bookmarkedIds = bookmarkedIds,
                onClose = { currentSubMode = "LIST" }
            )
        }
        "QUIZ" -> {
            InteractiveQuizGame(
                category = "KANJI",
                lesson = 1,
                quizItems = allKanjiItems,
                allItems = allKanjiItems,
                modeJpToBn = true,
                hardMode = false,
                userId = userId,
                ttsManager = ttsManager,
                onSaveQuizResult = { history ->
                    firestoreRepository.recordQuizResult(history)
                },
                onWrongAnswer = { item ->
                    coroutineScope.launch {
                        firestoreRepository.saveProblem(
                            Bookmark(
                                bookmarkId = "qp_${item.id}",
                                userId = userId,
                                itemId = item.id,
                                category = "QZ_PROBLEM:${item.category}",
                                japanese = item.japanese,
                                romaji = item.romaji,
                                bangla = item.bangla
                            )
                        )
                    }
                },
                onClose = { currentSubMode = "LIST" }
            )
        }
        else -> {
            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "Kanji",
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
                            .padding(horizontal = 16.dp)
                    ) {
                        // Mode Action Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { currentSubMode = "FLASHCARD" },
                                colors = ButtonDefaults.buttonColors(containerColor = DopamineCardElevated),
                                border = BorderStroke(1.dp, DopamineAmber.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("kanji_flashcards_button")
                            ) {
                                Icon(
                                    Icons.Default.Style,
                                    contentDescription = null,
                                    tint = DopamineAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Flashcards",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DopamineTextPrimary
                                )
                            }

                            Button(
                                onClick = { currentSubMode = "QUIZ" },
                                colors = ButtonDefaults.buttonColors(containerColor = DopamineCardElevated),
                                border = BorderStroke(1.dp, DopamineEmerald.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("kanji_quiz_button")
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = DopamineEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Kanji Quiz",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DopamineTextPrimary
                                )
                            }
                        }

                        // Kanji List Header
                        Text(
                            text = "${allKanjiItems.size} KANJI CHARACTERS",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DopamineAmber,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 28.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = allKanjiItems,
                                key = { it.id },
                                contentType = { "kanji_row" }
                            ) { item ->
                                val isBookmarked = bookmarkedIds.contains(item.id)
                                val isThisSpeaking = isSpeaking && activeSpeakingId == item.id
                                KanjiItemRow(
                                    item = item,
                                    isBookmarked = isBookmarked,
                                    isSpeaking = isThisSpeaking,
                                    onSpeak = remember(item.id) {
                                        {
                                            activeSpeakingId = item.id
                                            ttsManager.speak(item.audioPhrase ?: item.japanese)
                                        }
                                    },
                                    onToggleBookmark = remember(item.id, userId) {
                                        {
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
                                            Unit
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KanjiItemRow(
    item: LearningItem,
    isBookmarked: Boolean,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DopamineCard,
        border = BorderStroke(1.dp, DopamineOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large Kanji Box
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(14.dp),
                color = DopamineCardElevated,
                border = BorderStroke(1.5.dp, DopamineAmber.copy(alpha = 0.55f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.japanese,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = DopamineTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.romaji,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineAmber,
                    lineHeight = 19.sp
                )
                if (!item.english.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.english,
                        fontSize = 13.5.sp,
                        color = DopamineTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.bangla,
                    fontSize = 14.5.sp,
                    color = DopamineEmerald,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 19.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AudioSpeakerButton(
                    onClick = onSpeak,
                    isSpeaking = isSpeaking,
                    size = 42
                )
                Spacer(modifier = Modifier.width(8.dp))
                BookmarkStarButton(
                    isBookmarked = isBookmarked,
                    onToggle = onToggleBookmark,
                    size = 42
                )
            }
        }
    }
}
