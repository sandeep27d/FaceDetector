package com.example.facedetector

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.mediapipe.tasks.components.containers.Detection
import com.skydoves.landscapist.glide.GlideImage
import kotlin.math.min

@Composable
fun GalleryScreen(images: List<ImageWithFaces>, onTag: (Long, Detection, String) -> Unit) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(140.dp),
    ) {
        items(images) { imageWithFaces ->
            ImageCard(
                image = imageWithFaces,
                tagFace = { detection, tag ->
                    onTag(imageWithFaces.imageId, detection, tag)
                }
            )
        }
    }
}


@Composable
fun ImageCard(
    image: ImageWithFaces,
    tagFace: (Detection, String) -> Unit
) {

    val aspectRatio: Float by remember { derivedStateOf { image.bitmap.width.toFloat() / image.bitmap.height.toFloat() } }
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .aspectRatio(aspectRatio),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boxWidth = this.maxWidth
            val imgWidth = boxWidth.value
            val imgHeight = boxWidth.value / aspectRatio
            val scaleFactor = min(
                imgWidth * 1f / image.bitmap.width,
                imgHeight * 1f / image.bitmap.height
            )

            GlideImage(
                imageModel = { image.bitmap }, modifier = Modifier
                    .width(boxWidth)
            )

            for (face in image.faces) {
                val detection = face.detection
                val boundingBox = detection.boundingBox()

                val top = boundingBox.top.dp * scaleFactor
                val bottom = boundingBox.bottom.dp * scaleFactor
                val left = boundingBox.left.dp * scaleFactor
                val right = boundingBox.right.dp * scaleFactor
                val mutableTag = remember {
                    mutableStateOf(face.faceTag)
                }

                var openDialog by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .width(right)
                        .height(bottom)
                        .padding(start = left, top = top)
                        .background(Color.Transparent)
                        .border(1.dp, Color.Red)
                        .clickable {
                            openDialog = true
                        }
                )

                if (openDialog)
                    InputDialogView { tag ->
                        openDialog = false
                        if (tag.isNotEmpty()) {
                            tagFace(detection, tag)
                        }
                        mutableTag.value = tag
                        Toast
                            .makeText(context, tag, Toast.LENGTH_SHORT)
                            .show()
                    }

                val tag by mutableTag
                Text(
                    text = tag,
                    modifier = Modifier.padding(start = left, top = bottom - 20.dp),
                    color = Color.Green
                )
            }
        }
    }
}

@Composable
fun InputDialogView(onDismiss: (text: String) -> Unit) {
    val context = LocalContext.current
    var newTag by remember {
        mutableStateOf("")
    }

    Dialog(onDismissRequest = { onDismiss("") }) {
        Card(
            //shape = MaterialTheme.shapes.medium,
            shape = RoundedCornerShape(10.dp),
            // modifier = modifier.size(280.dp, 240.dp)
            modifier = Modifier.padding(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                Modifier
                    .background(Color.White)
            ) {

                Text(
                    text = "Tag this person",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 20.sp
                )

                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it }, modifier = Modifier.padding(8.dp),
                    label = { Text("Tage Face") }
                )

                Row {
                    OutlinedButton(
                        onClick = { onDismiss("") },
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1F)
                    ) {
                        Text(text = "Cancel")
                    }


                    Button(
                        onClick = {
                            Toast.makeText(context, newTag, Toast.LENGTH_SHORT).show()
                            onDismiss(newTag)
                        },
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1F)
                    ) {
                        Text(text = "Ok")
                    }
                }


            }
        }
    }
}