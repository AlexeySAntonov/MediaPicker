package com.aleksejantonov.mediapicker.picker.adapter.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.CameraCaptureItem
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate

class CameraCaptureDelegate(
    private val listener: () -> Unit
) : AbsListItemAdapterDelegate<CameraCaptureItem, DiffListItem, CameraCaptureDelegate.ViewHolder>() {

    override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is CameraCaptureItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camera, parent, false)
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