package id.co.edtslib.uikit.tray

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewBottomTrayBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.viewBinding


class BottomSheetTray : BottomSheetDialogFragment() {

    val binding by viewBinding<ViewBottomTrayBinding>()

    var delegate: BottomTrayDelegate? = null

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

    // Title View Override
    @StyleRes
    var titleTextAppearance: Int? = R.style.TextAppearance_Inter_Semibold_H1
    @ColorRes
    var titleTextColor: Int? = R.color.black_70
    @DrawableRes
    var endIcon: Int? = R.drawable.ic_cancel_24
    @ColorRes
    var endIconTint: Int? = R.color.black_50
    @StringRes
    var endIconText: Int? = null
    @DrawableRes
    var navigationIcon: Int? = R.drawable.ic_arrow_left
    @ColorRes
    var navigationIconTint: Int? = R.color.black_50

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    fun getBottomSheetBehavior(): BottomSheetBehavior<View>? {
        return bottomSheetBehavior
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_EDTS_UIKit_BottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { _ ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetBehavior = bottomSheet?.let { sheet -> BottomSheetBehavior.from(sheet) }

            bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    delegate?.onStateChanged(bottomSheet, newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    delegate?.onSlide(bottomSheet, slideOffset)
                }
            })
        }

        return dialog
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

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.dragHandleView.isVisible = dragHandleVisibility
        binding.ctaDivider.isVisible = titleDividerVisibility
        binding.btnNavigation.isVisible = shouldShowNavigation
        binding.btnClose.isVisible = shouldShowClose

        bindTitleView()

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

    private val bottomSheetTrayBehavior = object : BottomSheetBehavior.BottomSheetCallback() {
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
    }

    // Todo : Fix Snapping
    private fun setupBottomSheetBehavior() {
        bottomSheetBehavior?.apply {
            if (snapPoints.isNotEmpty()) {
                addBottomSheetCallback(bottomSheetTrayBehavior)
            }
            isHideable = dismissOnOutsideTouch
        }
    }

    // Todo : Add Custom Animation Implementation
    private fun setCustomAnimations() {
        // dialog?.window?.attributes?.windowAnimations = R.style.CustomBottomSheetAnimation // Define your animations in styles.xml
    }

    override fun onDismiss(dialog: DialogInterface) {
        delegate?.onDismiss(dialog)

        super.onDismiss(dialog)
        // Implement any custom logic on dismiss if needed
    }

    private fun bindTitleView() {
        titleTextAppearance?.let { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.tvTitle.setTextAppearance(it)
        } else {
            binding.tvTitle.setTextAppearance(requireContext(), it)
        }}
        titleTextColor?.let { binding.tvTitle.setTextColor(context.color(it)) }

        binding.btnNavigation.icon = navigationIcon?.let { context?.drawable(it) }
        binding.btnNavigation.iconTint = navigationIconTint?.let { context?.colorStateList(it) }

        binding.btnClose.icon = endIcon?.let { context?.drawable(it) }
        binding.btnClose.iconTint = endIconTint?.let { context?.colorStateList(it) }

        binding.btnClose.text = endIconText?.let { context?.getString(it) }
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