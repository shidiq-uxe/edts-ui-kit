package id.co.edtslib.uikit.boarding

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 0.0 means 0% from the screen while 1.0 means 100% from screen
@Parcelize
data class BoardingItemAlignment(
    var horizontalAlignmentPercent: Float = 0f,
    var verticalAlignmentPercent: Float = 0f
): Parcelable