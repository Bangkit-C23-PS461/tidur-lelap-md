package com.capstone.tidurlelap.ui.sleeptrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.UserModel
import com.capstone.tidurlelap.data.remote.response.SaveSleepSessionResponse
import com.capstone.tidurlelap.data.remote.response.UserResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import com.capstone.tidurlelap.data.remote.retrofit.ApiService
import com.capstone.tidurlelap.databinding.FragmentSleepTrackBinding
import com.capstone.tidurlelap.ui.ViewModelFactory
import com.capstone.tidurlelap.ui.result.ResultActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.lifecycleScope
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class SleepTrackFragment : Fragment() {


    private var _binding: FragmentSleepTrackBinding? = null
    private var fileName: String = ""

    private var recorder: MediaRecorder? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private var isRecording = false

    private var startTime: String = ""
    private var endTime: String = ""


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSleepTrackBinding.inflate(inflater, container, false)
        val root: View = binding.root
        (activity as AppCompatActivity).supportActionBar?.hide()

        val dashboardViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(UserPreference.getInstance(requireContext().dataStore))
            ).get(SleepTrackViewModel::class.java)

        dashboardViewModel.getUser().observe(viewLifecycleOwner) {
            val token = it.token
            dashboardViewModel.getUserData(token)
        }

        dashboardViewModel.getDetailUser().observe(viewLifecycleOwner){
            val username = it.username
            binding.tvGreeting.text = getString(R.string.greeting, username)
        }

        // Record to the external cache directory for visibility
        fileName = "${requireContext().externalCacheDir?.absolutePath}/audiorecordtest.aac"

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        val dashboardViewModel =
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

        dashboardViewModel.getUser().observe(viewLifecycleOwner) { user ->
            addAudio(user.token, startTime, endTime)
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }

    private fun addAudio(token: String, startTime: String, endTime: String, ) {
        if (fileName != null) {
            val file = File(fileName)
            val requestFile = file.asRequestBody("audio/aac".toMediaType())
            val audioPart = MultipartBody.Part.createFormData("audioRecording", file.name, requestFile)
            val startTimeBody = startTime.toRequestBody("text/plain".toMediaType())
            val endTimeBody = endTime.toRequestBody("text/plain".toMediaType())

            val uploadAudioRequest =
                ApiConfig.getApiService().saveSleepSession(
                    "Bearer $token",
                    startTimeBody,
                    endTimeBody,
                    audioPart)
            uploadAudioRequest.enqueue(object : Callback<SaveSleepSessionResponse> {
                override fun onResponse(
                    call: Call<SaveSleepSessionResponse>,
                    response: Response<SaveSleepSessionResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            Toast.makeText(
                                requireContext(),
                                responseBody.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(requireContext(), ResultActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    } else {
                        Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<SaveSleepSessionResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(
                requireContext(),
                "Have you tried to track your sleep?",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCurrentTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val currentTime = Date()
        return dateFormat.format(currentTime)
    }
}
