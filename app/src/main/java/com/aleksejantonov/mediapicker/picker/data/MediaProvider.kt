package com.aleksejantonov.mediapicker.picker.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.SparseArray
import androidx.core.util.forEach
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

class MediaProvider(private val context: Context) : IMediaProvider {

    private val mediaFolders = SparseArray<FolderItem>()
    private val mediaFoldersDataFlow = MutableStateFlow<List<FolderItem>>(emptyList())

    override fun observeMediaFolders(): Flow<List<FolderItem>> = mediaFoldersDataFlow

    override fun observeSystemImages(): Flow<List<SystemMediaModel>> = callbackFlow {
        val imagesContentObserver = object : ContentObserver(Handler(context.mainLooper)) {
            override fun onChange(selfChange: Boolean) {
                offer(getSystemImages())
            }
        }

        context.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imagesContentObserver)

        // Initial data
        offer(getSystemImages())

        awaitClose { context.contentResolver.unregisterContentObserver(imagesContentObserver) }
    }

    override fun observeScreenshots(): Flow<List<SystemMediaModel>> = callbackFlow {
        val imagesContentObserver = object : ContentObserver(Handler(context.mainLooper)) {
            override fun onChange(selfChange: Boolean) {
                offer(getScreenshots())
            }
        }

        context.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imagesContentObserver)

        // Initial data
        offer(getScreenshots())

        awaitClose { context.contentResolver.unregisterContentObserver(imagesContentObserver) }
    }

    override fun observeSystemMedia(): Flow<List<SystemMediaModel>> = callbackFlow {
        val imagesContentObserver = object : ContentObserver(Handler(context.mainLooper)) {
            override fun onChange(selfChange: Boolean) {
                offer(getSystemMedia())
            }
        }
        val videosContentObserver = object : ContentObserver(Handler(context.mainLooper)) {
            override fun onChange(selfChange: Boolean) {
                offer(getSystemMedia())
            }
        }

        context.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imagesContentObserver)
        context.contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, videosContentObserver)

        // Initial data
        offer(getSystemMedia())

        awaitClose {
            context.contentResolver.unregisterContentObserver(imagesContentObserver)
            context.contentResolver.unregisterContentObserver(videosContentObserver)
        }
    }

    private fun getSystemImages(): List<SystemMediaModel> {
        mediaFolders.clear()

        val items = mutableListOf<SystemMediaModel>()
        if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            var cursor: Cursor? = null
            try {
                cursor = if (Build.VERSION.SDK_INT >= 26) {
                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        SELECTION_PHOTOS,
                        null,
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    )
                } else {
                    MediaStore.Images.Media.query(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        SELECTION_PHOTOS,
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
                        val systemMediaModel = SystemMediaModel(
                            uniqueId = cursor.getLong(imageIdColumn),
                            name = cursor.getString(imageNameColumn),
                            path = cursor.getString(imagePathColumn),
                            date = cursor.getInt(imageDateModifiedColumn),
                            bucketId = cursor.getInt(imageBucketIdColumn),
                            bucketName = cursor.getString(imageBucketNameColumn),
                            type = SystemMediaModel.Type.IMAGE
                        )
                        updateMediaFolders(model = systemMediaModel)
                        items.add(systemMediaModel)
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
        mediaFoldersDataFlow.value = mediaFolders.toSortedList()
        return items
    }

    private fun getScreenshots(): List<SystemMediaModel> {
        val items = mutableListOf<SystemMediaModel>()
        if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            var cursor: Cursor? = null
            try {
                cursor = if (Build.VERSION.SDK_INT >= 26) {
                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        SELECTION_SCREENSHOTS,
                        null,
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    )
                } else {
                    MediaStore.Images.Media.query(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        SELECTION_SCREENSHOTS,
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
                            SystemMediaModel(
                                uniqueId = cursor.getLong(imageIdColumn),
                                name = cursor.getString(imageNameColumn),
                                path = cursor.getString(imagePathColumn),
                                date = cursor.getInt(imageDateModifiedColumn),
                                bucketId = cursor.getInt(imageBucketIdColumn),
                                bucketName = cursor.getString(imageBucketNameColumn),
                                type = SystemMediaModel.Type.IMAGE
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

    private fun getSystemMedia(): List<SystemMediaModel> {
        mediaFolders.clear()

        val items = mutableListOf<SystemMediaModel>()
        if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            var cursor: Cursor? = null
            /**
             * Retrieve images
             */
            try {
                cursor = if (Build.VERSION.SDK_INT >= 26) {
                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        SELECTION_PHOTOS,
                        null,
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    )
                } else {
                    MediaStore.Images.Media.query(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionPhotos,
                        SELECTION_PHOTOS,
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
                        val systemMediaModel = SystemMediaModel(
                            uniqueId = cursor.getLong(imageIdColumn),
                            name = cursor.getString(imageNameColumn),
                            path = cursor.getString(imagePathColumn),
                            date = cursor.getInt(imageDateModifiedColumn),
                            bucketId = cursor.getInt(imageBucketIdColumn),
                            bucketName = cursor.getString(imageBucketNameColumn),
                            type = SystemMediaModel.Type.IMAGE
                        )
                        updateMediaFolders(systemMediaModel)
                        items.add(systemMediaModel)
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
                        SELECTION_VIDEOS,
                        null,
                        MediaStore.Video.Media.DATE_MODIFIED + " DESC"
                    )
                } else {
                    MediaStore.Images.Media.query(
                        context.contentResolver,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projectionVideos,
                        SELECTION_VIDEOS,
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
                        val systemMediaModel = SystemMediaModel(
                            uniqueId = cursor.getLong(videoIdColumn),
                            name = cursor.getString(videoNameColumn),
                            path = cursor.getString(videoPathColumn),
                            date = cursor.getInt(videoDateModifiedColumn),
                            duration = cursor.getLong(videoDurationColumn),
                            bucketId = cursor.getInt(videoBucketIdColumn),
                            bucketName = cursor.getString(videoBucketNameColumn),
                            type = SystemMediaModel.Type.VIDEO
                        )
                        updateMediaFolders(systemMediaModel)
                        items.add(systemMediaModel)
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
        mediaFoldersDataFlow.value = mediaFolders.toSortedList()
        return items.sortedByDescending { it.date }
    }

    private fun updateMediaFolders(model: SystemMediaModel) {
        val allMediaFolder = mediaFolders[RECENTS_BUCKET_ID]
        if (allMediaFolder == null) {
            mediaFolders.put(
                RECENTS_BUCKET_ID,
              FolderItem.default(name = "All media", previewUri = model.uri)
            )
        } else {
            mediaFolders.put(RECENTS_BUCKET_ID, allMediaFolder.copy(mediaCount = allMediaFolder.mediaCount + 1))
        }

        val particularMediaFolder = mediaFolders[model.bucketId]
        if (particularMediaFolder == null) {
            mediaFolders.put(
                model.bucketId,
                FolderItem(id = model.bucketId, name = model.bucketName, mediaCount = 1, previewUri = model.uri, lastModified = model.date)
            )
        } else {
            mediaFolders.put(model.bucketId, particularMediaFolder.copy(mediaCount = particularMediaFolder.mediaCount + 1))
        }
    }

    private fun SparseArray<FolderItem>.toSortedList(): List<FolderItem> {
        val resultList = mutableListOf<FolderItem>()
        forEach { _, value -> resultList.add(value) }
        return resultList.sortedByDescending { it.lastModified }
    }

    companion object {
        private const val JPEG_LONG_FILE_EXTENSION = "jpeg"
        private const val PNG_FILE_EXTENSION = "png"
        private const val MP4_FILE_EXTENSION = "mp4"

        private const val IMAGE_JPEG_MIME_TYPE = "image/$JPEG_LONG_FILE_EXTENSION"
        private const val IMAGE_PNG_MIME_TYPE = "image/$PNG_FILE_EXTENSION"
        private const val VIDEO_MP4_MIME_TYPE = "video/$MP4_FILE_EXTENSION"

        const val RECENTS_BUCKET_ID = 0

        private val DIRECTORY_SCREENSHOTS: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Environment.DIRECTORY_SCREENSHOTS else "Screenshots"

        private const val SELECTION_PHOTOS = "${MediaStore.Files.FileColumns.MIME_TYPE} IN ('$IMAGE_JPEG_MIME_TYPE','$IMAGE_PNG_MIME_TYPE')"
        private const val SELECTION_VIDEOS = "${MediaStore.Files.FileColumns.MIME_TYPE} IS '$VIDEO_MP4_MIME_TYPE'"
        private val SELECTION_SCREENSHOTS = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} IS '$DIRECTORY_SCREENSHOTS' COLLATE NOCASE"

        /**
         * Ignore warnings about the following columns:
         * [MediaStore.Images.Media.BUCKET_ID], [MediaStore.Images.Media.BUCKET_DISPLAY_NAME], [MediaStore.Video.Media.DURATION]
         * Until [Build.VERSION_CODES.Q] these columns were declared in [MediaStore.Images.ImageColumns]/[MediaStore.Video.VideoColumns]
         * Since [Build.VERSION_CODES.Q] these columns is declared in [MediaStore.MediaColumns].
         * [MediaStore.Images.ImageColumns] and [MediaStore.Video.VideoColumns] extend [MediaStore.MediaColumns], so access remained the same.
         */
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

    }
}