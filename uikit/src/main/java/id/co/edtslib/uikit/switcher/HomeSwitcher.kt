package id.co.edtslib.uikit.switcher

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.res.use
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.HomepageSwitcherBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater

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

    init {
        init(attrs)
        setupTabs()
    }

    private fun init(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.HomeSwitcher, 0, 0).use {
            firstTabTitle = it.getString(R.styleable.HomeSwitcher_firstTabTitle) ?: firstTabTitle
            secondTabTitle = it.getString(R.styleable.HomeSwitcher_secondTabTitle) ?: secondTabTitle
            firstTabSubtitle = it.getString(R.styleable.HomeSwitcher_firstTabSubtitle) ?: firstTabSubtitle
            secondTabSubtitle = it.getString(R.styleable.HomeSwitcher_secondTabSubtitle) ?: secondTabSubtitle
            firstTabIcon = it.getResourceId(R.styleable.HomeSwitcher_firstTabIcon, firstTabIcon)
            secondTabIcon = it.getResourceId(R.styleable.HomeSwitcher_secondTabIcon, secondTabIcon)
        }
    }

    private fun setupTabs() {
        clipChildren = false

        setActiveTab(binding.xpressTab)

        binding.xpressTab.setOnClickListener {
            setActiveTab(it)
            binding.xpressTab.isEnabled = false
            binding.xtraTab.isEnabled = true
        }

        binding.xtraTab.setOnClickListener {
            setActiveTab(it)
            binding.xtraTab.isEnabled = false
            binding.xpressTab.isEnabled = true
        }
    }

    private fun setActiveTab(selectedTab: View) {
        val targetTranslationX = selectedTab.left.toFloat() - binding.activeTab.left.toFloat()

        val (startColor, endColor) = when (selectedTab) {
            binding.xpressTab -> {
                context.color(R.color.secondary_30) to context.color(R.color.support_extra)
            }
            binding.xtraTab -> {
                context.color(R.color.support_extra) to context.color(R.color.secondary_30)
            }
            else -> {
                context.color(R.color.secondary_30) to context.color(R.color.support_extra)
            }
        }

        val springAnimation = SpringAnimation(binding.activeTab, DynamicAnimation.TRANSLATION_X, targetTranslationX).apply {
            spring = SpringForce(targetTranslationX).apply {
                stiffness = 300f
                dampingRatio = 0.75f
            }

            addUpdateListener(object : DynamicAnimation.OnAnimationUpdateListener {
                override fun onAnimationUpdate(animation: DynamicAnimation<*>?, value: Float, velocity: Float) {
                    updateActiveTabContent(selectedTab)
                }
            })
        }

        springAnimation.start()

        val colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                val interpolatedColor = ArgbEvaluator().evaluate(progress, endColor, startColor) as Int
                binding.activeTab.setBackgroundColor(interpolatedColor)
            }
        }

        colorAnimator.start()
    }

    private fun updateActiveTabContent(selectedTab: View) {
        val (defaultTitleColor, defaultSubtitleColor, selectedTextColor) = Triple(
            context.color(R.color.black_60),
            context.color(R.color.black_40),
            context.color(R.color.white)
        )
        val (defaultIconColor, selectedIconColor) =
            context.colorStateList(R.color.black_60) to context.colorStateList(R.color.white)

        when (selectedTab) {
            binding.xpressTab -> {
                binding.ivXpressLogo.imageTintList = selectedIconColor
                binding.ivXtraLogo.imageTintList = defaultIconColor

                binding.tvXpressTitle.setTextColor(selectedTextColor)
                binding.tvXpressSubtitle.setTextColor(selectedTextColor)

                binding.tvXtraTitle.setTextColor(defaultTitleColor)
                binding.tvXtraSubtitle.setTextColor(defaultSubtitleColor)

                delegate?.setOnSwitchChangedListener(Tab.Xpress)
            }
            binding.xtraTab -> {
                binding.ivXtraLogo.imageTintList = selectedIconColor
                binding.ivXpressLogo.imageTintList = defaultIconColor

                binding.tvXtraTitle.setTextColor(selectedTextColor)
                binding.tvXtraSubtitle.setTextColor(selectedTextColor)

                binding.tvXpressTitle.setTextColor(defaultTitleColor)
                binding.tvXpressSubtitle.setTextColor(defaultSubtitleColor)

                delegate?.setOnSwitchChangedListener(Tab.Xtra)
            }
            else -> Toast.makeText(context, "Unknown tab clicked", Toast.LENGTH_SHORT).show()
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
