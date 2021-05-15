package com.aleksejantonov.mediapicker.picker.adapter.delegate

import android.graphics.Bitmap
import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.CameraCaptureItem
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.hideAndShowWithDelay
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.photocapture.PhotoCaptureView.Companion.CAPTURE_APPEARANCE_DURATION
import com.aleksejantonov.mediapicker.photocapture.business.ICameraController
import com.aleksejantonov.mediapicker.picker.MediaPickerView.Companion.GALLERY_APPEARANCE_DURATION
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.item_camera.view.*
import java.lang.ref.WeakReference

class CameraCaptureDelegate(
  private val lifeCycleOwner: WeakReference<LifecycleOwner>,
  private val cameraController: ICameraController,
  private val listener: (Bitmap?) -> Unit
) : AbsListItemAdapterDelegate<CameraCaptureItem, DiffListItem, CameraCaptureDelegate.ViewHolder>() {

  override fun isForViewType(item: DiffListItem, items: MutableList<DiffListItem>, position: Int) = item is CameraCaptureItem

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camera, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(item: CameraCaptureItem, viewHolder: ViewHolder, payloads: MutableList<Any>) {
    viewHolder.bind()
  }

  override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    (holder as ViewHolder).releaseCamera()
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val handler = Handler()
    private var handlerCallback: Runnable? = null

    fun bind() {
      with(itemView) {
        startCameraPreview()
        setOnClickListener {
          viewFinder.hideAndShowWithDelay(CAPTURE_APPEARANCE_DURATION * 2)
          listener.invoke(viewFinder.bitmap)
        }
      }
    }

    private fun startCameraPreview() {
      handlerCallback?.let { handler.removeCallbacks(it) }
      handlerCallback = Runnable { cameraController.initCameraProvider(lifeCycleOwner, itemView.viewFinder.surfaceProvider) }
        .also { handler.postDelayed(it, GALLERY_APPEARANCE_DURATION) }
    }

    fun releaseCamera() {
      handlerCallback?.let { handler.removeCallbacks(it) }
      handlerCallback = null
      cameraController.releaseCameraProvider()
    }
  }
}