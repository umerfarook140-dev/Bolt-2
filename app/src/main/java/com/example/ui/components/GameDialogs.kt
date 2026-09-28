package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun VictoryDialog(
    levelNumber: Int,
    stars: Int,
    rewardCoins: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300)) + scaleIn(tween(350))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(24.dp))
                    .padding(24.dp)
                    .testTag("victory_dialog"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "LEVEL COMPLETE!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    // 3 Stars display
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val isEarned = i <= stars
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (isEarned) Color(0xFFFBBF24) else Color(0xFF475569),
                                modifier = Modifier.size(if (i == 2) 48.dp else 36.dp)
                            )
                        }
                    }

                    // Reward badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF334155))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Reward:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Reward",
                            tint = Color(0xFFFCD34D),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+$rewardCoins Coins",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF08A)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("next_level_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEXT LEVEL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReplay,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("replay_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replay", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onHome,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B))
                        ) {
                            Text("Menu", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FailMistakesDialog(
    onContinueRewardedAd: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2C1517), Color(0xFF140A0C))
                    )
                )
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("fail_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "OUT OF HEARTS!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEF4444),
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "You made 3 mistakes on blocked screws. Watch a short test ad to restore hearts and keep your current puzzle progress!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // REWARDED AD BUTTON APPEARS FIRST (AS REQUIRED)
                Button(
                    onClick = onContinueRewardedAd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("rewarded_ad_continue_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONTINUE (TEST AD)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                }

                // REPLAY LEVEL BUTTON
                Button(
                    onClick = onReplay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("fail_replay_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Replay Level From Start", color = Color.White)
                }

                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Text("Main Menu", color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSettings: () -> Unit,
    onLevelSelect: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("pause_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("resume_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("RESUME", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restart Level", color = Color.White)
                }

                Button(
                    onClick = onLevelSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("levels_map_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Text("Level Select", color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onMusicToggle: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                // Sound FX switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                        Text("Sound Effects", color = Color.White)
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = onSoundToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF38BDF8),
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }

                // Music FX switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isMusicEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                        Text("Ambient Audio", color = Color.White)
                    }
                    Switch(
                        checked = isMusicEnabled,
                        onCheckedChange = onMusicToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF10B981),
                            checkedTrackColor = Color(0xFF059669)
                        )
                    )
                }

                Text(
                    text = "Bolt Drop: Screw Master v1.0\nGuaranteed Solvable Mechanics • 500 Progressive Stages",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}
