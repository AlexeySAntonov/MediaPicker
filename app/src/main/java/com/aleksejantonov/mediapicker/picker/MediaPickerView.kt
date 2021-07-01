package com.aleksejantonov.mediapicker.picker

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.SL
import com.aleksejantonov.mediapicker.base.*
import com.aleksejantonov.mediapicker.base.ui.BottomSheetable
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.aleksejantonov.mediapicker.cameraview.CameraView
import com.aleksejantonov.mediapicker.picker.adapter.MediaItemsAdapter
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.GalleryMediaItem
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class MediaPickerView(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet), BottomSheetable, LifecycleOwner {

  private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
  private val cameraController by lazy { SL.initAndGetCameraController() }

  private val screenWidth by lazy { context.getScreenWidth() }
  private val screenHeight by lazy { context.getScreenHeight() }
  private var singleImage: Boolean = false
  private var limit: Int = 24

  private var mediaRecyclerView: RecyclerView? = null
  private var cameraView: CameraView? = null
  private var doneButton: MaterialButton? = null
  private var fakeToolbarOverlay: View? = null
  private var closeImageView: ImageView? = null
  private var titleTextView: TextView? = null
  private var animatorSet: AnimatorSet? = null

  private var onHideAnimCompleteListener: (() -> Unit)? = null
  private var cameraViewActive: Boolean = false

  private val safeHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

  private val mediaAdapter by lazy {
    MediaItemsAdapter(
      glide = Glide.with(this),
      onMediaClick = { viewModel.onMediaClick(it) },
    )
  }

  private val scrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      // OMG 😂 Even changing cameraX surfaces so expensive and lagging so it's easier to mimicking cell behaviour
      cameraView?.translationY = (cameraView?.translationY ?: 0f) - dy
    }
  }

  private val contentObserver = Observer<List<DiffListItem>> { bindItems(it) }
  private val limitObserver = Observer<Unit> { onSelectionLimitReached() }

  private val viewModel by lazy {
    MediaPickerViewModel(
      mediaProvider = SL.initAndGetMediaProvider(),
      singleImage = singleImage,
      limit = limit
    )
  }

  init {
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      width = LayoutHelper.MATCH_PARENT,
      rawHeightPx = LayoutHelper.MATCH_PARENT,
    )
    translationY = screenHeight.toFloat()
    setBackgroundResource(R.color.white)
    setupRecyclerView()
    setupDoneButton()
    setupCameraView()
    setupFakeToolbarOverlay()
    setupCloseButton()
    setupTitle()
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    mediaRecyclerView?.adapter = mediaAdapter
    mediaRecyclerView?.addOnScrollListener(scrollListener)
    viewModel.content.observeForever(contentObserver)
    viewModel.limitEvent.observeForever(limitObserver)
  }

  override fun onDetachedFromWindow() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    viewModel.limitEvent.removeObserver(limitObserver)
    viewModel.content.removeObserver(contentObserver)
    viewModel.dispatchOnCleared()
    mediaRecyclerView?.adapter = null
    mediaRecyclerView?.removeOnScrollListener(scrollListener)
    animatorSet = null
    cameraController.releaseCameraProvider()
    safeHandler.removeCallbacksAndMessages(null)
    super.onDetachedFromWindow()
  }

  override fun animateShow() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(this@MediaPickerView, View.TRANSLATION_Y, screenHeight.toFloat(), 0f)
          .setDuration(GALLERY_APPEARANCE_DURATION),
        ObjectAnimator.ofFloat(requireNotNull(closeImageView), View.ALPHA, 0f, 1f)
          .setDuration(GALLERY_APPEARANCE_DURATION),
        ObjectAnimator.ofFloat(requireNotNull(titleTextView), View.ALPHA, 0f, 1f)
          .setDuration(GALLERY_APPEARANCE_DURATION),
      )
      interpolator = AccelerateDecelerateInterpolator()
      doOnEnd { if (it == animatorSet) animatorSet = null }
      start()
    }
  }

  override fun animateHide() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(this@MediaPickerView, View.TRANSLATION_Y, 0f, screenHeight.toFloat())
          .setDuration(GALLERY_DISAPPEARANCE_DURATION),
        ObjectAnimator.ofFloat(requireNotNull(closeImageView), View.ALPHA, 1f, 0f)
          .setDuration(GALLERY_DISAPPEARANCE_DURATION),
        ObjectAnimator.ofFloat(requireNotNull(titleTextView), View.ALPHA, 1f, 0f)
          .setDuration(GALLERY_DISAPPEARANCE_DURATION),
      )
      interpolator = AccelerateInterpolator()
      doOnEnd {
        if (it == animatorSet) {
          animatorSet = null
          onHideAnimCompleteListener?.invoke()
        }
      }
      start()
    }
  }

  override fun getLifecycle(): Lifecycle {
    return lifecycleRegistry
  }

  fun onBackPressed() {
    if (cameraViewActive) cameraView?.onBackPressed()
    else animateHide()
  }

  fun onHideAnimationComplete(listener: () -> Unit) {
    this.onHideAnimCompleteListener = listener
  }

  private fun setupFakeToolbarOverlay() {
    fakeToolbarOverlay = View(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        rawHeightPx = statusBarHeight() + dpToPx(FAKE_TOOLBAR_HEIGHT.toFloat()),
        gravity = Gravity.START or Gravity.TOP
      )
      setBackgroundResource(R.color.white)
    }
    fakeToolbarOverlay?.let { addView(it) }
  }

  private fun setupCloseButton() {
    closeImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = CLOSE_IMAGE_DIMEN,
        height = CLOSE_IMAGE_DIMEN,
        leftMargin = CLOSE_IMAGE_MARGIN,
        gravity = Gravity.START or Gravity.TOP
      )
      setMargins(top = statusBarHeight() + dpToPx(CLOSE_IMAGE_MARGIN.toFloat()))
      scaleType = ImageView.ScaleType.CENTER
      setImageResource(R.drawable.ic_close_clear_24dp)
      setBackgroundResource(R.drawable.selector_button_dark)
      setOnClickListener { animateHide() }
    }
    closeImageView?.let { addView(it) }
  }

  private fun setupTitle() {
    titleTextView = TextView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = FAKE_TOOLBAR_HEIGHT,
        leftMargin = FAKE_TOOLBAR_HEIGHT,
        rightMargin = TITLE_TEXT_MARGIN,
        gravity = Gravity.START or Gravity.TOP
      )
      setMargins(top = statusBarHeight())
      gravity = Gravity.START or Gravity.CENTER_VERTICAL
      setLines(1)
      ellipsize = TextUtils.TruncateAt.END
      setText(if (singleImage) R.string.media_picker_select_images_title else R.string.media_picker_select_media_title)
      textSize = 18f
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
        gravity = Gravity.TOP
      )
      setMargins(top = statusBarHeight() + dpToPx(FAKE_TOOLBAR_HEIGHT.toFloat()))
      layoutManager = GridLayoutManager(context, 3)
      setHasFixedSize(true)
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setPaddings(bottom = navBarHeight())
      clipToPadding = false
    }
    mediaRecyclerView?.let { addView(it) }
  }

  private fun setupDoneButton() {
    doneButton = MaterialButton(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = DONE_BUTTON_DIMEN,
        leftMargin = DONE_BUTTON_MARGIN,
        rightMargin = DONE_BUTTON_MARGIN,
        bottomMargin = DONE_BUTTON_MARGIN / 2,
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
      )
      insetTop = 0
      insetBottom = 0
      cornerRadius = dpToPx(DONE_BUTTON_DIMEN.toFloat() / 2)
      setRippleColorResource(R.color.semiTransparent)
      setText(R.string.done)
      setBackgroundColor(ContextCompat.getColor(context, R.color.appGreen))
      isVisible = false
      setOnClickListener { viewModel.performDoneAction() }
    }
    doneButton?.let { addView(it) }
  }

  private fun setupCameraView() {
    cameraView = CameraView.newInstance(context)
    cameraView?.let { addView(it) }
    cameraView?.onShowAnimationPreparation { suppressPickerUI(true) }
    cameraView?.onShowAnimationStarted { cameraViewActive = true }
    cameraView?.onHideAnimationComplete {
      suppressPickerUI(false)
      cameraViewActive = false
    }
    safeHandler.postDelayed(
      { cameraView?.connectWithCameraController(lifeCycleOwner = this) },
      GALLERY_APPEARANCE_DURATION
    )
  }

  private fun bindItems(items: List<DiffListItem>) {
    mediaAdapter.items = items
    val selectedCount = items.count { it is GalleryMediaItem && it.selected }
    titleTextView?.text = if (selectedCount > 0) {
      if (singleImage) {
        resources.getQuantityString(
          R.plurals.media_picker_select_images_title_plural,
          selectedCount,
          selectedCount
        )
      } else {
        val selectedPhotosCount = items.filter { it is GalleryMediaItem && !it.isVideo && it.selected }.size
        when {
          selectedPhotosCount == selectedCount -> resources.getQuantityString(
            R.plurals.media_picker_select_images_title_plural,
            selectedCount,
            selectedCount
          )
          selectedPhotosCount != 0 -> resources.getQuantityString(
            R.plurals.media_picker_select_media_title_plural,
            selectedCount,
            selectedCount
          )
          else -> resources.getQuantityString(
            R.plurals.media_picker_select_video_title_plural,
            selectedCount,
            selectedCount
          )
        }
      }
    } else {
      if (singleImage) {
        context.getText(R.string.media_picker_select_images_title)
      } else {
        context.getText(R.string.media_picker_select_media_title)
      }
    }
    doneButton?.animateVisibility(selectedCount > 0)
  }

  private fun suppressPickerUI(suppress: Boolean) {
    mediaRecyclerView?.stopScroll()
  }

  private fun onSelectionLimitReached() {
    toast(resources.getString(R.string.you_have_reached_the_set_limit_of_media, limit))
  }

  companion object {
    private const val CLOSE_IMAGE_DIMEN = 48
    private const val CLOSE_IMAGE_MARGIN = 4
    private const val FAKE_TOOLBAR_HEIGHT = CLOSE_IMAGE_DIMEN + CLOSE_IMAGE_MARGIN * 2
    private const val TITLE_TEXT_MARGIN = 16
    private const val DONE_BUTTON_DIMEN = 48
    private const val DONE_BUTTON_MARGIN = 32

    const val GALLERY_APPEARANCE_DURATION = 330L
    private const val GALLERY_DISAPPEARANCE_DURATION = 220L

    fun newInstance(
      parentContext: Context,
      singleImage: Boolean = false,
      limit: Int = 24
    ) = MediaPickerView(parentContext).apply {
      this.singleImage = singleImage
      this.limit = limit
    }
  }
}