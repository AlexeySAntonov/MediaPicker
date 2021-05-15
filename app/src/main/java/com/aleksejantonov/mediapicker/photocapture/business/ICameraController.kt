package com.aleksejantonov.mediapicker.photocapture.business

import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

interface ICameraController {
  fun initCameraProvider(lifeCycleOwner: WeakReference<LifecycleOwner>, initialSurfaceProvider: Preview.SurfaceProvider)
  fun setSurfaceProvider(surfaceProvider: Preview.SurfaceProvider)
  fun startFocusAndMetering(action: FocusMeteringAction)
  fun cancelFocusAndMetering()
  fun releaseCameraProvider()
}