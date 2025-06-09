package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class DiscountRedemptionPagerFragment : Fragment() {

    private var onLongClickCallback: (() -> Boolean)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discount_redemption_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.imageView).setOnLongClickListener {
            onLongClickCallback?.invoke() == true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(onLongClickCallback: () -> Boolean = {false}) =
            DiscountRedemptionPagerFragment().apply {
                this.onLongClickCallback = onLongClickCallback
            }
    }
}
