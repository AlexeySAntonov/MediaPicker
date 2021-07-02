package com.aleksejantonov.mediapicker.cameraview.captureview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.dpToPx
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.aleksejantonov.mediapicker.base.vibrate
import com.google.android.material.button.MaterialButton

class CaptureView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

  private var captureButton: MaterialButton? = null
  private var captureButtonFrame: View? = null

  private var captureClickListener: (() -> Unit)? = null

  init {
    setupCaptureFrame()
    setupCaptureButton()
  }

  fun onCaptureClick(listener: () -> Unit) {
    captureClickListener = listener
  }

  private fun setupCaptureFrame() {
    captureButtonFrame = View(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = CAPTURE_FRAME_DIMEN,
        height = CAPTURE_FRAME_DIMEN,
        gravity = Gravity.CENTER,
      )
      setBackgroundResource(R.drawable.background_circle_border)
    }
    captureButtonFrame?.let { addView(it) }
  }

  private fun setupCaptureButton() {
    captureButton = MaterialButton(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = CAPTURE_BUTTON_DIMEN,
        height = CAPTURE_BUTTON_DIMEN,
        gravity = Gravity.CENTER,
      )
      insetTop = 0
      insetBottom = 0
      cornerRadius = dpToPx(CAPTURE_BUTTON_DIMEN.toFloat()) / 2
      setRippleColorResource(R.color.semiTransparent)
      setBackgroundColor(ContextCompat.getColor(context, R.color.white))
      alpha = 0.75f
      setOnClickListener {
        context.vibrate(50L)
        captureClickListener?.invoke()
      }
    }
    captureButton?.let { addView(it) }
  }

  companion object {
    const val CAPTURE_FRAME_DIMEN = 64
    const val CAPTURE_FRAME_MARGIN = 20
    private const val CAPTURE_BUTTON_DIMEN = 56
  }
}