package com.aleksejantonov.mediapicker

import android.content.Context
import android.util.Size
import com.aleksejantonov.mediapicker.base.getScreenHeight
import com.aleksejantonov.mediapicker.base.getScreenWidth
import com.aleksejantonov.mediapicker.cameraview.business.CameraController
import com.aleksejantonov.mediapicker.cameraview.business.ICameraController
import com.aleksejantonov.mediapicker.picker.data.IMediaProvider
import com.aleksejantonov.mediapicker.picker.data.MediaProvider
import java.lang.ref.WeakReference

object SL {
  private lateinit var weakRefContext: WeakReference<Context>
  val screenResolution: Size
    get() {
      val width = weakRefContext.get()?.getScreenWidth() ?: 0
      val height = weakRefContext.get()?.getScreenHeight() ?: 0
      return if (width > height) Size(width, height)
      else Size(height, width)
    }

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