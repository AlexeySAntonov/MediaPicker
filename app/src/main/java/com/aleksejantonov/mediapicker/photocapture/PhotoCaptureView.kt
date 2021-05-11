package com.aleksejantonov.mediapicker.photocapture

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.dpToPx
import com.aleksejantonov.mediapicker.base.setMargins
import com.aleksejantonov.mediapicker.base.statusBarHeight
import com.aleksejantonov.mediapicker.base.ui.BottomSheetable
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.google.common.util.concurrent.ListenableFuture
import timber.log.Timber

class PhotoCaptureView(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet), BottomSheetable, LifecycleOwner {

  private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

  private var previewView: PreviewView? = null
  private var closeImageView: ImageView? = null
  private var initialFrameImageView: ImageView? = null
  private var animatorSet: AnimatorSet? = null

  // Used to bind the lifecycle of cameras to the lifecycle owner
  private var cameraProvider: ProcessCameraProvider? = null
  private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
  private var previewUseCase: Preview? = null

  private var onHideAnimCompleteListener: (() -> Unit)? = null

  init {
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      width = LayoutHelper.MATCH_PARENT,
      rawHeightPx = LayoutHelper.MATCH_PARENT,
    )
    setBackgroundResource(R.color.appBlack)
    isClickable = true
    isFocusable = true
    setupPreviewView()
    setupCloseButton()
    setupInitialFrameImageView()
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    startCameraPreview()
  }

  override fun onDetachedFromWindow() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    releaseCamera()
    animatorSet = null
    super.onDetachedFromWindow()
  }

  override fun animateShow() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(requireNotNull(closeImageView), View.ALPHA, 0f, 1f)
          .setDuration(CAPTURE_APPEARANCE_DURATION),
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

  private fun setupPreviewView() {
    previewView = PreviewView(context).apply {
      layoutParams = LayoutHelper.getFrameParams(
        context = context,
        width = LayoutHelper.MATCH_PARENT,
        height = LayoutHelper.MATCH_PARENT,
        gravity = Gravity.START or Gravity.TOP
      )
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
        previewUseCase?.let { cameraProvider?.bindToLifecycle(this, cameraSelector, it) }

      } catch (e: Exception) {
        Timber.e("Use case binding failed with exception: $e")
      }

    }, ContextCompat.getMainExecutor(context))
  }

  private fun releaseCamera() {
    cameraProviderFuture?.cancel(true)
    cameraProviderFuture = null
    previewUseCase?.let { cameraProvider?.unbind(it) }
    previewUseCase = null
    cameraProvider = null
  }

  companion object {
    private const val CLOSE_IMAGE_DIMEN = 48
    private const val CLOSE_IMAGE_MARGIN = 4

    private const val CAPTURE_APPEARANCE_DURATION = 330L
    private const val CAPTURE_DISAPPEARANCE_DURATION = 220L

    fun newInstance(parentContext: Context, initialBitmap: Bitmap?) = PhotoCaptureView(parentContext).apply {
      initialFrameImageView?.setImageBitmap(initialBitmap)
    }
  }
}