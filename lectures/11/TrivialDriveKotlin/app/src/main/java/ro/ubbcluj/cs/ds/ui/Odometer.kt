package ro.ubbcluj.cs.ds.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.floor
import kotlin.math.max

@Composable
fun OdometerDisplay(
  value: Double,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .border(2.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
      .background(Color.Black)
      .padding(4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    // Calculate each digit's effective position (rotation)
    // We display 6 digits: 100000s, 10000s, 1000s, 100s, 10s, 1s
    val numDigits = 6
    val positions = DoubleArray(numDigits)

    // Algorithm for mechanical carry
    // pos[0] (1s) = value
    // pos[i] = floor(value / 10^i) + max(0, pos[i-1]%10 - 9)

    var previousPos = value
    for (i in 0 until numDigits) {
      val divisor = Math.pow(10.0, i.toDouble())
      val raw = value / divisor
      val base = floor(raw)
      val remainder = previousPos % 10.0
      val carry = max(0.0, remainder - 9.0)

      // For the 1s place, we just use the raw value so it scrolls continuously
      // For others, it snaps until the carry kicks in
      if (i == 0) {
        positions[i] = value
      } else {
        positions[i] = base + carry
      }
      previousPos = positions[i]
    }

    // Render from Left (High) to Right (Low), so reverse the array indexing
    for (i in (numDigits - 1) downTo 0) {
      OdometerDigit(position = positions[i])
      if (i > 0) {
        Spacer(modifier = Modifier.width(1.dp))
      }
    }
  }
}

@Composable
fun OdometerDigit(position: Double) {
  val digitHeight = 36.dp
  val digitWidth = 24.dp

  // The visual window shows just 1 digit height
  // We draw a strip of numbers moving up/down
  // To fake the specific digit, we look at position % 10

  val currentDigitValue = position % 10.0
  val wholePart = floor(currentDigitValue).toInt()
  val fractionPart = (currentDigitValue - wholePart).toFloat()

  // If fractionPart is 0.5, we show half of N and half of N+1
  // Visual offset: -fractionPart * digitHeight

  Box(
    modifier = Modifier
      .width(digitWidth)
      .height(digitHeight)
      .clip(RoundedCornerShape(2.dp))
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFF222222),
            Color(0xFF111111),
            Color(0xFF000000),
            Color(0xFF111111),
            Color(0xFF222222)
          )
        )
      )
      .border(0.5.dp, Color(0xFF444444), RoundedCornerShape(2.dp))
  ) {
    Layout(
      content = {
        // Determine which digits to draw.
        // We draw 'wholePart' and '(wholePart + 1) % 10'
        // Actually, simply drawing the strip 0..9,0 is expensive?
        // Just draw the two relevant digits.

        val current = wholePart
        val next = (wholePart + 1) % 10

        DigitText(current)
        DigitText(next)
      }
    ) { measurables, constraints ->
      val placeables = measurables.map { it.measure(constraints) }
      val h = constraints.maxHeight

      layout(constraints.maxWidth, constraints.maxHeight) {
        // current digit is at -fraction * h
        // next digit is at (1 - fraction) * h ? No, just below it.
        val yOffset = -(fractionPart * h).toInt()

        placeables[0].place(0, yOffset)
        placeables[1].place(0, yOffset + h)
      }
    }
  }
}

@Composable
fun DigitText(digit: Int) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = digit.toString(),
      color = Color.White,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
  }
}
