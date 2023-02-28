package by.goncharov.homestart.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import by.goncharov.homestart.R
import by.goncharov.homestart.helpers.Devices
import by.goncharov.homestart.helpers.Global
import by.goncharov.homestart.helpers.Pref
import by.goncharov.homestart.helpers.Theme

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, GeneralPreferenceFragment())
            .commit()
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        private val prefsChangedListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == Pref.PREF_THEME) requireActivity().recreate()
            }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(
                prefsChangedListener,
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
                prefsChangedListener,
            )
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            findPreference<Preference>(Pref.PREF_CONTROLS_AUTH)?.isVisible =
                Build.VERSION.SDK_INT >= 33
            findPreference<Preference>("devices")?.setOnPreferenceClickListener {
                startActivity(Intent(context, DevicesActivity::class.java))
                true
            }
            findPreference<Preference>("devices_json")?.setOnPreferenceClickListener {
                Devices.reloadFromPreferences()
                true
            }
            findPreference<Preference>("reset_json")?.setOnPreferenceClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_reset)
                    .setMessage(R.string.pref_reset_question)
                    .setPositiveButton(R.string.str_delete) { _, _ ->
                        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                            .putString("devices_json", Global.DEFAULT_JSON).apply()
                        Toast.makeText(context, R.string.pref_reset_toast, Toast.LENGTH_LONG).show()
                        Devices.reloadFromPreferences()
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .show()
                true
            }
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }
    }
}
