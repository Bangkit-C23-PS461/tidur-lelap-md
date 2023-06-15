package com.capstone.tidurlelap.data.remote.retrofit

import com.capstone.tidurlelap.data.remote.request.LoginRequest
import com.capstone.tidurlelap.data.remote.request.RegisterRequest
import com.capstone.tidurlelap.data.remote.response.LoginResponse
import com.capstone.tidurlelap.data.remote.response.RegisterResponse
import com.capstone.tidurlelap.data.remote.response.ResultResponse
import com.capstone.tidurlelap.data.remote.response.UserResponse
import com.capstone.tidurlelap.data.remote.response.SaveSleepSessionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("login")
    fun login(
        @Body requestBody: LoginRequest
    ): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("register")
    fun register(@Body requestBody: RegisterRequest): Call<RegisterResponse>

    @Multipart
    @POST("sleep/session")
    fun saveSleepSession(
        @Header("Authorization") auth: String,
        @Part("fromTime") fromTime: RequestBody,
        @Part("toTime") toTime: RequestBody,
        @Part audioRecording: MultipartBody.Part,
    ): Call<SaveSleepSessionResponse>

    @GET("user/profile")
    fun getUser(
        @Header("Authorization") token: String,
    ): Call<UserResponse>

    @GET("sleep/quality")
    fun getResult(
        @Header("Authorization") auth: String,
        @Query("date") date: String
    ): Call<ResultResponse>

}