package com.aleksejantonov.mediapicker.picker.adapter.delegate

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.CameraCaptureItem
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.google.common.util.concurrent.ListenableFuture
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.item_camera.view.*
import timber.log.Timber
import java.lang.ref.WeakReference

class CameraCaptureDelegate(
  private val lifeCycleOwner: WeakReference<LifecycleOwner>,
  private val listener: (Bitmap?) -> Unit
) : AbsListItemAdapterDelegate<CameraCaptureItem, DiffListItem, CameraCaptureDelegate.ViewHolder>() {

  override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is CameraCaptureItem

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camera, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(item: CameraCaptureItem, viewHolder: ViewHolder, payloads: MutableList<Any>) {
    viewHolder.bind(item)
  }

  override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    (holder as ViewHolder).releaseCamera()
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    // Used to bind the lifecycle of cameras to the lifecycle owner
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var previewUseCase: Preview? = null

    fun bind(item: CameraCaptureItem) {
      with(itemView) {
        startCameraPreview()
        setOnClickListener { listener.invoke(itemView.viewFinder.bitmap) }
      }
    }

    private fun startCameraPreview() {
      cameraProviderFuture = ProcessCameraProvider.getInstance(itemView.context)

      cameraProviderFuture?.addListener({
        cameraProvider = cameraProviderFuture?.get()

        // Preview
        previewUseCase = Preview.Builder()
          .build()
          .also {
            it.setSurfaceProvider(itemView.viewFinder.surfaceProvider)
          }

        // Select back camera as a default
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
          // Unbind use cases before rebinding
          previewUseCase?.let { cameraProvider?.unbind(it) }

          // Bind use cases to camera
          lifeCycleOwner.get()?.let { owner ->
            previewUseCase?.let { cameraProvider?.bindToLifecycle(owner, cameraSelector, it) }
          }

        } catch (e: Exception) {
          Timber.e("Use case binding failed with exception: $e")
        }

      }, ContextCompat.getMainExecutor(itemView.context))
    }

    fun releaseCamera() {
      cameraProviderFuture?.cancel(true)
      cameraProviderFuture = null
      previewUseCase?.let { cameraProvider?.unbind(it) }
      previewUseCase = null
      cameraProvider = null
    }
  }
}