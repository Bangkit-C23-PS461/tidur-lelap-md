package com.capstone.tidurlelap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.tidurlelap.data.remote.model.CalendarDay
import com.capstone.tidurlelap.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var calendarAdapter: CalendarAdapter

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

        fetchApiDataForCalendarDays(calendarDays)

        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Replace 'YourAdapter' with your own adapter class

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

    private fun fetchApiDataForCalendarDays(calendarDays: List<CalendarDay>) {
        for (day in calendarDays) {
            // TODO: Fetch data from the API for each day and update the corresponding CalendarDay object
        }

        // Notify the adapter that the data has changed
        calendarAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
