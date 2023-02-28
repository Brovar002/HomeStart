package by.goncharov.homestart.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.goncharov.homestart.BuildConfig
import by.goncharov.homestart.R
import by.goncharov.homestart.helpers.Theme

class AboutActivity : AppCompatActivity() {

    companion object {
        private const val GITHUB_REPOSITORY: String = "Brovar002/HomeStart"
        private const val REPOSITORY_URL: String = "https://github.com/$GITHUB_REPOSITORY"
    }

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

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_about)
            findPreference<Preference>("app_version")?.apply {
                summary = requireContext().getString(
                    R.string.about_app_version_desc,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                )
                setOnPreferenceClickListener {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("$REPOSITORY_URL/releases"),
                        ),
                    )
                    true
                }
            }
            findPreference<Preference>("github")?.apply {
                summary = REPOSITORY_URL
                setOnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY_URL)))
                    true
                }
            }
        }
    }
}
