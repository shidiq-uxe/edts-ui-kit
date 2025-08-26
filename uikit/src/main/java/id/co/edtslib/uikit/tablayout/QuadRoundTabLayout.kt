package id.co.edtslib.uikit.tablayout

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.util.Pools
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.badge.Badge
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.deviceWidth
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dp

class QuadRoundTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    private val rootContainer: FrameLayout
    private val contentContainer: LinearLayout
    private val activeIndicatorContainer: QuadRoundTabView
    private var selectedPosition = 0

    private val tabPool = Pools.SynchronizedPool<QuadRoundTabView>(8)
    private val tabs = mutableListOf<QuadRoundTabView>()
    private val tabItems = mutableListOf<TabItem>()

    var textPaddingStart = context.dimen(R.dimen.xxxs)
    var textPaddingEnd = context.dimen(R.dimen.xxxs)
    var tabPaddingStart = context.dimen(R.dimen.s)
    var tabPaddingEnd = context.dimen(R.dimen.s)

    var rootBackgroundColor = context.color(R.color.primary_30)
    var containerBackgroundColor = context.color(R.color.support_selected)

    var tabBackgroundColor = context.color(R.color.support_selected)
    var selectedTabBackgroundColor = context.color(R.color.white)

    var selectedBadgeBackgroundColor: Int = context.color(R.color.primary_30)

    var selectedBadgeTextColor: Int = context.color(R.color.white)

    var defaultTabHeight = context.dimen(R.dimen.m3)
        private set
    var defaultActiveTabHeight = context.dimen(R.dimen.l)
        private set

    lateinit var selectedDrawable: QuadRoundTabBackgroundDrawable
        private set

    var shouldAnimate = false

    var tabTextAppearance = R.style.TextAppearance_Inter_Medium_B4
    var activeTextAppearance = R.style.TextAppearance_Inter_Semibold_H3
    var tabTextColor = context.color(R.color.black_40)
    var selectedTextColor = context.color(R.color.primary_30)

    var badgeSize = context.dimen(R.dimen.s)

    var startIconTint = context.color(R.color.black_40)
    var startIconSize = context.dimen(R.dimen.s)
    var isStartIconVisible = false

    var activeElevation: Float = context.dimen(R.dimen.xxs)
    var shadowColor: Int = context.color(R.color.black_60)

    var badgeTextAppearance = R.style.TextAppearance_Inter_SemiBold_B4

    private var startTabModel = ShapeAppearanceModel().toBuilder()
        .setTopLeftCornerSize(context.dimen(R.dimen.xs))

    private var endTabModel = ShapeAppearanceModel().toBuilder()
        .setTopRightCornerSize(context.dimen(R.dimen.xs))

    private val defaultWidthPercentage = 0.23

    var currentSelectedTab = selectedPosition
        set(value) {
            field = value
            selectTab(value)
        }

    var isUserClickEnabled = true

    @QuadRoundTabLayoutScrollMode
    var scrollMode = MODE_AUTO
        set(@QuadRoundTabLayoutScrollMode value) {
            field = value
            requestLayout()
        }

    var delegate: QuadRoundTabLayoutDelegate? = null

    init {
        overScrollMode = OVER_SCROLL_NEVER
        isFillViewport = true

        // Create root container
        rootContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )

            setBackgroundColor(rootBackgroundColor)
        }

        // Content container for tabs
        contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        activeIndicatorContainer = QuadRoundTabView(context).apply {
            layoutParams = LayoutParams(0,0)
        }

        val shadowPlaceholder = View(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                context.dimen(R.dimen.xxxs).toInt()
            ).apply {
                gravity = Gravity.BOTTOM
                setMargins(0, 0, 0, -context.dimen(R.dimen.m2).toInt())
            }

            setBackgroundColor(context.color(R.color.red))
        }

        // Setup hierarchy
        rootContainer.addView(contentContainer)
        rootContainer.addView(activeIndicatorContainer)
        // Todo : Add Shadow
        // rootContainer.addView(shadowPlaceholder)

        addView(rootContainer)

        // Todo : Use Default Const
        // Initialize drawable
        bindMaterialDrawableElevation()
    }

    fun disableClippingUpToRoot(view: View) {
        var current = view.parent
        while (current is ViewGroup) {
            current.clipChildren = false
            current = current.parent
        }
    }

    private fun bindMaterialDrawableElevation() {
        selectedDrawable = QuadRoundTabBackgroundDrawable(
            width = 0,
            height = defaultActiveTabHeight.toInt(),
            tabColor = selectedTabBackgroundColor
        ).apply {
            // Todo : Trace the performance Later
            this.initializeElevationOverlay(context)
            this.setShadowColor(shadowColor)
            this.elevation = 12.dp
            this.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
        }

        activeIndicatorContainer.background = selectedDrawable
    }

    fun setTabs(tabItems: List<TabItem>) {
        // Recycle existing tabs
        tabs.forEach { releaseTabToPool(it) }
        tabs.clear()
        contentContainer.removeAllViews()

        this@QuadRoundTabLayout.tabItems.addAll(tabItems)

        val startIndex = 0
        val lastIndex = tabItems.lastIndex

        tabItems.forEachIndexed { index, item ->
            val tab = acquireTabFromPool(
                isOnStartIndex = index == startIndex,
                isOnLastIndex = index == lastIndex,
                items = tabItems
            ).apply {
                bind(item)
                setOnClickListener {
                    if (delegate?.onPreventSelected(index, this, item) != true) {
                        selectTab(index, shouldAnimate)
                    }
                }
            }
            tabs.add(tab)
            contentContainer.addView(tab)
        }

        selectedPosition = -1

        this.doOnLayout {
            // Todo : Clipping Workaround
            // disableClippingUpToRoot(activeIndicatorContainer)
            updateTabWidthsForSelection()

            if (tabItems.isNotEmpty()) {
                selectTab(0, animate = false)
            }
        }
    }

    private fun updateTabWidthsForSelection() {
        if (scrollMode == MODE_FIXED && tabs.size == 3) {
            tabs.forEachIndexed { index, tab ->
                (tab.layoutParams as LinearLayout.LayoutParams).apply {
                    if (index == selectedPosition) {
                        width = 150.dp.toInt()
                        weight = 0f
                    } else {
                        width = 0
                        weight = 1f
                    }
                }
            }
            contentContainer.requestLayout()
        }
    }

    fun selectTab(position: Int, animate: Boolean = false, item: TabItem) {
        if (position == selectedPosition) return

        val prevPosition = selectedPosition
        selectedPosition = position.coerceIn(0, tabs.size - 1)

        // Only do width manipulation in scrollable mode
        if (scrollMode == MODE_SCROLLABLE || (scrollMode == MODE_FIXED && tabs.size == 3)) {
            // Reset previous tab to original width
            if (prevPosition != selectedPosition && prevPosition >= 0) {
                resetTabWidth(prevPosition)
            }

            // Expand current tab if needed
            expandTabForFullText(selectedPosition)
        }

        updateActiveIndicator(animate, item)
        smoothScrollToTab(position)

        delegate?.onTabSelected(position, tabs[position], item)
    }

    fun selectTab(position: Int, animate: Boolean = false) {
        if (position == selectedPosition) return

        val prevPosition = selectedPosition
        selectedPosition = position.coerceIn(0, tabs.size - 1)

        if (scrollMode == MODE_SCROLLABLE || (scrollMode == MODE_FIXED && tabs.size == 3)) {
            if (prevPosition != selectedPosition && prevPosition >= 0) {
                resetTabWidth(prevPosition)
            }

            expandTabForFullText(selectedPosition)
        }

        val item = tabItems[position]

        updateActiveIndicator(animate, item)
        smoothScrollToTab(position)

        delegate?.onTabSelected(position, tabs[position], item)
    }

    private fun expandTabForFullText(position: Int) {
        val tab = tabs[position]
        val textWidth = calculateTextWidth(tab.textView.text.toString(), tab.textView.paint)
        val iconWidth = if (tab.iconView.isVisible) tab.iconView.width else 0
        val paddingHorizontal = textPaddingStart + textPaddingEnd + paddingStart + paddingEnd
        // val naturalWidth = textWidth + iconWidth + paddingHorizontal.toInt() + selectedDrawable.containerSubtract().times(2)
        val naturalWidth = 135.dp
        val defaultWidth = (context.deviceWidth * defaultWidthPercentage).toInt()

        // Only expand if natural width is larger than default
        if (naturalWidth > defaultWidth) {
            tab.updateLayoutParams<LinearLayout.LayoutParams> {
                width = naturalWidth.toInt()
            }

            // Request layout to apply changes
            contentContainer.requestLayout()
        }
    }

    private fun resetTabWidth(position: Int) {
        val tab = tabs.getOrNull(position) ?: return
        val defaultWidth = (context.deviceWidth * defaultWidthPercentage).toInt()

        tab.updateLayoutParams<LinearLayout.LayoutParams> {
            width = defaultWidth
        }

        contentContainer.requestLayout()
    }

    private fun calculateTextWidth(text: String, paint: TextPaint): Float {
        return paint.measureText(text)
    }

    private fun updateActiveIndicator(animate: Boolean, item: TabItem) {
        val targetTab = tabs[selectedPosition]

        // Wait for layout to complete if tab was expanded
        targetTab.doOnLayout {
            performIndicatorUpdate(targetTab, animate, item)
        }
    }

    private fun performIndicatorUpdate(targetTab: QuadRoundTabView, animate: Boolean, item: TabItem) {
        // Set Extra bounds for target with size
        val targetWidth = targetTab.width + selectedDrawable.containerSubtract().times(2)
        val targetHeight = selectedDrawable.intrinsicHeight
        val targetX = targetTab.left - selectedDrawable.containerSubtract()

        val selectedBackgroundColor = item.badge?.backgroundColor?.getColorForState(
            intArrayOf(android.R.attr.state_selected),
            item.badge.backgroundColor.defaultColor
        ) ?: selectedBadgeBackgroundColor

        val selectedTextColor = item.badge?.textColor?.getColorForState(
            intArrayOf(android.R.attr.state_selected),
            item.badge.textColor.defaultColor
        ) ?: selectedBadgeTextColor

        activeIndicatorContainer.bind(
            item = TabItem(
                title = item.title,
                iconRes = item.iconRes,
                badge = item.badge?.copy(
                    backgroundColor = ColorStateList.valueOf(selectedBackgroundColor),
                    textColor = ColorStateList.valueOf(selectedTextColor)
                )
            ),
            isSelected = true,
        )

        if (activeIndicatorContainer.width != targetWidth.toInt() || activeIndicatorContainer.height != targetHeight.toInt()) {
            activeIndicatorContainer.apply {
                layoutParams.apply {
                    width = targetWidth.toInt()
                    height = targetHeight.toInt()
                }
            }
            activeIndicatorContainer.requestLayout()
        }

        if (animate) {
            activeIndicatorContainer.animate()
                .translationX(targetX.toFloat())
                .setDuration(250)
                .withLayer()
                .start()
        } else {
            activeIndicatorContainer.translationX = targetX.toFloat()
        }
    }

    private fun acquireTabFromPool(
        isOnStartIndex: Boolean = false,
        isOnLastIndex: Boolean = false,
        items: List<TabItem>,
    ): QuadRoundTabView {
        return tabPool.acquire() ?: QuadRoundTabView(context).apply {
            val isOnFixedMode = scrollMode == MODE_FIXED
            val desiredWidth = if (isOnFixedMode) 0 else LayoutParams.WRAP_CONTENT

            layoutParams = LinearLayout.LayoutParams(
                desiredWidth,
                LayoutParams.MATCH_PARENT
            ).apply {
                weight = if (isOnFixedMode) 1f else 0f
                updateMargins(
                    top = 4.dp.toInt(),
                )
            }


            val startDrawableModel = startTabModel.build()
            val endDrawableModel = endTabModel.build()

            if (items.size == 2) {
                if (isOnStartIndex) {
                    this.background = MaterialShapeDrawable(startDrawableModel).apply {
                        fillColor = ColorStateList.valueOf(tabBackgroundColor)
                    }
                } else if (isOnLastIndex) {
                    this.background = MaterialShapeDrawable(endDrawableModel).apply {
                        fillColor = ColorStateList.valueOf(tabBackgroundColor)
                    }
                } else {
                    setBackgroundColor(tabBackgroundColor)
                }
            } else {
                this.setBackgroundColor(tabBackgroundColor)
            }
        }
    }

    private fun releaseTabToPool(tab: QuadRoundTabView) {
        tab.reset()
        tabPool.release(tab)
    }

    private fun smoothScrollToTab(position: Int) {
        if (!isScrollable()) return

        val targetView = tabs.getOrNull(position) ?: return
        val scrollTo = targetView.left - (width - targetView.width) / 2
        smoothScrollTo(scrollTo.minus(selectedDrawable.containerSubtract().times(2).toInt()), 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (scrollMode) {
            MODE_FIXED -> distributeTabWeights()
            MODE_AUTO -> checkAutoMode(widthMeasureSpec)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    // Todo : Research more on Weight System
    private fun distributeTabWeights() {
        when (tabs.size) {
            2 -> {
                tabs.forEach {
                    (it.layoutParams as LinearLayout.LayoutParams).apply {
                        width = context.deviceWidth.div(2)
                        weight = 0f
                        height = defaultTabHeight.toInt()
                    }
                }
            }
            3 -> {
                // Special case: active = 150dp, others split remaining space
                val remainingWidth = context.deviceWidth - 150.dp.toInt()
                val unselectedWidth = remainingWidth / 2

                tabs.forEachIndexed { index, tab ->
                    (tab.layoutParams as LinearLayout.LayoutParams).apply {
                        width = if (index == selectedPosition) 150.dp.toInt() else unselectedWidth
                        weight = 0f
                        height = defaultTabHeight.toInt()
                    }
                }
            }
            else -> {
                // Fallback - equal distribution (shouldn't happen in FIXED mode)
                tabs.forEach {
                    (it.layoutParams as LinearLayout.LayoutParams).apply {
                        width = context.deviceWidth.div(tabs.size)
                        weight = 0f
                        height = defaultTabHeight.toInt()
                    }
                }
            }
        }
    }

    // have to ensure the tab width is 23% of the screen width to match design
    private fun checkAutoMode(widthMeasureSpec: Int) {
        val defaultWidth = (context.deviceWidth * defaultWidthPercentage).toInt()
        val totalWidth = defaultWidth * tabs.size

        tabs.forEach { tab ->
            tab.updateLayoutParams {
                width = defaultWidth
                height = defaultTabHeight.toInt()
            }
        }

        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        scrollMode = if (totalWidth <= availableWidth && (tabs.size == 2 || tabs.size == 3)) MODE_FIXED else MODE_SCROLLABLE
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollable() && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollable() && super.onInterceptTouchEvent(ev)
    }

    private fun isScrollable() = scrollMode == MODE_SCROLLABLE || scrollMode == MODE_AUTO

    inner class QuadRoundTabView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : LinearLayout(context, attrs, defStyleAttr) {

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        // Todo : Add Icon Size
        internal val iconView = AppCompatImageView(context).apply {
            layoutParams = LayoutParams(
                startIconSize.toInt(),
                startIconSize.toInt()
            ).apply {
                gravity = Gravity.CENTER
                imageTintList = ColorStateList.valueOf(startIconTint)
                isVisible = isStartIconVisible
            }
        }

        internal val textView = AppCompatTextView(context).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END

            setTextAppearance(context, tabTextAppearance)
            setTextColor(tabTextColor)
            layoutParams = MarginLayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                updateMargins(
                    left = textPaddingStart.toInt(),
                    right = textPaddingEnd.toInt(),
                )
            }
        }

        private var badgeView: Badge = Badge(context).apply {
            layoutParams = LayoutParams(
                badgeSize.toInt(),
                badgeSize.toInt()
            )
        }

        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            addView(iconView)
            addView(textView)
            addView(badgeView)

            initAttributes()
        }

        private fun initAttributes() {
            updateTabPadding(false)
            applyOutlineProviderAndElevation()

            clipToPadding = false
        }

        private fun updateTabPadding(isSelected: Boolean) {
            val paddingStart = if (!isSelected) tabPaddingStart.toInt() else tabPaddingStart.plus(selectedDrawable.containerSubtract()).toInt()
            val paddingEnd = if (!isSelected) tabPaddingEnd.toInt() else tabPaddingEnd.plus(selectedDrawable.containerSubtract()).toInt()

            updatePadding(
                left = paddingStart,
                right = paddingEnd
            )
        }

        private fun applyOutlineProviderAndElevation() {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val path = selectedDrawable.getPath()
                    val paddedPath = Path(path)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        outline.setPath(paddedPath)
                    } else {
                        outline.setConvexPath(paddedPath)
                    }
                }
            }

            elevation = this@QuadRoundTabLayout.activeElevation
        }

        fun updateShapeAppearance(shapeAppearanceModel: ShapeAppearanceModel) {

        }

        fun bind(
            item: TabItem,
            isSelected: Boolean = false,
        ) {
            textView.text = item.title
            item.iconRes?.let { iconView.setImageResource(it) }

            updateTabPadding(isSelected)

            item.badge?.let { badge ->
                if (badge.text.isNotEmpty()) {
                    showBadge(badge)
                } else {
                    hideBadge()
                }
            } ?: hideBadge()


            applyOutlineProviderAndElevation()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setTextAppearance(if (isSelected) activeTextAppearance else tabTextAppearance)
            }
            textView.setTextColor(if (isSelected) selectedTextColor else tabTextColor)
        }

        fun showBadge(config: BadgeConfig) {
            badgeView.apply {
                text = config.text
                badgeColor = config.backgroundColor.defaultColor
                textColor = config.textColor.defaultColor
            }

            badgeView.isVisible = true
        }

        fun hideBadge() {
            badgeView.isVisible = false
        }

        fun reset() {
            textView.text = null
            iconView.setImageDrawable(null)
            hideBadge()
        }
    }

    data class TabItem(
        val title: String,
        @DrawableRes val iconRes: Int? = null,
        val badge: BadgeConfig? = null
    )

    data class BadgeConfig(
        val text: String,
        val backgroundColor: ColorStateList,
        val textColor: ColorStateList
    )


    companion object {
        const val MODE_SCROLLABLE = 0
        const val MODE_FIXED = 1
        const val MODE_AUTO = 2

        const val TAB_MODE_WRAP = 11
        const val TAB_MODE_FIXED = 12
    }
}