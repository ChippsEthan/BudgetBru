package com.example.budgetbruprog7313.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraHelper {

    private const val AUTHORITY_SUFFIX = ".fileprovider"

    fun createImageFile(context: Context): Pair<File, Uri>? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageDir = File(context.filesDir, "expense_photos")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            val imageFile = File(imageDir, "EXPENSE_${timeStamp}.jpg")
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

    fun getTakePhotoIntent(photoUri: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }
}