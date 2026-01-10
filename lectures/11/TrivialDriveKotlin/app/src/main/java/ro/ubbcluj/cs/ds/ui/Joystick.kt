package ro.ubbcluj.cs.ds.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Joystick(
  modifier: Modifier = Modifier,
  onMoved: (x: Float, y: Float) -> Unit
) {
  var knobPosition by remember { mutableStateOf(Offset.Zero) }
  var center by remember { mutableStateOf(Offset.Zero) }
  var radius by remember { mutableStateOf(0f) }

  Box(
    modifier = modifier
      .onSizeChanged {
        center = Offset(it.width / 2f, it.height / 2f)
        radius = it.width.coerceAtMost(it.height) / 2f
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragEnd = {
            knobPosition = Offset.Zero
            onMoved(0f, 0f)
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val newPos = knobPosition + dragAmount
            val distance = sqrt(newPos.x * newPos.x + newPos.y * newPos.y)
            val maxDistance = radius * 0.5f // Knob moves within 50% of the base radius

            knobPosition = if (distance > maxDistance) {
              val angle = atan2(newPos.y, newPos.x)
              Offset(
                x = cos(angle) * maxDistance,
                y = sin(angle) * maxDistance
              )
            } else {
              newPos
            }

            // Normalize output to -1.0 to 1.0
            onMoved(
              knobPosition.x / maxDistance,
              knobPosition.y / maxDistance
            )
          }
        )
      }
  ) {
    // Base
    Box(
      modifier = Modifier
        .matchParentSize()
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF444444), Color(0xFF222222))
          )
        )
    )

    // Knob
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .offset { IntOffset(knobPosition.x.roundToInt(), knobPosition.y.roundToInt()) }
        .size(60.dp)
        .shadow(8.dp, CircleShape)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF666666), Color(0xFF333333))
          )
        )
    )
  }
}
