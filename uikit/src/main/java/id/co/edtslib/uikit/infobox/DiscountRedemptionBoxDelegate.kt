package id.co.edtslib.uikit.infobox

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface DiscountRedemptionBoxDelegate {
    fun onInfoBoxClick(view: View)
    fun onScrolled(rv: RecyclerView, dx: Int, dy: Int)
    fun onScrollStateChanged(rv: RecyclerView, newState: Int)
}