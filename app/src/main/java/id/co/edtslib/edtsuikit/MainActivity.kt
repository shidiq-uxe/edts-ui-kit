package id.co.edtslib.edtsuikit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityMainBinding
import id.co.edtslib.edtsuikit.databinding.ItemGuidelinesParentBinding
import id.co.edtslib.edtsuikit.helper.GuidelineItem
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.pulltorefresh.LiquidRefreshLayout
import id.co.edtslib.uikit.utils.setLightStatusBar

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.setLightStatusBar()

        actionBar?.setDisplayHomeAsUpEnabled(true)

        bindItemMenu()

        binding.root.setOnRefreshListener(object : LiquidRefreshLayout.OnRefreshListener {
            override fun completeRefresh() {

            }

            override fun refreshing() {
                // Perform your refresh logic here
                Handler(Looper.getMainLooper()).postDelayed({
                    // Stop refresh animation after the operation is complete
                    binding.root.finishRefreshing()
                }, 2000)
            }
        })
    }

    private val ItemGuidelinesParentBinding.sharedElementOptions get() = ActivityOptionsCompat.makeSceneTransitionAnimation(
        this@MainActivity,
        this.tvDesignComponentTitle,
        ViewCompat.getTransitionName(this.tvDesignComponentTitle).orEmpty()
    ).toBundle()

    private fun bindItemMenu() {
        val guidelineItems = GuidelineItem.getAllItems()
        val guidelineItemCategories = GuidelineItem.getByCategory()

        binding.rvItemMenu.adapter = BaseAdapter.adapterOf<GuidelineItem, ItemGuidelinesParentBinding>(
            register = BaseAdapter.Register(
                onBindHolder = { position, item, binding, _ ->
                    binding.tvDesignComponentTitle.transitionName = "shared_title_$position"

                    binding.tvDesignComponentTitle.text = getString(item.titleRes)
                }
            ),
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old == new },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList = guidelineItems
        ).also { adapter ->
            adapter.setOnItemClickListener { binding, item, position ->
                item.navigate(this, binding.sharedElementOptions)
            }
        }
    }
}