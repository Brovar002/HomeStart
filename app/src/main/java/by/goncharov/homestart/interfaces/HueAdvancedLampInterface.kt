package by.goncharov.homestart.interfaces

interface HueAdvancedLampInterface : HueLampInterface {
    fun onBrightnessChanged(brightness: Int)
    fun onHueSatChanged(hue: Int, sat: Int)
    fun onCtChanged(ct: Int)
}
