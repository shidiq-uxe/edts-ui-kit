package id.co.edtslib.uikit.adapter

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

class AnimationWrapperAdapter(
    wrapped: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
    animationType: AnimationType
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var interpolator: Interpolator = LinearInterpolator()
    private var duration = WRAPPER_DEFAULT_DURATION
    private var lastPosition = RecyclerView.NO_POSITION
    private var shouldAnimateDuringScroll = true
    private var animatorType = animationType

    private var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    init {
        @Suppress("UNCHECKED_CAST")
        this.adapter = wrapped as RecyclerView.Adapter<RecyclerView.ViewHolder>
        super.setHasStableIds(this.adapter.hasStableIds())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return adapter.onCreateViewHolder(parent, viewType)
    }

    override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
        adapter.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
        adapter.unregisterAdapterDataObserver(observer)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        adapter.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        adapter.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        adapter.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        adapter.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapter.onBindViewHolder(holder, position)

        val adapterPosition = holder.adapterPosition
        if (shouldAnimateDuringScroll || adapterPosition > lastPosition) {
            val animators = getAnimators(holder.itemView, animatorType)

            animators.forEach { it.applyAttributes().start() }

            lastPosition = adapterPosition
        } else {
            holder.itemView.resetAnimationAttributes()
        }
    }

    private fun Animator.applyAttributes(): Animator {
        this.duration = this@AnimationWrapperAdapter.duration.toLong()
        this.interpolator = this@AnimationWrapperAdapter.interpolator
        return this
    }

    private fun getAnimators(
        view: View,
        animationType: AnimationType
    ): List<Animator> {
        return when (animationType) {
            is AnimationType.Fade -> listOf(
                ObjectAnimator.ofFloat(view, "alpha", animationType.startOpacity, 1f)
            )
            is AnimationType.Scale -> listOf(
                ObjectAnimator.ofFloat(view, "scaleX", animationType.fromScaleX, animationType.toScaleX),
                ObjectAnimator.ofFloat(view, "scaleY", animationType.fromScaleY, animationType.toScaleY)
            )
            is AnimationType.SlideInLeft -> listOf(
                ObjectAnimator.ofFloat(view, "translationX", -view.width.toFloat(), 0f)
            )
            is AnimationType.SlideInRight -> listOf(
                ObjectAnimator.ofFloat(view, "translationX", view.width.toFloat(), 0f)
            )
        }
    }

    fun setAnimationType(animationType: AnimationType) {
        this.animatorType = animationType
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        adapter.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return adapter.itemCount
    }

    fun setDuration(duration: Int) {
        this.duration = duration
    }

    fun setInterpolator(interpolator: Interpolator) {
        this.interpolator = interpolator
    }

    fun setStartPosition(start: Int) {
        lastPosition = start
    }

    fun shouldAnimateDuringScroll(animateDuringScroll: Boolean) {
        shouldAnimateDuringScroll = animateDuringScroll
    }

    override fun getItemViewType(position: Int): Int {
        return adapter.getItemViewType(position)
    }

    val wrappedAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
        get() = adapter

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        adapter.setHasStableIds(hasStableIds)
    }

    override fun getItemId(position: Int): Long {
        return adapter.getItemId(position)
    }

    private fun View.resetAnimationAttributes() {
        alpha = 1f
        scaleY = 1f
        scaleX = 1f
        translationY = 0f
        translationX = 0f
        rotation = 0f
        rotationY = 0f
        rotationX = 0f
        pivotY = measuredHeight / 2f
        pivotX = measuredWidth / 2f
        animate().setInterpolator(null).startDelay = 0
    }

    companion object {
        private const val WRAPPER_DEFAULT_DURATION = 300
    }
}

sealed class AnimationType {
    data class Fade(
        val startOpacity: Float = 0f
    ) : AnimationType()
    data class Scale(
        val fromScaleX: Float = 0.7f,
        val fromScaleY: Float = 0.7f,
        val toScaleX: Float = 1f,
        val toScaleY: Float = 1f
    ) : AnimationType()
    object SlideInLeft : AnimationType()
    object SlideInRight : AnimationType()
}