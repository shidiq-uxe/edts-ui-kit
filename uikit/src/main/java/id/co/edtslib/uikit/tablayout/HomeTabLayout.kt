package id.co.edtslib.uikit.tablayout

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.google.android.material.button.MaterialButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.res.use
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.ribbon.Ribbon
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.R as UIKitR
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.databinding.HomepageTabLayoutBinding as HomeTabLayoutBinding
import id.co.edtslib.uikit.utils.interpolator.EaseInterpolator

class HomeTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = HomeTabLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private val shapeModels = mutableMapOf<MaterialButton, ShapeAppearanceModel>()

    private val tab1: MaterialButton = binding.tab1
    private val tab2: MaterialButton = binding.tab2
    private val tab3: MaterialButton = binding.tab3

    private val tabs: List<MaterialButton> = listOf(tab1, tab2, tab3)

    private val leftEdges = binding.leftEdges
    private val rightEdges = binding.rightEdges

    private val activeIndicator = binding.ivActiveTab

    private var shapeBuilder = ShapeAppearanceModel.builder()

    var shouldAnimate = true

    var selectedTab = HomeTab.Grocery
        set(value) {
            field = value

            when (value) {
                HomeTab.Grocery -> updateActiveTab(tab1)
                HomeTab.Food -> updateActiveTab(tab2)
                HomeTab.Virtual -> updateActiveTab(tab3)
            }
        }

    var delegate: HomeTabLayoutDelegate? = null

    var titles = listOf(HomeTab.Grocery.toString(), HomeTab.Food.toString(), HomeTab.Virtual.toString())
        set(value) {
            field = value
            tab1.text = value[0]
            tab2.text = value[1]
            tab3.text = value[2]
        }

    var icons = listOf(HomeTab.Grocery.icon, HomeTab.Food.icon, HomeTab.Virtual.icon)
        set(value) {
            field = value
            tab1.icon = context.drawable(value.first())
            tab2.icon = context.drawable(value[1])
            tab3.icon = context.drawable(value[2])
        }

    var selectedColor = context.color(UIKitR.color.primary_30)
        set(value) {
            field = value
            currentSelectedTab?.apply {
                iconTint = ColorStateList.valueOf(value)
                setTextColor(value)
            }
        }

    var unselectedColor = context.color(UIKitR.color.white)
        set(value) {
            field = value
            tabs.forEach {
                it.iconTint = ColorStateList.valueOf(value)
                it.setTextColor(value)
            }
        }

    private var currentSelectedTab: MaterialButton? = null

    init {
        this.background = ColorDrawable(context.color(UIKitR.color.black_10))
        initAttrs(attrs)
        enableHardwareAcceleration()
        setupListeners()

        updateActiveTab(tab1)
        currentSelectedTab = binding.tab1

        leftEdges.alpha = 0f
    }

    fun enableHardwareAcceleration() {
        this.setLayerType(LAYER_TYPE_HARDWARE, null)
        binding.tab1.setLayerType(LAYER_TYPE_HARDWARE, null)
        binding.tab2.setLayerType(LAYER_TYPE_HARDWARE, null)
        binding.tab3.setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, UIKitR.styleable.HomeTabLayout, 0, 0).use {
            selectedColor = it.getColor(UIKitR.styleable.HomeTabLayout_selectedItemColor, context.color(UIKitR.color.primary_30))
            unselectedColor = it.getColor(UIKitR.styleable.HomeTabLayout_unselectedItemColor, context.color(UIKitR.color.white))
            shouldAnimate = it.getBoolean(UIKitR.styleable.HomeTabLayout_shouldAnimate, shouldAnimate)
        }
    }

    private fun setupListeners() {
        tab1.setOnClickListener { selectedTab = HomeTab.Grocery }
        tab2.setOnClickListener { selectedTab = HomeTab.Food }
        tab3.setOnClickListener { selectedTab = HomeTab.Virtual }
    }

    fun updateActiveTab(selectedTab: MaterialButton) {
        when (selectedTab) {
            tab1 -> activateTab(tab1, HomeTab.Grocery)
            tab2 -> activateTab(tab2, HomeTab.Food)
            tab3 -> activateTab(tab3, HomeTab.Virtual)
        }
    }

    private fun activateTab(selectedTab: MaterialButton, tab: HomeTab) {
        if (selectedTab == currentSelectedTab) return

        val previousTab = currentSelectedTab
        currentSelectedTab = selectedTab

        delegate?.onTabSelected(tab)
        updateActiveTabWidth(selectedTab)

        if (shouldAnimate) {
            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 450
                addUpdateListener { animator ->
                    val progress = animator.animatedFraction

                    updateTabContentColors(previousTab, selectedTab, progress)
                    repositionActiveButton(selectedTab, progress)
                    updateEdgesWithAnimation(selectedTab, progress)
                }
            }
            animator.start()
        } else {
            updateTabContentColors(previousTab, selectedTab, 1f)
            updateEdgesWithAnimation(selectedTab, 1f)
            doOnLayout {
                repositionActiveButton(selectedTab, 1f)
            }
        }
    }

    private fun updateActiveTabWidth(selectedTab: MaterialButton) {
        selectedTab.doOnLayout {
            val activeWidth = selectedTab.width + 24.dp

            val tabCachedShape = TabBackgroundDrawable(activeWidth.toInt(), selectedTab.height, context.color(UIKitR.color.black_10))
            binding.ivActiveTab.updateLayoutParams<MarginLayoutParams> {
                this.width = activeWidth.toInt()
            }

            binding.ivActiveTab.setImageDrawable(tabCachedShape)
        }
    }


    private fun repositionActiveButton(selectedTab: MaterialButton, progress: Float) {
        val targetX = selectedTab.x - 12.dp

        activeIndicator.x = (activeIndicator.x * (1 - progress)) + (targetX * progress)
    }

    private fun updateEdgesWithAnimation(selectedTab: MaterialButton, progress: Float) {
        val targetLeftAlpha = if (selectedTab == tab1) 0f else 1f
        val targetRightAlpha = if (selectedTab == tab3) 0f else 1f

        if (progress > 0.1f) {
            val interpolatedProgress = (progress - 0.1f) / 0.9f
            leftEdges.alpha = lerp(leftEdges.alpha, targetLeftAlpha, interpolatedProgress)
            rightEdges.alpha = lerp(rightEdges.alpha, targetRightAlpha, interpolatedProgress)

            leftEdges.translationY = lerp(leftEdges.translationY, if (targetLeftAlpha != 1f) -(12).dp else 0f, interpolatedProgress)
            rightEdges.translationY = lerp(rightEdges.translationY, if (targetRightAlpha != 1f) -(12).dp else 0f, interpolatedProgress)
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }

    private fun updateTabContentColors(previousTab: MaterialButton?, newTab: MaterialButton, progress: Float) {
        if (previousTab == null) {
            newTab.setTextColor(selectedColor)
            newTab.icon?.setTint(selectedColor)

            tabs.forEach { tab ->
                if (tab != newTab) {
                    tab.setTextColor(unselectedColor)
                    tab.icon?.setTint(unselectedColor)
                }
            }
            return
        }

        val previousTabColor = interpolateColor(selectedColor, unselectedColor, progress)
        previousTab.setTextColor(previousTabColor)
        previousTab.iconTint = ColorStateList.valueOf(previousTabColor)

        val newTabColor = interpolateColor(unselectedColor, selectedColor, progress)
        newTab.setTextColor(newTabColor)
        newTab.iconTint = ColorStateList.valueOf(newTabColor)
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val fractionClamped = fraction.coerceIn(0f, 1f)
        return ArgbEvaluator().evaluate(fractionClamped, startColor, endColor) as Int
    }

    fun getTab(tab: HomeTab): MaterialButton {
        return when (tab) {
            HomeTab.Grocery -> tab1
            HomeTab.Food -> tab2
            HomeTab.Virtual -> tab3
        }
    }

    enum class HomeTab {
        Grocery {
            override val value: Int get() = ordinal
            override val icon: Int get() = UIKitR.drawable.ic_basket_grocery_16
            override fun toString() = "Grocery"
        },
        Food {
            override val value: Int get() = ordinal
            override val icon: Int get() = UIKitR.drawable.ic_food_16
            override fun toString() = "Food"
        },
        Virtual {
            override val value: Int get() = ordinal
            override val icon: Int get() = UIKitR.drawable.ic_card_virtual_16
            override fun toString() = "Virtual"
        };

        abstract val value: Int
        abstract val icon: Int
    }
}

