package id.co.edtslib.uikit.infobox

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.use
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewDiscountRedemptionBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater

class DiscountRedemptionBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    private val binding = ViewDiscountRedemptionBinding.inflate(this.context.inflater, this, true)

    var delegate: DiscountRedemptionBoxDelegate? = null

    var titleText: CharSequence? = null
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var infoText: CharSequence? = null
        set(value) {
            field = value
            binding.chipInfo.text = if (isHtml) value.toString().parseAsHtml() else value
        }

    var boxBackgroundColor: Int = this.context.color(R.color.primary_30)
        set(value) {
            field = value
            binding.root.setCardBackgroundColor(ColorStateList.valueOf(value))
        }

    var infoBackgroundColor: Int = this.context.color(R.color.primary_10)
        set(value) {
            field = value
            binding.chipInfo.chipBackgroundColor = ColorStateList.valueOf(value)
        }

    var isExpanded: Boolean = true
        set(value) {
            field = value
            adjustExpandedState(value)

            if (!value) {
                this.doOnLayout { boxHeight = this.height }
            }
        }

    var isHtml: Boolean = false
        set(value) {
            field = value
            binding.chipInfo.text = if (value) infoText.toString().parseAsHtml() else infoText
        }

    private var shimmerFrameLayout: ShimmerFrameLayout? = null

    var isLoading: Boolean = false
        set(value) {
            field = value
            binding.chipInfo.apply {
                binding.skeletonShimmerChip.isVisible = value
                binding.iconButton.isVisible = !value

                if (value) {
                    this.text = null
                } else {
                    this.text = infoText
                }
            }
        }

    private var boxHeight = 0

    init {
        initAttrs(attrs)
        bindClickAction()
        adjustChipTextPadding()
    }

    fun initAttrs(attrs: AttributeSet?) {
        if (attrs != null) {
            this.context.obtainStyledAttributes(attrs, R.styleable.DiscountRedemptionBox).use {
                titleText = it.getString(R.styleable.DiscountRedemptionBox_titleText)
                infoText = it.getString(R.styleable.DiscountRedemptionBox_infoText)
                isExpanded = it.getBoolean(R.styleable.DiscountRedemptionBox_isExpanded, isExpanded)
                isHtml = it.getBoolean(R.styleable.DiscountRedemptionBox_isHtml, isHtml)
            }
        }
    }

    private fun bindClickAction() {
        binding.chipInfo.setOnClickListener {
            delegate?.onInfoBoxClick(it)
        }
    }

    private fun adjustChipTextPadding() {
        binding.iconButton.doOnLayout {
            binding.chipInfo.textEndPadding = it.width.plus(it.marginEnd).plus(it.marginStart).toFloat()
        }
    }

    private fun adjustExpandedState(isExpanded: Boolean) {
        binding.root.updateCornerRadius(isExpanded)

        binding.ivDecorativeIllustration.updateLayoutParams<MarginLayoutParams> {
            this.updateMargins(
                right = if (isExpanded) 0 else 10.dp.toInt(),
            )
        }

        binding.chipInfo.updateLayoutParams<MarginLayoutParams> {
            this.updateMargins(
                bottom = if (isExpanded) 12.dp.toInt() else 8.dp.toInt()
            )
        }

        binding.tvTitle.isVisible = isExpanded
    }

    private fun MaterialCardView.updateCornerRadius(isExpanded: Boolean) {
        this.shapeAppearanceModel = this.shapeAppearanceModel.toBuilder().apply {
            if (isExpanded) {
                setAllCornerSizes(8.dp)
            } else {
                setTopLeftCornerSize(0.dp)
                setTopRightCornerSize(0.dp)
            }
        }.build()
    }

    fun hideOnShrinkState(shouldAnimate: Boolean = true) {
        this.doOnLayout { boxHeight = this.height }
        if (shouldAnimate) {
            this.animate()
                .translationY(-boxHeight.toFloat())
                .setDuration(TRANSLATE_DURATION)
                .withEndAction {
                    this.isVisible = false
                }
                .start()
        } else {
            isVisible = false
        }
    }

    fun showOnShrinkState(shouldAnimate: Boolean = true) {
        this.doOnLayout { boxHeight = this.height }
        if (shouldAnimate) {
            this.animate()
                .translationY(0f)
                .setDuration(TRANSLATE_DURATION)
                .withStartAction {
                    this.translationY = -boxHeight.toFloat()
                    this.isVisible = true
                }
                .start()
        } else {
            isVisible = true
        }
    }

    fun attachToRecyclerView(
        recyclerView: RecyclerView,
        targetAdapterPosition: Int,
    ) {
        var isSticky = false
        var shouldStick = false

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val viewHolder = rv.findViewHolderForAdapterPosition(targetAdapterPosition)
                val bottomOfTheBox = viewHolder?.itemView?.bottom ?: 0

                shouldStick = bottomOfTheBox <= rv.top

                if (!shouldStick && isSticky) {
                    isSticky = false
                    hideOnShrinkState()
                }
            }

            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if (shouldStick && !isSticky) {
                            isSticky = true
                            showOnShrinkState()
                        }
                    }

                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                        if (isSticky) {
                            isSticky = false
                            hideOnShrinkState()
                        }
                    }
                }
            }
        }

        recyclerView.addOnScrollListener(scrollListener)
    }

    companion object {
        private const val TRANSLATE_DURATION = 200L
    }
}