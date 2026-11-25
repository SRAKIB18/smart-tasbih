package com.ruhutasbih.app.ui.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager(context: Context) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playClickSound(soundType: String = "BEEP") {
        try {
            val tone = when (soundType) {
                "WOOD_CLICK" -> ToneGenerator.TONE_PROP_ACK
                "METALLIC_TINK" -> ToneGenerator.TONE_PROP_PROMPT
                "SOFT_TICK" -> ToneGenerator.TONE_DTMF_1
                else -> ToneGenerator.TONE_PROP_BEEP // "BEEP"
            }
            toneGenerator?.startTone(tone, 30)
        } catch (e: Exception) {
            // Safe ignore if audio hardware unavailable
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // Safe cleanup
        }
    }
}