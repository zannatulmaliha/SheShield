package com.example.sheshield.SOS

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SosCountDown(
    seconds: Int = 5,
    onFinish: () -> Unit,
    onCancel: () -> Unit
) {
    var secondsLeft by remember { mutableStateOf(seconds) }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        } else {
            onFinish()
        }
    }

    // Pulse animation for the main circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Ripple effect animation
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val particlePositions = listOf(
                Offset(size.width * 0.15f, size.height * 0.2f),
                Offset(size.width * 0.85f, size.height * 0.25f),
                Offset(size.width * 0.1f, size.height * 0.7f),
                Offset(size.width * 0.9f, size.height * 0.75f),
                Offset(size.width * 0.25f, size.height * 0.15f),
                Offset(size.width * 0.75f, size.height * 0.85f)
            )

            particlePositions.forEach { pos ->
                drawCircle(
                    color = Color.Red.copy(alpha = 0.1f),
                    radius = 3f,
                    center = pos
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Ripple circles behind main countdown
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Outer ripple
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .scale(rippleScale)
                        .alpha(rippleAlpha)
                        .border(3.dp, Color.Red.copy(alpha = 0.3f), CircleShape)
                )

                // Glow effect
                Canvas(modifier = Modifier.size(300.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Red.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.width / 2
                        )
                    )
                }


                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .scale(pulseScale)
                        .background(
                            Color(0xFFFF1111),
                            CircleShape
                        )
                        .border(
                            width = 6.dp,
                            color = Color(0xFFFF9500),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = secondsLeft.toString(),
                            fontSize = 86.sp,

                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "seconds",
                            fontSize = 12.sp,
                            color = Color.White,

                            letterSpacing = 1.sp
                        )
                    }
                }
            }





            Text(
                text = "Emergency contacts will be notified",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )


            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                shape = RoundedCornerShape(50.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF404040),
                        shape = RoundedCornerShape(50.dp)
                    )
            ) {
                Text(
                    text = "Cancel SOS",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}