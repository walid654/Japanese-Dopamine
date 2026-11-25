package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Bookmark
import com.example.data.model.QuizHistory
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class UserFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private val repo by lazy { UserFirestoreRepository(firestore) }

    @Test
    fun saveUserProfile_and_getUserProfile_succeeds_for_owner() = runBlocking {
        val uid = signInTestUser("testuser1@example.com")
        val profile = UserProfile(
            userId = uid,
            displayName = "Sakib Learner",
            email = "testuser1@example.com",
            studySeconds = 120L,
            quizCount = 2L,
            quizCorrect = 10L
        )

        withTimeout(5000L) {
            repo.saveUserProfile(profile)
        }

        val loaded = withTimeout(5000L) {
            repo.getUserProfile(uid)
        }

        assertNotNull(loaded)
        assertEquals(uid, loaded?.userId)
        assertEquals("Sakib Learner", loaded?.displayName)
    }

    @Test
    fun recordQuizResult_and_toggleBookmark_succeeds_for_owner() = runBlocking {
        val uid = signInTestUser("testuser2@example.com")
        val quiz = QuizHistory(
            quizId = "quiz_101",
            userId = uid,
            category = "VOCABULARY",
            lesson = 1L,
            score = 9L,
            totalQuestions = 10L,
            percentage = 90L
        )

        withTimeout(5000L) {
            repo.recordQuizResult(quiz)
        }

        val bookmark = Bookmark(
            bookmarkId = "bm_1",
            userId = uid,
            itemId = "h1",
            category = "HIRAGANA",
            japanese = "あ",
            romaji = "a",
            bangla = "অ / আ"
        )

        val added = withTimeout(5000L) {
            repo.toggleBookmark(bookmark)
        }
        assertTrue(added)

        val removed = withTimeout(5000L) {
            repo.toggleBookmark(bookmark)
        }
        assertFalse(removed)
    }

    @Test
    fun crossUser_access_is_rejected_by_rules() = runBlocking {
        val aliceUid = signInTestUser("alice@example.com")
        val aliceProfile = UserProfile(
            userId = aliceUid,
            displayName = "Alice"
        )
        repo.saveUserProfile(aliceProfile)

        // Now sign in as Bob
        val bobUid = signInTestUser("bob@example.com")
        assertFalse(aliceUid == bobUid)

        // Bob tries to read Alice's profile
        try {
            withTimeout(3000L) {
                repo.getUserProfile(aliceUid)
            }
            fail("Expected PERMISSION_DENIED when Bob tries to read Alice's profile")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertTrue(
                "Expected PERMISSION_DENIED but got: $e",
                firestoreException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
    }
}
