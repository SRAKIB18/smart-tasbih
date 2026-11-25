package com.ruhutasbih.app.ui.util

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class AudioReciter(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onDoneCallback: (() -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        try {
            tts = TextToSpeech(appContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            applyMaleArabicVoice()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        onDoneCallback?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        onDoneCallback?.invoke()
                    }
                }
            })

            isInitialized = true
        }
    }

    private fun applyMaleArabicVoice() {
        try {
            val arabicLocale = Locale("ar", "SA")
            val result = tts?.setLanguage(arabicLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale("ar")
            }

            // Prefer Male Voice in system TTS engine
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val voices = tts?.voices
                if (!voices.isNullOrEmpty()) {
                    val maleVoice = voices.firstOrNull { voice ->
                        voice.locale.language == "ar" && (
                            voice.name.lowercase().contains("male") ||
                            voice.name.lowercase().contains("ar-x-m") ||
                            voice.name.lowercase().contains("ar-ww-x-mma") ||
                            voice.name.lowercase().contains("ar-xa-x-ard")
                        )
                    } ?: voices.firstOrNull { voice ->
                        voice.locale.language == "ar" && !voice.name.lowercase().contains("female")
                    }
                    if (maleVoice != null) {
                        tts?.voice = maleVoice
                    }
                }
            }

            // Lower pitch (0.80f) guarantees a deep, clear male Qari recitation tone
            tts?.setPitch(0.80f)
            tts?.setSpeechRate(0.85f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speak(text: String, onStart: () -> Unit = {}, onDone: () -> Unit = {}) {
        if (text.isBlank()) return
        onDoneCallback = onDone

        try {
            if (isInitialized && tts != null) {
                applyMaleArabicVoice()
                onStart()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DHIKR_UTTERANCE_${System.currentTimeMillis()}")
            } else {
                tts = TextToSpeech(appContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        applyMaleArabicVoice()
                        onStart()
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DHIKR_UTTERANCE")
                    } else {
                        onDone()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onDone()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isSpeaking(): Boolean {
        return try {
            tts?.isSpeaking == true
        } catch (e: Exception) {
            false
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}