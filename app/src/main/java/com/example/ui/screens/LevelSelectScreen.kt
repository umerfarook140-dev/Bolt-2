package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelRecordEntity
import kotlinx.coroutines.launch

data class TierRange(val title: String, val startLevel: Int, val endLevel: Int)

@Composable
fun LevelSelectScreen(
    highestUnlockedLevel: Int,
    levelRecords: List<LevelRecordEntity>,
    onLevelSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierRanges = listOf(
        TierRange("Tutorial 1-10", 1, 10),
        TierRange("Easy 11-30", 11, 30),
        TierRange("Medium 31-60", 31, 60),
        TierRange("Advanced 61-100", 61, 100),
        TierRange("Hard 101-200", 101, 200),
        TierRange("Very Hard 201-300", 201, 300),
        TierRange("Expert 301-400", 301, 400),
        TierRange("Master 401-500", 401, 500)
    )

    var selectedTier by remember { mutableStateOf(tierRanges[0]) }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val recordsMap = remember(levelRecords) {
        levelRecords.associateBy { it.levelNumber }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0B0E14)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), CircleShape)
                        .testTag("level_select_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "SELECT LEVEL",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )

                // Placeholder for balance
                Spacer(modifier = Modifier.size(46.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tier selector tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tierRanges) { tier ->
                    val isSelected = tier == selectedTier
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.White else Color(0xFF475569),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTier = tier
                                scope.launch {
                                    gridState.scrollToItem(tier.startLevel - 1)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tier.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF0F172A) else Color.LightGray
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 500 Level Nodes Grid (4 columns)
            val allLevelNumbers = (1..500).toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("level_select_grid")
            ) {
                items(allLevelNumbers) { levelNum ->
                    val isUnlocked = levelNum <= highestUnlockedLevel
                    val record = recordsMap[levelNum]
                    val stars = record?.stars ?: 0

                    LevelCard(
                        levelNumber = levelNum,
                        isUnlocked = isUnlocked,
                        stars = stars,
                        onClick = {
                            if (isUnlocked) {
                                onLevelSelected(levelNum)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelCard(
    levelNumber: Int,
    isUnlocked: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isUnlocked) {
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0xFF161922), Color(0xFF0B0D12))
                    )
                }
            )
            .border(
                width = if (isUnlocked) 1.2.dp else 0.8.dp,
                color = if (isUnlocked) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color(0xFF334155),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_node_$levelNumber"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = "$levelNumber",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Stars row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isEarned = i <= stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isEarned) Color(0xFFFBBF24) else Color(0xFF475569),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$levelNumber",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
