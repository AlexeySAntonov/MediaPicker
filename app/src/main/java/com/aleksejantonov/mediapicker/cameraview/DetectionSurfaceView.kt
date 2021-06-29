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
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.max

class DetectionSurfaceView(context: Context) : View(context) {

  private val screenWidth by lazy { context.getScreenWidth() }
  private val screenHeight by lazy { context.getScreenHeight() }

  private val defaultStrokeWidth = dpToPx(2f).toFloat()
  private val redPaint: Paint by lazy {
    Paint().apply {
      style = Paint.Style.STROKE
      color = Color.RED
    }
  }
  private val whitePaint: Paint by lazy {
    Paint().apply {
      style = Paint.Style.STROKE
      color = Color.WHITE
    }
  }
  private val leftPaint: Paint by lazy {
    Paint().apply {
      style = Paint.Style.STROKE
      color = Color.GREEN
    }
  }
  private val rightPaint: Paint by lazy {
    Paint().apply {
      style = Paint.Style.STROKE
      color = Color.YELLOW
    }
  }
  private val faceRectList = mutableListOf<Rect>()
  private var pose: Pose? = null
  private var surfaceScaleFactor: Float = 0f

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
    if (faceRectList.isNotEmpty()) {
      for (faceRect in faceRectList) {
        canvas.drawRect(faceRect, redPaint.apply { strokeWidth = defaultStrokeWidth / surfaceScaleFactor })
      }
      faceRectList.clear()
    }
    pose?.let {
      val leftShoulder = it.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
      val rightShoulder = it.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
      val leftElbow = it.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
      val rightElbow = it.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
      val leftWrist = it.getPoseLandmark(PoseLandmark.LEFT_WRIST)
      val rightWrist = it.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
      val leftHip = it.getPoseLandmark(PoseLandmark.LEFT_HIP)
      val rightHip = it.getPoseLandmark(PoseLandmark.RIGHT_HIP)
      val leftKnee = it.getPoseLandmark(PoseLandmark.LEFT_KNEE)
      val rightKnee = it.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
      val leftAnkle = it.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
      val rightAnkle = it.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

      val leftPinky = it.getPoseLandmark(PoseLandmark.LEFT_PINKY)
      val rightPinky = it.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
      val leftIndex = it.getPoseLandmark(PoseLandmark.LEFT_INDEX)
      val rightIndex = it.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
      val leftThumb = it.getPoseLandmark(PoseLandmark.LEFT_THUMB)
      val rightThumb = it.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
      val leftHeel = it.getPoseLandmark(PoseLandmark.LEFT_HEEL)
      val rightHeel = it.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
      val leftFootIndex = it.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
      val rightFootIndex = it.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

      drawLine(leftShoulder, rightShoulder, whitePaint, canvas)
      drawLine(leftHip, rightHip, whitePaint, canvas)
      // Left body
      drawLine(leftShoulder, leftElbow, leftPaint, canvas)
      drawLine(leftElbow, leftWrist, leftPaint, canvas)
      drawLine(leftShoulder, leftHip, leftPaint, canvas)
      drawLine(leftHip, leftKnee, leftPaint, canvas)
      drawLine(leftKnee, leftAnkle, leftPaint, canvas)
      drawLine(leftWrist, leftThumb, leftPaint, canvas)
      drawLine(leftWrist, leftPinky, leftPaint, canvas)
      drawLine(leftWrist, leftIndex, leftPaint, canvas)
      drawLine(leftIndex, leftPinky, leftPaint, canvas)
      drawLine(leftAnkle, leftHeel, leftPaint, canvas)
      drawLine(leftHeel, leftFootIndex, leftPaint, canvas)
      // Right body
      drawLine(rightShoulder, rightElbow, rightPaint, canvas)
      drawLine(rightElbow, rightWrist, rightPaint, canvas)
      drawLine(rightShoulder, rightHip, rightPaint, canvas)
      drawLine(rightHip, rightKnee, rightPaint, canvas)
      drawLine(rightKnee, rightAnkle, rightPaint, canvas)
      drawLine(rightWrist, rightThumb, rightPaint, canvas)
      drawLine(rightWrist, rightPinky, rightPaint, canvas)
      drawLine(rightWrist, rightIndex, rightPaint, canvas)
      drawLine(rightIndex, rightPinky, rightPaint, canvas)
      drawLine(rightAnkle, rightHeel, rightPaint, canvas)
      drawLine(rightHeel, rightFootIndex, rightPaint, canvas)

      pose = null
    }
  }

  fun onFaceDetection(faces: List<Face>) {
    if (faces.isNotEmpty()) {
      for (face in faces) faceRectList.add(face.boundingBox)
    }
    invalidate()
  }

  fun onPoseDetection(pose: Pose) {
    this.pose = pose
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
    surfaceScaleFactor = max(heightRatio, widthRatio)
    scaleX = surfaceScaleFactor
    scaleY = surfaceScaleFactor
  }

  private fun drawLine(
    startLandmark: PoseLandmark?,
    endLandmark: PoseLandmark?,
    paint: Paint,
    canvas: Canvas,
  ) {
    if (startLandmark != null && endLandmark != null) {
      val startX = startLandmark.position.x
      val startY = startLandmark.position.y
      val endX = endLandmark.position.x
      val endY = endLandmark.position.y
      canvas.drawLine(startX, startY, endX, endY, paint.apply { strokeWidth = defaultStrokeWidth / surfaceScaleFactor })
    }
  }
}