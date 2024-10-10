package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.edtsuikit.databinding.ActivityTextfieldBinding

class GuidelinesTextfieldActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityTextfieldBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textfield)

    }

}