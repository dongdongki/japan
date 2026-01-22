package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.model.Word
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * Repository for managing Word data (words, verbs, particles)
 * Optimized with caching and O(1) ID lookup
 */
class WordRepository(private val context: Context) {

    private var wordData: List<Word>? = null
    private var adjectiveData: List<Word>? = null
    private var verbsData: List<Word>? = null
    private var particlesData: List<Word>? = null
    private var adverbData: List<Word>? = null
    private var conjunctionData: List<Word>? = null

    // Cached combined list and ID lookup map for O(1) access
    private var cachedAllWords: List<Word>? = null
    private var wordByIdMap: Map<Int, Word>? = null

    // Reusable Gson instance (avoid creating new instances)
    private val gson = Gson()
    private val wordListType = object : TypeToken<List<Map<String, String>>>() {}.type

    init {
        loadData()
    }

    private fun loadData() {
        try {
            var currentId = 0

            // Load nouns
            wordData = loadWordFile("noun.json", "noun", currentId)
            currentId += wordData?.size ?: 0

            // Load adjectives
            adjectiveData = loadWordFile("adjective.json", "adjective", currentId)
            currentId += adjectiveData?.size ?: 0

            // Load verbs
            verbsData = loadWordFile("verbs.json", "verb", currentId)
            currentId += verbsData?.size ?: 0

            // Load particles
            particlesData = loadWordFile("particles.json", "particle", currentId)
            currentId += particlesData?.size ?: 0

            // Load adverbs
            adverbData = loadWordFile("adverb.json", "adverb", currentId)
            currentId += adverbData?.size ?: 0

            // Load conjunctions
            conjunctionData = loadWordFile("conjunction.json", "conjunction", currentId)

            // Pre-build cached lists for performance
            buildCaches()

            android.util.Log.d(
                "WordRepository",
                "Loaded ${wordData?.size} nouns, ${adjectiveData?.size} adjectives, ${verbsData?.size} verbs, ${particlesData?.size} particles, ${adverbData?.size} adverbs, ${conjunctionData?.size} conjunctions"
            )
        } catch (e: Exception) {
            android.util.Log.e("WordRepository", "Error loading word data", e)
        }
    }

    /**
     * Helper to load a single word file - reduces code duplication
     * Uses .use{} to automatically close streams
     */
    private fun loadWordFile(fileName: String, defaultPartOfSpeech: String, startId: Int): List<Word> {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val listAsMap: List<Map<String, String>> = gson.fromJson(reader, wordListType)

                    listAsMap.mapIndexed { index, map ->
                        Word(
                            id = startId + index,
                            kanji = map["kanji"] ?: "",
                            meaning = map["meaning"] ?: "",
                            hiragana = map["hiragana"] ?: "",
                            partOfSpeech = map["partOfSpeech"] ?: defaultPartOfSpeech
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WordRepository", "Error loading $fileName", e)
            emptyList()
        }
    }

    /**
     * Build cached combined list and ID lookup map
     */
    private fun buildCaches() {
        val allWords = mutableListOf<Word>()
        allWords.addAll(wordData.orEmpty())
        allWords.addAll(adjectiveData.orEmpty())
        allWords.addAll(verbsData.orEmpty())
        allWords.addAll(particlesData.orEmpty())
        allWords.addAll(adverbData.orEmpty())
        allWords.addAll(conjunctionData.orEmpty())

        cachedAllWords = allWords
        wordByIdMap = allWords.associateBy { it.id }
    }

    /**
     * Get all words (basic vocabulary only)
     */
    fun getWords(): List<Word> = wordData.orEmpty()

    /**
     * Get all verbs
     */
    fun getVerbs(): List<Word> = verbsData.orEmpty()

    /**
     * Get all particles
     */
    fun getParticles(): List<Word> = particlesData.orEmpty()

    /**
     * Get words by grammar type (legacy - for backward compatibility)
     */
    fun getWordsByType(grammarType: String): List<Word> {
        return when (grammarType) {
            "verbs" -> verbsData.orEmpty()
            "particles" -> particlesData.orEmpty()
            else -> wordData.orEmpty()
        }
    }

    /**
     * Get words by part of speech (noun, adjective, verb, particle, adverb, conjunction)
     */
    fun getWordsByPartOfSpeech(partOfSpeech: String): List<Word> {
        return when (partOfSpeech) {
            "noun" -> wordData.orEmpty()
            "adjective" -> adjectiveData.orEmpty()
            "verb" -> verbsData.orEmpty()
            "particle" -> particlesData.orEmpty()
            "adverb" -> adverbData.orEmpty()
            "conjunction" -> conjunctionData.orEmpty()
            else -> getAllWords()
        }
    }

    /**
     * Get all words combined (nouns + adjectives + verbs + particles + adverbs + conjunctions)
     * Returns cached list - O(1) operation
     */
    fun getAllWords(): List<Word> {
        return cachedAllWords ?: run {
            buildCaches()
            cachedAllWords ?: emptyList()
        }
    }

    /**
     * Get word by ID - O(1) lookup using cached map
     */
    fun getWordById(id: Int): Word? {
        return wordByIdMap?.get(id) ?: run {
            buildCaches()
            wordByIdMap?.get(id)
        }
    }
}
