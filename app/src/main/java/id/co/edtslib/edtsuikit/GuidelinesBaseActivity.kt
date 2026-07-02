package id.co.edtslib.edtsuikit

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.uikit.utils.setActionBarTopScrim
import id.co.edtslib.uikit.utils.setStatusBarIcons
import id.co.edtslib.uikit.utils.setSystemBarScrims
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

    /**
     * Whether edge-to-edge rendering is enabled for this activity.
     *
     * When true, content draws behind the status and navigation bars. This is the
     * default since Material 3 expects edge-to-edge. Override to `false` if you
     * see unwanted shadows bleeding above the action bar (e.g. after upgrading
     * Material library from 1.9.0 to 1.10.0+), or if the screen intentionally
     * uses a solid system bar.
     */
    open val enableEdgeToEdge: Boolean = true

    /**
     * Whether the status bar icons use a light appearance (dark icons on light scrim).
     *
     * Override to `false` when using a dark [statusBarScrimColor] so the system
     * draws light (white) icons instead.
     */
    open val isLightStatusBar: Boolean = true

    /**
     * Background color behind the status bar.
     *
     * On API 35+, [Window.setStatusBarColor] is ignored when edge-to-edge is active.
     * This property supplies the color for an inset-aware scrim view injected behind
     * the content. Override it in child activities for a custom status bar tint.
     *
     * Default: [Color.WHITE].
     */
    @ColorInt
    open val statusBarScrimColor: Int = Color.WHITE

    /**
     * Background color behind the navigation bar, or null for no scrim.
     *
     * Same mechanism as [statusBarScrimColor] but for the bottom system bar.
     */
    @ColorInt
    open val navigationBarScrimColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (enableEdgeToEdge) {
            enableEdgeToEdge()
        }
        super.onCreate(savedInstanceState)
        setStatusBarIcons(isLightStatusBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.customView?.transitionName = "shared_title_<id>"
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        view?.let { windowInsetsConfig.applyTo(it) }
        applySystemBarScrims()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        windowInsetsConfig.applyTo(contentFrameRoot)
        applySystemBarScrims()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        view?.let { windowInsetsConfig.applyTo(it) }
        applySystemBarScrims()
    }

    private val contentFrameRoot: View
        get() = (window.decorView as ViewGroup)
            .findViewById<FrameLayout>(android.R.id.content)
            .getChildAt(0)

    private fun applySystemBarScrims() {
        setSystemBarScrims(
            statusBarColor = statusBarScrimColor.takeIf { it != Color.TRANSPARENT },
            navigationBarColor = navigationBarScrimColor
        )
        setActionBarTopScrim(statusBarScrimColor)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}