package id.co.edtslib.uikit.utils

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.provider.Settings

internal fun Context.areSystemAnimationsEnabled(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
