package com.aleksejantonov.mediapicker.mediaprovider

import android.net.Uri
import android.provider.MediaStore

/**
 * @property uniqueId - The unique ID for a row in system db.
 * @property name - /storage/0000-0000/DCIM/Vacation/IMG1024.JPG} would have a name of 'IMG1024.JPG'.
 * @property path - Absolute filesystem path to the media item on disk.
 * @property date - The time the media item was last modified.
 * @property duration - The duration of the media item.
 * @property bucketId - The primary bucket ID of this media item.
 * @property bucketName - The primary bucket display name of this media item.
 * @property type - [SystemMediaModel.Type].
 * @property uri - content://media/external/images(video)/media/4572
 */
data class SystemMediaModel(
    val uniqueId: Long,
    val name: String,
    val path: String,
    val date: Int,
    val duration: Long = 0L,
    val bucketId: Int,
    val bucketName: String,
    val type: Type,
    val uri: Uri = Uri.withAppendedPath(type.contentUri, uniqueId.toString()),
) {

  enum class Type(val contentUri: Uri) {
    IMAGE(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
    VIDEO(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
  }

}