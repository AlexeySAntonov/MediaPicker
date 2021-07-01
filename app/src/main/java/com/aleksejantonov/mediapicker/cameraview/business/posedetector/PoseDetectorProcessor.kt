package com.aleksejantonov.mediapicker.cameraview.business.posedetector

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import timber.log.Timber

class PoseDetectorProcessor : IPoseDetectorProcessor {

  private val detector: PoseDetector
  private val executor = TaskExecutors.MAIN_THREAD

  init {
    val options = PoseDetectorOptions.Builder()
      .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
      .build()
    detector = PoseDetection.getClient(options)
  }

  @SuppressLint("UnsafeOptInUsageError")
  override fun processImageProxy(image: ImageProxy, onDetectionFinished: (Pose) -> Unit) {
    detector.process(InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees))
      .addOnSuccessListener(executor) { results: Pose -> onDetectionFinished(results) }
      .addOnFailureListener(executor) { e: Exception -> Timber.e(e, "Error detecting pose") }
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