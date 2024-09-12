package id.co.edtslib.edtsuikit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import id.co.edtslib.edtsuikit.databinding.BoardingTrayBinding
import id.co.edtslib.uikit.boarding.Boarding
import id.co.edtslib.uikit.boarding.BoardingItemAlignment
import id.co.edtslib.uikit.boarding.IndicatorAlignment
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.marginStart
import id.co.edtslib.uikit.R as UIKitR

class BoardingTray(
    private val context: Context,
    private val fragmentManager: FragmentManager
) {

    private var bottomSheet: BottomSheetTray? = null

    fun show(onClickCallback: (View) -> Unit) {
        val binding = BoardingTrayBinding.inflate(
            LayoutInflater.from(context),
            null, false
        )
        
        val boardingView = binding.boardingView

        boardingView.autoScrollInterval = 3
        binding.boardingView.items = List(3) {
            Boarding(
                image = context.drawable(UIKitR.drawable.ill_onboard_example),
                title = "Title $it",
                description = "${context.getString(R.string.lorem)} $it"
            )
        }

        boardingView.indicatorAlignment = IndicatorAlignment.Start(
            horizontalMargin = context.dimen(id.co.edtslib.uikit.R.dimen.dimen_16)
        )

        boardingView.contentAlignment = BoardingItemAlignment(
            horizontalAlignmentPercent = 0.5f,
            verticalAlignmentPercent = 0.5f
        )

        bottomSheet = BottomSheetTray.newInstance(
            title = context.getString(R.string.test_button_text),
            contentLayout = binding.root
        ).apply {
            dismissOnOutsideTouch = true
            isCancelableOnTouchOutside = true
            customBackgroundColor = context.color(UIKitR.color.white)

            shouldShowNavigation = false
            titleDividerVisibility = true
            shouldShowClose = true

            endIconText = R.string.test_button_text
        }

        binding.btnTest.setOnClickListener {
            onClickCallback(it)
        }

        bottomSheet?.show(fragmentManager, TRAY_TAG)

        bottomSheet?.dialog?.setOnDismissListener {
            // Todo : On Dismiss Action
        }
    }

    fun close() {
        bottomSheet?.dismiss()
    }

    companion object {
        private const val TRAY_TAG = "BOARDING_TRAY_TAG"
    }
}