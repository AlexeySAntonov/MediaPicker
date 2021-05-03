package com.aleksejantonov.mediapicker.picker.data

import android.net.Uri

data class FolderItem(
    val id: Int,
    val name: String,
    val mediaCount: Int,
    val previewUri: Uri,
    val lastModified: Int,
) {

  companion object {
    fun default(name: String, previewUri: Uri = Uri.EMPTY) = FolderItem(
        id = MediaProvider.RECENTS_BUCKET_ID,
        name = name,
        mediaCount = 1,
        previewUri = previewUri,
        lastModified = Int.MAX_VALUE
    )
  }
}
