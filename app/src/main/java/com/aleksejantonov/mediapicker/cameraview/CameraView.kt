package com.aleksejantonov.mediapicker.cameraview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.SL
import com.aleksejantonov.mediapicker.base.*
import com.aleksejantonov.mediapicker.base.ui.BottomSheetable
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper


class CameraView(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet), BottomSheetable {

  private val screenWidth by lazy { context.getScreenWidth() }
  private val initialDimen by lazy { (screenWidth - dpToPx(6f)) / 3 }
  private val initialTopMargin by lazy { statusBarHeight() + dpToPx(FAKE_TOOLBAR_HEIGHT.toFloat()) }
  private val initialTranslation by lazy { dpToPx(1f).toFloat() }
  private var safeHandler: Handler? = null
  private val cameraController by lazy { SL.initAndGetCameraController() }

  private var previewView: PreviewView? = null
  private var closeImageView: ImageView? = null
  private var overlayImageView: ImageView? = null
  private var focusAnimatorSet: AnimatorSet? = null

  private var onHideAnimStartedListener: (() -> Unit)? = null
  private var onHideAnimCompleteListener: (() -> Unit)? = null
  private val previewStreamStateObserver = Observer<PreviewView.StreamState> { onPreviewState(it) }

  init {
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      rawWidthPx = initialDimen,
      rawHeightPx = initialDimen,
      gravity = Gravity.TOP or Gravity.START
    )
    setMargins(top = initialTopMargin)
    translationX = initialTranslation
    translationY = initialTranslation
    setBackgroundResource(R.color.appBlack)
    isClickable = true
    isFocusable = true
    setupPreviewView()
    setupCloseButton()
    setupOverlayImageView()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    safeHandler = Handler(Looper.getMainLooper())
  }

  override fun onDetachedFromWindow() {
    focusAnimatorSet = null
    safeHandler?.removeCallbacksAndMessages(null)
    safeHandler = null
    previewView?.previewStreamState?.removeObserver(previewStreamStateObserver)
    super.onDetachedFromWindow()
  }

  override fun animateShow() {
    // Animate only when preview is ready
    previewView?.previewStreamState?.observeForever(previewStreamStateObserver)
  }

  private fun onPreviewState(state: PreviewView.StreamState) {
    if (state == PreviewView.StreamState.STREAMING) {
      previewView?.previewStreamState?.removeObserver(previewStreamStateObserver)
      val transitionSet = TransitionSet()
        .addTransition(ChangeBounds())
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setDuration(CAPTURE_APPEARANCE_DURATION)

      transitionSet.addListener(object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) {
          this@CameraView.elevation = dpToPx(4f).toFloat()
          overlayImageView?.isVisible = false
        }

        override fun onTransitionEnd(transition: Transition) {
          closeImageView?.isVisible = true
        }

        override fun onTransitionCancel(transition: Transition) = Unit

        override fun onTransitionPause(transition: Transition) = Unit

        override fun onTransitionResume(transition: Transition) = Unit
      })

      TransitionManager.beginDelayedTransition(this, transitionSet)

      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
        gravity = Gravity.TOP or Gravity.START
      )
      translationX = 0.0f
      translationY = 0.0f
      setMargins(top = 0)
    }
  }

  override fun animateHide() {
    val transitionSet = TransitionSet()
      .addTransition(ChangeBounds())
      .setInterpolator(AccelerateInterpolator())
      .setDuration(CAPTURE_DISAPPEARANCE_DURATION)

    transitionSet.addListener(object : Transition.TransitionListener {
      override fun onTransitionStart(transition: Transition) {
        cameraController.cancelFocusAndMetering()
        // SurfaceView animation limitation, cover with bitmap on hide transition for now
        // https://developer.android.com/training/transitions#Limitations
        overlayImageView?.setImageBitmap(previewView?.bitmap)
        overlayImageView?.isVisible = true
        previewView?.isVisible = false
        closeImageView?.isVisible = false
      }

      override fun onTransitionEnd(transition: Transition) {
        this@CameraView.elevation = 0f
        previewView?.isVisible = true
        overlayImageView?.setImageResource(R.drawable.ic_photo_camera_white_24dp)
        onHideAnimCompleteListener?.invoke()
      }

      override fun onTransitionCancel(transition: Transition) = Unit

      override fun onTransitionPause(transition: Transition) = Unit

      override fun onTransitionResume(transition: Transition) = Unit
    })

    TransitionManager.beginDelayedTransition(this, transitionSet)

    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      rawWidthPx = initialDimen,
      rawHeightPx = initialDimen,
      gravity = Gravity.TOP or Gravity.START
    )
    translationX = initialTranslation
    translationY = initialTranslation
    setMargins(top = initialTopMargin)
  }

  fun getSurfaceProvider(): Preview.SurfaceProvider? {
    return previewView?.surfaceProvider
  }

  fun onHideAnimationStarted(listener: () -> Unit) {
    this.onHideAnimStartedListener = listener
  }

  fun onHideAnimationComplete(listener: () -> Unit) {
    this.onHideAnimCompleteListener = listener
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun setupPreviewView() {
    previewView = PreviewView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
        gravity = Gravity.START or Gravity.TOP
      )
      setOnTouchListener { _, event ->
        // Convert UI coordinates into camera sensor coordinates
        val point = meteringPointFactory.createPoint(event.x, event.y)

        // Prepare focus action to be triggered
        val action = FocusMeteringAction.Builder(point).build()

        // Execute focus action
        cameraController.startFocusAndMetering(action).also { onCameraFocus(event.x, event.y) }

        return@setOnTouchListener true
      }
    }
    previewView?.let { addView(it) }
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
      setMargins(top = dpToPx(CLOSE_IMAGE_MARGIN.toFloat()) + statusBarHeight())
      scaleType = ImageView.ScaleType.CENTER
      setImageResource(R.drawable.ic_close_clear_24dp)
      setColorFilter(Color.WHITE)
      setBackgroundResource(R.drawable.selector_button_light)
      isVisible = false
      setOnClickListener { animateHide() }
    }
    closeImageView?.let { addView(it) }
  }

  private fun setupOverlayImageView() {
    overlayImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
        gravity = Gravity.START or Gravity.TOP
      )
      setImageResource(R.drawable.ic_photo_camera_white_24dp)
      scaleType = ImageView.ScaleType.CENTER
      setOnClickListener { animateShow() }
    }
    overlayImageView?.let { addView(it) }
  }

  private fun onCameraFocus(focusX: Float, focusY: Float) {
    val innerTouchView = View(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = FOCUS_VIEW_DIMEN,
        height = FOCUS_VIEW_DIMEN,
        gravity = Gravity.TOP or Gravity.START
      )
      alpha = 0.2f
      scaleX = 0.5f
      scaleY = 0.5f
      val dimenPx = dpToPx(FOCUS_VIEW_DIMEN.toFloat())
      x = focusX - dimenPx / 2
      y = focusY - dimenPx / 2
      setBackgroundResource(R.drawable.background_circle_white)
    }
    val outerTouchView = View(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = FOCUS_VIEW_DIMEN,
        height = FOCUS_VIEW_DIMEN,
        gravity = Gravity.TOP or Gravity.START
      )
      scaleX = 1.5f
      scaleY = 1.5f
      val dimenPx = dpToPx(FOCUS_VIEW_DIMEN.toFloat())
      x = focusX - dimenPx / 2
      y = focusY - dimenPx / 2
      setBackgroundResource(R.drawable.background_circle_outer_touch)
    }
    addView(innerTouchView)
    addView(outerTouchView)

    focusAnimatorSet?.cancel()
    focusAnimatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(innerTouchView, View.ALPHA, 0.2f, 0.8f)
          .setDuration(FOCUS_TOUCH_DURATION),
        ObjectAnimator.ofFloat(innerTouchView, View.SCALE_X, 0.5f, 1f)
          .setDuration(FOCUS_TOUCH_DURATION),
        ObjectAnimator.ofFloat(innerTouchView, View.SCALE_Y, 0.5f, 1f)
          .setDuration(FOCUS_TOUCH_DURATION),
        ObjectAnimator.ofFloat(outerTouchView, View.SCALE_X, 1.5f, 1f)
          .setDuration(FOCUS_TOUCH_DURATION),
        ObjectAnimator.ofFloat(outerTouchView, View.SCALE_Y, 1.5f, 1f)
          .setDuration(FOCUS_TOUCH_DURATION),
      )
      interpolator = AccelerateDecelerateInterpolator()
      doOnEnd {
        if (it == focusAnimatorSet) {
          focusAnimatorSet = null
          safeHandler?.postDelayed({
            this@CameraView.removeView(innerTouchView)
            this@CameraView.removeView(outerTouchView)
          }, 150L)
        }
      }
      start()
    }
  }

  companion object {
    private const val CLOSE_IMAGE_DIMEN = 48
    private const val CLOSE_IMAGE_MARGIN = 4
    private const val FAKE_TOOLBAR_HEIGHT = CLOSE_IMAGE_DIMEN + CLOSE_IMAGE_MARGIN * 2
    private const val FOCUS_VIEW_DIMEN = 48

    const val CAPTURE_APPEARANCE_DURATION = 220L
    private const val CAPTURE_DISAPPEARANCE_DURATION = 120L
    private const val FOCUS_TOUCH_DURATION = 350L

    fun newInstance(parentContext: Context) = CameraView(parentContext)
  }
}