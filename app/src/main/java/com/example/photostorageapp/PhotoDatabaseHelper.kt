// PhotoDatabaseHelper.kt
package com.example.photostorageapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Photo(val id: Int, val imageData: ByteArray)

class PhotoDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "PhotoDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE photos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                image_data BLOB
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS photos")
        onCreate(db)
    }

    fun insertImage(image: ByteArray): Long {
        val db = writableDatabase
        val values = ContentValues().apply { put("image_data", image) }
        val id = db.insert("photos", null, values)
        db.close()
        return id
    }

    fun getAllPhotos(): List<Photo> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, image_data FROM photos", null)
        val photos = mutableListOf<Photo>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val imageData = cursor.getBlob(1)
                photos.add(Photo(id, imageData))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return photos
    }

    fun deletePhoto(id: Int): Int {
        val db = writableDatabase
        val result = db.delete("photos", "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun updatePhoto(id: Int, newImage: ByteArray): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("image_data", newImage)
        }
        val result = db.update("photos", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }
}
