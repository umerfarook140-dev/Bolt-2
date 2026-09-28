package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopGameHeader(
    levelNumber: Int,
    difficultyName: String,
    mistakesCount: Int,
    maxMistakes: Int = 3,
    coins: Int,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Level & Difficulty Info
        Column {
            Text(
                text = "LEVEL $levelNumber",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF334155))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = difficultyName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                )
            }
        }

        // Mistakes (Hearts) indicator
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("mistakes_indicator"),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until maxMistakes) {
                val isHeartLost = i < mistakesCount
                Icon(
                    imageVector = if (!isHeartLost) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (!isHeartLost) "Heart Available" else "Heart Lost",
                    tint = if (!isHeartLost) Color(0xFFEF4444) else Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Coins and Pause button
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coins badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF854D0E), Color(0xFFCA8A04))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Coins",
                    tint = Color(0xFFFEF08A),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // Pause button
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF475569), CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause Game",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
