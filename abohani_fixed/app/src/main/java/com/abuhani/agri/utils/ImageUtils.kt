package com.abuhani.agri.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * ضغط الصورة قبل الرفع - يقلل الحجم مع الحفاظ على الجودة
     * يصحح اتجاه الصورة تلقائياً
     */
    fun compressImage(context: Context, uri: Uri, maxSizeKb: Int = 500): ByteArray {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
        
        // تصحيح الاتجاه
        val exifStream = context.contentResolver.openInputStream(uri)!!
        val exif = ExifInterface(exifStream)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        exifStream.close()

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        val maxDimension = 1200
        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val stream2 = context.contentResolver.openInputStream(uri)!!
        var bitmap = BitmapFactory.decodeStream(stream2, null, decodeOptions)!!
        stream2.close()

        // تطبيق تصحيح الاتجاه
        bitmap = rotateBitmap(bitmap, orientation)

        var quality = 85
        var output: ByteArray
        do {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            output = baos.toByteArray()
            quality -= 10
        } while (output.size > maxSizeKb * 1024 && quality > 20)

        bitmap.recycle()
        return output
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
