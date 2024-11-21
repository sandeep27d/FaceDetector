package com.example.facedetector.vm

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facedetector.gallery.FaceRepository
import com.example.facedetector.gallery.FaceWithTag
import com.example.facedetector.gallery.ImageWithFaces
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Detection
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: FaceRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _images = MutableStateFlow<MutableList<ImageWithFaces>>(mutableListOf())
    val images: StateFlow<List<ImageWithFaces>> get() = _images

    @RequiresApi(Build.VERSION_CODES.O)
    fun scanGallery(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {

            val baseOptionsBuilder = BaseOptions.builder()
            baseOptionsBuilder.setDelegate(Delegate.CPU)
            baseOptionsBuilder.setModelAssetPath("face_detection_short_range.tflite")

            val optionsBuilder =
                FaceDetector.FaceDetectorOptions.builder()
                    .setBaseOptions(baseOptionsBuilder.build())
                    .setMinDetectionConfidence(0.5F)
                    .setRunningMode(RunningMode.IMAGE)


            val options = optionsBuilder.build()
            val faceDetector = FaceDetector.createFromOptions(context, options)


            val queryArgs = Bundle()
            queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, 20)

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA
            )
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null,

                )

            val tempImages = mutableListOf<ImageWithFaces>()
            cursor?.use {
                while (it.moveToNext() && tempImages.size < 50) {
                    val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
                    val bitmap =
                        if (index == -1) null else BitmapFactory.decodeFile(it.getString(index))

                    if (bitmap != null) {
                        val idIndex = it.getColumnIndex(MediaStore.Images.Media._ID)
                        val imageId = if (idIndex == -1) 0 else it.getLong(idIndex)

                        val mpImage = BitmapImageBuilder(bitmap).build()
                        val faceListWithTags = mutableListOf<FaceWithTag>()
                        faceDetector?.detect(mpImage)?.also { detectionResult ->
                            if (detectionResult.detections().isNotEmpty()) {
                                for (detection in detectionResult.detections()) {
                                    val tag = repository.getTag(
                                        imageId,
                                        detection.boundingBox().top,
                                        detection.boundingBox().left
                                    ) ?: ""
                                    faceListWithTags.add(FaceWithTag(tag, detection))
                                }
                                tempImages.add(ImageWithFaces(imageId, bitmap, faceListWithTags))
                            }
                        }
                    }
                }
            }

            _images.value = tempImages
            _isLoading.value = false
        }
    }

    fun tagFace(imageId: Long, detection: Detection, tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveTag(imageId, detection, tag)
        }
    }
}

