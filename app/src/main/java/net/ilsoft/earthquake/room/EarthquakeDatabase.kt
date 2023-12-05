package net.ilsoft.earthquake.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Earthquake::class], version = 1)
@TypeConverters(EarthquakeTypeConverters::class)
abstract class EarthquakeDatabase : RoomDatabase() {
    abstract fun earthquakeDAO(): EarthquakeDAO
}