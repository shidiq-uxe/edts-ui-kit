package id.co.edtslib.uikit.coachmark

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

class NotchTriangleEdgeTreatment(
    private val triangleWidth: Float,         // full width of the triangle notch
    private val triangleHeight: Float,        // height of the triangle notch
    private val triangleOffset: Float,        // horizontal offset from the left edge (in pixels)
    private val roundedCornerRadius: Float,   // radius for rounding the tip edges
    private val isEdgeAtTop: Boolean = true   // if true, notch points upward (for top edge); else downward
) : EdgeTreatment() {

    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath
    ) {
        // Calculate half the notch width.
        val halfWidth = triangleWidth / 2f

        // Determine the start and end positions for the notch on the edge.
        val notchStart = (triangleOffset - halfWidth).coerceAtLeast(0f)
        val notchEnd = (triangleOffset + halfWidth).coerceAtMost(length)

        // Draw the edge from the start until the notch begins.
        shapePath.lineTo(notchStart, 0f)

        if (isEdgeAtTop) {
            // For a notch on the top edge, the tip goes upward (negative Y direction).
            // Left curve of the notch.
            shapePath.cubicToPoint(
                notchStart + roundedCornerRadius, 0f,
                triangleOffset - roundedCornerRadius, -triangleHeight,
                triangleOffset, -triangleHeight
            )
            // Right curve of the notch.
            shapePath.cubicToPoint(
                triangleOffset + roundedCornerRadius, -triangleHeight,
                notchEnd - roundedCornerRadius, 0f,
                notchEnd, 0f
            )
        } else {
            // For a notch on the bottom edge, the tip goes downward (positive Y direction).
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

        // Continue drawing the rest of the edge.
        shapePath.lineTo(length, 0f)
    }
}
