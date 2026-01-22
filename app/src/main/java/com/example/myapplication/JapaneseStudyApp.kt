package com.example.myapplication

import android.app.Application
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

/**
 * Application class for Japanese Study App with Hilt dependency injection
 */
@HiltAndroidApp
class JapaneseStudyApp : Application(), TextToSpeech.OnInitListener {

    companion object {
        private var tts: TextToSpeech? = null
        private var isTtsInitialized = false
        private var isTtsWarmedUp = false

        fun speak(text: String) {
            if (isTtsInitialized) {
                // Add small silence before text to prevent clipping on first play
                val params = Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                }

                if (!isTtsWarmedUp) {
                    // First time: play silent warmup then actual text
                    tts?.playSilentUtterance(50, TextToSpeech.QUEUE_ADD, "warmup")
                    tts?.speak(text, TextToSpeech.QUEUE_ADD, params, "speech_${System.currentTimeMillis()}")
                    isTtsWarmedUp = true
                } else {
                    // Subsequent plays: just speak normally
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "speech_${System.currentTimeMillis()}")
                }
            }
        }

        fun isTtsReady(): Boolean = isTtsInitialized

        /**
         * Reset warmup state (call when TTS needs re-warmup, e.g., after long pause)
         */
        fun resetWarmup() {
            isTtsWarmedUp = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize TTS when app starts
        tts = TextToSpeech(applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.JAPANESE)
            isTtsInitialized = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)

            if (isTtsInitialized) {
                // Set speech rate slightly slower for clearer pronunciation
                tts?.setSpeechRate(0.9f)

                // Pre-warm TTS engine with silent utterance to prevent first-play clipping
                tts?.playSilentUtterance(1, TextToSpeech.QUEUE_ADD, "init_warmup")

                // Set up listener to track when warmup is done
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "init_warmup") {
                            isTtsWarmedUp = true
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {}
                    override fun onError(utteranceId: String?, errorCode: Int) {}
                })
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        tts?.stop()
        tts?.shutdown()
    }
}
