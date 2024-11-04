package id.co.edtslib.edtsuikit

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.MotionEvent
import androidx.core.transition.addListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import id.co.edtslib.edtsuikit.databinding.ActivityPoinkuResearchBinding
import id.co.edtslib.edtsuikit.databinding.ItemGridLayoutPoinkuBinding
import id.co.edtslib.edtsuikit.databinding.ItemLinearLayoutPoinkuBinding
import id.co.edtslib.uikit.adapter.AnimationType
import id.co.edtslib.uikit.adapter.AnimationWrapperAdapter
import id.co.edtslib.uikit.adapter.multiTypeAdapter
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.gridMarginItemDecoration
import id.co.edtslib.uikit.utils.linearMarginItemDecoration
import id.co.edtslib.uikit.utils.px
import kotlin.random.Random

class PoinkuResearchActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityPoinkuResearchBinding>()

    private val adapter = multiTypeAdapter(
        diffCallback = object : DiffUtil.ItemCallback<DummyItem>() {
            override fun areItemsTheSame(oldItem: DummyItem, newItem: DummyItem) =  oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: DummyItem, newItem: DummyItem) = oldItem == newItem
        },
        viewTypeConfig = {
            layoutViewType.ordinal
        },
        bindingConfig = {
            registerViewType(
                viewType = LayoutViewType.GRID.ordinal,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemGridLayoutPoinkuBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemGridLayoutPoinkuBinding).apply {
                        Glide.with(this@PoinkuResearchActivity)
                            .load(item.image)
                            .into(this.ivProductPlaceholder)

                        this.tvTitle.text = item.title
                        this.tvSubtitle.text = "Tersedia ${item.availability.first}/${item.availability.second} Kupon"
                        this.Chip1.text = item.voucherType
                        this.Chip2.text = "${item.point} Poin"
                    }
                }
            )

            registerViewType(
                viewType = LayoutViewType.LINEAR.ordinal,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemLinearLayoutPoinkuBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemLinearLayoutPoinkuBinding).apply {
                        Glide.with(this@PoinkuResearchActivity)
                            .load(item.image)
                            .into(this.ivProductPlaceholder)

                        this.tvTitle.text = item.title
                        this.tvSubtitle.text = "Tersedia ${item.availability.first}/${item.availability.second} Kupon"
                        this.Chip1.text = item.voucherType
                        this.Chip2.text = "${item.point} Poin"
                    }
                }
            )
        },
        onViewDetachedFromWindow = { holder ->
            holder.itemView.clearAnimation()
        }
    ).apply { items = List(50) {
        DummyItem(
            image = images.random(),
            title = titles.random(),
            availability = availabilities.random(),
            voucherType = voucherType.random(),
            point = (1..1000).random()
        )
    } }


    private val titles get() = listOf("Voucher Belanja Elektronik Rp100.000" , "Red Spirit - Double Fry Pan Red", "Skin Weapon Magallanica: Free Fire", "Diskon 10% XBox Series X 512 Tb - Black Version")
    private val availabilities get() = listOf(
        (1..1000).random() to 1000,
        (1..50).random() to 50,
        (1..100).random() to 100,
        (1..3).random() to 3
    )
    private val voucherType get()  = listOf("Delivery", "Voucher Merchant", "i-Kupon")

    private val images get() = listOf(
        "https://s3-alpha-sig.figma.com/img/6c2f/36e8/ffcf10f6d9edb5dcb7fe13091b83a4ad?Expires=1731283200&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=gka7TYPhziv1v6edhrOuZ9T3LVhZ7KO019e7bosuWR3kCgUp45hVISV1wV0KDIuy88UgGNtNIq3nBM8MIdmMYJPNBq80pw8brqkEcu~ij6W1aounkuUQo~PxMhVgWp4vs5uEmD4dS92UfG~6sYYfI8SIKFaB55Ms~TXfHZqqSGrYB~qfew1B8XYd0odkzA49OSBhvSYrY60pzE~feVT~gc0aA6pLEDigliJY5jplGsedWngcknpqgdh8FI3DTK542k9MWpr4lpF3pRtPlLZH~gczdjRoQ3~KxjkgryN1RLbiUahtDnC2W8703IroPoOmbiMAu19I9aRAv62FOGbzIg__",
        "https://s3-alpha-sig.figma.com/img/5ab7/3d0f/f2c98baca9fe6fc7423e3a62fde5a913?Expires=1731283200&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=Jzh14WhMT59xtIezfxJ22y5IpTnQWjePx81ciFsT~HpN1O~8PlqhRsXVNzymogZjXz-4Ka9c5kShqZITG8PlVLOCMjvoU~Hbj2vH~2LnXo4e8d5U6MG4liORNBYVyNSAEcmEv3hpY1Ih8vr3aE0lI~6Fj5cOlQUkflTj04LNgeg1HCcLegebXKg0I5ri7YdWD5OQSFc6xxZv4GzcrPrtMFfyLSeyoyC0RniLkw4L3Jb~WRqnRuYo1XamTnd12ra8UdvAC85W4pstRYAN1AxayrELgOZtXQvBG70vm7tO8vyzpU70pBKFtYdsYhrICsQ7hAUQqpuqBO4Wm82MQOobbw__",
        "https://s3-alpha-sig.figma.com/img/f08e/13c5/34173b537edfebe07ce808f34503dad7?Expires=1731283200&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=otMx7qfesojwgs2TZg~Bi6ktxPThnoeaJyJIhSfZ6WgkRzkzCukc67La1eCJxK~4qzKFG6rtncRhY-5UvXZqdkiOIfV66mTeEgfr185qnKoZHbmgOpIgvEbl~8MOKrWpUhY-WEyzl9Hx0agE3X85Xu3HZf2QJqg9hfmfdlqgxnWAzL~q-Q1vQb3Zfp9ttWVFL7StDYpFxVgs7ZIaQnoQgyL0lY1TJqky6M2EmnNGyXRyetIpuYhKRkj3lu1BCn379cD-8lks~bPh50vbTAvSiWDU7NbafqNvGGmy9WqLxwZ-zWRDia8OOddfQ4Xi~x3KSN5LKKBgdRVNcHHMwoKekg__",
        "https://s3-alpha-sig.figma.com/img/1750/330d/6648b7b26d65723688e8afa3d104153e?Expires=1731283200&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=Mgn81C~Duyy8qwjQZVuzP8D9hfS3cSHqO8LfkGJZ0~g0yp~KVmBuTc5nZvd6OCrxPXFU0ywh-S8Zb9-zV4bdbEz39Von~GzrzVd5mOBSKPFg57FacR8Gd8FlQDyZhPGna6lGBZ76NzWU4g1zU2R6d6T2zJ8~oV6dPSRFc9vl7Kz67mHY3UmMq606MydKbe~4X0hyQTDMum1QIwumMx8KcPx9JOYUOqf9YX~aTkiPuzFB0A9I3KO87nuw5IyrPar0jOXOD~tpoC~IEx3xBQXCpg1rQA-phT4zIAVCuX~A-w2G9CZgh5QXg~VqTG7gc8aSFbihLYEr0-nJfTi4bEyk2Q__"
    )


    private var layoutViewType = LayoutViewType.GRID
        set(value) {
            field = value

            val transition = AutoTransition().apply {
                duration = 200

                addListener(
                    onStart = { binding.btnLayoutType.isClickable = false },
                    onEnd = { binding.btnLayoutType.isClickable = true }
                )
            }

            TransitionManager.beginDelayedTransition(binding.rvLayoutChange, transition)

            binding.rvLayoutChange.layoutManager = when(value) {
                LayoutViewType.GRID -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                LayoutViewType.LINEAR -> LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            }

            binding.rvLayoutChange.removeItemDecoration(itemDecorator)
            binding.rvLayoutChange.addItemDecoration(itemDecorator)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poinku_research)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupPulsingEffect()
        setupTouchEffect()

        binding.cbViewType.setOnCheckedChangeListener { _, isChecked ->
            layoutViewType = if (isChecked) LayoutViewType.LINEAR else LayoutViewType.GRID
        }

        binding.btnLayoutType.setOnClickListener {
            layoutViewType = if (layoutViewType == LayoutViewType.GRID) LayoutViewType.LINEAR else LayoutViewType.GRID
        }
    }

    private fun setupPulsingEffect() {
        val shimmer = Shimmer.AlphaHighlightBuilder()
            .setBaseAlpha(1f)
            .setHighlightAlpha(0.75f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .setDuration(3000)
            .build()

        binding.shimmerFrameLayout.setShimmer(shimmer)
        binding.shimmerFrameLayout.startShimmer()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchEffect() {
        val views = listOf(binding.btnLayoutType, binding.shimmerFrameLayout)

        val scaleUpValue = 1.035f
        val scaleDownValue = 1f

        binding.btnLayoutType.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.btnLayoutType.animate().scaleX(scaleUpValue).scaleY(scaleUpValue).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.btnLayoutType.animate().scaleX(scaleDownValue).scaleY(scaleDownValue).setDuration(100).start()
                }
            }

            return@setOnTouchListener view?.onTouchEvent(motionEvent) ?: true
        }
    }

    private val itemDecorator by lazy {
        val dimenXS = dimenPixelSize(id.co.edtslib.uikit.R.dimen.xs) ?: 12.px.toInt()
        val dimenS = dimenPixelSize(id.co.edtslib.uikit.R.dimen.s) ?: 16.px.toInt()

        return@lazy when(layoutViewType) {
            LayoutViewType.LINEAR -> linearMarginItemDecoration(
                orientation = LinearLayoutManager.VERTICAL,
                margin = MarginItem(
                    top = dimenXS.div(2),
                    left = dimenS,
                    right = dimenS,
                    bottom = dimenXS.div(2),
                )
            )

            LayoutViewType.GRID -> gridMarginItemDecoration(
                spanCount = 2,
                spacing = MarginItem(
                    top = dimenS.div(2),
                    left = dimenS.div(2),
                    right = dimenS.div(2),
                    bottom = dimenS.div(2),
                    first = dimenS,
                    last = dimenS
                ),
                includeEdge = true
            )
        }
    }


    private fun setupRecyclerView() {
        binding.rvLayoutChange.addItemDecoration(itemDecorator)
        val animationAdapter = AnimationWrapperAdapter(adapter, AnimationType.Scale())

        binding.rvLayoutChange.adapter = animationAdapter
    }

    enum class LayoutViewType {
        GRID, LINEAR
    }


    data class DummyItem(
        val image: String,
        val title: String,
        val availability: Pair<Int, Int>,
        val voucherType: String,
        val point: Int
    )
}