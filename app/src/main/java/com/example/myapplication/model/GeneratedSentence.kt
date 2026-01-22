package com.example.myapplication.model

data class GeneratedSentence(
    val japanese: String,
    val reading: String,
    val korean: String,
    val usedWordIds: List<Int>
)

data class SentenceGenerationResult(
    val sentences: List<GeneratedSentence>,
    val totalWords: Int,
    val usedWords: Int,
    val unusedWords: Int,
    val groupCount: Int,
    val unusedWordDetails: List<Pair<Int, String>>, // (ID, 단어)
    val unknownWords: List<UnknownWord> = emptyList() // AI가 추가한 모르는 단어들
)
