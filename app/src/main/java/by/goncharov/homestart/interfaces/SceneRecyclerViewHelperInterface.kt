package by.goncharov.homestart.interfaces

import android.view.View
import by.goncharov.homestart.data.SceneListItem

interface SceneRecyclerViewHelperInterface {
    fun onItemClicked(view: View, data: SceneListItem)
    fun onStateChanged(view: View, data: SceneListItem, state: Boolean)
}
