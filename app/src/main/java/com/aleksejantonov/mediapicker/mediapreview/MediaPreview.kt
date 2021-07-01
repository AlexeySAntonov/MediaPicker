package com.aleksejantonov.mediapicker.mediapreview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.dpToPx
import com.aleksejantonov.mediapicker.base.navBarHeight
import com.aleksejantonov.mediapicker.base.setMargins
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.bumptech.glide.Glide
import java.io.File

class MediaPreview(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

  private var previewImageView: ImageView? = null
  private var clearButton: ImageView? = null
  private var applyButton: ImageView? = null

  private var dismissListener: (() -> Unit)? = null

  init {
    isClickable = true
    isFocusable = true
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      width = LayoutHelper.MATCH_PARENT,
      height = LayoutHelper.MATCH_PARENT,
    )
    setupPreviewImageView()
    setupClearButton()
    setupApplyButton()
  }

  fun onDismiss(dismissListener: (() -> Unit)?) {
    this.dismissListener = dismissListener
  }

  private fun setupPreviewImageView() {
    previewImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
      )
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
      setBackgroundResource(R.drawable.selector_button_light)
      setOnClickListener { dismissListener?.invoke() }
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
      setBackgroundResource(R.drawable.selector_button_light)
      setOnClickListener { dismissListener?.invoke() }
    }
    applyButton?.let { addView(it) }
  }

  private fun loadPreview(file: File) {
    previewImageView?.let {
      Glide.with(this)
        .load(file)
        .centerCrop()
        .into(it)
    }
  }

  companion object {
    private const val CONTROLS_DIMEN = 56
    private const val CONTROLS_MARGIN = 40

    fun newInstance(context: Context, file: File): MediaPreview = MediaPreview(context).apply {
      loadPreview(file)
    }
  }

}