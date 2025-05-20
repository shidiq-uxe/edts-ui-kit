package id.co.edtslib.uikit.chip

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewDiscountRedemptionCategoryBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.deviceWidth
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater

class DiscountRedemptionChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    // Todo : Next Improvement : Adjustable Size Percent
    private val binding = ViewDiscountRedemptionCategoryBinding.inflate(this.context.inflater, this, true).apply {
        root.updateLayoutParams {
            width = (context.deviceWidth * 0.25).toInt()
        }
    }

    var delegate: DiscountRedemptionChipDelegate? = null

    val chipCard = binding.cardContainer

    var text: String? = null
        set(value) {
            field = value

            binding.tvTitle.text = value.toString()
        }

    var isIndicatorVisible: Boolean = false
        set(value) {
            field = value
            binding.indicator.isVisible = value
        }

    // Todo : Next Improvement
    var cornerRadius: Float = 0f

    @DiscountRedemptionChipStateAnnotation var state: Int = STATE_DEFAULT
        set(value) {
            field = value
            changeState(value)
        }

    init {
        bindClickAction()
    }

    fun changeState(@DiscountRedemptionChipStateAnnotation state: Int) {
        when (state) {
            STATE_CHECKED -> {
                changeColorState(
                    backgroundColor = context.color(R.color.primary_30),
                    textColor = context.color(R.color.white)
                )
            }
            STATE_INACTIVE -> {
                changeColorState(
                    backgroundColor = context.color(R.color.black_30),
                    textColor = context.color(R.color.white)
                )
            }
            STATE_DEFAULT -> {
                changeColorState(
                    backgroundColor = context.color(R.color.support_selected),
                    textColor = context.color(R.color.black_50)
                )
            }
        }
    }

    private fun changeColorState(
        @ColorInt backgroundColor: Int,
        @ColorInt textColor: Int
    ) {
        chipCard.setCardBackgroundColor(backgroundColor)
        binding.tvTitle.setTextColor(textColor)
    }

    private fun bindClickAction() {
        binding.cardContainer.setOnClickListener { view ->
            delegate?.onClick(view, state)
        }
    }

    companion object {
        const val STATE_CHECKED = 2
        const val STATE_INACTIVE = 1
        const val STATE_DEFAULT = 0
    }
}