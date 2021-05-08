package com.aleksejantonov.mediapicker.base.ui

interface DiffListItem {
    fun itemId(): Long

    companion object {
        const val CAMERA_ITEM_ID = -10L
    }
}