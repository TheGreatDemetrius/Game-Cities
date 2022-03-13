package ru.cities.game.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import ru.cities.game.util.EMPTY_STRING
import java.util.Locale

class Speech(context: Context, private val text: String = EMPTY_STRING) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)

    override fun onInit(p0: Int) {
        try {
            tts.language = Locale("ru", "RU")
            speakOut(text)
        } catch (e: Exception) {
            System.err.println("TTS has not been detected.")
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun destroy() {
        tts.shutdown()
    }
}
