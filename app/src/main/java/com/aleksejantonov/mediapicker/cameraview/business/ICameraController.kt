package com.aleksejantonov.mediapicker.cameraview.business

import androidx.annotation.FloatRange
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.pose.Pose
import java.io.File
import java.lang.ref.WeakReference

interface ICameraController {
  fun initCameraProvider(
    lifeCycleOwner: WeakReference<LifecycleOwner>,
    initialSurfaceProvider: Preview.SurfaceProvider,
    onFaceDetection: (List<Face>) -> Unit,
    onPoseDetection: (Pose) -> Unit,
    onSourceInfo: (Pair<Int, Int>) -> Unit,
    rotation: Int,
  )

  fun onImageCapture(
    onImageSaved: (File) -> Unit,
    onError: () -> Unit = {},
  )

  fun setSurfaceProvider(surfaceProvider: Preview.SurfaceProvider)
  fun clearSurfaceProvider()
  fun startFocusAndMetering(action: FocusMeteringAction)
  fun cancelFocusAndMetering()
  fun setLinearZoom(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float)
  fun releaseCameraProvider()
}