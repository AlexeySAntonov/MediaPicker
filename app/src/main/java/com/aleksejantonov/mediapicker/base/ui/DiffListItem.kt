package com.aleksejantonov.mediapicker.base.ui

interface DiffListItem {
    fun isTheSame(other: DiffListItem): Boolean
    fun isContentTheSame(other: DiffListItem): Boolean
}