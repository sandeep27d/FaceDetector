package com.example.facedetector.db
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FaceTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: FaceTag)

    @Query("SELECT tagName FROM face_table WHERE imageId = :imageId AND top = :top AND `left` = :left")
    suspend fun fetchTag(imageId: Long, top: Float, left: Float):String?
}