package com.aleksejantonov.mediapicker.picker.delegate.items

import android.graphics.Bitmap
import com.aleksejantonov.mediapicker.base.ui.DiffListItem

data class CameraCaptureItem(
    val frame: Bitmap? = null
) : DiffListItem {

    override fun isTheSame(other: DiffListItem): Boolean = other is CameraCaptureItem
    override fun isContentTheSame(other: DiffListItem): Boolean = other is CameraCaptureItem
            && this == other
}