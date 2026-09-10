package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesTextBadgeBinding
import id.co.edtslib.uikit.core.textbadge.CoreTextBadge
import id.co.edtslib.uikit.core.textbadge.DefaultShape
import id.co.edtslib.uikit.recipes.textbadge.klik.KlikTextBadge
import id.co.edtslib.uikit.recipes.textbadge.klik.KlikVariant
import id.co.edtslib.uikit.recipes.textbadge.poinku.PoinkuTextBadge
import id.co.edtslib.uikit.recipes.textbadge.poinku.PoinkuVariant
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesTextBadgeActivity : GuidelinesBaseActivity() {
    private val binding by viewBinding<ActivityGuidelinesTextBadgeBinding>()

    override val hasConfigMenu = true

    private var config = TextBadgeConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toTextBadgeConfig() ?: TextBadgeConfig()
        supportFragmentManager.setFragmentResultListener(
            TextBadgeConfigBottomSheet.RESULT_CONFIG,
            this,
        ) { _, result ->
            config = result.toTextBadgeConfig()
            updatePreview()
        }

        setContentView(binding.root)

        setupCoreDefault()
        setupKlikFair()
        setupPoinkuLoyalty()
        setupPoinkuCoupon()
        setupPoinkuCurrency()
        setupPoinkuFreeform()

        updatePreview()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CONFIG, config.toBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onConfigMenuClicked() {
        showConfigSheet()
    }

    private fun setupCoreDefault() = with(binding) {
        badgeAppearanceSemiBold.apply {
            text = "Default"
            setCoreShape(DefaultShape())
            iconVisible = true
        }
        badgeAppearanceSemiBoldDisabled.apply {
            text = "Default Disabled"
            setCoreShape(DefaultShape())
            isEnabled = false
            iconVisible = true
        }
    }

    private fun setupKlikFair() = with(binding) {
        badgeKlikPromo.apply {
            text = "Promo"; iconVisible = true
            klikVariant = KlikVariant.Promo.DEFAULT
        }
        badgeKlikPromoDisabled.apply {
            text = "Promo Disabled"; iconVisible = true
            klikVariant = KlikVariant.Promo.DEFAULT; isEnabled = false
        }
        badgeKlikFairSmall.apply {
            text = "Fair Small"; iconVisible = true
            klikVariant = KlikVariant.Fair.SMALL
        }
        badgeKlikFairBig.apply {
            text = "Fair Big"; iconVisible = true
            klikVariant = KlikVariant.Fair.BIG
        }
        badgeKlikFairDisabled.apply {
            text = "Fair Disabled"; iconVisible = true
            klikVariant = KlikVariant.Fair.SMALL; isEnabled = false
        }
        badgeKlikFairBigDisabled.apply {
            text = "Fair Big Disabled"; iconVisible = true
            klikVariant = KlikVariant.Fair.BIG; isEnabled = false
        }
        badgeKlikHighlight.apply {
            text = "Highlight"; iconVisible = true
            klikVariant = KlikVariant.Highlight.DEFAULT
        }
        badgeKlikHighlightDisabled.apply {
            text = "Highlight Disabled"; iconVisible = true
            klikVariant = KlikVariant.Highlight.DEFAULT; isEnabled = false
        }
        badgeKlikQuota.apply {
            text = "Quota"; iconVisible = true
            klikVariant = KlikVariant.Quota.DEFAULT
        }
        badgeKlikQuotaDisabled.apply {
            text = "Quota Disabled"; iconVisible = true
            klikVariant = KlikVariant.Quota.DEFAULT; isEnabled = false
        }
    }

    private fun setupPoinkuLoyalty() = with(binding) {
        badgePoinkuLoyaltyFull.apply {
            text = "Loyalty Full"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Loyalty.Full
        }
    }

    private fun setupPoinkuCoupon() = with(binding) {
        badgePoinkuCouponFull.apply {
            text = "Coupon Full"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Coupon.Full
        }
        badgePoinkuCouponLite.apply {
            text = "Coupon Lite"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Coupon.Lite
        }
        badgePoinkuCouponFullDisabled.apply {
            text = "Coupon Full Disabled"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Coupon.Full
            isEnabled = false
        }
        badgePoinkuCouponLiteDisabled.apply {
            text = "Coupon Lite Disabled"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Coupon.Lite
            isEnabled = false
        }
    }

    private fun setupPoinkuCurrency() = with(binding) {
        badgePoinkuCurrencyFull.apply {
            text = "Currency Full"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Currency.Full
        }
        badgePoinkuCurrencyLite.apply {
            text = "Currency Lite"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Currency.Lite
        }
        badgePoinkuCurrencyFullDisabled.apply {
            text = "Currency Full Disabled"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Currency.Full
            isEnabled = false
        }
        badgePoinkuCurrencyLiteDisabled.apply {
            text = "Currency Lite Disabled"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Currency.Lite
            isEnabled = false
        }
    }

    private fun setupPoinkuFreeform() = with(binding) {
        badgePoinkuFreeform.apply {
            text = "Freeform"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Freeform
        }
        badgePoinkuFreeformDisabled.apply {
            text = "Freeform Disabled"
            iconVisible = true
            poinkuVariant = PoinkuVariant.Freeform
            isEnabled = false
        }
    }

    private fun updatePreview() {
        val container = binding.previewContainer
        container.removeAllViews()

        val badge: CoreTextBadge = when (config.product) {
            TextBadgeProduct.CORE -> CoreTextBadge(this).apply { setCoreShape(DefaultShape()) }
            TextBadgeProduct.KLIK -> KlikTextBadge(this).apply { klikVariant = config.klikVariant() }
            TextBadgeProduct.POINKU -> PoinkuTextBadge(this).apply { poinkuVariant = config.poinkuVariant() }
        }

        badge.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply { gravity = Gravity.CENTER }

        badge.text = config.text
        badge.setIcon(UIKitR.drawable.ic_placeholder_medium_24)
        badge.iconVisible = config.iconVisible

        if (config.borderWidth > 0) {
            badge.borderWidth = config.borderWidth.toFloat().dp
            badge.borderColor = config.tintColor ?: this.color(UIKitR.color.primary_30)
        }
        badge.setCornerRadius(config.cornerRadius.toFloat())
        config.tintColor?.let { badge.badgeColor = it }

        badge.isEnabled = !config.disabled

        container.addView(badge)
    }

    private fun showConfigSheet() {
        if (supportFragmentManager.findFragmentByTag(TextBadgeConfigBottomSheet.TAG) != null) return
        TextBadgeConfigBottomSheet.newInstance(config)
            .show(supportFragmentManager, TextBadgeConfigBottomSheet.TAG)
    }

    companion object {
        private const val STATE_CONFIG = "text_badge_config_state"
    }
}