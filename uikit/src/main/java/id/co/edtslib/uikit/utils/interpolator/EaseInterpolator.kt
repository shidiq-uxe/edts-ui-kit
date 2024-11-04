package id.co.edtslib.uikit.utils.interpolator

import android.view.animation.PathInterpolator

object EaseInterpolator {
    // Ease In Interpolator
    val EaseInQuadInterpolator = PathInterpolator(0.550f, 0.085f, 0.680f, 0.530f)
    val EaseInQubicInterpolator = PathInterpolator(0.550f, 0.055f, 0.675f, 0.190f)
    val EaseInQuartInterpolator = PathInterpolator(0.895f, 0.030f, 0.685f, 0.220f)
    val EaseInQuintInterpolator = PathInterpolator(0.755f, 0.050f, 0.855f, 0.060f)
    val EaseInSineInterpolator = PathInterpolator(0.470f, 0.000f, 0.745f, 0.715f)
    val EaseInExpoInterpolator = PathInterpolator(0.470f, 0.000f, 0.745f, 0.715f)
    val EaseInCircInterpolator = PathInterpolator(0.600f, 0.040f, 0.980f, 0.335f)
    val EaseInBackInterpolator = PathInterpolator(0.600f, -0.280f, 0.735f, 0.045f)

    // Ease Out Interpolator
    val EaseOutQuadInterpolator = PathInterpolator(0.250f, 0.460f, 0.450f, 0.940f)
    val EaseOutQubicInterpolator = PathInterpolator(0.215f, 0.610f, 0.355f, 1.000f)
    val EaseOutQuartInterpolator = PathInterpolator(0.165f, 0.840f, 0.440f, 1.000f)
    val EaseOutQuintInterpolator = PathInterpolator(0.230f, 1.000f, 0.320f, 1.000f)
    val EaseOutSineInterpolator = PathInterpolator(0.390f, 0.575f, 0.565f, 1.000f)
    val EaseOutExpoInterpolator = PathInterpolator(0.190f, 1.000f, 0.220f, 1.000f)
    val EaseOutCircInterpolator = PathInterpolator(0.075f, 0.820f, 0.165f, 1.000f)
    val EaseOutBackInterpolator = PathInterpolator(0.175f, 0.885f, 0.320f, 1.275f)

    // Ease In Out Interpolator
    val EaseInOutQuadInterpolator = PathInterpolator(0.455f, 0.030f, 0.515f, 0.955f)
    val EaseInOutQubicInterpolator = PathInterpolator(0.645f, 0.045f, 0.355f, 1.000f)
    val EaseInOutQuartInterpolator = PathInterpolator(0.770f, 0.000f, 0.175f, 1.000f)
    val EaseInOutQuintInterpolator = PathInterpolator(0.860f, 0.000f, 0.070f, 1.000f)
    val EaseInOutSineInterpolator = PathInterpolator(0.445f, 0.050f, 0.550f, 0.950f)
    val EaseInOutExpoInterpolator = PathInterpolator(1.000f, 0.000f, 0.000f, 1.000f)
    val EaseInOutCircInterpolator = PathInterpolator(0.785f, 0.135f, 0.150f, 0.860f)
    val EaseInOutBackInterpolator = PathInterpolator(0.680f, -0.550f, 0.265f, 1.550f)
}