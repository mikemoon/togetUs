package sky.kr.co.newtogetusa.ui.main.search.player

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogVoiceSearchBinding
import java.util.Locale

class SearchVoiceDialogFragment : DialogFragment() {

    private var _binding: DialogVoiceSearchBinding? = null
    private val binding get() = _binding!!

    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lastRecognizedText = ""
    private var onRecognized: ((String) -> Unit)? = null
    private var isRecognizerReleased = false

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!canUpdateUi()) return@registerForActivityResult
            if (granted) {
                startListening()
            } else {
                showFailedState()
            }
        }

    private val noInputTimeoutRunnable = Runnable {
        if (!canUpdateUi()) return@Runnable
        stopListening()
        showFailedState()
    }

    private val silenceTimeoutRunnable = Runnable {
        if (!canUpdateUi()) return@Runnable
        if (lastRecognizedText.isNotBlank()) {
            completeRecognition(lastRecognizedText)
        } else {
            stopListening()
            showFailedState()
        }
    }

    override fun getTheme(): Int = R.style.RoundedCornersDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isRecognizerReleased = false
        _binding = DialogVoiceSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            releaseSpeechRecognizer()
            dismissAllowingStateLoss()
        }
        binding.btnRetry.setOnClickListener {
            lastRecognizedText = ""
            startWithPermission()
        }
        startWithPermission()
    }

    override fun onDestroyView() {
        releaseSpeechRecognizer()
        _binding = null
        super.onDestroyView()
    }

    fun onRecognized(callback: (String) -> Unit): SearchVoiceDialogFragment {
        onRecognized = callback
        return this
    }

    private fun startWithPermission() {
        val context = context ?: return
        if (!canUpdateUi()) return

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            showFailedState()
            return
        }

        val permissionState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        val context = context ?: return
        if (!canUpdateUi()) return

        releaseSpeechRecognizer()
        showListeningState()
        isRecognizerReleased = false

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() {
                    if (!canUpdateUi()) return
                    handler.removeCallbacks(noInputTimeoutRunnable)
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    if (!canUpdateUi()) return
                    if (lastRecognizedText.isNotBlank()) {
                        completeRecognition(lastRecognizedText)
                    } else {
                        showFailedState()
                    }
                }

                override fun onResults(results: Bundle?) {
                    if (!canUpdateUi()) return
                    val text = results.firstSpeechText()
                    if (text.isNotBlank()) {
                        completeRecognition(text)
                    } else {
                        showFailedState()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    if (!canUpdateUi()) return
                    val text = partialResults.firstSpeechText()
                    if (text.isNotBlank() && text != lastRecognizedText) {
                        lastRecognizedText = text
                        handler.removeCallbacks(noInputTimeoutRunnable)
                        handler.removeCallbacks(silenceTimeoutRunnable)
                        handler.postDelayed(silenceTimeoutRunnable, SILENCE_TIMEOUT_MS)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREA.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
        handler.postDelayed(noInputTimeoutRunnable, NO_INPUT_TIMEOUT_MS)
    }

    private fun stopListening() {
        handler.removeCallbacks(noInputTimeoutRunnable)
        handler.removeCallbacks(silenceTimeoutRunnable)
        if (!isRecognizerReleased) {
            runCatching { speechRecognizer?.stopListening() }
            runCatching { speechRecognizer?.cancel() }
        }
    }

    private fun releaseSpeechRecognizer() {
        handler.removeCallbacks(noInputTimeoutRunnable)
        handler.removeCallbacks(silenceTimeoutRunnable)
        if (isRecognizerReleased) return

        isRecognizerReleased = true
        runCatching { speechRecognizer?.cancel() }
        runCatching { speechRecognizer?.destroy() }
        speechRecognizer = null
    }

    private fun completeRecognition(text: String) {
        val recognized = text.trim()
        if (recognized.isBlank()) {
            showFailedState()
            return
        }
        releaseSpeechRecognizer()
        dismissAllowingStateLoss()
        onRecognized?.invoke(recognized)
    }

    private fun showListeningState() {
        val binding = _binding ?: return
        binding.tvTitle.text = "듣고 있어요"
        binding.tvSubtitle.text = "장소나 주소를 말씀해 주세요."
        binding.tvStatus.text = "음성 인식 진행 중..."
        binding.progressListening.isVisible = true
        binding.ivVoice.isVisible = false
        binding.tvStatus.isVisible = true
        binding.llButtons.isVisible = false
    }

    private fun showFailedState() {
        val binding = _binding ?: return
        stopListening()
        binding.tvTitle.text = "인식하지 못했어요"
        binding.tvSubtitle.text = "잘 알아듣지 못했어요.\n조금 더 크고 또렷하게 말씀해 주세요."
        binding.progressListening.isVisible = false
        binding.ivVoice.isVisible = true
        binding.tvStatus.isVisible = false
        binding.llButtons.isVisible = true
    }

    private fun canUpdateUi(): Boolean =
        _binding != null && isAdded && !isStateSaved && !isRecognizerReleased

    private fun Bundle?.firstSpeechText(): String {
        return this
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()
    }

    companion object {
        private const val NO_INPUT_TIMEOUT_MS = 5_000L
        private const val SILENCE_TIMEOUT_MS = 2_000L
    }
}
