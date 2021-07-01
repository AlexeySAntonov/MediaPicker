package com.aleksejantonov.mediapicker.mediapreview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.dpToPx
import com.aleksejantonov.mediapicker.base.navBarHeight
import com.aleksejantonov.mediapicker.base.setMargins
import com.aleksejantonov.mediapicker.base.ui.AnimatableAppearance
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import timber.log.Timber
import java.io.File
import java.io.IOException

class MediaPreview(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet), AnimatableAppearance {

  private var previewImageView: ImageView? = null
  private var clearButton: ImageView? = null
  private var applyButton: ImageView? = null
  private var animatorSet: AnimatorSet? = null

  private var onHideAnimCompleteListener: (() -> Unit)? = null

  init {
    isClickable = true
    isFocusable = true
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      width = LayoutHelper.MATCH_PARENT,
      height = LayoutHelper.MATCH_PARENT,
      gravity = Gravity.CENTER
    )
    scaleX = 0.25f
    scaleY = 0.25f
    isVisible = false
    setBackgroundResource(R.color.appBlack)
    setupPreviewImageView()
    setupClearButton()
    setupApplyButton()
  }

  override fun onDetachedFromWindow() {
    animatorSet = null
    super.onDetachedFromWindow()
  }

  override fun animateShow() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(this@MediaPreview, View.SCALE_X, 1f)
          .setDuration(PREVIEW_APPEARANCE_DURATION),
        ObjectAnimator.ofFloat(this@MediaPreview, View.SCALE_Y, 1f)
          .setDuration(PREVIEW_APPEARANCE_DURATION),
      )
      interpolator = AccelerateDecelerateInterpolator()
      doOnStart { this@MediaPreview.isVisible = true }
      doOnEnd { if (it == animatorSet) animatorSet = null }
      start()
    }
  }

  override fun animateHide() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(this@MediaPreview, View.SCALE_X, 0.25f)
          .setDuration(PREVIEW_DISAPPEARANCE_DURATION),
        ObjectAnimator.ofFloat(this@MediaPreview, View.SCALE_Y, 0.25f)
          .setDuration(PREVIEW_DISAPPEARANCE_DURATION),
      )
      interpolator = AccelerateInterpolator()
      doOnEnd {
        if (it == animatorSet) animatorSet = null
        onHideAnimCompleteListener?.invoke()
      }
      start()
    }
  }

  fun onHideAnimationComplete(listener: () -> Unit) {
    this.onHideAnimCompleteListener = listener
  }

  private fun setupPreviewImageView() {
    previewImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
      )
      scaleType = ImageView.ScaleType.CENTER_CROP
    }
    previewImageView?.let { addView(it) }
  }

  private fun setupClearButton() {
    clearButton = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = CONTROLS_DIMEN,
        height = CONTROLS_DIMEN,
        leftMargin = CONTROLS_MARGIN,
        gravity = Gravity.START or Gravity.BOTTOM
      )
      setMargins(bottom = dpToPx(CONTROLS_MARGIN.toFloat()) + navBarHeight())
      scaleType = ImageView.ScaleType.CENTER
      setImageResource(R.drawable.ic_delete_40)
      setColorFilter(Color.WHITE)
      setBackgroundResource(R.drawable.background_circle_grey)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        foreground = ResourcesCompat.getDrawable(resources, R.drawable.selector_button_light, context.theme)
      }
      setOnClickListener { animateHide() }
    }
    clearButton?.let { addView(it) }
  }

  private fun setupApplyButton() {
    applyButton = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = CONTROLS_DIMEN,
        height = CONTROLS_DIMEN,
        rightMargin = CONTROLS_MARGIN,
        gravity = Gravity.END or Gravity.BOTTOM
      )
      setMargins(bottom = dpToPx(CONTROLS_MARGIN.toFloat()) + navBarHeight())
      scaleType = ImageView.ScaleType.CENTER
      setImageResource(R.drawable.ic_done_40)
      setColorFilter(Color.WHITE)
      setBackgroundResource(R.drawable.background_circle_grey)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        foreground = ResourcesCompat.getDrawable(resources, R.drawable.selector_button_light, context.theme)
      }
      setOnClickListener { animateHide() }
    }
    applyButton?.let { addView(it) }
  }

  private fun loadPreview(file: File) {
    val rotation = file.rotation()
    val sourceBitmap = BitmapFactory.decodeFile(file.absolutePath)
    val matrix = Matrix()
    matrix.postRotate(rotation)
    val rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
    previewImageView?.setImageBitmap(rotatedBitmap)
    animateShow()
  }

  private fun File.rotation(): Float {
    var orientation = ExifInterface.ORIENTATION_UNDEFINED
    try {
      val exif = ExifInterface(path)
      orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
    } catch (ignored: IOException) {
      Timber.e("Error reading EXIF %s", ignored.message ?: "")
    }
    return when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> 90f
      ExifInterface.ORIENTATION_ROTATE_180 -> 180f
      ExifInterface.ORIENTATION_ROTATE_270 -> 270f
      else -> 0f
    }
  }

  companion object {
    private const val CONTROLS_DIMEN = 56
    private const val CONTROLS_MARGIN = 40

    private const val PREVIEW_APPEARANCE_DURATION = 150L
    private const val PREVIEW_DISAPPEARANCE_DURATION = 100L

    fun newInstance(context: Context, file: File): MediaPreview = MediaPreview(context).apply {
      loadPreview(file)
    }
  }

}