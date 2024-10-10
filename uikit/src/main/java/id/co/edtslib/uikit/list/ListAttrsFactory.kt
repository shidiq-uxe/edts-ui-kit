package id.co.edtslib.uikit.list

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.px

object ListAttrsFactory {
    fun initAttrs(context: Context, attrs: AttributeSet?, options: ListOptions) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListItem)

        options.apply {
            titleText = typedArray.getString(R.styleable.ListItem_titleText) ?: ""
            titleTextAppearance = typedArray.getResourceId(R.styleable.ListItem_titleTextAppearance, 0)
            titleTextColor = typedArray.getColor(R.styleable.ListItem_titleTextColor, context.color(R.color.black_70))
            isTitleVisible = typedArray.getBoolean(R.styleable.ListItem_titleVisible, true)
            subtitleText = typedArray.getString(R.styleable.ListItem_subTitleText)
            subtitleTextAppearance = typedArray.getResourceId(R.styleable.ListItem_subtitleTextAppearance, 0)
            subtitleTextColor = typedArray.getColor(R.styleable.ListItem_subtitleTextColor, context.color(R.color.black_50))
            isSubtitleVisible = typedArray.getBoolean(R.styleable.ListItem_subtitleVisible, true)
            startIcon = typedArray.getDrawable(R.styleable.ListItem_startIcon)
            startIconTint = typedArray.getColor(R.styleable.ListItem_startIconTint, Color.TRANSPARENT)
            isStartIconVisible = typedArray.getBoolean(R.styleable.ListItem_startIconVisible, true)
            startIconSize = typedArray.getDimension(R.styleable.ListItem_startIconSize, 0f)
            startIconGravity = typedArray.getInt(R.styleable.ListItem_startIconGravity, 1)
            endIcon = typedArray.getDrawable(R.styleable.ListItem_endIcon)
            endIconTint = typedArray.getColor(R.styleable.ListItem_endIconTint, Color.TRANSPARENT)
            isEndIconVisible = typedArray.getBoolean(R.styleable.ListItem_endIconVisible, true)
            endIconSize = typedArray.getDimension(R.styleable.ListItem_endIconSize, 0f)
            endIconGravity = typedArray.getInt(R.styleable.ListItem_endIconGravity, 1)
            cornerRadius = typedArray.getDimension(R.styleable.ListItem_cornerRadius, 0f)
            backgroundTint = typedArray.getColor(R.styleable.ListItem_backgroundColor, Color.WHITE)
            cardElevation = typedArray.getDimension(R.styleable.ListItem_cardElevation, 0f)
            strokeColor = typedArray.getColor(R.styleable.ListItem_strokeColor, context.color(R.color.black_30))
            strokeWidth = typedArray.getDimension(R.styleable.ListItem_strokeWidth, 0f)
        }.also {
            typedArray.recycle()
        }
    }

}