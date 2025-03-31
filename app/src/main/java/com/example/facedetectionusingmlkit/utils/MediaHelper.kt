package com.example.facedetectionusingmlkit.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity

fun getGalleryPhotos(context: Context): List<GalleryPhotoEntity> {
    val galleryPhotos = mutableListOf<GalleryPhotoEntity>()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA, // File path (Deprecated in API 29+)
        MediaStore.Images.Media.DATE_TAKEN
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

    val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    context.contentResolver.query(
        queryUri, projection, null, null, sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val path = cursor.getString(pathColumn)
            val dateTaken = cursor.getLong(dateColumn)

            val fileUri = ContentUris.withAppendedId(queryUri, id)

            galleryPhotos.add(
                GalleryPhotoEntity(
                    id = 0, // Room will auto-generate this
                    fileUri = fileUri,
                    photoName = name,
                    filePath = path,
                    capturedDate = dateTaken,
                    isProcessed = false,
                    noOfFaces = 0
                )
            )
        }
    }

    return galleryPhotos
}
