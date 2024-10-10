package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesListItemBinding
import id.co.edtslib.uikit.utils.snack

class GuidelinesListItemActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesListItemBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_list_item)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.liMultilineWithEndIcon.setOnClickListener {
            it.snack("Multiline With Start And End Icon Clicked", isAnchored = true)
        }
    }
}