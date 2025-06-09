package id.co.edtslib.uikit.tablayout

import androidx.annotation.IntDef
import id.co.edtslib.uikit.tablayout.QuadRoundTabLayout.Companion.MODE_AUTO
import id.co.edtslib.uikit.tablayout.QuadRoundTabLayout.Companion.MODE_FIXED
import id.co.edtslib.uikit.tablayout.QuadRoundTabLayout.Companion.MODE_SCROLLABLE

@Retention(AnnotationRetention.SOURCE)
@IntDef(MODE_SCROLLABLE, MODE_FIXED, MODE_AUTO)
annotation class QuadRoundTabLayoutScrollMode()
