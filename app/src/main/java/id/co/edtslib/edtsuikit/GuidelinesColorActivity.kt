package id.co.edtslib.edtsuikit

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.edtsuikit.databinding.ActivityColorBinding
import id.co.edtslib.edtsuikit.databinding.ItemColorBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.R as Core_uiR

class GuidelinesColorActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityColorBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = BaseAdapter.adapterOf<Triple<String, String, Int>, ItemColorBinding>(
            register = BaseAdapter.Register(
                onBindHolder = { _, (color, colorName, colorRes), binding, _ ->
                    binding.ivColor.setBackgroundColor(color(colorRes))
                    binding.tvColorName.text = colorName
                    binding.tvColorCode.text = color
                }
            ),
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old.first == new.first },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList = colorResources
        )

        binding.rvGuidelinesColor.adapter = adapter

        val spacingInPixels = resources?.getDimensionPixelSize(Core_uiR.dimen.xxs) ?: 0

        val spacingItemDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State,
            ) {
                outRect.bottom = spacingInPixels
            }
        }

        binding.rvGuidelinesColor.addItemDecoration(spacingItemDecoration)
    }

    private val colorResources = listOf(
        Triple("#1659AB", "primary_50", Core_uiR.color.primary_50),
        Triple("#457ABC", "primary_40", Core_uiR.color.primary_40),
        Triple("#8AACD5", "primary_30", Core_uiR.color.primary_30),
        Triple("#D0DEEE", "primary_20", Core_uiR.color.primary_20),
        Triple("#E8EEF7", "primary_10", Core_uiR.color.primary_10),

        Triple("#FF3030", "secondary_50", Core_uiR.color.secondary_50),
        Triple("#FF5959", "secondary_40", Core_uiR.color.secondary_40),
        Triple("#FF9797", "secondary_30", Core_uiR.color.secondary_30),
        Triple("#FFD6D6", "secondary_20", Core_uiR.color.secondary_20),
        Triple("#FFEAEA", "secondary_10", Core_uiR.color.secondary_10),

        Triple("#FFAB00", "red", Core_uiR.color.red),
        Triple("#FFBC33", "red_40", Core_uiR.color.red_40),
        Triple("#FFD580", "red_30", Core_uiR.color.red_30),
        Triple("#FFEECC", "red_20", Core_uiR.color.red_20),
        Triple("#FFF7E5", "red_10", Core_uiR.color.red_10),

        Triple("#1A9F67", "black_70", Core_uiR.color.black_70),
        Triple("#48B285", "black_60", Core_uiR.color.black_60),
        Triple("#8CCFB3", "black_50", Core_uiR.color.black_50),
        Triple("#D1ECE1", "black_40", Core_uiR.color.black_40),
        Triple("#E8F5F0", "black_30", Core_uiR.color.black_30),
        Triple("#EFF3F6", "black_20", Core_uiR.color.black_20),
        Triple("#F8FBFC", "black_10", Core_uiR.color.black_10),
        Triple("#FFFFFF", "white", Core_uiR.color.white),

        Triple("#32B9D7", "alert_primary", Core_uiR.color.alert_primary),
        Triple("#5BC7DF", "alert_background", Core_uiR.color.alert_background),
        Triple("#98DCEB", "alert_border", Core_uiR.color.alert_border),

        Triple("#ED608F", "warning_primary", Core_uiR.color.warning_primary),
        Triple("#F180A5", "warning_background", Core_uiR.color.warning_background),
        Triple("#F6AFC7", "warning_border", Core_uiR.color.warning_border),

        Triple("#18A4F2", "success_primary", Core_uiR.color.success_primary),
        Triple("#46B6F5", "success_background", Core_uiR.color.success_background),
        Triple("#8BD2F9", "success_border", Core_uiR.color.success_border),

        Triple("#191919", "support_others", Core_uiR.color.support_others),
        Triple("#484E54", "support_extra", Core_uiR.color.support_extra),
        Triple("#62686E", "support_fab", Core_uiR.color.support_fab),
        Triple("#878F99", "support_selected", Core_uiR.color.support_selected),
    )
}