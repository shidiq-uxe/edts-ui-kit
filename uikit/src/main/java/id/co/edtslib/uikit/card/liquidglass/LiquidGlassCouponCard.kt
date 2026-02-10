package id.co.edtslib.uikit.card.liquidglass

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import eightbitlab.com.blurview.BlurTarget
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewLiquidGlassCouponCardBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import kotlin.text.isNullOrEmpty

class LiquidGlassCouponCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    private val binding = ViewLiquidGlassCouponCardBinding.inflate(this.context.inflater, this, true)

    var delegate: LiquidGlassCouponCardDelegate? = null

    var title: String? = "Kupon Saya"
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var subtitle: String? = "Kumpulkan kupon yang kamu punya"
        set(value) {
            field = value
            binding.tvSubtitle.text = value
        }

    var badgeText: String? = null
        set(value) {
            field = value
            binding.couponCardBadge.text = value
            binding.couponCardBadge.isVisible = isBadgeVisible && !value.isNullOrEmpty()

        }

    var isBadgeVisible: Boolean = true
        set(value) {
            field = value
            binding.couponCardBadge.isVisible = value && !badgeText.isNullOrEmpty()
        }

    var startIconRes: Int? = 0
        set(value) {
            field = value
            value?.let { binding.ivStartIcon.setImageResource(it) }
        }

    var endIconRes: Int? = 0
        set(value) {
            field = value
            value?.let { binding.ivEndIcon.setImageResource(it) }
        }

    var isEndIconVisible: Boolean = true
        set(value) {
            field = value
            binding.ivEndIcon.isVisible = value
        }

    init {
        initAttrs(attrs)
        setWillNotDraw(false)
        setupMaterialCard()
        setupBadge()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.LiquidGlassCouponCard).apply {
                title = getString(R.styleable.LiquidGlassCouponCard_titleText) ?: title
                subtitle = getString(R.styleable.LiquidGlassCouponCard_subTitleText) ?: subtitle
                badgeText = getString(R.styleable.LiquidGlassCouponCard_badgeText)

                isBadgeVisible = getBoolean(
                    R.styleable.LiquidGlassCouponCard_badgeVisible,
                    true
                )

                startIconRes = getResourceId(
                    R.styleable.LiquidGlassCouponCard_startIcon,
                    R.drawable.ic_coupon_star_24
                )

                endIconRes = getResourceId(
                    R.styleable.LiquidGlassCouponCard_endIcon,
                    R.drawable.ic_chevron_right_16
                )

                isEndIconVisible = getBoolean(
                    R.styleable.LiquidGlassCouponCard_endIconVisible,
                    true
                )

                recycle()
            }
        }
    }

    private fun setupMaterialCard() {
        radius = 8f.dp
        setCardBackgroundColor(context.color(R.color.transparent))
        cardElevation = 0f.dp

        isClickable = true
        isFocusable = true

        binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blurView.clipToOutline = true
    }

    fun setupBlur(
        activity: Activity,
        target: BlurTarget
    ) {
        val decorView = activity.window.decorView
        val windowBackground = decorView.background

        binding.blurView.setupWith(target, 1f, false)
            .setFrameClearDrawable(windowBackground)
            .setOverlayColor(context.color(R.color.liquidGlassBackground))
            .setBlurRadius(30f)
    }

    private fun setupBadge() {
        binding.couponCardBadge.apply {
            badgeColor = context.color(R.color.red_30)
            includeStroke = true
            badgeDrawable.strokeWidth = context.dimen(R.dimen.dimen_1)
            setExtraPadding(
                left = 2.dp.toInt(),
                top = 1.dp.toInt(),
                right = 2.dp.toInt(),
                bottom = 1.dp.toInt())
            textColor = context.color(R.color.white)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val cardRadius = radius

        val path = Path().apply {
            addRoundRect(
                RectF(
                    0f,
                    0f,
                    widthF,
                    heightF
                ),
                cardRadius,
                cardRadius,
                Path.Direction.CW
            )
        }

        // --- RIM EDGE: VERTICAL GLOSS ---
        val sideGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.dimen(R.dimen.dimen_2)
            shader = LinearGradient(
                0f,
                0f,
                widthF,
                0f,
                intArrayOf(
                    context.color(R.color.liquidGlassRim),
                    context.color(R.color.transparent),
                    context.color(R.color.transparent),
                    context.color(R.color.liquidGlassRim)
                ),
                floatArrayOf(
                    0f,
                    0.01f,
                    0.99f,
                    1f
                ),
                Shader.TileMode.CLAMP
            )
        }

        canvas.drawPath(path, sideGlowPaint)
        canvas.save()
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(SCALE_DURATION)
                    .start()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(SCALE_DURATION)
                    .start()

                if (event.action == MotionEvent.ACTION_UP) {
                    delegate?.onCardClicked(this)
                    performClick()
                }
            }
        }
        return true
    }

    companion object {
        private const val SCALE_DURATION = 100L
    }
}