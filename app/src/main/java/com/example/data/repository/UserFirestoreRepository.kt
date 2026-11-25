package com.example.data.repository

import com.example.data.model.Bookmark
import com.example.data.model.QuizHistory
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserFirestoreRepository(
    private val firestore: FirebaseFirestore,
    private val guestLocalRepository: GuestLocalRepository? = null
) {

    private fun userDoc(userId: String) = firestore.collection("users").document(userId)
    private fun quizColl(userId: String) = userDoc(userId).collection("quiz_history")
    private fun bookmarkColl(userId: String) = userDoc(userId).collection("bookmarks")

    suspend fun getUserProfile(userId: String): UserProfile? {
        if (userId.startsWith("guest_") && guestLocalRepository != null) {
            return guestLocalRepository.getUserProfile(userId)
        }
        val snapshot = userDoc(userId).get().await()
        return snapshot.toObject(UserProfile::class.java)
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        if (userId.startsWith("guest_") && guestLocalRepository != null) {
            return guestLocalRepository.observeUserProfile(userId)
        }
        return callbackFlow {
            val listener = userDoc(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }
            awaitClose { listener.remove() }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        if (profile.userId.startsWith("guest_") && guestLocalRepository != null) {
            guestLocalRepository.saveUserProfile(profile)
            return
        }
        val data = hashMapOf<String, Any?>(
            "userId" to profile.userId,
            "displayName" to profile.displayName.ifBlank { "Learner" },
            "email" to profile.email,
            "photoUrl" to profile.photoUrl,
            "studySeconds" to profile.studySeconds,
            "quizCount" to profile.quizCount,
            "quizCorrect" to profile.quizCorrect,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val docRef = userDoc(profile.userId)
        val exists = docRef.get().await().exists()
        if (!exists) {
            data["createdAt"] = FieldValue.serverTimestamp()
            docRef.set(data).await()
        } else {
            docRef.set(data, SetOptions.merge()).await()
        }
    }

    suspend fun updateStudySeconds(userId: String, additionalSeconds: Long, displayName: String = "Learner") {
        if (additionalSeconds <= 0) return
        if (userId.startsWith("guest_") && guestLocalRepository != null) {
            guestLocalRepository.updateStudySeconds(userId, additionalSeconds, displayName)
            return
        }
        val docRef = userDoc(userId)
        val exists = docRef.get().await().exists()
        if (exists) {
            docRef.update(
                "studySeconds", FieldValue.increment(additionalSeconds),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } else {
            val initial = hashMapOf<String, Any?>(
                "userId" to userId,
                "displayName" to displayName,
                "studySeconds" to additionalSeconds,
                "quizCount" to 0L,
                "quizCorrect" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(initial).await()
        }
    }

    suspend fun recordQuizResult(quizHistory: QuizHistory, displayName: String = "Learner") {
        if (quizHistory.userId.startsWith("guest_") && guestLocalRepository != null) {
            guestLocalRepository.recordQuizResult(quizHistory, displayName)
            return
        }
        val qDoc = quizColl(quizHistory.userId).document(quizHistory.quizId)
        val qData = hashMapOf<String, Any?>(
            "quizId" to quizHistory.quizId,
            "userId" to quizHistory.userId,
            "category" to quizHistory.category,
            "lesson" to quizHistory.lesson,
            "score" to quizHistory.score,
            "totalQuestions" to quizHistory.totalQuestions,
            "percentage" to quizHistory.percentage,
            "createdAt" to FieldValue.serverTimestamp()
        )
        qDoc.set(qData).await()

        val uDoc = userDoc(quizHistory.userId)
        val exists = uDoc.get().await().exists()
        if (exists) {
            uDoc.update(
                "quizCount", FieldValue.increment(1),
                "quizCorrect", FieldValue.increment(quizHistory.score),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } else {
            val initial = hashMapOf<String, Any?>(
                "userId" to quizHistory.userId,
                "displayName" to displayName,
                "studySeconds" to 0L,
                "quizCount" to 1L,
                "quizCorrect" to quizHistory.score,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            uDoc.set(initial).await()
        }
    }

    fun observeQuizHistory(userId: String): Flow<List<QuizHistory>> {
        if (userId.startsWith("guest_") && guestLocalRepository != null) {
            return guestLocalRepository.observeQuizHistory(userId)
        }
        return callbackFlow {
            val listener = quizColl(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull { it.toObject(QuizHistory::class.java) }
                        ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
    }

    private fun sanitizeBookmark(bookmark: Bookmark): Bookmark {
        val safeId = bookmark.bookmarkId
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(120)
            .ifBlank { "bm_${System.currentTimeMillis()}" }
        return bookmark.copy(
            bookmarkId = safeId,
            itemId = bookmark.itemId.take(120),
            category = bookmark.category.take(50),
            japanese = bookmark.japanese.take(300),
            romaji = bookmark.romaji.take(300),
            bangla = bookmark.bangla.take(500)
        )
    }

    suspend fun toggleBookmark(bookmark: Bookmark): Boolean {
        val safe = sanitizeBookmark(bookmark)
        if (safe.userId.startsWith("guest_") && guestLocalRepository != null) {
            return guestLocalRepository.toggleBookmark(safe)
        }
        val docRef = bookmarkColl(safe.userId).document(safe.bookmarkId)
        val snap = docRef.get().await()
        if (snap.exists()) {
            docRef.delete().await()
            return false
        }
        // Also check legacy bm_<itemId> document if toggling a flashcard problem
        if (safe.bookmarkId.startsWith("fp_")) {
            val legacyId = "bm_${safe.itemId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")}".take(120)
            val legacyRef = bookmarkColl(safe.userId).document(legacyId)
            val legacySnap = legacyRef.get().await()
            if (legacySnap.exists()) {
                legacyRef.delete().await()
                return false
            }
        }
        val data = hashMapOf<String, Any?>(
            "bookmarkId" to safe.bookmarkId,
            "userId" to safe.userId,
            "itemId" to safe.itemId,
            "category" to safe.category,
            "japanese" to safe.japanese,
            "romaji" to safe.romaji,
            "bangla" to safe.bangla,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
        return true
    }

    suspend fun saveProblem(bookmark: Bookmark) {
        val safe = sanitizeBookmark(bookmark)
        if (safe.userId.startsWith("guest_") && guestLocalRepository != null) {
            guestLocalRepository.saveProblem(safe)
            return
        }
        val docRef = bookmarkColl(safe.userId).document(safe.bookmarkId)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            val data = hashMapOf<String, Any?>(
                "bookmarkId" to safe.bookmarkId,
                "userId" to safe.userId,
                "itemId" to safe.itemId,
                "category" to safe.category,
                "japanese" to safe.japanese,
                "romaji" to safe.romaji,
                "bangla" to safe.bangla,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
        }
    }

    suspend fun removeProblem(bookmark: Bookmark) {
        val safe = sanitizeBookmark(bookmark)
        if (safe.userId.startsWith("guest_") && guestLocalRepository != null) {
            guestLocalRepository.removeBookmarkById(safe.bookmarkId)
            return
        }
        bookmarkColl(safe.userId).document(safe.bookmarkId).delete().await()
    }

    fun observeBookmarks(userId: String): Flow<List<Bookmark>> {
        if (userId.startsWith("guest_") && guestLocalRepository != null) {
            return guestLocalRepository.observeBookmarks(userId)
        }
        return callbackFlow {
            val listener = bookmarkColl(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull { it.toObject(Bookmark::class.java) }
                        ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
    }
}
