package com.capstone.tidurlelap.data.remote.retrofit

import com.capstone.tidurlelap.data.remote.response.LoginResponse
import com.capstone.tidurlelap.data.remote.response.RegisterResponse
import com.capstone.tidurlelap.data.remote.response.ResultResponse
import com.capstone.tidurlelap.data.remote.response.SaveSleepSessionResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>


    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @Multipart
    @POST("sleep/session")
    fun saveSleepSession(
        @Header("Authorization") auth: String,
        @Part file: MultipartBody.Part
    ): Call<SaveSleepSessionResponse>

    @GET("sleep/quality?date=2023-05-22")
    fun getResult(
        @Header("Authorization") auth: String,
    ): Call<ResultResponse>
}