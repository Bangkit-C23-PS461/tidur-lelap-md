package com.capstone.tidurlelap.data.remote.model

import java.util.Date

data class CalendarDay(
    val date: Date,
    val dayOfWeek: String,
    // Add any other relevant properties for a day in the calendar
    var sleepTime: String = "",
    var sleepNoise: String = "",
    var snoreCount: String = "",
    var sleepQuality: Int = 0,
    var isDataFetched: Boolean = false
)