package id.co.edtslib.edtsuikit

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.edtsuikit.databinding.ActivityTypographyBinding
import id.co.edtslib.edtsuikit.databinding.ItemTypographyBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.utils.DividerItemDecoration
import id.co.edtslib.uikit.utils.setLightStatusBar
import id.co.edtslib.uikit.R as Core_uiR

class GuidelinesTypographyActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityTypographyBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_typography)
        setLightStatusBar()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val textNames = resources.getStringArray(R.array.array_guidelines_font)
        val texts = resources.getStringArray(R.array.array_guidelines_font_title)
        val textAppearances = listOf(
            // Light
            Core_uiR.style.TextAppearance_Inter_Semibold_D1,
            Core_uiR.style.TextAppearance_Inter_Semibold_D2,

            // Regular
            Core_uiR.style.TextAppearance_Inter_Semibold_H1,
            Core_uiR.style.TextAppearance_Inter_Semibold_H2,
            Core_uiR.style.TextAppearance_Inter_Semibold_H3,
            // Medium
            Core_uiR.style.TextAppearance_Inter_Regular_B1,
            Core_uiR.style.TextAppearance_Inter_Regular_B2,
            Core_uiR.style.TextAppearance_Inter_Regular_B3,
            Core_uiR.style.TextAppearance_Inter_Regular_B4,
            // Bold
            Core_uiR.style.TextAppearance_Inter_Regular_P1,
        )

        val list = textNames.mapIndexed { i, s -> Triple(s, texts[i], textAppearances[i]) }

        val adapter = BaseAdapter.adapterOf<Triple<String, String, Int>, ItemTypographyBinding>(
            register = BaseAdapter.Register(
                onBindHolder = { _, (textName, text, appearance), binding, _ ->
                    binding.tvTitleSize.text = textName
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.tvTitleSize.setTextAppearance(appearance)
                    } else{
                        binding.tvTitleSize.setTextAppearance(this, appearance)
                    }

                    binding.tvTitleName.text = text
                }
            ), diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old.first == new.first },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList = list
        )

        adapter.setOnItemClickListener { itemBinding, (textName, text, appearance), _ ->
            itemBinding.tvTitleSize.isSelected = !itemBinding.tvTitleSize.isSelected
        }

        binding.rvGuidelinesFont.adapter = adapter
        binding.rvGuidelinesFont.addItemDecoration(
            DividerItemDecoration(
                context = this,
                orientation = androidx.recyclerview.widget.DividerItemDecoration.VERTICAL,
                drawableRes = 0,
                horizontalInset = Core_uiR.dimen.s
            )
        )
    }
}