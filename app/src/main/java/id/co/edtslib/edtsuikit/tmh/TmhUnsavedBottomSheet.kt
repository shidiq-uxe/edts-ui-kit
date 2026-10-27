package id.co.edtslib.edtsuikit.tmh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.BottomSheetTmhUnsavedBinding
import id.co.edtslib.uikit.button.Button

class TmhUnsavedBottomSheet : TmhBottomSheet() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dragHandleVisibility = !(arguments?.getBoolean(ARG_STANDALONE) ?: false)
        title = getString(R.string.tmh_unsaved_title)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val content = BottomSheetTmhUnsavedBinding.inflate(inflater, container, false)
        content.btnStay.setOnClickListener { closeSheet() }
        content.btnLeave.buttonType = Button.ButtonType.SECONDARY_LARGE
        content.btnStay.buttonType = Button.ButtonType.FILLED_LARGE
        content.btnStay.tmhTextAppearance(R.style.TextAppearance_Tmh_SemiBold_Button_Text_Large, 24)
        content.btnLeave.tmhTextAppearance(R.style.TextAppearance_Tmh_SemiBold_Button_Text_Large, 24)
        content.btnLeave.setOnClickListener {
            parentFragmentManager.setFragmentResult(RESULT_DISCARD, Bundle.EMPTY)
            closeSheet()
        }
        contentView = content.root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        private const val ARG_STANDALONE = "standalone"

        fun newInstance(isStandalone: Boolean = false): TmhUnsavedBottomSheet = TmhUnsavedBottomSheet().apply {
            arguments = Bundle().apply { putBoolean(ARG_STANDALONE, isStandalone) }
        }

        const val TAG = "TMH_UNSAVED"
        const val RESULT_DISCARD = "tmh_discard_result"
    }
}
