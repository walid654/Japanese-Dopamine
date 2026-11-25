package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Bookmark
import com.example.data.model.QuizHistory
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class GuestLocalRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("japanese_guest_data", Context.MODE_PRIVATE)

    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    private val quizHistoryFlow = MutableStateFlow<List<QuizHistory>>(emptyList())
    private val bookmarksFlow = MutableStateFlow<List<Bookmark>>(emptyList())

    init {
        loadAll()
    }

    private fun loadAll() {
        val uid = prefs.getString("guest_uid", "guest_default") ?: "guest_default"
        val name = prefs.getString("guest_name", "Guest Learner") ?: "Guest Learner"
        val studySeconds = prefs.getLong("guest_study_seconds", 0L)
        val quizCount = prefs.getLong("guest_quiz_count", 0L)
        val quizCorrect = prefs.getLong("guest_quiz_correct", 0L)

        profileFlow.value = UserProfile(
            userId = uid,
            displayName = name,
            studySeconds = studySeconds,
            quizCount = quizCount,
            quizCorrect = quizCorrect
        )

        // Load Quiz History
        val quizJsonStr = prefs.getString("guest_quiz_history", "[]") ?: "[]"
        val qList = mutableListOf<QuizHistory>()
        try {
            val qArray = JSONArray(quizJsonStr)
            for (i in 0 until qArray.length()) {
                val obj = qArray.getJSONObject(i)
                qList.add(
                    QuizHistory(
                        quizId = obj.optString("quizId", "q_$i"),
                        userId = uid,
                        category = obj.optString("category", "VOCABULARY"),
                        lesson = obj.optLong("lesson", 1L),
                        score = obj.optLong("score", 0L),
                        totalQuestions = obj.optLong("totalQuestions", 10L),
                        percentage = obj.optLong("percentage", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        quizHistoryFlow.value = qList

        // Load Bookmarks
        val bmJsonStr = prefs.getString("guest_bookmarks", "[]") ?: "[]"
        val bList = mutableListOf<Bookmark>()
        try {
            val bmArray = JSONArray(bmJsonStr)
            for (i in 0 until bmArray.length()) {
                val obj = bmArray.getJSONObject(i)
                bList.add(
                    Bookmark(
                        bookmarkId = obj.optString("bookmarkId", "bm_$i"),
                        userId = uid,
                        itemId = obj.optString("itemId", ""),
                        category = obj.optString("category", ""),
                        japanese = obj.optString("japanese", ""),
                        romaji = obj.optString("romaji", ""),
                        bangla = obj.optString("bangla", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bookmarksFlow.value = bList
    }

    fun getUserProfile(userId: String): UserProfile? {
        loadAll()
        return profileFlow.value
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        loadAll()
        return profileFlow.asStateFlow()
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString("guest_uid", profile.userId)
            .putString("guest_name", profile.displayName)
            .putLong("guest_study_seconds", profile.studySeconds)
            .putLong("guest_quiz_count", profile.quizCount)
            .putLong("guest_quiz_correct", profile.quizCorrect)
            .apply()
        profileFlow.value = profile
    }

    fun updateStudySeconds(userId: String, additionalSeconds: Long, displayName: String = "Guest Learner") {
        if (additionalSeconds <= 0) return
        val current = prefs.getLong("guest_study_seconds", 0L)
        val newSeconds = current + additionalSeconds
        prefs.edit().putLong("guest_study_seconds", newSeconds).apply()

        profileFlow.value = profileFlow.value?.copy(studySeconds = newSeconds) ?: UserProfile(
            userId = userId,
            displayName = displayName,
            studySeconds = newSeconds
        )
    }

    fun recordQuizResult(quizHistory: QuizHistory, displayName: String = "Guest Learner") {
        val currentList = quizHistoryFlow.value.toMutableList()
        currentList.add(0, quizHistory) // latest first
        quizHistoryFlow.value = currentList

        // Save to prefs
        val array = JSONArray()
        for (q in currentList) {
            val obj = JSONObject()
            obj.put("quizId", q.quizId)
            obj.put("category", q.category)
            obj.put("lesson", q.lesson)
            obj.put("score", q.score)
            obj.put("totalQuestions", q.totalQuestions)
            obj.put("percentage", q.percentage)
            array.put(obj)
        }

        val count = prefs.getLong("guest_quiz_count", 0L) + 1
        val correct = prefs.getLong("guest_quiz_correct", 0L) + quizHistory.score
        prefs.edit()
            .putString("guest_quiz_history", array.toString())
            .putLong("guest_quiz_count", count)
            .putLong("guest_quiz_correct", correct)
            .apply()

        profileFlow.value = profileFlow.value?.copy(
            quizCount = count,
            quizCorrect = correct
        )
    }

    fun observeQuizHistory(userId: String): Flow<List<QuizHistory>> = quizHistoryFlow.asStateFlow()

    fun toggleBookmark(bookmark: Bookmark): Boolean {
        val currentList = bookmarksFlow.value.toMutableList()
        val exists = currentList.any { it.bookmarkId == bookmark.bookmarkId || (it.itemId == bookmark.itemId && !it.bookmarkId.startsWith("qp_") && !bookmark.bookmarkId.startsWith("qp_")) }
        val added = if (exists) {
            currentList.removeAll { it.bookmarkId == bookmark.bookmarkId || (it.itemId == bookmark.itemId && !it.bookmarkId.startsWith("qp_") && !bookmark.bookmarkId.startsWith("qp_")) }
            false
        } else {
            currentList.add(0, bookmark)
            true
        }
        persistBookmarks(currentList)
        return added
    }

    fun saveProblem(bookmark: Bookmark) {
        val currentList = bookmarksFlow.value.toMutableList()
        val alreadyExists = currentList.any { it.bookmarkId == bookmark.bookmarkId }
        if (!alreadyExists) {
            currentList.add(0, bookmark)
            persistBookmarks(currentList)
        }
    }

    fun removeBookmarkById(bookmarkId: String) {
        val currentList = bookmarksFlow.value.toMutableList()
        if (currentList.removeAll { it.bookmarkId == bookmarkId }) {
            persistBookmarks(currentList)
        }
    }

    private fun persistBookmarks(list: List<Bookmark>) {
        bookmarksFlow.value = list
        val array = JSONArray()
        for (b in list) {
            val obj = JSONObject()
            obj.put("bookmarkId", b.bookmarkId)
            obj.put("itemId", b.itemId)
            obj.put("category", b.category)
            obj.put("japanese", b.japanese)
            obj.put("romaji", b.romaji)
            obj.put("bangla", b.bangla)
            array.put(obj)
        }
        prefs.edit().putString("guest_bookmarks", array.toString()).apply()
    }

    fun observeBookmarks(userId: String): Flow<List<Bookmark>> = bookmarksFlow.asStateFlow()

    fun clearGuestSession() {
        prefs.edit()
            .remove("guest_name")
            .apply()
        profileFlow.value = null
    }
}
