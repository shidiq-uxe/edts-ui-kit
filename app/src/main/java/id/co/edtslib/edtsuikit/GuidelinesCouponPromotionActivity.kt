package id.co.edtslib.edtsuikit

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ActionBar
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesCouponPromotionBinding
import id.co.edtslib.edtsuikit.databinding.ItemPromotionCouponBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.adapter.multiTypeAdapter
import id.co.edtslib.uikit.databinding.ListBinding
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.attachLinearMarginItemDecoration
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dp
import kotlin.math.abs
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.button.MaterialButton
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.showFloatingAnimation
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesCouponPromotionActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinesCouponPromotionBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)

            insets
        }

        dummyAdapter()
        setupScrollingLogic()
        // setupRecyclerScrollBehavior()
    }

    private var lastIsCollapsed = false
    private var isTabHidden = false
    private var radiusAnimator: ValueAnimator? = null


    /*TODO: Tab Level 2 Elevation and color interpolation*/
    private fun setupScrollingLogic() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val percentage = abs(verticalOffset).toFloat() / totalScrollRange
            val isCollapsed = abs(verticalOffset) >= totalScrollRange

            val fadeMultiplier = 1.75f
            binding.liquidGlassCard.alpha =
                (1 - percentage * fadeMultiplier).coerceIn(0f, 1f)

            val toolbarContentColors = Pair(color(R.color.white), color(id.co.edtslib.uikit.R.color.black_50))
            val toolbarInterpolatedColor = ArgbEvaluator().evaluate(percentage, toolbarContentColors.first, toolbarContentColors.second) as Int
            binding.toolbar.setNavigationIconTint(toolbarInterpolatedColor)

            val tabLevel2Colors = Pair(color(id.co.edtslib.uikit.R.color.black_10), color(R.color.white))
            val tabLevel2InterpolatedColor = ArgbEvaluator().evaluate(percentage, tabLevel2Colors.first, tabLevel2Colors.second) as Int

            binding.cgTabLevel2.background = tabLevel2InterpolatedColor.toDrawable()

            if (lastIsCollapsed == isCollapsed) return@addOnOffsetChangedListener
            lastIsCollapsed = isCollapsed

            val toolbarSize = if (isCollapsed) dimen(com.google.android.material.R.dimen.abc_action_bar_default_height_material) else dimen(
                id.co.edtslib.uikit.R.dimen.l)

            WindowCompat.getInsetsController(window, window.decorView)
                .isAppearanceLightStatusBars = isCollapsed

            animateRadiusChange(isCollapsed)
        }

        binding.rvCoupons.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Only hide if we are collapsed and scrolling DOWN
                if (lastIsCollapsed && dy > 0 && !isTabHidden) {
                    hideTab()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // When user stops scrolling (IDLE), show the tab if it's hidden
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isTabHidden) {
                    showTab()
                }
            }
        })
    }

    private fun hideTab() {
        isTabHidden = true
        binding.cgTabLevel2.animate()
            .translationY(-binding.cgTabLevel2.height.toFloat())
            .alpha(0f)
            .setDuration(200L)
            .start()
    }

    private fun showTab() {
        isTabHidden = false
        binding.cgTabLevel2.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(200L)
            .start()
    }


    private fun animateRadiusChange(isCollapsed: Boolean) {
        val from = if (isCollapsed) 12.dp else 0.dp
        val to = if (isCollapsed) 0.dp else 12.dp

        radiusAnimator?.cancel()

        radiusAnimator = ValueAnimator.ofFloat(from, to).apply {
            duration = 180L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                binding.stickyTabContainer.shapeAppearanceModel =
                    binding.stickyTabContainer.shapeAppearanceModel
                        .toBuilder()
                        .setTopLeftCornerSize(value)
                        .setTopRightCornerSize(value)
                        .build()
            }
            start()
        }
    }


    private fun dummyAdapter() {
        binding.rvCoupons.adapter = BaseAdapter.adapterOf<String, ItemPromotionCouponBinding>(
            register = BaseAdapter.Register(
                onBindHolder = { position, item, adapterBinding, _ ->
                    adapterBinding.btnRedeem.setOnClickListener {
                        if (position == 2) {
                            it.alertSnack(
                                message = "Kupon tidak bisa ditukar"
                            )
                        } else {
                            it.showFloatingAnimation(
                                icon = id.co.edtslib.uikit.R.drawable.ic_voucher_applied_16,
                                text = "+1",
                                // duration = 1500L
                            )

                            adapterBinding.btnRedeem.setButtonToClickedState(id.co.edtslib.uikit.R.color.slr_button_filled_bg)
                        }
                    }
                }
            ), diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old == new },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList = List(25) { "Item No $it" }
        ).apply {
            setOnItemClickListener { binding, item, position ->

            }
        }

        binding.rvCoupons.attachLinearMarginItemDecoration(
            orientation = LinearLayoutManager.VERTICAL,
            margin = MarginItem(first =  0, top = 12.dp.toInt())
        )
    }

    fun MaterialButton.setButtonToClickedState(
        strokeColorRes: Int
    ) {
        tag = tag ?: ButtonState(
            backgroundColor = backgroundTintList,
            textColor = currentTextColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )

        background = null
        backgroundTintList = colorStateList(UIKitR.color.white)

        strokeWidth = 2.dp.toInt()
        strokeColor = ColorStateList.valueOf(color(UIKitR.color.primary_30))

        rippleColor = ColorStateList.valueOf(color(UIKitR.color.primary_30))


        setTextColor(ContextCompat.getColor(context, strokeColorRes))

        this.text = "Lihat"

        isEnabled = false
    }

    // Helper to revert button state
    fun MaterialButton.revertButtonState() {
        val savedState = tag as? ButtonState ?: return

        backgroundTintList = savedState.backgroundColor
        setTextColor(savedState.textColor)
        strokeColor = savedState.strokeColor
        strokeWidth = savedState.strokeWidth
        isEnabled = true
    }

    // Data class to store button state
    private data class ButtonState(
        val backgroundColor: android.content.res.ColorStateList?,
        val textColor: Int,
        val strokeColor: android.content.res.ColorStateList?,
        val strokeWidth: Int
    )
}