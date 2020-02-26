package com.aleksejantonov.mediapicker.picker

import com.aleksejantonov.mediapicker.base.DiffListItem

interface MediaPickerView {
    fun showItems(items: List<DiffListItem>, selectedCount: Int)
    fun dispatchTakePictureIntent()
}