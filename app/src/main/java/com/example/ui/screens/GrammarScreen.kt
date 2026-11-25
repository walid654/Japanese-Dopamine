package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed as lazyListItemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrammarLesson
import com.example.data.model.ParticlePoint
import com.example.data.model.ParticleQuestion
import com.example.data.model.ParticleRule
import com.example.data.model.QuizHistory
import com.example.data.repository.JapaneseContentRepository
import com.example.data.repository.UserFirestoreRepository
import com.example.ui.components.AmbientBottomGradientBackground
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.DopamineTopBar
import com.example.ui.components.StackedModuleBox
import com.example.ui.theme.*
import com.example.util.TtsManager
import kotlinx.coroutines.launch

@Composable
fun GrammarScreen(
    repository: JapaneseContentRepository,
    firestoreRepository: UserFirestoreRepository,
    userId: String,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 21 Lessons, 1 = 28 Particles
    var activeLesson by remember { mutableStateOf<GrammarLesson?>(null) }
    var activeParticle by remember { mutableStateOf<ParticleRule?>(null) }
    var isParticleQuizActive by remember { mutableStateOf(false) }

    val grammarLessons = remember { repository.getGrammarLessons() }
    val particleRules = remember { repository.getParticleRules() }
    val particleQuestions = remember { repository.getParticleQuestions() }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    var activeSpeakingKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) activeSpeakingKey = null
    }

    val palettePairs = remember {
        listOf(
            DopamineCyan to DopamineCrimson,
            Color(0xFF9D7BFF) to DopamineAmber,
            DopamineEmerald to DopamineCyan,
            DopamineSakura to DopamineEmerald,
            DopamineAmber to Color(0xFF9D7BFF),
            DopamineCrimson to DopamineAmber
        )
    }

    when {
        isParticleQuizActive -> {
            ParticleQuizEngine(
                questions = particleQuestions,
                userId = userId,
                ttsManager = ttsManager,
                onSaveResult = { history ->
                    firestoreRepository.recordQuizResult(history)
                },
                onWrongAnswer = { q ->
                    coroutineScope.launch {
                        firestoreRepository.saveProblem(
                            com.example.data.model.Bookmark(
                                bookmarkId = "qp_${q.id}",
                                userId = userId,
                                itemId = q.id,
                                category = "QZ_PROBLEM:PARTICLE",
                                japanese = q.sentenceJp,
                                romaji = q.sentenceRomaji,
                                bangla = q.sentenceBangla
                            )
                        )
                    }
                },
                onClose = { isParticleQuizActive = false }
            )
        }
        activeLesson != null -> {
            val lesson = activeLesson!!
            BackHandler { activeLesson = null }
            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "Lesson ${lesson.lesson}",
                            subtitle = "${lesson.rules.size} Rules • Short Grammar",
                            onBackClick = { activeLesson = null },
                            centerTitle = true
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    lazyListItemsIndexed(
                        items = lesson.rules,
                        key = { idx, _ -> "l_${lesson.lesson}_$idx" },
                        contentType = { _, _ -> "grammar_rule" }
                    ) { idx, rule ->
                        val rowKey = "l_${lesson.lesson}_$idx"
                        val isRowSpeaking = isSpeaking && activeSpeakingKey == rowKey
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DopamineCard,
                            border = BorderStroke(1.dp, DopamineOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Rule ${idx + 1}: ${rule.pattern}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DopamineEmerald
                                )
                                if (rule.formula.isNotBlank()) {
                                    Text(
                                        text = "সূত্র: ${rule.formula}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DopamineAmber,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }
                                if (rule.explanation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = rule.explanation,
                                        fontSize = 13.5.sp,
                                        color = DopamineTextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }

                                if (rule.exampleJp.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DopamineDeepNight,
                                        border = BorderStroke(1.dp, DopamineOutline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = rule.exampleJp,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DopamineTextPrimary,
                                                    lineHeight = 21.sp
                                                )
                                                if (rule.exampleRomaji.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = rule.exampleRomaji,
                                                        fontSize = 12.sp,
                                                        color = DopamineAmber,
                                                        lineHeight = 17.sp
                                                    )
                                                }
                                                if (rule.exampleBangla.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = rule.exampleBangla,
                                                        fontSize = 12.5.sp,
                                                        color = DopamineEmerald,
                                                        lineHeight = 18.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AudioSpeakerButton(
                                                onClick = {
                                                    activeSpeakingKey = rowKey
                                                    ttsManager.speak(rule.exampleJp)
                                                },
                                                isSpeaking = isRowSpeaking,
                                                size = 36
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
        activeParticle != null -> {
            val particleRule = activeParticle!!
            BackHandler { activeParticle = null }
            val pointsList = remember(particleRule) {
                if (particleRule.points.isNotEmpty()) {
                    particleRule.points
                } else {
                    listOf(ParticlePoint(usage = particleRule.usage, exampleJp = particleRule.exampleJp))
                }
            }

            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "${particleRule.particle} (${particleRule.romaji})",
                            subtitle = particleRule.title,
                            onBackClick = { activeParticle = null },
                            centerTitle = true
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    lazyListItemsIndexed(
                        items = pointsList,
                        key = { idx, _ -> "p_${particleRule.particle}_$idx" },
                        contentType = { _, _ -> "particle_point" }
                    ) { idx, pt ->
                        val pointKey = "p_${particleRule.particle}_$idx"
                        val isPointSpeaking = isSpeaking && activeSpeakingKey == pointKey
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DopamineCard,
                            border = BorderStroke(1.dp, DopamineOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DopamineCyan.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "Point ${idx + 1}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DopamineCyan,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }

                                if (pt.usage.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pt.usage,
                                        fontSize = 13.5.sp,
                                        color = DopamineTextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }

                                if (pt.exampleJp.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DopamineDeepNight,
                                        border = BorderStroke(1.dp, DopamineOutline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = pt.exampleJp,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DopamineTextPrimary,
                                                lineHeight = 21.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AudioSpeakerButton(
                                                onClick = {
                                                    activeSpeakingKey = pointKey
                                                    ttsManager.speak(pt.exampleJp)
                                                },
                                                isSpeaking = isPointSpeaking,
                                                size = 36
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
        else -> {
            BackHandler { onBack() }
            Box(modifier = modifier.fillMaxSize()) {
                AmbientBottomGradientBackground()

                Scaffold(
                    topBar = {
                        DopamineTopBar(
                            title = "Grammar",
                            onBackClick = onBack,
                            centerTitle = true,
                            actions = {
                                if (selectedTab == 1) {
                                    Button(
                                        onClick = { isParticleQuizActive = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = DopamineEmerald),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Tab Row
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
                                        "Lessons",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 0) DopamineCrimson else DopamineTextSecondary
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        "Particle",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 1) DopamineAmber else DopamineTextSecondary
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedTab == 0) {
                            // 21 Lesson Straight Red Boxes Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(
                                    items = grammarLessons,
                                    key = { _, l -> l.lesson },
                                    contentType = { _, _ -> "grammar_lesson_box" }
                                ) { _, lesson ->
                                    StackedModuleBox(
                                        title = "Lesson ${lesson.lesson}",
                                        badge = "${lesson.rules.size} Rules",
                                        iconText = "L${lesson.lesson}",
                                        onClick = remember(lesson.lesson) { { activeLesson = lesson } },
                                        modifier = Modifier.testTag("grammar_lesson_box_${lesson.lesson}")
                                    )
                                }
                            }
                        } else {
                            // 28 Particle Straight Red Boxes Grid + Quiz Banner
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item(
                                    key = "particle_quiz_banner",
                                    span = { GridItemSpan(3) },
                                    contentType = "particle_quiz_banner"
                                ) {
                                    Surface(
                                        onClick = { isParticleQuizActive = true },
                                        shape = RoundedCornerShape(14.dp),
                                        color = DopamineCardElevated,
                                        border = BorderStroke(1.dp, DopamineEmerald),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .testTag("particle_quiz_banner")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(DopamineEmerald.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Quiz, contentDescription = null, tint = DopamineEmerald)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Particle Mastery Quiz • প্র্যাকটিস কুইজ",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.5.sp,
                                                    color = DopamineTextPrimary
                                                )
                                                Text(
                                                    text = "16 Official Questions with Explanations",
                                                    fontSize = 12.sp,
                                                    color = DopamineTextSecondary
                                                )
                                            }
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DopamineEmerald)
                                        }
                                    }
                                }

                                itemsIndexed(
                                    items = particleRules,
                                    key = { idx, r -> "${idx}_${r.particle}" },
                                    contentType = { _, _ -> "particle_box" }
                                ) { idx, rule ->
                                    val ptsCount = if (rule.points.isNotEmpty()) rule.points.size else 1
                                    StackedModuleBox(
                                        title = rule.romaji,
                                        badge = "$ptsCount ${if (ptsCount == 1) "Point" else "Points"}",
                                        iconText = rule.particle,
                                        onClick = remember(idx, rule.particle) { { activeParticle = rule } },
                                        modifier = Modifier.testTag("particle_box_${rule.particle}")
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

@Composable
private fun ParticleQuizEngine(
    questions: List<ParticleQuestion>,
    userId: String,
    ttsManager: TtsManager,
    onSaveResult: suspend (QuizHistory) -> Unit,
    onClose: () -> Unit,
    onWrongAnswer: ((ParticleQuestion) -> Unit)? = null
) {
    BackHandler { onClose() }

    val coroutineScope = rememberCoroutineScope()
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AmbientBottomGradientBackground()
            Text("No questions found.", color = DopamineTextSecondary)
        }
        return
    }

    val currentQ = questions[currentIndex]

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBottomGradientBackground()

        Scaffold(
            topBar = {
                DopamineTopBar(
                    title = "Particle Quiz",
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = DopamineEmerald,
                    trackColor = DopamineCardElevated
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DopamineCard,
                    border = BorderStroke(1.dp, DopamineOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fill in the blank with correct particle:",
                            fontSize = 11.sp,
                            color = DopamineAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQ.sentenceJp,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DopamineTextPrimary
                        )
                        Text(
                            text = currentQ.sentenceRomaji,
                            fontSize = 13.sp,
                            color = DopamineTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQ.sentenceBangla,
                            fontSize = 14.sp,
                            color = DopamineEmerald,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options
                currentQ.options.forEachIndexed { idx, opt ->
                    val isSelected = selectedOptionIndex == idx
                    val isCorrect = idx == currentQ.correctIndex

                    val bg = when {
                        !isSubmitted -> if (isSelected) DopamineCardElevated else DopamineCard
                        isCorrect -> DopamineEmerald.copy(alpha = 0.25f)
                        isSelected && !isCorrect -> DopamineCrimson.copy(alpha = 0.25f)
                        else -> DopamineCard
                    }

                    val border = when {
                        !isSubmitted -> if (isSelected) DopamineEmerald else DopamineOutline
                        isCorrect -> DopamineEmerald
                        isSelected && !isCorrect -> DopamineCrimson
                        else -> DopamineOutline
                    }

                    Surface(
                        onClick = {
                            if (!isSubmitted) {
                                selectedOptionIndex = idx
                                isSubmitted = true
                                if (isCorrect) {
                                    score++
                                } else {
                                    onWrongAnswer?.invoke(currentQ)
                                }
                                ttsManager.speak(currentQ.sentenceJp.replace("()", opt))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = bg,
                        border = BorderStroke(1.dp, border),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = opt,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DopamineTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSubmitted) {
                                if (isCorrect) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = DopamineEmerald)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = DopamineCrimson)
                                }
                            }
                        }
                    }
                }

                // Explanation
                if (isSubmitted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DopamineCardElevated,
                        border = BorderStroke(1.dp, DopamineAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ব্যাখ্যা (Explanation):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DopamineAmber
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 13.sp,
                                color = DopamineTextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isSubmitted) {
                Button(
                    onClick = {
                        if (currentIndex < questions.lastIndex) {
                            currentIndex++
                            selectedOptionIndex = null
                            isSubmitted = false
                        } else {
                            isQuizCompleted = true
                            coroutineScope.launch {
                                val history = QuizHistory(
                                    quizId = "particle_quiz_${System.currentTimeMillis()}",
                                    userId = userId,
                                    category = "PARTICLE",
                                    lesson = 1L,
                                    score = score.toLong(),
                                    totalQuestions = questions.size.toLong(),
                                    percentage = ((score * 100) / questions.size).toLong()
                                )
                                onSaveResult(history)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DopamineEmerald)
                ) {
                    Text(
                        text = if (currentIndex < questions.lastIndex) "Next Question" else "Finish Particle Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
        }
    }

    if (isQuizCompleted) {
        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = DopamineEmerald)
                ) {
                    Text("Return to Particles")
                }
            },
            title = { Text("Particle Quiz Completed! 🎉", color = DopamineTextPrimary) },
            text = {
                Column {
                    Text("Your Score: $score / ${questions.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DopamineAmber)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Results synced to your cloud profile.", fontSize = 12.sp, color = DopamineEmerald)
                }
            },
            containerColor = DopamineCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
