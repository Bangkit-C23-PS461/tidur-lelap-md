package com.capstone.tidurlelap.ui.tracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.tidurlelap.R
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.tidurlelap.databinding.ActivityTrackingBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import java.util.Arrays
import kotlin.math.log10

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class TrackingActivity : AppCompatActivity() {

    private var fileName: String = ""

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var recordButton: Button
    private lateinit var playButton: Button

    private var isRecording = false
    private var isPlaying = false

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can start recording here.
                startRecording()
            } else {
                // Permission denied, handle the case gracefully.
                // You can show a dialog or disable the recording functionality.
            }
        }
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_tracking)

        // Record to the external cache directory for visibility
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        recordButton = findViewById(R.id.btn_track)
        playButton = findViewById(R.id.btn_play)

        recordButton.setOnClickListener {
            onRecordButtonClicked()
        }

        playButton.setOnClickListener {
            onPlayButtonClicked()
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    private fun onRecordButtonClicked() {
        if (isRecording) {
            stopRecording()
            recordButton.text = getString(R.string.start_recording)
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startRecording()
                recordButton.text = getString(R.string.stop_recording)
            } else {
                // Handle the case when the permission is not granted
                // You can show a dialog or request the permission again
                // explaining why it's needed.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            }
        }
        isRecording = !isRecording
    }

    private fun onPlayButtonClicked() {
        if (isPlaying) {
            stopPlaying()
            playButton.text = getString(R.string.start_playing)
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startPlaying()
                playButton.text = getString(R.string.stop_playing)
            } else {
                // Handle the case when the permission is not granted
                // You can show a dialog or request the permission again
                // explaining why it's needed.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            }
        }
        isPlaying = !isPlaying
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(fileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "prepare() failed")
                }

                start()
            }
        } else {
            // Handle the case when the permission is not granted
            // You can show a dialog or request the permission again
            // explaining why it's needed.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioBuffer = ShortArray(bufferSize)

            try {
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                audioRecord.startRecording()

                var sumDb = 0.0
                var samplesCount = 0

                while (isRecording) {
                    val samplesRead = audioRecord.read(audioBuffer, 0, bufferSize)

                    for (i in 0 until samplesRead) {
                        val amplitude = audioBuffer[i].toDouble() / Short.MAX_VALUE
                        val db = 20 * Math.log10(amplitude)

                        sumDb += db
                        samplesCount++
                    }
                }

                audioRecord.stop()
                audioRecord.release()

                val meanDb = sumDb / samplesCount
                val medianDb = calculateMedianDb(audioBuffer, samplesCount)

                // Potong audio berdasarkan nilai dB yang diinginkan
                val audioFile = File(fileName)
                val outputFile = File("${externalCacheDir?.absolutePath}/trimmed_audio.3gp")
                val inputStream = FileInputStream(audioFile)
                val outputStream = FileOutputStream(outputFile)

                val buffer = ByteArray(bufferSize)

                while (inputStream.read(buffer) != -1) {
                    // Menghitung dB dari buffer
                    val amplitude = ByteBuffer.wrap(buffer).short.toDouble() / Short.MAX_VALUE
                    val db = 20 * log10(amplitude)

                    // Potong audio jika nilai dB buffer di atas meanDb/medianDb
                    if (db > meanDb) {
                        outputStream.write(buffer)
                    }
                }

                inputStream.close()
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "stopRecording() failed: ${e.message}")
            } catch (e: SecurityException) {
                Log.e(LOG_TAG, "stopRecording() failed: ${e.message}")
            }
        }
    }

    private fun calculateMedianDb(audioBuffer: ShortArray, samplesCount: Int): Double {
        val sortedBuffer = audioBuffer.copyOf(samplesCount)
        Arrays.sort(sortedBuffer)

        val medianIndex = samplesCount / 2
        val medianValue = sortedBuffer[medianIndex].toDouble() / Short.MAX_VALUE

        return 20 * Math.log10(medianValue)
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
        }


}