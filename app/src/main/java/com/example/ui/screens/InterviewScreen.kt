package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InterviewItem
import com.example.data.repository.JapaneseContentRepository
import com.example.ui.components.AmbientBottomGradientBackground
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.DopamineTopBar
import com.example.ui.components.StackedModuleBox
import com.example.ui.theme.*
import com.example.util.TtsManager

private data class InterviewCategoryTile(
    val key: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun InterviewScreen(
    repository: JapaneseContentRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categoryTiles = remember {
        listOf(
            InterviewCategoryTile("Myself", "Myself", Icons.Default.Person),
            InterviewCategoryTile("Family", "Family", Icons.Default.People),
            InterviewCategoryTile("Japan", "Japan", Icons.Default.Public),
            InterviewCategoryTile("General Knowledge", "General Knowledge", Icons.Default.Lightbulb)
        )
    }

    val categoryCounts = remember(categoryTiles) {
        categoryTiles.associate { tile ->
            tile.key to repository.getInterviewItems(category = tile.key).size
        }
    }

    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    var activeSpeakingKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) activeSpeakingKey = null
    }

    if (selectedCategory != null) {
        val activeCategory = selectedCategory!!
        val interviewList = remember(activeCategory) {
            repository.getInterviewItems(category = activeCategory)
        }

        BackHandler {
            ttsManager.stop()
            selectedCategory = null
        }

        Box(modifier = modifier.fillMaxSize()) {
            AmbientBottomGradientBackground()

            Scaffold(
                topBar = {
                    DopamineTopBar(
                        title = activeCategory,
                        subtitle = "${interviewList.size} Questions",
                        onBackClick = {
                            ttsManager.stop()
                            selectedCategory = null
                        },
                        centerTitle = true
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 28.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(
                        items = interviewList,
                        key = { it.id },
                        contentType = { "interview_card" }
                    ) { item ->
                        InterviewItemCard(
                            item = item,
                            activeSpeakingKey = if (isSpeaking) activeSpeakingKey else null,
                            onSpeak = remember(item.id) {
                                { speakKey, text ->
                                    activeSpeakingKey = speakKey
                                    ttsManager.speak(text)
                                }
                            }
                        )
                    }
                }
            }
        }
    } else {
        BackHandler { onBack() }

        Box(modifier = modifier.fillMaxSize()) {
            AmbientBottomGradientBackground()

            Scaffold(
                topBar = {
                    DopamineTopBar(
                        title = "Interview",
                        onBackClick = onBack,
                        centerTitle = true
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = categoryTiles,
                        key = { it.key },
                        contentType = { "interview_tile" }
                    ) { tile ->
                        val count = categoryCounts[tile.key] ?: 0
                        StackedModuleBox(
                            title = tile.title,
                            badge = "$count Questions",
                            icon = tile.icon,
                            onClick = remember(tile.key) { { selectedCategory = tile.key } },
                            modifier = Modifier.testTag("interview_box_${tile.key.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InterviewItemCard(
    item: InterviewItem,
    activeSpeakingKey: String?,
    onSpeak: (speakKey: String, text: String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val questionSpeakKey = "q_${item.id}"

    Surface(
        modifier = Modifier.fillMaxWidth().testTag("interview_card_${item.number}"),
        shape = RoundedCornerShape(14.dp),
        color = DopamineCard,
        border = BorderStroke(1.dp, DopamineOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DopamineCardElevated,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${item.number}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DopamineSakura
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DopamineSakura.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = item.categoryBn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DopamineSakura,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.questionJp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DopamineTextPrimary
                    )
                    Text(
                        text = item.questionRomaji,
                        fontSize = 12.sp,
                        color = DopamineAmber
                    )
                    Text(
                        text = item.questionBn,
                        fontSize = 13.sp,
                        color = DopamineEmerald,
                        fontWeight = FontWeight.Medium
                    )
                }

                AudioSpeakerButton(
                    onClick = { onSpeak(questionSpeakKey, item.audioPhrase ?: item.questionJp) },
                    isSpeaking = activeSpeakingKey == questionSpeakKey,
                    size = 36
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DopamineOutline, modifier = Modifier.padding(bottom = 10.dp))

                    Text(
                        text = "মডেল উত্তর (Model Answers):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DopamineAmber,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    item.answers.forEachIndexed { aIdx, ans ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DopamineCardElevated,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ans.jp,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DopamineTextPrimary
                                    )
                                    Text(
                                        text = ans.romaji,
                                        fontSize = 11.sp,
                                        color = DopamineAmber
                                    )
                                    Text(
                                        text = ans.bn,
                                        fontSize = 12.sp,
                                        color = DopamineEmerald
                                    )
                                }
                                val ansSpeakKey = "a_${item.id}_$aIdx"
                                AudioSpeakerButton(
                                    onClick = { onSpeak(ansSpeakKey, ans.jp) },
                                    isSpeaking = activeSpeakingKey == ansSpeakKey,
                                    size = 32
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
