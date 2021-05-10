package com.aleksejantonov.mediapicker.picker

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aleksejantonov.mediapicker.base.mvvm.BaseViewModel
import com.aleksejantonov.mediapicker.base.mvvm.SingleLiveEvent
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.picker.business.SelectionInteractor
import com.aleksejantonov.mediapicker.picker.data.IMediaProvider
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.CameraCaptureItem
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.GalleryMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MediaPickerViewModel(
  private val mediaProvider: IMediaProvider,
  private val singleImage: Boolean,
  private val limit: Int,
) : BaseViewModel() {

  private val _content = MutableLiveData<List<DiffListItem>>()
  val content: LiveData<List<DiffListItem>> get() = _content

  private val _limitEvent = SingleLiveEvent<Unit>()
  val limitEvent: LiveData<Unit> get() = _limitEvent

  private val selectionInteractor = SelectionInteractor<GalleryMediaItem>(emptyList())

  init {
    viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
      combine(
        if (singleImage) mediaProvider.observeSystemImages()
        else mediaProvider.observeSystemMedia(),
        selectionInteractor.observeSelection(),
      ) { systemMedia, selected ->
        val selectedIds = selected.map { it.id }.toSet()
        systemMedia
          .map {
            GalleryMediaItem.from(
              systemMediaModel = it,
              selected = selectedIds.contains(it.uniqueId),
              orderNumber = selected.indexOfFirst { item -> item.id == it.uniqueId } + 1,
            )
          }
      }.collect { _content.postValue(listOf(CameraCaptureItem()) + it) }
    }
  }

  fun onMediaClick(item: GalleryMediaItem) {
    if (singleImage) {
      processSingleImage(item)
    } else {
      val selected = selectionInteractor.selected().firstOrNull { it.id == item.id }
      if (selectionInteractor.selectedCount() >= limit && selected == null) {
        _limitEvent.postValue(Unit)
      } else {
        selected?.let { selectionInteractor.deselect(it) } ?: selectionInteractor.select(item)
      }
    }
  }

  fun performDoneAction() {
    val selectedMedia = selectionInteractor.selected()
    /**
     * Do anything with the selected [GalleryMediaItem]s.
     */
    onCloseClick()
  }

  fun handlePhotoCapture(uri: Uri, path: String) {
    // Do anything with the obtained photo
  }

  fun onCloseClick() {

  }

  private fun processSingleImage(item: GalleryMediaItem) {
    /**
     * Do anything with the selected [GalleryMediaItem].
     */
    onCloseClick()
  }

  override fun onCleared() {
    selectionInteractor.reset()
  }
}