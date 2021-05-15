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

    val mediaProvider: IMediaProvider by lazy { MediaProvider(requireNotNull(weakRefContext.get())) }
    val cameraController: ICameraController by lazy { CameraController(requireNotNull(weakRefContext.get())) }
}