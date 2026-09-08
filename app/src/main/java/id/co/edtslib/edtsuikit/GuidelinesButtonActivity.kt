package id.co.edtslib.edtsuikit

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import id.co.edtslib.edtsuikit.databinding.ActivityButtonBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.adapter.ViewAdapter
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.utils.dp

class GuidelinesButtonActivity : GuidelinesBaseActivity() {

    override val hasConfigMenu = true

    private val binding by viewBinding<ActivityButtonBinding>()

    override val enableEdgeToEdge: Boolean
        get() = false

    private var buttonAdapter: ViewAdapter<Button, ButtonVariant>? = null

    private var config = ButtonConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toButtonConfig() ?: ButtonConfig()
        supportFragmentManager.setFragmentResultListener(
            ButtonConfigBottomSheet.RESULT_CONFIG,
            this,
        ) { _, result ->
            config = result.toButtonConfig()
            updateButtons()
        }

        setContentView(R.layout.activity_button)
        setupButtonList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CONFIG, config.toBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onConfigMenuClicked() {
        showConfigSheet()
    }

    private fun setupButtonList() {
        binding.rvButtons.layoutManager = LinearLayoutManager(this)

        buttonAdapter = ViewAdapter.adapterOf<Button, ButtonVariant>(
            viewFactory = { context -> createButton(context) },
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old.section == new.section && old.type == new.type && old.size == new.size },
                areContentsTheSame = { _, _ -> false }
            ),
            configure = { position, button, variant ->
                button.strokeWidth = 0
                button.strokeColor = null

                button.buttonType = variant.type
                button.text = variant.label
                button.isEnabled = !config.disabled
                button.isDestructive = config.destructive
                button.shouldShowShimmer = config.shimmer
                button.cornerRadius = config.cornerRadius.toInt()

                if (!config.destructive && !config.disabled) {
                    config.tintColor?.let { color ->
                        val colorList = ColorStateList.valueOf(color)
                        when (variant.section) {
                            Section.FILLED -> button.backgroundTintList = colorList
                            Section.SECONDARY, Section.TERTIARY -> {
                                button.strokeColor = colorList
                                button.setTextColor(color)
                            }
                            Section.TEXT -> button.setTextColor(color)
                        }
                    }
                }

                (button.layoutParams as? androidx.recyclerview.widget.RecyclerView.LayoutParams)?.apply {
                    setMargins(0, 8.dp.toInt(), 0, 0)
                }
            },
            itemList = config.visibleVariants
        ).also { binding.rvButtons.adapter = it }
    }

    private fun updateButtons() {
        buttonAdapter?.items = config.visibleVariants
    }

    private fun showConfigSheet() {
        if (supportFragmentManager.findFragmentByTag(ButtonConfigBottomSheet.TAG) != null) return
        ButtonConfigBottomSheet.newInstance(config)
            .show(supportFragmentManager, ButtonConfigBottomSheet.TAG)
    }

    private fun createButton(context: Context): Button {
        return Button(context, null).apply {
            layoutParams = androidx.recyclerview.widget.RecyclerView.LayoutParams(
                androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT,
                androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
            )
        }
    }

    companion object {
        private const val STATE_CONFIG = "button_config_state"
    }
}
