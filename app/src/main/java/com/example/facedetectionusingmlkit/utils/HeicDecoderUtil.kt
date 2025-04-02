package com.example.facedetectionusingmlkit.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaCodecList
import android.media.MediaCodecInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.InputStream

object HeicDecoderUtil {
    const val MY_TAG = "HeicDecoderUtil"
    /**
    Decode a HEIC or any image from URI.
    Uses hardware-accelerated ImageDecoder when possible, otherwise falls back to BitmapFactory for JPEG/PNG only.
     */
    fun decodeBitmap(context: Context, uri: Uri, targetSize: Int? = null): Bitmap {
        val mimeType = context.contentResolver.getType(uri)
        Log.i(MY_TAG, "mimeType: $mimeType")
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mimeType?.contains("heic") == true) {
                if (isHeicHardwareDecodeSupported()) {
                    Log.d(MY_TAG, "Hardware decode")
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_HARDWARE
                        targetSize?.let {
                            decoder.setTargetSize(it, it * info.size.height / info.size.width)
                        }
                    }
                } else {
                    Log.e(MY_TAG, "HEIC hardware decode not supported")
                    throw UnsupportedOperationException("HEIC hardware decode not supported")
                }
            } else {
                Log.i(MY_TAG, "Non Heic format image with below version Android 9")
                decodeWithBitmapFactory(context.contentResolver, uri, targetSize)
            }
        } catch (e: Exception) {
            Log.e("HeicDecoderUtil", "Fallback decode: ${e.message}")
            decodeWithBitmapFactory(context.contentResolver, uri, targetSize)
        }
    }

    private fun decodeWithBitmapFactory(
        resolver: ContentResolver,
        uri: Uri,
        targetSize: Int? = null
    ): Bitmap {
        val options = BitmapFactory.Options()
        if (targetSize != null) {
            options.inJustDecodeBounds = true
            resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false
        }
        return resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)!!
        } ?: throw IllegalArgumentException("Cannot decode image: $uri")
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun isHeicHardwareDecodeSupported(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        return codecList.codecInfos.any { codecInfo ->
            !codecInfo.isEncoder && codecInfo.supportedTypes.any { type ->
                type.equals("image/vnd.android.heic", ignoreCase = true) ||
                        type.equals("video/hevc", ignoreCase = true)
            }
        }
    }
}