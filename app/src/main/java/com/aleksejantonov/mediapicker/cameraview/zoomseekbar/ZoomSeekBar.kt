package com.aleksejantonov.mediapicker.cameraview.zoomseekbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper

class ZoomSeekBar(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

  private var seekBar: SeekBar? = null
  private var lessImageView: ImageView? = null
  private var moreImageView: ImageView? = null

  private var changeListener: ((Float) -> Unit)? = null

  init {
    setupLessMoreImageViews()
    setupSeekBar()
  }

  fun onChange(listener: (Float) -> Unit) {
    changeListener = listener
  }

  fun resetZoom() {
    seekBar?.progress = 0
  }

  private fun setupSeekBar() {
    seekBar = SeekBar(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.WRAP_CONTENT,
        gravity = Gravity.CENTER,
        leftMargin = LESS_MORE_DIMEN + LESS_MORE_MARGIN,
        rightMargin = LESS_MORE_DIMEN + LESS_MORE_MARGIN,
      )
      thumbTintList = ColorStateList.valueOf(Color.WHITE)
      progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.semiTransparentWhite))
      progressTintList = ColorStateList.valueOf(Color.WHITE)
      max = 100
      setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
          changeListener?.invoke(progress.toFloat() / 100)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
      })
    }
    seekBar?.let { addView(it) }
  }

  private fun setupLessMoreImageViews() {
    lessImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LESS_MORE_DIMEN,
        height = LESS_MORE_DIMEN,
        gravity = Gravity.START or Gravity.CENTER_VERTICAL,
        leftMargin = LESS_MORE_MARGIN,
      )
      setImageResource(R.drawable.ic_reduce_16dp)
    }
    lessImageView?.let { addView(it) }

    moreImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LESS_MORE_DIMEN,
        height = LESS_MORE_DIMEN,
        gravity = Gravity.END or Gravity.CENTER_VERTICAL,
        rightMargin = LESS_MORE_MARGIN,
      )
      setImageResource(R.drawable.ic_add_16dp)
    }
    moreImageView?.let { addView(it) }
  }

  companion object {
    const val LESS_MORE_DIMEN = 16
    const val LESS_MORE_MARGIN = 24
  }

}