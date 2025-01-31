package id.co.edtslib.uikit.switcher

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.res.use
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.material.button.MaterialButton
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.HomepageSwitcherBinding
import id.co.edtslib.uikit.tablayout.HomeTabLayout.HomeTab
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater
import kotlin.math.abs

class HomeSwitcher @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = HomepageSwitcherBinding.inflate(context.inflater, this, true)

    var delegate: HomeSwitcherDelegate? = null

    var firstTabTitle: String = context.getString(R.string.switcher_xpress_title)
        set(value) {
            binding.tvXpressTitle.text = value
        }
    var secondTabTitle: String = context.getString(R.string.switcher_xtra_title)
        set(value) {
            binding.tvXtraTitle.text = value
        }

    var firstTabSubtitle: String = context.getString(R.string.switcher_xpress_subtitle)
        set(value) {
            binding.tvXpressSubtitle.text = value
        }
    var secondTabSubtitle: String = context.getString(R.string.switcher_xtra_subtitle)
        set(value) {
            binding.tvXtraSubtitle.text = value
        }

    var firstTabIcon: Int = R.drawable.ic_flash_xpress_24
        set(value) {
            binding.ivXpressLogo.setImageDrawable(context.drawable(value))
        }
    var secondTabIcon: Int = R.drawable.ic_box_xtra_16
        set(value) {
            binding.ivXtraLogo.setImageDrawable(context.drawable(value))
        }

    var selectedTab = Tab.Xpress
        set(value) {
            field = value

            when(value) {
                Tab.Xpress -> {
                    updateActiveTab(binding.xpressTab)
                }
                Tab.Xtra -> {
                    updateActiveTab(binding.xtraTab)
                }
            }
        }

    var isDraggable = false

    private var initialTouchX = 0f
    private var initialTranslationX = 0f
    private var maxTranslationX = 0f
    private var isDragging = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        init(attrs)
        setupTabs()

        binding.root.doOnLayout {
            maxTranslationX = binding.xtraTab.left - binding.xpressTab.left.toFloat()
        }
    }

    private fun init(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.HomeSwitcher, 0, 0).use {
            firstTabTitle = it.getString(R.styleable.HomeSwitcher_firstTabTitle) ?: firstTabTitle
            secondTabTitle = it.getString(R.styleable.HomeSwitcher_secondTabTitle) ?: secondTabTitle
            firstTabSubtitle = it.getString(R.styleable.HomeSwitcher_firstTabSubtitle) ?: firstTabSubtitle
            secondTabSubtitle = it.getString(R.styleable.HomeSwitcher_secondTabSubtitle) ?: secondTabSubtitle
            firstTabIcon = it.getResourceId(R.styleable.HomeSwitcher_firstTabIcon, firstTabIcon)
            secondTabIcon = it.getResourceId(R.styleable.HomeSwitcher_secondTabIcon, secondTabIcon)
            isDraggable = it.getBoolean(R.styleable.HomeSwitcher_isDraggable, isDraggable)
        }
    }

    private fun setupTabs() {
        clipChildren = false
        binding.activeTab.setBackgroundColor(context.color(R.color.secondary_30))

        // Rest of your existing code
        binding.xpressTab.setOnClickListener { selectedTab = Tab.Xpress }
        binding.xtraTab.setOnClickListener { selectedTab = Tab.Xtra }
    }

    fun updateActiveTab(selectedTab: View) {
        when (selectedTab) {
            binding.xpressTab -> {
                setActiveTab(binding.xpressTab, Tab.Xpress).also {
                    delegate?.onSwitchChangedListener(Tab.Xpress)
                }
                binding.xpressTab.isEnabled = false
                binding.xtraTab.isEnabled = true
            }
            binding.xtraTab -> {
                setActiveTab(binding.xtraTab, Tab.Xtra).also {
                    delegate?.onSwitchChangedListener(Tab.Xtra)
                }
                binding.xtraTab.isEnabled = false
                binding.xpressTab.isEnabled = true
            }
        }
    }

    private fun setActiveTab(selectedTab: View, tab: Tab) {
        if (this.selectedTab != tab) {
            this.selectedTab = tab
        }

        val targetTranslationX = selectedTab.left.toFloat() - binding.activeTab.left.toFloat()

        // Update UI immediately to target state if not dragging
        if (!isDragging || !isDraggable) {
            updateUIForTranslationX(targetTranslationX)
        }

        val springAnimation = SpringAnimation(binding.activeTab, DynamicAnimation.TRANSLATION_X, targetTranslationX).apply {
            spring = SpringForce(targetTranslationX).apply {
                stiffness = 300f
                dampingRatio = 0.75f
            }
            addUpdateListener { _, value, _ ->
                updateUIForTranslationX(value)
            }
            addEndListener { _, _, _, _ ->
                delegate?.onSwitchAnimationEndListener(tab)
            }
        }
        springAnimation.start()
    }

    private fun updateUIForTranslationX(translationX: Float) {
        if (maxTranslationX == 0f) return

        val progress = translationX / maxTranslationX

        // Background color
        val startBgColor = context.color(R.color.secondary_30)
        val endBgColor = context.color(R.color.support_extra)
        binding.activeTab.setBackgroundColor(interpolateColor(startBgColor, endBgColor, progress))

        // Text and icon colors
        val selectedTextColor = context.color(R.color.white)
        val defaultTitleColor = context.color(R.color.black_60)
        val defaultSubtitleColor = context.color(R.color.black_40)
        val defaultIconColor = context.color(R.color.black_60)
        val selectedIconColor = context.color(R.color.white)

        // Xpress tab
        binding.tvXpressTitle.setTextColor(interpolateColor(selectedTextColor, defaultTitleColor, progress))
        binding.tvXpressSubtitle.setTextColor(interpolateColor(selectedTextColor, defaultSubtitleColor, progress))
        binding.ivXpressLogo.setColorFilter(interpolateColor(selectedIconColor, defaultIconColor, progress))

        // Xtra tab
        binding.tvXtraTitle.setTextColor(interpolateColor(defaultTitleColor, selectedTextColor, progress))
        binding.tvXtraSubtitle.setTextColor(interpolateColor(defaultSubtitleColor, selectedTextColor, progress))
        binding.ivXtraLogo.setColorFilter(interpolateColor(defaultIconColor, selectedIconColor, progress))
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val fractionClamped = fraction.coerceIn(0f, 1f)
        return ArgbEvaluator().evaluate(fractionClamped, startColor, endColor) as Int
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isDraggable) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.x
                initialTranslationX = binding.activeTab.translationX
                isDragging = false
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) {
                    val deltaX = abs(event.x - initialTouchX)
                    if (deltaX > touchSlop) {
                        isDragging = true
                        parent.requestDisallowInterceptTouchEvent(true)
                        return true
                    }
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
                var newTranslation = (initialTranslationX + deltaX).coerceIn(0f, maxTranslationX)
                binding.activeTab.translationX = newTranslation
                updateUIForTranslationX(newTranslation)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent.requestDisallowInterceptTouchEvent(false)
                val targetTab = if (binding.activeTab.translationX >= maxTranslationX / 2) Tab.Xtra else Tab.Xpress
                selectedTab = targetTab
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }


    enum class Tab {
        Xpress {
            override fun toString() = javaClass.simpleName.toString()
        },
        Xtra {
            override fun toString() = javaClass.simpleName.toString()
        };
    }
}
