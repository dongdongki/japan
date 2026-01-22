package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class DailyWord(
    val id: Int,
    val word: String,
    val reading: String,
    val meaning: String,
    @SerializedName("example_jp")
    val exampleJp: String,
    @SerializedName("example_kr")
    val exampleKr: String
)
