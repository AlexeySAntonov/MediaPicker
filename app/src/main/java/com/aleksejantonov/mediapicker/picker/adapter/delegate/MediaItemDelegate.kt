package com.aleksejantonov.mediapicker.picker.adapter.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.*
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.picker.adapter.MediaItemsAdapter.Companion.PAYLOAD_ORDER_NUMBER
import com.aleksejantonov.mediapicker.picker.adapter.MediaItemsAdapter.Companion.PAYLOAD_SELECTED
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.GalleryMediaItem
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate

class MediaItemDelegate(
  private val glide: RequestManager,
  private val listener: (GalleryMediaItem) -> Unit
) : AbsListItemAdapterDelegate<GalleryMediaItem, DiffListItem, MediaItemDelegate.ViewHolder>() {

  override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is GalleryMediaItem

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    val size = parent.context.getScreenWidth() / 3
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
    return ViewHolder(view, size)
  }

  override fun onBindViewHolder(item: GalleryMediaItem, viewHolder: ViewHolder, payloads: MutableList<Any>) {
    if (payloads.any { it == PAYLOAD_SELECTED }) {
      viewHolder.updateSelected(item)
      return
    }
    if (payloads.any { it == PAYLOAD_ORDER_NUMBER }) {
      viewHolder.updateOrderNumber(item)
      return
    }
    viewHolder.bind(item)
  }

  override fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) {
    (viewHolder as ViewHolder).onViewRecycled()
  }

  inner class ViewHolder(itemView: View, private val size: Int) : RecyclerView.ViewHolder(itemView) {
    private val imageView = itemView.findViewById<ImageView>(R.id.image)
    private val checkTextView = itemView.findViewById<TextView>(R.id.checkTextView)
    private val duration = itemView.findViewById<TextView>(R.id.duration)
    private val durationGroup = itemView.findViewById<Group>(R.id.durationGroup)

    fun bind(item: GalleryMediaItem) {
      with(itemView) {
        durationGroup.isVisible = item.isVideo
        duration.text = formatDuration(item.duration.toInt() / 1000, false)
        setOnClickListener { listener.invoke(item) }

        glide.load(item.uri)
          .override(size)
          .thumbnail(
            glide.load(item.uri)
              .override(50)
              .transition(DrawableTransitionOptions.withCrossFade(150))
          )
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .into(imageView)

        updateSelected(item)
        updateOrderNumber(item)
      }
    }

    fun updateSelected(item: GalleryMediaItem) {
      updateOrderNumber(item)
      if (item.selected) {
        imageView.scaleX = 1.0f
        imageView.scaleY = 1.0f
        imageView.animateScale(0.75f, duration = 220L)
      } else {
        imageView.scaleX = 0.75f
        imageView.scaleY = 0.75f
        imageView.animateScale(1.0f, duration = 220L)
      }
    }

    fun updateOrderNumber(item: GalleryMediaItem) {
      checkTextView.setBackgroundResource(if (item.selected) R.drawable.background_circle_green_border else R.drawable.background_circle_border)
      checkTextView.text = if (item.selected) item.orderNumber.toString() else String()
    }

    fun onViewRecycled() = releaseGlide(imageView)
  }
}