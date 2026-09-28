package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoosterType

@Composable
fun BoosterControls(
    hammerCount: Int,
    magnetCount: Int,
    undoCount: Int,
    extraSlotCount: Int,
    isHammerActive: Boolean,
    onBoosterClick: (BoosterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo Booster
        BoosterButton(
            boosterType = BoosterType.UNDO,
            icon = Icons.Default.Undo,
            count = undoCount,
            isSelected = false,
            primaryColor = Color(0xFF38BDF8),
            onClick = { onBoosterClick(BoosterType.UNDO) }
        )

        // Hammer Booster
        BoosterButton(
            boosterType = BoosterType.HAMMER,
            icon = Icons.Default.Build,
            count = hammerCount,
            isSelected = isHammerActive,
            primaryColor = Color(0xFFF97316),
            onClick = { onBoosterClick(BoosterType.HAMMER) }
        )

        // Magnet Booster
        BoosterButton(
            boosterType = BoosterType.MAGNET,
            icon = Icons.Default.FlashOn,
            count = magnetCount,
            isSelected = false,
            primaryColor = Color(0xFFA855F7),
            onClick = { onBoosterClick(BoosterType.MAGNET) }
        )

        // Extra Slot Booster
        BoosterButton(
            boosterType = BoosterType.EXTRA_SLOT,
            icon = Icons.Default.Refresh,
            count = extraSlotCount,
            isSelected = false,
            primaryColor = Color(0xFF10B981),
            onClick = { onBoosterClick(BoosterType.EXTRA_SLOT) }
        )
    }
}

@Composable
fun BoosterButton(
    boosterType: BoosterType,
    icon: ImageVector,
    count: Int,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = if (count > 0) primaryColor else Color(0xFF64748B),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = if (count > 0) "$count" else "+",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(elevation = if (isSelected) 8.dp else 3.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            Brush.radialGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.5f)))
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.2.dp,
                        color = if (isSelected) Color.White else primaryColor.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onClick)
                    .testTag("booster_${boosterType.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = boosterType.title,
                    tint = if (isSelected) Color.White else primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = boosterType.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) primaryColor else Color.LightGray
            )
        )
    }
}
