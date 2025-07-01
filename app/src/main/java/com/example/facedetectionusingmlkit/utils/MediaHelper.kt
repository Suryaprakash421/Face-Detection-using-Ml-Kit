package com.example.facedetectionusingmlkit.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import java.io.File
import kotlin.collections.set

object MediaHelper {

    /**
     * Get Gallery images
     * */
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

    /**
     * Get WhatsApp images
     * */
    fun getWhatsAppPhotos(context: Context): List<GalleryPhotoEntity> {
        val galleryPhotos = mutableListOf<GalleryPhotoEntity>()
        val folderImageCount = mutableMapOf<String, Int>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA, // File path (Deprecated API 29+, still usable for WhatsApp filter pre-API 30)
            MediaStore.Images.Media.DATE_TAKEN
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%WhatsApp%")

        context.contentResolver.query(
            queryUri, projection, selection, selectionArgs, sortOrder
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
                val folder = File(path).parent
                if (folder != null) {
                    folderImageCount[folder] = folderImageCount.getOrDefault(folder, 0) + 1
                }

                val fileUri = ContentUris.withAppendedId(queryUri, id)

                galleryPhotos.add(
                    GalleryPhotoEntity(
                        id = id.toInt(), // Room will auto-generate this
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
        folderImageCount.forEach { (folder, count) ->
            Logger.d("WhatsAppImageFolders", "Folder: $folder, Photos: $count")
        }
        return galleryPhotos
    }

}
