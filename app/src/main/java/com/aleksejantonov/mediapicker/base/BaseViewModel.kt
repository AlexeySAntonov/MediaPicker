package com.aleksejantonov.mediapicker.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {

  open val exceptionHandler = CoroutineExceptionHandler { _, e -> Timber.e(e) }

}