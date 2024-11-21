package com.example.facedetector

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.facedetector.db.AppDatabase
import com.example.facedetector.factory.ViewModelFactory
import com.example.facedetector.gallery.FaceRepository
import com.example.facedetector.gallery.GalleryScreen
import com.example.facedetector.ui.theme.FaceDetectorTheme
import com.example.facedetector.vm.MainActivityViewModel


class MainActivity : ComponentActivity() {


    private val repository: FaceRepository by lazy {
        val database = AppDatabase.getDatabase(this)
        val dao = database.faceTagDao()
        FaceRepository(dao)
    }

    private val viewModel: MainActivityViewModel by viewModels {
        ViewModelFactory(repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanGallery(this)
        } else {
            // Handle permission denial
            Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLoading by viewModel.isLoading.collectAsState()
            val images by viewModel.images.collectAsState()
            FaceDetectorTheme {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(100.dp)
                                .aspectRatio(1f)
                        )
                    }
                } else {
                    GalleryScreen(
                        images
                    ) { imageId, detection, tag ->
                        viewModel.tagFace(
                            imageId,
                            detection,
                            tag
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) ==
                    PackageManager.PERMISSION_GRANTED -> {
                if (viewModel.images.value.isEmpty()) {
                    viewModel.scanGallery(this)
                }
            }
            else -> {
                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }
}