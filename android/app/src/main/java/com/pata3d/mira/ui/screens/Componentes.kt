package com.pata3d.mira.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pata3d.mira.ui.theme.GlowGradient
import com.pata3d.mira.ui.theme.NeonOrange
import com.pata3d.mira.ui.theme.NeonPinkSoft
import com.pata3d.mira.ui.theme.NeonPurple
import com.pata3d.mira.ui.theme.PrioAlta
import com.pata3d.mira.ui.theme.PrioBaixa
import com.pata3d.mira.ui.theme.PrioMedia
import java.util.Calendar

@Composable
fun ChipPrioridade(prioridade: String) {
    val (bg, fg, ponto) = when (prioridade) {
        "Alta" -> Triple(Color(0x33FF5C6B), PrioAlta, PrioAlta)
        "Média", "Media" -> Triple(Color(0x30FF9A1F), PrioMedia, NeonOrange)
        else -> Triple(Color(0x26B14CFF), PrioBaixa, NeonPurple)
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(10.dp)
                    .background(ponto, CircleShape)
            )
            Text(prioridade.uppercase(), style = MaterialTheme.typography.labelMedium, color = fg)
        }
    }
}

@Composable
fun ChipInfo(texto: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0x221D0B2A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x552F123D))
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = NeonPinkSoft,
        )
    }
}

@Composable
fun MiraGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(6.dp)
                .blur(26.dp)
                .background(Color(0x26FF39C8), RoundedCornerShape(28.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xC012071C), RoundedCornerShape(28.dp))
                .border(1.dp, Color(0xAA79208D), RoundedCornerShape(28.dp))
        ) {
            content()
        }
    }
}

@Composable
fun GlowPrimaryButton(
    texto: String,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "glow")
    val alpha = pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha"
    )
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(alpha.value)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .blur(20.dp)
                .background(GlowGradient, RoundedCornerShape(24.dp))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(GlowGradient, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x80FFD17E), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(texto, style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
    }
}

fun horaFormatada(minutosDoDia: Int): String =
    "%02d:%02d".format(minutosDoDia / 60, minutosDoDia % 60)

fun horaDeMillis(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}

fun ehDiaInteiro(millis: Long): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0
}

private val meses = listOf(
    "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
)
private val diasSemana = listOf("Domingo", "Segunda", "Terca", "Quarta", "Quinta", "Sexta", "Sabado")

fun nomeMes(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "${meses[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
}

fun rotuloDia(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "${diasSemana[cal.get(Calendar.DAY_OF_WEEK) - 1]}, ${cal.get(Calendar.DAY_OF_MONTH)}"
}
