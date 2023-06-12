package com.capstone.tidurlelap.data.remote.response

import com.google.gson.annotations.SerializedName

data class ResultResponse(

	@field:SerializedName("snoreCount")
	val snoreCount: Int,

	@field:SerializedName("sleepNoise")
	val sleepNoise: Int,

	@field:SerializedName("sleepTime")
	val sleepTime: Int,

	@field:SerializedName("sleepScore")
	val sleepScore: Int
)
