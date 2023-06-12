package com.capstone.tidurlelap.ui.result

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.UserModel
import com.capstone.tidurlelap.data.remote.response.ResultResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultViewModel(private val pref: UserPreference) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _result = MutableLiveData<ResultResponse>()
    val result: LiveData<ResultResponse> = _result

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun getResult(token: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getResult("Bearer $token")
        client.enqueue(object: Callback<ResultResponse> {
            override fun onResponse(
                call: Call<ResultResponse>,
                response: Response<ResultResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _result.value = response.body()
                } else {
                    Log.e("ResultViewModel", "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("ResultViewModel", "onFailure: ${t.message.toString()}")
            }
        })
    }
}