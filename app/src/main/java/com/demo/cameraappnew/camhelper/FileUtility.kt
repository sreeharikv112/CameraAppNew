package com.demo.cameraappnew.camhelper

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileUtility {

    @Throws(IOException::class)
    fun createImageFileWithBitmap(context: Context,bitmap: Bitmap):File{
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val file = File(storageDir,"JPEG_${timeStamp}_.jpeg")
        if (!file.exists()) {
            val fos = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        }
    return file
    }
}