package by.goncharov.homestart.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.goncharov.homestart.R
import by.goncharov.homestart.adapters.HueLampListAdapter
import by.goncharov.homestart.api.HueAPI
import by.goncharov.homestart.data.DeviceItem
import by.goncharov.homestart.data.ListViewItem
import by.goncharov.homestart.helpers.HueUtils
import by.goncharov.homestart.helpers.UpdateHandler
import by.goncharov.homestart.interfaces.HueAdvancedLampInterface
import by.goncharov.homestart.interfaces.HueRoomInterface
import by.goncharov.homestart.interfaces.RecyclerViewHelperInterface
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class HueLampsFragment :
    Fragment(R.layout.fragment_hue_lamps),
    RecyclerViewHelperInterface,
    HueAdvancedLampInterface {

    companion object {
        const val TAG: String = "Hue Lamp Fragment"
    }

    private lateinit var c: Context
    private lateinit var lampData: HueRoomInterface
    private lateinit var hueAPI: HueAPI
    private lateinit var queue: RequestQueue
    private lateinit var requestCallBack: HueAPI.RequestCallback
    private val updateHandler: UpdateHandler = UpdateHandler()

    override var id: String = ""
    override var canReceiveRequest: Boolean = true
    override lateinit var device: DeviceItem
    override lateinit var addressPrefix: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        c = context ?: throw IllegalStateException()
        lampData = context as HueRoomInterface
        hueAPI = HueAPI(c, lampData.device.id)
        queue = Volley.newRequestQueue(context)

        device = lampData.device
        addressPrefix = lampData.addressPrefix

        val recyclerView = super.onCreateView(inflater, container, savedInstanceState) as RecyclerView
        val hueLampStateListener = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isPressed) {
                hueAPI.switchLightByID(
                    (compoundButton.parent as ViewGroup).findViewById<TextView>(R.id.hidden).text.toString(),
                    b,
                )
            }
        }

        val adapter = HueLampListAdapter(hueLampStateListener, this@HueLampsFragment)
        recyclerView.layoutManager = LinearLayoutManager(c)
        recyclerView.adapter = adapter

        requestCallBack = object : HueAPI.RequestCallback {
            override fun onLightsLoaded(response: JSONObject?) {
                if (response != null) {
                    var currentObject: JSONObject
                    var currentState: JSONObject
                    val listItems: ArrayList<ListViewItem> = arrayListOf()
                    val colorArray: ArrayList<Int> = arrayListOf()
                    for (i in response.keys()) {
                        currentObject = response.optJSONObject(i) ?: JSONObject()
                        currentState = currentObject.optJSONObject("state") ?: JSONObject()
                        colorArray += if (currentState.has("hue") && currentState.has("sat")) {
                            HueUtils.hueSatToRGB(
                                currentState.getInt("hue"),
                                currentState.getInt("sat"),
                            )
                        } else if (currentState.has("ct")) {
                            HueUtils.ctToRGB(currentState.getInt("ct"))
                        } else {
                            Color.parseColor("#FFFFFF")
                        }
                        listItems += ListViewItem(
                            title = currentObject.optString("name"),
                            summary =
                            if (currentState.optBoolean("reachable")) {
                                resources.getString(R.string.hue_tap)
                            } else {
                                resources.getString(R.string.str_unreachable)
                            },
                            hidden = i,
                            state = currentState.optBoolean("on"),
                        )
                    }
                    adapter.updateData(recyclerView, listItems, colorArray)
                }
            }
        }

        return recyclerView
    }

    override fun onStart() {
        super.onStart()
        updateHandler.setUpdateFunction {
            if (lampData.canReceiveRequest && hueAPI.readyForRequest) {
                hueAPI.loadLightsByIDs(lampData.lights ?: JSONArray(), requestCallBack)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        updateHandler.stop()
    }

    override fun onItemClicked(view: View, position: Int) {
        id = view.findViewById<TextView>(R.id.hidden).text.toString()
        HueColorSheet(this).show(
            (c as AppCompatActivity).supportFragmentManager,
            HueColorSheet::class.simpleName,
        )
    }

    override fun onColorChanged(color: Int) {
        Log.d(TAG, "Empty onColorChanged")
    }
    override fun onBrightnessChanged(brightness: Int) {
        Log.d(TAG, "Empty onBrightnessChanged")
    }
    override fun onHueSatChanged(hue: Int, sat: Int) {
        Log.d(TAG, "Empty onHueSatChanged")
    }
    override fun onCtChanged(ct: Int) {
        Log.d(TAG, "Empty onCtChanged")
    }
}
