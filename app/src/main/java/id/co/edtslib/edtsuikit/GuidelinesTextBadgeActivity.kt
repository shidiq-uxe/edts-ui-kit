package id.co.edtslib.edtsuikit

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesTextBadgeBinding
import id.co.edtslib.uikit.badge.TextBadge.BadgeShape
import id.co.edtslib.uikit.badge.TextBadge.BadgeSize
import id.co.edtslib.uikit.utils.color
import androidx.core.graphics.toColorInt

class GuidelinesTextBadgeActivity : AppCompatActivity() {
    private val binding by viewBinding<ActivityGuidelinesTextBadgeBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupBackgroundShape()
        setupBackgroundSize()
        setupWidthRelative()
    }

    private fun setupBackgroundShape() = with(binding) {
        badgeAppearanceSemiBold.apply {
            text = "Status"
            badgeShape = BadgeShape.STATUS
            iconVisible = true
        }
        badgeAppearanceRegular.apply {
            text = "Highlight"
            iconVisible = true
            setGradientBackground(
                intArrayOf(
                    "#6E8BCF".toColorInt(),
                    "#17C12E".toColorInt()
                ),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            textColor = context.color(R.color.white)
            iconColor = context.color(R.color.white)
            badgeShape = BadgeShape.HIGHLIGHT
        }

        badgeAppearanceSemiBoldDisabled.apply {
            text = "Status Disabled"
            badgeShape = BadgeShape.STATUS
            isEnabled = false
            iconVisible = true
            iconColor = context.color(R.color.white)
        }

        badgeAppearanceRegularDisabled.apply {
            text = "Highlight Disabled"
            iconVisible = true
            setGradientBackground(
                intArrayOf(
                    "#6E8BCF".toColorInt(),
                    "#17C12E".toColorInt()
                ),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            textColor = context.color(R.color.white)
            iconColor = context.color(R.color.white)
            badgeShape = BadgeShape.HIGHLIGHT
            isEnabled = false
        }
    }

    private fun setupBackgroundSize() = with(binding) {
        badgeSizeSmall.apply {
            text = "Small"
            iconVisible = true
            badgeSize = BadgeSize.SMALL
        }
        badgeSizeMedium.apply {
            text = "Medium"
            iconVisible = true
            badgeSize = BadgeSize.MEDIUM
        }
        badgeSizeLarge.apply {
            text = "Large"
            iconVisible = true
            badgeSize = BadgeSize.LARGE
        }
    }

    private fun setupWidthRelative() = with(binding){
        badgeRelativeWidthWrapContent.apply {
            text = "Wrap Content - Null Width"
            relativeWidth = null
        }

        badgeRelativeWidth25Percent.apply {
            text = "25% Width"
            relativeWidth = 25
        }

        badgeRelativeWidth50Percent.apply {
            text = "50% Width"
            relativeWidth = 50
        }

        badgeRelativeWidth75Percent.apply {
            text = "75% Width"
            relativeWidth = 75
        }

        badgeRelativeWidthMatchParent.apply {
            text = "Match Parent - 100% Width"
            relativeWidth = 100
        }
    }
}