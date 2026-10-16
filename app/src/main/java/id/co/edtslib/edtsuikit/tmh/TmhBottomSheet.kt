package id.co.edtslib.edtsuikit.tmh

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.uikit.tray.BottomSheetTray

/** App-local dismissal interception; shared tray behavior remains unchanged. */
open class TmhBottomSheet : BottomSheetTray() {

    private var shouldAllowDismiss = false
    private var dismissBackCallback: OnBackPressedCallback? = null
    private var measuredContent: ContentLayout? = null

    protected open fun requestDismiss() {
        closeSheet()
    }

    protected fun closeSheet() {
        shouldAllowDismiss = true
        dismiss()
    }

    protected fun restoreExpandedState() {
        val sheetDialog = dialog as? BottomSheetDialog ?: return
        sheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    protected fun refreshSheetSize() {
        view?.post { if (isAdded && view != null) sizeSheet() }
    }

    private fun sizeSheet() {
        val sheetDialog = dialog as? BottomSheetDialog ?: return
        val maximumHeight = (requireActivity().window.decorView.height * MAX_HEIGHT_FRACTION).toInt()
        if (maximumHeight <= 0) return
        // Cap normal framework measurement; never measure the attached text hierarchy here.
        measuredContent?.maximumHeight = (maximumHeight - binding.flContent.top).coerceAtLeast(0)
        sheetDialog.behavior.maxHeight = maximumHeight
        sheetDialog.behavior.isFitToContents = true
        sheetDialog.behavior.skipCollapsed = true
        sheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun getTheme(): Int = R.style.ThemeOverlay_Tmh_BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun cancel() {
                if (shouldAllowDismiss) super.cancel() else requestDismiss()
            }
        }.apply {
            setCanceledOnTouchOutside(true)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dragHandleVisibility = true
        titleDividerVisibility = true
        shouldShowClose = true
        titleTextAppearance = R.style.TextAppearance_Tmh_Semibold_H1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val content = contentView ?: return
        measuredContent = ContentLayout(requireContext()).apply {
            addView(content, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        contentView = measuredContent
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.contentDescription = getString(R.string.tmh_close)
        binding.btnClose.setOnClickListener { requestDismiss() }
        binding.root.tmhTypography()
        val insetTarget = content.findViewById<View>(R.id.footer) ?: content
        val initialBottomPadding = insetTarget.paddingBottom
        val initialLeftPadding = binding.root.paddingLeft
        val initialRightPadding = binding.root.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val safeArea = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            binding.root.updatePadding(
                left = initialLeftPadding + safeArea.left,
                right = initialRightPadding + safeArea.right,
            )
            insetTarget.updatePadding(bottom = initialBottomPadding + safeArea.bottom)
            refreshSheetSize()
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        dismissBackCallback?.remove()
        dismissBackCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = requestDismiss()
        }.also { callback ->
            // Consume Back in this window so it cannot also pop the underlying PPP Activity.
            (dialog as? BottomSheetDialog)?.onBackPressedDispatcher?.addCallback(this, callback)
        }
        dialog?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
        }
        ViewCompat.requestApplyInsets(binding.root)
        binding.root.doOnLayout { sizeSheet() }
    }

    override fun onDestroyView() {
        dismissBackCallback?.remove()
        dismissBackCallback = null
        measuredContent = null
        contentView = null
        super.onDestroyView()
    }

    private class ContentLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {

        var maximumHeight = Int.MAX_VALUE
            set(value) {
                if (field == value) return
                field = value
                requestLayout()
            }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val mode = MeasureSpec.getMode(heightMeasureSpec)
            val height = if (mode == MeasureSpec.UNSPECIFIED) maximumHeight
            else MeasureSpec.getSize(heightMeasureSpec).coerceAtMost(maximumHeight)
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST))
        }
    }

    companion object {
        const val MAX_HEIGHT_FRACTION = 0.9f
    }
}
