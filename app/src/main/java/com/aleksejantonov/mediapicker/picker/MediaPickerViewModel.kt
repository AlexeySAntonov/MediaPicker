package com.aleksejantonov.mediapicker.picker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aleksejantonov.mediapicker.base.BaseViewModel
import com.aleksejantonov.mediapicker.base.BottomSheetRouter
import com.aleksejantonov.mediapicker.base.DiffListItem
import com.aleksejantonov.mediapicker.mediaprovider.IMediaProvider
import com.aleksejantonov.mediapicker.picker.delegate.items.CameraCaptureItem
import com.aleksejantonov.mediapicker.picker.delegate.items.GalleryMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MediaPickerViewModel(
    private val mediaProvider: IMediaProvider,
    private val bottomSheetRouter: BottomSheetRouter,
    private val singleImage: Boolean,
) : BaseViewModel() {

    private val _content = MutableLiveData<List<DiffListItem>>()
    val content: LiveData<List<DiffListItem>> get() = _content

    private val selectedItemsChannel = ConflatedBroadcastChannel<List<GalleryMediaItem>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            combine(
                if (singleImage) mediaProvider.observeSystemImages()
                else mediaProvider.observeSystemMedia(),
                selectedItemsChannel.asFlow(),
            ) { systemMedia, selectedIds ->
                systemMedia
                    .map {
                        GalleryMediaItem.from(
                            systemMediaModel = it,
                            selected = selectedIds.contains(it.uniqueId),
                        )
                    }
            }.collect { _content.postValue(listOf(CameraCaptureItem()) + it) }
        }
    }
}