package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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

/** TMH sizing and safe-area policy using the shared tray's dismissal-request contract. */
open class TmhBottomSheet : BottomSheetTray() {

    private var measuredContent: ContentLayout? = null

    protected open fun requestDismiss() {
        closeSheet()
    }

    protected fun closeSheet() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCloseRequested = { requestDismiss() }
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
