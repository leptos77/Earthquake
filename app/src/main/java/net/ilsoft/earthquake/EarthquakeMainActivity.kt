package net.ilsoft.earthquake

import android.app.Activity
import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import net.ilsoft.earthquake.R.*
import java.lang.Exception

class EarthquakeMainActivity : AppCompatActivity() {
    private val TAG_LIST_FRAGMENT: String = "TAG_LIST_FRAGMENT"
    private val MENU_PREFERENCES: Int = Menu.FIRST + 1
    private val SHOW_PREFERENCES = 1

    private lateinit var earthquakeListFragment: EarthquakeListFragment
    val earthquakeViewModel: EarthquakeViewModel by viewModels { EarthquakeViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_earthquake_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


//        val fm = supportFragmentManager
//
//        if (savedInstanceState == null) {
//            val ft = fm.beginTransaction()
//            earthquakeListFragment = EarthquakeListFragment(earthquakeViewModel) {
//                earthquakeViewModel.loadEarthquake()
//            }
//            ft.add(id.view_pager, earthquakeListFragment, TAG_LIST_FRAGMENT)
//            ft.commitNow()
//            Log.d("프레그먼트-메인", "프레그먼트 실행")
//        } else {
//            earthquakeListFragment =
//                fm.findFragmentByTag(TAG_LIST_FRAGMENT) as EarthquakeListFragment
//        }

        val viewPager2 = findViewById<ViewPager2>(R.id.view_pager)
        if (viewPager2 != null) {
            val pagerAdapter = EarthquakeTabsPagerAdapter(this)
            viewPager2.adapter = pagerAdapter
            val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
            TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.tab_list)
                    1 -> getString(R.string.tab_map)
                    else -> ""
                }
            }.attach()
        }

//        ProcessLifecycleOwner.get().lifecycle.addObserver(earthquakeListFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menu?.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_settings)
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchableInfo = searchManager.getSearchableInfo(
            ComponentName(
                applicationContext,
                EarthquakeSearchResultActivity::class.java
            )
        )
        val searchView = menu?.findItem(id.search_view)?.actionView as SearchView
        searchView.setSearchableInfo(searchableInfo)
        searchView.setIconifiedByDefault(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_PREFERENCES -> {
                val intent = Intent(this, PreferencesActivity::class.java)
                startActivityForResult(intent, SHOW_PREFERENCES)
                return true
            }

            id.settings_menu_item -> {
                val intent = Intent(this, PreferencesActivity::class.java)
                startActivityForResult(intent, SHOW_PREFERENCES)
                return true
            }
        }
        return false
    }

    inner class EarthquakeTabsPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> {
                    earthquakeListFragment = EarthquakeListFragment(earthquakeViewModel) {
                        earthquakeViewModel.loadEarthquake()
                    }
                    ProcessLifecycleOwner.get().lifecycle.addObserver(earthquakeListFragment)
                    return earthquakeListFragment
//                        .apply {
//                        arguments =
//                            Bundle().apply { putString("title", getString(R.string.tab_list)) }
//                    }
                }

                1 -> {
                    return EarthquakeMapFragment()
//                        .apply {
//                        arguments =
//                            Bundle().apply { putString("title", getString(R.string.tab_map)) }
//                    }
                }

                else -> throw Exception("프래그먼트가 없습니다.")
            }
        }
    }
}