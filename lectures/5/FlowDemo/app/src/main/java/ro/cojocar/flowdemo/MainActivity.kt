package ro.cojocar.flowdemo

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val time by ticTac().collectAsStateWithLifecycle(initialValue = Triple(0, 0, 0))
      FlowDemoTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          AnalogClock(time.first, time.second, time.third)
        }
      }
    }
  }
}

@Composable
fun AnalogClock(hours: Int, minutes: Int, seconds: Int, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawClock(hours, minutes, seconds)
    }
  }
}

private const val totalHours = 24

private fun DrawScope.drawClock(hours: Int, minutes: Int, seconds: Int) {
  val center = this.center
  val radius = size.minDimension / 2

  // Draw the clock face
  drawCircle(
    color = Color.Black,
    radius = radius,
    center = center,
    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
  )

  // Draw the hour numbers
  val hourPaint = Paint().apply {
    textAlign = Paint.Align.CENTER
    textSize = 40f
    color = 0xFF000000.toInt()
  }

  for (i in 1..totalHours) {
    val angle = i * 15 - 90
    val textRadius = radius * 0.8f
    val x = center.x + textRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
    val y =
      center.y + textRadius * sin(Math.toRadians(angle.toDouble())).toFloat() + hourPaint.textSize / 3
    drawContext.canvas.nativeCanvas.drawText(i.toString(), x, y, hourPaint)
  }

  // Draw the minute labels
  val minutePaint = Paint().apply {
    textAlign = Paint.Align.CENTER
    textSize = 20f
    color = 0xFF000000.toInt()
  }

  for (i in 5..60 step 5) {
    val angle = i * 6 - 90
    val textRadius = radius - 60f
    val x = center.x + textRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
    val y =
      center.y + textRadius * sin(Math.toRadians(angle.toDouble())).toFloat() + minutePaint.textSize / 3
    drawContext.canvas.nativeCanvas.drawText(i.toString(), x, y, minutePaint)
  }

  // Draw the minute ticks
  for (i in 0 until 60) {
    val angle = i * 6 - 90
    val tickLength = if (i % 5 == 0) 20f else 10f
    val startRadius = radius - tickLength
    val endRadius = radius
    val start = Offset(
      x = center.x + startRadius * cos(Math.toRadians(angle.toDouble())).toFloat(),
      y = center.y + startRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
    )
    val end = Offset(
      x = center.x + endRadius * cos(Math.toRadians(angle.toDouble())).toFloat(),
      y = center.y + endRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
    )
    drawLine(color = Color.Black, start = start, end = end, strokeWidth = 2f)
  }

  // Draw the hour hand
  val hourAngle = (hours % totalHours + minutes / 60f) * 15 - 90
  val hourHandLength = radius * 0.5f
  val hourStart = center
  val hourEnd = Offset(
    x = center.x + hourHandLength * cos(Math.toRadians(hourAngle.toDouble())).toFloat(),
    y = center.y + hourHandLength * sin(Math.toRadians(hourAngle.toDouble())).toFloat()
  )
  drawLine(color = Color.Black, start = hourStart, end = hourEnd, strokeWidth = 8f)

  // Draw the minute hand
  val minuteAngle = (minutes + seconds / 60f) * 6 - 90
  val minuteHandLength = radius * 0.7f
  val minuteStart = center
  val minuteEnd = Offset(
    x = center.x + minuteHandLength * cos(Math.toRadians(minuteAngle.toDouble())).toFloat(),
    y = center.y + minuteHandLength * sin(Math.toRadians(minuteAngle.toDouble())).toFloat()
  )
  drawLine(color = Color.Black, start = minuteStart, end = minuteEnd, strokeWidth = 4f)

  // Draw the second hand
  val secondAngle = (seconds % 60) * 6 - 90
  val secondHandLength = radius * 0.9f
  val secondStart = center
  val secondEnd = Offset(
    x = center.x + secondHandLength * cos(Math.toRadians(secondAngle.toDouble())).toFloat(),
    y = center.y + secondHandLength * sin(Math.toRadians(secondAngle.toDouble())).toFloat()
  )
  drawLine(color = Color.Red, start = secondStart, end = secondEnd, strokeWidth = 2f)
}

private fun ticTac() = flow {
  val calendar = Calendar.getInstance()
  while (true) {
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)
    emit(Triple(hours, minutes, seconds))
    delay(1000)
    calendar.add(Calendar.SECOND, 1)
  }
}

@Composable
fun FlowDemoTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    content = content
  )
}