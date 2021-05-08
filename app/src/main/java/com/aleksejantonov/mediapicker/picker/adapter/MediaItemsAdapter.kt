package com.aleksejantonov.mediapicker.picker.adapter

import androidx.recyclerview.widget.DiffUtil
import com.aleksejantonov.mediapicker.base.ui.DiffCalculator
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.picker.adapter.delegate.CameraCaptureDelegate
import com.aleksejantonov.mediapicker.picker.adapter.delegate.MediaItemDelegate
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.GalleryMediaItem
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter

class MediaItemsAdapter(
  private val onCameraClick: () -> Unit,
  private val onMediaClick: (GalleryMediaItem) -> Unit,
) : ListDelegationAdapter<List<DiffListItem>>() {

  init {
    delegatesManager.apply {
      addDelegate(MediaItemDelegate(onMediaClick))
      addDelegate(CameraCaptureDelegate(onCameraClick))
    }
  }

  override fun setItems(newItems: List<DiffListItem>) {
    if (items == null) {
      items = newItems
      notifyDataSetChanged()
    } else {
      val diffResult = DiffUtil.calculateDiff(DiffCalculator(items, newItems))
      diffResult.dispatchUpdatesTo(this)
      items = newItems
    }
  }
}