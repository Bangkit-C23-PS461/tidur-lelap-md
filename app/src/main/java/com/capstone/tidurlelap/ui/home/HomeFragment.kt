package com.capstone.tidurlelap.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.CalendarDay
import com.capstone.tidurlelap.data.remote.response.ResultResponse
import com.capstone.tidurlelap.data.remote.retrofit.ApiConfig
import com.capstone.tidurlelap.data.remote.retrofit.ApiService
import com.capstone.tidurlelap.databinding.FragmentHomeBinding
import com.capstone.tidurlelap.ui.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
class HomeFragment : Fragment() {

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var apiService: ApiService

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDate.layoutManager = layoutManager
        (activity as AppCompatActivity).supportActionBar?.hide()

        val calendarDays = createCalendarDays()

        calendarAdapter = CalendarAdapter(calendarDays)

        binding.rvDate.adapter = calendarAdapter

        val homeViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(UserPreference.getInstance(requireContext().dataStore))
            ).get(HomeViewModel::class.java)

        homeViewModel.getDetailUser().observe(viewLifecycleOwner) { user ->
            binding.textView.text = getString(R.string.greeting_home, user.username)
        }

        homeViewModel.getUser().observe(viewLifecycleOwner) { user ->
            fetchApiDataForCalendarDays(user.token, calendarDays)
        }

        apiService = ApiConfig.getApiService()

        return root
    }

    private fun createCalendarDays(): List<CalendarDay> {
        val calendarDays = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()

        // Get the current date
        val currentDate = calendar.time

        // Set the calendar to the start of the week (e.g., Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        // Iterate through each day of the week and create a CalendarDay object
        for (i in 1..7) {
            val date = calendar.time
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)

            // Create a CalendarDay object with the date and day of the week
            val calendarDay = CalendarDay(date, dayOfWeek)

            // Add the CalendarDay object to the list
            calendarDays.add(calendarDay)

            // Move to the next day
            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }

        return calendarDays
    }

    private fun fetchApiDataForCalendarDays(token: String, calendarDays: List<CalendarDay>) {
        val apiService = ApiConfig.getApiService()

        for (day in calendarDays) {
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day.date)

            apiService.getResult("Bearer $token", dateString)
                .enqueue(object : Callback<ResultResponse> {
                    override fun onResponse(call: Call<ResultResponse>, response: Response<ResultResponse>) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()

                            // Update the corresponding CalendarDay object with the fetched values
                            apiResponse?.let {
                                day.sleepTime = it.sleepTime.toString()
                                day.sleepNoise = it.sleepNoise.toString()
                                day.snoreCount = it.snoreCount.toString()
                            }

                            // Notify the adapter that the data has changed
                            calendarAdapter.notifyDataSetChanged()
                        } else {
                            // Handle API error response
                            // You can show an error message or handle the error in any other way
                        }
                    }

                    override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                        // Handle API call failure
                        // You can show an error message or handle the failure in any other way
                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
