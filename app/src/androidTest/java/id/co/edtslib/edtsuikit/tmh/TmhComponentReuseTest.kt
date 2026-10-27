package id.co.edtslib.edtsuikit.tmh

import android.os.SystemClock
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.ItemTmhFairBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TmhComponentReuseTest {

    @Test
    fun updatingTheSameFairReusesItsProductAdapterAndKeepsTheHorizontalOffset() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { activity ->
            SystemClock.sleep(1800)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            lateinit var products: RecyclerView
            lateinit var adapter: RecyclerView.Adapter<*>
            lateinit var layoutManager: LinearLayoutManager
            var position = 0
            var offset = 0
            activity.onActivity { screen ->
                val fairs = screen.findViewById<RecyclerView>(R.id.rvFairs)
                products = requireNotNull(fairs.findViewHolderForAdapterPosition(0))
                    .itemView.findViewById(R.id.rvProducts)
                adapter = requireNotNull(products.adapter)
                layoutManager = products.layoutManager as LinearLayoutManager
                products.scrollBy(120, 0)
                position = layoutManager.findFirstVisibleItemPosition()
                offset = requireNotNull(layoutManager.findViewByPosition(position)).left
                assertTrue(requireNotNull(products.findViewHolderForAdapterPosition(0))
                    .itemView.findViewById<View>(R.id.ivSelected).visibility == View.VISIBLE)
                @Suppress("UNCHECKED_CAST")
                val fairAdapter = fairs.adapter as BaseAdapter<TmhFair, ItemTmhFairBinding>
                fairAdapter.items = fairAdapter.items.mapIndexed { index, fair ->
                    if (index == 0) fair.copy(savedQuantities = emptyMap(), state = TmhFairState.ELIGIBLE) else fair
                }
            }
            SystemClock.sleep(650)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            activity.onActivity {
                assertSame(adapter, products.adapter)
                assertSame(layoutManager, products.layoutManager)
                assertEquals(position, layoutManager.findFirstVisibleItemPosition())
                assertEquals(offset, requireNotNull(layoutManager.findViewByPosition(position)).left)
                assertFalse(requireNotNull(products.findViewHolderForAdapterPosition(0))
                    .itemView.findViewById<View>(R.id.ivSelected).visibility == View.VISIBLE)
            }
        }
    }
}
