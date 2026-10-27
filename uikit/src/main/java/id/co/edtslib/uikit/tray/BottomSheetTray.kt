package id.co.edtslib.uikit.tray

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewBottomTrayBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.viewBinding


open class BottomSheetTray : BottomSheetDialogFragment() {

    val binding by viewBinding<ViewBottomTrayBinding>()

    var delegate: BottomTrayDelegate? = null

    /**
     * Receives Close, Back, outside-tap and swipe dismissal requests. When set, the host
     * decides whether to call [dismiss]. Programmatic dismissal bypasses this callback.
     * Null preserves the default dismissal behavior.
     */
    var onCloseRequested: (() -> Unit)? = null
        set(value) {
            field = value
            closeBackCallback?.isEnabled = value != null
        }

    var title: String? = null
    var customTitleView: View? = null
    var contentView: View? = null

    var dismissOnOutsideTouch: Boolean = true
    var isCancelableOnTouchOutside: Boolean = true
    var customBackgroundColor: Int? = null

    var snapPoints: IntArray = intArrayOf()

    /** Slides the opaque sheet fully offscreen like drag dismissal. False retains Material window motion. */
    var customAnimationsEnabled: Boolean = true
        set(value) {
            field = value
            (dialog as? BottomSheetDialog)?.configureTrayMotion(value)
        }

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
    private var closeBackCallback: OnBackPressedCallback? = null

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

    private fun interceptCloseRequest(): Boolean {
        val callback = onCloseRequested ?: return false
        callback()
        return true
    }

    fun getBottomSheetBehavior(): BottomSheetBehavior<View>? {
        return bottomSheetBehavior
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

    private fun prepareDismissMotion() {
        (dialog as? BottomSheetDialog)?.let {
            it.configureTrayMotion(customAnimationsEnabled)
            if (it.dismissWithAnimation) {
                // Explicit dismissal is allowed even when dragging or outside cancellation is disabled.
                it.behavior.isHideable = true
            }
        }
    }

    private fun bindTitleView() {
        titleTextAppearance?.let { binding.tvTitle.setTextAppearance(it) }
        titleTextColor?.let { binding.tvTitle.setTextColor(context.color(it)) }

        binding.btnNavigation.buttonDrawable = navigationIcon?.let { context?.drawable(it) }
        binding.btnNavigation.buttonTintList = navigationIconTint?.let { context?.colorStateList(it) }

        binding.btnClose.buttonDrawable = endIcon?.let { context?.drawable(it) }
        binding.btnClose.buttonTintList = endIconTint?.let { context?.colorStateList(it) }

        binding.btnClose.text = endIconText?.let { context?.getString(it) }
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_EDTS_UIKit_BottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = TrayDialog(
            context = requireContext(),
            theme = theme,
            isCustomMotionEnabled = { customAnimationsEnabled },
            shouldAnimateDismiss = {
                viewLifecycleOwnerLiveData.value?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true
            },
            interceptCloseRequest = ::interceptCloseRequest,
        )
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetBehavior = bottomSheet?.let { sheet -> BottomSheetBehavior.from(sheet) }

            setupBottomSheetBehavior()
            dialog.setCanceledOnTouchOutside(isCancelableOnTouchOutside)

            delegate?.onShow(dialogInterface)

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

    open override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ViewBottomTrayBinding.inflate(inflater, container, false).root

    open override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            if (!interceptCloseRequest()) dismiss()
        }

        binding.dragHandleView.isVisible = dragHandleVisibility
        binding.ctaDivider.isVisible = titleDividerVisibility
        binding.btnNavigation.isVisible = shouldShowNavigation
        binding.btnClose.isVisible = shouldShowClose

        bindTitleView()
        binding.root.doOnLayout {
            if (customTitleView == null) {
                binding.root.touchDelegate = TrayActionTouchDelegate(
                    binding.root, listOf(binding.btnNavigation, binding.btnClose)
                )
            }
        }

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

    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.configureTrayMotion(customAnimationsEnabled)
        closeBackCallback?.remove()
        closeBackCallback = object : OnBackPressedCallback(onCloseRequested != null) {
            override fun handleOnBackPressed() {
                if (isCancelable && !interceptCloseRequest()) dismiss()
            }
        }.also { callback ->
            // Consume Back in the dialog window, including when the host vetoes dismissal.
            (dialog as? BottomSheetDialog)?.onBackPressedDispatcher?.addCallback(this, callback)
        }
    }

    override fun onDestroyView() {
        closeBackCallback?.remove()
        closeBackCallback = null
        bottomSheetBehavior = null
        super.onDestroyView()
    }

    override fun dismiss() {
        prepareDismissMotion()
        super.dismiss()
    }

    override fun dismissAllowingStateLoss() {
        prepareDismissMotion()
        super.dismissAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        delegate?.onDismiss(dialog)

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
