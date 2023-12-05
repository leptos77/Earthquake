package net.ilsoft.earthquake

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import net.ilsoft.earthquake.room.Earthquake

class EarthquakeListFragment(
    val earthquakeViewModel: EarthquakeViewModel,
    val updateEarthquakes: () -> Unit
) : Fragment(),
    LifecycleEventObserver {
    lateinit var recyclerView: RecyclerView
    private val earthquakeAdapter = EarthquakeRecyclerViewAdapter(mutableListOf<Earthquake>())
    private lateinit var swipeToRefreshView: SwipeRefreshLayout
    private var mMinimumMagnitude = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_earthquake_list, container, false)
        recyclerView = view.findViewById(R.id.list) as RecyclerView
        swipeToRefreshView = view.findViewById(R.id.swiperefresh) as SwipeRefreshLayout
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = earthquakeAdapter
        swipeToRefreshView.setOnRefreshListener {
            updateEarthquakes()
            if (swipeToRefreshView.isRefreshing) {
                swipeToRefreshView.isRefreshing = false
            }
        }
    }

    fun setEarthquakes(earthquakes: MutableList<Earthquake>) {
        // Preference의 값을 읽어온다
        updateFromPreferences()

        earthquakeAdapter.earthquakes.clear()
        earthquakeAdapter.notifyDataSetChanged()
        earthquakes.forEach {
            if (!earthquakeAdapter.earthquakes.contains(it) && it.magnitude >= mMinimumMagnitude) {
                earthquakeAdapter.earthquakes.add(it)
                earthquakeAdapter.notifyItemInserted(earthquakeAdapter.earthquakes.indexOf(it))
            }
        }

        // 기존 값에서 최소 지진수치보다 작은것은 없앤다.
        if (earthquakes.isNotEmpty()) {
            earthquakes.reversed().forEachIndexed { index, earthquake ->
                if (earthquake.magnitude < mMinimumMagnitude && earthquakes.size > index) {
                    earthquakes.removeAt(index)
                    earthquakeAdapter.notifyItemRemoved(index)
                }
            }
        }

        if (swipeToRefreshView.isRefreshing) {
            swipeToRefreshView.isRefreshing = false
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            Log.d("프레그먼트", "라이프사이클 이벤트 발생")
            earthquakeViewModel.earthquakes?.observe(source) {
//                earthquakeAdapter.earthquakes = it as MutableList<Earthquake>
                setEarthquakes(it.toMutableList())
                earthquakeAdapter.notifyDataSetChanged()
                if (swipeToRefreshView.isRefreshing) {
                    swipeToRefreshView.isRefreshing = false
                }
            }
            earthquakeViewModel.loadEarthquake()

            // 프리퍼런스 이벤트 등록
            context?.let {
                val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                prefs.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (PreferencesActivity.PREF_MIN_MAG == key) {
                        val earthquakes = earthquakeViewModel.earthquakes?.value
                        if (earthquakes != null) setEarthquakes(earthquakes.toMutableList())
                    }
                }
            }
        }
    }

    private fun updateFromPreferences() {
        context?.let {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
            mMinimumMagnitude = prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3")?.toInt() ?: 3
        }
    }
}