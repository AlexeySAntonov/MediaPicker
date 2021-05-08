package com.aleksejantonov.mediapicker.picker.adapter.delegate.items

import android.graphics.Bitmap
import com.aleksejantonov.mediapicker.base.ui.DiffListItem

data class CameraCaptureItem(
    val frame: Bitmap? = null
) : DiffListItem {

    override fun itemId(): Long = DiffListItem.CAMERA_ITEM_ID
}