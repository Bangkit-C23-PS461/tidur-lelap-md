package com.capstone.tidurlelap.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.UserModel
import com.capstone.tidurlelap.data.remote.response.RegisterResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupViewModel(private val pref: UserPreference) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isRegistrationSuccessful = MutableLiveData<Boolean>()
    val isRegistrationSuccessful: LiveData<Boolean> = _isRegistrationSuccessful

    fun userRegister(name: String, email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().register(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        viewModelScope.launch {
                            pref.saveUser(
                                UserModel(
                                    false,
                                    ""
                                )
                            )
                        }
                        _isRegistrationSuccessful.value = true
                    } else {
                        _isRegistrationSuccessful.value = false
                    }
                } else {
                    _isLoading.value = false
                    val responseBody = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        RegisterResponse::class.java
                    )
                    _message.value = responseBody.message.toString()
                    _isRegistrationSuccessful.value = false
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = t.message.toString()
            }
        })
    }
}