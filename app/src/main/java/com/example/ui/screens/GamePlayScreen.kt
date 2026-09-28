package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.ScreenState
import com.example.ui.components.BoosterControls
import com.example.ui.components.FailMistakesDialog
import com.example.ui.components.PauseDialog
import com.example.ui.components.PuzzleCanvas
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ToolboxBar
import com.example.ui.components.TopGameHeader
import com.example.ui.components.VictoryDialog
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun GamePlayScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val levelData = uiState.levelData

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF131722),
                        Color(0xFF090D16)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Game Header
            TopGameHeader(
                levelNumber = uiState.currentLevelNumber,
                difficultyName = levelData?.difficultyName ?: "Tutorial",
                mistakesCount = uiState.mistakesCount,
                coins = uiState.coins,
                onPauseClick = { viewModel.togglePause(true) }
            )

            // 2. Exactly 4 Toolboxes at top
            ToolboxBar(
                toolboxes = uiState.toolboxes,
                tempLockerScrew = uiState.tempLockerScrew
            )

            // 3. Central 3D Puzzle Board Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                PuzzleCanvas(
                    pieces = uiState.pieces,
                    screws = uiState.screws,
                    flyingScrews = uiState.flyingScrews,
                    particles = uiState.particles,
                    onScrewTap = { screw -> viewModel.onScrewTapped(screw) },
                    onPanelTap = { piece -> viewModel.onPanelTapped(piece) },
                    onFrameUpdate = { dt -> viewModel.onFrameUpdate(dt) }
                )
            }

            // 4. Bottom Booster Bar
            BoosterControls(
                hammerCount = uiState.hammerCount,
                magnetCount = uiState.magnetCount,
                undoCount = uiState.undoCount,
                extraSlotCount = uiState.extraSlotCount,
                isHammerActive = uiState.isHammerActive,
                onBoosterClick = { booster -> viewModel.onBoosterClicked(booster) }
            )
        }

        // Overlay Dialogs
        if (uiState.isVictory) {
            VictoryDialog(
                levelNumber = uiState.currentLevelNumber,
                stars = uiState.victoryStars,
                rewardCoins = uiState.victoryCoinsReward,
                onNextLevel = { viewModel.nextLevel() },
                onReplay = { viewModel.restartCurrentLevel() },
                onHome = { viewModel.navigateTo(ScreenState.HOME) }
            )
        }

        if (uiState.isFailed) {
            FailMistakesDialog(
                onContinueRewardedAd = { viewModel.continueWithRewardedAd(context) },
                onReplay = { viewModel.restartCurrentLevel() },
                onHome = { viewModel.navigateTo(ScreenState.HOME) }
            )
        }

        if (uiState.isPaused) {
            PauseDialog(
                onResume = { viewModel.togglePause(false) },
                onRestart = {
                    viewModel.togglePause(false)
                    viewModel.restartCurrentLevel()
                },
                onSettings = {
                    viewModel.toggleSettings(true)
                },
                onLevelSelect = {
                    viewModel.togglePause(false)
                    viewModel.navigateTo(ScreenState.LEVEL_SELECT)
                }
            )
        }

        if (uiState.isSettingsOpen) {
            SettingsDialog(
                isSoundEnabled = uiState.isSoundEnabled,
                isMusicEnabled = uiState.isMusicEnabled,
                onSoundToggle = { viewModel.setSoundEnabled(it) },
                onMusicToggle = { viewModel.setMusicEnabled(it) },
                onClose = { viewModel.toggleSettings(false) }
            )
        }
    }
}
