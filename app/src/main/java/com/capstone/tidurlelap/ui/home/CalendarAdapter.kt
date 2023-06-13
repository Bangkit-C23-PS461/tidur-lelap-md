package com.capstone.tidurlelap.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.remote.model.CalendarDay
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val calendarDays: List<CalendarDay>) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_button, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendarDay = calendarDays[position]

        // Bind data to the views in the item layout
        holder.dateTextView.text = SimpleDateFormat("dd-MM", Locale.getDefault()).format(calendarDay.date)
        holder.dayOfWeekTextView.text = calendarDay.dayOfWeek

        // TODO: Display any other relevant information from the CalendarDay object
    }

    override fun getItemCount(): Int {
        return calendarDays.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        val dayOfWeekTextView: TextView = itemView.findViewById(R.id.tvDay)

        // TODO: Add references to other views in the item layout if needed
    }
}