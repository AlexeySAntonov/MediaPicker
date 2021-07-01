package com.aleksejantonov.mediapicker.cameraview.business.facedetector

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import timber.log.Timber

class FaceDetectorProcessor : IFaceDetectorProcessor {

  private val detector: FaceDetector

  private val executor = TaskExecutors.MAIN_THREAD

  init {
    val faceDetectorOptions = FaceDetectorOptions.Builder()
      .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
      .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
      .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
      .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
      .setMinFaceSize(0.2f)
      .build()

    detector = FaceDetection.getClient(faceDetectorOptions)
  }

  @SuppressLint("UnsafeOptInUsageError")
  override fun processImageProxy(image: ImageProxy, onFaceDetection: (List<Face>) -> Unit) {
    detector.process(InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees))
      .addOnSuccessListener(executor) { results: List<Face> -> onFaceDetection(results) }
      .addOnFailureListener(executor) { e: Exception ->
        Timber.e(e, "Error detecting face")
      }
      .addOnCompleteListener {
        try {
          image.close()
        } catch (e: Exception) {
          Timber.e(e, "Image resource cannot be closed")
        }
      }
  }

  override fun stop() {
    detector.close()
  }
}