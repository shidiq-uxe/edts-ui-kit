package id.co.edtslib.edtsuikit.tmh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.BottomSheetTmhScenariosBinding
import id.co.edtslib.edtsuikit.databinding.ItemTmhScenarioBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.R as UIKitR

class TmhScenarioBottomSheet : TmhBottomSheet() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.tmh_scenarios)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val content = BottomSheetTmhScenariosBinding.inflate(inflater, container, false)
        val selected = arguments?.getString(ARG_SELECTED)
        val adapter = BaseAdapter.adapterOf<TmhScenario, ItemTmhScenarioBinding>(
            register = BaseAdapter.Register(
                onBindHolder = { _, item, row, _ ->
                    row.root.tmhTypography()
                    row.root.text = getString(item.labelRes)
                    row.root.isSelected = item.name == selected
                    row.root.setTextColor(requireContext().color(
                        if (row.root.isSelected) UIKitR.color.primary_30 else UIKitR.color.black_70
                    ))
                },
            ),
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old == new },
                areContentsTheSame = { old, new -> old == new },
            ),
            itemList = TmhScenario.entries,
        )
        adapter.setOnItemClickListener { _, scenario, _ ->
            parentFragmentManager.setFragmentResult(RESULT, Bundle().apply {
                putString(KEY_SCENARIO, scenario.name)
            })
            closeSheet()
        }
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { refreshSheetSize() }
        })
        content.root.adapter = adapter
        contentView = content.root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        const val TAG = "TMH_SCENARIOS"
        const val RESULT = "tmh_scenario_result"
        const val KEY_SCENARIO = "scenario"
        private const val ARG_SELECTED = "selected"

        fun newInstance(scenario: TmhScenario): TmhScenarioBottomSheet = TmhScenarioBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_SELECTED, scenario.name) }
        }
    }
}
