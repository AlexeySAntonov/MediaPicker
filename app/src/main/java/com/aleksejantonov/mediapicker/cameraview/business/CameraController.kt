package com.aleksejantonov.mediapicker.cameraview.business

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.aleksejantonov.mediapicker.SL
import com.aleksejantonov.mediapicker.cameraview.business.facedetector.FaceDetectorProcessor
import com.aleksejantonov.mediapicker.cameraview.business.facedetector.IFaceDetectorProcessor
import com.aleksejantonov.mediapicker.cameraview.business.posedetector.IPoseDetectorProcessor
import com.aleksejantonov.mediapicker.cameraview.business.posedetector.PoseDetectorProcessor
import com.google.android.gms.tasks.TaskExecutors
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.pose.Pose
import timber.log.Timber
import java.lang.ref.WeakReference

class CameraController(
  private val context: Context,
) : ICameraController {

  // Used to bind the lifecycle of cameras to the lifecycle owner
  private var cameraProvider: ProcessCameraProvider? = null
  private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
  private var previewUseCase: Preview? = null
  private var analysisUseCase: ImageAnalysis? = null
  private var faceProcessor: IFaceDetectorProcessor? = null
  private var poseProcessor: IPoseDetectorProcessor? = null
  private var camera: Camera? = null

  override fun initCameraProvider(
    lifeCycleOwner: WeakReference<LifecycleOwner>,
    initialSurfaceProvider: Preview.SurfaceProvider,
    onFaceDetection: (List<Face>) -> Unit,
    onPoseDetection: (Pose) -> Unit,
    onSourceInfo: (Pair<Int, Int>) -> Unit,
  ) {
    if (cameraProvider != null && previewUseCase != null) {
      setSurfaceProvider(initialSurfaceProvider)
      return
    }
    releaseCameraProvider()
    cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture?.addListener(
      {
        cameraProvider = cameraProviderFuture?.get()
        faceProcessor = FaceDetectorProcessor()
        poseProcessor = PoseDetectorProcessor()

        previewUseCase = Preview.Builder()
          .setTargetResolution(SL.screenResolution)
          .build()
          .apply { setSurfaceProvider(initialSurfaceProvider) }

        var sourceInfoObtained = false
        analysisUseCase = ImageAnalysis.Builder()
          .setTargetResolution(SL.screenResolution)
          .build()
          .apply {
            setAnalyzer(
              TaskExecutors.MAIN_THREAD,
              { imageProxy: ImageProxy ->
                if (!sourceInfoObtained) {
                  onSourceInfo.invoke(imageProxy.width to imageProxy.height)
                  sourceInfoObtained = true
                }
                try {
                  faceProcessor?.processImageProxy(imageProxy, onFaceDetection)
                  poseProcessor?.processImageProxy(imageProxy, onPoseDetection)
                } catch (e: MlKitException) {
                  Timber.e("Failed to process image. Error: ${e.localizedMessage}")
                }
              }
            )
          }

        try {
          // Unbind use cases before rebinding
          previewUseCase?.let { cameraProvider?.unbind(it) }
          analysisUseCase?.let { cameraProvider?.unbind(it) }

          // Select back camera as a default
          val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

          // Bind use cases to camera
          lifeCycleOwner.get()?.let { owner ->
            camera = cameraProvider?.bindToLifecycle(owner, cameraSelector, previewUseCase, analysisUseCase)
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
    camera?.cameraControl?.cancelFocusAndMetering()
    camera = null
    previewUseCase?.setSurfaceProvider(null)
    previewUseCase = null
    analysisUseCase = null
    faceProcessor?.stop()
    faceProcessor = null
    poseProcessor?.stop()
    poseProcessor = null
    cameraProvider?.unbindAll()
    cameraProvider = null
  }

}