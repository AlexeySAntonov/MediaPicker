package com.aleksejantonov.mediapicker.picker

import android.net.Uri
import com.aleksejantonov.mediapicker.base.BottomSheetRouter
import com.aleksejantonov.mediapicker.base.RxPresenter
import com.aleksejantonov.mediapicker.base.replaceAll
import com.aleksejantonov.mediapicker.mediaprovider.IMediaProvider
import com.aleksejantonov.mediapicker.picker.delegate.items.CameraCaptureItem
import com.aleksejantonov.mediapicker.picker.delegate.items.MediaItem
import com.arellomobile.mvp.InjectViewState
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@InjectViewState
class MediaPickerPresenter(
    private val mediaProvider: IMediaProvider,
    private val bottomSheetRouter: BottomSheetRouter
) : RxPresenter<MediaPickerView>() {

    private var singleImage: Boolean = true
    private var observerId: String = ""

    fun putInfo(singleImage: Boolean, observerId: String) {
        this.singleImage = singleImage
        this.observerId = observerId
    }

    private val allItems = hashSetOf<MediaItem>()
    private val selectedItems = hashSetOf<Int>()
    private val selectedItemsRelay = PublishRelay.create<Set<Int>>()

    override fun onFirstViewAttach() {
        Observables
            .combineLatest(
                selectedItemsRelay.startWith(emptySet<Int>()),
                getMediaObserver().doOnNext { allItems.replaceAll(it) }
            ) { selectedIds, images ->
                images.map { it.copy(selected = selectedIds.contains(it.uniqueId)) }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    viewState.showItems(listOf(CameraCaptureItem()) + it, selectedItems.size)
                },
                onError = Timber::e
            )
            .keepUntilPresenterDestroyed()
    }

    fun onMediaClick(image: MediaItem) {
        if (singleImage) {
            selectedItems.clear()
            if (!image.selected) selectedItems.add(image.uniqueId)
            selectedItemsRelay.accept(selectedItems)
        } else {
            if (image.selected) {
                selectedItems.remove(image.uniqueId)
            } else {
                selectedItems.add(image.uniqueId)
            }
            selectedItemsRelay.accept(selectedItems)
        }
    }

    fun onCameraClick() = viewState.dispatchTakePictureIntent()

    fun performDoneAction() {
        if (singleImage) {
            val resultImage = allItems.find { it.uniqueId == selectedItems.firstOrNull() }
            resultImage?.let { mediaProvider.singleImageRelay.accept(observerId to it.uri) }
        } else {
            val resultImages = allItems.filter { selectedItems.contains(it.uniqueId) }
            if (resultImages.isNotEmpty()) mediaProvider.multiItemsRelay.accept(observerId to resultImages.map { it.path })
        }
        onCloseClick()
    }

    fun handlePhotoCapture(uri: Uri, path: String) {
        if (singleImage) mediaProvider.singleImageRelay.accept(observerId to uri)
        else mediaProvider.multiItemsRelay.accept(observerId to listOf(path))
        onCloseClick()
    }

    fun onCloseClick() {
        bottomSheetRouter.close()
    }

    private fun getMediaObserver(): Observable<List<MediaItem>> {
        return if (singleImage) mediaProvider.observeSystemImages()
        else mediaProvider.observeSystemMedia()
    }

}