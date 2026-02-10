package id.co.edtslib.uikit.coachmark

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

class NotchTriangleEdgeTreatment(
    private val triangleWidth: Float,
    private val triangleHeight: Float,
    private val triangleOffset: Float,
    private val roundedCornerRadius: Float,
    private val isEdgeAtTop: Boolean = true
) : EdgeTreatment() {

    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath
    ) {
        val halfWidth = triangleWidth / 2f

        val notchStart = (triangleOffset - halfWidth).coerceAtLeast(0f)
        val notchEnd = (triangleOffset + halfWidth).coerceAtMost(length)

        shapePath.lineTo(notchStart, 0f)

        if (isEdgeAtTop) {
            shapePath.cubicToPoint(
                notchStart + roundedCornerRadius, 0f,
                triangleOffset - roundedCornerRadius, -triangleHeight,
                triangleOffset, -triangleHeight
            )
            shapePath.cubicToPoint(
                triangleOffset + roundedCornerRadius, -triangleHeight,
                notchEnd - roundedCornerRadius, 0f,
                notchEnd, 0f
            )
        } else {
            shapePath.cubicToPoint(
                notchStart + roundedCornerRadius, 0f,
                triangleOffset - roundedCornerRadius, triangleHeight,
                triangleOffset, triangleHeight
            )
            shapePath.cubicToPoint(
                triangleOffset + roundedCornerRadius, triangleHeight,
                notchEnd - roundedCornerRadius, 0f,
                notchEnd, 0f
            )
        }

        shapePath.lineTo(length, 0f)
    }
}
