package id.co.edtslib.uikit.boarding

sealed class IndicatorAlignment (
    val marginStart: Float = 0f,
    val marginEnd: Float = 0f
) {
    data class Start(
        val horizontalMargin: Float = 0f
    ): IndicatorAlignment(horizontalMargin, horizontalMargin)

    data class Center(
        val startMargin: Float = 0f,
        val endMargin: Float = 0f
    ): IndicatorAlignment(startMargin, endMargin)

    data class End(
        val horizontalMargin: Float = 0f
    ): IndicatorAlignment(horizontalMargin, horizontalMargin)

    companion object {
        val entries = listOf(
            IndicatorAlignment.Start(),
            IndicatorAlignment.Center(),
            IndicatorAlignment.End()
        )
    }
}
