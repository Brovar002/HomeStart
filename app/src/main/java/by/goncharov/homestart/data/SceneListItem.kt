package by.goncharov.homestart.data

import by.goncharov.homestart.R

data class SceneListItem(
    var title: String = "",
    var hidden: String = "",
    var icon: Int = R.drawable.ic_circle,
    var state: Boolean = false,
    var brightness: String = "",
    var color: Int = 0,
)
