package net.ilsoft.earthquake

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.switchMap
import androidx.recyclerview.widget.RecyclerView
import net.ilsoft.earthquake.room.Earthquake
import net.ilsoft.earthquake.room.EarthquakeDatabaseAccessor


class EarthquakeSearchResultActivity : AppCompatActivity() {
    private val mEarthquakes: ArrayList<Earthquake> = ArrayList()
    private val mEarthquakeAdapter = EarthquakeRecyclerViewAdapter(mEarthquakes)
    private var searchQuery: MutableLiveData<String?>? = null
    private var searchResults: LiveData<List<Earthquake>>? = null
    private var selectedSearchSuggestionId: MutableLiveData<String?>? = null
    private var selectedSearchSuggestion: LiveData<Earthquake>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earthquake_search_result)
        // 위로가기 버튼
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.search_result_list)
        recyclerView.adapter = mEarthquakeAdapter

        //검색 쿼리 라이브 데이터를 초기화한다.
        searchQuery = MutableLiveData()
        searchQuery?.value = null

        //검색 쿼리 라이브 데이터를 검색 결과 라이브 데이터에 연결한다.
        //검색 쿼리가 변경되면 데이터베이스에 쿼리를 수행해
        //검색 결과가 변경되도록 변환 Map을 구성한다.
        searchResults = searchQuery?.switchMap {
            EarthquakeDatabaseAccessor
                .getInstance(applicationContext)
                .earthquakeDAO()
                .searchEarthquakes("%$it%")
        }
        //검색 결과 라이브 데이터의 변경 내용을 관찰한다.
        searchResults?.observe(this@EarthquakeSearchResultActivity, searchQueryResultObserver)

        // 선택된 검색 제안 Id  라이브 데이터를 초기화한다.
        selectedSearchSuggestionId = MutableLiveData()
        selectedSearchSuggestionId?.value = null
        //선택된 검색 제안 ID라이브 데이터를 선택된 검색 제안 라이브 데이터에 연결한다.
        //선택된 검색 제안의 ID가 변경되면 데이터베이스에 ㅜ커리를 수행해 해당 지진 데이터를 반환하는 라이브 데이터를 변경하도록 변환 Map을 구성한다.
        selectedSearchSuggestion = selectedSearchSuggestionId?.switchMap {
            EarthquakeDatabaseAccessor
                .getInstance(applicationContext)
                .earthquakeDAO()
                .getEarthquake(it!!)
        }
        if (Intent.ACTION_VIEW == intent.action) {
            selectedSearchSuggestion?.observe(this, selectedSearchSuggestionObserver)
            intent.data?.let { setSelectedSearchSuggestion(it) }
        } else {
            //액티비티가 검색 쿼리로부터 시작되면
            val query = intent.getStringExtra(SearchManager.QUERY)
            setSearchQuery(query!!)
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery?.value = query
    }

    val searchQueryResultObserver = { updatedEarthquakes: List<Earthquake>? ->
        // 변경된 검색 쿼리 결과로 UI를 변경한다.
        mEarthquakes.clear()
        updatedEarthquakes?.let {
            mEarthquakes.addAll(it)
        }
        mEarthquakeAdapter.notifyDataSetChanged()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (Intent.ACTION_VIEW == intent?.action) {
            setSelectedSearchSuggestion(intent.data!!)
        } else {
            val query = intent?.getStringExtra(SearchManager.QUERY)
            setSearchQuery(query!!)
        }
    }

    fun setSelectedSearchSuggestion(dataString: Uri) {
        val id = dataString.pathSegments.get(1)
        selectedSearchSuggestionId?.value = id
    }

    val selectedSearchSuggestionObserver: Observer<Earthquake> = Observer {
        setSearchQuery(it.details)
    }
}