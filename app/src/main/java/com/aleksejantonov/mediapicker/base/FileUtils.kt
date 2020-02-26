package com.aleksejantonov.mediapicker.base

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private val defaultLocale: Locale get() = Locale.getDefault()
private val IMAGE_FILE_DATE_FORMATTER = SimpleDateFormat("yyyyMMdd_HHmmss", defaultLocale)

@Throws(IOException::class)
fun createImageFile(context: Context): File? {
    // Create an image file name
    val timeStamp: String = IMAGE_FILE_DATE_FORMATTER.format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return storageDir?.let {
        File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            it /* directory */
        )
    }
}