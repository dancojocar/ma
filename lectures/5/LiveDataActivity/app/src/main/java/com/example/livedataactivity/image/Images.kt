package com.example.livedataactivity.image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

@Composable
fun NetworkImageComponentPicasso(
  url: String,
  modifier: Modifier = Modifier
) {
  // Source code inspired from - https://kotlinlang.slack.com/archives/CJLTWPH7S/p1573002081371500.
  val sizeModifier = modifier
    .fillMaxWidth()
    .sizeIn(maxHeight = 200.dp)
  var image by remember { mutableStateOf<ImageBitmap?>(null) }
  var drawable by remember { mutableStateOf<Drawable?>(null) }
  DisposableEffect(url) {
    val picasso = Picasso.get()
    val target = object : Target {
      override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        drawable = placeHolderDrawable
      }

      override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        drawable = errorDrawable
      }

      override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        image = bitmap?.asImageBitmap()
      }
    }
    picasso
      .load(url)
      .into(target)
    onDispose {
      image = null
      drawable = null
      picasso.cancelRequest(target)
    }
  }

  val theImage = image
  val theDrawable = drawable
  if (theImage != null) {
    Column(
      modifier = sizeModifier,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(bitmap = theImage, contentDescription = null)
    }
  } else if (theDrawable != null) {
    Canvas(modifier = sizeModifier) {
      drawIntoCanvas { canvas ->
        theDrawable.draw(canvas.nativeCanvas)
      }
    }
  }
}