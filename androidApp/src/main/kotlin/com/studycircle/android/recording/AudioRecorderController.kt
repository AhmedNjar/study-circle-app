package com.studycircle.android.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

// Wraps Android's MediaRecorder for the Quran-recitation taskrunner flow.
// Deliberately NOT in the KMP shared module — MediaRecorder is an Android
// API with no commonMain equivalent. An iOS build's equivalent (wrapping
// AVAudioRecorder) would be its own platform-specific controller living in
// an iosApp target, not a shared class with an expect/actual split, since
// the two recording APIs don't share enough shape to make that worthwhile.
class AudioRecorderController(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAtMillis: Long = 0L

    fun start(): String {
        val file = File(context.cacheDir, "recitation_${System.currentTimeMillis()}.m4a")
        outputFile = file

        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()

        recorder = r
        startedAtMillis = System.currentTimeMillis()
        return file.absolutePath
    }

    /** Returns (localPath, durationSeconds). */
    fun stop(): Pair<String, Int> {
        val r = recorder ?: error("Recording was never started")
        r.stop()
        r.release()
        recorder = null
        val durationSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000).toInt()
        val path = outputFile?.absolutePath ?: error("No output file")
        return path to durationSeconds
    }

    fun cancel() {
        runCatching {
            recorder?.stop()
            recorder?.release()
        }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}
