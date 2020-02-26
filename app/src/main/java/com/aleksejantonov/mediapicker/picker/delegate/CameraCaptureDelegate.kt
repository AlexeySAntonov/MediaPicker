package com.aleksejantonov.mediapicker.picker.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.mediapicker.picker.delegate.items.CameraCaptureItem
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.DiffListItem

class CameraCaptureDelegate(
    private val listener: () -> Unit
) : AbsListItemAdapterDelegate<CameraCaptureItem, DiffListItem, CameraCaptureDelegate.ViewHolder>() {

    override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is CameraCaptureItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camera_capture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: CameraCaptureItem, viewHolder: ViewHolder, payloads: MutableList<Any>) {
        viewHolder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: CameraCaptureItem) {
            with(itemView) {
                setOnClickListener { listener.invoke() }
            }
        }
    }
}