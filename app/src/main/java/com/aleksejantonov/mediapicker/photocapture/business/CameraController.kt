package com.aleksejantonov.mediapicker.photocapture.business

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import timber.log.Timber
import java.lang.ref.WeakReference

class CameraController(
  private val context: Context,
) : ICameraController {

  // Used to bind the lifecycle of cameras to the lifecycle owner
  private var cameraProvider: ProcessCameraProvider? = null
  private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
  private var previewUseCase: Preview? = null
  private var camera: Camera? = null

  override fun initCameraProvider(lifeCycleOwner: WeakReference<LifecycleOwner>, initialSurfaceProvider: Preview.SurfaceProvider) {
    releaseCameraProvider()
    cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture?.addListener(
      {
        cameraProvider = cameraProviderFuture?.get()
        previewUseCase = Preview.Builder()
          .build()
          .also { it.setSurfaceProvider(initialSurfaceProvider) }

        try {
          // Unbind use cases before rebinding
          previewUseCase?.let { cameraProvider?.unbind(it) }

          // Select back camera as a default
          val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

          // Bind use cases to camera
          lifeCycleOwner.get()?.let { owner ->
            previewUseCase?.let { camera = cameraProvider?.bindToLifecycle(owner, cameraSelector, it) }
          }

        } catch (e: Exception) {
          Timber.e("Use case binding failed with exception: $e")
        }
      },
      ContextCompat.getMainExecutor(context)
    )
  }

  override fun setSurfaceProvider(surfaceProvider: Preview.SurfaceProvider) {
    previewUseCase?.setSurfaceProvider(surfaceProvider)
  }

  override fun clearSurfaceProvider() {
    previewUseCase?.setSurfaceProvider(null)
  }

  override fun startFocusAndMetering(action: FocusMeteringAction) {
    camera?.cameraControl?.startFocusAndMetering(action)
  }

  override fun cancelFocusAndMetering() {
    camera?.cameraControl?.cancelFocusAndMetering()
  }

  override fun releaseCameraProvider() {
    cameraProviderFuture?.cancel(true)
    cameraProviderFuture = null
    previewUseCase?.setSurfaceProvider(null)
    previewUseCase = null
    cameraProvider?.unbindAll()
    cameraProvider = null
    camera?.cameraControl?.cancelFocusAndMetering()
    camera = null
  }

}