package com.aleksejantonov.mediapicker.picker.adapter.delegate

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

class CameraCaptureDelegate(
  private val listener: () -> Unit
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

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    // Used to bind the lifecycle of cameras to the lifecycle owner
    private var cameraProvider: ProcessCameraProvider? = null

    fun bind(item: CameraCaptureItem) {
      with(itemView) {
        startCameraPreview()
        setOnClickListener { listener.invoke() }
      }
    }

    private fun startCameraPreview() {
      cameraProviderFuture = ProcessCameraProvider.getInstance(itemView.context)

      cameraProviderFuture?.addListener({
        cameraProvider = cameraProviderFuture?.get()

        // Preview
        val preview = Preview.Builder()
          .build()
          .also {
            it.setSurfaceProvider(itemView.viewFinder.surfaceProvider)
          }

        // Select back camera as a default
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
          // Unbind use cases before rebinding
          cameraProvider?.unbindAll()

          // Bind use cases to camera
          (itemView.context as? LifecycleOwner)?.let { cameraProvider?.bindToLifecycle(it, cameraSelector, preview) }

        } catch (e: Exception) {
          Timber.e("Use case binding failed with exception: $e")
        }

      }, ContextCompat.getMainExecutor(itemView.context))
    }

    fun releaseCamera() {
      cameraProviderFuture?.cancel(true)
      cameraProviderFuture = null
      cameraProvider?.unbindAll()
      cameraProvider = null
    }
  }
}