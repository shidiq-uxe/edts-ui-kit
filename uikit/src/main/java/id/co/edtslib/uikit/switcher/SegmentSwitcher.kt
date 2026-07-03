package id.co.edtslib.uikit.switcher

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.use
import androidx.core.util.Pools
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.deviceWidth
import id.co.edtslib.uikit.utils.dimen
import kotlin.math.abs
import kotlin.math.roundToInt

class SegmentSwitcher @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var delegate: SegmentSwitcherDelegate? = null

    var shouldAnimate = true

    var isDraggable = false

    var tabWidthMode = TAB_WIDTH_FIXED
        set(value) {
            field = value
            if (tabItems.isNotEmpty()) applyTabLayout()
        }

    var tabWidthFactor = 0.23f
        set(value) {
            field = value
            if (tabItems.isNotEmpty() && tabWidthMode == TAB_WIDTH_FIXED) applyTabLayout()
        }

    var tabViewProvider: ((Context) -> View)? = null

    var activeBackgroundColor: Int = context.color(R.color.primary_40)
        set(value) {
            field = value
            updatePillBackground()
        }

    var activeTextColor: Int = context.color(R.color.white)
        set(value) {
            field = value
            updateAllTabColors()
        }

    var inactiveTitleColor: Int = context.color(R.color.black_60)
        set(value) {
            field = value
            updateAllTabColors()
        }

    var inactiveSubtitleColor: Int = context.color(R.color.black_40)
        set(value) {
            field = value
            updateAllTabColors()
        }

    var inactiveIconColor: Int = context.color(R.color.black_60)
        set(value) {
            field = value
            updateAllTabColors()
        }

    var trackColor: Int = context.color(R.color.black_20)
        set(value) {
            field = value
            (contentFrame.background as? GradientDrawable)?.setColor(value)
        }

    var cornerRadius: Float = context.dimen(R.dimen.xs)
        set(value) {
            field = value
            updateCornerRadii()
        }

    var selectedPosition: Int = -1
        set(value) {
            val clamped = value.coerceIn(0, (tabViews.size - 1).coerceAtLeast(0))
            if (tabViews.isEmpty()) {
                field = -1
                return
            }
            val oldPosition = field
            field = clamped

            tabViews.forEachIndexed { index, tab ->
                tab.isEnabled = index != clamped
            }

            animateToPosition(clamped, oldPosition)

            if (clamped in tabItems.indices) {
                delegate?.onSwitchChangedListener(clamped, tabItems[clamped])
            }

            if (isScrollable) {
                smoothScrollToTab(clamped)
            }
        }

    private val tabViewPool = Pools.SynchronizedPool<SwitcherTabView>(8)
    private val springForcePool = Pools.SynchronizedPool<SpringForce>(4)

    private val scrollView = HorizontalScrollView(context).apply {
        overScrollMode = OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        clipChildren = false
    }

    private val contentFrame = FrameLayout(context).apply {
        clipChildren = false
    }

    private val tabsContainer = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        clipChildren = false
        clipToPadding = false
    }

    private val activePill = View(context).apply {
        id = View.generateViewId()
    }

    private val tabViews = mutableListOf<View>()
    private val tabItems = mutableListOf<TabItem>()

    private var isScrollable = false
    private var isSpringAnimating = false

    private var initialTouchX = 0f
    private var initialTranslationX = 0f
    private var maxTranslationX = 0f
    private var isDragging = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var activePillWidth = 0
    private var activePillHeight = 0

    init {
        clipChildren = false
        initAttrs(attrs)
        setupViews()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SegmentSwitcher, 0, 0).use {
            activeBackgroundColor = it.getColor(R.styleable.SegmentSwitcher_activeBackgroundColor, activeBackgroundColor)
            activeTextColor = it.getColor(R.styleable.SegmentSwitcher_activeTextColor, activeTextColor)
            inactiveTitleColor = it.getColor(R.styleable.SegmentSwitcher_inactiveTitleColor, inactiveTitleColor)
            inactiveSubtitleColor = it.getColor(R.styleable.SegmentSwitcher_inactiveSubtitleColor, inactiveSubtitleColor)
            inactiveIconColor = it.getColor(R.styleable.SegmentSwitcher_inactiveIconColor, inactiveIconColor)
            trackColor = it.getColor(R.styleable.SegmentSwitcher_trackColor, trackColor)
            cornerRadius = it.getDimension(R.styleable.SegmentSwitcher_cornerRadius, cornerRadius)
            isDraggable = it.getBoolean(R.styleable.SegmentSwitcher_isDraggable, isDraggable)
            shouldAnimate = it.getBoolean(R.styleable.SegmentSwitcher_shouldAnimate, shouldAnimate)
            tabWidthMode = it.getInt(R.styleable.SegmentSwitcher_tabWidthMode, tabWidthMode)
        }
    }

    private fun setupViews() {
        // Track background on contentFrame so it covers all tabs (even when scrolling)
        contentFrame.background = GradientDrawable().apply {
            setColor(trackColor)
            cornerRadius = this@SegmentSwitcher.cornerRadius
        }

        activePill.elevation = context.dimen(R.dimen.dimen_2)
        contentFrame.addView(activePill, FrameLayout.LayoutParams(0, 0).apply {
            gravity = Gravity.START or Gravity.TOP
        })

        contentFrame.addView(tabsContainer, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        tabsContainer.elevation = context.dimen(R.dimen.dimen_2)

        scrollView.addView(contentFrame, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        addView(scrollView, LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
    }

    fun setTabs(items: List<TabItem>) {
        setTabsInternal(items)
    }

    private fun setTabsInternal(items: List<TabItem>) {
        tabViews.forEach { releaseTabView(it) }
        tabViews.clear()
        tabsContainer.removeAllViews()
        tabItems.clear()

        if (items.isEmpty()) {
            isScrollable = false
            selectedPosition = -1
            return
        }

        tabItems.addAll(items)
        isScrollable = items.size > 3

        items.forEach { item ->
            val tabView = createTabView(item)
            tabView.setOnClickListener {
                val index = tabViews.indexOf(it)
                if (index >= 0) selectedPosition = index
            }
            tabViews.add(tabView)
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (!isScrollable) {
                lp.width = 0
                lp.weight = 1f
            } else if (tabWidthMode == TAB_WIDTH_FIXED) {
                lp.width = (context.deviceWidth * tabWidthFactor).roundToInt()
            }
            tabsContainer.addView(tabView, lp)
        }

        if (!isScrollable && items.isNotEmpty()) {
            tabsContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            contentFrame.updateLayoutParams<FrameLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }
        } else {
            tabsContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            contentFrame.updateLayoutParams<FrameLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        scrollView.isFillViewport = !isScrollable

        selectedPosition = -1

        contentFrame.doOnLayout {
            updatePillDimensions()
            updateAllTabColors()
            if (tabItems.isNotEmpty()) {
                selectedPosition = 0
            }
        }
    }

    private fun updatePillDimensions() {
        val firstTab = tabViews.firstOrNull() ?: return
        activePillWidth = firstTab.width
        activePillHeight = contentFrame.height

        activePill.updateLayoutParams<FrameLayout.LayoutParams> {
            width = activePillWidth
            height = activePillHeight
        }

        maxTranslationX = if (tabViews.size > 1) {
            tabViews.last().left - tabViews.first().left.toFloat()
        } else {
            0f
        }

        updatePillBackground()
    }

    private fun updatePillBackground() {
        val bgColor = if (selectedPosition in tabItems.indices) {
            resolveColor(tabItems[selectedPosition].activeBackgroundColor ?: activeBackgroundColor)
        } else {
            activeBackgroundColor
        }

        val drawable = GradientDrawable().apply {
            setColor(bgColor)
            cornerRadius = this@SegmentSwitcher.cornerRadius
        }
        activePill.background = drawable
    }

    private fun updateCornerRadii() {
        (contentFrame.background as? GradientDrawable)?.cornerRadius = cornerRadius
        updatePillBackground()
    }

    private fun applyTabLayout() {
        if (tabItems.isEmpty()) return
        val items = tabItems.toList()
        tabViews.forEach { releaseTabView(it) }
        tabViews.clear()
        tabsContainer.removeAllViews()
        tabItems.clear()
        setTabsInternal(items)
    }

    private fun acquireTabFromPool(): SwitcherTabView {
        return tabViewPool.acquire() ?: SwitcherTabView(context)
    }

    private fun releaseTabToPool(tab: SwitcherTabView) {
        tab.reset()
        tab.setOnClickListener(null)
        tabViewPool.release(tab)
    }

    private fun acquireSpringForce(finalPosition: Float): SpringForce {
        val force = springForcePool.acquire() ?: SpringForce(finalPosition)
        force.finalPosition = finalPosition
        force.stiffness = 300f
        force.dampingRatio = 0.75f
        return force
    }

    private fun releaseSpringForce(force: SpringForce) {
        springForcePool.release(force)
    }

    private fun animateToPosition(toPosition: Int, fromPosition: Int) {
        val targetTab = tabViews.getOrNull(toPosition) ?: return
        val targetTranslationX = targetTab.left.toFloat()

        // Update pill width to match target tab (important for wrap mode where widths differ)
        val targetWidth = targetTab.width
        if (activePillWidth != targetWidth) {
            activePillWidth = targetWidth
            activePill.updateLayoutParams<FrameLayout.LayoutParams> {
                width = targetWidth
            }
        }

        val springForce = acquireSpringForce(targetTranslationX)
        val springAnimation = SpringAnimation(activePill, DynamicAnimation.TRANSLATION_X).apply {
            spring = springForce
            addUpdateListener { _, value, _ ->
                updateUIForTranslationX(value)
            }
            addEndListener { _, _, _, _ ->
                isSpringAnimating = false
                releaseSpringForce(springForce)
                if (toPosition in tabItems.indices) {
                    delegate?.onSwitchAnimationEndListener(toPosition, tabItems[toPosition])
                }
            }
        }

        isSpringAnimating = true

        if (!shouldAnimate) {
            activePill.translationX = targetTranslationX
            updateUIForTranslationX(targetTranslationX)
            isSpringAnimating = false
            releaseSpringForce(springForce)
            if (toPosition in tabItems.indices) {
                delegate?.onSwitchAnimationEndListener(toPosition, tabItems[toPosition])
            }
        } else {
            springAnimation.start()
        }
    }

    private fun updateUIForTranslationX(translationX: Float) {
        if (tabViews.size < 2 || maxTranslationX == 0f) return

        val activeBgColorStart: Int
        val activeBgColorEnd: Int
        val fromIndex: Int
        val toIndex: Int
        val localProgress: Float

        val pillCenter = translationX + activePillWidth / 2f

        val bracketIndex = findTabBracket(pillCenter)
        fromIndex = bracketIndex
        toIndex = (bracketIndex + 1).coerceAtMost(tabViews.size - 1)

        if (fromIndex == toIndex) {
            localProgress = 0f
        } else {
            val fromCenter = tabViews[fromIndex].left + tabViews[fromIndex].width / 2f
            val toCenter = tabViews[toIndex].left + tabViews[toIndex].width / 2f
            val span = toCenter - fromCenter
            localProgress = if (span > 0f) {
                ((pillCenter - fromCenter) / span).coerceIn(0f, 1f)
            } else {
                0f
            }
        }

        activeBgColorStart = getActiveBgColor(fromIndex)
        activeBgColorEnd = getActiveBgColor(toIndex)

        val pillBgColor = interpolateColor(activeBgColorStart, activeBgColorEnd, localProgress)
        (activePill.background as? GradientDrawable)?.setColor(pillBgColor)

        tabViews.forEachIndexed { index, v ->
            when (index) {
                fromIndex -> {
                    if (fromIndex == toIndex) {
                        setViewColors(v, activeTextColor, activeTextColor, activeTextColor)
                    } else {
                        val titleC = interpolateColor(activeTextColor, inactiveTitleColor, localProgress)
                        val subtitleC = interpolateColor(activeTextColor, inactiveSubtitleColor, localProgress)
                        val iconC = interpolateColor(activeTextColor, inactiveIconColor, localProgress)
                        setViewColors(v, titleC, subtitleC, iconC)
                    }
                }
                toIndex -> {
                    if (fromIndex != toIndex) {
                        val titleC = interpolateColor(inactiveTitleColor, activeTextColor, localProgress)
                        val subtitleC = interpolateColor(inactiveSubtitleColor, activeTextColor, localProgress)
                        val iconC = interpolateColor(inactiveIconColor, activeTextColor, localProgress)
                        setViewColors(v, titleC, subtitleC, iconC)
                    }
                }
                else -> {
                    setViewColors(v, inactiveTitleColor, inactiveSubtitleColor, inactiveIconColor)
                }
            }
        }
    }

    private fun findTabBracket(pillCenter: Float): Int {
        for (i in 0 until tabViews.size - 1) {
            val tabCenter = tabViews[i].left + tabViews[i].width / 2f
            val nextTabCenter = tabViews[i + 1].left + tabViews[i + 1].width / 2f
            if (pillCenter < (tabCenter + nextTabCenter) / 2f) {
                return i
            }
        }
        return (tabViews.size - 1).coerceAtLeast(0)
    }

    private fun updateAllTabColors() {
        if (tabViews.isEmpty()) return
        val selPos = selectedPosition.coerceIn(0, tabViews.size - 1)
        tabViews.forEachIndexed { index, view ->
            if (index == selPos) {
                setViewColors(view, activeTextColor, activeTextColor, activeTextColor)
            } else {
                setViewColors(view, inactiveTitleColor, inactiveSubtitleColor, inactiveIconColor)
            }
        }
    }

    private fun setViewColors(view: View, title: Int, subtitle: Int, icon: Int) {
        if (view is SwitcherTabView) {
            view.setTextColors(title, subtitle, icon)
            return
        }
        view.findViewById<TextView>(R.id.segment_switcher_tab_title)?.setTextColor(title)
        view.findViewById<TextView>(R.id.segment_switcher_tab_subtitle)?.setTextColor(subtitle)
        view.findViewById<ImageView>(R.id.segment_switcher_tab_icon)?.setColorFilter(icon)
    }

    private fun createTabView(item: TabItem): View {
        return if (tabViewProvider != null) {
            tabViewProvider!!(context).also { bindCustomTabView(it, item) }
        } else {
            acquireTabFromPool().also { it.bind(item) }
        }
    }

    private fun releaseTabView(view: View) {
        if (view is SwitcherTabView) releaseTabToPool(view)
        view.setOnClickListener(null)
    }

    private fun bindCustomTabView(view: View, item: TabItem) {
        val titleTv = view.findViewById<TextView>(R.id.segment_switcher_tab_title)
        val subtitleTv = view.findViewById<TextView>(R.id.segment_switcher_tab_subtitle)
        val iconIv = view.findViewById<ImageView>(R.id.segment_switcher_tab_icon)

        titleTv?.text = item.title
        subtitleTv?.let {
            if (!item.subtitle.isNullOrEmpty()) { it.text = item.subtitle; it.visibility = View.VISIBLE }
            else it.visibility = View.GONE
        }
        iconIv?.let {
            if (item.iconRes != null) { it.setImageResource(item.iconRes); it.visibility = View.VISIBLE }
            else it.visibility = View.GONE
        }
    }

    private fun getActiveBgColor(index: Int): Int {
        return tabItems.getOrNull(index)?.activeBackgroundColor?.let { resolveColor(it) }
            ?: activeBackgroundColor
    }

    private fun getActiveTextColor(index: Int): Int {
        return tabItems.getOrNull(index)?.activeTextColor?.let { resolveColor(it) }
            ?: activeTextColor
    }

    private fun getInactiveTextColor(index: Int): Int {
        return tabItems.getOrNull(index)?.inactiveTextColor?.let { resolveColor(it) }
            ?: inactiveTitleColor
    }

    private fun resolveColor(colorInt: Int): Int {
        return try {
            context.color(colorInt)
        } catch (_: Exception) {
            colorInt
        }
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val fractionClamped = fraction.coerceIn(0f, 1f)
        return ArgbEvaluator().evaluate(fractionClamped, startColor, endColor) as Int
    }

    private fun smoothScrollToTab(position: Int) {
        if (!isScrollable) return
        val targetView = tabViews.getOrNull(position) ?: return
        val scrollTo = targetView.left - (scrollView.width - targetView.width) / 2
        scrollView.smoothScrollTo(scrollTo.coerceAtLeast(0), 0)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isDraggable || isScrollable) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.x
                initialTranslationX = activePill.translationX
                isDragging = false
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging && abs(event.x - initialTouchX) > touchSlop) {
                    isDragging = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
                return false
            }
            else -> return false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDragging || !isDraggable) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - initialTouchX
                val maxX = maxTranslationX.coerceAtLeast(0f)
                val newTranslation = (initialTranslationX + deltaX).coerceIn(0f, maxX)
                activePill.translationX = newTranslation
                updateUIForTranslationX(newTranslation)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent.requestDisallowInterceptTouchEvent(false)

                val pillCenter = activePill.translationX + activePillWidth / 2f
                var closestIndex = 0
                var closestDist = Float.MAX_VALUE
                tabViews.forEachIndexed { index, tab ->
                    val tabCenter = tab.left + tab.width / 2f
                    val dist = abs(pillCenter - tabCenter)
                    if (dist < closestDist) {
                        closestDist = dist
                        closestIndex = index
                    }
                }
                selectedPosition = closestIndex
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    companion object {
        const val TAB_WIDTH_FIXED = 0
        const val TAB_WIDTH_WRAP = 1
    }
}
