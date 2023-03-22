package by.goncharov.homestart.activities

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.goncharov.homestart.R
import by.goncharov.homestart.adapters.SimpleListAdapter
import by.goncharov.homestart.api.Tasmota
import by.goncharov.homestart.api.UnifiedAPI
import by.goncharov.homestart.data.DeviceItem
import by.goncharov.homestart.data.SimpleListItem
import by.goncharov.homestart.data.UnifiedRequestCallback
import by.goncharov.homestart.helpers.Devices
import by.goncharov.homestart.helpers.Theme
import by.goncharov.homestart.interfaces.HomeRecyclerViewHelperInterface
import by.goncharov.homestart.interfaces.RecyclerViewHelperInterface

class ShortcutTasmotaActivity : AppCompatActivity(), RecyclerViewHelperInterface {

    private var deviceId: String? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        recyclerView = findViewById(R.id.recyclerView)

        val devices = Devices(this)
        val listItems: ArrayList<SimpleListItem> = ArrayList(devices.length)
        var currentDevice: DeviceItem
        for (i in 0 until devices.length) {
            currentDevice = devices.getDeviceByIndex(i)
            if (currentDevice.mode == "Tasmota") {
                listItems += SimpleListItem(
                    title = currentDevice.name,
                    summary = currentDevice.address,
                    hidden = currentDevice.id,
                    icon = currentDevice.iconId,
                )
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SimpleListAdapter(listItems, this)
    }

    override fun onItemClicked(view: View, position: Int) {
        if (deviceId == null) {
            deviceId = view.findViewById<TextView>(R.id.hidden).text.toString()
            Tasmota(this, deviceId ?: throw IllegalStateException(), null).loadList(
                object : UnifiedAPI.CallbackInterface {
                    override fun onItemsLoaded(
                        holder: UnifiedRequestCallback,
                        recyclerViewInterface: HomeRecyclerViewHelperInterface?,
                    ) {
                        if (holder.response != null) {
                            @Suppress("UNCHECKED_CAST")
                            recyclerView.adapter = SimpleListAdapter(
                                holder.response as ArrayList<SimpleListItem>,
                                this@ShortcutTasmotaActivity,
                            )
                        } else {
                            deviceId = null
                            Toast.makeText(
                                this@ShortcutTasmotaActivity,
                                holder.errorMessage,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }

                    override fun onExecuted(result: String, shouldRefresh: Boolean) {
                        Log.d("Shortcut Tasmota Activity", "Empty onExecuted")
                    }
                },
            )
        } else {
            val device = Devices(this).getDeviceById(deviceId ?: throw IllegalStateException())
            val lampName = view.findViewById<TextView>(R.id.title).text
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val shortcutManager = this.getSystemService(ShortcutManager::class.java)
                if (shortcutManager != null) {
                    setResult(
                        RESULT_OK,
                        shortcutManager.createShortcutResultIntent(
                            ShortcutInfo.Builder(this, device.id + lampName)
                                .setShortLabel(lampName)
                                .setLongLabel(lampName)
                                .setIcon(Icon.createWithResource(this, device.iconId))
                                .setIntent(
                                    Intent(this, ShortcutTasmotaActionActivity::class.java)
                                        .putExtra(
                                            "command",
                                            view.findViewById<TextView>(R.id.summary).text,
                                        )
                                        .putExtra("device", device.id)
                                        .setAction(Intent.ACTION_MAIN)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                                )
                                .build(),
                        ),
                    )
                    finish()
                }
            } else {
                Toast.makeText(this, R.string.pref_add_shortcut_failed, Toast.LENGTH_LONG).show()
            }
        }
    }
}
