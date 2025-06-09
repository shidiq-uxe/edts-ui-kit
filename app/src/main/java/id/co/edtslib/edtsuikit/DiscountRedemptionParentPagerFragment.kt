package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import id.co.edtslib.edtsuikit.databinding.FragmentDiscountRedemptionParentPagerBinding

class DiscountRedemptionParentPagerFragment : Fragment() {

    private var onLongClickCallback: (() -> Boolean)? = null

    private val binding by viewBinding<FragmentDiscountRedemptionParentPagerBinding>()

    private var itemList: List<String> = listOf("Paragon Fair", "Paragon Fair 2", "Gajian Indomaret", "Live Shopping Semriwings")
    private var itemList2: List<String> = listOf<String>("Luminarc Soup Idaman Fair", "Toples Kaca Indomaret")

    private var selectedPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        selectedPosition = arguments?.getInt("selectedPosition") ?: 0

        return inflater.inflate(R.layout.fragment_discount_redemption_parent_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = if (selectedPosition == 0) itemList else itemList2

        binding.vp.adapter = Adapter(this.requireActivity(), items)

        TabLayoutMediator(binding.tabLayout, binding.vp) { tab, position -> tab.text = items[position] }
            .attach()
    }

    companion object {
        @JvmStatic
        fun newInstance(selectedPosition: Int, onLongClickCallback: () -> Boolean = { false }) =
            DiscountRedemptionParentPagerFragment().apply {
                this.onLongClickCallback = onLongClickCallback
                arguments = Bundle().apply {
                    putInt("selectedPosition", selectedPosition)
                }
            }
    }

    inner class Adapter(fragmentActivity: FragmentActivity, var items: List<String>) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int) = DiscountRedemptionPagerFragment.newInstance()

        override fun getItemCount() = items.size
    }
}
