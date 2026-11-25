package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.repository.JapaneseContentRepository
import com.example.data.repository.UserFirestoreRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.TtsManager
import kotlinx.coroutines.launch

@Composable
fun VocabHubScreen(
    repository: JapaneseContentRepository,
    firestoreRepository: UserFirestoreRepository,
    userId: String,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var activeLesson by remember { mutableStateOf<Int?>(null) }
    var showMasterQuizPlaceholder by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentSubMode by remember { mutableStateOf("LIST") } // "LIST", "FLASHCARD", "QUIZ"

    val bookmarks by firestoreRepository.observeBookmarks(userId).collectAsState(initial = emptyList())
    val bookmarkedIds = remember(bookmarks) {
        bookmarks
            .filter { !it.bookmarkId.startsWith("qp_") && !it.category.startsWith("QZ_PROBLEM") }
            .map { it.itemId }
            .toSet()
    }

    val allVocabItems = remember { repository.getVocabItems() }
    val lessonCounts = remember(allVocabItems) {
        allVocabItems.groupingBy { it.lesson }.eachCount()
    }

    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    var activeSpeakingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) activeSpeakingId = null
    }

    val selectedLesson = activeLesson ?: 1
    val lessonFullList = remember(selectedLesson, allVocabItems) {
        allVocabItems.filter { it.lesson == selectedLesson }
    }
    val lessonFilteredList = remember(selectedLesson, searchQuery, lessonFullList) {
        if (searchQuery.isBlank()) {
            lessonFullList
        } else {
            lessonFullList.filter {
                it.japanese.contains(searchQuery, ignoreCase = true) ||
                    it.romaji.contains(searchQuery, ignoreCase = true) ||
                    it.bangla.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Quietly pre-cache lesson words in background when online for reliable offline playback
    LaunchedEffect(activeLesson) {
        if (activeLesson != null && lessonFullList.isNotEmpty()) {
            ttsManager.preCachePhrases(lessonFullList.map { it.audioPhrase ?: it.japanese })
        }
    }

    when {
        activeLesson != null && currentSubMode == "FLASHCARD" -> {
            FlashcardViewer(
                title = "Lesson $selectedLesson Flashcards",
                items = lessonFullList,
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
        activeLesson != null && currentSubMode == "QUIZ" -> {
            InteractiveQuizGame(
                category = "VOCABULARY",
                lesson = selectedLesson,
                quizItems = lessonFullList.ifEmpty { allVocabItems },
                allItems = allVocabItems,
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
                onClose = { currentSubMode = "LIST" },
                maxQuestions = null // Ask ALL words in this lesson in shuffled order
            )
        }
        activeLesson != null -> {
            BackHandler {
                activeLesson = null
                searchQuery = ""
            }
            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "Lesson $selectedLesson",
                            subtitle = "${lessonFullList.size} Words",
                            onBackClick = {
                                activeLesson = null
                                searchQuery = ""
                            },
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
                    // Flashcards & Take Quiz Action Strip (Full lesson items)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { currentSubMode = "FLASHCARD" },
                            colors = ButtonDefaults.buttonColors(containerColor = DopamineCardElevated),
                            border = BorderStroke(1.dp, DopamineAmber.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_flashcards_button")
                        ) {
                            Icon(
                                Icons.Default.Style,
                                contentDescription = null,
                                tint = DopamineAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Flashcards (${lessonFullList.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DopamineTextPrimary
                            )
                        }

                        Button(
                            onClick = { currentSubMode = "QUIZ" },
                            colors = ButtonDefaults.buttonColors(containerColor = DopamineCardElevated),
                            border = BorderStroke(1.dp, DopamineEmerald.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_quiz_button")
                        ) {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = null,
                                tint = DopamineEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Take Quiz (${lessonFullList.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DopamineTextPrimary
                            )
                        }
                    }

                    // Search inside this lesson
                    DopamineSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Lesson $selectedLesson words...",
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = "${lessonFilteredList.size} WORDS IN LESSON $selectedLesson",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DopamineAmber,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = lessonFilteredList,
                            key = { it.id },
                            contentType = { "vocab_row" }
                        ) { item ->
                            val isBookmarked = bookmarkedIds.contains(item.id)
                            val isThisSpeaking = isSpeaking && activeSpeakingId == item.id
                            val onSpeakItem = remember(item.id) {
                                {
                                    activeSpeakingId = item.id
                                    ttsManager.speak(item.audioPhrase ?: item.japanese)
                                }
                            }
                            val onBookmarkItem = remember(item.id, userId) {
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
                            VocabItemRow(
                                item = item,
                                isBookmarked = isBookmarked,
                                isSpeaking = isThisSpeaking,
                                onSpeak = onSpeakItem,
                                onToggleBookmark = onBookmarkItem
                            )
                        }
                    }
                }
                }
            }
        }
        showMasterQuizPlaceholder -> {
            BackHandler { showMasterQuizPlaceholder = false }
            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "Vocabulary Quiz",
                            onBackClick = { showMasterQuizPlaceholder = false },
                            centerTitle = true
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = DopamineCard,
                        border = BorderStroke(1.dp, DopamineOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(DopamineAmber.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = DopamineAmber,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Vocabulary Quiz",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = DopamineTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All 25 Lessons Combined Quiz\n(Content will be added here later)",
                                fontSize = 14.sp,
                                color = DopamineTextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showMasterQuizPlaceholder = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DopamineCrimson),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Back to Lessons", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                }
            }
        }
        else -> {
            // Main Vocabulary Screen: Centered "Vocabulary" title ONLY + 3-column Straight Red Boxes (Lesson 1..25 + Vocabulary Quiz)
            BackHandler { onBack() }
            val boxSlots = remember { (1..25).toList() + listOf(26) }

            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "Vocabulary",
                            onBackClick = onBack,
                            centerTitle = true
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = boxSlots,
                            key = { _, slot -> slot },
                            contentType = { _, slot -> if (slot <= 25) "lesson_box" else "quiz_box" }
                        ) { _, slot ->
                            if (slot <= 25) {
                                val wordCount = lessonCounts[slot] ?: 0
                                StackedModuleBox(
                                    title = "Lesson $slot",
                                    badge = "$wordCount Words",
                                    iconText = "L$slot",
                                    onClick = remember(slot) {
                                        {
                                            activeLesson = slot
                                            currentSubMode = "LIST"
                                            searchQuery = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("vocab_lesson_box_$slot")
                                )
                            } else {
                                StackedModuleBox(
                                    title = "Vocabulary Quiz",
                                    badge = "25 Lessons",
                                    icon = Icons.Default.Quiz,
                                    isDimmed = true,
                                    onClick = { showMasterQuizPlaceholder = true },
                                    modifier = Modifier.testTag("vocab_master_quiz_box")
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
fun VocabItemRow(
    item: LearningItem,
    isBookmarked: Boolean,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DopamineCard,
        border = BorderStroke(1.dp, DopamineOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.japanese,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DopamineTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.romaji,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DopamineAmber
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.bangla,
                    fontSize = 14.sp,
                    color = DopamineEmerald,
                    fontWeight = FontWeight.Medium
                )
                if (!item.english.isNullOrBlank()) {
                    Text(
                        text = item.english,
                        fontSize = 11.sp,
                        color = DopamineTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AudioSpeakerButton(
                    onClick = onSpeak,
                    isSpeaking = isSpeaking,
                    size = 38
                )
                Spacer(modifier = Modifier.width(6.dp))
                BookmarkStarButton(
                    isBookmarked = isBookmarked,
                    onToggle = onToggleBookmark,
                    size = 38
                )
            }
        }
    }
}
