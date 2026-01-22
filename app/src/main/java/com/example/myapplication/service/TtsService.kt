package com.example.myapplication.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Service for managing Text-to-Speech functionality
 * Uses reference counting to safely manage TTS lifecycle when used by multiple ViewModels
 */
class TtsService(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var referenceCount = 0

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.JAPANESE)
            isTtsInitialized = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
        }
    }

    /**
     * Acquire a reference to this TTS service
     * Call this when a ViewModel starts using TTS
     */
    @Synchronized
    fun acquire() {
        referenceCount++
        android.util.Log.d("TtsService", "acquire() - reference count: $referenceCount")
    }

    /**
     * Release a reference to this TTS service
     * TTS will only be shut down when all references are released
     */
    @Synchronized
    fun release() {
        if (referenceCount > 0) {
            referenceCount--
            android.util.Log.d("TtsService", "release() - reference count: $referenceCount")

            if (referenceCount == 0) {
                shutdown()
            }
        }
    }

    /**
     * Speak the given text in Japanese
     */
    fun speak(text: String) {
        if (isTtsInitialized) {
            // Add small pause before actual text to prevent first sound cutoff
            tts?.speak("...$text", TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    /**
     * Check if TTS is initialized and ready to use
     */
    fun isInitialized(): Boolean = isTtsInitialized

    /**
     * Internal shutdown method - only called when reference count reaches 0
     */
    private fun shutdown() {
        android.util.Log.d("TtsService", "shutdown() - cleaning up TTS resources")
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsInitialized = false
    }
}
