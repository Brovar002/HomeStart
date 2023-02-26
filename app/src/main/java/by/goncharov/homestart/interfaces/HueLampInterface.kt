package by.goncharov.homestart.interfaces

import by.goncharov.homestart.data.DeviceItem

interface HueLampInterface {
    var id: String
    var device: DeviceItem
    var addressPrefix: String
    var canReceiveRequest: Boolean

    fun onColorChanged(color: Int)
}
