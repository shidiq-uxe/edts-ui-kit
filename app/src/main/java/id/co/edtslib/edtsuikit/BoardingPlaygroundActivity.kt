package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityBoardingPlaygroundBinding
import id.co.edtslib.edtsuikit.databinding.ViewOnBoardingBinding
import id.co.edtslib.uikit.boarding.Boarding
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater

class BoardingPlaygroundActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityBoardingPlaygroundBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_boarding_playground)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val boardingPagerBinding = ViewOnBoardingBinding.inflate(inflater, binding.flContent, true)

        boardingPagerBinding.boardingPagerView.items = List(3) {
            Boarding(
                image = drawable(id.co.edtslib.uikit.R.drawable.ill_onboard_example),
                title = "Title $it",
                description = getString(R.string.lorem)
            )
        }
    }
}