package com.example.myapplication.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.KanaCharacter
import com.example.myapplication.model.KanaData
import com.example.myapplication.model.KanaRow
import com.example.myapplication.model.Word
import com.example.myapplication.model.Song
import com.example.myapplication.model.SongInfo
import com.example.myapplication.model.Sentence
import com.example.myapplication.repository.SongRepository
import com.example.myapplication.repository.SentenceRepository
import com.example.myapplication.repository.WordRepository
import com.example.myapplication.repository.KanaRepository
import com.example.myapplication.repository.PreferencesRepository
import com.example.myapplication.repository.DailyWordRepository
import com.example.myapplication.model.DailyWord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    application: Application,
    private val kanaRepository: KanaRepository,
    private val wordRepository: WordRepository,
    private val songRepository: SongRepository,
    private val sentenceRepository: SentenceRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dailyWordRepository: DailyWordRepository
) : AndroidViewModel(application) {

    // Use Application-level TTS instead of creating a new instance (prevents resource duplication)
    // TTS is managed by JapaneseStudyApp singleton

    private var kanaData: KanaData? = null
    private var wordData: List<Word>? = null
    val weakWords = MutableLiveData<Set<Int>>(emptySet())
    val weakSentences = MutableLiveData<Set<Int>>(emptySet())
    val showMeaning = MutableLiveData(true)
    val penWidth = MutableLiveData(12f)
    val eraserWidth = MutableLiveData(40f)

    // Cached SharedPreferences instance to avoid repeated getSharedPreferences calls
    private val sharedPrefs by lazy {
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // Cached selected kana set for O(1) lookup during RecyclerView binding
    private var cachedSelectedKana: Set<String>? = null

    // Helper ViewModels
    val kanaViewModel = KanaQuizViewModel(kanaRepository, preferencesRepository)
    val wordViewModel = WordQuizViewModel(wordRepository, preferencesRepository)
    val songViewModel = SongQuizViewModel(songRepository)
    val sentenceViewModel = SentenceQuizViewModel(sentenceRepository, preferencesRepository)

    val kanaType = MutableLiveData("hiragana")
    val rangeType = MutableLiveData("all")
    val selectedRows = MutableLiveData<List<String>>(emptyList())
    val quizMode = MutableLiveData("reading")
    val quizType = MutableLiveData<String>()
    var currentSongDirectory: String = "pretender"  // Track current song directory for quiz/writing
    private var quizList: List<Any> = emptyList()
    private var currentQuizList: MutableList<Any> = mutableListOf()
    val currentProblem = MutableLiveData<Any?>()
    val problemCount = MutableLiveData(0)
    val sessionCorrect = MutableLiveData(0)
    val sessionTotal = MutableLiveData(0)

    // Quiz state
    val isMultipleChoice = MutableLiveData(true)
    val multipleChoices = MutableLiveData<List<String>>(emptyList())
    val wrongAnswers = MutableLiveData<MutableList<Any>>(mutableListOf())
    val isWeakWordsQuiz = MutableLiveData(false)
    val remainingProblems = MutableLiveData(0)

    // Observer for weak words changes
    private val weakWordsObserver = androidx.lifecycle.Observer<Set<Int>> { syncedWeakWords ->
        syncedWeakWords?.let {
            weakWords.postValue(it)
        }
    }

    init {
        loadAllData()
        loadWeakWords()
        loadPenWidth()
        loadEraserWidth()

        // Observe weak words changes from preferences
        preferencesRepository.weakWordsLiveData.observeForever(weakWordsObserver)
    }

    fun getGroupedKanaList(): Map<String, List<KanaCharacter>> {
        val kanaRow = if (kanaType.value == "hiragana") kanaData?.hiragana else kanaData?.katakana
        return kanaRow?.getAllRowsAsMap() ?: LinkedHashMap()
    }

    private fun loadPenWidth() {
        penWidth.value = sharedPrefs.getFloat("pen_width", 12f)
    }

    fun savePenWidth(width: Float) {
        penWidth.value = width
        sharedPrefs.edit().putFloat("pen_width", width).apply()
    }

    private fun loadEraserWidth() {
        eraserWidth.value = sharedPrefs.getFloat("eraser_width", 40f)
    }

    fun saveEraserWidth(width: Float) {
        eraserWidth.value = width
        sharedPrefs.edit().putFloat("eraser_width", width).apply()
    }

    private fun loadWeakWords() {
        val stringSet = sharedPrefs.getStringSet("weak_words", emptySet()) ?: emptySet()
        weakWords.value = stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleWeakWord(word: Word) {
        val currentWeakWords = weakWords.value.orEmpty().toMutableSet()
        if (currentWeakWords.contains(word.id)) {
            currentWeakWords.remove(word.id)
        } else {
            currentWeakWords.add(word.id)
        }
        weakWords.value = currentWeakWords
        saveWeakWords()
    }

    fun toggleWeakWord(wordId: Int) {
        val currentWeakWords = weakWords.value.orEmpty().toMutableSet()
        if (currentWeakWords.contains(wordId)) {
            currentWeakWords.remove(wordId)
        } else {
            currentWeakWords.add(wordId)
        }
        weakWords.value = currentWeakWords
        saveWeakWords()
    }

    private fun saveWeakWords() {
        val stringSet = weakWords.value.orEmpty().map { it.toString() }.toSet()
        sharedPrefs.edit().putStringSet("weak_words", stringSet).apply()
    }

    fun isWeakWord(word: Word): Boolean {
        return weakWords.value?.contains(word.id) ?: false
    }

    fun isWeakWord(wordId: Int): Boolean {
        return weakWords.value?.contains(wordId) ?: false
    }

    fun speak(text: String) {
        // Use Application-level TTS singleton instead of local instance
        com.example.myapplication.JapaneseStudyApp.speak(text)
    }

    /**
     * Get nouns list (legacy method name for compatibility)
     */
    fun getWordList(): List<Word> {
        if (wordData == null) {
            wordData = wordRepository.getWords()  // Only load nouns
        }
        return wordData.orEmpty()
    }

    /**
     * Get words by part of speech with caching
     */
    fun getWordListByPartOfSpeech(partOfSpeech: String): List<Word> {
        return wordRepository.getWordsByPartOfSpeech(partOfSpeech)
    }

    /**
     * Helper function to get word list based on part of speech string
     * Handles null values and returns appropriate list
     */
    fun getWordListForPartOfSpeech(partOfSpeech: String?): List<Word> {
        return when (partOfSpeech) {
            "noun" -> wordRepository.getWordsByPartOfSpeech("noun")
            "verb" -> wordRepository.getWordsByPartOfSpeech("verb")
            "particle" -> wordRepository.getWordsByPartOfSpeech("particle")
            "adjective" -> wordRepository.getWordsByPartOfSpeech("adjective")
            "adverb" -> wordRepository.getWordsByPartOfSpeech("adverb")
            "conjunction" -> wordRepository.getWordsByPartOfSpeech("conjunction")
            else -> wordRepository.getAllWords() // Use all words for general quiz
        }
    }

    fun getNextWordId(currentWordId: Int): Int? {
        val wordList = getWordList()
        val currentIndex = wordList.indexOfFirst { it.id == currentWordId }
        if (currentIndex == -1 || currentIndex == wordList.size - 1) return null
        return wordList[currentIndex + 1].id
    }

    fun getPreviousWordId(currentWordId: Int): Int? {
        val wordList = getWordList()
        val currentIndex = wordList.indexOfFirst { it.id == currentWordId }
        if (currentIndex <= 0) return null
        return wordList[currentIndex - 1].id
    }

    private fun loadAllData() {
        try {
            // Kana data is already loaded by KanaRepository, get from there
            val hiragana = kanaRepository.getHiraganaData()
            val katakana = kanaRepository.getKatakanaData()

            if (hiragana != null && katakana != null) {
                kanaData = KanaData(
                    hiragana = hiragana,
                    katakana = katakana
                )
            }

            // wordData is lazily loaded when needed via getWordList()
            // No need to pre-load here
        } catch (e: Exception) {
            android.util.Log.e("QuizViewModel", "Error in loadAllData", e)
        }
    }

    fun startKanaQuiz() {
        android.util.Log.d("QuizViewModel", "========================================")
        android.util.Log.d("QuizViewModel", "startKanaQuiz called")

        quizType.value = "kana"
        quizMode.value = "reading"
        isMultipleChoice.value = false  // Kana quiz is NEVER multiple choice

        android.util.Log.d("QuizViewModel", "Kana quiz settings:")
        android.util.Log.d("QuizViewModel", "  quizType.value = ${quizType.value}")
        android.util.Log.d("QuizViewModel", "  quizMode.value = ${quizMode.value}")
        android.util.Log.d("QuizViewModel", "  isMultipleChoice.value = ${isMultipleChoice.value}")

        val hData = kanaData?.hiragana
        val kData = kanaData?.katakana
        val tempList = mutableListOf<KanaCharacter>()
        val hCharacters = if (rangeType.value == "row") getSelectedCharacters(hData, selectedRows.value) else getAllCharacters(hData)
        val kCharacters = if (rangeType.value == "row") getSelectedCharacters(kData, selectedRows.value) else getAllCharacters(kData)

        when (kanaType.value) {
            "hiragana" -> tempList.addAll(hCharacters)
            "katakana" -> tempList.addAll(kCharacters)
            "both", "mixed" -> {
                tempList.addAll(hCharacters)
                tempList.addAll(kCharacters)
            }
        }
        startQuizInternal(tempList)
    }

    fun startWordQuiz(quizMode: String = "meaning", onlyWeakWords: Boolean = false, isMultiple: Boolean = false) {
        android.util.Log.d("QuizViewModel", "========================================")
        android.util.Log.d("QuizViewModel", "startWordQuiz called: quizMode=$quizMode, onlyWeakWords=$onlyWeakWords, isMultiple=$isMultiple")

        this.quizMode.value = quizMode
        this.isMultipleChoice.value = isMultiple

        // Set word quiz view model properties
        wordViewModel.quizMode.value = quizMode
        wordViewModel.isMultipleChoice.value = isMultiple

        // Get current part of speech from quizType if available
        val currentPartOfSpeech = quizType.value

        val list = if (onlyWeakWords) {
            // CRITICAL: Use getAllWords() not wordData (which only contains nouns)
            // to get all weak words regardless of part of speech
            wordRepository.getAllWords().filter { isWeakWord(it) }
        } else {
            // Use the part of speech specific list if quizType is set
            when (currentPartOfSpeech) {
                "noun", "verb", "particle", "adjective", "adverb", "conjunction" ->
                    getWordListForPartOfSpeech(currentPartOfSpeech)
                else -> wordRepository.getAllWords() // Use all words for general quiz
            }
        }

        // CRITICAL: Must set wordViewModel's quizList for generateMultipleChoices() to work
        wordViewModel.quizList = list
        android.util.Log.d("QuizViewModel", "Set wordViewModel.quizList with ${list.size} words")

        // Keep quizType as "word" for compatibility, or use the specific partOfSpeech
        if (quizType.value !in listOf("noun", "verb", "particle", "adjective", "adverb", "conjunction")) {
            quizType.value = "word"
        }

        android.util.Log.d("QuizViewModel", "startWordQuiz: partOfSpeech=$currentPartOfSpeech, listSize=${list.size}, isMultiple=$isMultiple")
        startQuizInternal(list)
    }

    private fun startQuizInternal(list: List<Any>) {
        quizList = list
        currentQuizList = list.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        problemCount.value = 0
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    fun nextProblem() {
        if (currentQuizList.isEmpty()) {
            // For daily_word and daily_word_listening quiz, don't cycle - end after one round
            if (quizType.value == "daily_word" || quizType.value == "daily_word_listening") {
                currentProblem.value = null
                remainingProblems.value = 0
                return
            }

            if (quizList.isNotEmpty()) {
                currentQuizList = quizList.shuffled().toMutableList()
                remainingProblems.value = currentQuizList.size
            } else {
                currentProblem.value = null
                remainingProblems.value = 0
                return
            }
        }
        currentProblem.value = currentQuizList.removeFirst()
        problemCount.value = (problemCount.value ?: 0) + 1
        remainingProblems.value = currentQuizList.size
    }

    fun checkAnswer(userAnswer: String): Boolean {
        val problem = currentProblem.value ?: return false
        val correctAnswerSource = when (quizType.value) {
            "kana" -> (problem as? KanaCharacter)?.kor
            "word" -> (problem as? Word)?.meaning
            "song" -> {
                val song = problem as? Song
                when (quizMode.value) {
                    "meaning" -> song?.meaning
                    "reading" -> song?.hiragana
                    else -> song?.meaning
                }
            }
            "sentence", "weak_sentences" -> {
                val sentence = problem as? Sentence
                when (quizMode.value) {
                    "meaning" -> sentence?.meaning
                    "reading" -> sentence?.hiragana
                    else -> sentence?.meaning
                }
            }
            "weak_words",
            "verbs", "particles", "adjectives", "adverbs", "conjunctions",
            "noun", "verb", "particle", "adjective", "adverb", "conjunction" -> {
                val word = problem as? Word
                when (wordViewModel.quizMode.value ?: quizMode.value) {
                    "reverse" -> word?.kanji
                    "reading" -> word?.hiragana
                    else -> word?.meaning
                }
            }
            "daily_word", "daily_word_listening" -> (problem as? DailyWord)?.meaning
            else -> null
        }

        val isCorrect = isAnswerCorrect(userAnswer, correctAnswerSource)
        if (isCorrect) {
            sessionCorrect.value = (sessionCorrect.value ?: 0) + 1
        }
        sessionTotal.value = (sessionTotal.value ?: 0) + 1
        return isCorrect
    }

    /**
     * Check if user answer matches any of the correct answers
     * - Splits by comma to handle multiple correct answers
     * - Removes special characters (~, (, ), etc.) for comparison
     * - Case insensitive comparison
     */
    private fun isAnswerCorrect(userAnswer: String, correctAnswerSource: String?): Boolean {
        if (correctAnswerSource == null) return false

        val normalizedUserAnswer = normalizeAnswer(userAnswer)
        if (normalizedUserAnswer.isEmpty()) return false

        // Split by comma and check each possible answer
        val possibleAnswers = correctAnswerSource.split(",").map { it.trim() }

        return possibleAnswers.any { answer ->
            val normalizedAnswer = normalizeAnswer(answer)
            // Exact match after normalization
            normalizedUserAnswer.equals(normalizedAnswer, ignoreCase = true) ||
            // User answer contains the normalized answer (for partial match)
            normalizedUserAnswer.contains(normalizedAnswer, ignoreCase = true) ||
            // Normalized answer contains user answer (for partial match)
            normalizedAnswer.contains(normalizedUserAnswer, ignoreCase = true)
        }
    }

    /**
     * Normalize answer by removing special characters and extra whitespace
     */
    private fun normalizeAnswer(answer: String): String {
        return answer
            .replace("~", "")
            .replace("～", "")
            .replace("(", "")
            .replace(")", "")
            .replace("（", "")
            .replace("）", "")
            .replace("・", "")
            .replace("、", "")
            .trim()
    }

    private fun getSelectedCharacters(data: KanaRow?, selected: List<String>?): List<KanaCharacter> {
        if (data == null || selected.isNullOrEmpty()) return emptyList()
        return data.getCharactersByRowNames(selected)
    }

    private fun getAllCharacters(data: KanaRow?): List<KanaCharacter> {
        return data?.getAllCharacters() ?: emptyList()
    }

    // Song and Sentence methods
    fun getSongList(): List<SongInfo> = songRepository.getAvailableSongs()

    fun getSongVocabulary(songDirectory: String): List<Song> = songRepository.getSongVocabulary(songDirectory)

    fun getPretenderVocabulary(): List<Song> = songRepository.getPretenderVocabulary()

    fun clearSongCache(songDirectory: String) = songRepository.clearCache(songDirectory)

    fun getSentenceList(): List<Sentence> = sentenceRepository.getSentences()

    fun getSentencesByBatchId(batchId: String): List<Sentence> =
        sentenceRepository.getSentencesByBatchId(batchId)

    @Deprecated("Use getSentencesByBatchId instead")
    fun getSentencesByIdRange(startId: Int, endId: Int): List<Sentence> =
        sentenceRepository.getSentences().filter { it.id in startId..endId }

    fun getAllWeakWords(): List<Any> {
        val weakWordIds = weakWords.value.orEmpty()
        val result = mutableListOf<Any>()

        // Get weak words (ID < 10000) - includes all part of speech (nouns, verbs, particles, adjectives, adverbs, conjunctions)
        val allWords = wordRepository.getAllWords().filter { weakWordIds.contains(it.id) }
        result.addAll(allWords)

        // Get weak songs (ID 10000-19999)
        val songs = songRepository.getPretenderVocabulary().filter {
            weakWordIds.contains(10000 + it.id)
        }
        result.addAll(songs)

        // Get weak sentences (ID >= 20000)
        val sentences = sentenceRepository.getSentences().filter {
            weakWordIds.contains(it.id)
        }
        result.addAll(sentences)

        return result
    }

    /**
     * Get only weak words (excluding songs and sentences)
     * For use in word-only quizzes and writing tests
     */
    fun getWeakWordList(): List<Word> {
        val weakWordIds = weakWords.value.orEmpty()
        // Get weak words (ID < 10000) - includes all part of speech
        return wordRepository.getAllWords().filter { weakWordIds.contains(it.id) }
    }

    fun getWeakSentenceList(): List<Sentence> {
        val weakWordIds = weakWords.value.orEmpty()
        return sentenceRepository.getSentences().filter { weakWordIds.contains(it.id) }
    }

    fun isWeakSentence(sentence: Sentence): Boolean {
        return weakWords.value?.contains(sentence.id) ?: false
    }

    fun toggleWeakSentence(sentence: Sentence) {
        val currentWeakWords = weakWords.value.orEmpty().toMutableSet()
        if (currentWeakWords.contains(sentence.id)) {
            currentWeakWords.remove(sentence.id)
        } else {
            currentWeakWords.add(sentence.id)
        }
        weakWords.value = currentWeakWords
        saveWeakWords()
    }

    fun clearAllWeakWords() {
        val currentWeakWords = weakWords.value.orEmpty()
        // Keep only sentence IDs (>= 20000)
        val sentencesOnly = currentWeakWords.filter { it >= 20000 }.toSet()
        weakWords.value = sentencesOnly
        saveWeakWords()
    }

    fun clearAllWeakSentences() {
        val currentWeakWords = weakWords.value.orEmpty()
        // Keep only word and song IDs (< 20000)
        val wordsOnly = currentWeakWords.filter { it < 20000 }.toSet()
        weakWords.value = wordsOnly
        saveWeakWords()
    }

    /**
     * 삭제된 문장 ID들을 weak words에서 제거
     */
    fun removeDeletedSentenceIds(deletedIds: List<Int>) {
        if (deletedIds.isEmpty()) return

        val currentWeakWords = weakWords.value.orEmpty().toMutableSet()
        val deletedIdSet = deletedIds.toSet()
        val removed = currentWeakWords.removeAll(deletedIdSet)

        if (removed) {
            weakWords.value = currentWeakWords
            saveWeakWords()
            android.util.Log.d("QuizViewModel", "삭제된 문장 ID ${deletedIds.size}개가 weak words에서 제거됨")
        }
    }

    /**
     * 유효하지 않은 문장 ID들을 weak words에서 정리
     */
    fun cleanupInvalidWeakSentences() {
        val currentWeakWords = weakWords.value.orEmpty()
        val validIds = sentenceRepository.filterValidSentenceIds(currentWeakWords)

        if (validIds.size != currentWeakWords.size) {
            val removedCount = currentWeakWords.size - validIds.size
            weakWords.value = validIds
            saveWeakWords()
            android.util.Log.d("QuizViewModel", "유효하지 않은 weak sentence ID ${removedCount}개 정리됨")
        }
    }

    fun toggleMeaningVisibility(): Boolean {
        showMeaning.value = !(showMeaning.value ?: true)
        return showMeaning.value ?: true
    }

    // Kana methods
    fun getAllKanaList(): List<KanaCharacter> {
        val kanaRow = if (kanaType.value == "hiragana") kanaData?.hiragana else kanaData?.katakana
        return getAllCharacters(kanaRow)
    }

    fun getSelectedKanaList(): List<KanaCharacter> {
        val kanaRow = if (kanaType.value == "hiragana") kanaData?.hiragana else kanaData?.katakana
        return getSelectedCharacters(kanaRow, selectedRows.value)
    }

    /**
     * Check if kana is selected - uses cached set for O(1) lookup during RecyclerView binding
     */
    fun isKanaSelected(kana: KanaCharacter): Boolean {
        // Use cached set, or load it if null
        if (cachedSelectedKana == null) {
            cachedSelectedKana = sharedPrefs.getStringSet("selected_kana", emptySet()) ?: emptySet()
        }
        return cachedSelectedKana!!.contains("${kana.kana}_${kana.romaji}")
    }

    fun toggleKanaSelection(kana: KanaCharacter) {
        val selectedKana = (cachedSelectedKana ?: sharedPrefs.getStringSet("selected_kana", emptySet()) ?: emptySet()).toMutableSet()
        val key = "${kana.kana}_${kana.romaji}"

        if (selectedKana.contains(key)) {
            selectedKana.remove(key)
        } else {
            selectedKana.add(key)
        }

        // Update cache and save to SharedPreferences
        cachedSelectedKana = selectedKana
        sharedPrefs.edit().putStringSet("selected_kana", selectedKana).apply()
    }

    fun clearSelectedKana() {
        cachedSelectedKana = emptySet()
        sharedPrefs.edit().remove("selected_kana").apply()
    }

    // Quiz type methods
    fun setQuizType(type: String) {
        quizType.value = type
    }

    // Start quiz methods
    fun startSongQuiz(mode: String, isMultiple: Boolean = true) {
        quizType.value = "song"
        quizMode.value = mode
        isMultipleChoice.value = isMultiple
        songViewModel.quizMode.value = mode
        songViewModel.isMultipleChoice.value = isMultiple
        val songList = songViewModel.generateSongQuizList(currentSongDirectory)
        quizList = songList
        currentQuizList = songList.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    fun startSentenceQuiz(mode: String, isMultiple: Boolean = true, useWeakSentences: Boolean = false) {
        quizType.value = if (useWeakSentences) "weak_sentences" else "sentence"
        quizMode.value = mode
        isMultipleChoice.value = isMultiple
        sentenceViewModel.quizMode.value = mode
        sentenceViewModel.isMultipleChoice.value = isMultiple

        val sentenceList = if (useWeakSentences) {
            getWeakSentenceList()
        } else {
            sentenceViewModel.generateSentenceQuizList()
        }

        quizList = sentenceList
        currentQuizList = sentenceList.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    fun startWordQuizByType(type: String, mode: String = "meaning", isMultiple: Boolean = false) {
        android.util.Log.d("QuizViewModel", "========================================")
        android.util.Log.d("QuizViewModel", "startWordQuizByType called: type=$type, mode=$mode, isMultiple=$isMultiple")

        quizType.value = type
        quizMode.value = mode
        isMultipleChoice.value = isMultiple
        wordViewModel.quizMode.value = mode
        wordViewModel.isMultipleChoice.value = isMultiple
        setQuizType(type)

        android.util.Log.d("QuizViewModel", "After setting values:")
        android.util.Log.d("QuizViewModel", "  quizType.value = ${quizType.value}")
        android.util.Log.d("QuizViewModel", "  quizMode.value = ${quizMode.value}")
        android.util.Log.d("QuizViewModel", "  isMultipleChoice.value = ${isMultipleChoice.value}")
        android.util.Log.d("QuizViewModel", "  wordViewModel.quizMode.value = ${wordViewModel.quizMode.value}")
        android.util.Log.d("QuizViewModel", "  wordViewModel.isMultipleChoice.value = ${wordViewModel.isMultipleChoice.value}")

        // Map quiz type to part of speech
        val partOfSpeech = when (type) {
            "verb", "verbs" -> "verb"
            "particle", "particles" -> "particle"
            "adjective", "adjectives" -> "adjective"
            "adverb", "adverbs" -> "adverb"
            "conjunction", "conjunctions" -> "conjunction"
            "noun", "nouns" -> "noun"
            else -> type  // Use type as-is if not in the above list
        }

        android.util.Log.d("QuizViewModel", "Loading words for partOfSpeech: $partOfSpeech (from type: $type)")
        val wordList = wordViewModel.generateWordQuizList(partOfSpeech)
        android.util.Log.d("QuizViewModel", "Loaded ${wordList.size} words")
        if (wordList.isNotEmpty()) {
            android.util.Log.d("QuizViewModel", "First word: ${wordList.first().kanji} - ${wordList.first().meaning} (partOfSpeech: ${wordList.first().partOfSpeech})")
        }

        quizList = wordList
        currentQuizList = wordList.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    // Generate multiple choices for word-type quizzes (verbs, particles, etc.)
    fun generateMultipleChoicesForWord(problem: Word) {
        wordViewModel.generateMultipleChoices(problem)
    }

    // Daily word quiz - meaning quiz only (word -> meaning)
    fun startDailyWordQuiz(checkedDays: Set<Int>) {
        if (checkedDays.isEmpty()) return

        quizType.value = "daily_word"
        quizMode.value = "meaning"
        isMultipleChoice.value = false

        // Collect words from all checked days
        val allWords = mutableListOf<DailyWord>()
        for (day in checkedDays.sorted()) {
            allWords.addAll(dailyWordRepository.getWordsForDay(day))
        }

        if (allWords.isEmpty()) return

        quizList = allWords
        currentQuizList = allWords.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    // Weak daily word quiz - quiz from checked weak words
    fun startWeakDailyWordQuiz(weakWordIds: Set<Int>) {
        if (weakWordIds.isEmpty()) return

        quizType.value = "daily_word"
        quizMode.value = "meaning"
        isMultipleChoice.value = false

        // Get all words and filter by weak word IDs
        val allWords = dailyWordRepository.getAllWords()
        val weakWords = allWords.filter { it.id in weakWordIds }

        if (weakWords.isEmpty()) return

        quizList = weakWords
        currentQuizList = weakWords.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    // Random quiz - (일차 수 × 5) 문제를 완전 랜덤으로 출제
    fun startRandomQuiz(words: List<DailyWord>) {
        if (words.isEmpty()) return

        quizType.value = "daily_word"
        quizMode.value = "meaning"
        isMultipleChoice.value = false

        quizList = words
        currentQuizList = words.toMutableList()  // 이미 섞여서 온 상태
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    // Daily word listening quiz - hear word, type meaning
    fun startDailyWordListeningQuiz(checkedDays: Set<Int>) {
        if (checkedDays.isEmpty()) return

        quizType.value = "daily_word_listening"
        quizMode.value = "listening"
        isMultipleChoice.value = false

        // Collect words from all checked days
        val allWords = mutableListOf<DailyWord>()
        for (day in checkedDays.sorted()) {
            allWords.addAll(dailyWordRepository.getWordsForDay(day))
        }

        if (allWords.isEmpty()) return

        quizList = allWords
        currentQuizList = allWords.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    // Weak daily word listening quiz
    fun startWeakDailyWordListeningQuiz(weakWordIds: Set<Int>) {
        if (weakWordIds.isEmpty()) return

        quizType.value = "daily_word_listening"
        quizMode.value = "listening"
        isMultipleChoice.value = false

        // Get all words and filter by weak word IDs
        val allWords = dailyWordRepository.getAllWords()
        val weakWords = allWords.filter { it.id in weakWordIds }

        if (weakWords.isEmpty()) return

        quizList = weakWords
        currentQuizList = weakWords.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        wrongAnswers.value = mutableListOf()
        remainingProblems.value = currentQuizList.size
        nextProblem()
    }

    // Generate multiple choices for daily word quiz
    fun generateMultipleChoicesForDailyWord(problem: DailyWord) {
        val allDailyWords = dailyWordRepository.getAllWords()
        val correctAnswer = problem.meaning
        val wrongAnswers = allDailyWords
            .filter { it.id != problem.id }
            .shuffled()
            .take(3)
            .map { it.meaning }

        val choices = (listOf(correctAnswer) + wrongAnswers).shuffled()
        multipleChoices.value = choices
    }

    override fun onCleared() {
        super.onCleared()
        // Remove observer to prevent memory leak
        preferencesRepository.weakWordsLiveData.removeObserver(weakWordsObserver)
        // TTS is now managed by Application singleton, no cleanup needed here
    }
}