package id.co.edtslib.edtsuikit

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesBottomTrayBinding
import id.co.edtslib.edtsuikit.databinding.ViewMotionTrayContentBinding
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.stepper.StepperButtonTouchTargetMode
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.tray.BottomTrayDelegate

class GuidelinesBottomTrayActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesBottomTrayBinding>()

    private fun configureStepper(stepper: StepperButton, editable: Boolean = false) {
        stepper.setAllowTextEditing(editable)
        stepper.buttonTouchTargetMode = StepperButtonTouchTargetMode.EXPANDED
        stepper.isAlwaysExpanded = true
        stepper.isDragRepeatEnabled = true
        stepper.setMaxValue(12)
        stepper.setCount(8, animate = false)
    }

    private fun showBottomSheet(useCustomMotion: Boolean) {
        if (supportFragmentManager.findFragmentByTag(TRAY_TAG) != null) return
        val content = ViewMotionTrayContentBinding.inflate(layoutInflater)
        val tray = BottomSheetTray.newInstance(
            title = getString(R.string.motion_tray_title),
            contentLayout = content.root
        ).apply {
            customAnimationsEnabled = useCustomMotion
            dragHandleVisibility = true
            shouldShowClose = true
            shouldShowNavigation = true
            titleDividerVisibility = true
            delegate = object : BottomTrayDelegate {
                override fun onShow(dialogInterface: DialogInterface) {
                    this@GuidelinesBottomTrayActivity.binding.tvBSBehavior.setText(R.string.motion_tray_shown)
                }

                override fun onDismiss(dialogInterface: DialogInterface) {
                    this@GuidelinesBottomTrayActivity.binding.tvBSBehavior.setText(R.string.motion_tray_dismissed)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    this@GuidelinesBottomTrayActivity.binding.tvBSBehavior.text = getString(R.string.motion_tray_state, newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            }
        }
        content.btnDismissTray.setOnClickListener { tray.dismiss() }
        tray.show(supportFragmentManager, TRAY_TAG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_bottom_tray)
        configureStepper(binding.stepperAnimated)
        configureStepper(binding.stepperInstant)
        binding.stepperInstant.isCountAnimationEnabled = false
        configureStepper(binding.stepperEditable, editable = true)
        configureStepper(binding.stepperDisabled)
        binding.stepperDisabled.isEnabled = false
        binding.stepperCollapsed.setAllowTextEditing(false)
        binding.stepperCollapsed.buttonTouchTargetMode = StepperButtonTouchTargetMode.EXPANDED
        binding.stepperCollapsed.setMaxValue(12)
        binding.btnShowBottomSheet.setOnClickListener { showBottomSheet(useCustomMotion = true) }
        binding.btnShowMaterialSheet.setOnClickListener { showBottomSheet(useCustomMotion = false) }
    }

    companion object {
        private const val TRAY_TAG = "component_motion_tray"
    }
}
