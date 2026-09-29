package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * High-fidelity atmospheric backdrop with subtle ambient lighting,
 * smooth depth gradients, and soft glowing accents.
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Subtle pulsating glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF070A12), // Deepest obsidian indigo
                            Color(0xFF0D1220), // Rich midnight slate
                            Color(0xFF090D18)  // Atmospheric deep navy
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFEEF2FF),
                            Color(0xFFF1F5F9)
                        )
                    )
                }
            )
    ) {
        // Atmospheric Canvas ambient glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (isDarkMode) {
                // Top-right Indigo/Violet ambient aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryIndigo.copy(alpha = pulseAlpha),
                            Color(0xFF818CF8).copy(alpha = pulseAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.85f, height * 0.12f),
                        radius = width * 0.65f
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.65f
                )

                // Bottom-left warm Amber/Gold coin ambient aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CoinGold.copy(alpha = pulseAlpha * 0.6f),
                            CoinGoldDark.copy(alpha = pulseAlpha * 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.1f, height * 0.75f),
                        radius = width * 0.55f
                    ),
                    center = Offset(width * 0.1f, height * 0.75f),
                    radius = width * 0.55f
                )

                // Center subtle Emerald prosperity accent aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldSuccess.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.5f, height * 0.45f),
                        radius = width * 0.45f
                    ),
                    center = Offset(width * 0.5f, height * 0.45f),
                    radius = width * 0.45f
                )
            } else {
                // Light mode gentle indigo top aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryIndigoLight.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.85f, height * 0.1f),
                        radius = width * 0.6f
                    ),
                    center = Offset(width * 0.85f, height * 0.1f),
                    radius = width * 0.6f
                )

                // Light mode warm gold accent aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CoinGoldLight.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.15f, height * 0.6f),
                        radius = width * 0.5f
                    ),
                    center = Offset(width * 0.15f, height * 0.6f),
                    radius = width * 0.5f
                )
            }
        }

        // Screen content rendered above atmospheric backdrop
        content()
    }
}
