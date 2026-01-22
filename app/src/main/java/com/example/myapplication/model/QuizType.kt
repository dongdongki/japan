package com.example.myapplication.model

/**
 * Type-safe enum for quiz types.
 * Replaces string-based quiz type comparisons for compile-time safety.
 */
enum class QuizType(val value: String) {
    KANA("kana"),
    WORD("word"),
    VERB("verb"),
    PARTICLE("particle"),
    ADJECTIVE("adjective"),
    ADVERB("adverb"),
    CONJUNCTION("conjunction"),
    NOUN("noun"),
    SONG("song"),
    SENTENCE("sentence"),
    WEAK_WORDS("weak_words"),
    WEAK_SENTENCES("weak_sentences"),
    DAILY_WORD("daily_word"),
    DAILY_WORD_LISTENING("daily_word_listening");

    companion object {
        /**
         * Convert string value to QuizType enum.
         * Returns null if no matching type is found.
         */
        fun fromString(value: String?): QuizType? = entries.find { it.value == value }

        /**
         * Check if the given type is a word-based quiz type.
         */
        fun isWordType(type: QuizType?): Boolean = type in WORD_TYPES

        /**
         * Set of quiz types that are word-based (for UI handling).
         */
        val WORD_TYPES = setOf(
            WORD, VERB, PARTICLE, ADJECTIVE, ADVERB, CONJUNCTION, NOUN, WEAK_WORDS
        )
    }
}
