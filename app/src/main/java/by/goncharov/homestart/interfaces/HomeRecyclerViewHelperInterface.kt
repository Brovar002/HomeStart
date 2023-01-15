package by.goncharov.homestart.interfaces

import android.view.View
import by.goncharov.homestart.data.ListViewItem

interface HomeRecyclerViewHelperInterface {
    fun onItemClicked(view: View, data: ListViewItem)
    fun onStateChanged(view: View, data: ListViewItem, state: Boolean)
}
