package com.capstone.tidurlelap.ui.sleeptrack

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.UserDetailModel
import com.capstone.tidurlelap.data.remote.model.UserModel
import com.capstone.tidurlelap.data.remote.response.ResultResponse
import com.capstone.tidurlelap.data.remote.response.UserResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SleepTrackViewModel(private val pref: UserPreference) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _username = MutableLiveData<UserResponse>()
    val username: LiveData<UserResponse> = _username

    fun getDetailUser(): LiveData<UserDetailModel> {
        return pref.getDetailUser().asLiveData()
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun getUserData(token: String) {
        val client = ApiConfig.getApiService().getUser("Bearer $token")
        client.enqueue(object: Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    _username.value = response.body()
                    val responseBody = response.body()
                    if(responseBody != null) {
                        viewModelScope.launch {
                            val email = responseBody.email
                            val username = responseBody.username
                            val userDetailModel = UserDetailModel(email, username)
                            pref.saveDetailUser(userDetailModel)
                        }
                    }
                }
                else {
                    Log.e("UsernameViewModel", "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("UsernameViewModel", "onFailure: ${t.message.toString()}")
            }
        })
    }
}