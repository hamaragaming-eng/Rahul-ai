package com.example.rahulai.system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onErrorOccurred: (String) -> Unit = {},
    private val onRmsChanged: (Float) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRecognitionAvailable = MutableStateFlow(true)
    val speechRecognitionAvailable: StateFlow<Boolean> = _speechRecognitionAvailable.asStateFlow()

    var isVoiceOutputEnabled = true

    init {
        initTts()
        mainHandler.post {
            initSpeechRecognizer()
        }
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {
            isTtsInitialized = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.let { engine ->
                val hindiLocale = Locale.forLanguageTag("hi-IN")
                val result = engine.setLanguage(hindiLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
                engine.setPitch(1.0f)
                engine.setSpeechRate(0.95f)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                _speechRecognitionAvailable.value = true
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            onRmsChanged(rmsdB)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            val message = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio error. Please check mic."
                                SpeechRecognizer.ERROR_CLIENT -> "Voice recognition service not available on this emulator. Use quick commands or text."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed."
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue for voice recognition."
                                SpeechRecognizer.ERROR_NO_MATCH -> "Awaaz sunai nahi di. Kripya dobara bolein ya text type karein."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic again."
                                else -> "Voice recognizer status ($error)."
                            }
                            onErrorOccurred(message)
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()
                            if (!text.isNullOrBlank()) {
                                onSpeechRecognized(text)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } else {
                _speechRecognitionAvailable.value = false
            }
        } catch (e: Exception) {
            _speechRecognitionAvailable.value = false
        }
    }

    fun startListening() {
        mainHandler.post {
            stopSpeaking()
            if (!_speechRecognitionAvailable.value || speechRecognizer == null) {
                initSpeechRecognizer()
            }
            speechRecognizer?.let { recognizer ->
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                try {
                    recognizer.startListening(intent)
                    _isListening.value = true
                } catch (e: Exception) {
                    _isListening.value = false
                    onErrorOccurred("Voice service unavailable on this device. Type your command below.")
                }
            } ?: run {
                onErrorOccurred("Microphone service is not supported in this browser emulator. Please use text or quick command buttons.")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _isListening.value = false
        }
    }

    fun speak(text: String) {
        if (!isVoiceOutputEnabled || !isTtsInitialized) return
        stopListening()
        val cleanText = text
            .replace(Regex("[*#_`\\[\\]{}()]"), "")
            .trim()
        if (cleanText.isBlank()) return

        try {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "RAHUL_AI_${System.currentTimeMillis()}")
        } catch (_: Exception) {}
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
    }
}
