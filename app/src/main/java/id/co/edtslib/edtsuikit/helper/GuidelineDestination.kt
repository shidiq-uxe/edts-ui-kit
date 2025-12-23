package id.co.edtslib.edtsuikit.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import id.co.edtslib.edtsuikit.GuidelineCoachmarkActivity
import id.co.edtslib.edtsuikit.GuidelinePDPActivity
import id.co.edtslib.edtsuikit.GuidelinesAlertBoxActivity
import id.co.edtslib.edtsuikit.GuidelinesAnimatedCardViewActivity
import id.co.edtslib.edtsuikit.GuidelinesBoardingActivity
import id.co.edtslib.edtsuikit.GuidelinesBottomTrayActivity
import id.co.edtslib.edtsuikit.GuidelinesButtonActivity
import id.co.edtslib.edtsuikit.GuidelinesCartActivity
import id.co.edtslib.edtsuikit.GuidelinesColorActivity
import id.co.edtslib.edtsuikit.GuidelinesCouponPromotionActivity
import id.co.edtslib.edtsuikit.GuidelinesDiscountRedemptionActivity
import id.co.edtslib.edtsuikit.GuidelinesGreyScaleViewGroupActivity
import id.co.edtslib.edtsuikit.GuidelinesHTMLTextViewActivity
import id.co.edtslib.edtsuikit.GuidelinesHomeSwitcherActivity
import id.co.edtslib.edtsuikit.GuidelinesHomepageExploration
import id.co.edtslib.edtsuikit.GuidelinesListItemActivity
import id.co.edtslib.edtsuikit.GuidelinesOtpActivity
import id.co.edtslib.edtsuikit.GuidelinesPopupActivity
import id.co.edtslib.edtsuikit.GuidelinesProgressBarActivity
import id.co.edtslib.edtsuikit.GuidelinesSearchProductActivity
import id.co.edtslib.edtsuikit.GuidelinesSearchbarActivity
import id.co.edtslib.edtsuikit.GuidelinesSegmentedTabLayoutActivity
import id.co.edtslib.edtsuikit.GuidelinesSnackbarActivity
import id.co.edtslib.edtsuikit.GuidelinesTextfieldActivity
import id.co.edtslib.edtsuikit.GuidelinesTypographyActivity
import id.co.edtslib.edtsuikit.HtmlListDemoActivity
import id.co.edtslib.edtsuikit.PoinkuResearchActivity
import id.co.edtslib.edtsuikit.R

sealed class GuidelineItem(
    @StringRes val titleRes: Int,
    val activityClass: Class<out Activity>
) {
    // Basic Components
    object Typography : GuidelineItem(R.string.guidelines_typography, GuidelinesTypographyActivity::class.java)

    object Button : GuidelineItem(R.string.guidelines_button, GuidelinesButtonActivity::class.java)
    object Textfield : GuidelineItem(R.string.guidelines_textfield, GuidelinesTextfieldActivity::class.java)

    object Color : GuidelineItem(R.string.guidelines_color, GuidelinesColorActivity::class.java)

    // Input Components
    object Otp : GuidelineItem(R.string.guidelines_otp, GuidelinesOtpActivity::class.java)
    object Searchbar : GuidelineItem(R.string.guidelines_searchbar, GuidelinesSearchbarActivity::class.java)

    // Feedback Components
    object AlertBox : GuidelineItem(R.string.guidelines_alert_box, GuidelinesAlertBoxActivity::class.java)

    object Snackbar : GuidelineItem(R.string.guidelines_snackbar, GuidelinesSnackbarActivity::class.java)

    object Popup : GuidelineItem(R.string.guidelines_popup, GuidelinesPopupActivity::class.java)

    // Layout Components
    object Boarding : GuidelineItem(R.string.guidelines_boarding, GuidelinesBoardingActivity::class.java)

    object ListItem : GuidelineItem(R.string.guidelines_list_item, GuidelinesListItemActivity::class.java)

    object BottomTray : GuidelineItem(R.string.guidelines_bottom_tray, GuidelinesBottomTrayActivity::class.java)

    object SegmentedTab : GuidelineItem(R.string.guidelines_segmented_tab, GuidelinesSegmentedTabLayoutActivity::class.java)

    // Progress & Loading
    object ProgressBar : GuidelineItem(R.string.guidelines_progress_bar, GuidelinesProgressBarActivity::class.java)

    // Navigation & Home
    object HomeSwitcher : GuidelineItem(R.string.guidelines_home_switcher, GuidelinesHomeSwitcherActivity::class.java)

    object HomepageExploration : GuidelineItem(R.string.guidelines_homepage_exploration, GuidelinesHomepageExploration::class.java)

    // User Guidance
    object Coachmark : GuidelineItem(R.string.guidelines_coachmark, GuidelineCoachmarkActivity::class.java)

    // Shopping Features
    object Cart : GuidelineItem(R.string.guidelines_cart, GuidelinesCartActivity::class.java)
    object DiscountRedemption : GuidelineItem(R.string.guidelines_discount_redemption, GuidelinesDiscountRedemptionActivity::class.java)

    // Advanced Components
    object AnimatedCard : GuidelineItem(R.string.guidelines_animated_card, GuidelinesAnimatedCardViewActivity::class.java)

    object GreyScaleViewGroup : GuidelineItem(R.string.guidelines_greyscale_viewgroup, GuidelinesGreyScaleViewGroupActivity::class.java)

    object HtmlTextView : GuidelineItem(R.string.guidelines_html_textview, GuidelinesHTMLTextViewActivity::class.java)

    // Research & Demo
    object Research : GuidelineItem(R.string.guidelines_research, PoinkuResearchActivity::class.java)

    object HtmlListDemo : GuidelineItem(R.string.guidelines_html_list_demo, HtmlListDemoActivity::class.java)

    object SearchProduct : GuidelineItem(R.string.guidelines_search_product_demo, GuidelinesSearchProductActivity::class.java)

    object PDP : GuidelineItem(R.string.guidelines_pdp, GuidelinePDPActivity::class.java)

    object CouponPromotion : GuidelineItem(R.string.guidelines_coupon_ongkir_promotion, GuidelinesCouponPromotionActivity::class.java)

    companion object {
        fun getAllItems(): List<GuidelineItem> = listOf(
            Typography,
            Button,
            Textfield,
            Color,
            Otp,
            Searchbar,
            AlertBox,
            Boarding,
            ListItem,
            Snackbar,
            BottomTray,
            Research,
            ProgressBar,
            Popup,
            SegmentedTab,
            HomeSwitcher,
            HomepageExploration,
            Coachmark,
            Cart,
            DiscountRedemption,
            AnimatedCard,
            GreyScaleViewGroup,
            HtmlTextView,
            HtmlListDemo,
            SearchProduct,
            PDP,
            CouponPromotion,
        )

        fun getByCategory(): Map<String, List<GuidelineItem>> = mapOf(
            "Basic Components" to listOf(
                Typography,
                Button,
                Textfield,
                Color
            ),
            "Input Components" to listOf(
                Otp,
                Searchbar
            ),
            "Feedback Components" to listOf(
                AlertBox,
                Snackbar,
                Popup
            ),
            "Layout Components" to listOf(
                Boarding,
                ListItem,
                BottomTray,
                SegmentedTab
            ),
            "Progress & Loading" to listOf(
                ProgressBar
            ),
            "Navigation & Home" to listOf(
                HomeSwitcher,
                HomepageExploration
            ),
            "User Guidance" to listOf(
                Coachmark
            ),
            "Shopping Features" to listOf(
                Cart,
                DiscountRedemption
            ),
            "Advanced Components" to listOf(
                AnimatedCard,
                GreyScaleViewGroup,
                HtmlTextView
            ),
            "Research & Demo" to listOf(
                Research,
                HtmlListDemo,
                PDP,
            )
        )
    }

    fun navigate(context: Context, options: Bundle? = null) {
        context.startActivity(Intent(context, activityClass), options)
    }
}