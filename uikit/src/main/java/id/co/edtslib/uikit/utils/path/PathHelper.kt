package id.co.edtslib.uikit.utils.path

import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi

private data class P(val x: Float, val y: Float)

fun pathToConvexPath(path: Path, sampleStep: Float = 4f): Path {
    val pts = ArrayList<P>()
    val pm = PathMeasure(path, false)
    var done = false
    while (!done) {
        val len = pm.length
        if (len > 0f) {
            var d = 0f
            val pos = FloatArray(2)
            while (d < len) {
                pm.getPosTan(d, pos, null)
                pts.add(P(pos[0], pos[1]))
                d += sampleStep
            }
            pm.getPosTan(len, pos, null)
            pts.add(P(pos[0], pos[1]))
        }
        done = !pm.nextContour()
    }

    if (pts.size < 3) {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val p = Path()
        p.addRect(bounds, Path.Direction.CW)
        return p
    }

    pts.sortWith(compareBy<P> { it.x }.thenBy { it.y })
    val lower = ArrayList<P>()
    val upper = ArrayList<P>()

    fun cross(a: P, b: P, c: P): Float {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
    }

    for (pt in pts) {
        while (lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], pt) <= 0f) {
            lower.removeAt(lower.size - 1)
        }
        lower.add(pt)
    }
    for (i in pts.size - 1 downTo 0) {
        val pt = pts[i]
        while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], pt) <= 0f) {
            upper.removeAt(upper.size - 1)
        }
        upper.add(pt)
    }

    lower.removeAt(lower.size - 1)
    upper.removeAt(upper.size - 1)
    val hull = ArrayList<P>(lower.size + upper.size)
    hull.addAll(lower)
    hull.addAll(upper)

    val convex = Path()
    hull.firstOrNull()?.let {
        convex.moveTo(it.x, it.y)
        for (i in 1 until hull.size) {
            val hp = hull[i]
            convex.lineTo(hp.x, hp.y)
        }
        convex.close()
    }

    return convex
}