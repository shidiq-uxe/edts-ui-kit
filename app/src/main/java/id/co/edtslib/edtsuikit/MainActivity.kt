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
        val items = resources.getStringArray(R.array.array_guidelines_title).toList()

        binding.rvItemMenu.adapter = BaseAdapter.adapterOf<String, ItemGuidelinesParentBinding>(
            register = BaseAdapter.Register(
                onBindHolder = { position, item, binding, _ ->
                    binding.tvDesignComponentTitle.transitionName = "shared_title_$position"

                    binding.tvDesignComponentTitle.text = item
                }
            ),
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old == new },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList = items
        ).also { adapter ->

            adapter.setOnItemClickListener { binding, item, position ->
                startActivity(
                    when(position) {
                        0 -> Intent(this, GuidelinesTypographyActivity::class.java)
                        1 -> Intent(this, GuidelinesButtonActivity::class.java)
                        2 -> Intent(this, GuidelinesTextfieldActivity::class.java)
                        3 -> Intent(this, GuidelinesColorActivity::class.java)
                        4 -> Intent(this, GuidelinesOtpActivity::class.java)
                        5 -> Intent(this, GuidelinesSearchbarActivity::class.java)
                        6 -> Intent(this, GuidelinesAlertBoxActivity::class.java)
                        7 -> Intent(this, GuidelinesBoardingActivity::class.java)
                        8 -> Intent(this, GuidelinesListItemActivity::class.java)
                        9 -> Intent(this, GuidelinesSnackbarActivity::class.java)
                        10 -> Intent(this, GuidelinesBottomTrayActivity::class.java)
                        11 -> Intent(this, PoinkuResearchActivity::class.java)
                        12 -> Intent(this, GuidelinesProgressBarActivity::class.java)
                        13 -> Intent(this, GuidelinesPopupActivity::class.java)
                        14 -> Intent(this, GuidelinesSegmentedTabLayoutActivity::class.java)
                        15 -> Intent(this, GuidelinesHomeSwitcherActivity::class.java)
                        16 -> Intent(this, GuidelinesHomepageExploration::class.java)
                        17 -> Intent(this, GuidelineCoachmarkActivity::class.java)
                        else -> Intent(this, SpotlightTrialsActivity::class.java)
                    },
                    binding.sharedElementOptions
                )
            }
        }
    }
}