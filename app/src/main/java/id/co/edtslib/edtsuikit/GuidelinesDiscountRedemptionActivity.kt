package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import id.co.edtslib.uikit.utils.setCurrentItem

class GuidelinesDiscountRedemptionActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesDiscountRedemptionBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_discount_redemption)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.chipGroup.items = List(5) {
            DiscountRedemptionCategory(
                id = it.hashCode().toString(),
                categoryName = "Category $it",
                state = when (it) {
                    in 0..1 -> DiscountRedemptionChip.STATE_DEFAULT
                    in 2..4 -> DiscountRedemptionChip.STATE_INACTIVE
                    else -> DiscountRedemptionChip.STATE_DEFAULT
                }
            )
        }

        initAdapter()
        simulateDummySkeleton()
    }

    private fun simulateDummySkeleton() {
        binding.dummyTab.addOnTabSelectedListener( object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.vpCategory.isInvisible = true
                binding.chipGroup.isInvisible = true
                binding.skeletonPlaceholder.root.isVisible = true

                Handler(Looper.getMainLooper()).postDelayed({
                    binding.vpCategory.isInvisible = false
                    binding.chipGroup.isInvisible = false
                    binding.skeletonPlaceholder.root.isVisible = false
                }, 2000L)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }

    private fun initAdapter() {
        val adapter = Adapter(this)
        binding.vpCategory.adapter = adapter
        binding.vpCategory.isUserInputEnabled = false

        binding.chipGroup.delegate = object : DiscountRedemptionChipGroupDelegate {
            override fun onItemSelected(
                position: Int,
                view: View,
                item: DiscountRedemptionCategory
            ) {
                binding.vpCategory.setCurrentItem(position, 350L)
            }
        }
    }
}

class Adapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int) = DiscountRedemptionPagerFragment()

    override fun getItemCount() = 5
}