package com.aleksejantonov.mediapicker.cameraview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.base.dpToPx
import com.aleksejantonov.mediapicker.base.getScreenHeight
import com.aleksejantonov.mediapicker.base.getScreenWidth
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.google.mlkit.vision.face.Face
import timber.log.Timber
import kotlin.math.max

class DetectionSurfaceView(context: Context) : View(context) {

  private val screenWidth by lazy { context.getScreenWidth() }
  private val screenHeight by lazy { context.getScreenHeight() }

  private val faceRectPaint: Paint by lazy {
    Paint().apply {
      style = Paint.Style.STROKE
      color = Color.RED
      strokeWidth = dpToPx(2f).toFloat()
    }
  }
  private val faceRectList = mutableListOf<Rect>()
  private var needToDrawFaceRect: Boolean = false
  private var rectScaleFactor: Float = 0f

  init {
    setBackgroundResource(R.drawable.background_rect_border)
  }

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
    if (needToDrawFaceRect && faceRectList.isNotEmpty()) {
      for (faceRect in faceRectList) {
        Timber.e("On draw rect: ${faceRect.left}, ${faceRect.top}, ${faceRect.right}, ${faceRect.bottom}")
        canvas.drawRect(faceRect, faceRectPaint)
      }
      needToDrawFaceRect = false
      faceRectList.clear()
    }
  }

  fun onFaceDetection(faces: List<Face>) {
    if (faces.isNotEmpty()) {
      for (face in faces) faceRectList.add(face.boundingBox)
      needToDrawFaceRect = true
      invalidate()
    }
  }

  fun onSourceInfo(width: Int, height: Int) {
    layoutParams = LayoutHelper.getFrameParams(
      context = context,
      rawWidthPx = width,
      rawHeightPx = height,
      gravity = Gravity.CENTER
    )

    val heightRatio: Float = screenHeight.toFloat() / height
    val widthRatio: Float = screenWidth.toFloat() / width
    rectScaleFactor = max(heightRatio, widthRatio)
    scaleX = rectScaleFactor
    scaleY = rectScaleFactor
  }
}