package by.goncharov.homestart.data

data class UnifiedRequestCallback(
    val response: ArrayList<ListViewItem>?,
    val deviceId: String,
    val errorMessage: String = "",
)
