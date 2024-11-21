package com.example.facedetector

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.facedetector.db.AppDatabase
import com.example.facedetector.db.FaceTag
import com.example.facedetector.db.FaceTagDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class FaceTagDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var faceTagDao: FaceTagDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()

        faceTagDao = database.faceTagDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun sample_test(){
        assert(true)
    }

    @Test
    fun insertAndRetrieveTag_success() = runBlocking {
        val faceTag = FaceTag(imageId = 1L, faceId = 1, tagName = "PersonA", left = 10f, top = 10f, right = 50f, bottom = 50f)
        faceTagDao.insertTag(faceTag)

        val retrievedTag = faceTagDao.fetchTag(1L, 10f, left = 10f)
        Assert.assertEquals("PersonA", retrievedTag)
    }

    @Test
    fun retrieveTag_noMatchingData_returnsNull() = runBlocking {
        val retrievedTag = faceTagDao.fetchTag(2L, 10f, left = 10f)
        Assert.assertNull(retrievedTag)
    }

    @Test
    fun largeDatasetQuery_performanceTest() = runBlocking {
        val faceTags = List(10000) {
            FaceTag(imageId = it.toLong(), faceId = it, tagName = "Person$it", left = 10f, top = 10f+it, right = 50f, bottom = 50f)
        }

        faceTags.forEach { faceTagDao.insertTag(it) }

        val tag = faceTagDao.fetchTag(5000.toLong(), top = 5010f, left = 10f)
        Assert.assertEquals("Person5000", tag)
    }

}
