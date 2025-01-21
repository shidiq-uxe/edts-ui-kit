package id.co.edtslib.uikit.ribbon

import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import id.co.edtslib.uikit.utils.px

class RibbonOutlineProvider(private val ribbon: Ribbon) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val path = if (ribbon.gravity == Ribbon.Gravity.START) {
            ribbon.drawStartContainer()
        } else {
            ribbon.drawEndContainer()
        }

        val paddedPath = Path(path)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outline.setPath(paddedPath)
        } else {
            outline.setConvexPath(paddedPath)
        }
    }
}
