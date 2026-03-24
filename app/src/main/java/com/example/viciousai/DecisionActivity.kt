package com.example.viciousai

import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DecisionActivity : AppCompatActivity() {

    // ── Constantes ────────────────────────────────────────────────────────────
    companion object {
        private const val TAG              = "ViciousAI_Audio"
        private const val SEGMENT_DURATION = 5_000L
        private const val SAMPLE_RATE      = 44_100
        private const val BIT_RATE         = 128_000
    }

    // ── État ──────────────────────────────────────────────────────────────────
    private var isRecording  = false
    private var segmentIndex = 0
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    // ── Handler pour la boucle de segmentation ────────────────────────────────
    private val handler = Handler(Looper.getMainLooper())
    private val segmentRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                rotateSegment()
                handler.postDelayed(this, SEGMENT_DURATION)
            }
        }
    }

    // ── Vues ──────────────────────────────────────────────────────────────────
    private lateinit var statusText:  TextView
    private lateinit var segmentText: TextView
    private lateinit var startButton: MaterialButton
    private lateinit var stopButton:  MaterialButton

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decision)

        statusText  = findViewById(R.id.statusText)
        segmentText = findViewById(R.id.segmentText)
        startButton = findViewById(R.id.startRecordingButton)
        stopButton  = findViewById(R.id.stopRecordingButton)

        stopButton.isEnabled = false

        startButton.setOnClickListener { startRecordingLoop() }
        stopButton.setOnClickListener  { stopRecordingLoop()  }

        Log.i(TAG, "DecisionActivity créée — dossier : ${getOutputDir().absolutePath}")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) stopRecordingLoop()
    }

    // ── Logique d'enregistrement ──────────────────────────────────────────────

    private fun startRecordingLoop() {
        isRecording  = true
        segmentIndex = 0

        startButton.isEnabled = false
        stopButton.isEnabled  = true
        statusText.text       = "Enregistrement en cours…"

        Log.i(TAG, "═══ Démarrage de la session ═══")

        startNewSegment()
        handler.postDelayed(segmentRunnable, SEGMENT_DURATION)
    }

    private fun stopRecordingLoop() {
        isRecording = false
        handler.removeCallbacks(segmentRunnable)
        stopAndReleaseRecorder()

        startButton.isEnabled = true
        stopButton.isEnabled  = false
        statusText.text       = "Terminé — $segmentIndex segment(s)"
        segmentText.text      = ""

        Log.i(TAG, "═══ Session terminée — $segmentIndex segment(s) ═══")
        logAllFiles()
    }

    private fun rotateSegment() {
        stopAndReleaseRecorder()
        startNewSegment()
    }

    private fun startNewSegment() {
        segmentIndex++
        val file = File(getOutputDir(), "segment_${segmentIndex}_${timestamp()}.m4a")
        currentFile = file

        recorder = createRecorder(file).also { it.start() }

        Log.d(TAG, "▶ Segment #$segmentIndex démarré → ${file.name}")
        segmentText.text = "Segment #$segmentIndex en cours…"
    }

    private fun stopAndReleaseRecorder() {
        val savedFile = currentFile
        try {
            recorder?.apply { stop(); release() }
            savedFile?.let {
                Log.d(TAG, "■ Segment #$segmentIndex sauvegardé — ${it.name} (${it.length() / 1024} Ko)")
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "✘ Erreur segment #$segmentIndex : ${e.message}")
            savedFile?.delete()
        } finally {
            recorder    = null
            currentFile = null
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getOutputDir(): File =
        File(filesDir, "audio_segments").also { if (!it.exists()) it.mkdirs() }

    private fun timestamp(): String =
        SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())

    private fun createRecorder(outputFile: File): MediaRecorder =
        MediaRecorder(this).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(SAMPLE_RATE)
            setAudioEncodingBitRate(BIT_RATE)
            setOutputFile(outputFile.absolutePath)
            prepare()
        }

    private fun logAllFiles() {
        val files = getOutputDir().listFiles()?.sortedBy { it.name } ?: emptyList()
        Log.i(TAG, "── Fichiers dans audio_segments/ ──")
        if (files.isEmpty()) {
            Log.i(TAG, "  (aucun fichier)")
        } else {
            files.forEach { Log.i(TAG, "  ${it.name}  —  ${it.length() / 1024} Ko") }
        }
        Log.i(TAG, "────────────────────────────────────")
    }
}