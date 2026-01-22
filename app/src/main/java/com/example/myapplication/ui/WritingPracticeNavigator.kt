package com.example.myapplication.ui

import com.example.myapplication.model.Word
import com.example.myapplication.model.Song
import com.example.myapplication.model.Sentence

/**
 * WritingPracticeFragment의 네비게이션 로직을 분리한 헬퍼 클래스
 * 다양한 모드(Word, Song, Sentence, Mixed)에서의 이전/다음 아이템 탐색 담당
 */
class WritingPracticeNavigator {

    private var currentWordList: List<Word> = emptyList()
    private var currentSongList: List<Song> = emptyList()
    private var currentSentenceList: List<Sentence> = emptyList()
    private var currentMixedList: List<Any> = emptyList()

    private var isSongMode: Boolean = false
    private var isSentenceMode: Boolean = false
    private var isMixedMode: Boolean = false

    /**
     * Word 모드로 설정
     */
    fun setWordMode(wordList: List<Word>) {
        isSongMode = false
        isSentenceMode = false
        isMixedMode = false
        currentWordList = wordList
    }

    /**
     * Song 모드로 설정
     */
    fun setSongMode(songList: List<Song>) {
        isSongMode = true
        isSentenceMode = false
        isMixedMode = false
        currentSongList = songList
    }

    /**
     * Sentence 모드로 설정
     */
    fun setSentenceMode(sentenceList: List<Sentence>) {
        isSongMode = false
        isSentenceMode = true
        isMixedMode = false
        currentSentenceList = sentenceList
    }

    /**
     * Mixed 모드로 설정 (Word와 Song 혼합)
     */
    fun setMixedMode(mixedList: List<Any>) {
        isSongMode = false
        isSentenceMode = false
        isMixedMode = true
        currentMixedList = mixedList
    }

    /**
     * 다음 아이템 ID 반환 (순환 네비게이션)
     */
    fun getNextId(currentId: Int): Int? {
        return when {
            isMixedMode -> getNextMixedId(currentId)
            isSongMode -> getNextSongId(currentId)
            isSentenceMode -> getNextSentenceId(currentId)
            else -> getNextWordId(currentId)
        }
    }

    /**
     * 이전 아이템 ID 반환 (순환 네비게이션)
     */
    fun getPreviousId(currentId: Int): Int? {
        return when {
            isMixedMode -> getPreviousMixedId(currentId)
            isSongMode -> getPreviousSongId(currentId)
            isSentenceMode -> getPreviousSentenceId(currentId)
            else -> getPreviousWordId(currentId)
        }
    }

    /**
     * 현재 ID로 아이템 데이터 가져오기
     * @return Triple(kanji, meaning, hiragana) 또는 null
     */
    fun getItemData(id: Int): Triple<String, String, String>? {
        return when {
            isMixedMode -> getMixedItemData(id)
            isSongMode -> getSongItemData(id)
            isSentenceMode -> getSentenceItemData(id)
            else -> getWordItemData(id)
        }
    }

    // Mixed 모드 네비게이션
    private fun getNextMixedId(id: Int): Int? {
        if (currentMixedList.isEmpty()) return null
        val currentIndex = findMixedIndex(id) ?: return null
        val nextIndex = if (currentIndex == currentMixedList.size - 1) 0 else currentIndex + 1
        return getMixedItemId(currentMixedList[nextIndex])
    }

    private fun getPreviousMixedId(id: Int): Int? {
        if (currentMixedList.isEmpty()) return null
        val currentIndex = findMixedIndex(id) ?: return null
        val prevIndex = if (currentIndex == 0) currentMixedList.size - 1 else currentIndex - 1
        return getMixedItemId(currentMixedList[prevIndex])
    }

    private fun findMixedIndex(id: Int): Int? {
        val index = currentMixedList.indexOfFirst { item ->
            when (item) {
                is Word -> item.id == id
                is Song -> (10000 + item.id) == id
                else -> false
            }
        }
        return if (index == -1) null else index
    }

    private fun getMixedItemId(item: Any): Int? {
        return when (item) {
            is Word -> item.id
            is Song -> 10000 + item.id
            else -> null
        }
    }

    private fun getMixedItemData(id: Int): Triple<String, String, String>? {
        val item = currentMixedList.find {
            when (it) {
                is Word -> it.id == id
                is Song -> (10000 + it.id) == id
                else -> false
            }
        }
        return when (item) {
            is Word -> Triple(item.kanji, item.meaning, item.hiragana)
            is Song -> Triple(item.kanji, item.meaning, item.hiragana)
            else -> null
        }
    }

    // Song 모드 네비게이션
    private fun getNextSongId(id: Int): Int? {
        if (currentSongList.isEmpty()) return null
        val songId = id - 10000
        val currentIndex = currentSongList.indexOfFirst { it.id == songId }
        if (currentIndex == -1) return null
        val nextIndex = if (currentIndex == currentSongList.size - 1) 0 else currentIndex + 1
        return 10000 + currentSongList[nextIndex].id
    }

    private fun getPreviousSongId(id: Int): Int? {
        if (currentSongList.isEmpty()) return null
        val songId = id - 10000
        val currentIndex = currentSongList.indexOfFirst { it.id == songId }
        if (currentIndex == -1) return null
        val prevIndex = if (currentIndex == 0) currentSongList.size - 1 else currentIndex - 1
        return 10000 + currentSongList[prevIndex].id
    }

    private fun getSongItemData(id: Int): Triple<String, String, String>? {
        val songId = id - 10000
        val song = currentSongList.find { it.id == songId } ?: return null
        return Triple(song.kanji, song.meaning, song.hiragana)
    }

    // Sentence 모드 네비게이션
    private fun getNextSentenceId(id: Int): Int? {
        if (currentSentenceList.isEmpty()) return null
        val currentIndex = currentSentenceList.indexOfFirst { it.id == id }
        if (currentIndex == -1) return null
        val nextIndex = if (currentIndex == currentSentenceList.size - 1) 0 else currentIndex + 1
        return currentSentenceList[nextIndex].id
    }

    private fun getPreviousSentenceId(id: Int): Int? {
        if (currentSentenceList.isEmpty()) return null
        val currentIndex = currentSentenceList.indexOfFirst { it.id == id }
        if (currentIndex == -1) return null
        val prevIndex = if (currentIndex == 0) currentSentenceList.size - 1 else currentIndex - 1
        return currentSentenceList[prevIndex].id
    }

    private fun getSentenceItemData(id: Int): Triple<String, String, String>? {
        val sentence = currentSentenceList.find { it.id == id } ?: return null
        return Triple(sentence.kanji, sentence.meaning, sentence.hiragana)
    }

    // Word 모드 네비게이션
    private fun getNextWordId(id: Int): Int? {
        if (currentWordList.isEmpty()) return null
        val currentIndex = currentWordList.indexOfFirst { it.id == id }
        if (currentIndex == -1) return null
        val nextIndex = if (currentIndex == currentWordList.size - 1) 0 else currentIndex + 1
        return currentWordList[nextIndex].id
    }

    private fun getPreviousWordId(id: Int): Int? {
        if (currentWordList.isEmpty()) return null
        val currentIndex = currentWordList.indexOfFirst { it.id == id }
        if (currentIndex == -1) return null
        val prevIndex = if (currentIndex == 0) currentWordList.size - 1 else currentIndex - 1
        return currentWordList[prevIndex].id
    }

    private fun getWordItemData(id: Int): Triple<String, String, String>? {
        val word = currentWordList.find { it.id == id } ?: return null
        return Triple(word.kanji, word.meaning, word.hiragana)
    }
}
