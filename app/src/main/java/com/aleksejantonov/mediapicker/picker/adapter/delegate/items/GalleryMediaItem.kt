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
    val orderNumber: Int,
) : DiffListItem {

    override fun itemId(): Long = id

    companion object {
        fun from(
            systemMediaModel: SystemMediaModel,
            selected: Boolean,
            orderNumber: Int,
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
                orderNumber = orderNumber
            )
        }

    }

}
