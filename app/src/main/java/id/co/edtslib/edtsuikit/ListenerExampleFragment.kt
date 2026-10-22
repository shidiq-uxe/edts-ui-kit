package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import id.co.edtslib.edtsuikit.databinding.ViewCardTestBinding
import id.co.edtslib.uikit.core.listitem.ListItem
import id.co.edtslib.uikit.core.listitem.ListItemEvent
import id.co.edtslib.uikit.core.listitem.ListItemListener

class ListenerExampleFragment : Fragment() {

    private var _binding: ViewCardTestBinding? = null
    private val binding get() = _binding!!

    private var card: ProductCardController? = null

    private val product = Product(
        id = "1",
        name = "Aqua Air Mineral 600ml",
        imageUrl = null,
        price = 2_000
    )

    private val analyticsListener = ListItemListener<Product, ProductCardState> { e ->
        when (e) {
            is ListItemEvent.Click -> Log.d(TAG, "analytics: click ${e.item.id}")
            is ListItemEvent.LongClick -> Log.d(TAG, "analytics: long click ${e.item.id}")
            is ListItemEvent.Custom -> Log.d(TAG, "analytics: action ${e.key.name}")
            is ListItemEvent.StateChanged -> Log.d(TAG, "analytics: state ${e.oldState} -> ${e.newState}")
        }
    }

    private val firstTapHint = object : ListItemListener<Product, ProductCardState> {
        override fun onEvent(event: ListItemEvent<Product, ProductCardState>) {
            if (event is ListItemEvent.Click) {
                toast("Tip: long press the card to add one")
                card?.removeListener(this)
            }
        }
    }

    private val screenListener = ListItemListener<Product, ProductCardState> { e ->
        when (e) {
            is ListItemEvent.Click -> toast("Open: ${e.item.name}")
            is ListItemEvent.LongClick -> card?.update { it.copy(quantity = it.quantity + 1) }
            is ListItemEvent.Custom -> if (e.key === ProductActions.Image) toast("Image tapped")
            is ListItemEvent.StateChanged -> toast("Quantity = ${e.newState.quantity}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewCardTestBinding.inflate(inflater, container, false)

        card = ListItem<Product, ProductCardState>(binding.root, ProductCardState())
            .enableCardClicks()
            .bindAction(binding.ivCouponImage, ProductActions.Image)
            .setRenderer { _, item, _ -> binding.tvProductName.text = item.name }
            .addListener(analyticsListener)
            .addListener(firstTapHint)

        card?.bind(product)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        card?.addListener(screenListener)
    }

    override fun onStop() {
        card?.removeListener(screenListener)
        super.onStop()
    }

    override fun onDestroyView() {
        card?.release()
        card = null
        _binding = null
        super.onDestroyView()
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    private companion object {
        const val TAG = "ListenerExample"
    }
}