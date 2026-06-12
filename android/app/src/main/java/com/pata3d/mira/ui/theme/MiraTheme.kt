package com.pata3d.mira.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BgTop = Color(0xFF0B0416)
val BgBottom = Color(0xFF160222)
val BgPanel = Color(0xFF14071F)
val BgPanelSoft = Color(0xFF1B0A29)
val BgStroke = Color(0xFF6F1A7F)
val BgStrokeSoft = Color(0xFF4A1555)

val NeonPink = Color(0xFFFF39C8)
val NeonPinkSoft = Color(0xFFFF72DA)
val NeonPurple = Color(0xFFB14CFF)
val NeonOrange = Color(0xFFFF9A1F)
val NeonAmber = Color(0xFFFFB11A)
val NeonRed = Color(0xFFFF5C6B)

val TextPrimary = Color(0xFFF8F2FF)
val TextSecondary = Color(0xFFC89AD9)
val TextMuted = Color(0xFF9A78AA)

val PrioAlta = NeonRed
val PrioMedia = NeonAmber
val PrioBaixa = NeonPurple
val Success = NeonPink
val ActionGreen = Color(0xFF41D3A2)
val WarnOrange = NeonOrange
val AccentBlue = NeonPurple
val AccentCyan = NeonPinkSoft
val AccentPurple = NeonPurple

val GlowGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF0E9B), Color(0xFFFF5C4D), Color(0xFFFFA51F))
)
val PinkGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF168D), Color(0xFFD420FF))
)
val BackgroundGradient = Brush.verticalGradient(
    listOf(BgTop, BgBottom, Color(0xFF09020F))
)
val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x40FF38C7), Color(0x10FF38C7), Color(0x08FFFFFF)),
    start = Offset.Zero,
    end = Offset.Infinite,
)

private val MiraColors = darkColorScheme(
    primary = NeonPink,
    onPrimary = Color.White,
    primaryContainer = Color(0x33FF3BC8),
    onPrimaryContainer = TextPrimary,
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0x22B14CFF),
    onSecondaryContainer = TextPrimary,
    tertiary = NeonOrange,
    background = BgTop,
    onBackground = TextPrimary,
    surface = BgPanel,
    onSurface = TextPrimary,
    surfaceVariant = BgPanelSoft,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = Color(0xFF221030),
    outline = BgStroke,
    outlineVariant = BgStrokeSoft,
    error = NeonRed,
    onError = Color.White,
    errorContainer = Color(0x33FF5C6B),
    onErrorContainer = Color(0xFFFFB8C0),
)

private val MiraTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontSize = 58.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        shadow = Shadow(color = Color(0x40FF66CC), blurRadius = 18f),
    ),
    displayMedium = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleSmall = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
    bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal, color = TextPrimary),
    bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, color = TextPrimary),
    bodySmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, color = TextSecondary),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary),
)

@Composable
fun MiraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MiraColors,
        typography = MiraTypography,
        content = content,
    )
}

@Composable
fun MiraBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .blur(72.dp)
                .background(Color(0x14FF2AC5), CircleShape)
                .fillMaxSize(0.28f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .blur(88.dp)
                .background(Color(0x10B14CFF), CircleShape)
                .fillMaxSize(0.34f)
        )
        content()
    }
}

fun Modifier.miraCardShape(radius: Int = 26): Modifier =
    clip(RoundedCornerShape(radius.dp))
        .background(Color(0x700F0719))
        .border(1.dp, Color(0x88A722A7), RoundedCornerShape(radius.dp))

