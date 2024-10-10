package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.uikit.utils.setLightStatusBar

open class GuidelinesBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.customView?.transitionName = "shared_title_<id>"

        postponeEnterTransition()
        startPostponedEnterTransition()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()

        return true
    }
}