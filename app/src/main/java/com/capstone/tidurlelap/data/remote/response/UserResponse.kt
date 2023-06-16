package com.capstone.tidurlelap.data.remote.response

import com.google.gson.annotations.SerializedName

data class UserResponse (
    @field:SerializedName("email")
    val email : String,

    @field:SerializedName("username")
    val username : String
    )