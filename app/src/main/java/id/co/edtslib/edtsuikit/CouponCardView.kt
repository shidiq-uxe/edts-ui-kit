package id.co.edtslib.edtsuikit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath
import android.view.View
import androidx.core.view.doOnLayout
import com.google.android.material.shape.ShapeAppearanceModel
import android.content.res.TypedArray
import android.os.Build
import android.util.TypedValue
import id.co.edtslib.edtsuikit.databinding.ItemPromotionCouponBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import kotlin.math.max
import kotlin.math.min
import id.co.edtslib.uikit.R as UIKitR

class CouponCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    val binding = ItemPromotionCouponBinding.inflate(context.inflater, this, true)

    private var notchRadiusPx: Float = 8.dp
    private var notchRoundedPx: Float = 0f
    private var updateOnLayout: Boolean = true

    private val dividerId = R.id.divider

    init {
        applyPreDefinedCardStyle()


        /*// read custom attrs or use defaults (dp)
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CouponCardView)
        try {
            notchRadiusPx = a.getDimension(R.styleable.CouponCardView_notchRadius, dpToPx(8f))
            notchRoundedPx = a.getDimension(R.styleable.CouponCardView_notchRounded, dpToPx(4f))
            updateOnLayout = a.getBoolean(R.styleable.CouponCardView_updateOnLayout, true)
        } finally {
            a.recycle()
        }*/


        // apply notches once laid out; also observe layout changes for responsiveness
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        post {
            applyNotchesAlignedToDivider()
        }
    }

    // Todo : Trace Re-layout count
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    private fun applyPreDefinedCardStyle() {
        this.strokeWidth = 0
        this.strokeColor = context.color(R.color.white)
        this.cardElevation = 2.dp

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val shadowColor = context.color(UIKitR.color.black_50)
            this.outlineAmbientShadowColor = shadowColor
            this.outlineSpotShadowColor = shadowColor
        }

    }

    private fun applyNotchesAlignedToDivider() {
        val divider = findViewById<View?>(dividerId) ?: return
        val cardH = this.height
        val divH = divider.height
        if (cardH == 0 || divH == 0) return

        // Todo: use getContentPadding() depending on API
        val cardPaddingTop = this.contentPaddingTop
        val dividerCenter = divider.top + divider.height / 2f

        val centerY = dividerCenter + cardPaddingTop


        val cornerRadiusPx = this.radius
        val usable = cardH - (cornerRadiusPx * 2f)

        val fraction = ((centerY - cornerRadiusPx) / usable)
            .coerceIn(0f, 1f)


        val leftEdge = TicketEdgeTreatment(
            centerFraction = fraction,
            notchRadius = notchRadiusPx,
            roundedCornerRadius = notchRoundedPx,
            inside = true,
            side = TicketEdgeTreatment.Side.LEFT
        )
        val rightEdge = TicketEdgeTreatment(
            centerFraction = fraction,
            notchRadius = notchRadiusPx,
            roundedCornerRadius = notchRoundedPx,
            inside = true,
            side = TicketEdgeTreatment.Side.RIGHT
        )

        val shapeModel = ShapeAppearanceModel.Builder()
            .setAllCornerSizes(cornerRadiusPx)
            .setLeftEdge(leftEdge)
            .setRightEdge(rightEdge)
            .build()

        this.shapeAppearanceModel = shapeModel
    }

    fun setNotchRadiusDp(radius: Float) {
        this.notchRadiusPx = radius.dp
        applyNotchesAlignedToDivider()
    }

    fun setNotchRoundedDp(notchRound: Float) {
        this.notchRoundedPx = notchRound.dp
        applyNotchesAlignedToDivider()
    }

    class TicketEdgeTreatment(
        private val centerFraction: Float,
        private val notchRadius: Float,
        private val roundedCornerRadius: Float = 0f,
        private val inside: Boolean = true,
        private val side: Side = Side.TOP,
        private val manualOffsetPx: Float = 0f
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

            c += manualOffsetPx

            if (notchRadius <= 0f || c - notchRadius >= length || c + notchRadius <= 0f) {
                shapePath.lineTo(length, 0f)
                return
            }

            val r = roundedCornerRadius * interpolation
            val left = c - notchRadius
            val right = c + notchRadius

            shapePath.lineTo((left - r).coerceAtLeast(0f), 0f)

            if (r > 0f) {
                shapePath.addArc(left - r, 0f, left + r, r * 2f, 270f, 90f)
            }

            if (inside) {
                shapePath.addArc(c - notchRadius, -notchRadius, c + notchRadius, notchRadius, 180f, -180f)
            } else {
                shapePath.addArc(c - notchRadius, -notchRadius, c + notchRadius, notchRadius, 180f, 180f)
            }

            if (r > 0f) {
                shapePath.addArc(right - r, 0f, right + r, r * 2f, 270f, 90f)
            }

            shapePath.lineTo(length, 0f)
        }
    }
}