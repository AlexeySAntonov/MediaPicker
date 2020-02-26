package com.aleksejantonov.mediapicker.picker.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.aleksejantonov.mediapicker.base.DiffListItem
import com.aleksejantonov.mediapicker.picker.delegate.items.MediaItem
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.animateScale
import com.aleksejantonov.mediapicker.base.animateVisibility
import com.aleksejantonov.mediapicker.base.formatDuration
import com.aleksejantonov.mediapicker.base.isVisible
import com.bumptech.glide.Glide

class MediaItemDelegate(
    private val listener: (MediaItem) -> Unit
) : AbsListItemAdapterDelegate<MediaItem, DiffListItem, MediaItemDelegate.ViewHolder>() {

    override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is MediaItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: MediaItem, viewHolder: ViewHolder, payloads: MutableList<Any>) {
        viewHolder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.image)
        private val added = itemView.findViewById<ImageView>(R.id.added)
        private val empty = itemView.findViewById<ImageView>(R.id.empty)
        private val duration = itemView.findViewById<TextView>(R.id.duration)
        private val durationGroup = itemView.findViewById<Group>(R.id.durationGroup)

        fun bind(item: MediaItem) {
            with(itemView) {
                if (item.selected) {
                    imageView.scaleX = 1.0f
                    imageView.scaleY = 1.0f
                    imageView.animateScale(0.75f)
                } else {
                    imageView.scaleX = 0.75f
                    imageView.scaleY = 0.75f
                    imageView.animateScale(1.0f)
                }
                added.animateVisibility(item.selected)
                empty.animateVisibility(!item.selected)
                durationGroup.isVisible = item.type == MediaItem.Type.VIDEO
                duration.text = formatDuration(item.duration / 1000, false)
                setOnClickListener { listener.invoke(item) }

                Glide.with(imageView)
                    .load(item.uri)
                    .into(imageView)
            }
        }
    }
}