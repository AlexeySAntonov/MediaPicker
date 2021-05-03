package com.aleksejantonov.mediapicker.picker.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.*
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.picker.delegate.items.GalleryMediaItem
import com.bumptech.glide.Glide

class MediaItemDelegate(
    private val listener: (GalleryMediaItem) -> Unit
) : AbsListItemAdapterDelegate<GalleryMediaItem, DiffListItem, MediaItemDelegate.ViewHolder>() {

    override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is GalleryMediaItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: GalleryMediaItem, viewHolder: ViewHolder, payloads: MutableList<Any>) {
        viewHolder.bind(item)
    }

    override fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) {
        (viewHolder as ViewHolder).onViewRecycled()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.image)
        private val added = itemView.findViewById<ImageView>(R.id.added)
        private val empty = itemView.findViewById<ImageView>(R.id.empty)
        private val duration = itemView.findViewById<TextView>(R.id.duration)
        private val durationGroup = itemView.findViewById<Group>(R.id.durationGroup)

        fun bind(item: GalleryMediaItem) {
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
                durationGroup.isVisible = item.isVideo
                duration.text = formatDuration(item.duration.toInt() / 1000, false)
                setOnClickListener { listener.invoke(item) }

                Glide.with(imageView)
                    .load(item.uri)
                    .into(imageView)
            }
        }

        fun onViewRecycled() = releaseGlide(imageView)
    }
}