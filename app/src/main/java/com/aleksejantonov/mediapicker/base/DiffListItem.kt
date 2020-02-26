package com.aleksejantonov.mediapicker.base

interface DiffListItem {
    fun isTheSame(other: DiffListItem): Boolean
    fun isContentTheSame(other: DiffListItem): Boolean
}