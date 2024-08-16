package id.co.edtslib.uikit.indicator

import androidx.annotation.IntDef
import id.co.edtslib.uikit.indicator.IndicatorSlideMode.Companion.NORMAL
import id.co.edtslib.uikit.indicator.IndicatorSlideMode.Companion.SMOOTH
import id.co.edtslib.uikit.indicator.IndicatorSlideMode.Companion.WORM
import id.co.edtslib.uikit.indicator.IndicatorSlideMode.Companion.COLOR
import id.co.edtslib.uikit.indicator.IndicatorSlideMode.Companion.SCALE

@IntDef(NORMAL, SMOOTH, WORM, COLOR, SCALE)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
annotation class IndicatorSlideModeAnnotation