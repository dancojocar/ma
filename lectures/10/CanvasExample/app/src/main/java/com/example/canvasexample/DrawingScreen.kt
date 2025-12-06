package com.example.canvasexample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import kotlin.math.abs

@Composable
fun DrawingScreen() {
    val drawColor = colorResource(R.color.opaque_yellow)
    val backgroundColor = colorResource(R.color.opaque_orange)

    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var canvas by remember { mutableStateOf<androidx.compose.ui.graphics.Canvas?>(null) }
    var redrawTrigger by remember { mutableStateOf(0) }

    val paint = remember {
        Paint().apply {
            color = drawColor
            strokeWidth = 12f
            style = PaintingStyle.Stroke
            strokeCap = StrokeCap.Round
            strokeJoin = StrokeJoin.Round
            isAntiAlias = true
        }
    }

    val path = remember { Path() }
    var mX by remember { mutableStateOf(0f) }
    var mY by remember { mutableStateOf(0f) }
    val touchTolerance = 4f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                if (bitmap == null) {
                    val img = ImageBitmap(size.width, size.height, ImageBitmapConfig.Argb8888)
                    val cvs = androidx.compose.ui.graphics.Canvas(img)
                    // Fill background
                    val bgPaint = Paint().apply { color = backgroundColor }
                    cvs.drawRect(Rect(0f, 0f, size.width.toFloat(), size.height.toFloat()), bgPaint)
                    
                    // Draw frame
                    val framePaint = Paint().apply { 
                        color = drawColor // Using drawColor for frame as per original logic? 
                        // Original: mPaint (which is drawColor) used for frame?
                        // canvas.drawRect(mFrame!!, mPaint)
                        // Yes.
                        style = PaintingStyle.Stroke
                        strokeWidth = 12f // Original didn't specify width for frame, but mPaint has width 12f.
                    }
                    val inset = 90f
                    cvs.drawRect(Rect(inset, inset, size.width - inset, size.height - inset), paint)

                    bitmap = img
                    canvas = cvs
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        path.reset()
                        path.moveTo(offset.x, offset.y)
                        mX = offset.x
                        mY = offset.y
                    },
                    onDrag = { change, _ ->
                        val x = change.position.x
                        val y = change.position.y
                        val dx = abs(x - mX)
                        val dy = abs(y - mY)
                        if (dx >= touchTolerance || dy >= touchTolerance) {
                            path.quadraticBezierTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                            mX = x
                            mY = y
                            canvas?.drawPath(path, paint)
                            redrawTrigger++ // Trigger redraw
                        }
                    },
                    onDragEnd = {
                        path.reset()
                    }
                )
            }
    ) {
        // Read redrawTrigger to ensure recomposition
        val trigger = redrawTrigger
        bitmap?.let {
            drawImage(it)
        }
    }
}
