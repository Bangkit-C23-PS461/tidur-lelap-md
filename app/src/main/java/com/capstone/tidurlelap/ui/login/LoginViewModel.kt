package com.capstone.tidurlelap.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.UserModel
import com.capstone.tidurlelap.data.remote.request.LoginRequest
import com.capstone.tidurlelap.data.remote.request.RegisterRequest
import com.capstone.tidurlelap.data.remote.response.LoginResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: UserPreference): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun authenticate(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        _isLoading.value = true
        val client = ApiConfig.getApiService().login(loginRequest)
        client.enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful) {
                    _isLoading.value = false
                    val responseBody = response.body()
                    if(responseBody != null) {
                        viewModelScope.launch {
                            pref.saveUser(
                                UserModel(
                                    true,
                                    responseBody.token
                                )
                            )
                            pref.login(responseBody.token)
                        }
                    } else {
                        _message.value = responseBody?.message
                    }
                }
                else {
                    _isLoading.value = false
                    val responseBody = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        LoginResponse::class.java
                    )
                    _message.value = responseBody.message
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = t.message.toString()
            }
        })
    }
}