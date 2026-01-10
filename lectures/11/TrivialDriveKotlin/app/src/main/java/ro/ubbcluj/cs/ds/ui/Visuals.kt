package ro.ubbcluj.cs.ds.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box


import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background

@Composable
fun Vehicle(
    gasLevel: Int,
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) {
        // Real Premium Car Image (Top Down)
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = ro.ubbcluj.cs.ds.R.drawable.premium_car),
            contentDescription = "Premium Car",
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    } else {
        // Real Standard Car Image (Top Down)
        // Dynamic tint based on gas level
        val tintColor = when {
            gasLevel <= 1 -> androidx.compose.ui.graphics.Color(0xFFFF5252) // Red tint for low gas
            gasLevel <= 2 -> androidx.compose.ui.graphics.Color(0xFFFFD740) // Yellow tint
            else -> null // No tint (original red color)
        }
        
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = ro.ubbcluj.cs.ds.R.drawable.standard_car),
            contentDescription = "Standard Car",
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            colorFilter = tintColor?.let { androidx.compose.ui.graphics.ColorFilter.tint(it, androidx.compose.ui.graphics.BlendMode.Modulate) }
        )
    }
}

@Composable
fun GasGauge(
    gasLevel: Int,
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) {
        PremiumGasGauge(modifier = modifier)
    } else {
        StandardGasGauge(gasLevel = gasLevel, modifier = modifier)
    }
}

@Composable
fun StandardGasGauge(
    gasLevel: Int,
    modifier: Modifier = Modifier
) {
    val maxGas = 4
    val isInfinite = gasLevel > maxGas
    val visualGasLevel = if (isInfinite) maxGas else gasLevel
    val percentage = visualGasLevel.toFloat() / maxGas.toFloat()
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20f
            val radius = size.minDimension / 2 - strokeWidth
            
            // Background Arc
            drawArc(
                color = Color.LightGray,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Foreground Arc
            val color = when {
                percentage < 0.25f -> Color.Red
                percentage < 0.5f -> Color.Yellow
                else -> Color.Green
            }
            
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f * percentage, // Cap at 180 degrees
                useCenter = false,
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = if (isInfinite) "\u221E" else "$gasLevel/$maxGas",
            style = if (isInfinite) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp)
        )
    }
}

@Composable
fun PremiumGasGauge(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gauge_glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 30f
            val radius = size.minDimension / 2 - strokeWidth
            
            // Golden Gradient Arc
            val brush = Brush.sweepGradient(
                colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00), Color(0xFFFFD700))
            )

            drawArc(
                brush = brush,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            
            // Inner Glow
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.1f * alpha),
                radius = radius * 0.8f
            )
        }
        Text(
            text = "\u221E", // Infinity symbol
            style = MaterialTheme.typography.displayLarge,
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DrivingOverlay(
    isPremium: Boolean,
    currentOdometer: Int,
    onAnimationFinished: () -> Unit
) {
    // Phase 1: Entrance (Center -> Start of Road)
    // Phase 2: Driving (Start of Road -> End of Road)
    
    val entranceDuration = 1000
    val driveDuration = 8000
    
    val infiniteTransition = rememberInfiniteTransition(label = "drive_anim")
    
    // We use a separate state to track entrance vs driving
    // But to keep it simple with existing structure, we can just delay the drive animation start
    // and have an entrance animation running before it.
    
    var animationPhase by remember { androidx.compose.runtime.mutableStateOf(AnimationPhase.ENTRANCE) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(entranceDuration.toLong())
        animationPhase = AnimationPhase.DRIVING
        kotlinx.coroutines.delay(driveDuration.toLong())
        onAnimationFinished()
    }
    
    // Entrance Animation (Center to Path Start)
    val entranceTransition = androidx.compose.animation.core.updateTransition(targetState = animationPhase, label = "entrance")
    val entranceProgress by entranceTransition.animateFloat(
        label = "entrance_progress",
        transitionSpec = { tween(durationMillis = entranceDuration, easing = LinearEasing) }
    ) { state ->
        if (state == AnimationPhase.ENTRANCE) 0f else 1f
    }

    // Drive Animation (Path)
    val driveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(driveDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drive_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        androidx.compose.foundation.Image(
             painter = androidx.compose.ui.res.painterResource(id = ro.ubbcluj.cs.ds.R.drawable.road_map),
             contentDescription = "Map",
             modifier = Modifier.fillMaxSize(),
             contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        val density = androidx.compose.ui.platform.LocalDensity.current
        var boxSize by remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { boxSize = it }
        ) {
            if (boxSize != androidx.compose.ui.unit.IntSize.Zero) {
                val width = boxSize.width.toFloat()
                val height = boxSize.height.toFloat()
                
                // Path Definition (Pixels)
                val path = remember(width, height) {
                    androidx.compose.ui.graphics.Path().apply {
                        moveTo(width * 0.70f, height * 1.05f) // Start Point
                        cubicTo(width * 0.70f, height * 0.95f, width * 0.50f, height * 0.80f, width * 0.15f, height * 0.72f)
                        cubicTo(width * 0.05f, height * 0.65f, width * 0.50f, height * 0.55f, width * 0.88f, height * 0.48f)
                        cubicTo(width * 1.0f, height * 0.42f, width * 0.60f, height * 0.35f, width * 0.30f, height * 0.25f)
                        cubicTo(width * 0.15f, height * 0.15f, width * 0.50f, height * 0.05f, width * 0.70f, -height * 0.10f)
                    }
                }
                
                // Calculate Path Start Position (Pixels)
                val startX = width * 0.70f
                val startY = height * 1.05f
                val centerX = width * 0.5f 
                val centerY = height * 0.5f
                
                // Determine Car Position based on Phase
                var currentX = 0f
                var currentY = 0f
                var currentRotation = 0f
                
                if (animationPhase == AnimationPhase.ENTRANCE) {
                    val anim = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        anim.animateTo(1f, animationSpec = tween(entranceDuration, easing = FastOutSlowInEasing))
                    }
                    
                    val t = anim.value
                    currentX = centerX + (startX - centerX) * t 
                    currentY = centerY + (startY - centerY) * t 
                    
                    // Calculate angle to start point
                    val dx = startX - centerX
                    val dy = startY - centerY
                    currentRotation = (Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))).toFloat() + 90f
                    
                } else {
                    // Driving Phase
                    val pathMeasure = remember(path) { androidx.compose.ui.graphics.PathMeasure() }
                    pathMeasure.setPath(path, false)
                    
                    val pathLength = pathMeasure.length
                    val distance = driveProgress * pathLength
                    val position = pathMeasure.getPosition(distance)
                    val tangent = pathMeasure.getTangent(distance)
                    
                    currentX = position.x
                    currentY = position.y
                    
                    val degrees = (Math.toDegrees(Math.atan2(tangent.y.toDouble(), tangent.x.toDouble()))).toFloat()
                    currentRotation = degrees + 90f
                }

                // Convert pixels to Dp for offset
                val xDp = with(density) { currentX.toDp() }
                val yDp = with(density) { currentY.toDp() }

                 Vehicle(
                    gasLevel = 4,
                    isPremium = isPremium,
                    modifier = Modifier
                        .offset(x = xDp - 30.dp, y = yDp - 30.dp)
                        .rotate(if (!isPremium) currentRotation + 180f else currentRotation) // Fix standard car rotation
                        .size(60.dp)
                )
            }
        }
        
        Text(
            text = if (animationPhase == AnimationPhase.ENTRANCE) "Heading to road..." else "Driving...",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp)))
    }

    // Odometer Overlay (Bottom Center/Right)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
         val increment = if (isPremium) 20 else 10
         // Only animate odometer during driving phase (after entrance)
         val progress = if (animationPhase == AnimationPhase.DRIVING) driveProgress else 0f
         val liveOdometer = currentOdometer + (progress * increment).toDouble()
         
         OdometerDisplay(
            value = liveOdometer,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
         )
    }
}

@Composable
fun ManualDrivingOverlay(
    isPremium: Boolean,
    currentOdometer: Int,
    timeRemaining: Int,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Map Background
        androidx.compose.foundation.Image(
             painter = androidx.compose.ui.res.painterResource(id = ro.ubbcluj.cs.ds.R.drawable.road_map),
             contentDescription = "Map",
             modifier = Modifier.fillMaxSize(),
             contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        // Physics State
        var carPosition by remember { mutableStateOf(Offset.Zero) }
        var velocity by remember { mutableStateOf(Offset.Zero) }
        var rotation by remember { mutableStateOf(0f) }
        var hasInitialized by remember { mutableStateOf(false) }
        
        // Initialize center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    if (!hasInitialized && size.width > 0) {
                        carPosition = Offset(size.width / 2f, size.height / 2f)
                        hasInitialized = true
                    }
                }
        )
        
        // Game Loop for Physics (approx 60fps)
        LaunchedEffect(hasInitialized) {
            if (hasInitialized) {
                 while (true) {
                    // Update Position
                    carPosition += velocity
                    
                    // Simple friction
                    velocity *= 0.95f
                    
                    // Update rotation if moving
                    if (velocity.getDistance() > 0.5f) {
                        val angle = Math.toDegrees(Math.atan2(velocity.y.toDouble(), velocity.x.toDouble())).toFloat()
                        rotation = angle + 90f
                    }
                    
                    withFrameNanos { _ -> }
                 }
            }
        }

        // Render Car
        if (hasInitialized) {
             val density = androidx.compose.ui.platform.LocalDensity.current
             val xDp = with(density) { carPosition.x.toDp() }
             val yDp = with(density) { carPosition.y.toDp() }
             
             Vehicle(
                gasLevel = 4,
                isPremium = isPremium,
                modifier = Modifier
                    .offset(x = xDp - 30.dp, y = yDp - 30.dp)
                    .rotate(if (!isPremium) rotation + 180f else rotation)
                    .size(60.dp)
            )
        }
        
        // HUD: Timer
        Text(
            text = "Time: ${timeRemaining}s",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .background(Color.Red.copy(alpha = 0.8f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(16.dp)
        )
        
        // HUD: Odometer (Static or incrementing based on movement?)
        // Let's keep it static + small increment for simplicity or re-use logic
        // For manual, we'll just show current odometer to keep it simple as requested "drive anywhere"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
             OdometerDisplay(
                value = currentOdometer.toDouble(),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
             )
        }

        // Joystick
        Joystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
                .size(150.dp),
            onMoved = { x, y ->
                // Update velocity based on joystick
                // Max speed 10
                velocity += Offset(x, y) * 2f
                // Cap max speed
                val speed = velocity.getDistance()
                if (speed > 15f) {
                    velocity = (velocity / speed) * 15f
                }
            }
        )
    }
}

enum class AnimationPhase {
    ENTRANCE,
    DRIVING
}
