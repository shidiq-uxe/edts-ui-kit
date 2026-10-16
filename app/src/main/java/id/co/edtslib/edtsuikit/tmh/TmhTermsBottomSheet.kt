package id.co.edtslib.edtsuikit.tmh

import android.os.Bundle
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
        val html = resources.openRawResource(R.raw.tmh_terms).bufferedReader().use { it.readText() }
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
        private const val ARG_FAIR_NAME = "fair_name"

        fun newInstance(fairName: String): TmhTermsBottomSheet = TmhTermsBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_FAIR_NAME, fairName) }
        }
    }
}
