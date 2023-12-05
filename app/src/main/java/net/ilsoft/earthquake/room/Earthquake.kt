package net.ilsoft.earthquake.room

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class Earthquake(
    @PrimaryKey
    val id: String,
    val date: Date?,
    val details: String,
    val location: Location?,
    val magnitude: Double,
    val link: String?
) {
    override fun toString(): String {
        val sdf = SimpleDateFormat("HH.mm", Locale.US)
        val dateString = sdf.format(date)
        return "$dateString : $magnitude $details"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Earthquake) {
            (other as Earthquake).id.contentEquals(id)
        } else false
    }
}
