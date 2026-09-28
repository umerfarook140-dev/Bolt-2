package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameColor
import com.example.model.Toolbox

@Composable
fun ToolboxBar(
    toolboxes: List<Toolbox>,
    tempLockerScrew: GameColor?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row containing exactly 4 toolboxes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("top_toolboxes_row"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (box in toolboxes) {
                ToolboxItem(
                    toolbox = box,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Temporary slot indicator if player unlocked extra slot booster
        if (tempLockerScrew != null) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Temp Pocket:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(tempLockerScrew.primaryColor)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
fun ToolboxItem(
    toolbox: Toolbox,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boxScale"
    )

    val boxColor = toolbox.color
    val isLocked = toolbox.isLocked
    val isComplete = toolbox.currentCount >= toolbox.capacity

    val containerBrush = if (isLocked) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF2B2D42), Color(0xFF1B1D28))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                boxColor.primaryColor.copy(alpha = 0.28f),
                boxColor.darkColor.copy(alpha = 0.45f),
                Color(0xFF111420)
            )
        )
    }

    val borderBrush = if (isLocked) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF4B5563), Color(0xFF374151))
        )
    } else if (isComplete) {
        Brush.sweepGradient(
            listOf(Color.White, boxColor.lightColor, Color.Yellow, Color.White)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(boxColor.lightColor.copy(alpha = 0.8f), boxColor.darkColor)
        )
    }

    Box(
        modifier = modifier
            .height(64.dp)
            .scale(if (isComplete) pulseScale else 1f)
            .shadow(
                elevation = if (!isLocked) 4.dp else 1.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(containerBrush)
            .border(
                width = if (isComplete) 2.dp else 1.2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("toolbox_${toolbox.index}"),
        contentAlignment = Alignment.Center
    ) {
        if (isLocked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Toolbox",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "LOCKED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                // Header badge showing color name
                Text(
                    text = boxColor.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = boxColor.lightColor,
                        letterSpacing = 0.5.sp
                    )
                )

                // 3 screw slots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (slotIdx in 0 until 3) {
                        val isFilled = slotIdx < toolbox.currentCount
                        ScrewSlotItem(
                            isFilled = isFilled,
                            color = boxColor
                        )
                    }
                }

                // Completion status or slot counter
                if (isComplete) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = Color.Green,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "DONE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green
                            )
                        )
                    }
                } else {
                    Text(
                        text = "${toolbox.currentCount}/3",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ScrewSlotItem(
    isFilled: Boolean,
    color: GameColor,
    modifier: Modifier = Modifier
) {
    if (isFilled) {
        // Metallic 3D screw head filled in slot
        Box(
            modifier = modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.metalSpecular,
                            color.primaryColor,
                            color.darkColor
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Hexagon or slot indent inside filled screw
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
            )
        }
    } else {
        // Empty recessed socket
        Box(
            modifier = modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(1.dp, color.primaryColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }
    }
}
