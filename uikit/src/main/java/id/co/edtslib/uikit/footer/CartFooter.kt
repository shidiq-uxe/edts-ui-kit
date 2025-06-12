package id.co.edtslib.uikit.footer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.withStyledAttributes
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewCartFooterKitBinding
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.setDrawable

open class CartFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    private val binding = ViewCartFooterKitBinding.inflate(this.context.inflater, this, true)

    private val skeletonLoaders = listOf(binding.shimmerTotal, binding.shimmerButton)

    var delegate: CartFooterDelegate? = null

    val extendedFooter = binding.extendedCouponSection

    var infoText: CharSequence? = null
        set(value) {
            field = value
            binding.extendedCouponSection.infoText = value
        }

    var totalText: CharSequence? = null
        set(value) {
            field = value
            binding.tvTotal.text = value
            binding.tvTotal.setDrawable(
                drawableRight = if (value.isNullOrEmpty()) null else context.drawable(R.drawable.ic_chevron_up_16)
            )
        }

    var buttonText: CharSequence? = null
        set(value) {
            field = value
            binding.btnSubmit.text = value
        }

    var isCouponSectionExpanded: Boolean = true
        set(value) {
            field = value
            binding.extendedCouponSection.isExpanded = value
        }

    var isInfoSectionVisible: Boolean = true
        set(value) {
            field = value
            binding.flInfoSection.isVisible = value
            isCouponSectionExpanded = value
        }

    var isLoading: Boolean = false
        set(value) {
            field = value
            binding.btnSubmit.isEnabled = !value
            binding.btnSubmit.text = if (value) null else buttonText
            binding.tvTotal.text = if (value) "" else totalText
            binding.tvTotal.setDrawable(
                drawableRight = if (value) null else context.drawable(R.drawable.ic_chevron_up_16)
            )
            skeletonLoaders.forEach {
                it.isVisible = value
            }
        }

    var cashbackBadgeIcon: Drawable = context.drawable(R.drawable.ill_point_n_stars_24)
        set(value) {
            field = value
            binding.illPointIcon.setImageDrawable(value)

        }

    var cashbackBadgeText: CharSequence? = null
        set(value) {
            field = value
            value?.let {
                binding.tvPromoPointInfo.text = if (isHtml) it.toString().parseAsHtml() else it
            }
        }

    var isHtml: Boolean = false
        set(value) {
            field = value
            binding.tvPromoPointInfo.text = if (value) cashbackBadgeText.toString().parseAsHtml() else cashbackBadgeText
        }

    var isCashbackBadgeVisible: Boolean = false
        set(value) {
            field = value
            binding.flCashbackBadge.isVisible = value
        }

    var isButtonEnabled: Boolean = false
        set(value) {
            field = value
            binding.btnSubmit.isEnabled = value
        }

    init {
        initAttrs(attrs)
        bindClickAction()
    }

    fun initAttrs(attrs: AttributeSet?) {
        this.context.withStyledAttributes(attrs, R.styleable.CartFooter) {
            infoText = getString(R.styleable.CartFooter_infoText)
            buttonText = getString(R.styleable.CartFooter_buttonText)
            isCouponSectionExpanded = getBoolean(R.styleable.CartFooter_isExpanded, isCouponSectionExpanded)
        }
    }

    private fun bindClickAction() {
        binding.btnSubmit.setOnClickListener {
            delegate?.onActionButtonClick()
        }
        binding.extendedCouponSection.setOnClickListener {
            delegate?.onCouponSectionClick()
        }
        binding.tvTotal.setOnClickListener {
            delegate?.onSummaryClick()
        }
    }

    fun attachToRecyclerView(
        recyclerView: RecyclerView,
        includePadding: Boolean = true,
    ) {
        binding.extendedCouponSection.attachToRecyclerView(recyclerView)

        if (includePadding) {
            recyclerView.clipToPadding = false
            doOnLayout {
                recyclerView.updatePadding(bottom = height.plus(recyclerView.paddingBottom))
            }
        }
    }

}