package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import id.co.edtslib.edtsuikit.databinding.BottomSheetAlertBoxConfigBinding
import id.co.edtslib.uikit.tray.BottomSheetTray

class AlertBoxConfigBottomSheet : BottomSheetTray() {

    private var config = AlertBoxConfig()
    private var sheetBinding: BottomSheetAlertBoxConfigBinding? = null

    private fun publish() {
        parentFragmentManager.setFragmentResult(RESULT, config.toBundle())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = (savedInstanceState?.getBundle(STATE_CONFIG) ?: arguments?.getBundle(ARG_CONFIG))
            ?.let(AlertBoxConfig::fromBundle) ?: AlertBoxConfig()
        title = getString(R.string.alert_demo_configure)
        dragHandleVisibility = true
        titleDividerVisibility = true
        shouldShowClose = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        sheetBinding = BottomSheetAlertBoxConfigBinding.inflate(inflater, null, false)
        contentView = sheetBinding!!.root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controls = requireNotNull(sheetBinding)
        controls.etMessage.setText(config.message)
        controls.swHtml.isChecked = config.isHtml
        controls.swStartIcon.isChecked = config.showStartIcon
        controls.swCloseIcon.isChecked = config.showClose
        controls.swFooterButton.isChecked = config.showButton
        controls.swInlineAction.isChecked = config.showAction
        controls.swDisabled.isChecked = config.disabled
        controls.spIconAlignment.setSelection(config.iconAlignment)
        controls.etMessage.doAfterTextChanged {
            config = config.copy(message = it.toString())
            publish()
        }
        controls.swHtml.setOnCheckedChangeListener { _, checked ->
            config = config.copy(isHtml = checked)
            publish()
        }
        controls.swStartIcon.setOnCheckedChangeListener { _, checked ->
            config = config.copy(showStartIcon = checked)
            publish()
        }
        controls.swCloseIcon.setOnCheckedChangeListener { _, checked ->
            config = config.copy(showClose = checked)
            publish()
        }
        controls.swFooterButton.setOnCheckedChangeListener { _, checked ->
            config = config.copy(showButton = checked)
            publish()
        }
        controls.swInlineAction.setOnCheckedChangeListener { _, checked ->
            config = config.copy(showAction = checked)
            publish()
        }
        controls.swDisabled.setOnCheckedChangeListener { _, checked ->
            config = config.copy(disabled = checked)
            publish()
        }
        controls.spIconAlignment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                config = config.copy(iconAlignment = position)
                publish()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        controls.btnResetConfig.setOnClickListener {
            config = AlertBoxConfig(showButton = arguments?.getBoolean(ARG_DEFAULT_FOOTER) == true)
            publish()
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CONFIG, config.toBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        sheetBinding = null
        contentView = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "ALERT_BOX_CONFIG"
        const val RESULT = "ALERT_BOX_CONFIG_RESULT"
        private const val ARG_CONFIG = "config"
        private const val ARG_DEFAULT_FOOTER = "default_footer"
        private const val STATE_CONFIG = "state_config"

        internal fun newInstance(config: AlertBoxConfig, defaultFooter: Boolean) = AlertBoxConfigBottomSheet().apply {
            arguments = Bundle().apply {
                putBundle(ARG_CONFIG, config.toBundle())
                putBoolean(ARG_DEFAULT_FOOTER, defaultFooter)
            }
        }
    }
}
