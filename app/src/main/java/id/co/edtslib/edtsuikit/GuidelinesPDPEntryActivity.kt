package id.co.edtslib.edtsuikit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesPdpentryBinding
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dp

class GuidelinesPDPEntryActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesPdpentryBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_guidelines_pdpentry)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val actionBarSize = dimensionFromAttribute(com.google.android.material.R.attr.actionBarSize)

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top.plus(actionBarSize), systemBars.right, systemBars.bottom)
            insets
        }

        bindOnClickListener()
    }

    private fun bindOnClickListener() {
        val searchPageIntent = Intent(this, GuidelinesSearchProductActivity::class.java)
        val pdpIntent = Intent(this, GuidelinePDPActivity::class.java)

        binding.btnProductSearchPage.setOnClickListener {
            startActivity(searchPageIntent)
        }

        binding.btnPDPNormalEntry.setOnClickListener {
            startActivity(pdpIntent)
        }
        binding.btnPDPDisabledVariant.setOnClickListener {
            pdpIntent.apply {
                putExtra(SHOULD_DISABLE_VARIANT, true)
            }.also {
                startActivity(it)
            }
        }
        binding.btnPDPToast.setOnClickListener {
            pdpIntent.apply {
                putExtra(SHOULD_SHOW_TOAST, true)
            }.also {
                startActivity(it)
            }
        }
    }

    fun Context.dimensionFromAttribute(attribute: Int): Int {
        val attributes = obtainStyledAttributes(intArrayOf(attribute))
        val dimension = attributes.getDimensionPixelSize(0, 0)
        attributes.recycle()
        return dimension
    }


    companion object {
        val SHOULD_DISABLE_VARIANT = "should_disable_variant"
        val SHOULD_SHOW_TOAST = "should_show_toast"
    }
}