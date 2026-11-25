package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun testAll87CdAudioFilesExistInRaw() {
        val rawDir = File("src/main/res/raw")
        assertTrue("raw directory must exist", rawDir.exists() && rawDir.isDirectory)

        val cdFiles = (1..87).map { File(rawDir, "cd_%02d.mp3".format(it)) }
        for (f in cdFiles) {
            assertTrue("Audio file ${f.name} must exist and not be empty", f.exists() && f.length() > 1000)
        }
        assertEquals("Should have all 87 CD audio files", 87, cdFiles.size)
    }

    @Test
    fun testCdTracksJsonIntegrity() {
        val jsonFile = File("src/main/assets/data/cd_tracks.json")
        assertTrue("cd_tracks.json must exist", jsonFile.exists())
        val text = jsonFile.readText()

        // Check that all 87 CDs are present
        for (i in 1..87) {
            assertTrue("Must contain \"cdNumber\": $i", text.contains("\"cdNumber\": $i"))
            assertTrue("Must contain \"cd_$i\"", text.contains("\"cd_$i\""))
        }
    }

    @Test
    fun testVocabularyHasWatashiAndAll25Lessons() {
        val jsonFile = File("src/main/assets/data/all_items.json")
        assertTrue("all_items.json must exist", jsonFile.exists())
        val text = jsonFile.readText()

        assertTrue("Must contain 'わたし'", text.contains("\"japanese\": \"わたし\""))
        assertTrue("Must contain 'watashi'", text.contains("\"romaji\": \"watashi\""))
        assertTrue("Must contain 'আমি'", text.contains("\"bangla\": \"আমি\""))

        for (l in 1..25) {
            assertTrue("Must contain lesson $l", text.contains("\"lesson\": $l"))
        }
    }

    @Test
    fun testGrammarHas21Lessons() {
        val jsonFile = File("src/main/assets/data/grammar_lessons.json")
        assertTrue("grammar_lessons.json must exist", jsonFile.exists())
        val text = jsonFile.readText()

        for (l in 1..21) {
            assertTrue("Must contain grammar lesson $l", text.contains("\"lesson\": $l"))
        }
    }
}
