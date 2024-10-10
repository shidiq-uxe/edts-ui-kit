package id.co.edtslib.edtsuikit

import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesBottomTrayBinding
import id.co.edtslib.edtsuikit.databinding.LayoutGuidelinesBottomSheetItemBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.databinding.ListBinding
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.tray.BottomTrayDelegate
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.px


class GuidelinesBottomTrayActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesBottomTrayBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_bottom_tray)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnShowBottomSheet.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        GuidelinesBottomTray(this, supportFragmentManager).apply {
            this.bottomSheetDelegate = bottomTrayDelegate

            this.bottomSheetBehavior?.halfExpandedRatio = 0.2f
            this.bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            this.bottomSheetBehavior?.saveFlags = BottomSheetBehavior.SAVE_ALL
        }.show()
    }

    private val bottomTrayDelegate = object : BottomTrayDelegate {
        override fun onDismiss(dialogInterface: DialogInterface) {
            Log.e(DIALOG_TAG, "Dismissed")
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val stateName = when (newState) {
                BottomSheetBehavior.STATE_DRAGGING -> "State Dragging"
                BottomSheetBehavior.STATE_EXPANDED -> "State Expanded"
                BottomSheetBehavior.STATE_COLLAPSED -> "State Collapsed"
                BottomSheetBehavior.STATE_HALF_EXPANDED -> "State Half Expanded"
                BottomSheetBehavior.STATE_HIDDEN -> "State Hidden"
                BottomSheetBehavior.STATE_SETTLING -> "State Settling"
                else -> "State Unknown"
            }

            binding.tvBSBehavior.text = stateName
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            Handler().postDelayed(500L) {
                Log.e(DIALOG_TAG, "Slide Offset : $slideOffset")
            }


        }
    }

    companion object {
        private const val DIALOG_TAG = "GUIDELINE_TRAY_TAG"
    }
}