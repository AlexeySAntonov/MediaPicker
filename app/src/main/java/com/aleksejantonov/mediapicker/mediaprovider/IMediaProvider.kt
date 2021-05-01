package com.aleksejantonov.mediapicker.mediaprovider

import kotlinx.coroutines.flow.Flow

interface IMediaProvider {
    fun observeMediaFolders(): Flow<List<FolderItem>>
    fun observeSystemImages(): Flow<List<SystemMediaModel>>
    fun observeScreenshots(): Flow<List<SystemMediaModel>>
    fun observeSystemMedia(): Flow<List<SystemMediaModel>>
}