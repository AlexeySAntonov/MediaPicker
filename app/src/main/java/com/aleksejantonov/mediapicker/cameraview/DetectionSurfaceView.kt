package com.aleksejantonov.mediapicker.cameraview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import com.aleksejantonov.mediapicker.base.*
import com.aleksejantonov.mediapicker.base.ui.LayoutHelper
import com.google.mlkit.vision.face.Face
import kotlin.math.max

class DetectionSurfaceView(context: Context) : View(context) {

  private val screenWidth by lazy { context.getScreenWidth() }
  private val screenHeight by lazy { context.getScreenHeight() }

  private val defaultStrokeWidth = dpToPx(2f).toFloat()
  private val faceRectPaint: Paint by lazy {
    Paint().apply {
      style = Paint.Style.STROKE
      color = Color.RED
    }
  }
  private val faceRectList = mutableListOf<Rect>()
  private var needToDrawFaceRect: Boolean = false
  private var rectScaleFactor: Float = 0f

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
    if (needToDrawFaceRect && faceRectList.isNotEmpty()) {
      for (faceRect in faceRectList) {
        canvas.drawRect(faceRect, faceRectPaint.apply { strokeWidth = defaultStrokeWidth / rectScaleFactor })
      }
      needToDrawFaceRect = false
      faceRectList.clear()
    }
  }

  fun onFaceDetection(faces: List<Face>) {
    if (faces.isNotEmpty()) {
      for (face in faces) faceRectList.add(face.boundingBox)
      needToDrawFaceRect = true
    }
    invalidate()
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