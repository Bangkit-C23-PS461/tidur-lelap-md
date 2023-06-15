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

        calendarAdapter = CalendarAdapter(calendarDays.toMutableList()) { calendarDay ->
            // Implement logic to update sleepTime, sleepNoise, and snoreCount
            // You can show a dialog or navigate to another screen to capture user input
            // and then update the corresponding values in calendarDay object
            val updatedSleepTime = "Updated Sleep Time"
            val updatedSleepNoise = "Updated Sleep Noise"
            val updatedSnoreCount = "Updated Snore Count"

            // Update the values of the calendarDay object
            calendarDay.sleepTime = updatedSleepTime
            calendarDay.sleepNoise = updatedSleepNoise
            calendarDay.snoreCount = updatedSnoreCount

            // Notify adapter that the data has changed for the clicked item
            val clickedItemPosition = calendarDays.indexOf(calendarDay)
            calendarAdapter.notifyItemChanged(clickedItemPosition)
        }

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
            // Check if data for the day is already fetched
            if (!day.isDataFetched) {
                val calendar = Calendar.getInstance() // Create a new instance of Calendar

                calendar.time = day.date // Set the Calendar's date to the current day

                val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                apiService.getResult("Bearer $token", dateString).enqueue(object : Callback<ResultResponse> {
                    override fun onResponse(call: Call<ResultResponse>, response: Response<ResultResponse>) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            // Update the corresponding TextView values
                            binding.tvSleepTime.text = result?.sleepTime?.toString() ?: ""
                            binding.tvSleepScore.text = result?.sleepScore?.toString() ?: ""
                            binding.tvSnoreCount.text = result?.snoreCount?.toString() ?: ""
                            binding.tvSleepNoise.text = result?.sleepNoise?.toString() ?: ""

                            // Use the result values as needed
                            day.sleepQuality = result?.sleepScore ?: 0
                            day.isDataFetched = true  // Set the flag to indicate data is fetched
                            calendarAdapter.notifyDataSetChanged()
                        } else {
                            // Handle API error
                        }
                    }

                    override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                        // Handle network error
                    }
                })
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
