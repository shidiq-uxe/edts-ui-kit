package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSegmentSwitcherBinding
import id.co.edtslib.edtsuikit.databinding.BottomSheetSwitcherConfigBinding
import id.co.edtslib.uikit.switcher.SegmentSwitcher
import id.co.edtslib.uikit.switcher.SegmentSwitcherDelegate
import id.co.edtslib.uikit.switcher.TabItem
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.px
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesSegmentSwitcherActivity : GuidelinesBaseActivity() {

    override val hasConfigMenu = true

    private val binding by viewBinding<ActivityGuidelinesSegmentSwitcherBinding>()

    private var currentConfigTarget: SegmentSwitcher? = null

    private val allSwitchers
        get() = listOf(
            binding.switcher2Tab, binding.switcher3Tab, binding.switcherScroll,
            binding.switcherCustomFixed, binding.switcherCustomScroll
        )

    private val tab2Items = listOf(
        TabItem("Xpress", "Fast Delivery", id.co.edtslib.uikit.R.drawable.ic_flash_xpress_24),
        TabItem("Xtra", "More Features", id.co.edtslib.uikit.R.drawable.ic_box_xtra_16)
    )

    private val tab3Items = listOf(
        TabItem("Grocery", "Daily Needs", id.co.edtslib.uikit.R.drawable.ic_food_16),
        TabItem("Food", "Ready to Eat", id.co.edtslib.uikit.R.drawable.ic_food_16),
        TabItem("Virtual", "Digital Goods", id.co.edtslib.uikit.R.drawable.ic_box_xtra_16)
    )

    private val tab6Items = listOf(
        TabItem("Grocery", "Fresh Produce", id.co.edtslib.uikit.R.drawable.ic_food_16),
        TabItem("Food", "Hot Meals", id.co.edtslib.uikit.R.drawable.ic_food_16),
        TabItem("Virtual", "e-Vouchers", id.co.edtslib.uikit.R.drawable.ic_box_xtra_16),
        TabItem("Xpress", "Fast Ship", id.co.edtslib.uikit.R.drawable.ic_flash_xpress_24),
        TabItem("Xtra", "Premium", id.co.edtslib.uikit.R.drawable.ic_box_xtra_16),
        TabItem("Deals", "Best Prices", id.co.edtslib.uikit.R.drawable.ic_flash_xpress_24)
    )

    private val tabCustomFixedItems = listOf(
        TabItem("Coffee", "Hot Brew", id.co.edtslib.uikit.R.drawable.ic_food_16),
        TabItem("Tea", "Herbal", id.co.edtslib.uikit.R.drawable.ic_food_16)
    )

    private val tabCustomScrollItems = listOf(
        TabItem("Rock", "Loud", id.co.edtslib.uikit.R.drawable.ic_food_16),
        TabItem("Jazz", "Smooth", id.co.edtslib.uikit.R.drawable.ic_food_16),
        TabItem("Pop", "Chart Hits", id.co.edtslib.uikit.R.drawable.ic_flash_xpress_24),
        TabItem("Classical", "Orchestral", id.co.edtslib.uikit.R.drawable.ic_box_xtra_16)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val delegate = object : SegmentSwitcherDelegate {
            override fun onSwitchChangedListener(position: Int, tab: TabItem) {}
            override fun onSwitchAnimationEndListener(position: Int, tab: TabItem) {}
        }

        // 1. Fixed — 2 Tabs
        binding.switcher2Tab.apply {
            this.delegate = delegate
            isDraggable = true
            setTabs(tab2Items)
        }
        currentConfigTarget = binding.switcher2Tab

        // 2. Fixed — 3 Tabs
        binding.switcher3Tab.apply {
            this.delegate = delegate
            isDraggable = true
            setTabs(tab3Items)
        }

        // 3. Scrollable — 6 Tabs
        binding.switcherScroll.apply {
            this.delegate = delegate
            setTabs(tab6Items)
        }

        // 4. Fixed — 2 Tabs (Custom Layout)
        binding.switcherCustomFixed.apply {
            this.delegate = delegate
            isDraggable = true
            tabViewProvider = { ctx -> makeCustomTabView(ctx) }
            setTabs(tabCustomFixedItems)
        }

        // 5. Scrollable — 4 Tabs (Custom Layout + Wrap)
        binding.switcherCustomScroll.apply {
            this.delegate = delegate
            tabWidthMode = SegmentSwitcher.TAB_WIDTH_WRAP
            tabViewProvider = { ctx -> makeCustomTabView(ctx) }
            setTabs(tabCustomScrollItems)
        }
    }

    override fun onConfigMenuClicked() {
        showConfigSheet()
    }

    private fun showConfigSheet() {
        val sheetBinding = BottomSheetSwitcherConfigBinding.inflate(
            LayoutInflater.from(this), null, false
        )

        val target = currentConfigTarget ?: return

        sheetBinding.swAnimate.isChecked = target.shouldAnimate
        sheetBinding.swDraggable.isChecked = target.isDraggable
        sheetBinding.swWrapWidth.isChecked = target.tabWidthMode == SegmentSwitcher.TAB_WIDTH_WRAP
        sheetBinding.tvWidthFactorValue.text = "%.2f".format(target.tabWidthFactor)
        sheetBinding.slWidthFactor.value = target.tabWidthFactor
        sheetBinding.slCornerRadius.value = target.cornerRadius.px
        sheetBinding.tvCornerRadiusValue.text = "${target.cornerRadius.px.toInt()}dp"

        sheetBinding.btnThemeDefault.setOnClickListener {
            allSwitchers.forEach {
                it.activeBackgroundColor = color(id.co.edtslib.uikit.R.color.primary_40)
                it.activeTextColor = color(id.co.edtslib.uikit.R.color.white)
                it.trackColor = color(id.co.edtslib.uikit.R.color.black_20)
            }
        }

        sheetBinding.btnThemeOrange.setOnClickListener {
            allSwitchers.forEach {
                it.activeBackgroundColor = color(id.co.edtslib.uikit.R.color.secondary_30)
                it.activeTextColor = color(id.co.edtslib.uikit.R.color.white)
                it.trackColor = color(id.co.edtslib.uikit.R.color.black_20)
            }
        }

        sheetBinding.btnThemeGreen.setOnClickListener {
            allSwitchers.forEach {
                it.activeBackgroundColor = color(id.co.edtslib.uikit.R.color.support_extra)
                it.activeTextColor = color(id.co.edtslib.uikit.R.color.white)
                it.trackColor = color(id.co.edtslib.uikit.R.color.black_20)
            }
        }

        sheetBinding.swAnimate.setOnCheckedChangeListener { _, checked ->
            allSwitchers.forEach { it.shouldAnimate = checked }
        }

        sheetBinding.swDraggable.setOnCheckedChangeListener { _, checked ->
            allSwitchers.forEach { it.isDraggable = checked }
        }

        sheetBinding.swWrapWidth.setOnCheckedChangeListener { _, checked ->
            allSwitchers.forEach {
                it.tabWidthMode = if (checked) SegmentSwitcher.TAB_WIDTH_WRAP else SegmentSwitcher.TAB_WIDTH_FIXED
            }
        }

        sheetBinding.slWidthFactor.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            allSwitchers.forEach { it.tabWidthFactor = value }
            sheetBinding.tvWidthFactorValue.text = "%.2f".format(value)
        })

        sheetBinding.slCornerRadius.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            allSwitchers.forEach { it.cornerRadius = value.dp }
            sheetBinding.tvCornerRadiusValue.text = "${value.toInt()}dp"
        })

        BottomSheetTray.newInstance(
            title = "Configure Switcher",
            contentLayout = sheetBinding.root
        ).apply {
            dismissOnOutsideTouch = true
            isCancelableOnTouchOutside = true
            dragHandleVisibility = true
            titleDividerVisibility = true
            shouldShowClose = true
        }.show(supportFragmentManager, "CONFIG_SHEET")
    }

    private fun makeCustomTabView(ctx: android.content.Context): LinearLayout {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16.dp.toInt(), 12.dp.toInt(), 16.dp.toInt(), 12.dp.toInt())

            ImageView(ctx).apply {
                id = UIKitR.id.segment_switcher_tab_icon
                layoutParams = LinearLayout.LayoutParams(20.dp.toInt(), 20.dp.toInt())
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }.also { addView(it) }

            TextView(ctx).apply {
                id = UIKitR.id.segment_switcher_tab_title
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = 6.dp.toInt() }
                setTextAppearance(UIKitR.style.TextAppearance_Inter_Medium_B4)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }.also { addView(it) }
        }
    }
}
