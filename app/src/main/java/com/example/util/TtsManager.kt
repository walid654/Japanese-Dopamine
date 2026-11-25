package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Offline-First Japanese Audio Pronunciation Manager.
 * 1. Actively prefers local/offline Japanese TTS voices (!isNetworkConnectionRequired) across installed engines.
 * 2. Stores downloaded pronunciation MP3s permanently in app filesDir (never purged like cacheDir).
 * 3. Automatically intercepts asynchronous TTS onError callbacks (e.g. when internet is off)
 *    and falls back to cached offline MP3s cleanly.
 * 4. Quietly caches spoken/lesson phrases whenever online so they continue working 100% offline.
 */
class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "TtsManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var tts: TextToSpeech? = null
    private var isNativeReady = false
    private var isNativeJapaneseSupported = false
    private var selectedVoiceRequiresNetwork = false
    private var triedAltEngine = false

    private var fallbackPlayer: MediaPlayer? = null
    private var defaultJapaneseVoice: Voice? = null
    private var femaleJapaneseVoice: Voice? = null
    private var maleJapaneseVoice: Voice? = null
    private val activeUtterances = ConcurrentHashMap<String, String>()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentSpeakingText = MutableStateFlow<String?>(null)
    val currentSpeakingText: StateFlow<String?> = _currentSpeakingText.asStateFlow()

    private val _hasOfflineVoice = MutableStateFlow(false)
    val hasOfflineVoice: StateFlow<Boolean> = _hasOfflineVoice.asStateFlow()

    // Persistent offline audio store inside filesDir + legacy cacheDir fallback
    private val persistentAudioDir: File = File(context.filesDir, "audio_pronunciation").apply {
        if (!exists()) mkdirs()
    }
    private val legacyCacheDir: File = File(context.cacheDir, "audio_pronunciation").apply {
        if (!exists()) mkdirs()
    }

    init {
        initTtsEngine(null)
    }

    private fun initTtsEngine(enginePackage: String?) {
        try {
            tts?.shutdown()
            tts = if (enginePackage != null) {
                TextToSpeech(context.applicationContext, this, enginePackage)
            } else {
                TextToSpeech(context.applicationContext, this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate TextToSpeech ($enginePackage): ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isNativeReady = true
            setupNativeTts()
        } else {
            Log.w(TAG, "Native TextToSpeech init returned code: $status. Offline cache/fallback active.")
            isNativeReady = false
        }
    }

    private fun setupNativeTts() {
        val t = tts ?: return

        val japaneseLocales = listOf(
            Locale.JAPANESE,
            Locale.JAPAN,
            Locale("ja", "JP"),
            Locale("ja")
        )

        var matchedLocale: Locale? = null
        for (loc in japaneseLocales) {
            val availability = t.isLanguageAvailable(loc)
            if (availability >= TextToSpeech.LANG_AVAILABLE) {
                val setResult = t.setLanguage(loc)
                if (setResult != TextToSpeech.LANG_MISSING_DATA && setResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    matchedLocale = loc
                    break
                }
            }
        }

        // Explicitly inspect available voices and strongly prefer an OFFLINE Japanese voice
        var chosenVoice: Voice? = null
        try {
            val voices = t.voices?.filter { voice ->
                voice.locale.language.equals("ja", ignoreCase = true) &&
                    !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
            }.orEmpty()

            val offlineVoices = voices.filter { !it.isNetworkConnectionRequired }
            val candidatePool = offlineVoices.ifEmpty { voices }
            chosenVoice = candidatePool.maxByOrNull { it.quality }

            // Identify distinct male/female Google/Samsung Japanese voices if multiple exist
            femaleJapaneseVoice = candidatePool.firstOrNull {
                val n = it.name.lowercase()
                n.contains("jab") || n.contains("female") || n.contains("woman") || n.contains("haruka")
            } ?: chosenVoice
            maleJapaneseVoice = candidatePool.firstOrNull {
                val n = it.name.lowercase()
                n.contains("jac") || n.contains("jad") || n.contains("male") || n.contains("ichiro")
            } ?: candidatePool.lastOrNull { it.name != femaleJapaneseVoice?.name } ?: chosenVoice

            if (chosenVoice != null) {
                defaultJapaneseVoice = chosenVoice
                t.voice = chosenVoice
                selectedVoiceRequiresNetwork = chosenVoice.isNetworkConnectionRequired
                _hasOfflineVoice.value = !chosenVoice.isNetworkConnectionRequired
                isNativeJapaneseSupported = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not enumerate TTS voices: ${e.message}")
        }

        if (chosenVoice == null && matchedLocale != null) {
            isNativeJapaneseSupported = true
            selectedVoiceRequiresNetwork = t.voice?.isNetworkConnectionRequired ?: false
            _hasOfflineVoice.value = !selectedVoiceRequiresNetwork
        }

        // If default engine has no offline Japanese voice, try Google TTS / Samsung TTS once
        if (!triedAltEngine && !_hasOfflineVoice.value) {
            triedAltEngine = true
            val currentEngine = t.defaultEngine
            val altEngine = try {
                t.engines.firstOrNull {
                    it.name != currentEngine &&
                        (it.name == "com.google.android.tts" || it.name == "com.samsung.SMT")
                }?.name
            } catch (_: Exception) {
                null
            }
            if (altEngine != null) {
                Log.i(TAG, "Trying alternative TTS engine for offline Japanese voice: $altEngine")
                initTtsEngine(altEngine)
                return
            }
        }

        if (isNativeJapaneseSupported) {
            t.setSpeechRate(0.88f)
            t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    mainHandler.post { _isSpeaking.value = true }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId != null) activeUtterances.remove(utteranceId)
                    mainHandler.post {
                        _isSpeaking.value = false
                        _currentSpeakingText.value = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    val failedText = utteranceId?.let { activeUtterances.remove(it) }
                    mainHandler.post {
                        handleTtsSpeakFailure(failedText)
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    val failedText = utteranceId?.let { activeUtterances.remove(it) }
                    Log.w(TAG, "Native TTS async error ($errorCode) for '$failedText', falling back to offline audio.")
                    mainHandler.post {
                        handleTtsSpeakFailure(failedText)
                    }
                }
            })
            Log.i(
                TAG,
                "Native Japanese TTS initialized (locale=$matchedLocale, offline=${_hasOfflineVoice.value}, voice=${chosenVoice?.name})"
            )
        } else {
            Log.w(TAG, "Japanese voice pack not installed on phone. Using offline cache / online download fallback.")
        }
    }

    private fun handleTtsSpeakFailure(failedText: String?) {
        if (!failedText.isNullOrBlank()) {
            val cached = getCachedAudioFile(failedText)
            if (cached != null) {
                playAudioFile(cached)
                return
            }
            if (isNetworkAvailable()) {
                playOnlineAndCachePronunciation(failedText)
                return
            }
        }
        _isSpeaking.value = false
        _currentSpeakingText.value = null
    }

    fun speak(text: String, speaker: String? = null) {
        stop()

        val cleanText = normalizeSpeakableText(text)
        if (cleanText.isEmpty()) return

        _currentSpeakingText.value = cleanText
        _isSpeaking.value = true

        val cachedFile = getCachedAudioFile(cleanText)
        val online = isNetworkAvailable()

        // 1. If device is OFFLINE and native voice isn't confirmed offline-ready, play permanent local cache first!
        if (!online && cachedFile != null && (!isNativeReady || !isNativeJapaneseSupported || selectedVoiceRequiresNetwork)) {
            playAudioFile(cachedFile)
            return
        }

        // 2. If native Japanese TTS is ready (and either offline-capable OR online)
        if (isNativeReady && isNativeJapaneseSupported && tts != null && (!selectedVoiceRequiresNetwork || online)) {
            applySpeakerTone(speaker)
            val utteranceId = "tts_${System.currentTimeMillis()}"
            activeUtterances[utteranceId] = cleanText
            val res = tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (res == TextToSpeech.SUCCESS) {
                // Warm persistent offline cache silently in background while online
                if (online && cachedFile == null) {
                    scope.launch { downloadAudio(cleanText) }
                }
                return
            } else {
                activeUtterances.remove(utteranceId)
                Log.w(TAG, "Native TTS speak returned error ($res), falling back to cached/streaming audio.")
            }
        }

        // 3. Fallback: play cached offline MP3 if available, otherwise download & cache permanently
        if (cachedFile != null) {
            playAudioFile(cachedFile)
        } else if (online) {
            playOnlineAndCachePronunciation(cleanText)
        } else {
            // Completely offline and never cached + no offline Japanese TTS pack on device
            _isSpeaking.value = false
            _currentSpeakingText.value = null
        }
    }

    private fun applySpeakerTone(speaker: String?) {
        val t = tts ?: return
        val s = speaker.orEmpty()
        try {
            when {
                s.contains("おとこ") || s.contains("男") -> {
                    maleJapaneseVoice?.let { t.voice = it }
                    t.setPitch(0.84f)
                    t.setSpeechRate(0.86f)
                }
                s.contains("おんな") || s.contains("女") -> {
                    femaleJapaneseVoice?.let { t.voice = it }
                    t.setPitch(1.06f)
                    t.setSpeechRate(0.88f)
                }
                else -> {
                    defaultJapaneseVoice?.let { t.voice = it }
                    t.setPitch(0.98f)
                    t.setSpeechRate(0.87f)
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Quietly pre-caches a set of Japanese phrases into persistent storage when online,
     * so learners can play them reliably even after turning off mobile data / Wi-Fi.
     */
    suspend fun preCachePhrases(phrases: List<String>) {
        if (!isNetworkAvailable()) return
        withContext(Dispatchers.IO) {
            for (raw in phrases.distinct()) {
                if (!isNetworkAvailable()) break
                val clean = normalizeSpeakableText(raw)
                if (clean.isBlank()) continue
                if (getCachedAudioFile(clean) != null) continue
                downloadAudio(clean)
                delay(180)
            }
        }
    }

    private fun normalizeSpeakableText(text: String): String {
        return text
            .replace(Regex("\\[[^\\]]*[～~][^\\]]*\\]"), "")
            .replace("[", "")
            .replace("]", "")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("（.*?）"), "")
            .replace("～", "")
            .replace("~", "")
            .replace("/", "、")
            .trim()
    }

    private fun playOnlineAndCachePronunciation(text: String) {
        scope.launch {
            try {
                val file = getCachedAudioFile(text)
                if (file != null && file.exists() && file.length() > 0) {
                    playAudioFile(file)
                } else {
                    val downloaded = downloadAudio(text)
                    if (downloaded != null && downloaded.exists()) {
                        playAudioFile(downloaded)
                    } else if (isNetworkAvailable()) {
                        playDirectStream(text)
                    } else {
                        _isSpeaking.value = false
                        _currentSpeakingText.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing pronunciation: ${e.message}", e)
                _isSpeaking.value = false
                _currentSpeakingText.value = null
            }
        }
    }

    private fun getCachedAudioFile(text: String): File? {
        val safeName = "ja_" + text.hashCode().toString() + ".mp3"
        val persistent = File(persistentAudioDir, safeName)
        if (persistent.exists() && persistent.length() > 200) return persistent
        val legacy = File(legacyCacheDir, safeName)
        if (legacy.exists() && legacy.length() > 200) {
            try {
                legacy.copyTo(persistent, overwrite = true)
                return persistent
            } catch (_: Exception) {
                return legacy
            }
        }
        return null
    }

    private suspend fun downloadAudio(text: String): File? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=ja&q=$encoded"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                requestMethod = "GET"
            }
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val safeName = "ja_" + text.hashCode().toString() + ".mp3"
                val tempFile = File(persistentAudioDir, "$safeName.tmp")
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val destFile = File(persistentAudioDir, safeName)
                if (tempFile.renameTo(destFile) && destFile.length() > 200) {
                    return@withContext destFile
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Download failed for '$text': ${e.message}")
        }
        null
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    private fun playAudioFile(file: File) {
        try {
            stopFallbackPlayer()
            fallbackPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isSpeaking.value = true
                }
                setOnCompletionListener {
                    _isSpeaking.value = false
                    _currentSpeakingText.value = null
                    stopFallbackPlayer()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "MediaPlayer error: what=$what extra=$extra")
                    _isSpeaking.value = false
                    _currentSpeakingText.value = null
                    stopFallbackPlayer()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing file: ${e.message}")
            _isSpeaking.value = false
            _currentSpeakingText.value = null
        }
    }

    private fun playDirectStream(text: String) {
        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=ja&q=$encoded"
            stopFallbackPlayer()
            fallbackPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .build()
                )
                setDataSource(urlString)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isSpeaking.value = true
                }
                setOnCompletionListener {
                    _isSpeaking.value = false
                    _currentSpeakingText.value = null
                    stopFallbackPlayer()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "Direct stream error: what=$what extra=$extra")
                    _isSpeaking.value = false
                    _currentSpeakingText.value = null
                    stopFallbackPlayer()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Direct stream failed: ${e.message}")
            _isSpeaking.value = false
            _currentSpeakingText.value = null
        }
    }

    private fun stopFallbackPlayer() {
        try {
            fallbackPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (_: Exception) {
        } finally {
            fallbackPlayer = null
        }
    }

    fun stop() {
        activeUtterances.clear()
        try {
            tts?.stop()
        } catch (_: Exception) {}
        stopFallbackPlayer()
        _isSpeaking.value = false
        _currentSpeakingText.value = null
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
