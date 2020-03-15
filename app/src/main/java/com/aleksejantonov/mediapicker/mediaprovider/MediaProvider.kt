package com.aleksejantonov.mediapicker.mediaprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import com.aleksejantonov.mediapicker.picker.delegate.items.MediaItem
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import timber.log.Timber

class MediaProvider(private val context: Context) : IMediaProvider {

    override val singleImageRelay: PublishRelay<Pair<String, Uri>> = PublishRelay.create()
    override val multiItemsRelay: PublishRelay<Pair<String, List<String>>> = PublishRelay.create()

    private val projectionPhotos = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )

    private val projectionVideos = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.BUCKET_ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME
    )

    override fun observeSystemImages(): Observable<List<MediaItem>> =
        Observable
            .create { emitter ->
                val contentObserver = object : ContentObserver(Handler(context.mainLooper)) {
                    override fun onChange(selfChange: Boolean) {
                        super.onChange(selfChange)
                        emitter.onNext(getSystemImages())
                    }
                }

                val contentResolver = context.contentResolver
                contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
                emitter.setCancellable { contentResolver.unregisterContentObserver(contentObserver) }

                // Initial data
                if (!emitter.isDisposed) emitter.onNext(getSystemImages())
            }

    override fun observeSystemMedia(): Observable<List<MediaItem>> =
        Observable
            .create { emitter ->
                val contentObserver = object : ContentObserver(Handler(context.mainLooper)) {
                    override fun onChange(selfChange: Boolean) {
                        super.onChange(selfChange)
                        emitter.onNext(getSystemMedia())
                    }
                }

                val contentResolver = context.contentResolver
                contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
                emitter.setCancellable { contentResolver.unregisterContentObserver(contentObserver) }

                // Initial data
                if (!emitter.isDisposed) emitter.onNext(getSystemMedia())
            }

    @SuppressLint("Recycle")
    private fun getSystemImages(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            var cursor: Cursor? = null
            try {
                cursor = if (Build.VERSION.SDK_INT >= 26) {
                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        null,
                        null,
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    )
                } else {
                    MediaStore.Images.Media.query(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        null,
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    )
                }

                if (cursor?.moveToFirst() == true) {

                    val imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val imageNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val imagePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    val imageDateModifiedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                    val imageBucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                    val imageBucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                    do {
                        items.add(
                            MediaItem(
                                uniqueId = cursor.getInt(imageIdColumn),
                                name = cursor.getString(imageNameColumn),
                                path = cursor.getString(imagePathColumn),
                                date = cursor.getInt(imageDateModifiedColumn),
                                bucketId = cursor.getInt(imageBucketIdColumn),
                                bucketName = cursor.getString(imageBucketNameColumn),
                                type = MediaItem.Type.IMAGE
                            )
                        )
                    } while (cursor.moveToNext())

                }
            } catch (e: Exception) {
                Timber.e("Content resolving error: $e")
            } finally {
                try {
                    cursor?.close()
                } catch (e: Exception) {
                    Timber.e("Cursor closing error: $e")
                }
            }
        }
        return items
    }

    @SuppressLint("Recycle")
    private fun getSystemMedia(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            var cursor: Cursor? = null
            /**
             * Retrieve images
             */
            try {
                cursor = if (Build.VERSION.SDK_INT >= 26) {
                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        null,
                        null,
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    )
                } else {
                    MediaStore.Images.Media.query(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        null,
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    )
                }

                if (cursor?.moveToFirst() == true) {

                    val imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val imageNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val imagePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    val imageDateModifiedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                    val imageBucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                    val imageBucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                    do {
                        items.add(
                            MediaItem(
                                uniqueId = cursor.getInt(imageIdColumn),
                                name = cursor.getString(imageNameColumn),
                                path = cursor.getString(imagePathColumn),
                                date = cursor.getInt(imageDateModifiedColumn),
                                bucketId = cursor.getInt(imageBucketIdColumn),
                                bucketName = cursor.getString(imageBucketNameColumn),
                                type = MediaItem.Type.IMAGE
                            )
                        )
                    } while (cursor.moveToNext())

                }
            } catch (e: Exception) {
                Timber.e("Content resolving error: $e")
            } finally {
                try {
                    cursor?.close()
                } catch (e: Exception) {
                    Timber.e("Cursor closing error: $e")
                }
            }

            /**
             * Retrieve videos
             */
            try {
                cursor = if (Build.VERSION.SDK_INT >= 26) {
                    context.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projectionVideos,
                        null,
                        null,
                        MediaStore.Video.Media.DATE_MODIFIED + " DESC"
                    )
                } else {
                    MediaStore.Images.Media.query(
                        context.contentResolver,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projectionVideos,
                        null,
                        MediaStore.Video.Media.DATE_MODIFIED + " DESC"
                    )
                }

                if (cursor?.moveToFirst() == true) {

                    val videoIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                    val videoNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    val videoPathColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                    val videoDateModifiedColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                    val videoDurationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                    val videoBucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                    val videoBucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                    do {
                        items.add(
                            MediaItem(
                                uniqueId = cursor.getInt(videoIdColumn),
                                name = cursor.getString(videoNameColumn),
                                path = cursor.getString(videoPathColumn),
                                date = cursor.getInt(videoDateModifiedColumn),
                                duration = cursor.getInt(videoDurationColumn),
                                bucketId = cursor.getInt(videoBucketIdColumn),
                                bucketName = cursor.getString(videoBucketNameColumn),
                                type = MediaItem.Type.VIDEO
                            )
                        )
                    } while (cursor.moveToNext())

                }
            } catch (e: Exception) {
                Timber.e("Content resolving error: $e")
            } finally {
                try {
                    cursor?.close()
                } catch (e: Exception) {
                    Timber.e("Cursor closing error: $e")
                }
            }
        }
        return items.sortedByDescending { it.date }
    }
}