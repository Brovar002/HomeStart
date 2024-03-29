package by.goncharov.homestart.api

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.preference.PreferenceManager
import by.goncharov.homestart.activities.HueConnectActivity
import by.goncharov.homestart.activities.HueLampActivity
import by.goncharov.homestart.custom.CustomJsonArrayRequest
import by.goncharov.homestart.data.UnifiedRequestCallback
import by.goncharov.homestart.helpers.Global
import by.goncharov.homestart.helpers.Global.volleyError
import by.goncharov.homestart.interfaces.HomeRecyclerViewHelperInterface
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject

class HueAPI(
    c: Context,
    deviceId: String,
    recyclerViewInterface: HomeRecyclerViewHelperInterface? = null,
) : UnifiedAPI(c, deviceId, recyclerViewInterface) {

    private val parser = HueAPIParser(c.resources)
    var readyForRequest: Boolean = true

    init {
        dynamicSummaries = false
        needsRealTimeData = true
    }

    interface RequestCallback {
        fun onLightsLoaded(response: JSONObject?)
    }

    fun getUsername(): String {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(deviceId, "") ?: ""
    }

    // For unified API
    override fun loadList(callback: CallbackInterface, extended: Boolean) {
        super.loadList(callback, extended)
        val jsonObjectRequest =
            JsonObjectRequest(
                Request.Method.GET,
                url + "api/${getUsername()}/groups",
                null,
                { response ->
                    val listItems = parser.parseResponse(response)
                    updateCache(listItems)
                    callback.onItemsLoaded(
                        UnifiedRequestCallback(
                            listItems,
                            deviceId,
                        ),
                        recyclerViewInterface,
                    )
                },
                { error ->
                    callback.onItemsLoaded(
                        UnifiedRequestCallback(
                            null,
                            deviceId,
                            volleyError(c, error),
                        ),
                        null,
                    )
                    if (error is ParseError) {
                        c.startActivity(
                            Intent(
                                c,
                                HueConnectActivity::class.java,
                            ).putExtra("deviceId", deviceId),
                        )
                    }
                },
            )
        queue.add(jsonObjectRequest)
    }

    override fun loadState(callback: RealTimeStatesCallback, offset: Int) {
        if (!readyForRequest) return
        val jsonObjectRequest =
            JsonObjectRequest(
                Request.Method.GET,
                url + "api/${getUsername()}/groups",
                null,
                { response ->
                    callback.onStatesLoaded(parser.parseStates(response), offset, dynamicSummaries)
                },
                { },
            )
        queue.add(jsonObjectRequest)
    }

    override fun execute(path: String, callback: CallbackInterface) {
        c.startActivity(
            Intent(c, HueLampActivity::class.java)
                .putExtra("id", path)
                .putExtra("device", deviceId),
        )
    }

    override fun changeSwitchState(id: String, state: Boolean) {
        switchGroupByID(id, state)
    }

    fun loadLightsByIDs(lightIDs: JSONArray, callback: RequestCallback) {
        val jsonObjectRequest =
            JsonObjectRequest(
                Request.Method.GET,
                url + "api/${getUsername()}/lights",
                null,
                { response ->
                    val returnObject = JSONObject()
                    var lightID: String
                    for (i in 0 until lightIDs.length()) {
                        lightID = lightIDs.getString(i)
                        returnObject.put(lightID, response.getJSONObject(lightID))
                    }
                    callback.onLightsLoaded(returnObject)
                },
                { callback.onLightsLoaded(null) },
            )
        queue.add(jsonObjectRequest)
    }

    fun switchLightByID(lightID: String, on: Boolean) {
        putObject("/lights/$lightID/state", "{\"on\":$on}")
    }

    fun changeBrightness(lightID: String, bri: Int) {
        putObject("/lights/$lightID/state", "{\"bri\":$bri}")
    }

    fun changeColorTemperature(lightID: String, ct: Int) {
        putObject("/lights/$lightID/state", "{\"ct\":$ct}")
    }

    fun changeHue(lightID: String, hue: Int) {
        putObject("/lights/$lightID/state", "{\"hue\":$hue}")
    }

    fun changeSaturation(lightID: String, sat: Int) {
        putObject("/lights/$lightID/state", "{\"sat\":$sat}")
    }

    fun changeHueSat(lightID: String, hue: Int, sat: Int) {
        putObject("/lights/$lightID/state", "{\"hue\":$hue, \"sat\":$sat}")
    }

    fun switchGroupByID(groupID: String, on: Boolean) {
        putObject("/groups/$groupID/action", "{\"on\":$on}")
    }

    fun changeBrightnessOfGroup(groupID: String, bri: Int) {
        putObject("/groups/$groupID/action", "{\"bri\":$bri}")
    }

    fun changeColorTemperatureOfGroup(groupID: String, ct: Int) {
        putObject("/groups/$groupID/action", "{\"ct\":$ct}")
    }

    fun changeHueOfGroup(groupID: String, hue: Int) {
        putObject("/groups/$groupID/action", "{\"hue\":$hue}")
    }

    fun changeSaturationOfGroup(groupID: String, sat: Int) {
        putObject("/groups/$groupID/action", "{\"sat\":$sat}")
    }

    fun changeHueSatOfGroup(groupID: String, hue: Int, sat: Int) {
        putObject("/groups/$groupID/action", "{\"hue\":$hue, \"sat\":$sat}")
    }

    fun activateSceneOfGroup(groupID: String, scene: String) {
        putObject("/groups/$groupID/action", "{\"scene\":$scene}")
    }

    private fun putObject(address: String, requestObject: String) {
        val request = CustomJsonArrayRequest(
            Request.Method.PUT,
            url + "api/${getUsername()}$address",
            JSONObject(requestObject),
            { },
            { e -> Log.e(Global.LOG_TAG, e.toString()) },
        )
        if (readyForRequest) {
            readyForRequest = false
            queue.add(request)
            Handler(Looper.getMainLooper()).postDelayed({ readyForRequest = true }, 100)
        }
    }
}
