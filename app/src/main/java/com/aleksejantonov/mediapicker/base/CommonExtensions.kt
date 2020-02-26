package com.aleksejantonov.mediapicker.base

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*

fun Context.getScreenHeight(): Int {
    val size = Point()
    (this as Activity).windowManager.defaultDisplay.getSize(size)
    return size.y
}

fun Fragment.getColor(@ColorRes colorId: Int): Int = context?.let {
    ContextCompat.getColor(it, colorId)
} ?: 0

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

fun View.animateScale(scaleFactor: Float, duration: Long = 300L) {
    animate()
        .scaleX(scaleFactor)
        .scaleY(scaleFactor)
        .setDuration(duration)
        .start()
}

fun View.animateVisibility(show: Boolean) {
    animate()
        .alpha(if (show) 1f else 0f)
        .withStartAction { if (show) isVisible = true }
        .withEndAction { if (!show) isVisible = false }
        .setDuration(200)
        .start()
}

fun formatDuration(duration: Int, isLong: Boolean): String {
    val h = duration / 3600
    val m = duration / 60 % 60
    val s = duration % 60
    return if (h == 0) {
        if (isLong) {
            String.format(Locale.US, "%02d:%02d", m, s)
        } else {
            String.format(Locale.US, "%d:%02d", m, s)
        }
    } else {
        String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    }
}