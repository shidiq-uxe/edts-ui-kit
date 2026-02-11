package id.co.edtslib.edtsuikit

import android.graphics.Color
import android.os.Bundle
import android.view.View.generateViewId
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isNotEmpty
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesMyCouponBinding
import id.co.edtslib.edtsuikit.databinding.DeleteItemMyCouponBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.attachLinearMarginItemDecoration
import id.co.edtslib.uikit.utils.dp
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesMyCouponActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesMyCouponBinding>()

    private var isTabHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.WHITE)
        )

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_guidelines_my_coupon)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addChips()
        bindAdapter()

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun bindAdapter() {
        binding.rvTest.adapter = BaseAdapter.adapterOf<Any, DeleteItemMyCouponBinding>(
            diff = BaseAdapter.Diff(
                areContentsTheSame = { old, new -> old == new }  ,
                areItemsTheSame = { old, new -> old == new },
            ),
            register = BaseAdapter.Register(
                onBindHolder = { position, item, adapterBinding, _ ->

                }
            ),
            itemList = List(20) {

            }
        )

        binding.rvTest.attachLinearMarginItemDecoration(
            orientation = LinearLayoutManager.VERTICAL,
            margin = MarginItem(first =  16.dp.toInt(), top = 12.dp.toInt())
        )

        binding.rvTest.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val isAtTop = layoutManager?.findFirstCompletelyVisibleItemPosition() == 0

                if (isAtTop && isTabHidden) {
                    showTab(
                        delay = 0L,
                    )
                } else if (dy != 0 && !isTabHidden && !isAtTop) {
                    hideTab()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isTabHidden) {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                    val isAtTop = layoutManager?.findFirstCompletelyVisibleItemPosition() == 0

                    showTab()
                }
            }
        })

        binding.cgTabLevel2.doOnLayout {
            binding.rvTest.updatePadding(top = it.height)
            binding.rvTest.scrollToPosition(0)
        }
    }

    private fun addChips() {
        val chipItems = listOf("Semua", "Xtra", "Xpress")

        addFilterChip(chipItems)
        checkChipByPosition(0)
    }

    private var onChipCheckListener: () -> Unit = {}

    private fun setOnChipCheckListener(action: () -> Unit) {
        onChipCheckListener = action
    }

    private fun addFilterChip(
        chipItems: List<String>,
        chipAttrs: Chip.() -> Unit = {}
    ) {
        chipItems.forEachIndexed { position, text ->
            val chip = Chip(this, null, UIKitR.attr.chipFilterStyle).apply {
                this.id = generateViewId()
                this.text = text

                chipAttrs()

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        checkChipByPosition(position)
                        onChipCheckListener.invoke()
                    }
                }
            }

            binding.cgTabLevel2.addView(chip)
        }
    }

    private fun hideTab(
        duration: Long = 150L,
        delay: Long = 100L,
    ) {
        isTabHidden = true
        binding.cgTabLevel2.animate()
            .translationY(-binding.cgTabLevel2.height.plus(2.dp).toFloat())
            .setDuration(duration)
            .setStartDelay(delay)
            .start()
    }

    private fun showTab(
        duration: Long = 200L,
        delay: Long = 300L,
    ) {
        isTabHidden = false
        binding.cgTabLevel2.animate()
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .start()
    }

    private fun checkChipByPosition(position: Int) {
        if (binding.cgTabLevel2.isNotEmpty()) {
            (binding.cgTabLevel2.getChildAt(position) as? Chip)?.isChecked = true
        }
    }


}