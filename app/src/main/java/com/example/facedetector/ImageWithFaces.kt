package com.example.facedetector
import android.graphics.Bitmap
import com.google.mediapipe.tasks.components.containers.Detection

data class ImageWithFaces(
    val imageId: Long,
    val bitmap: Bitmap,
    val faces: MutableList<FaceWithTag>
)

data class FaceWithTag(val faceTag: String, val detection: Detection)