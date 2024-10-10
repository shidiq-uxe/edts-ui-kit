package id.co.edtslib.uikit.list

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.imageview.ShapeableImageView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ListBinding
import id.co.edtslib.uikit.utils.verticalBias

class ListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ListBinding.inflate(LayoutInflater.from(context), this, true)

    private val cardView = binding.root

    private val titleView: TextView = binding.tvTitle
    private val subtitleView: TextView = binding.tvSubtitle

    private val startIconView: ShapeableImageView = binding.ivStartIcon
    private val endIconView: ShapeableImageView = binding.ivEndIcon

    private var attributeOptions = ListOptions()

    var titleText: String?
        get() = attributeOptions.titleText
        set(value) {
            attributeOptions.titleText = value
            titleView.text = value
            titleView.isVisible = value != null
        }

    var titleTextAppearance: Int
        get() = attributeOptions.titleTextAppearance
        set(value) {
            attributeOptions.titleTextAppearance = value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                titleView.setTextAppearance(value)
            } else {
                titleView.setTextAppearance(context, value)
            }
        }

    var titleTextColor: Int?
        get() = attributeOptions.titleTextColor
        set(value) {
            attributeOptions.titleTextColor = value
            value?.let { titleView.setTextColor(it) }
        }

    var isTitleVisible: Boolean
        get() = attributeOptions.isTitleVisible
        set(value) {
            attributeOptions.isTitleVisible = value
            titleView.isVisible = attributeOptions.isTitleVisible
        }

    var subtitleText: String?
        get() = attributeOptions.subtitleText
        set(value) {
            attributeOptions.subtitleText = value
            subtitleView.text = value
            subtitleView.isVisible = value != null
        }

    var subtitleTextAppearance: Int
        get() = attributeOptions.subtitleTextAppearance
        set(value) {
            attributeOptions.subtitleTextAppearance = value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                subtitleView.setTextAppearance(value)
            } else {
                subtitleView.setTextAppearance(context, value)
            }
        }

    var subtitleTextColor: Int?
        get() = attributeOptions.subtitleTextColor
        set(value) {
            attributeOptions.subtitleTextColor = value
            value?.let { subtitleView.setTextColor(it) }
        }

    var isSubtitleVisible: Boolean
        get() = attributeOptions.isSubtitleVisible
        set(value) {
            attributeOptions.isSubtitleVisible = value
            subtitleView.isVisible = value
        }

    var startIcon: Drawable?
        get() = attributeOptions.startIcon
        set(value) {
            attributeOptions.startIcon = value
            startIconView.setImageDrawable(value)
            startIconView.isVisible = value != null
        }

    var startIconTint: Int?
        get() = attributeOptions.startIconTint
        set(value) {
            attributeOptions.startIconTint = value
            value?.let { startIconView.setColorFilter(it) }
        }

    var isStartIconVisible: Boolean
        get() = attributeOptions.isStartIconVisible
        set(value) {
            attributeOptions.isStartIconVisible = value
            startIconView.isVisible = value
        }

    var startIconSize: Float?
        get() = attributeOptions.startIconSize
        set(value) {
            attributeOptions.startIconSize = value
            value?.let {
                startIconView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = value.toInt()
                    height = value.toInt()
                }
            }
        }

    var startIconGravity: ListOptions.IconGravity
        get() = ListOptions.IconGravity.values()[attributeOptions.startIconGravity]
        set(value) {
            attributeOptions.startIconGravity = value.ordinal

            when(value) {
                ListOptions.IconGravity.TOP -> 0f
                ListOptions.IconGravity.BOTTOM -> 1f
                else -> 0.5f
            }.let {
                startIconView.verticalBias(it)
            }
        }

    var endIcon: Drawable?
        get() = attributeOptions.endIcon
        set(value) {
            attributeOptions.endIcon = value
            endIconView.setImageDrawable(value)
            endIconView.isVisible = value != null
        }

    var endIconTint: Int?
        get() = attributeOptions.endIconTint
        set(value) {
            attributeOptions.endIconTint = value
            value?.let { endIconView.setColorFilter(it) }
        }

    var isEndIconVisible: Boolean
        get() = attributeOptions.isEndIconVisible
        set(value) {
            attributeOptions.isEndIconVisible = value
        }

    var endIconSize: Float?
        get() = attributeOptions.endIconSize
        set(value) {
            attributeOptions.endIconSize = value
            value?.let {
                endIconView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = value.toInt()
                    height = value.toInt()
                }
            }
        }

    var endIconGravity: ListOptions.IconGravity
        get() = ListOptions.IconGravity.values()[attributeOptions.endIconGravity]
        set(value) {
            attributeOptions.endIconGravity = value.ordinal

            when(value) {
                ListOptions.IconGravity.TOP -> 0f
                ListOptions.IconGravity.BOTTOM -> 1f
                else -> 0.5f
            }.let {
                endIconView.verticalBias(it)
            }
        }

    var cornerRadius: Float
        get() = attributeOptions.cornerRadius
        set(value) {
            attributeOptions.cornerRadius = value

            cardView.shapeAppearanceModel = cardView.shapeAppearanceModel.withCornerSize(value)
        }

    var topLeftCornerRadius: Float
        get() = attributeOptions.topLeftCornerRadius
        set(value) {
            attributeOptions.topLeftCornerRadius = value
        }

    var topRightCornerRadius: Float
        get() = attributeOptions.topRightCornerRadius
        set(value) {
            attributeOptions.topRightCornerRadius = value
        }

    var bottomLeftCornerRadius: Float
        get() = attributeOptions.bottomLeftCornerRadius
        set(value) {
            attributeOptions.bottomLeftCornerRadius = value
        }

    var bottomRightCornerRadius: Float
        get() = attributeOptions.bottomRightCornerRadius
        set(value) {
            attributeOptions.bottomRightCornerRadius = value
        }

    var strokeWidth: Float
        get() = attributeOptions.strokeWidth
        set(value) {
            attributeOptions.strokeWidth = value

            cardView.strokeWidth = value.toInt()
        }

    var strokeColor: Int?
        get() = attributeOptions.strokeColor
        set(value) {
            attributeOptions.strokeColor = value
            value?.let {
                cardView.strokeColor = it
            }
        }

    var backgroundTint: Int
        get() = attributeOptions.backgroundTint
        set(value) {
            attributeOptions.backgroundTint = value

            cardView.setCardBackgroundColor(value)
        }

    var cardElevation: Float
        get() = attributeOptions.cardElevation
        set(value) {
            attributeOptions.cardElevation = value

            cardView.elevation = value
        }


    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.ListItem, defStyleAttr, 0).apply {

                ListAttrsFactory.initAttrs(context, attrs, attributeOptions)
                bindViewItem()
            }
        }
    }


    private fun bindViewItem() {
        val startIconBias: Float = when(attributeOptions.startIconGravity) {
            ListOptions.IconGravity.TOP.ordinal -> 0f
            ListOptions.IconGravity.BOTTOM.ordinal -> 1f
            else -> 0.5f
        }

        val endIconBias: Float = when(attributeOptions.endIconGravity) {
            ListOptions.IconGravity.TOP.ordinal -> 0f
            ListOptions.IconGravity.BOTTOM.ordinal -> 1f
            else -> 0.5f
        }

        // Title
        titleView.text = attributeOptions.titleText.also {
            titleView.isVisible = !it.isNullOrEmpty()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleView.setTextAppearance(attributeOptions.titleTextAppearance)
        }
        attributeOptions.titleTextColor?.let { titleView.setTextColor(it) }
        titleView.isVisible = attributeOptions.isTitleVisible

        // Subtitle
        subtitleView.text = attributeOptions.subtitleText.also {
            subtitleView.isVisible = !it.isNullOrEmpty()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            subtitleView.setTextAppearance(attributeOptions.subtitleTextAppearance)
        }
        attributeOptions.subtitleTextColor?.let { subtitleView.setTextColor(it) }
        subtitleView.isVisible = attributeOptions.isSubtitleVisible

        // Start icon
        startIconView.setImageDrawable(attributeOptions.startIcon)
        attributeOptions.startIconTint?.let { startIconView.setColorFilter(it) }
        startIconView.isVisible = attributeOptions.isStartIconVisible || attributeOptions.startIcon != null
        startIconView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            attributeOptions.startIconSize?.let {
                if (it > 0) {
                    width = it.toInt()
                    height = it.toInt()
                }
            }
        }
        startIconView.verticalBias(startIconBias)

        // End icon
        endIconView.setImageDrawable(attributeOptions.endIcon)
        attributeOptions.endIconTint?.let { endIconView.setColorFilter(it) }
        endIconView.isVisible = attributeOptions.isEndIconVisible || attributeOptions.endIcon != null
        endIconView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            attributeOptions.endIconSize?.let {
                if (it > 0f) {
                    width = it.toInt()
                    height = it.toInt()
                }
            }
        }
        endIconView.verticalBias(endIconBias)

        cardView.radius = attributeOptions.cornerRadius
        cardView.strokeWidth = attributeOptions.strokeWidth.toInt()
        cardView.setCardBackgroundColor(attributeOptions.backgroundTint)
        cardView.elevation = attributeOptions.cardElevation
        attributeOptions.strokeColor?.let {
            cardView.strokeColor = it
        }

    }

    override fun isClickable(): Boolean {
        cardView.isClickable = super.isClickable()

        return super.isClickable()
    }
}