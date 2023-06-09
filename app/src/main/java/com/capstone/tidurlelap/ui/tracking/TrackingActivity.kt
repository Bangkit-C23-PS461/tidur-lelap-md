package com.capstone.tidurlelap.ui.tracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.tidurlelap.R
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.tidurlelap.databinding.ActivityTrackingBinding
import java.nio.ShortBuffer
import java.util.Arrays

class TrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTrack.setOnClickListener {
            recordAndProcessAudio()
        }
    }

    // Method to calculate the mean of the recorded sound
    companion object {
        private const val SAMPLE_RATE = 44100 // Sample rate must be the same as used when recording
        private const val RECORD_AUDIO_PERMISSION_REQUEST = 200
    }

    // Method to calculate the mean of the recorded sound
    private fun calculateMean(audioData: ShortArray): Double {
        var sum = 0.0
        for (data in audioData) {
            sum += data
        }
        return sum / audioData.size
    }

    // Method to calculate the median of the recorded sound
    private fun calculateMedian(audioData: ShortArray): Double {
        Arrays.sort(audioData)
        val middleIndex = audioData.size / 2
        return if (audioData.size % 2 == 0) {
            (audioData[middleIndex - 1] + audioData[middleIndex]) / 2.0
        } else {
            audioData[middleIndex].toDouble()
        }
    }

    // Method to record sound and calculate mean/median
    private fun recordAndProcessAudio() {
        if (isRecordAudioPermissionGranted()) {
            try {
                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val audioData = ShortArray(bufferSize)

                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                audioRecord.startRecording()
                audioRecord.read(audioData, 0, bufferSize)
                audioRecord.stop()

                val mean = calculateMean(audioData)
                val median = calculateMedian(audioData)

                // Use the mean/median value as per your requirements
                // Cut and send the audio segments that are above the mean/median
            } catch (e: SecurityException) {
                // Handle the SecurityException when recording audio is not permitted
            }
        } else {
            requestRecordAudioPermission()
        }
    }

    // Check if the RECORD_AUDIO permission is granted
    private fun isRecordAudioPermissionGranted(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        val result = ContextCompat.checkSelfPermission(this, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    // Request the RECORD_AUDIO permission
    private fun requestRecordAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        ActivityCompat.requestPermissions(this, arrayOf(permission), RECORD_AUDIO_PERMISSION_REQUEST)
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_AUDIO_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start recording
                    recordAndProcessAudio()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message)
                }
            }
        }
    }
}