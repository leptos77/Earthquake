package net.ilsoft.earthquake.room

import android.content.Context
import androidx.room.Room

class EarthquakeDatabaseAccessor {
    companion object {
        var instance: EarthquakeDatabase? = null
        val EARTHQUAKE_DB_NAME = "earthquake_db"
        fun getInstance(context: Context): EarthquakeDatabase {
            return if (instance == null) {
                Room.databaseBuilder(context, EarthquakeDatabase::class.java, EARTHQUAKE_DB_NAME)
                    .build()
            } else instance!!
        }
    }
}