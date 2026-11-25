package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class JapaneseContentRepository(private val context: Context) {

    private val allItemsList = mutableListOf<LearningItem>()
    private val hiraganaList = mutableListOf<KanaItem>()
    private val katakanaList = mutableListOf<KanaItem>()
    private val grammarLessonsList = mutableListOf<GrammarLesson>()
    private val particleRulesList = mutableListOf<ParticleRule>()
    private val particleQuestionsList = mutableListOf<ParticleQuestion>()
    private val interviewItemsList = mutableListOf<InterviewItem>()
    private val cdTracksList = mutableListOf<CdTrackInfo>()

    init {
        loadData()
    }

    private fun readAssetString(fileName: String): String {
        return context.assets.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.readText()
            }
        }
    }

    private fun loadData() {
        try {
            val allItemsJson = JSONArray(readAssetString("data/all_items.json"))
            for (i in 0 until allItemsJson.length()) {
                val obj = allItemsJson.getJSONObject(i)
                allItemsList.add(
                    LearningItem(
                        id = obj.optString("id", "item_$i"),
                        japanese = obj.optString("japanese", ""),
                        romaji = obj.optString("romaji", ""),
                        bangla = obj.optString("bangla", ""),
                        category = obj.optString("category", "VOCABULARY"),
                        lesson = obj.optInt("lesson", 1),
                        audioPhrase = if (obj.has("audioPhrase") && !obj.isNull("audioPhrase")) obj.getString("audioPhrase") else null,
                        english = if (obj.has("english") && !obj.isNull("english")) obj.getString("english") else null
                    )
                )
            }

            val hiraganaJson = JSONArray(readAssetString("data/hiragana_items.json"))
            for (i in 0 until hiraganaJson.length()) {
                val obj = hiraganaJson.getJSONObject(i)
                hiraganaList.add(
                    KanaItem(
                        id = obj.optString("id", "h_$i"),
                        japanese = obj.optString("japanese", ""),
                        romaji = obj.optString("romaji", ""),
                        bangla = obj.optString("bangla", ""),
                        category = "HIRAGANA"
                    )
                )
            }

            val katakanaJson = JSONArray(readAssetString("data/katakana_items.json"))
            for (i in 0 until katakanaJson.length()) {
                val obj = katakanaJson.getJSONObject(i)
                katakanaList.add(
                    KanaItem(
                        id = obj.optString("id", "k_$i"),
                        japanese = obj.optString("japanese", ""),
                        romaji = obj.optString("romaji", ""),
                        bangla = obj.optString("bangla", ""),
                        category = "KATAKANA"
                    )
                )
            }

            val grammarJson = JSONArray(readAssetString("data/grammar_lessons.json"))
            for (i in 0 until grammarJson.length()) {
                val lessonObj = grammarJson.getJSONObject(i)
                val rulesArray = lessonObj.optJSONArray("rules") ?: JSONArray()
                val rulesList = mutableListOf<GrammarRule>()
                for (j in 0 until rulesArray.length()) {
                    val rObj = rulesArray.getJSONObject(j)
                    rulesList.add(
                        GrammarRule(
                            pattern = rObj.optString("pattern", ""),
                            formula = rObj.optString("formula", ""),
                            explanation = rObj.optString("explanation", ""),
                            exampleJp = rObj.optString("exampleJp", ""),
                            exampleRomaji = rObj.optString("exampleRomaji", ""),
                            exampleBangla = rObj.optString("exampleBangla", "")
                        )
                    )
                }
                grammarLessonsList.add(
                    GrammarLesson(
                        lesson = lessonObj.optInt("lesson", i + 1),
                        title = lessonObj.optString("title", ""),
                        subtitle = lessonObj.optString("subtitle", ""),
                        rules = rulesList
                    )
                )
            }

            val particleRulesJson = JSONArray(readAssetString("data/particle_rules.json"))
            for (i in 0 until particleRulesJson.length()) {
                val obj = particleRulesJson.getJSONObject(i)
                val pointsArray = obj.optJSONArray("points") ?: JSONArray()
                val pointsList = mutableListOf<ParticlePoint>()
                for (p in 0 until pointsArray.length()) {
                    val pObj = pointsArray.getJSONObject(p)
                    pointsList.add(
                        ParticlePoint(
                            usage = pObj.optString("usage", ""),
                            exampleJp = pObj.optString("exampleJp", "")
                        )
                    )
                }
                particleRulesList.add(
                    ParticleRule(
                        particle = obj.optString("particle", ""),
                        romaji = obj.optString("romaji", ""),
                        title = obj.optString("title", ""),
                        usage = obj.optString("usage", ""),
                        formula = obj.optString("formula", ""),
                        exampleJp = obj.optString("exampleJp", ""),
                        exampleRomaji = obj.optString("exampleRomaji", ""),
                        exampleBangla = obj.optString("exampleBangla", ""),
                        points = pointsList
                    )
                )
            }

            val particleQuestionsJson = JSONArray(readAssetString("data/particle_questions.json"))
            for (i in 0 until particleQuestionsJson.length()) {
                val obj = particleQuestionsJson.getJSONObject(i)
                val optArray = obj.optJSONArray("options") ?: JSONArray()
                val options = mutableListOf<String>()
                for (k in 0 until optArray.length()) {
                    options.add(optArray.getString(k))
                }
                particleQuestionsList.add(
                    ParticleQuestion(
                        id = obj.optString("id", "pq_$i"),
                        number = obj.optInt("number", i + 1),
                        sentenceJp = obj.optString("sentenceJp", ""),
                        sentenceRomaji = obj.optString("sentenceRomaji", ""),
                        sentenceBangla = obj.optString("sentenceBangla", ""),
                        options = options,
                        correctIndex = obj.optInt("correctIndex", 0),
                        explanation = obj.optString("explanation", "")
                    )
                )
            }

            val interviewJson = JSONArray(readAssetString("data/interview_items.json"))
            for (i in 0 until interviewJson.length()) {
                val obj = interviewJson.getJSONObject(i)
                val answersArray = obj.optJSONArray("answers") ?: JSONArray()
                val answersList = mutableListOf<InterviewAnswer>()
                for (a in 0 until answersArray.length()) {
                    val ansObj = answersArray.getJSONObject(a)
                    answersList.add(
                        InterviewAnswer(
                            jp = ansObj.optString("jp", ""),
                            romaji = ansObj.optString("romaji", ""),
                            bn = ansObj.optString("bn", "")
                        )
                    )
                }
                interviewItemsList.add(
                    InterviewItem(
                        id = obj.optString("id", "iv_$i"),
                        number = obj.optInt("number", i + 1),
                        category = obj.optString("category", "General"),
                        categoryBn = obj.optString("categoryBn", "সাধারণ"),
                        questionJp = obj.optString("questionJp", ""),
                        questionRomaji = obj.optString("questionRomaji", ""),
                        questionBn = obj.optString("questionBn", ""),
                        answers = answersList,
                        audioPhrase = obj.optString("audioPhrase", obj.optString("questionJp", ""))
                    )
                )
            }

            try {
                val cdJson = JSONArray(readAssetString("data/cd_tracks.json"))
                for (i in 0 until cdJson.length()) {
                    val obj = cdJson.getJSONObject(i)
                    val cdNumber = obj.optInt("cdNumber", i + 1)
                    val resName = obj.optString("resName", "cd_%02d".format(cdNumber))
                    val audioResId = context.resources.getIdentifier(resName, "raw", context.packageName)
                    val scriptArray = obj.optJSONArray("script") ?: JSONArray()
                    val scriptList = mutableListOf<CdScriptLine>()
                    for (s in 0 until scriptArray.length()) {
                        val sObj = scriptArray.getJSONObject(s)
                        scriptList.add(
                            CdScriptLine(
                                speaker = sObj.optString("speaker", ""),
                                japanese = sObj.optString("japanese", ""),
                                romaji = sObj.optString("romaji", ""),
                                bangla = sObj.optString("bangla", "")
                            )
                        )
                    }
                    cdTracksList.add(
                        CdTrackInfo(
                            id = obj.optString("id", "cd_$cdNumber"),
                            cdNumber = cdNumber,
                            lessonNumber = obj.optInt("lessonNumber", 1),
                            lesson = obj.optString("lesson", "Lesson 1"),
                            title = obj.optString("title", "CD $cdNumber"),
                            subtitle = obj.optString("subtitle", ""),
                            part = obj.optString("part", ""),
                            resName = resName,
                            audioResId = audioResId,
                            script = scriptList
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllItems(): List<LearningItem> = allItemsList

    fun getVocabItems(lesson: Int? = null, query: String = ""): List<LearningItem> {
        return allItemsList.filter { item ->
            item.category == "VOCABULARY" &&
                (lesson == null || item.lesson == lesson) &&
                (query.isBlank() ||
                    item.japanese.contains(query, ignoreCase = true) ||
                    item.romaji.contains(query, ignoreCase = true) ||
                    item.bangla.contains(query, ignoreCase = true))
        }
    }

    fun getKanjiItems(range: String? = null, query: String = ""): List<LearningItem> {
        val kanji = allItemsList.filter { it.category == "KANJI" }
        val filteredByRange = when (range) {
            "#1-25" -> kanji.take(25)
            "#26-50" -> kanji.drop(25).take(25)
            "#51-75" -> kanji.drop(50).take(25)
            "#76-100" -> kanji.drop(75).take(25)
            "#101-110" -> kanji.drop(100)
            else -> kanji
        }
        return if (query.isBlank()) {
            filteredByRange
        } else {
            filteredByRange.filter {
                it.japanese.contains(query, ignoreCase = true) ||
                    it.romaji.contains(query, ignoreCase = true) ||
                    it.bangla.contains(query, ignoreCase = true)
            }
        }
    }

    fun getHiragana(): List<KanaItem> = hiraganaList

    fun getKatakana(): List<KanaItem> = katakanaList

    fun getGrammarLessons(): List<GrammarLesson> = grammarLessonsList

    fun getParticleRules(): List<ParticleRule> = particleRulesList

    fun getParticleQuestions(): List<ParticleQuestion> = particleQuestionsList

    fun getInterviewCategories(): List<String> =
        listOf("All", "Myself", "Family", "Japan", "General Knowledge")

    fun getInterviewItems(category: String? = null, query: String = ""): List<InterviewItem> {
        return interviewItemsList.filter { item ->
            val matchesCategory = category == null ||
                category == "All" ||
                item.category.equals(category, ignoreCase = true) ||
                (category.equals("Japan", ignoreCase = true) &&
                    item.category.startsWith("Japan", ignoreCase = true))
            matchesCategory &&
                (query.isBlank() ||
                    item.questionJp.contains(query, ignoreCase = true) ||
                    item.questionRomaji.contains(query, ignoreCase = true) ||
                    item.questionBn.contains(query, ignoreCase = true))
        }
    }

    fun getListeningItems(): List<LearningItem> =
        allItemsList.filter { it.category == "LISTENING" }

    fun getCountingItems(): List<LearningItem> =
        allItemsList.filter { it.category == "COUNTING" }

    fun getCdTracks(
        descending: Boolean = true,
        lesson: Int? = null,
        query: String = ""
    ): List<CdTrackInfo> {
        val filtered = cdTracksList.filter { track ->
            (lesson == null || track.lessonNumber == lesson) &&
                (query.isBlank() ||
                    track.title.contains(query, ignoreCase = true) ||
                    track.subtitle.contains(query, ignoreCase = true) ||
                    track.lesson.contains(query, ignoreCase = true) ||
                    "CD ${track.cdNumber}".contains(query, ignoreCase = true) ||
                    "${track.cdNumber}".contains(query))
        }
        return if (descending) {
            filtered.sortedByDescending { it.cdNumber }
        } else {
            filtered.sortedBy { it.cdNumber }
        }
    }
}

