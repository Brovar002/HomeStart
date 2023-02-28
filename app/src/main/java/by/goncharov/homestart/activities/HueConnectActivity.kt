package by.goncharov.homestart.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import by.goncharov.homestart.R
import by.goncharov.homestart.custom.CustomJsonArrayRequest
import by.goncharov.homestart.helpers.Devices
import by.goncharov.homestart.helpers.Global
import by.goncharov.homestart.helpers.Theme
import by.goncharov.homestart.helpers.UpdateHandler
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class HueConnectActivity : AppCompatActivity() {

    private val updateHandler = UpdateHandler()
    private var success = false
    private lateinit var queue: RequestQueue
    private lateinit var requestToRegisterUser: CustomJsonArrayRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.setNoActionBar(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hue_connect)

        queue = Volley.newRequestQueue(this)
        val deviceId = intent.getStringExtra("deviceId") ?: ""
        val jsonRequestObject = JSONObject("{\"devicetype\":\"Home App#${android.os.Build.PRODUCT}\"}")
        requestToRegisterUser = CustomJsonArrayRequest(
            Request.Method.POST,
            Devices(this).getDeviceById(deviceId).address + "api",
            jsonRequestObject,
            { response ->
                val responseObject = response.getJSONObject(0)
                if (responseObject.has("success") && !success) {
                    success = true
                    val username = responseObject.getJSONObject("success").getString("username")
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putString(deviceId, username).apply()
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra("device", deviceId)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                    )
                }
            },
            { error ->
                Toast.makeText(this, R.string.err, Toast.LENGTH_LONG).show()
                Log.e(Global.LOG_TAG, error.toString())
            },
        )

        findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        updateHandler.setUpdateFunction {
            if (!success) queue.add(requestToRegisterUser)
        }
    }

    override fun onStop() {
        super.onStop()
        updateHandler.stop()
    }
}
