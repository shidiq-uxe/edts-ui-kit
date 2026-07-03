package id.co.edtslib.uikit.switcher

data class TabItem(
    val title: String,
    val subtitle: String? = null,
    val iconRes: Int? = null,
    val activeBackgroundColor: Int? = null,
    val activeTextColor: Int? = null,
    val inactiveTextColor: Int? = null
)
