package com.aleksejantonov.mediapicker.photocapture

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
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.*
import com.aleksejantonov.mediapicker.base.ui.BottomSheetable
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.google.common.util.concurrent.ListenableFuture
import timber.log.Timber


class PhotoCaptureView(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet), BottomSheetable, LifecycleOwner {

  private val screenHeight by lazy { context.getScreenHeight() }
  private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
  private var safeHandler: Handler? = null

  private var previewView: PreviewView? = null
  private var closeImageView: ImageView? = null
  private var initialFrameImageView: ImageView? = null
  private var animatorSet: AnimatorSet? = null
  private var focusAnimatorSet: AnimatorSet? = null

  // Used to bind the lifecycle of cameras to the lifecycle owner
  private var cameraProvider: ProcessCameraProvider? = null
  private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
  private var camera: Camera? = null
  private var previewUseCase: Preview? = null

  private var onHideAnimCompleteListener: (() -> Unit)? = null

  init {
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      width = LayoutHelper.MATCH_PARENT,
      height = LayoutHelper.MATCH_PARENT,
      gravity = Gravity.TOP or Gravity.START
    )
    translationY = screenHeight.toFloat()
    setBackgroundResource(R.color.appBlack)
    isClickable = true
    isFocusable = true
    setupPreviewView()
    setupInitialFrameImageView()
    setupCloseButton()
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    safeHandler = Handler(Looper.getMainLooper())
    startCameraPreview()
  }

  override fun onDetachedFromWindow() {
    animatorSet = null
    focusAnimatorSet = null
    safeHandler?.removeCallbacksAndMessages(null)
    safeHandler = null
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    releaseCamera()
    super.onDetachedFromWindow()
  }

  override fun animateShow() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(this@PhotoCaptureView, View.TRANSLATION_Y, screenHeight.toFloat(), 0f)
          .setDuration(CAPTURE_APPEARANCE_DURATION),
        ObjectAnimator.ofFloat(requireNotNull(closeImageView), View.ALPHA, 0f, 1f)
          .setDuration(CAPTURE_APPEARANCE_DURATION),
      )
      interpolator = AccelerateDecelerateInterpolator()
      doOnEnd {
        if (it == animatorSet) {
          animatorSet = null
          initialFrameImageView?.let { imageView -> this@PhotoCaptureView.removeView(imageView) }
        }
      }
      start()
    }
  }

  override fun animateHide() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(this@PhotoCaptureView, View.TRANSLATION_Y, 0f, screenHeight.toFloat())
          .setDuration(CAPTURE_DISAPPEARANCE_DURATION),
        ObjectAnimator.ofFloat(requireNotNull(closeImageView), View.ALPHA, 1f, 0f)
          .setDuration(CAPTURE_DISAPPEARANCE_DURATION),
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
        camera?.cameraControl?.startFocusAndMetering(action).also { onCameraFocus(event.x, event.y) }

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
      setOnClickListener { animateHide() }
    }
    closeImageView?.let { addView(it) }
  }

  private fun setupInitialFrameImageView() {
    initialFrameImageView = ImageView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
        gravity = Gravity.START or Gravity.TOP
      )
      scaleType = ImageView.ScaleType.CENTER_CROP
    }
    initialFrameImageView?.let { addView(it) }
  }

  private fun startCameraPreview() {
    cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture?.addListener({
      cameraProvider = cameraProviderFuture?.get()

      // Preview
      previewUseCase = Preview.Builder()
        .build()
        .also {
          it.setSurfaceProvider(previewView?.surfaceProvider)
        }

      // Select back camera as a default
      val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

      try {
        // Unbind use cases before rebinding
        previewUseCase?.let { cameraProvider?.unbind(it) }

        // Bind use cases to camera
        previewUseCase?.let { camera = cameraProvider?.bindToLifecycle(this, cameraSelector, it) }

      } catch (e: Exception) {
        Timber.e("Use case binding failed with exception: $e")
      }

    }, ContextCompat.getMainExecutor(context))
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
            this@PhotoCaptureView.removeView(innerTouchView)
            this@PhotoCaptureView.removeView(outerTouchView)
          }, 150L)
        }
      }
      start()
    }
  }

  private fun releaseCamera() {
    camera?.cameraControl?.cancelFocusAndMetering()
    camera = null
    cameraProviderFuture?.cancel(true)
    cameraProviderFuture = null
    previewUseCase?.let { cameraProvider?.unbind(it) }
    previewUseCase = null
    cameraProvider = null
  }

  companion object {
    private const val CLOSE_IMAGE_DIMEN = 48
    private const val CLOSE_IMAGE_MARGIN = 4
    private const val FOCUS_VIEW_DIMEN = 48

    private const val CAPTURE_APPEARANCE_DURATION = 330L
    private const val CAPTURE_DISAPPEARANCE_DURATION = 220L
    private const val FOCUS_TOUCH_DURATION = 350L

    fun newInstance(parentContext: Context, initialBitmap: Bitmap?) = PhotoCaptureView(parentContext).apply {
      initialFrameImageView?.setImageBitmap(initialBitmap)
    }
  }
}