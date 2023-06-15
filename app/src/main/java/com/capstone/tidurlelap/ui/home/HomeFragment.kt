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
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
class HomeFragment : Fragment() {

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var apiService: ApiService
    private lateinit var userPreference: UserPreference

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val API_DELAY_MS = 3000L

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

        userPreference = UserPreference.getInstance(requireContext().dataStore)

        calendarAdapter = CalendarAdapter(calendarDays.toMutableList()) { calendarDay ->
            // Make API call here using the calendarDay object
            // You can use the same fetchApiDataForCalendarDays method or create a separate method for this

            val token = runBlocking { userPreference.getUser().firstOrNull()?.token }

            val calendar = Calendar.getInstance()
            calendar.time = calendarDay.date
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            apiService.getResult("Bearer $token", dateString).enqueue(object : Callback<ResultResponse> {
                override fun onResponse(call: Call<ResultResponse>, response: Response<ResultResponse>) {
                    if (response.isSuccessful) {
                        val result = response.body()

                        // Update the UI with the response data
                        binding.tvSleepTime.text = result?.sleepTime?.toString() ?: "0"
                        binding.tvSleepScore.text = result?.sleepScore?.toString() ?: ""
                        binding.tvSnoreCount.text = result?.snoreCount?.toString() ?: ""
                        binding.tvSleepNoise.text = result?.sleepNoise?.toString() ?: ""

                        // Update the CalendarDay object with the fetched data
                        calendarDay.sleepQuality = result?.sleepScore ?: 0
                        calendarDay.isDataFetched = true

                        // Notify the adapter that the data has changed for the clicked item
                        val clickedItemPosition = calendarDays.indexOf(calendarDay)
                        calendarAdapter.notifyItemChanged(clickedItemPosition)
                    } else {
                        // Handle API error
                    }
                }

                override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                    // Handle network error
                }
            })
        }

        binding.rvDate.adapter = calendarAdapter

        val homeViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(UserPreference.getInstance(requireContext().dataStore))
            ).get(HomeViewModel::class.java)

        homeViewModel.getUser().observe(viewLifecycleOwner) { user ->
            homeViewModel.getUserData(user.token)
            fetchApiDataForCalendarDays(user.token, calendarDays)
        }

        homeViewModel.getDetailUser().observe(viewLifecycleOwner) { user ->
            binding.textView.text = getString(R.string.greeting_home, user.username)
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
        val handler = Handler(Looper.getMainLooper())

        // Iterate through each day with delay
        calendarDays.forEachIndexed { index, day ->
            handler.postDelayed({
                // Check if data for the day is already fetched
                if (!day.isDataFetched) {
                    val calendar = Calendar.getInstance()
                    calendar.time = day.date

                    val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                    apiService.getResult("Bearer $token", dateString).enqueue(object : Callback<ResultResponse> {
                        override fun onResponse(call: Call<ResultResponse>, response: Response<ResultResponse>) {
                            if (response.isSuccessful) {
                                val result = response.body()

                                binding.tvSleepTime.text = result?.sleepTime?.toString() ?: ""
                                binding.tvSleepScore.text = result?.sleepScore?.toString() ?: ""
                                binding.tvSnoreCount.text = result?.snoreCount?.toString() ?: ""
                                binding.tvSleepNoise.text = result?.sleepNoise?.toString() ?: ""

                                day.sleepQuality = result?.sleepScore ?: 0
                                day.isDataFetched = true
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
            }, index * API_DELAY_MS) // Delay based on the index of the day
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
