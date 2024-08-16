package id.co.edtslib.uikit.tray

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.co.edtslib.uikit.databinding.ViewBottomTrayBinding
import id.co.edtslib.uikit.utils.viewBinding


class BottomSheetTray : BottomSheetDialogFragment() {

    val binding by viewBinding<ViewBottomTrayBinding>()

    var title: String? = null
    var customTitleView: View? = null
    var contentView: View? = null

    var dismissOnOutsideTouch: Boolean = true
    var isCancelableOnTouchOutside: Boolean = true
    var customBackgroundColor: Int? = null

    var snapPoints: IntArray = intArrayOf()
    var customAnimationsEnabled: Boolean = true

    var dragHandleVisibility: Boolean = false
    var titleDividerVisibility: Boolean = false
    var shouldShowNavigation: Boolean = false
    var shouldShowClose: Boolean = false

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    // Todo : Delete this line when the override theme is applied

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setCanceledOnTouchOutside(isCancelableOnTouchOutside)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ViewBottomTrayBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomSheetBehavior()

        customTitleView?.let {
            binding.flTitle.removeAllViews()
            binding.flTitle.addView(it)
        } ?: run {
            binding.tvTitle.text = title
            binding.flTitle.isVisible = title != null
        }

        contentView?.let {
            binding.flContent.removeAllViews()
            binding.flContent.addView(it)
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.ivPullBar.isVisible = dragHandleVisibility
        binding.ctaDivider.isVisible = titleDividerVisibility
        binding.ivBack.isVisible = shouldShowNavigation
        binding.ivClose.isVisible = shouldShowClose

        customBackgroundColor?.let {
            binding.root.setBackgroundColor(it)
        }

        // Todo : Corner Radius Workaround
        /*binding.root.background?.let { bg ->
            // Apply corner radius
            bg.mutate().apply {
                binding.root.background = this
                // This is just a placeholder, actual corner radius setting depends on your background drawable type
            }
        }*/

        if (customAnimationsEnabled) {
            setCustomAnimations()
        }
    }

    // Todo : Fix Snapping
    private fun setupBottomSheetBehavior() {
        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.peekHeight = sheet.height
                sheet.parent.parent.requestLayout()
            }
        }

        // bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        bottomSheetBehavior?.apply {
            if (snapPoints.isNotEmpty()) {
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        // Handle state changes if needed
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // Implement snapping behavior
                        val snapPoint = snapPoints.find { it.toFloat() / bottomSheet.height < slideOffset }
                        snapPoint?.let {
                            bottomSheetBehavior?.setPeekHeight(it, true)
                        }
                    }
                })
            }
            isHideable = dismissOnOutsideTouch
        }
    }

    // Todo : Add Custom Animation Implementation
    private fun setCustomAnimations() {
        // dialog?.window?.attributes?.windowAnimations = R.style.CustomBottomSheetAnimation // Define your animations in styles.xml
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Implement any custom logic on dismiss if needed
    }

    companion object {
        fun newInstance(
            title: String? = null,
            customTitleLayout: View? = null,
            contentLayout: View? = null
        ): BottomSheetTray {
            return BottomSheetTray().apply {
                this.title = title
                customTitleLayout?.let { this.customTitleView = customTitleLayout }
                contentLayout?.let { this.contentView = contentLayout }
            }
        }
    }
}