package net.ilsoft.earthquake

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

class PreferencesActivity : AppCompatActivity() {
    companion object {
        const val PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE"
        const val USER_PREFERENCE = "USER_PREFERENCE"
        const val PREF_MIN_MAG = "PREF_MIN_MAG"
        const val PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)
        // 위로가기 버튼
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class PrefFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.userpreferences, null)
        }

    }
}