package com.aleksejantonov.mediapicker.base

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.math.roundToInt

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

fun <E> MutableCollection<E>.replaceAll(collection: Collection<E>) {
    clear()
    addAll(collection)
}

fun Context.getPxFromDp(dpValue: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics
    )
        .toInt()
}

fun <T : Fragment> T.withArguments(action: Bundle.() -> Unit): T {
    arguments = Bundle().apply(action)
    return this
}

fun Context.dpToPx(dp: Float): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).roundToInt()
}

fun TextView.textColor(id: Int) {
    setTextColor(ContextCompat.getColor(context, id))
}

fun View.dpToPx(dp: Float): Int {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).roundToInt()
}