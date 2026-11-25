package com.example.ui.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.UserProfile
import com.example.data.repository.UserFirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Guest(val displayName: String, val userId: String) : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthManager(
    private val context: Context,
    private val firestoreRepository: UserFirestoreRepository
) {
    private val TAG = "AuthManager"
    private val prefs: SharedPreferences =
        context.getSharedPreferences("japanese_auth_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val currentUser = auth.currentUser
        val savedGuestName = prefs.getString("guest_display_name", null)
        val savedGuestId = prefs.getString("guest_user_id", null)

        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
        } else if (!savedGuestName.isNullOrBlank() && !savedGuestId.isNullOrBlank()) {
            _authState.value = AuthState.Guest(savedGuestName, savedGuestId)
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun trySilentSignIn() {
        checkExistingSession()
    }

    suspend fun signInWithGoogleIdToken(idToken: String) {
        _authState.value = AuthState.Loading
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: auth.currentUser
            if (user != null) {
                prefs.edit().remove("guest_display_name").apply()
                try {
                    val existing = firestoreRepository.getUserProfile(user.uid)
                    val profile = UserProfile(
                        userId = user.uid,
                        displayName = user.displayName?.takeIf { it.isNotBlank() } ?: existing?.displayName ?: "Japanese Learner",
                        email = user.email ?: existing?.email,
                        photoUrl = user.photoUrl?.toString() ?: existing?.photoUrl,
                        studySeconds = existing?.studySeconds ?: 0L,
                        quizCount = existing?.quizCount ?: 0L,
                        quizCorrect = existing?.quizCorrect ?: 0L
                    )
                    firestoreRepository.saveUserProfile(profile)
                } catch (e: Exception) {
                    Log.w(TAG, "Profile sync warning after sign-in: ${e.message}")
                }
                _authState.value = AuthState.Authenticated(user)
            } else {
                _authState.value = AuthState.Error("Authentication failed: no user returned.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Firebase sign-in failed", e)
            _authState.value = AuthState.Error(e.localizedMessage ?: "Sign-in failed")
        }
    }

    suspend fun loginAsGuest(name: String) {
        val cleanName = name.trim().ifBlank { "Guest Learner" }
        var guestId = prefs.getString("guest_user_id", null)
        if (guestId.isNullOrBlank()) {
            guestId = "guest_${UUID.randomUUID().toString().take(8)}"
            prefs.edit().putString("guest_user_id", guestId).apply()
        }
        prefs.edit().putString("guest_display_name", cleanName).apply()

        val existing = firestoreRepository.getUserProfile(guestId)
        val profile = UserProfile(
            userId = guestId,
            displayName = cleanName,
            studySeconds = existing?.studySeconds ?: 0L,
            quizCount = existing?.quizCount ?: 0L,
            quizCorrect = existing?.quizCorrect ?: 0L
        )
        firestoreRepository.saveUserProfile(profile)

        _authState.value = AuthState.Guest(cleanName, guestId)
    }

    fun signOut() {
        prefs.edit().remove("guest_display_name").apply()
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Auth sign out error: ${e.message}")
        }
        _authState.value = AuthState.Idle
    }
}
