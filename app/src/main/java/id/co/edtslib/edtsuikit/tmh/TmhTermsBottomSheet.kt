package id.co.edtslib.edtsuikit.tmh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.BottomSheetTmhTermsBinding

class TmhTermsBottomSheet : TmhBottomSheet() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.tmh_terms_title)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        contentView = BottomSheetTmhTermsBinding.inflate(inflater, container, false).root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        const val TAG = "TMH_TERMS"
    }
}
