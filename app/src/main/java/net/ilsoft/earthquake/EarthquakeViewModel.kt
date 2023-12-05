package net.ilsoft.earthquake

import android.app.Application
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import net.ilsoft.earthquake.room.Earthquake
import net.ilsoft.earthquake.room.EarthquakeDatabaseAccessor
import net.ilsoft.earthquake.workmanager.EarthquakeUpdateWorker
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class EarthquakeViewModel(val application: Application) : ViewModel() {
    private val TAG = "프레그먼트2"
//    val handler = android.os.Handler(Looper.getMainLooper())

    var earthquakes: LiveData<List<Earthquake>>? = null
        get() {
            if (field == null) {
                field = EarthquakeDatabaseAccessor
                    .getInstance(application)
                    .earthquakeDAO()
                    .loadAllEarthquakes()
                loadEarthquake()
            }
            return field
        }

    fun loadEarthquake() {
        // workermanager 호출
        EarthquakeUpdateWorker.scheduleUpdateWorker(application)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return EarthquakeViewModel(application) as T
            }
        }
    }
}