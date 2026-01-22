package com.example.myapplication.model

/**
 * 문장 생성 시 AI가 추가한 모르는 단어
 */
data class UnknownWord(
    val word: String,       // 일본어 단어 (한자 포함)
    val reading: String,    // 히라가나 읽기
    val meaning: String     // 한국어 뜻
)
