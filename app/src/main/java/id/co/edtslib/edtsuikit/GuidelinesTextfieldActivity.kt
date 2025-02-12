package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import id.co.edtslib.edtsuikit.databinding.ActivityTextfieldBinding
import id.co.edtslib.uikit.textfield.TextFieldDelegate
import id.co.edtslib.uikit.textfield.TextField

class GuidelinesTextfieldActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityTextfieldBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textfield)


        setErrorByDefault()
    }

    private fun setErrorByDefault() {
        binding.tilLabelInsideErrorWStartEndIcon.error = "Supporting Text"
        binding.tilLabelInsideErrorHelperCounter.error = "Supporting Text"
        binding.tilLabelInsideLoading.isLoading = true

        binding.tilLabelInside.apply {
            delegate = object : TextFieldDelegate {
                override fun onValueChange(value: String?) {
                    Log.e("Test", "Value $value")
                }
            }
            inputType = TextField.InputType.Email
            imeOption = TextField.ImeOption.Done
            maxLength = 50
        }
    }

}