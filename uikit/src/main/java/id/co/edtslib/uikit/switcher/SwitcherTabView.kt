package id.co.edtslib.uikit.switcher

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.drawable

internal class SwitcherTabView(context: Context) : LinearLayout(context) {

    private val iconView: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(
            context.dimen(R.dimen.m1).toInt(),
            context.dimen(R.dimen.m1).toInt()
        ).apply {
            topMargin = context.dimen(R.dimen.xxs).toInt()
            marginStart = context.dimen(R.dimen.s).toInt()
            gravity = Gravity.CENTER_VERTICAL
        }
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    private val titleView: TextView = TextView(context).apply {
        setTextAppearance(R.style.TextAppearance_Inter_Semibold_H3)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        gravity = Gravity.CENTER_VERTICAL
    }

    private val subtitleView: TextView = TextView(context).apply {
        setTextAppearance(R.style.TextAppearance_Inter_Regular_B4)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        gravity = Gravity.CENTER_VERTICAL
    }

    private val textContainer: LinearLayout = LinearLayout(context).apply {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            topMargin = context.dimen(R.dimen.xxs).toInt()
            marginStart = context.dimen(R.dimen.xxs).toInt()
            marginEnd = context.dimen(R.dimen.s).toInt()
            bottomMargin = context.dimen(R.dimen.xxs).toInt()
        }
        addView(titleView)
        addView(subtitleView)
    }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        clipChildren = false
        clipToPadding = false

        addView(iconView)
        addView(textContainer)
    }

    fun bind(item: TabItem) {
        item.iconRes?.let { iconView.setImageDrawable(context.drawable(it)) }
        iconView.isVisible = item.iconRes != null

        titleView.text = item.title

        if (!item.subtitle.isNullOrEmpty()) {
            subtitleView.text = item.subtitle
            subtitleView.isVisible = true
        } else {
            subtitleView.isVisible = false
        }
    }

    fun setTextColors(titleColor: Int, subtitleColor: Int, iconColor: Int) {
        titleView.setTextColor(titleColor)
        subtitleView.setTextColor(subtitleColor)
        iconView.setColorFilter(iconColor)
    }

    fun reset() {
        titleView.text = null
        subtitleView.text = null
        iconView.setImageDrawable(null)
        iconView.setColorFilter(null)
    }
}
