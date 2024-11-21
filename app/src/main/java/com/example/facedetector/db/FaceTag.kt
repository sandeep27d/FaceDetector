package com.example.facedetector.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "face_table")
data class FaceTag(
    @PrimaryKey(autoGenerate = true) val faceId: Int = 0,
    val imageId: Long,
    val tagName: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)