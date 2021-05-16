package com.aleksejantonov.mediapicker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.aleksejantonov.mediapicker.photocapture.PhotoCaptureView
import com.aleksejantonov.mediapicker.picker.MediaPickerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

  private var mediaPickerView: MediaPickerView? = null
  private var photoCaptureView: PhotoCaptureView? = null

  private val permissions: Array<String> = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    pick.setOnClickListener {
      if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && permissionsGranted()) {
        mediaPickerView ?: showMediaPickerView()
      } else {
        requestPermissions(permissions, 1212)
      }
    }
  }

  override fun onPause() {
    super.onPause()
    modalHost.removeAllViews()
    mediaPickerView = null
    photoCaptureView = null
  }

  override fun onBackPressed() {
    photoCaptureView?.animateHide()
      ?: mediaPickerView?.animateHide()
      ?: super.onBackPressed()
  }

  private fun showMediaPickerView() {
    mediaPickerView = MediaPickerView.newInstance(modalHost.context).apply {
      modalHost.addView(this)
      onCameraClick { bitmap, x, y, width ->  photoCaptureView ?: showPhotoCaptureView(bitmap, x, y, width) }
      onHideAnimationComplete {
        mediaPickerView?.let {
          modalHost.removeView(it)
          mediaPickerView = null
        }
      }
      animateShow()
    }
  }

  private fun showPhotoCaptureView(initialBitmap: Bitmap?, initialX: Float, initialY: Float, initialWidth: Int) {
    photoCaptureView = PhotoCaptureView.newInstance(modalHost.context, initialBitmap, initialX, initialY, initialWidth).apply {
      modalHost.addView(this)
      onHideAnimationStarted { mediaPickerView?.onFocus() }
      onHideAnimationComplete {
        photoCaptureView?.let {
          modalHost.removeView(it)
          photoCaptureView = null
        }
      }
      animateShow()
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun permissionsGranted(): Boolean {
    for (permission in permissions) {
      if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return false
    }
    return true
  }
}
