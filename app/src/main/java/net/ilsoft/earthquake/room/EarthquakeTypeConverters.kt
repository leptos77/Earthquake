package net.ilsoft.earthquake.room

import android.location.Location
import androidx.room.TypeConverter
import java.util.Date

class EarthquakeTypeConverters {
    @TypeConverter
    fun dateFromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return if (date == null) null else date.time
    }

    @TypeConverter
    fun locationToString(location: Location?): String? {
        return if (location == null) null else "${location.latitude},${location.longitude}"
    }

    @TypeConverter
    fun locationFromString(location: String?): Location? {
        return if (location != null && location.contains(",")) {
            val result = Location("Generated")
            val locationStrings = location.split(",")
            if (locationStrings.size == 2) {
                result.latitude = locationStrings[0].toDouble()
                result.longitude = locationStrings[1].toDouble()
                result
            } else null
        } else null
    }
}