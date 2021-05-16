package com.aleksejantonov.mediapicker

import android.content.Context
import com.aleksejantonov.mediapicker.photocapture.business.CameraController
import com.aleksejantonov.mediapicker.photocapture.business.ICameraController
import com.aleksejantonov.mediapicker.picker.data.IMediaProvider
import com.aleksejantonov.mediapicker.picker.data.MediaProvider
import java.lang.ref.WeakReference

object SL {
  private lateinit var weakRefContext: WeakReference<Context>

  fun init(context: Context) {
    this.weakRefContext = WeakReference(context)
  }

  @Volatile
  private var mediaProvider: IMediaProvider? = null
  @Volatile
  private var cameraController: ICameraController? = null

  fun initAndGetMediaProvider(): IMediaProvider {
    if (mediaProvider == null) {
      synchronized(this) {
        if (mediaProvider == null) {
          mediaProvider = MediaProvider(requireNotNull(weakRefContext.get()))
        }
      }
    }
    return requireNotNull(mediaProvider)
  }

  fun initAndGetCameraController(): ICameraController {
    if (cameraController == null) {
      synchronized(this) {
        if (cameraController == null) {
          cameraController = CameraController(requireNotNull(weakRefContext.get()))
        }
      }
    }
    return requireNotNull(cameraController)
  }

  @Synchronized
  fun releaseMediaProvider() {
    mediaProvider = null
  }

  @Synchronized
  fun releaseCameraController() {
    cameraController = null
  }

}