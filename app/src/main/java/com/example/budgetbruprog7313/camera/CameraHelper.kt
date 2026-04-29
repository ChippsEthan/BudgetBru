package com.example.budgetbruprog7313.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraHelper {
    private const val AUTHORITY_SUFFIX = ".fileprovider"

    fun createImageFile(context: Context): Pair<File, Uri>? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + AUTHORITY_SUFFIX,
                imageFile
            )
            Pair(imageFile, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}