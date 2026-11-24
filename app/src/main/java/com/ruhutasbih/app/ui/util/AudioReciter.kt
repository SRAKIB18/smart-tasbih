package com.ruhutasbih.app.ui.util

import android.content.Context
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
            val arabicLocale = Locale("ar", "SA")
            val result = tts?.setLanguage(arabicLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale("ar")
            }
            tts?.setSpeechRate(0.85f)
            tts?.setPitch(1.0f)

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

    fun speak(text: String, onStart: () -> Unit = {}, onDone: () -> Unit = {}) {
        if (text.isBlank()) return
        onDoneCallback = onDone

        try {
            if (isInitialized && tts != null) {
                onStart()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DHIKR_UTTERANCE_${System.currentTimeMillis()}")
            } else {
                tts = TextToSpeech(appContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.language = Locale("ar")
                        tts?.setSpeechRate(0.85f)
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