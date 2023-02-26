package by.goncharov.homestart.interfaces

import by.goncharov.homestart.helpers.HueLightListener
import org.json.JSONArray

interface HueRoomInterface : HueLampInterface {
    var lights: JSONArray?
    var lampData: HueLightListener
}
