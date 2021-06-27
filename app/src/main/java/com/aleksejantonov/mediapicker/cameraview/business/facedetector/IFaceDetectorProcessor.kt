package com.aleksejantonov.mediapicker.cameraview.business.facedetector

import com.google.mlkit.vision.face.Face
import androidx.camera.core.ImageProxy

interface IFaceDetectorProcessor {
  fun processImageProxy(image: ImageProxy, onFaceDetection: (List<Face>) -> Unit)
  fun stop()
}