package id.co.edtslib.edtsuikit

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import id.co.edtslib.edtsuikit.databinding.LayoutGuidelinesBottomSheetItemBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.databinding.ListBinding
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.tray.BottomTrayDelegate
import id.co.edtslib.uikit.utils.px

class GuidelinesBottomTray(
    private val context: Context,
    private val fragmentManager: FragmentManager
) {
    private var bottomSheet: BottomSheetTray? = null
    var bottomSheetDelegate: BottomTrayDelegate? = null

    var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    fun show() {
        val binding = LayoutGuidelinesBottomSheetItemBinding.inflate(
            LayoutInflater.from(context),
            null, false
        )

        binding.root.adapter = BaseAdapter.adapterOf<String, ListBinding>(
            register = BaseAdapter.Register(
                onBindHolder = { _, item, adapterBinding, _ ->
                    adapterBinding.tvTitle.text = item
                }
            ), diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old == new },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList = List(15) { "Item No $it" }
        )

        binding.root.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View,
                    parent: RecyclerView, state: RecyclerView.State,
                ) {
                    outRect.bottom = 8.px.toInt()
                }
            }
        )

        bottomSheet = BottomSheetTray.newInstance(
            title = "Bottom Sheet Tray",
            contentLayout = binding.root
        ).apply {
            dismissOnOutsideTouch = true
            isCancelableOnTouchOutside = true
            dragHandleVisibility = true

            shouldShowNavigation = false
            titleDividerVisibility = true
            shouldShowClose = true

            delegate = bottomSheetDelegate
        }

        bottomSheet?.show(fragmentManager, TRAY_TAG)

        this.bottomSheetBehavior = bottomSheet?.getBottomSheetBehavior()

        bottomSheet?.dialog?.setOnDismissListener {
            // Todo : On Dismiss Action
        }
    }

    fun close() {
        bottomSheet?.dismiss()
    }

    companion object {
        private const val TRAY_TAG = "GUIDELINE_TRAY_TAG"
    }
}