package com.aleksejantonov.mediapicker.base

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.math.roundToInt

fun Context.getScreenHeight(): Int {
    val size = Point()
    (this as Activity).windowManager.defaultDisplay.getSize(size)
    return size.y
}

fun Context.getScreenWidth(): Int {
    val size = Point()
    (this as Activity).windowManager.defaultDisplay.getSize(size)
    return size.x
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

fun Context.getPxFromDp(dpValue: Int): Int =
    TypedValue
    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics)
    .roundToInt()

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

fun View.setPaddings(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    setPadding(
        left ?: paddingLeft,
        top ?: paddingTop,
        right ?: paddingRight,
        bottom ?: paddingBottom
    )
}

fun View.statusBarHeight(): Int {
    return (context as Activity).statusBarHeight()
}

fun Activity.statusBarHeight(): Int {
    val rectangle = Rect()
    val window = window
    window.decorView.getWindowVisibleDisplayFrame(rectangle)
    return if (rectangle.top > 0) rectangle.top else statusBarHeightFromResources()
}

fun Activity.statusBarHeightFromResources(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}

fun View.navBarHeight(isLandscapeMode: Boolean = false): Int {
    return context.navBarHeight(isLandscapeMode)
}

fun Context.navBarHeight(isLandscapeMode: Boolean = false): Int {
    if (hasSoftBottomBar(isLandscapeMode)) {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId)
        }
    }
    return 0
}

fun Context.hasSoftBottomBar(isLandscapeMode: Boolean = false): Boolean {
    val bottomBarHeight = dpToPx(16f) // 16 is bottom bar height on android 10
    val screenSize = screenSize(this)
    val fullSize = fullScreenSize(this)
    return if (!isLandscapeMode) fullSize.y - screenSize.y >= bottomBarHeight
    else fullSize.x - screenSize.x >= bottomBarHeight
}

fun screenSize(context: Context): Point {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

fun fullScreenSize(context: Context): Point {
    // fix for cases when bottom of the screen has soft action bar that 'steals' tiny amount of parent height
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val size = Point()
    display.getRealSize(size)
    return size
}

fun View.toast(text: String, long: Boolean = false, gravity: Int? = null) {
    (context as? Activity)?.let { activity ->
        Toast.makeText(activity, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).apply {
            gravity?.let { setGravity(it, 0, 0) }
            show()
        }
    }
}

fun View.drawBitmap(x: Int = 0, y: Int = 0, w: Int = width, h: Int = height): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return Bitmap.createBitmap(bitmap, x, y, w, h)
}

fun View.setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val marginLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    marginLayoutParams.setMargins(
        left ?: marginLayoutParams.leftMargin,
        top ?: marginLayoutParams.topMargin,
        right ?: marginLayoutParams.rightMargin,
        bottom ?: marginLayoutParams.bottomMargin
    )
    this.layoutParams = marginLayoutParams
}

fun View.hideAndShowWithDelay(delay: Long) {
    animate()
        .setDuration(delay)
        .withStartAction { isVisible = false }
        .withEndAction { isVisible = true }
        .start()
}