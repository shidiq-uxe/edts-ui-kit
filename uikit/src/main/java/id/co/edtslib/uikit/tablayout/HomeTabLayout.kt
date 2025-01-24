package id.co.edtslib.uikit.tablayout

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.button.MaterialButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
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

    private val tab1: MaterialButton = binding.tab1
    private val tab2: MaterialButton = binding.tab2
    private val tab3: MaterialButton = binding.tab3
    private val activeButton: MaterialButton = binding.activeButton

    private val tabs: List<MaterialButton> = listOf(tab1, tab2, tab3)

    private val leftEdges = binding.leftEdges
    private val rightEdges = binding.rightEdges

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

    var titles = listOf(HomeTab.Grocery.value, HomeTab.Food.value, HomeTab.Virtual.value)
        set(value) {
            field = value
            tab1.text = context.getString(value.first())
            tab2.text = context.getString(value[1])
            tab3.text = context.getString(value[2])
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
            activeButton.iconTint = ColorStateList.valueOf(value)
            activeButton.setTextColor(value)
        }
    var unselectedColor = context.color(UIKitR.color.white)
        set(value) {
            field = value
            tabs.forEach {
                it.iconTint = ColorStateList.valueOf(value)
                it.setTextColor(value)
            }
        }

    init {
        this.background = ColorDrawable(context.color(UIKitR.color.black_10))
        initAttrs(attrs)
        setupListeners()
        updateActiveTab(tab1)

        // Initial Alpha
        leftEdges.alpha = 0f
    }

    private fun initAttrs(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, UIKitR.styleable.HomeTabLayout, 0, 0).use {
            selectedColor = it.getColor(UIKitR.styleable.HomeTabLayout_selectedItemColor, context.color(UIKitR.color.primary_30))
            unselectedColor = it.getColor(UIKitR.styleable.HomeTabLayout_unselectedItemColor, context.color(UIKitR.color.white))
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
        activeButton.text = tab.toString()
        activeButton.icon = context.drawable(tab.icon)

        delegate?.onTabSelected(tab)

        repositionActiveButton(selectedTab)
        animateButtonCorners(selectedTab)
    }


    private fun repositionActiveButton(selectedTab: MaterialButton) {
        activeButton.animate()
            .x(selectedTab.x)
            .setInterpolator(EaseInterpolator.EaseInOutQubicInterpolator)
            .setDuration(200)
            .start()
    }

    private fun animateButtonCorners(selectedTab: MaterialButton) {
        val cornerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400
            addUpdateListener { animator ->
                val progress = animator.animatedFraction
                updateCornersWithAnimation(selectedTab, progress)
                updateEdgesWithAnimation(selectedTab)
            }
        }
        cornerAnimator.start()
    }

    private fun updateCornersWithAnimation(selectedTab: MaterialButton, progress: Float) {
        when (selectedTab) {
            tab1 -> {
                tab1.shapeAppearanceModel = createShapeAppearance(0f, 0f, progress)
                tab2.shapeAppearanceModel = createShapeAppearance(12.dp, 0f, progress)
                tab3.shapeAppearanceModel = createShapeAppearance(0f, 0f, progress)
            }
            tab2 -> {
                tab1.shapeAppearanceModel = createShapeAppearance(0f, 12.dp, progress)
                tab2.shapeAppearanceModel = createShapeAppearance(0f, 0f, progress)
                tab3.shapeAppearanceModel = createShapeAppearance(12.dp, 0f, progress)
            }
            tab3 -> {
                tab1.shapeAppearanceModel = createShapeAppearance(0f, 0f, progress)
                tab2.shapeAppearanceModel = createShapeAppearance(0f, 12.dp, progress)
                tab3.shapeAppearanceModel = createShapeAppearance(0f, 0f, progress)
            }
        }
    }

    private var isAnimatingLeftEdge = false
    private var isAnimatingRightEdge = false

    private fun updateEdgesWithAnimation(selectedTab: MaterialButton) {
        when (selectedTab) {
            tab1 -> {
                if (!isAnimatingLeftEdge && leftEdges.alpha > 0f) {
                    animateEdgeAlpha(leftEdges, 0f)
                    isAnimatingLeftEdge = true
                }
                if (!isAnimatingRightEdge && rightEdges.alpha < 1f) {
                    animateEdgeAlpha(rightEdges, 1f)
                    isAnimatingRightEdge = true
                }
            }
            tab2 -> {
                if (!isAnimatingLeftEdge && leftEdges.alpha < 1f) {
                    animateEdgeAlpha(leftEdges, 1f)
                    isAnimatingLeftEdge = true
                }
                if (!isAnimatingRightEdge && rightEdges.alpha < 1f) {
                    animateEdgeAlpha(rightEdges, 1f)
                    isAnimatingRightEdge = true
                }
            }
            tab3 -> {
                if (!isAnimatingLeftEdge && leftEdges.alpha < 1f) {
                    animateEdgeAlpha(leftEdges, 1f)
                    isAnimatingLeftEdge = true
                }
                if (!isAnimatingRightEdge && rightEdges.alpha > 0f) {
                    animateEdgeAlpha(rightEdges, 0f)
                    isAnimatingRightEdge = true
                }
            }
        }
    }

    private fun animateEdgeAlpha(edge: View, targetAlpha: Float) {
        edge.animate()
            .alpha(targetAlpha)
            .setDuration(300)
            .setInterpolator(EaseInterpolator.EaseInOutQubicInterpolator)
            .withEndAction {
                if (edge == leftEdges) isAnimatingLeftEdge = false
                if (edge == rightEdges) isAnimatingRightEdge = false
            }
            .start()
    }


    private fun createShapeAppearance(
        bottomLeft: Float,
        bottomRight: Float,
        progress: Float
    ): ShapeAppearanceModel {
        return ShapeAppearanceModel.builder()
            .setBottomLeftCornerSize(bottomLeft * progress)
            .setBottomRightCornerSize(bottomRight * progress)
            .build()
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
            override fun toString() = javaClass.simpleName.toString()
        },
        Food {
            override val value: Int get() = ordinal
            override val icon: Int get() = UIKitR.drawable.ic_food_16
            override fun toString() = javaClass.simpleName.toString()
        },
        Virtual {
            override val value: Int get() = ordinal
            override val icon: Int get() = UIKitR.drawable.ic_card_virtual_16
            override fun toString() = javaClass.simpleName.toString()
        };

        abstract val value: Int
        abstract val icon: Int
    }


}

