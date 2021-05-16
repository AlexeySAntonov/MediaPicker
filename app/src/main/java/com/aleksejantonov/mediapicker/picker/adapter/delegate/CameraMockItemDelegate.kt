package com.aleksejantonov.mediapicker.picker.adapter.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.CameraMockItem
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate

class CameraMockItemDelegate : AbsListItemAdapterDelegate<CameraMockItem, DiffListItem, CameraMockItemDelegate.ViewHolder>() {

  override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is CameraMockItem

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    return ViewHolder(FrameLayout(parent.context))
  }

  override fun onBindViewHolder(item: CameraMockItem, viewHolder: ViewHolder, payloads: MutableList<Any>) = Unit

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}