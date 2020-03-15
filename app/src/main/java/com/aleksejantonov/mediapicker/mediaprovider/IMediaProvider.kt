package com.aleksejantonov.mediapicker.mediaprovider

import android.net.Uri
import com.aleksejantonov.mediapicker.picker.delegate.items.MediaItem
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

interface IMediaProvider {
    val singleImageRelay: PublishRelay<Pair<String, Uri>>
    val multiItemsRelay: PublishRelay<Pair<String, List<String>>>

    fun observeSystemImages(): Observable<List<MediaItem>>
    fun observeSystemMedia(): Observable<List<MediaItem>>
}