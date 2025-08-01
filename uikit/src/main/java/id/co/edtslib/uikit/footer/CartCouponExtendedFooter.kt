package id.co.edtslib.uikit.footer

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.animation.doOnEnd
import androidx.core.content.res.use
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewCartFooterCouponBinding
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.fade
import id.co.edtslib.uikit.utils.inflater

class CartCouponExtendedFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    internal val binding = ViewCartFooterCouponBinding.inflate(this.context.inflater, this, true)

    var delegate: CartCouponExtendedFooterDelegate? = null

    var isExpanded: Boolean = true
        set(value) {
            field = value
            binding.tvInfo.updateLayoutParams<MarginLayoutParams> {
                updateMargins(
                    bottom = if (value) 24.dp.toInt() else 12.dp.toInt()
                )
            }
        }

    var infoText: CharSequence? = null
        set(value) {
            field = value
            binding.tvInfo.text = value
        }

    init {
        initAttrs(attrs)
        bindClickAction()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.CartCouponExtendedFooter).use {
            isExpanded = it.getBoolean(R.styleable.CartCouponExtendedFooter_isExpanded, isExpanded)
            infoText = it.getString(R.styleable.CartCouponExtendedFooter_infoText)
        }
    }

    private fun bindClickAction() {
        binding.root.setOnClickListener {
            delegate?.onFooterClick(it)
        }
    }

    fun hideCouponWithY(shouldAnimate: Boolean = true) {
        if (shouldAnimate) {
            if (!isVisible) return

            val distance = height.toFloat()
            animate().translationY(distance)
                .setDuration(TRANSLATE_DURATION)
                .withEndAction { isVisible = false }
                .start()
        } else {
            isVisible = false
        }
    }

    fun showCouponWithY(shouldAnimate: Boolean = true) {
        if (shouldAnimate) {
            if (isVisible) return

            translationY = height.toFloat()
            isVisible = true

            animate().translationY(0f)
                .setDuration(TRANSLATE_DURATION)
                .start()
        } else {
            isVisible = true
        }
    }


    fun attachToRecyclerView(recyclerView: RecyclerView) {
        var isSticky = true
        var hasUserScrolled = false

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!hasUserScrolled) return

                if (isSticky) {
                    isSticky = false
                    hideCouponWithY()
                }
            }

            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                        hasUserScrolled = true
                        if (isSticky) {
                            isSticky = false
                            hideCouponWithY()
                        }
                    }

                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if (!isSticky) {
                            isSticky = true
                            showCouponWithY()
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