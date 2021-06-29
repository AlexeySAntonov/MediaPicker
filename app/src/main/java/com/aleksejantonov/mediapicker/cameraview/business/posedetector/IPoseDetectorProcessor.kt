package com.aleksejantonov.mediapicker.cameraview.business.posedetector

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.pose.Pose

interface IPoseDetectorProcessor {

  fun processImageProxy(image: ImageProxy, onDetectionFinished: (Pose) -> Unit)
  fun stop()
}