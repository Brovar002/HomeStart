package by.goncharov.homestart.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.goncharov.homestart.R
import by.goncharov.homestart.activities.HueSceneActivity
import by.goncharov.homestart.adapters.HueSceneGridAdapter
import by.goncharov.homestart.api.HueAPI
import by.goncharov.homestart.custom.CustomJsonArrayRequest
import by.goncharov.homestart.data.SceneGridItem
import by.goncharov.homestart.helpers.ColorUtils
import by.goncharov.homestart.helpers.Global
import by.goncharov.homestart.helpers.HueUtils
import by.goncharov.homestart.interfaces.HueLampInterface
import by.goncharov.homestart.interfaces.RecyclerViewHelperInterface
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class HueScenesFragment : Fragment(R.layout.fragment_hue_scenes), RecyclerViewHelperInterface {

    companion object {
        var scenesChanged: Boolean = false
        const val SCENES = "/scenes/"
    }

    private var scenesRequest: JsonObjectRequest? = null
    private var selectedScene: CharSequence = ""
    private var selectedSceneName: CharSequence = ""
    private lateinit var c: Context
    private lateinit var lampData: HueLampInterface
    private lateinit var hueAPI: HueAPI
    private lateinit var queue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        c = context ?: throw IllegalStateException()
        lampData = context as HueLampInterface
        hueAPI = HueAPI(c, lampData.device.id)
        queue = Volley.newRequestQueue(context)

        val recyclerView = super.onCreateView(inflater, container, savedInstanceState) as RecyclerView
        val adapter = HueSceneGridAdapter(this, this)
        recyclerView.layoutManager = GridLayoutManager(c, 3)
        recyclerView.adapter = adapter

        scenesRequest =
            JsonObjectRequest(
                Request.Method.GET,
                lampData.addressPrefix + SCENES,
                null,
                { response ->
                    try {
                        val gridItems: ArrayList<SceneGridItem> = ArrayList(response.length())
                        val scenes: ArrayList<Pair<String, String>> = ArrayList(response.length() / 4)
                        var currentObject: JSONObject
                        for (i in response.keys()) {
                            currentObject = response.getJSONObject(i)
                            if (currentObject.optString("group") == lampData.id) {
                                scenes.add(Pair(i, currentObject.getString("name")))
                            }
                        }
                        if (scenes.size > 0) {
                            var completedRequests = 0
                            for (i in 0 until scenes.size) {
                                queue.add(
                                    JsonObjectRequest(
                                        Request.Method.GET,
                                        lampData.addressPrefix + SCENES + scenes[i].first,
                                        null,
                                        { sceneResponse ->
                                            val states = sceneResponse.getJSONObject("lightstates")
                                            val currentSceneValues = ArrayList<Int>(states.length())
                                            var lampObject: JSONObject
                                            for (j in states.keys()) {
                                                lampObject = states.getJSONObject(j)
                                                if (lampObject.getBoolean("on")) {
                                                    if (lampObject.has("hue") && lampObject.has("sat")) {
                                                        currentSceneValues.clear()
                                                        currentSceneValues.add(
                                                            HueUtils.hueSatToRGB(
                                                                lampObject.getInt("hue"),
                                                                lampObject.getInt("sat"),
                                                            ),
                                                        )
                                                    } else if (lampObject.has("xy")) {
                                                        val xyArray = lampObject.getJSONArray("xy")
                                                        currentSceneValues.clear()
                                                        currentSceneValues.add(
                                                            ColorUtils.xyToRGB(
                                                                xyArray.getDouble(0),
                                                                xyArray.getDouble(1),
                                                            ),
                                                        )
                                                    } else if (lampObject.has("ct")) {
                                                        currentSceneValues.add(
                                                            HueUtils.ctToRGB(lampObject.getInt("ct")),
                                                        )
                                                    }
                                                }
                                            }
                                            gridItems += SceneGridItem(
                                                name = scenes[i].second,
                                                hidden = scenes[i].first,
                                                color = if (currentSceneValues.size > 0) currentSceneValues[0] else Color.WHITE,
                                            )
                                            completedRequests++
                                            if (completedRequests == scenes.size) {
                                                val sortedItems =
                                                    gridItems.sortedWith(compareBy { it.color })
                                                        .toMutableList()
                                                sortedItems += SceneGridItem(
                                                    name = resources.getString(R.string.hue_add_scene),
                                                    hidden = "add",
                                                )
                                                adapter.updateData(sortedItems)
                                            }
                                        },
                                        { error ->
                                            Log.e(Global.LOG_TAG, error.toString())
                                        },
                                    ),
                                )
                            }
                        } else {
                            adapter.updateData(
                                mutableListOf(
                                    SceneGridItem(
                                        name = resources.getString(R.string.hue_add_scene),
                                        hidden = "add",
                                    ),
                                ),
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(Global.LOG_TAG, e.toString())
                    }
                },
                { error ->
                    Toast.makeText(c, Global.volleyError(c, error), Toast.LENGTH_LONG).show()
                },
            )
        queue.add(scenesRequest)
        return recyclerView
    }

    override fun onItemClicked(view: View, position: Int) {
        val hiddenText = view.findViewById<TextView>(R.id.hidden).text.toString()
        if (hiddenText == "add") {
            startActivity(
                Intent(c, HueSceneActivity::class.java).putExtra(
                    "deviceId",
                    lampData.device.id,
                ).putExtra("room", lampData.id),
            )
        } else {
            hueAPI.activateSceneOfGroup(lampData.id, hiddenText)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?,
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        selectedScene = v.findViewById<TextView>(R.id.hidden).text
        selectedSceneName = v.findViewById<TextView>(R.id.title).text
        if (selectedScene != "add") MenuInflater(c).inflate(R.menu.activity_hue_lamp_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.title) {
            resources.getString(R.string.str_edit) -> {
                startActivity(
                    Intent(c, HueSceneActivity::class.java)
                        .putExtra("deviceId", lampData.device.id)
                        .putExtra("room", lampData.id)
                        .putExtra("scene", selectedScene),
                )
                true
            }
            resources.getString(R.string.str_delete) -> {
                AlertDialog.Builder(c)
                    .setTitle(R.string.str_delete)
                    .setMessage(R.string.hue_delete_scene)
                    .setPositiveButton(R.string.str_delete) { _, _ ->
                        val deleteSceneRequest = CustomJsonArrayRequest(
                            Request.Method.DELETE,
                            lampData.addressPrefix + SCENES + selectedScene,
                            null,
                            { queue.add(scenesRequest) },
                            { e -> Log.e(Global.LOG_TAG, e.toString()) },
                        )
                        queue.add(deleteSceneRequest)
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .show()
                true
            }
            else -> {
                super.onContextItemSelected(item)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (scenesChanged) {
            scenesChanged = false
            queue.add(scenesRequest)
        }
    }
}
