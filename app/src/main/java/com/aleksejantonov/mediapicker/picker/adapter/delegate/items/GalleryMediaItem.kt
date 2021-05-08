package com.aleksejantonov.mediapicker.picker.adapter.delegate.items

import android.net.Uri
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.picker.data.SystemMediaModel


data class GalleryMediaItem(
    val id: Long,
    val path: String,
    val uri: Uri,
    val duration: Long,
    val bucketId: Int,
    val bucketName: String,
    val isVideo: Boolean,
    val selected: Boolean,
) : DiffListItem {

    override fun isTheSame(other: DiffListItem): Boolean = other is GalleryMediaItem
            && other.id == this.id

    override fun isContentTheSame(other: DiffListItem): Boolean = other is GalleryMediaItem
            && this == other

    companion object {
        fun from(
            systemMediaModel: SystemMediaModel,
            selected: Boolean,
        ) = with(systemMediaModel) {
            GalleryMediaItem(
                id = uniqueId,
                path = path,
                uri = uri,
                duration = duration,
                bucketId = bucketId,
                bucketName = bucketName,
                isVideo = type == SystemMediaModel.Type.VIDEO,
                selected = selected,
            )
        }

    }

}
