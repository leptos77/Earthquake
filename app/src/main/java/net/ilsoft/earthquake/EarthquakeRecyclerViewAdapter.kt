package net.ilsoft.earthquake

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.ilsoft.earthquake.databinding.ListItemEarthquakeBinding
import net.ilsoft.earthquake.room.Earthquake
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class EarthquakeRecyclerViewAdapter constructor(var earthquakes: MutableList<Earthquake>) :
    RecyclerView.Adapter<EarthquakeRecyclerViewAdapter.ViewHolder>() {
    companion object {
        private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.US)
        private val MAGNITUDE_FORMAT = DecimalFormat("0.0")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListItemEarthquakeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val earthquake = earthquakes[position]
        holder.binding.earthquake = earthquake
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return earthquakes.size
    }


    class ViewHolder(val binding: ListItemEarthquakeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.timeformat = TIME_FORMAT
            binding.magnitudeformat = MAGNITUDE_FORMAT
        }
    }
}