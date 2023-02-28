package by.goncharov.homestart.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.goncharov.homestart.api.HueAPI

class ShortcutHueSceneActionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra("scene") && intent.hasExtra("group") && intent.hasExtra("device")) {
            HueAPI(
                this,
                intent.getStringExtra("device") ?: throw IllegalStateException(),
                null,
            ).activateSceneOfGroup(
                intent.getStringExtra("group") ?: throw IllegalStateException(),
                intent.getStringExtra("scene") ?: throw IllegalStateException(),
            )
        }
        finish()
    }
}
