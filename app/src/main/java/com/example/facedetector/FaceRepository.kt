package com.example.facedetector

import com.example.facedetector.db.FaceTag
import com.example.facedetector.db.FaceTagDao
import com.google.mediapipe.tasks.components.containers.Detection


class FaceRepository (private val dao: FaceTagDao) {
    suspend fun saveTag(imageId: Long, detection: Detection, tag: String) {
        dao.insertTag(
            FaceTag(
                imageId = imageId,
                left = detection.boundingBox().left,
                top = detection.boundingBox().top,
                right = detection.boundingBox().right,
                bottom = detection.boundingBox().bottom,
                tagName = tag
            )
        )
    }

    suspend fun getTag(imageId: Long, top: Float, left: Float): String? {
        return dao.fetchTag(imageId, top, left)
    }
}