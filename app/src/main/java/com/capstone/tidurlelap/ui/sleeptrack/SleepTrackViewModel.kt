package com.capstone.tidurlelap.ui.sleeptrack

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.UserDetailModel
import com.capstone.tidurlelap.data.remote.model.UserModel
import com.capstone.tidurlelap.data.remote.response.SaveSleepSessionResponse
import com.capstone.tidurlelap.data.remote.response.UserResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class SleepTrackViewModel(private val pref: UserPreference) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _username = MutableLiveData<UserResponse>()
    val username: LiveData<UserResponse> = _username

    private val _responseStatus = MutableLiveData<String>()

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    fun getDetailUser(): LiveData<UserDetailModel> {
        return pref.getDetailUser().asLiveData()
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun addAudio(token: String, startTime: String, endTime: String, fileName: String) {
            val file = File(fileName)
            val requestFile = file.asRequestBody("audio/aac".toMediaType())
            val audioPart = MultipartBody.Part.createFormData("audioRecording", file.name, requestFile)
            val startTimeBody = startTime.toRequestBody("text/plain".toMediaType())
            val endTimeBody = endTime.toRequestBody("text/plain".toMediaType())

            _isLoading.value = true
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
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            _responseStatus.value = responseBody.message
                        } else {
                            _responseStatus.value = responseBody?.message
                        }
                        _isSuccess.value = true
                    } else {
                        _responseStatus.value = "Recording failed"
                        _isSuccess.value = false
                    }
                }
                override fun onFailure(call: Call<SaveSleepSessionResponse>, t: Throwable) {
                    _isLoading.value = false
                    _responseStatus.value = t.message.toString()
                    _isSuccess.value = false
                }
            })
    }
}