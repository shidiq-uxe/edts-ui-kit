package id.co.edtslib.uikit.utils.span

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.text.Layout
import android.text.ParcelableSpan
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px

class OrderedSpan(
    private val character: String?,
    @Px private val gapWidth: Int = STANDARD_GAP_WIDTH,
    @ColorInt private val color: Int = STANDARD_COLOR,
    @Px private val textSizePx: Float,
    // Infer wantColor: if a non-standard color is provided, assume it's wanted.
    private val wantColor: Boolean = color != STANDARD_COLOR
) : LeadingMarginSpan, ParcelableSpan {

    companion object {
        const val STANDARD_GAP_WIDTH = 8
        private const val STANDARD_COLOR = 0 // Consider using Color.TRANSPARENT or a specific default

        @JvmField
        val CREATOR: Parcelable.Creator<OrderedSpan> = object : Parcelable.Creator<OrderedSpan> {
            override fun createFromParcel(parcel: Parcel): OrderedSpan {
                return OrderedSpan(parcel)
            }

            override fun newArray(size: Int): Array<OrderedSpan?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Creates a [OrderedSpan] from a parcel.
     * Note: The order of reading from the Parcel MUST match the order of writing in writeToParcel.
     */
    private constructor(src: Parcel) : this(
        character = src.readString(),
        gapWidth = src.readInt(),
        textSizePx = src.readFloat(),
        color = src.readInt(),
        wantColor = src.readInt() != 0
    )

    override fun getSpanTypeId(): Int = 102

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(character)
        dest.writeInt(gapWidth)
        dest.writeFloat(textSizePx)
        dest.writeInt(color)
        dest.writeInt(if (wantColor) 1 else 0)
    }

    private val numberWidth: Int by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSizePx
        paint.measureText(character).toInt() + gapWidth
    }

    override fun getLeadingMargin(first: Boolean): Int = numberWidth

    /**
     * Get the distance, in pixels, between the number and the paragraph.
     */
    fun getGapWidth(): Int = gapWidth

    /**
     * Get the number to display.
     */
    fun getCharacter(): String = character.toString()

    /**
     * Get the number color.
     *
     * @return the number color
     */
    @ColorInt
    fun getColor(): Int = color

    /**
     * Checks if a custom color should be applied.
     */
    fun isColorWanted(): Boolean = wantColor


    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        // Only draw on the first line of the paragraph this span is attached to
        if (first && text is Spanned && text.getSpanStart(this) == start) {
            val originalColor = paint.color
            if (wantColor) {
                paint.color = this.color
            }

            val xPosition = if (dir > 0) { // LTR
                x.toFloat()
            } else { // RTL
                x - paint.measureText(character)
            }

            canvas.drawText(character.toString(), xPosition, baseline.toFloat(), paint)

            if (wantColor) {
                paint.color = originalColor
            }
        }
    }

    override fun toString(): String {
        return "OrderedSpan(character=$character, gapWidth=$gapWidth, textSize=$textSizePx, color=${String.format("#%08X", color)}, wantColor=$wantColor)"
    }
}