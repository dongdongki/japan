package com.example.myapplication.model

data class Word(
    val id: Int,
    val kanji: String,
    val meaning: String,
    val hiragana: String,
    val partOfSpeech: String = "noun" // noun, adjective, verb, particle
)
