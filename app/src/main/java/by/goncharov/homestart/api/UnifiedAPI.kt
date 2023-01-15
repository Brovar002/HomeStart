package by.goncharov.homestart.api

import android.content.Context
import android.content.res.Resources
import by.goncharov.homestart.data.ListViewItem
import by.goncharov.homestart.data.UnifiedRequestCallback
import by.goncharov.homestart.helpers.Devices
import by.goncharov.homestart.interfaces.HomeRecyclerViewHelperInterface
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.json.JSONObject

open class UnifiedAPI(
    protected val c: Context,
    val deviceId: String,
    protected val recyclerViewInterface: HomeRecyclerViewHelperInterface?
) {

    companion object {
        private const val LIST_REQUEST_TIMEOUT = 1000
        private val listCache: MutableMap<String, Pair<Long, ArrayList<ListViewItem>>> = mutableMapOf()
    }

    interface CallbackInterface {
        fun onItemsLoaded(
            holder: UnifiedRequestCallback,
            recyclerViewInterface: HomeRecyclerViewHelperInterface?
        )

        fun onExecuted(result: String, shouldRefresh: Boolean = false)
    }

    interface RealTimeStatesCallback {
        fun onStatesLoaded(states: ArrayList<Boolean?>, offset: Int, dynamicSummary: Boolean)
    }

    var dynamicSummaries: Boolean = true
    var needsRealTimeData: Boolean = false

    protected val url: String = Devices(c).getDeviceById(deviceId).address
    protected val queue: RequestQueue = Volley.newRequestQueue(c)

    protected fun updateCache(items: ArrayList<ListViewItem>) {
        listCache[deviceId] = Pair(System.currentTimeMillis(), items)
    }

    open fun loadList(callback: CallbackInterface, extended: Boolean = false) {
        if (System.currentTimeMillis() - (listCache[deviceId]?.first ?: 0) < LIST_REQUEST_TIMEOUT) {
            callback.onItemsLoaded(
                UnifiedRequestCallback(listCache[deviceId]?.second, deviceId, ""),
                recyclerViewInterface
            )
            return
        }
    }

    open fun loadStates(callback: RealTimeStatesCallback, offset: Int) {}
    open fun execute(path: String, callback: CallbackInterface) {}
    open fun changeSwitchState(id: String, state: Boolean) {}

    open class Parser(protected val resources: Resources, protected val api: UnifiedAPI? = null) {
        open fun parseResponse(response: JSONObject): ArrayList<ListViewItem> = arrayListOf()
        open fun parseStates(response: JSONObject): ArrayList<Boolean?> = arrayListOf()
    }
}
