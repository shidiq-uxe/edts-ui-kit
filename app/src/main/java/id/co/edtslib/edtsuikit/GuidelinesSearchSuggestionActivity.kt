package id.co.edtslib.edtsuikit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSearchSuggestionBinding
import id.co.edtslib.uikit.searchbar.SearchBarDelegate
import id.co.edtslib.uikit.utils.hideKeyboard

class GuidelinesSearchSuggestionActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinesSearchSuggestionBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guidelines_search_suggestion)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onSearch()
        onClickListener()
        onImeClickListener()
    }

    private fun onSearch() {
        binding.sbProduct.editText.imeOptions = EditorInfo.IME_ACTION_SEARCH

        binding.sbProduct.searchBarDelegate = object : SearchBarDelegate {
            override fun onCloseIconClicked(view: View) {}

            override fun onFocusChange(view: View, hasFocus: Boolean) {}

            override fun onTextChange(text: String) {}
        }
    }

    private fun onClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun onImeClickListener() {
        binding.sbProduct.editText.setOnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                val query = view.text.toString().trim()

                if (query.isNotEmpty()) {
                    performSearch()
                }

                view.hideKeyboard()

                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch() {
        startActivity(
            Intent(this, GuidelinesSearchProductActivity::class.java)
        )
    }
}