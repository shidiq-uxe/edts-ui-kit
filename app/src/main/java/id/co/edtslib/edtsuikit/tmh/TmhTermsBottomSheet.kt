package id.co.edtslib.edtsuikit.tmh

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.BottomSheetTmhTermsBinding
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.html.ListStyles
import id.co.edtslib.uikit.utils.html.withGap
import id.co.edtslib.uikit.utils.html.withIndent
import id.co.edtslib.uikit.R as UIKitR

class TmhTermsBottomSheet : TmhBottomSheet() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(ARG_FAIR_NAME)?.takeIf { it.isNotBlank() }?.let {
            getString(R.string.tmh_terms_fair_title, it)
        } ?: getString(R.string.tmh_terms_title)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val content = BottomSheetTmhTermsBinding.inflate(inflater, container, false)
        var html = resources.openRawResource(R.raw.tmh_terms).bufferedReader().use { it.readText() }
        if (arguments?.getBoolean(ARG_INTEGRATED) == true) {
            val offers = arguments?.getStringArrayList(ARG_OFFERS).orEmpty()
                .joinToString("; ") { TextUtils.htmlEncode(it) }
                .ifEmpty { getString(R.string.tmh_terms_offer_fallback) }
            html = html.replace(
                "Tebus Murah 1 pc [Nama Produk] diskon [Harga Diskon]. [Kalau lebih dari 1, di list aja lagi]",
                getString(R.string.tmh_terms_offer, offers),
            )
                .replace("[Tipe Kelipatan]", "berlaku kelipatan, maksimal 2 tebus murah")
                .replace(
                    "minimal Rp100.000",
                    "minimal ${requireArguments().getLong(ARG_MINIMUM_SPEND, TmhFlowState.THRESHOLD).rupiahText()}",
                )
        }
        val renderer = HtmlRenderer(
            config = HtmlRendererConfig(orderedStyle = ListStyles.numbered.withIndent(8).withGap(4)),
            fontManager = FontManager(requireContext()),
        )
        content.tvTermsContent.text = buildHighlightedMessage(
            context = requireContext(),
            parsedMessage = renderer.render(html, content.tvTermsContent),
            highlightedTextAppearance = TextStyleKey.P1_SEMIBOLD.get(requireContext(), UIKitR.color.black_70),
        )
        contentView = content.root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        const val TAG = "TMH_TERMS"
        private const val ARG_OFFERS = "offers"
        private const val ARG_INTEGRATED = "integrated"
        private const val ARG_FAIR_NAME = "fair_name"
        private const val ARG_MINIMUM_SPEND = "minimum_spend"

        fun newInstance(
            fairName: String,
            isIntegrated: Boolean = false,
            offerDescriptions: List<String> = emptyList(),
            minimumSpend: Long = TmhFlowState.THRESHOLD,
        ): TmhTermsBottomSheet = TmhTermsBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_FAIR_NAME, fairName)
                putBoolean(ARG_INTEGRATED, isIntegrated)
                putStringArrayList(ARG_OFFERS, ArrayList(offerDescriptions))
                putLong(ARG_MINIMUM_SPEND, minimumSpend)
            }
        }
    }
}
