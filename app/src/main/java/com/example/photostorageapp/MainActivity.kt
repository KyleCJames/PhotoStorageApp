// MainActivity.kt
package com.example.photostorageapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var dbHelper: PhotoDatabaseHelper
    private lateinit var adapter: PhotoAdapter

    private var selectedPhotoIdToUpdate: Int? = null

    private lateinit var imagePicker: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.fab)
        dbHelper = PhotoDatabaseHelper(this)

        adapter = PhotoAdapter(listOf(), this::deletePhoto, this::updatePhotoPrompt)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val imageBytes = bitmapToByteArray(bitmap)

                selectedPhotoIdToUpdate?.let { id ->
                    dbHelper.updatePhoto(id, imageBytes)
                    Toast.makeText(this, "Photo updated", Toast.LENGTH_SHORT).show()
                    selectedPhotoIdToUpdate = null
                } ?: run {
                    dbHelper.insertImage(imageBytes)
                    Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show()
                }

                loadPhotos()
            }
        }

        fab.setOnClickListener {
            selectedPhotoIdToUpdate = null
            imagePicker.launch("image/*")
        }

        loadPhotos()
    }

    private fun loadPhotos() {
        val photos = dbHelper.getAllPhotos()
        adapter.updatePhotos(photos)
    }

    private fun deletePhoto(photo: Photo) {
        dbHelper.deletePhoto(photo.id)
        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show()
        loadPhotos()
    }

    private fun updatePhotoPrompt(photo: Photo) {
        selectedPhotoIdToUpdate = photo.id
        imagePicker.launch("image/*")
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
