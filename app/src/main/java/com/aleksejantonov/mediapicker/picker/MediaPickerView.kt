package com.aleksejantonov.mediapicker.picker

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.getScreenHeight
import com.aleksejantonov.mediapicker.base.textColor
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.aleksejantonov.mediapicker.picker.adapter.MediaItemsAdapter
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.GalleryMediaItem

class MediaPickerView(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

  private val screenHeight by lazy { context.getScreenHeight() }

  private var closeImageView: ImageView? = null
  private var titleTextView: TextView? = null
  private var mediaRecyclerView: RecyclerView? = null

  private var onCameraClickListener: (() -> Unit)? = null
  private var onMediaClickListener: ((GalleryMediaItem) -> Unit)? = null

  private val mediaAdapter by lazy {
    MediaItemsAdapter(
      onCameraClick = { onCameraClickListener?.invoke() },
      onMediaClick = { onMediaClickListener?.invoke(it) }
    )
  }

  init {
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      width = LayoutHelper.MATCH_PARENT,
      rawHeightPx = screenHeight,
    )
    translationY = screenHeight.toFloat()
    setBackgroundResource(R.color.white)
    setupCloseButton()
    setupTitle()
    setupRecyclerView()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mediaRecyclerView?.adapter = mediaAdapter
  }

  override fun onDetachedFromWindow() {
    mediaRecyclerView?.adapter = null
    super.onDetachedFromWindow()
  }

  fun onCameraClick(listener: () -> Unit) {
    this.onCameraClickListener = listener
  }

  fun onMediaClick(listener: (GalleryMediaItem) -> Unit) {
    this.onMediaClickListener = listener
  }

  private fun setupCloseButton() {
    closeImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = CLOSE_IMAGE_DIMEN,
        height = CLOSE_IMAGE_DIMEN,
        topMargin = CLOSE_IMAGE_MARGIN,
        leftMargin = CLOSE_IMAGE_MARGIN,
        gravity = Gravity.START or Gravity.TOP
      )
      setImageResource(R.drawable.ic_close_clear_24dp)
      setOnClickListener { }
    }
    closeImageView?.let { addView(it) }
  }

  private fun setupTitle() {
    titleTextView = TextView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = CLOSE_IMAGE_DIMEN + CLOSE_IMAGE_MARGIN * 2,
        leftMargin = CLOSE_IMAGE_DIMEN + CLOSE_IMAGE_MARGIN * 2 + TITLE_TEXT_MARGIN,
        rightMargin = TITLE_TEXT_MARGIN,
        gravity = Gravity.START or Gravity.TOP
      )
      gravity = Gravity.START or Gravity.CENTER_VERTICAL
      textSize = 16f
      textColor(R.color.appTextColor)
      typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    titleTextView?.let { addView(it) }
  }

  private fun setupRecyclerView() {
    mediaRecyclerView = RecyclerView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
        topMargin = CLOSE_IMAGE_DIMEN + CLOSE_IMAGE_MARGIN * 2
      )
      layoutManager = GridLayoutManager(context, 3)
      setHasFixedSize(true)
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
    mediaRecyclerView?.let { addView(it) }
  }

  private companion object {
    const val CLOSE_IMAGE_DIMEN = 48
    const val CLOSE_IMAGE_MARGIN = 4
    const val TITLE_TEXT_MARGIN = 16
  }
}