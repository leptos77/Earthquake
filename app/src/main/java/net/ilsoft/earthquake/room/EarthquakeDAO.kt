package net.ilsoft.earthquake.room

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EarthquakeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEarthquakes(earthquakes: List<Earthquake>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEarthquake(earthquake: Earthquake)

    @Delete
    fun deleteEarthquake(earthquake: Earthquake)

    @Query("SELECT * FROM earthquake ORDER BY date DESC")
    fun loadAllEarthquakes(): LiveData<List<Earthquake>>

    @Query("SELECT id as _id, details as suggest_text_1, id as suggest_intent_data_id FROM earthquake WHERE details LIKE :query ORDER BY date DESC")
    fun generateSearchSuggestions(query: String): Cursor

    @Query("SELECT * FROM earthquake WHERE details LIKE :query ORDER BY date DESC")
    fun searchEarthquakes(query: String): LiveData<List<Earthquake>>

    @Query("SELECT * FROM earthquake WHERE id = :id LIMIT 1")
    fun getEarthquake(id: String): LiveData<Earthquake>

    @Query("SELECT * FROM earthquake ORDER BY date DESC")
    fun loadAllEarthquakesBlocking(): List<Earthquake>
}