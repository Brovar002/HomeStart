package by.goncharov.homestart.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.goncharov.homestart.api.Tasmota
import by.goncharov.homestart.api.UnifiedAPI
import by.goncharov.homestart.data.UnifiedRequestCallback
import by.goncharov.homestart.interfaces.HomeRecyclerViewHelperInterface

class ShortcutTasmotaActionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra("command") && intent.hasExtra("device")) {
            Tasmota(
                this,
                intent.getStringExtra("device") ?: throw IllegalStateException(),
                null,
            ).execute(
                intent.getStringExtra("command") ?: throw IllegalStateException(),
                object : UnifiedAPI.CallbackInterface {
                    override fun onItemsLoaded(
                        holder: UnifiedRequestCallback,
                        recyclerViewInterface: HomeRecyclerViewHelperInterface?,
                    ) {
                    }

                    override fun onExecuted(result: String, shouldRefresh: Boolean) {
                        Toast.makeText(
                            this@ShortcutTasmotaActionActivity,
                            result,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                },
            )
        }
        finish()
    }
}
