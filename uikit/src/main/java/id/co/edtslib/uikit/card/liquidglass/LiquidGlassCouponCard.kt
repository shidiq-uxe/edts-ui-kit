package id.co.edtslib.uikit.card.liquidglass

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import eightbitlab.com.blurview.BlurTarget
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewLiquidGlassCouponCardBinding
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater

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
        }

    var startIconRes: Int? = R.drawable.ic_coupon_star_24
        set(value) {
            field = value
            value?.let { binding.ivStartIcon.setImageResource(it) }
        }

    var endIconRes: Int? = R.drawable.ic_chevron_right_16
        set(value) {
            field = value
            value?.let { binding.ivEndIcon.setImageResource(it) }
        }

    var isEndIconVisible: Boolean = true
        set(value) {
            field = value
            binding.ivEndIcon.visibility =
                if (value) View.VISIBLE else View.GONE
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
                title = getString(R.styleable.LiquidGlassCouponCard_titleText)
                subtitle = getString(R.styleable.LiquidGlassCouponCard_subTitleText)
                badgeText = getString(R.styleable.LiquidGlassCouponCard_badgeText)

                startIconRes = getResourceId(
                    R.styleable.LiquidGlassCouponCard_startIcon,
                    0
                ).takeIf { it != 0 }

                endIconRes = getResourceId(
                    R.styleable.LiquidGlassCouponCard_endIcon,
                    0
                ).takeIf { it != 0 }

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
        setCardBackgroundColor(Color.TRANSPARENT)
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

        binding.blurView.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setOverlayColor(getResources().getColor(R.color.liquidGlassBackground))
            .setBlurRadius(24f)
    }

    private fun setupBadge() {
        binding.couponCardBadge.apply {
            badgeColor = context.getColor(R.color.red_30)
            includeStroke = true
            badgeDrawable.strokeWidth = resources.getDimension(R.dimen.dimen_1)
            extraPadding(
                left = 2.dp.toInt(),
                top = 1.dp.toInt(),
                right = 2.dp.toInt(),
                bottom = 1.dp.toInt())
            textColor = context.getColor(R.color.white)
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
            strokeWidth = resources.getDimension(R.dimen.dimen_2)
            shader = LinearGradient(
                0f,
                0f,
                widthF,
                0f,
                intArrayOf(
                    ContextCompat.getColor(context, R.color.liquidGlassRim),
                    ContextCompat.getColor(context, R.color.white_transparent),
                    ContextCompat.getColor(context, R.color.white_transparent),
                    ContextCompat.getColor(context, R.color.liquidGlassRim)
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
                delegate?.onCardPressed(this)
                animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(SCALE_DURATION)
                    .start()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                delegate?.onCardReleased(this)
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