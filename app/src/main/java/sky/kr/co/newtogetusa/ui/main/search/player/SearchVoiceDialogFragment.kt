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

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startListening()
            } else {
                showFailedState()
            }
        }

    private val noInputTimeoutRunnable = Runnable {
        stopListening()
        showFailedState()
    }

    private val silenceTimeoutRunnable = Runnable {
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
            stopListening()
            dismiss()
        }
        binding.btnRetry.setOnClickListener {
            lastRecognizedText = ""
            startWithPermission()
        }
        startWithPermission()
    }

    override fun onDestroyView() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _binding = null
        super.onDestroyView()
    }

    fun onRecognized(callback: (String) -> Unit): SearchVoiceDialogFragment {
        onRecognized = callback
        return this
    }

    private fun startWithPermission() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            showFailedState()
            return
        }

        val permissionState = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        stopListening()
        showListeningState()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext()).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() {
                    handler.removeCallbacks(noInputTimeoutRunnable)
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    if (lastRecognizedText.isNotBlank()) {
                        completeRecognition(lastRecognizedText)
                    } else {
                        showFailedState()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val text = results.firstSpeechText()
                    if (text.isNotBlank()) {
                        completeRecognition(text)
                    } else {
                        showFailedState()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
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
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
    }

    private fun completeRecognition(text: String) {
        stopListening()
        val recognized = text.trim()
        if (recognized.isBlank()) {
            showFailedState()
            return
        }
        dismiss()
        onRecognized?.invoke(recognized)
    }

    private fun showListeningState() {
        binding.tvTitle.text = "듣고 있어요"
        binding.tvSubtitle.text = "장소나 주소를 말씀해 주세요."
        binding.tvStatus.text = "음성 인식 진행 중..."
        binding.progressListening.isVisible = true
        binding.ivVoice.isVisible = false
        binding.tvStatus.isVisible = true
        binding.llButtons.isVisible = false
    }

    private fun showFailedState() {
        stopListening()
        binding.tvTitle.text = "인식하지 못했어요"
        binding.tvSubtitle.text = "잘 알아듣지 못했어요.\n조금 더 크고 또렷하게 말씀해 주세요."
        binding.progressListening.isVisible = false
        binding.ivVoice.isVisible = true
        binding.tvStatus.isVisible = false
        binding.llButtons.isVisible = true
    }

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
