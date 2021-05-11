package com.aleksejantonov.mediapicker.picker.adapter

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.picker.adapter.delegate.CameraCaptureDelegate
import com.aleksejantonov.mediapicker.picker.adapter.delegate.MediaItemDelegate
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.GalleryMediaItem
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import java.lang.ref.WeakReference

class MediaItemsAdapter(
  private val lifeCycleOwner: WeakReference<LifecycleOwner>,
  private val onCameraClick: (Bitmap?) -> Unit,
  private val onMediaClick: (GalleryMediaItem) -> Unit,
) : AsyncListDifferDelegationAdapter<DiffListItem>(DIFF_CALLBACK) {

  init {
    delegatesManager.apply {
      addDelegate(MediaItemDelegate(onMediaClick))
      addDelegate(CameraCaptureDelegate(lifeCycleOwner, onCameraClick))
    }
  }

  companion object {
    const val PAYLOAD_SELECTED = "PAYLOAD_SELECTED"
    const val PAYLOAD_ORDER_NUMBER = "PAYLOAD_ORDER_NUMBER"

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DiffListItem>() {
      override fun areItemsTheSame(oldItem: DiffListItem, newItem: DiffListItem): Boolean {
        return oldItem.itemId() == newItem.itemId()
      }

      override fun areContentsTheSame(oldItem: DiffListItem, newItem: DiffListItem): Boolean {
        return oldItem.equals(newItem)
      }

      override fun getChangePayload(oldItem: DiffListItem, newItem: DiffListItem): Any? {
        if (oldItem is GalleryMediaItem && newItem is GalleryMediaItem) {
          if (oldItem.selected != newItem.selected) {
            return PAYLOAD_SELECTED
          }
          if (oldItem.orderNumber != newItem.orderNumber) {
            return PAYLOAD_ORDER_NUMBER
          }
        }
        return super.getChangePayload(oldItem, newItem)
      }
    }
  }
}