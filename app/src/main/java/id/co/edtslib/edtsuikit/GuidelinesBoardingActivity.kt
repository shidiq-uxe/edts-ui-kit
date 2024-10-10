package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.edtsuikit.databinding.ActivityBoardingBinding
import id.co.edtslib.uikit.boarding.Boarding
import id.co.edtslib.uikit.boarding.BoardingItemAlignment
import id.co.edtslib.uikit.utils.drawable

class GuidelinesBoardingActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityBoardingBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boarding)

        val boardingView = binding.boardingView

        boardingView.autoScrollInterval = 3
        boardingView.items = List(3) {
            Boarding(
                image = drawable(id.co.edtslib.uikit.R.drawable.ill_onboard_example),
                title = "Title $it",
                description = "${getString(R.string.lorem)} $it"
            )
        }

        // Set Horizontal Offset by 50% from the left screen
        // Set Vertical Offset by 60% from the top Screen
        boardingView.contentAlignment = BoardingItemAlignment(
            horizontalAlignmentPercent = 0.5f,
            verticalAlignmentPercent = 0.6f
        )
    }
}