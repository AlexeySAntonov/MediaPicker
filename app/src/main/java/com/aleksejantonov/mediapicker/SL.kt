package com.aleksejantonov.mediapicker

import android.content.Context
import com.aleksejantonov.mediapicker.navigation.BottomSheetRouter
import com.aleksejantonov.mediapicker.picker.data.IMediaProvider
import com.aleksejantonov.mediapicker.picker.data.MediaProvider
import java.lang.ref.WeakReference

object SL {
    private lateinit var weakRefContext: WeakReference<Context>

    fun init(context: Context) {
        this.weakRefContext = WeakReference(context)
    }

    val mediaProvider: IMediaProvider by lazy { MediaProvider(requireNotNull(weakRefContext.get())) }
    val bottomSheetRouter: BottomSheetRouter by lazy { BottomSheetRouter() }
}