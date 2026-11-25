package com.example.ui.screens

import android.media.MediaPlayer
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CdScriptLine
import com.example.data.model.CdTrackInfo
import com.example.data.repository.JapaneseContentRepository
import com.example.ui.components.AmbientBottomGradientBackground
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.DopamineTopBar
import com.example.ui.components.StackedModuleBox
import com.example.ui.theme.*
import com.example.util.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface ListeningScriptRow {
    val rowKey: String
    val contentType: String

    data class VisualCard(
        val visualKey: String,
        override val rowKey: String
    ) : ListeningScriptRow {
        override val contentType: String = "n5_visual_card"
    }

    data class ScriptLine(
        val index: Int,
        val line: CdScriptLine,
        val isAnswerLine: Boolean,
        override val rowKey: String
    ) : ListeningScriptRow {
        override val contentType: String = "script_line"
    }
}

@Composable
fun ListeningScreen(
    repository: JapaneseContentRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedTrack by remember { mutableStateOf<CdTrackInfo?>(null) }
    val isTtsSpeaking by ttsManager.isSpeaking.collectAsState()
    val currentSpeakingText by ttsManager.currentSpeakingText.collectAsState()

    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // MediaPlayer state
    val coroutineScope = rememberCoroutineScope()
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentTrackId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(1) }

    // Sequential TTS playback state for N5 tracks without bundled MP3 files
    var ttsSequenceJob by remember { mutableStateOf<Job?>(null) }
    var isTtsSequencePlaying by remember { mutableStateOf(false) }
    var ttsSequenceTrackId by remember { mutableStateOf<String?>(null) }
    var activeScriptLineIndex by remember { mutableIntStateOf(-1) }
    var singleLineSpeakIndex by remember { mutableIntStateOf(-1) }
    val selectedN5Answers = remember { mutableStateMapOf<String, Int>() }

    val cdTracks = remember {
        repository.getCdTracks(descending = false)
    }

    // Pre-cache N5 listening script lines silently when online so offline playback works
    LaunchedEffect(cdTracks) {
        val n5Lines = cdTracks.filter { it.cdNumber >= 88 }
            .flatMap { it.script }
            .map { it.japanese }
        if (n5Lines.isNotEmpty()) {
            ttsManager.preCachePhrases(n5Lines)
        }
    }

    // Clear single-line highlight once TTS finishes speaking
    LaunchedEffect(isTtsSpeaking, isTtsSequencePlaying) {
        if (!isTtsSpeaking && !isTtsSequencePlaying) {
            singleLineSpeakIndex = -1
        }
    }

    // Audio progress polling loop for MediaPlayer tracks
    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying && mediaPlayer != null) {
            try {
                currentPositionMs = mediaPlayer?.currentPosition ?: 0
                durationMs = (mediaPlayer?.duration ?: 1).coerceAtLeast(1)
            } catch (_: Exception) {
            }
            delay(200)
        }
    }

    fun stopTtsSequence() {
        ttsSequenceJob?.cancel()
        ttsSequenceJob = null
        isTtsSequencePlaying = false
        ttsSequenceTrackId = null
        activeScriptLineIndex = -1
    }

    // Cleanup MediaPlayer & TTS sequence when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            stopTtsSequence()
            ttsManager.stop()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun applySpeed(speed: Float) {
        playbackSpeed = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { mp ->
                    val params = mp.playbackParams
                    params.speed = speed
                    mp.playbackParams = params
                }
            } catch (_: Exception) {
            }
        }
    }

    fun startTtsSequence(track: CdTrackInfo, startIndex: Int = 0) {
        if (track.script.isEmpty()) return
        stopTtsSequence()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        currentTrackId = null
        singleLineSpeakIndex = -1

        val clampedStart = startIndex.coerceIn(0, track.script.lastIndex)
        ttsSequenceTrackId = track.id
        isTtsSequencePlaying = true

        ttsSequenceJob = coroutineScope.launch {
            try {
                for (idx in clampedStart until track.script.size) {
                    activeScriptLineIndex = idx
                    val scriptLine = track.script[idx]
                    ttsManager.speak(scriptLine.japanese, scriptLine.speaker)
                    // Wait until speech begins (up to 1.5s) then finishes
                    var waitStart = 0
                    while (!ttsManager.isSpeaking.value && waitStart < 15) {
                        delay(80)
                        waitStart++
                    }
                    while (ttsManager.isSpeaking.value) {
                        delay(120)
                    }
                    delay(320)
                }
            } finally {
                isTtsSequencePlaying = false
                ttsSequenceTrackId = null
                activeScriptLineIndex = -1
            }
        }
    }

    fun playTrack(track: CdTrackInfo) {
        singleLineSpeakIndex = -1
        val resId = if (track.audioResId != 0) {
            track.audioResId
        } else {
            context.resources.getIdentifier(track.resName, "raw", context.packageName)
        }

        if (resId == 0) {
            // Use sequential TTS playback for tracks without a bundled MP3 (e.g. N5 Listening 1-5)
            if (isTtsSequencePlaying && ttsSequenceTrackId == track.id) {
                stopTtsSequence()
                ttsManager.stop()
            } else {
                startTtsSequence(track, if (activeScriptLineIndex >= 0) activeScriptLineIndex else 0)
            }
            return
        }

        stopTtsSequence()
        ttsManager.stop()
        if (currentTrackId == track.id && mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
            } else {
                mediaPlayer?.start()
                applySpeed(playbackSpeed)
                isPlaying = true
            }
        } else {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            try {
                val mp = MediaPlayer.create(context, resId)
                mediaPlayer = mp
                currentTrackId = track.id
                durationMs = mp.duration.coerceAtLeast(1)
                mp.setOnCompletionListener {
                    isPlaying = false
                    currentPositionMs = 0
                }
                applySpeed(playbackSpeed)
                mp.start()
                isPlaying = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seekRelative(deltaMs: Int) {
        mediaPlayer?.let { mp ->
            val target = (mp.currentPosition + deltaMs).coerceIn(0, durationMs)
            mp.seekTo(target)
            currentPositionMs = target
        }
    }

    fun stopPlayback() {
        stopTtsSequence()
        singleLineSpeakIndex = -1
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
        isPlaying = false
        currentPositionMs = 0
        ttsManager.stop()
    }

    if (selectedTrack != null) {
        val track = selectedTrack!!
        val hasRawAudio = remember(track.id) {
            track.audioResId != 0 ||
                context.resources.getIdentifier(track.resName, "raw", context.packageName) != 0
        }
        val scriptDisplayRows = remember(track.id) {
            buildList {
                track.script.forEachIndexed { index, line ->
                    val visualKey = if (track.cdNumber >= 88) {
                        detectN5QuestionVisualKey(line.speaker)
                    } else null
                    if (visualKey != null) {
                        add(ListeningScriptRow.VisualCard(visualKey, "${track.id}_vis_${index}_$visualKey"))
                    }
                    add(
                        ListeningScriptRow.ScriptLine(
                            index = index,
                            line = line,
                            isAnswerLine = line.speaker.contains("（こたえ）"),
                            rowKey = "${track.id}_line_$index"
                        )
                    )
                }
            }
        }
        val isThisPlaying = if (hasRawAudio) {
            (currentTrackId == track.id && isPlaying)
        } else {
            (ttsSequenceTrackId == track.id && isTtsSequencePlaying)
        }
        val isThisActive = if (hasRawAudio) {
            (currentTrackId == track.id)
        } else {
            (ttsSequenceTrackId == track.id && isTtsSequencePlaying)
        }

        BackHandler {
            stopPlayback()
            selectedTrack = null
        }

        Box(modifier = modifier.fillMaxSize()) {
            AmbientBottomGradientBackground()

            Scaffold(
                topBar = {
                    val topTitle = if (track.cdNumber >= 88) {
                        "N5 Listening ${track.cdNumber - 87}"
                    } else {
                        "CD ${track.cdNumber}"
                    }
                    DopamineTopBar(
                        title = topTitle,
                        subtitle = track.lesson,
                        onBackClick = {
                            stopPlayback()
                            selectedTrack = null
                        },
                        centerTitle = true
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "player_${track.id}", contentType = "player_card") {
                        CdPlayerDetailCard(
                            track = track,
                            isPlaying = isThisPlaying,
                            isActive = isThisActive,
                            currentPosMs = if (hasRawAudio && isThisActive) currentPositionMs else if (!hasRawAudio && isThisActive) (activeScriptLineIndex + 1).coerceAtLeast(0) else 0,
                            totalDurationMs = if (hasRawAudio && isThisActive) durationMs else if (!hasRawAudio) track.script.size.coerceAtLeast(1) else 1,
                            isTtsPlaylistMode = !hasRawAudio,
                            playbackSpeed = playbackSpeed,
                            onPlayPause = { playTrack(track) },
                            onSeek = { newPos ->
                                if (hasRawAudio && isThisActive && mediaPlayer != null) {
                                    mediaPlayer?.seekTo(newPos)
                                    currentPositionMs = newPos
                                } else if (!hasRawAudio && track.script.isNotEmpty()) {
                                    val targetIdx = (newPos - 1).coerceIn(0, track.script.lastIndex)
                                    if (targetIdx != activeScriptLineIndex) {
                                        startTtsSequence(track, targetIdx)
                                    }
                                }
                            },
                            onRewind = {
                                if (hasRawAudio) seekRelative(-5000)
                                else if (track.script.isNotEmpty()) {
                                    val prev = (activeScriptLineIndex - 1).coerceAtLeast(0)
                                    startTtsSequence(track, prev)
                                }
                            },
                            onForward = {
                                if (hasRawAudio) seekRelative(5000)
                                else if (track.script.isNotEmpty()) {
                                    val next = (activeScriptLineIndex + 1).coerceAtMost(track.script.lastIndex)
                                    startTtsSequence(track, next)
                                }
                            },
                            onCycleSpeed = {
                                val nextSpeed = when (playbackSpeed) {
                                    1.0f -> 1.2f
                                    1.2f -> 0.8f
                                    else -> 1.0f
                                }
                                applySpeed(nextSpeed)
                            }
                        )
                    }

                    if (track.cdNumber in CDS_WITH_VISUALS) {
                        item(key = "cd_visual_${track.id}", contentType = "cd_visual_section") {
                            CdListeningVisualSection(cdNumber = track.cdNumber)
                        }
                    }

                    if (scriptDisplayRows.isNotEmpty()) {
                        item(key = "script_header_${track.id}", contentType = "script_header") {
                            Text(
                                text = "SCRIPT & BANGLA MEANING",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DopamineAmber,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(
                            items = scriptDisplayRows,
                            key = { it.rowKey },
                            contentType = { it.contentType }
                        ) { row ->
                            when (row) {
                                is ListeningScriptRow.VisualCard -> {
                                    N5QuestionVisualCard(
                                        questionKey = row.visualKey,
                                        selectedOption = selectedN5Answers[row.visualKey],
                                        onSelectOption = remember(row.visualKey) {
                                            { chosen -> selectedN5Answers[row.visualKey] = chosen }
                                        }
                                    )
                                }
                                is ListeningScriptRow.ScriptLine -> {
                                    val index = row.index
                                    val line = row.line
                                    val isAnswerLine = row.isAnswerLine
                            val isLineSpeaking =
                                (isTtsSequencePlaying && ttsSequenceTrackId == track.id && activeScriptLineIndex == index) ||
                                    (isTtsSpeaking && !isTtsSequencePlaying && singleLineSpeakIndex == index)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isAnswerLine) DopamineCardElevated else DopamineCard,
                                border = BorderStroke(
                                    width = if (isLineSpeaking || isAnswerLine) 1.5.dp else 1.dp,
                                    color = when {
                                        isLineSpeaking -> DopamineCrimson
                                        isAnswerLine -> DopamineEmerald.copy(alpha = 0.65f)
                                        else -> DopamineOutline
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (line.speaker.isNotBlank()) {
                                            Text(
                                                text = line.speaker,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isAnswerLine) DopamineEmerald else DopamineAmber
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                        }
                                        Text(
                                            text = line.japanese,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DopamineTextPrimary
                                        )
                                        Text(
                                            text = line.romaji,
                                            fontSize = 12.sp,
                                            color = DopamineTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = line.bangla,
                                            fontSize = if (isAnswerLine) 14.5.sp else 13.sp,
                                            color = DopamineEmerald,
                                            fontWeight = if (isAnswerLine) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AudioSpeakerButton(
                                        onClick = {
                                            stopTtsSequence()
                                            mediaPlayer?.pause()
                                            isPlaying = false
                                            singleLineSpeakIndex = index
                                            ttsManager.speak(line.japanese, line.speaker)
                                        },
                                        isSpeaking = isLineSpeaking,
                                        size = 38
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
    } else {
        BackHandler { onBack() }

        Box(modifier = modifier.fillMaxSize()) {
            AmbientBottomGradientBackground()

            Scaffold(
                topBar = {
                    DopamineTopBar(
                        title = "Listening",
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
                    items(
                        items = cdTracks,
                        key = { it.id },
                        contentType = { "cd_box" }
                    ) { track ->
                        val isN5Track = track.cdNumber >= 88
                        val boxTitle = if (isN5Track) {
                            "N5 Listening ${track.cdNumber - 87}"
                        } else {
                            "CD ${track.cdNumber}"
                        }
                        val badgeLabel = remember(track.id) {
                            if (track.cdNumber in CDS_WITH_VISUALS) "${track.lesson} • ছবি" else track.lesson
                        }
                        StackedModuleBox(
                            title = boxTitle,
                            badge = badgeLabel,
                            iconText = "${track.cdNumber}",
                            onClick = remember(track.id) { { selectedTrack = track } },
                            modifier = Modifier.testTag("cd_box_${track.cdNumber}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CdPlayerDetailCard(
    track: CdTrackInfo,
    isPlaying: Boolean,
    isActive: Boolean,
    currentPosMs: Int,
    totalDurationMs: Int,
    isTtsPlaylistMode: Boolean,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onCycleSpeed: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cd_card_${track.cdNumber}"),
        shape = RoundedCornerShape(18.dp),
        color = DopamineCard,
        border = BorderStroke(
            1.5.dp,
            if (isPlaying) DopamineCrimson else DopamineOutline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying) DopamineCrimson.copy(alpha = 0.25f)
                            else DopamineCardElevated
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = if (isPlaying) DopamineCrimson else DopamineAmber,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DopamineTextPrimary
                    )
                    if (track.subtitle.isNotBlank()) {
                        Text(
                            text = track.subtitle,
                            fontSize = 12.sp,
                            color = if (isPlaying) DopamineAmber else DopamineTextSecondary
                        )
                    }
                }

                val pulseModifier = if (isPlaying) {
                    val transition = rememberInfiniteTransition(label = "pulse")
                    val scaleState = transition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.12f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    Modifier.graphicsLayer {
                        val s = scaleState.value
                        scaleX = s
                        scaleY = s
                    }
                } else {
                    Modifier
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .then(pulseModifier)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) DopamineCrimson else DopamineCardElevated)
                        .testTag("play_pause_cd_${track.cdNumber}")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) Color.White else DopamineAmber,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = (currentPosMs.toFloat() / totalDurationMs.coerceAtLeast(1)).coerceIn(0f, 1f),
                    onValueChange = { frac ->
                        onSeek((frac * totalDurationMs).toInt())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = DopamineCrimson,
                        activeTrackColor = DopamineCrimson,
                        inactiveTrackColor = DopamineOutline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTtsPlaylistMode) "Line $currentPosMs" else formatTime(currentPosMs),
                        fontSize = 11.sp,
                        color = DopamineTextSecondary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRewind, modifier = Modifier.size(34.dp)) {
                            Icon(
                                Icons.Default.Replay5,
                                contentDescription = if (isTtsPlaylistMode) "Previous line" else "Rewind 5s",
                                tint = DopamineTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onForward, modifier = Modifier.size(34.dp)) {
                            Icon(
                                Icons.Default.Forward5,
                                contentDescription = if (isTtsPlaylistMode) "Next line" else "Forward 5s",
                                tint = DopamineTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (!isTtsPlaylistMode) {
                            FilledTonalButton(
                                onClick = onCycleSpeed,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = DopamineCardElevated,
                                    contentColor = DopamineAmber
                                ),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("${playbackSpeed}x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = if (isTtsPlaylistMode) "$totalDurationMs lines" else formatTime(totalDurationMs),
                        fontSize = 11.sp,
                        color = DopamineTextSecondary
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%02d:%02d", mins, secs)
}
