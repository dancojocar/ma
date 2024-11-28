package com.example.cardanimation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.cardanimation.ui.theme.CardAnimationTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      CardAnimationTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          AnimatedCards()
        }
      }
    }
  }
}


@Preview
@Composable
fun AnimatedCards() {
  var colors by remember {
    mutableStateOf(
      listOf(
        Color(0xff4285f4),
        Color(0xff34a853),
        Color(0xfffbbc05),
        Color(0xffea4335),
      ).reversed()
    )
  }

  Box(
    Modifier
      .background(Color.Black)
      .padding(vertical = 32.dp)
      .fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    colors.forEachIndexed { idx, color ->
      key(color) {
        AnimatedCard(order = idx,
          totalCount = colors.size,
          backgroundColor = color,
          onMoveToBack = {
            colors = listOf(color) + (colors - color)
          })
      }
    }
  }
}

@Composable
fun AnimatedCard(
  order: Int,
  totalCount: Int,
  backgroundColor: Color = Color.White,
  onMoveToBack: () -> Unit
) {
  val animatedScale by animateFloatAsState(
    targetValue = 1f - (totalCount - order) * 0.05f, label = "",
  )
  val animatedYOffset by animateDpAsState(
    targetValue = ((totalCount - order) * -20).dp, label = "",
  )

  Box(
    modifier = Modifier
      .offset { IntOffset(x = 0, y = animatedYOffset.roundToPx()) }
      .graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
      }
      .swipeToBack { onMoveToBack() }
  ) {
    SampleCard(backgroundColor = backgroundColor)
  }
}

@Composable
fun SampleCard(backgroundColor: Color = Color.White) {
  Card(
    modifier = Modifier
      .height(220.dp)
      .fillMaxWidth(.8f),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .padding(vertical = 24.dp, horizontal = 32.dp),
      verticalArrangement = Arrangement.Bottom
    ) {
      Row(
        Modifier.fillMaxWidth(0.5f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          Modifier
            .size(36.dp)
            .pillShape()
        )
        Spacer(Modifier.width(8.dp))
        Column {
          Box(
            Modifier
              .height(12.dp)
              .fillMaxWidth()
              .pillShape()
          )
          Spacer(Modifier.height(6.dp))
          Box(
            Modifier
              .height(12.dp)
              .fillMaxWidth(0.6f)
              .pillShape()
          )
        }
      }
    }
  }
}

fun Modifier.pillShape() =
  this.then(
    background(Color.Black.copy(0.4f), CircleShape)
  )

fun Modifier.swipeToBack(
  onMoveToBack: () -> Unit
): Modifier = composed {
  val offsetY = remember { Animatable(0f) }
  val rotation = remember { Animatable(0f) }
  var leftSide by remember { mutableStateOf(true) }
  var clearedHurdle by remember { mutableStateOf(false) }

  pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)

    coroutineScope {
      while(true) {
        offsetY.stop()
        val velocityTracker = VelocityTracker()

        awaitPointerEventScope {
          verticalDrag(awaitFirstDown().id) { change ->
            val verticalDragOffset = offsetY.value + change.positionChange().y
            val horizontalPosition = change.previousPosition.x

            leftSide = horizontalPosition <= size.width / 2
            val offsetXRatioFromMiddle = if (leftSide) {
              horizontalPosition / (size.width / 2)
            } else {
              (size.width - horizontalPosition) / (size.width / 2)
            }
            val rotationalOffset = max(1f, (1f - offsetXRatioFromMiddle) * 4f)

            launch {
              offsetY.snapTo(verticalDragOffset)
              rotation.snapTo(if (leftSide) rotationalOffset else -rotationalOffset)
            }

            velocityTracker.addPosition(change.uptimeMillis, change.position)
            if (change.positionChange() != Offset.Zero) change.consume()
          }
        }

        val velocity = velocityTracker.calculateVelocity().y
        val targetOffsetY = decay.calculateTargetValue(offsetY.value, velocity)

        if (targetOffsetY.absoluteValue <= size.height) {
          // Not enough velocity; Reset.
          launch { offsetY.animateTo(targetValue = 0f, initialVelocity = velocity) }
          launch { rotation.animateTo(targetValue = 0f, initialVelocity = velocity) }
        } else {
          // Enough velocity to fling the card to the back
          val boomerangDuration = 1600
          val maxDistanceToFling = (size.height * 2).toFloat()
          val maxRotations = 1
          val easeInOutEasing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)

          val distanceToFling = min(
            targetOffsetY.absoluteValue + size.height, maxDistanceToFling
          )
          val rotationToFling = min(
            360f * (targetOffsetY.absoluteValue / size.height).roundToInt(),
            360f * maxRotations
          )
          val rotationOvershoot = rotationToFling + 12f

          val animationJobs = listOf(
            launch {
              rotation.animateTo(targetValue = if (leftSide) rotationToFling else -rotationToFling,
                initialVelocity = velocity,
                animationSpec = keyframes {
                  durationMillis = boomerangDuration
                  0f at 0 using easeInOutEasing
                  (if (leftSide) rotationOvershoot else -rotationOvershoot) at boomerangDuration - 50 using LinearOutSlowInEasing
                  (if (leftSide) rotationToFling else -rotationToFling) at boomerangDuration
                })
              rotation.snapTo(0f)
            },
            launch {
              offsetY.animateTo(targetValue = 0f,
                initialVelocity = velocity,
                animationSpec = keyframes {
                  durationMillis = boomerangDuration
                  -distanceToFling at (boomerangDuration / 2) using easeInOutEasing
                  40f at boomerangDuration - 70
                }
              ) {
                if (value <= -size.height * 2 && !clearedHurdle) {
                  onMoveToBack()
                  clearedHurdle = true
                }
              }
            }
          )
          animationJobs.joinAll()
          clearedHurdle = false
        }
      }
    }
  }
    .offset { IntOffset(0, offsetY.value.roundToInt()) }
    .graphicsLayer {
      transformOrigin = TransformOrigin.Center
      rotationZ = rotation.value
    }
}