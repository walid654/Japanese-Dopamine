package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.KanaItem
import com.example.data.repository.JapaneseContentRepository
import com.example.ui.components.AmbientBottomGradientBackground
import com.example.ui.components.DopamineTopBar
import com.example.ui.theme.*
import com.example.util.TtsManager

private sealed interface KanaGridSlot {
    val slotKey: String

    data class Character(val item: KanaItem) : KanaGridSlot {
        override val slotKey: String = item.id
    }

    data class EmptySpacer(override val slotKey: String) : KanaGridSlot

    data class SectionTitle(val title: String, override val slotKey: String) : KanaGridSlot
}

private fun buildAlignedKanaSlots(items: List<KanaItem>, prefix: String): List<KanaGridSlot> {
    if (items.isEmpty()) return emptyList()
    val slots = mutableListOf<KanaGridSlot>()
    val byId = items.associateBy { it.id }

    fun addRow(ids: List<String?>, rowTag: String) {
        ids.forEachIndexed { colIdx, suffix ->
            if (suffix == null) {
                slots.add(KanaGridSlot.EmptySpacer("${prefix}_sp_${rowTag}_$colIdx"))
            } else {
                val item = byId["${prefix}_$suffix"]
                if (item != null) {
                    slots.add(KanaGridSlot.Character(item))
                } else {
                    slots.add(KanaGridSlot.EmptySpacer("${prefix}_miss_${rowTag}_$colIdx"))
                }
            }
        }
    }

    // 1. Basic Gojuon (46 chars, properly aligned 5 columns)
    addRow(listOf("a", "i", "u", "e", "o"), "a")
    addRow(listOf("ka", "ki", "ku", "ke", "ko"), "ka")
    addRow(listOf("sa", "shi", "su", "se", "so"), "sa")
    addRow(listOf("ta", "chi", "tsu", "te", "to"), "ta")
    addRow(listOf("na", "ni", "nu", "ne", "no"), "na")
    addRow(listOf("ha", "hi", "fu", "he", "ho"), "ha")
    addRow(listOf("ma", "mi", "mu", "me", "mo"), "ma")
    addRow(listOf("ya", null, "yu", null, "yo"), "ya")
    addRow(listOf("ra", "ri", "ru", "re", "ro"), "ra")
    addRow(listOf("wa", null, null, "wo", "n"), "wa")

    // 2. Dakuten & Handakuten
    slots.add(KanaGridSlot.SectionTitle("Dakuten & Handakuten", "${prefix}_sec_dakuten"))
    addRow(listOf("ga", "gi", "gu", "ge", "go"), "ga")
    addRow(listOf("za", "ji", "zu", "ze", "zo"), "za")
    addRow(listOf("da", "dji", "dzu", "de", "do"), "da")
    addRow(listOf("ba", "bi", "bu", "be", "bo"), "ba")
    addRow(listOf("pa", "pi", "pu", "pe", "po"), "pa")

    // 3. Yoon (Combined sounds - 3 per row)
    slots.add(KanaGridSlot.SectionTitle("Combined (Yoon)", "${prefix}_sec_yoon"))
    val yoonRows = listOf(
        listOf("kya", null, "kyu", null, "kyo"),
        listOf("sha", null, "shu", null, "sho"),
        listOf("cha", null, "chu", null, "cho"),
        listOf("nya", null, "nyu", null, "nyo"),
        listOf("hya", null, "hyu", null, "hyo"),
        listOf("mya", null, "myu", null, "myo"),
        listOf("rya", null, "ryu", null, "ryo"),
        listOf("gya", null, "gyu", null, "gyo"),
        listOf("ja", null, "ju", null, "jo"),
        listOf("bya", null, "byu", null, "byo"),
        listOf("pya", null, "pyu", null, "pyo")
    )
    yoonRows.forEachIndexed { idx, row ->
        addRow(row, "yoon_$idx")
    }

    return slots
}

@Composable
fun KanaScreen(
    repository: JapaneseContentRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Hiragana, 1 = Katakana
    var lastTappedId by remember { mutableStateOf<String?>(null) }

    val hiraganaList = remember { repository.getHiragana() }
    val katakanaList = remember { repository.getKatakana() }

    val hiraganaSlots = remember(hiraganaList) { buildAlignedKanaSlots(hiraganaList, "h") }
    val katakanaSlots = remember(katakanaList) { buildAlignedKanaSlots(katakanaList, "k") }
    val currentSlots = if (selectedTab == 0) hiraganaSlots else katakanaSlots

    val isSpeaking by ttsManager.isSpeaking.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBottomGradientBackground()

        Scaffold(
            topBar = {
                DopamineTopBar(
                    title = "Hiragana Katakana",
                    subtitle = "Audio Chart",
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
                // Tab Row (English only)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DopamineCard,
                    contentColor = DopamineTextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Hiragana",
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
                                "Katakana",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) DopamineEmerald else DopamineTextSecondary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "বর্ণে ট্যাপ করে উচ্চারণ শুনুন:",
                    fontSize = 12.sp,
                    color = DopamineTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Grid of 5 properly aligned columns
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = currentSlots,
                        key = { it.slotKey },
                        contentType = { slot ->
                            when (slot) {
                                is KanaGridSlot.Character -> "kana_char"
                                is KanaGridSlot.EmptySpacer -> "kana_spacer"
                                is KanaGridSlot.SectionTitle -> "kana_section"
                            }
                        },
                        span = { slot ->
                            when (slot) {
                                is KanaGridSlot.SectionTitle -> GridItemSpan(5)
                                else -> GridItemSpan(1)
                            }
                        }
                    ) { slot ->
                        when (slot) {
                            is KanaGridSlot.Character -> {
                                val accentColor = if (selectedTab == 0) DopamineCrimson else DopamineEmerald
                                val isActiveSpeaking = isSpeaking && lastTappedId == slot.item.id
                                KanaGridCard(
                                    item = slot.item,
                                    accentColor = accentColor,
                                    isActiveSpeaking = isActiveSpeaking,
                                    onClick = remember(slot.item.id) {
                                        {
                                            lastTappedId = slot.item.id
                                            ttsManager.speak(slot.item.japanese)
                                        }
                                    }
                                )
                            }
                            is KanaGridSlot.EmptySpacer -> {
                                Box(modifier = Modifier.aspectRatio(0.85f))
                            }
                            is KanaGridSlot.SectionTitle -> {
                                Text(
                                    text = slot.title.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) DopamineCrimson else DopamineEmerald,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
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
private fun KanaGridCard(
    item: KanaItem,
    accentColor: Color,
    isActiveSpeaking: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActiveSpeaking) DopamineCardElevated else DopamineCard,
        border = BorderStroke(
            width = if (isActiveSpeaking) 1.5.dp else 1.dp,
            color = if (isActiveSpeaking) accentColor else DopamineOutline
        ),
        modifier = Modifier
            .aspectRatio(0.85f)
            .testTag("kana_card_${item.romaji}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.japanese,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DopamineTextPrimary
            )
            Text(
                text = item.romaji,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Text(
                text = item.bangla,
                fontSize = 9.sp,
                color = DopamineTextSecondary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
