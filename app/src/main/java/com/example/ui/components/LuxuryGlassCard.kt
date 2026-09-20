package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAccentColor

@Composable
fun LuxuryGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    hasGlow: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val accentColor = LocalAccentColor.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    val borderColor = if (hasGlow) {
        accentColor.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    }

    val gradientBorder = Brush.linearGradient(
        listOf(
            borderColor,
            if (hasGlow) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .border(borderWidth, gradientBorder, shape)
            .background(surfaceColor, shape)
            .then(clickModifier)
            .padding(16.dp),
        content = content
    )
}
