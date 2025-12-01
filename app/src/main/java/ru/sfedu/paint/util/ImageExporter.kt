package ru.sfedu.paint.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageExporter {
    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        format: ImageFormat = ImageFormat.PNG,
        quality: Int = 100
    ): Uri? = withContext(Dispatchers.IO) {
        val filename = "Paint_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.${format.extension}"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Paint")
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(format.compressFormat, quality, outputStream)
                }
            }
            uri
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val paintDir = File(imagesDir, "Paint")
            if (!paintDir.exists()) {
                paintDir.mkdirs()
            }
            
            val imageFile = File(paintDir, filename)
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(format.compressFormat, quality, outputStream)
            }
            
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
            }
            
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }
    }
    
    enum class ImageFormat(val extension: String, val mimeType: String, val compressFormat: android.graphics.Bitmap.CompressFormat) {
        PNG("png", "image/png", android.graphics.Bitmap.CompressFormat.PNG),
        JPEG("jpg", "image/jpeg", android.graphics.Bitmap.CompressFormat.JPEG)
    }
}











