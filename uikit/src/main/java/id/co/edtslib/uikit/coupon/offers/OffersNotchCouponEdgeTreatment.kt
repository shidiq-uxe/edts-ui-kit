package id.co.edtslib.uikit.coupon.offers

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath
import kotlin.math.max
import kotlin.math.min

class OffersNotchCouponEdgeTreatment(
    private val centerFraction: Float,
    private val notchRadius: Float,
    private val roundedCornerRadius: Float = 0f,
    private val inside: Boolean = true,
    private val side: Side = Side.TOP,
    private val extraOffsetPx: Float = 0f
) : EdgeTreatment() {

    enum class Side { TOP, RIGHT, BOTTOM, LEFT }

    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath
    ) {
        val frac = min(1f, max(0f, centerFraction))
        var c = length * frac

        if (side == Side.LEFT) {
            c = length - c
        }

        c += extraOffsetPx

        if (notchRadius <= 0f || c - notchRadius >= length || c + notchRadius <= 0f) {
            shapePath.lineTo(length, 0f)
            return
        }

        val r = roundedCornerRadius * interpolation

        val left = c - notchRadius
        val right = c + notchRadius

        shapePath.lineTo(max(0f, left - r), 0f)

        if (r > 0f) {
            shapePath.addArc(
                (left - r),
                0f,
                (left + r),
                r * 2f,
                270f,
                90f
            )
        }

        if (inside) {
            shapePath.addArc(
                c - notchRadius,
                -notchRadius,
                c + notchRadius,
                notchRadius,
                180f,
                -180f
            )
        } else {
            shapePath.addArc(
                c - notchRadius,
                -notchRadius,
                c + notchRadius,
                notchRadius,
                180f,
                180f
            )
        }

        if (r > 0f) {
            shapePath.addArc(
                (right - r),
                0f,
                (right + r),
                r * 2f,
                270f,
                90f
            )
        }

        shapePath.lineTo(length, 0f)
    }
}
