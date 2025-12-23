package id.co.edtslib.edtsuikit.helper

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.dp

data class SelectionItem(
    val title: String,
    val description: String,
    val id: String = title
)

class PDPSelectionGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var leftCard: MaterialCardView? = null
    private var rightCard: MaterialCardView? = null

    private var leftItem: SelectionItem? = null
    private var rightItem: SelectionItem? = null

    private var selectedPosition = -1

    private var onSelectionChangedListener: ((SelectionItem, Int) -> Unit)? = null

    private val cardSpacing: Int = resources.getDimensionPixelSize(R.dimen.xs)
    private val cardCornerRadius: Float = resources.getDimension(R.dimen.xxs)
    private val textMargin: Int = resources.getDimensionPixelSize(R.dimen.xxs)

    private val selectedStrokeColor: Int by lazy {
        context.getColor(R.color.primary_30)
    }
    private val unselectedStrokeColor: Int by lazy {
        context.getColor(R.color.black_30)
    }
    private val disabledStrokeColor: Int by lazy {
        context.getColor(R.color.black_20)
    }
    private val selectedTextColor: Int by lazy {
        context.getColor(R.color.primary_30)
    }
    private val unselectedTitleTextColor: Int by lazy {
        context.getColor(R.color.black_70)
    }
    private val unselectedDescriptionTextColor: Int by lazy {
        context.getColor(R.color.black_50)
    }
    private val disabledTextColor: Int by lazy {
        context.getColor(R.color.black_30)
    }

    private val animationDuration = 500L

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    /**
     * Set the two selection items
     */
    fun setItems(
        leftItem: SelectionItem,
        rightItem: SelectionItem,
        defaultSelectedPosition: Int = 0
    ) {
        this.leftItem = leftItem
        this.rightItem = rightItem

        removeAllViews()

        leftCard = createCard(leftItem, 0)
        rightCard = createCard(rightItem, 1)

        addView(leftCard, createCardLayoutParams())
        addView(rightCard, createCardLayoutParams())

        if (defaultSelectedPosition in 0..1) {
            setSelectedPosition(defaultSelectedPosition)
        }
    }

    /**
     * Create layout params for cards (0.5 weight each with spacing)
     */
    private fun createCardLayoutParams(): LayoutParams {
        return LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 0.5f
            if (childCount > 0) {
                marginStart = cardSpacing / 2
            }
        }
    }

    /**
     * Create a single selection card
     */
    private fun createCard(item: SelectionItem, position: Int): MaterialCardView {
        return MaterialCardView(context).apply {
            radius = cardCornerRadius
            strokeWidth = context.dimen(R.dimen.dimen_1).toInt()
            isCheckable = false
            checkedIcon = null
            isClickable = false
            isFocusable = false
            cardElevation = 0f

            setCardBackgroundColor(context.color(R.color.white))
            rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)

            setupCardColorStateLists(this)

            addView(createCardContent(item))

            setOnClickListener {
                if (isEnabled) {
                    handleCardClick(position)
                }
            }
        }
    }

    /**
     * Setup ColorStateLists for card states
     */
    private fun setupCardColorStateLists(card: MaterialCardView) {
        val strokeColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                disabledStrokeColor,
                selectedStrokeColor,
                unselectedStrokeColor
            )
        )

        card.strokeColor = unselectedStrokeColor
    }

    /**
     * Create the content inside each card
     */
    private fun createCardContent(item: SelectionItem): LinearLayout {
        return LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(textMargin, textMargin, textMargin, textMargin)

            val titleTextColorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf()
                ),
                intArrayOf(
                    disabledTextColor,
                    unselectedTitleTextColor
                )
            )

            val descriptionTextColorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf()
                ),
                intArrayOf(
                    disabledTextColor,
                    unselectedDescriptionTextColor
                )
            )

            val titleView = MaterialTextView(context).apply {
                text = item.title
                setTextAppearance(R.style.TextAppearance_Inter_Medium_B3)
                setTextColor(titleTextColorStateList)
                tag = "title"
            }

            val descriptionView = MaterialTextView(context).apply {
                text = item.description
                setTextAppearance(R.style.TextAppearance_Inter_Regular_B3)
                setTextColor(descriptionTextColorStateList)
                tag = "description"
                val marginLayoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = context.dimen(R.dimen.dimen_2).toInt()
                }
                layoutParams = marginLayoutParams
            }

            addView(titleView)
            addView(descriptionView)
        }
    }

    /**
     * Handle card click
     */
    private fun handleCardClick(position: Int) {
        if (selectedPosition == position) return

        setSelectedPosition(position)

        // Notify listener
        val item = when (position) {
            0 -> leftItem
            1 -> rightItem
            else -> null
        }
        item?.let {
            onSelectionChangedListener?.invoke(it, position)
        }
    }

    /**
     * Set the selected position programmatically
     */
    fun setSelectedPosition(position: Int) {
        if (position !in 0..1) return

        when (selectedPosition) {
            0 -> animateCardDeselection(leftCard)
            1 -> animateCardDeselection(rightCard)
        }

        selectedPosition = position
        when (position) {
            0 -> animateCardSelection(leftCard)
            1 -> animateCardSelection(rightCard)
        }
    }

    /**
     * Animate card selection with smooth stroke color and text color transition
     */
    private fun animateCardSelection(card: MaterialCardView?) {
        card?.apply {
            val contentLayout = getChildAt(0) as? LinearLayout
            val titleView = contentLayout?.findViewWithTag<MaterialTextView>("title")
            val descriptionView = contentLayout?.findViewWithTag<MaterialTextView>("description")

            val strokeColorAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                unselectedStrokeColor,
                selectedStrokeColor
            ).apply {
                duration = animationDuration
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    strokeColor = animator.animatedValue as Int
                }
            }

            val textColorAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                unselectedTitleTextColor,
                selectedTextColor
            ).apply {
                duration = animationDuration
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    val color = animator.animatedValue as Int
                    titleView?.setTextColor(color)
                }
            }


            strokeColorAnimator.start()
            textColorAnimator.start()

            isChecked = true
        }
    }

    /**
     * Animate card deselection with smooth stroke color and text color transition
     */
    private fun animateCardDeselection(card: MaterialCardView?) {
        card?.apply {
            val contentLayout = getChildAt(0) as? LinearLayout
            val titleView = contentLayout?.findViewWithTag<MaterialTextView>("title")
            val descriptionView = contentLayout?.findViewWithTag<MaterialTextView>("description")

            val strokeColorAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                selectedStrokeColor,
                unselectedStrokeColor
            ).apply {
                duration = animationDuration
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    strokeColor = animator.animatedValue as Int
                }
            }

            val textColorAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                selectedTextColor,
                unselectedTitleTextColor
            ).apply {
                duration = animationDuration
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    val color = animator.animatedValue as Int
                    titleView?.setTextColor(color)
                }
            }

            strokeColorAnimator.start()
            textColorAnimator.start()

            isChecked = false
        }
    }

    /**
     * Get currently selected item
     */
    fun getSelectedItem(): SelectionItem? {
        return when (selectedPosition) {
            0 -> leftItem
            1 -> rightItem
            else -> null
        }
    }

    /**
     * Get selected position (0 = left, 1 = right)
     */
    fun getSelectedPosition(): Int = selectedPosition

    /**
     * Set selection changed listener
     */
    fun setOnSelectionChangedListener(listener: (SelectionItem, Int) -> Unit) {
        onSelectionChangedListener = listener
    }

    /**
     * Enable or disable the left card
     */
    fun setLeftCardEnabled(enabled: Boolean) {
        leftCard?.isEnabled = enabled

        val contentLayout = leftCard?.getChildAt(0) as? LinearLayout
        val titleView = contentLayout?.findViewWithTag<MaterialTextView>("title")
        val descriptionView = contentLayout?.findViewWithTag<MaterialTextView>("description")

        titleView?.isEnabled = enabled
        descriptionView?.isEnabled = enabled
    }

    /**
     * Enable or disable the right card
     */
    fun setRightCardEnabled(enabled: Boolean) {
        rightCard?.isEnabled = enabled

        val contentLayout = rightCard?.getChildAt(0) as? LinearLayout
        val titleView = contentLayout?.findViewWithTag<MaterialTextView>("title")
        val descriptionView = contentLayout?.findViewWithTag<MaterialTextView>("description")

        titleView?.isEnabled = enabled
        descriptionView?.isEnabled = enabled
    }

    /**
     * Enable or disable both cards
     */
    fun setCardsEnabled(enabled: Boolean) {
        leftCard?.isEnabled = enabled
        rightCard?.isEnabled = enabled


    }

    /**
     * Check if left card is enabled
     */
    fun isLeftCardEnabled(): Boolean = leftCard?.isEnabled ?: false

    /**
     * Check if right card is enabled
     */
    fun isRightCardEnabled(): Boolean = rightCard?.isEnabled ?: false
}