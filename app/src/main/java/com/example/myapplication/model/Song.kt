package com.example.myapplication.model

data class Song(
    val id: Int,
    val kanji: String,
    val meaning: String,
    val hiragana: String,
    val songTitle: String = "pretender",
    val time: String? = null,  // 시작 타임스탬프 (예: "00:14.4")
    val endTime: String? = null  // 끝 타임스탬프 (예: "00:16.9")
)
