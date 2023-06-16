package com.capstone.tidurlelap.ui.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.CalendarDay
import com.capstone.tidurlelap.data.remote.model.UserDetailModel
import com.capstone.tidurlelap.data.remote.model.UserModel
import com.capstone.tidurlelap.data.remote.response.ResultResponse
import com.capstone.tidurlelap.data.remote.response.UserResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeViewModel(private val pref: UserPreference) : ViewModel() {

    private val _username = MutableLiveData<UserResponse>()
    val username: LiveData<UserResponse> = _username

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _result = MutableLiveData<ResultResponse>()
    val result: LiveData<ResultResponse> = _result

    fun getDetailUser(): LiveData<UserDetailModel> {
        return pref.getDetailUser().asLiveData()
    }

    fun getUserData(token: String) {
        val client = ApiConfig.getApiService().getUser("Bearer $token")
        client.enqueue(object: Callback<UserResponse> {
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

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun fetchApiDataForCalendarDays(token: String, calendarDays: List<CalendarDay>) {
        val handler = Handler(Looper.getMainLooper())
        val API_DELAY_MS = 3000L

        // Iterate through each day with delay
        calendarDays.forEachIndexed { index, day ->
            handler.postDelayed({
                // Check if data for the day is already fetched
                if (!day.isDataFetched) {
                    val calendar = Calendar.getInstance()
                    calendar.time = day.date

                    val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    val apiService = ApiConfig.getApiService()

                    apiService.getResult("Bearer $token", dateString).enqueue(object : Callback<ResultResponse> {
                        override fun onResponse(call: Call<ResultResponse>, response: Response<ResultResponse>) {
                            if (response.isSuccessful) {
                                _result.value = response.body()
                                day.isDataFetched = true
                            } else {
                                Log.e("HomeViewModel", "onFailure: ${response.message()}")
                            }
                        }

                        override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                            Log.e("HomeViewModel", "onFailure: ${t.message.toString()}")
                        }
                    })
                }
            }, index * API_DELAY_MS) // Delay based on the index of the day
        }
    }
}