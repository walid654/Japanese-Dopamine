package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.repository.GuestLocalRepository
import com.example.data.repository.JapaneseContentRepository
import com.example.data.repository.UserFirestoreRepository
import com.example.ui.auth.AuthManager
import com.example.ui.auth.AuthState
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.*
import com.example.ui.theme.JapaneseDopamineTheme
import com.example.util.TtsManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager
    private lateinit var contentRepository: JapaneseContentRepository
    private lateinit var guestRepository: GuestLocalRepository
    private lateinit var firestoreRepository: UserFirestoreRepository
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configurePreferredRefreshRate()

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dbIdRes = resources.getIdentifier("firestore_database_id", "string", packageName)
        val databaseId = if (dbIdRes != 0) getString(dbIdRes) else "(default)"
        val firestore = try {
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), databaseId)
        } catch (e: Exception) {
            FirebaseFirestore.getInstance()
        }

        ttsManager = TtsManager(this)
        contentRepository = JapaneseContentRepository(this)
        guestRepository = GuestLocalRepository(this)
        firestoreRepository = UserFirestoreRepository(firestore, guestRepository)
        authManager = AuthManager(this, firestoreRepository)

        setContent {
            JapaneseDopamineTheme {
                val authState by authManager.authState.collectAsState()
                val coroutineScope = rememberCoroutineScope()
                var showIntro by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = authState) {
                    is AuthState.Guest, is AuthState.Authenticated -> {
                        val userId = if (state is AuthState.Guest) {
                            state.userId
                        } else {
                            (state as AuthState.Authenticated).user.uid
                        }

                        val displayName = if (state is AuthState.Guest) {
                            state.displayName
                        } else {
                            (state as AuthState.Authenticated).user.displayName ?: "Learner"
                        }

                        val userProfile by firestoreRepository.observeUserProfile(userId).collectAsState(initial = null)
                        var currentScreen by remember { mutableStateOf(ScreenDestination.DASHBOARD) }

                        // Study timer: tracks 30 seconds of active foreground study
                        LaunchedEffect(userId) {
                            while (true) {
                                delay(30000L) // 30 seconds
                                firestoreRepository.updateStudySeconds(
                                    userId = userId,
                                    additionalSeconds = 30L,
                                    displayName = displayName
                                )
                            }
                        }

                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            when (currentScreen) {
                                ScreenDestination.DASHBOARD -> {
                                    DashboardScreen(
                                        userProfile = userProfile,
                                        onNavigate = { currentScreen = it },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.KANA -> {
                                    KanaScreen(
                                        repository = contentRepository,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.VOCAB -> {
                                    VocabHubScreen(
                                        repository = contentRepository,
                                        firestoreRepository = firestoreRepository,
                                        userId = userId,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.KANJI -> {
                                    KanjiHubScreen(
                                        repository = contentRepository,
                                        firestoreRepository = firestoreRepository,
                                        userId = userId,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.GRAMMAR -> {
                                    GrammarScreen(
                                        repository = contentRepository,
                                        firestoreRepository = firestoreRepository,
                                        userId = userId,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.INTERVIEW -> {
                                    InterviewScreen(
                                        repository = contentRepository,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.LISTENING -> {
                                    ListeningScreen(
                                        repository = contentRepository,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.COUNTING -> {
                                    CountingScreen(
                                        repository = contentRepository,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.BOOKMARKS -> {
                                    BookmarksScreen(
                                        repository = contentRepository,
                                        firestoreRepository = firestoreRepository,
                                        userId = userId,
                                        ttsManager = ttsManager,
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                ScreenDestination.PROFILE -> {
                                    ProfileScreen(
                                        userProfile = userProfile,
                                        firestoreRepository = firestoreRepository,
                                        userId = userId,
                                        onSignOut = {
                                            authManager.signOut()
                                        },
                                        onBack = { currentScreen = ScreenDestination.DASHBOARD },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        AuthScreen(
                            authState = state,
                            onGoogleSignIn = { idToken ->
                                coroutineScope.launch {
                                    authManager.signInWithGoogleIdToken(idToken)
                                }
                            },
                            onGuestLogin = { name ->
                                coroutineScope.launch {
                                    authManager.loginAsGuest(name)
                                }
                            }
                        )
                    }
                }

                    if (showIntro) {
                        IntroSplashScreen(
                            onFinished = { showIntro = false }
                        )
                    }
                }
            }
        }
    }

    private fun configurePreferredRefreshRate() {
        try {
            val activeDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            } ?: return

            val currentMode = activeDisplay.mode
            val modes = activeDisplay.supportedModes
            // Prefer up to ~90Hz at the native physical resolution, falling back gracefully on 60Hz panels.
            val sameSizeModes = modes.filter {
                it.physicalWidth == currentMode.physicalWidth &&
                    it.physicalHeight == currentMode.physicalHeight
            }.ifEmpty { modes.toList() }

            val preferred90Hz = sameSizeModes
                .filter { it.refreshRate in 59.5f..92f }
                .maxByOrNull { it.refreshRate }

            if (preferred90Hz != null && preferred90Hz.modeId != currentMode.modeId) {
                val params = window.attributes
                params.preferredDisplayModeId = preferred90Hz.modeId
                window.attributes = params
            }
        } catch (_: Exception) {
            // Keep system default display mode on OEMs that restrict mode switching
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
