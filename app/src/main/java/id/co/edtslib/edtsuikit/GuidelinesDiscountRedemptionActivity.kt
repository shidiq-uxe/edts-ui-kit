package id.co.edtslib.edtsuikit

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesDiscountRedemptionBinding
import id.co.edtslib.uikit.chip.DiscountRedemptionChip
import id.co.edtslib.uikit.chipgroup.DiscountRedemptionCategory
import id.co.edtslib.uikit.chipgroup.DiscountRedemptionChipGroupDelegate
import id.co.edtslib.uikit.tablayout.QuadRoundTabLayout
import id.co.edtslib.uikit.utils.SystemBarStyle
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.setCurrentItem
import id.co.edtslib.uikit.utils.setSystemBarStyle
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.tabs.TabLayoutMediator
import id.co.edtslib.uikit.tablayout.QuadRoundTabLayoutDelegate
import id.co.edtslib.uikit.utils.colorStateList

class GuidelinesDiscountRedemptionActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesDiscountRedemptionBinding>()

    private var items1 = listOf<String>("Paragon Fair", "Paragon Fair 2", "Gajian Indomaret", "Live Shopping Semriwings")
    private var items2 = listOf<String>("Luminarc Soup Idaman Fair", "Toples Kaca Indomaret")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_discount_redemption)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSystemBarStyle(
            statusBarStyle = SystemBarStyle.Dark(color(id.co.edtslib.uikit.R.color.primary_30)),
            navigationBarStyle = SystemBarStyle.Light(Color.WHITE)
        )

        supportActionBar?.setBackgroundDrawable(color(id.co.edtslib.uikit.R.color.primary_30).toDrawable())


        bindNavigationAction()
        initAdapter()
        dummyStepperCheck()
        // simulateDummySkeleton()
        simulateDummyLevel1Tabs()

        binding.dummyTab.isInvisible = true
        binding.vpCategory.isInvisible = true
        binding.skeletonPlaceholder.root.isVisible = true

        Handler(Looper.getMainLooper()).postDelayed({
            binding.dummyTab.isInvisible = false
            binding.vpCategory.isInvisible = false
            binding.skeletonPlaceholder.root.isVisible = false
        }, 3000L)
    }

    private fun simulateDummyLevel1Tabs() {
        binding.dummyTab.setTabs(
            listOf(
                QuadRoundTabLayout.TabItem(
                    title = "Hadiah Produk",
                    badge = QuadRoundTabLayout.BadgeConfig(
                        text = "3",
                        textColor = color(R.color.white),
                        backgroundColor = colorStateList(id.co.edtslib.uikit.R.color.slr_badge_bg)
                    )
                ),
                QuadRoundTabLayout.TabItem(
                    title = "Tebus Murah",
                    badge = QuadRoundTabLayout.BadgeConfig(
                        text = "2",
                        textColor = color(R.color.white),
                        backgroundColor = colorStateList(id.co.edtslib.uikit.R.color.slr_badge_bg)
                    )
                ),
                QuadRoundTabLayout.TabItem(
                    title = "Clearance Sale",
                    badge = QuadRoundTabLayout.BadgeConfig(
                        text = "1",
                        textColor = color(R.color.white),
                        backgroundColor = colorStateList(id.co.edtslib.uikit.R.color.slr_badge_bg)
                    )
                ),
                QuadRoundTabLayout.TabItem(
                    title = "Event Sale",
                    badge = QuadRoundTabLayout.BadgeConfig(
                        text = "1",
                        textColor = color(R.color.white),
                        backgroundColor = colorStateList(id.co.edtslib.uikit.R.color.slr_badge_bg)
                    )
                ),
                QuadRoundTabLayout.TabItem(
                    title = "Flash Sale",
                    badge = QuadRoundTabLayout.BadgeConfig(
                        text = "1",
                        textColor = color(R.color.white),
                        backgroundColor = colorStateList(id.co.edtslib.uikit.R.color.slr_badge_bg)
                    )
                ),
            )
        )

        binding.dummyTab.delegate = object : QuadRoundTabLayoutDelegate {
            override fun onTabSelected(
                position: Int,
                view: QuadRoundTabLayout.QuadRoundTabView,
                item: QuadRoundTabLayout.TabItem
            ) {
                binding.vpCategory.setCurrentItem(position, false)
            }

            override fun onPreventSelected(
                position: Int,
                view: QuadRoundTabLayout.QuadRoundTabView,
                item: QuadRoundTabLayout.TabItem
            ): Boolean {
                return false
            }
        }
    }

    private fun initAdapter() {
        val adapter = Adapter(this, items1)
        binding.vpCategory.adapter = adapter
        binding.vpCategory.isUserInputEnabled = false
    }

    private fun dummyStepperCheck(): Boolean {
        binding.btnSubmit.isEnabled = !binding.btnSubmit.isEnabled
        binding.tvError.isVisible = !binding.tvError.isVisible

        return true
    }

    private fun bindNavigationAction() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    inner class Adapter(fragmentActivity: FragmentActivity, var items: List<String>) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int) = DiscountRedemptionParentPagerFragment.newInstance(position)

        override fun getItemCount() = items.size
    }
}