package com.example.myapplication.service

import com.google.gson.annotations.SerializedName

data class OpenAIRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>,
    val temperature: Double = 0.7,
    @SerializedName("max_completion_tokens")
    val maxCompletionTokens: Int = 2000
)

data class Message(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)
