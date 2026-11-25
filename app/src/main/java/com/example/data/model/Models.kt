package com.example.data.model

import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

@Immutable
data class LearningItem(
    val id: String,
    val japanese: String,
    val romaji: String,
    val bangla: String,
    val category: String,
    val lesson: Int = 1,
    val audioPhrase: String? = null,
    val english: String? = null
)

@Immutable
data class KanaItem(
    val id: String,
    val japanese: String,
    val romaji: String,
    val bangla: String,
    val category: String
)

@Immutable
data class GrammarRule(
    val pattern: String,
    val formula: String,
    val explanation: String,
    val exampleJp: String,
    val exampleRomaji: String,
    val exampleBangla: String
)

@Immutable
data class GrammarLesson(
    val lesson: Int,
    val title: String,
    val subtitle: String,
    val rules: List<GrammarRule>
)

@Immutable
data class ParticlePoint(
    val usage: String,
    val exampleJp: String
)

@Immutable
data class ParticleRule(
    val particle: String,
    val romaji: String,
    val title: String,
    val usage: String,
    val formula: String,
    val exampleJp: String,
    val exampleRomaji: String,
    val exampleBangla: String,
    val points: List<ParticlePoint> = emptyList()
)

@Immutable
data class ParticleQuestion(
    val id: String,
    val number: Int,
    val sentenceJp: String,
    val sentenceRomaji: String,
    val sentenceBangla: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Immutable
data class InterviewAnswer(
    val jp: String,
    val romaji: String,
    val bn: String
)

@Immutable
data class InterviewItem(
    val id: String,
    val number: Int,
    val category: String,
    val categoryBn: String,
    val questionJp: String,
    val questionRomaji: String,
    val questionBn: String,
    val answers: List<InterviewAnswer>,
    val audioPhrase: String? = null
)

// Firestore Cloud Entities

@Immutable
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val studySeconds: Long = 0L,
    val quizCount: Long = 0L,
    val quizCorrect: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Immutable
data class QuizHistory(
    @DocumentId val id: String = "",
    val quizId: String = "",
    val userId: String = "",
    val category: String = "",
    val lesson: Long = 0L,
    val score: Long = 0L,
    val totalQuestions: Long = 0L,
    val percentage: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Immutable
data class Bookmark(
    @DocumentId val id: String = "",
    val bookmarkId: String = "",
    val userId: String = "",
    val itemId: String = "",
    val category: String = "",
    val japanese: String = "",
    val romaji: String = "",
    val bangla: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Immutable
data class CdScriptLine(
    val speaker: String,
    val japanese: String,
    val romaji: String,
    val bangla: String
)

@Immutable
data class CdTrackInfo(
    val id: String,
    val cdNumber: Int,
    val lessonNumber: Int = 1,
    val lesson: String,
    val title: String,
    val subtitle: String,
    val part: String = "",
    val resName: String = "",
    val audioResId: Int = 0,
    val script: List<CdScriptLine> = emptyList()
)

