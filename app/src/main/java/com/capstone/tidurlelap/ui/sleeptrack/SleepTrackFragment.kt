package com.capstone.tidurlelap.ui.sleeptrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.databinding.FragmentSleepTrackBinding
import com.capstone.tidurlelap.ui.ViewModelFactory
import com.capstone.tidurlelap.ui.result.ResultActivity
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class SleepTrackFragment : Fragment() {


    private var _binding: FragmentSleepTrackBinding? = null
    private var fileName: String = ""

    private var recorder: MediaRecorder? = null

    // Requesting permission to RECORD_AUDIO
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private var isRecording = false

    private var startTime: String = ""
    private var endTime: String = ""


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSleepTrackBinding.inflate(inflater, container, false)
        val root: View = binding.root
        (activity as AppCompatActivity).supportActionBar?.hide()

        // Record to the external cache directory for visibility
        fileName = "${requireContext().externalCacheDir?.absolutePath}/audiorecordtest.aac"

        setupViewModel()

        binding.btnTrack.setOnClickListener {
            onRecordButtonClicked()
        }

        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )

        return root
    }

    private fun setupViewModel() {
        val sleepTrackViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(UserPreference.getInstance(requireContext().dataStore))
            ).get(SleepTrackViewModel::class.java)

        sleepTrackViewModel.isLoading.observe(viewLifecycleOwner) {
            showLoading(it)
        }

        sleepTrackViewModel.getDetailUser().observe(viewLifecycleOwner){
            val username = it.username
            binding.tvGreeting.text = getString(R.string.greeting, username)
        }

        sleepTrackViewModel.isSuccess.observe(viewLifecycleOwner) {
            if (it) {
                val intent = Intent(requireContext(), ResultActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
            else {
                showDialog("Error", "Recording failed")
            }
        }
    }

    private fun onRecordButtonClicked() {
        if (isRecording) {
            stopRecording()
            binding.btnTrack.text = getString(R.string.tracking_before)
        } else {
            startRecording()
            binding.btnTrack.text = getString(R.string.tracking_after)
        }
        isRecording = !isRecording
    }

    @SuppressLint("RestrictedApi")
    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            val samplingRate = 44100
            setAudioSamplingRate(samplingRate)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()

            startTime = getCurrentTimeFormatted()
            Log.d("Time", "startTime: $startTime")
        }
    }

    private fun stopRecording() {
        val sleepTrackViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(UserPreference.getInstance(requireContext().dataStore))
            ).get(SleepTrackViewModel::class.java)

        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        endTime = getCurrentTimeFormatted()
        Log.d("Time", "endTime: $endTime")

        sleepTrackViewModel.getUser().observe(viewLifecycleOwner) { user ->
            sleepTrackViewModel.addAudio(user.token, startTime, endTime, fileName)
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }

    private fun getCurrentTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val currentTime = Date()
        return dateFormat.format(currentTime)
    }

    private fun showDialog(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
        alertDialog.show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
