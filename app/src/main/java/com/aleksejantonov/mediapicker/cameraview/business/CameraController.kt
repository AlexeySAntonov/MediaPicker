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
import java.io.File
import java.lang.ref.WeakReference

class CameraController(
  private val context: Context,
) : ICameraController {

  private val executor = TaskExecutors.MAIN_THREAD

  // Used to bind the lifecycle of cameras to the lifecycle owner
  private var cameraProvider: ProcessCameraProvider? = null
  private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
  private var previewUseCase: Preview? = null
  private var analysisUseCase: ImageAnalysis? = null
  private var captureUseCase: ImageCapture? = null
  private var faceProcessor: IFaceDetectorProcessor? = null
  private var poseProcessor: IPoseDetectorProcessor? = null
  private var camera: Camera? = null

  override fun initCameraProvider(
    lifeCycleOwner: WeakReference<LifecycleOwner>,
    initialSurfaceProvider: Preview.SurfaceProvider,
    onFaceDetection: (List<Face>) -> Unit,
    onPoseDetection: (Pose) -> Unit,
    onSourceInfo: (Pair<Int, Int>) -> Unit,
    rotation: Int,
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

        initPreviewUseCase(initialSurfaceProvider)
//        initImageAnalysisUseCase(onFaceDetection, onPoseDetection, onSourceInfo)
        initImageCaptureUseCase(rotation)

        try {
          // Unbind use cases before rebinding
          cameraProvider?.unbindAll()

          // Select back camera as a default
          val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

          // Bind use cases to camera
          lifeCycleOwner.get()?.let { owner ->
            camera = cameraProvider?.bindToLifecycle(
              owner,
              cameraSelector,
              *listOfNotNull(previewUseCase, analysisUseCase, captureUseCase).toTypedArray()
            )
          }

        } catch (e: Exception) {
          Timber.e("Use case binding failed with exception: $e")
        }
      },
      ContextCompat.getMainExecutor(context)
    )
  }

  override fun onImageCapture(
    onImageSaved: (File) -> Unit,
    onError: () -> Unit,
  ) {
    val file = File(context.filesDir, "${TEMP_CAMERA_IMAGE_FILE_PREFIX}${System.currentTimeMillis()}.jpg")
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    captureUseCase?.takePicture(outputFileOptions, executor,
      object : ImageCapture.OnImageSavedCallback {
        override fun onError(error: ImageCaptureException) {
          onError.invoke()
          Timber.e(error)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
          onImageSaved.invoke(file)
        }
      })
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

  override fun setLinearZoom(linearZoom: Float) {
    camera?.cameraControl?.setLinearZoom(linearZoom)
  }

  override fun releaseCameraProvider() {
    cameraProviderFuture?.cancel(true)
    cameraProviderFuture = null
    camera?.cameraControl?.cancelFocusAndMetering()
    camera = null
    previewUseCase?.setSurfaceProvider(null)
    previewUseCase = null
    analysisUseCase = null
    captureUseCase = null
    faceProcessor?.stop()
    faceProcessor = null
    poseProcessor?.stop()
    poseProcessor = null
    cameraProvider?.unbindAll()
    cameraProvider = null
  }

  private fun initPreviewUseCase(surfaceProvider: Preview.SurfaceProvider) {
    previewUseCase = Preview.Builder()
      .setTargetResolution(SL.screenResolution)
      .build()
      .apply { setSurfaceProvider(surfaceProvider) }
  }

  private fun initImageAnalysisUseCase(
    onFaceDetection: (List<Face>) -> Unit,
    onPoseDetection: (Pose) -> Unit,
    onSourceInfo: (Pair<Int, Int>) -> Unit,
  ) {
    faceProcessor = FaceDetectorProcessor()
    poseProcessor = PoseDetectorProcessor()
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
  }

  private fun initImageCaptureUseCase(rotation: Int) {
    captureUseCase = ImageCapture.Builder()
      .setTargetRotation(rotation)
      .build()
  }

  companion object {
    const val TEMP_CAMERA_IMAGE_FILE_PREFIX = "camera_tmp_image"
  }

}