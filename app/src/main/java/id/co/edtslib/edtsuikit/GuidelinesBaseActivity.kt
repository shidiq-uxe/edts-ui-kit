package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.uikit.utils.setLightStatusBar
import id.co.edtslib.uikit.utils.window.WindowInsetsConfig

open class GuidelinesBaseActivity : AppCompatActivity() {

    /**
     * Override in child to change insets behavior declaratively.
     *
     * Example:
     *   override val windowInsetsConfig = WindowInsetsConfig.TopBottomOnly
     *   override val windowInsetsConfig = WindowInsetsConfig.None
     *   override val windowInsetsConfig = WindowInsetsConfig.Custom { v, insets ->
     *       val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
     *       v.updatePadding(bottom = bars.bottom)   // bottom-only, preserve existing padding
     *       insets
     *   }
     */
    open val windowInsetsConfig: WindowInsetsConfig = WindowInsetsConfig.SystemBars

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setLightStatusBar()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.customView?.transitionName = "shared_title_<id>"

        postponeEnterTransition()
        startPostponedEnterTransition()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        view?.let { windowInsetsConfig.applyTo(it) }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        windowInsetsConfig.applyTo(contentFrameRoot)
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        view?.let { windowInsetsConfig.applyTo(it) }
    }

    private val contentFrameRoot: View
        get() = (window.decorView as ViewGroup)
            .findViewById<FrameLayout>(android.R.id.content)
            .getChildAt(0)

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}