package com.aleksejantonov.mediapicker

import android.content.Context
import com.aleksejantonov.mediapicker.base.BottomSheetRouter
import com.aleksejantonov.mediapicker.mediaprovider.IMediaProvider
import com.aleksejantonov.mediapicker.mediaprovider.MediaProvider
import java.lang.ref.WeakReference

object SL {
    private lateinit var weakRefContext: WeakReference<Context>

    fun init(context: Context) {
        this.weakRefContext = WeakReference(context)
    }

    val mediaProvider: IMediaProvider by lazy { MediaProvider(requireNotNull(weakRefContext.get())) }
    val bottomSheetRouter: BottomSheetRouter by lazy { BottomSheetRouter() }
}