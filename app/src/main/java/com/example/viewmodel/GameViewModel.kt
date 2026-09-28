package com.example.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.data.GameProfileEntity
import com.example.data.GameRepository
import com.example.data.LevelRecordEntity
import com.example.engine.LevelGenerator
import com.example.engine.PhysicsSimulator
import com.example.engine.SoundEngine
import com.example.model.BoosterType
import com.example.model.FlyingScrew
import com.example.model.GameColor
import com.example.model.LevelData
import com.example.model.MoveHistory
import com.example.model.PuzzlePiece
import com.example.model.ScreenState
import com.example.model.Screw
import com.example.model.Toolbox
import com.example.model.VisualParticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

data class GameUiState(
    val screenState: ScreenState = ScreenState.HOME,
    val currentLevelNumber: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val coins: Int = 150,
    val hammerCount: Int = 3,
    val magnetCount: Int = 3,
    val undoCount: Int = 5,
    val extraSlotCount: Int = 2,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val levelData: LevelData? = null,
    val pieces: List<PuzzlePiece> = emptyList(),
    val screws: List<Screw> = emptyList(),
    val toolboxes: List<Toolbox> = emptyList(),
    val extraColorQueue: List<GameColor> = emptyList(),
    val flyingScrews: List<FlyingScrew> = emptyList(),
    val particles: List<VisualParticle> = emptyList(),
    val tempLockerScrew: GameColor? = null,
    val mistakesCount: Int = 0,
    val isHammerActive: Boolean = false,
    val isPaused: Boolean = false,
    val isVictory: Boolean = false,
    val isFailed: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val victoryStars: Int = 3,
    val victoryCoinsReward: Int = 50,
    val levelRecords: List<LevelRecordEntity> = emptyList(),
    val moveHistory: List<MoveHistory> = emptyList()
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    val soundEngine = SoundEngine()
    private val rng = Random()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // Observe game profile from Room
        viewModelScope.launch {
            repository.gameProfile.collect { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            currentLevelNumber = profile.currentLevel,
                            highestUnlockedLevel = profile.highestUnlockedLevel,
                            coins = profile.coins,
                            hammerCount = profile.hammerCount,
                            magnetCount = profile.magnetCount,
                            undoCount = profile.undoCount,
                            extraSlotCount = profile.extraSlotCount,
                            isSoundEnabled = profile.isSoundEnabled,
                            isMusicEnabled = profile.isMusicEnabled
                        )
                    }
                    soundEngine.setSoundEnabled(profile.isSoundEnabled)
                    soundEngine.setMusicEnabled(profile.isMusicEnabled)
                } else {
                    // Initialize default profile in DB
                    repository.saveProfile(GameProfileEntity())
                }
            }
        }

        // Observe completed levels records
        viewModelScope.launch {
            repository.levelRecords.collect { records ->
                _uiState.update { it.copy(levelRecords = records) }
            }
        }
    }

    fun navigateTo(screen: ScreenState) {
        soundEngine.playTap()
        _uiState.update { it.copy(screenState = screen) }
    }

    fun startLevel(levelNumber: Int) {
        val levelData = LevelGenerator.generateLevel(levelNumber)
        _uiState.update {
            it.copy(
                screenState = ScreenState.PLAYING,
                currentLevelNumber = levelNumber,
                levelData = levelData,
                pieces = levelData.pieces.map { p -> p.copy(supportingScrewIds = p.supportingScrewIds.toMutableSet()) },
                screws = levelData.screws.map { s -> s.copy() },
                toolboxes = levelData.initialToolboxes.map { b -> b.copy() },
                extraColorQueue = levelData.extraColorQueue,
                flyingScrews = emptyList(),
                particles = emptyList(),
                tempLockerScrew = null,
                mistakesCount = 0,
                isHammerActive = false,
                isPaused = false,
                isVictory = false,
                isFailed = false,
                moveHistory = emptyList()
            )
        }
    }

    fun restartCurrentLevel() {
        startLevel(_uiState.value.currentLevelNumber)
    }

    fun nextLevel() {
        val next = _uiState.value.currentLevelNumber + 1
        startLevel(next)
    }

    fun togglePause(paused: Boolean) {
        soundEngine.playTap()
        _uiState.update { it.copy(isPaused = paused) }
    }

    fun toggleSettings(open: Boolean) {
        soundEngine.playTap()
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEngine.setSoundEnabled(enabled)
        _uiState.update { it.copy(isSoundEnabled = enabled) }
        saveCurrentProfile()
    }

    fun setMusicEnabled(enabled: Boolean) {
        soundEngine.setMusicEnabled(enabled)
        _uiState.update { it.copy(isMusicEnabled = enabled) }
        saveCurrentProfile()
    }

    fun onScrewTapped(screw: Screw) {
        val state = _uiState.value
        if (state.isPaused || state.isVictory || state.isFailed) return

        // If Hammer booster is active, hammer removes this screw or its attached piece
        if (state.isHammerActive) {
            useHammerOnScrew(screw)
            return
        }

        if (screw.isRemoved || screw.isUnscrewing) return

        // 1. Accessibility & Blocking Check
        val isBlocked = PhysicsSimulator.isScrewBlocked(screw, state.pieces)
        if (isBlocked) {
            // Subtle mistake feedback & wrong-click increment
            soundEngine.playMistake()
            spawnBlockedSparks(screw.x, screw.y)
            val newMistakes = state.mistakesCount + 1
            _uiState.update {
                it.copy(
                    mistakesCount = newMistakes,
                    isFailed = newMistakes >= 3
                )
            }
            return
        }

        // 2. Find target active toolbox or temporary slot
        val targetBoxIndex = state.toolboxes.indexOfFirst { box ->
            !box.isLocked && box.color == screw.color && box.currentCount < box.capacity
        }

        var isUsingTempSlot = false
        val finalBoxIndex = if (targetBoxIndex >= 0) {
            targetBoxIndex
        } else if (state.tempLockerScrew == null && state.extraSlotCount > 0) {
            // Allow using temp pocket
            isUsingTempSlot = true
            0
        } else {
            // No matching open toolbox container
            soundEngine.playMistake()
            spawnBlockedSparks(screw.x, screw.y)
            val newMistakes = state.mistakesCount + 1
            _uiState.update {
                it.copy(
                    mistakesCount = newMistakes,
                    isFailed = newMistakes >= 3
                )
            }
            return
        }

        // 3. Unscrew and remove screw
        soundEngine.playUnscrewFast()
        screw.isUnscrewing = true

        viewModelScope.launch {
            // Snappy fast 0.18s unscrew animation
            val startTime = System.currentTimeMillis()
            val durationMs = 180L
            while (System.currentTimeMillis() - startTime < durationMs) {
                val progress = ((System.currentTimeMillis() - startTime) / durationMs.toFloat()).coerceIn(0f, 1f)
                screw.unscrewProgress = progress
                kotlinx.coroutines.delay(16)
            }

            screw.isUnscrewing = false
            screw.isRemoved = true

            // Calculate target position at top of board (normalized 0..1000)
            val targetX = when (finalBoxIndex) {
                0 -> 140f
                1 -> 380f
                2 -> 620f
                else -> 860f
            }
            val targetY = -60f // Top toolbox row in normalized canvas coordinate space

            // Control point for smooth curved bezier flight
            val controlX = (screw.x + targetX) / 2f + (if (screw.x < 500f) -80f else 80f)
            val controlY = (screw.y + targetY) / 2f - 120f

            val flying = FlyingScrew(
                id = System.currentTimeMillis(),
                screwId = screw.id,
                color = screw.color,
                startX = screw.x,
                startY = screw.y,
                targetX = targetX,
                targetY = targetY,
                controlX = controlX,
                controlY = controlY,
                targetToolboxIndex = finalBoxIndex,
                isTempLocker = isUsingTempSlot
            )

            // React attached pieces
            val detachedPieceIds = mutableListOf<Int>()
            for (piece in _uiState.value.pieces) {
                if (piece.isDetached || piece.isFallen) continue
                if (piece.supportingScrewIds.contains(screw.id)) {
                    piece.supportingScrewIds.remove(screw.id)
                    if (piece.supportingScrewIds.isEmpty()) {
                        // All supporting screws removed -> PANEL MUST FALL DOWNWARD
                        piece.isDetached = true
                        piece.velocityY = 180f
                        piece.velocityX = (rng.nextFloat() - 0.5f) * 20f // minimal sideways
                        piece.angularVelocity = (rng.nextFloat() - 0.5f) * 1.2f
                        detachedPieceIds.add(piece.id)
                        soundEngine.playPieceFall()
                    } else {
                        // Spring micro-jiggle reaction
                        piece.jiggleAmount = 1f
                    }
                }
            }

            // Save move history for Undo
            val move = MoveHistory(
                screwId = screw.id,
                screwColor = screw.color,
                toolboxIndex = finalBoxIndex,
                wasDetachedPieceIds = detachedPieceIds
            )

            _uiState.update {
                it.copy(
                    flyingScrews = it.flyingScrews + flying,
                    moveHistory = it.moveHistory + move
                )
            }

            // Await flying screw arrival
            kotlinx.coroutines.delay(260)

            // On screw arrive at toolbox
            if (isUsingTempSlot) {
                soundEngine.playToolboxSnap()
                _uiState.update { it.copy(tempLockerScrew = screw.color) }
            } else {
                onScrewArrivedAtToolbox(finalBoxIndex, screw.color)
            }

            checkVictoryCondition()
        }
    }

    private fun onScrewArrivedAtToolbox(boxIndex: Int, color: GameColor) {
        soundEngine.playToolboxSnap()
        val boxes = _uiState.value.toolboxes.toMutableList()
        val box = boxes.getOrNull(boxIndex) ?: return
        box.currentCount++

        // Spawn entry spark particles
        spawnToolboxSparks(boxIndex)

        // Check if toolbox is now complete (3 matching screws)
        if (box.currentCount >= box.capacity) {
            soundEngine.playToolboxComplete()
            box.isCompleting = true

            viewModelScope.launch {
                kotlinx.coroutines.delay(350)
                // Replace completed toolbox with next required color from queue
                val queue = _uiState.value.extraColorQueue.toMutableList()
                val nextColor = if (queue.isNotEmpty()) {
                    queue.removeAt(0)
                } else {
                    // Fallback to next dynamic color
                    GameColor.fromIndex(box.color.ordinal + 1)
                }

                box.color = nextColor
                box.currentCount = 0
                box.isCompleting = false

                // Unlock any locked toolbox if unlock criteria met
                val totalRemoved = _uiState.value.screws.count { it.isRemoved }
                for (b in boxes) {
                    if (b.isLocked && totalRemoved >= b.unlockRequirement) {
                        b.isLocked = false
                    }
                }

                _uiState.update {
                    it.copy(
                        toolboxes = boxes,
                        extraColorQueue = queue
                    )
                }

                // Check if temp locker screw can now enter the refreshed toolbox
                val tempScrew = _uiState.value.tempLockerScrew
                if (tempScrew != null && box.color == tempScrew && box.currentCount < box.capacity) {
                    box.currentCount++
                    _uiState.update { it.copy(tempLockerScrew = null) }
                }

                checkVictoryCondition()
            }
        } else {
            _uiState.update { it.copy(toolboxes = boxes) }
        }
    }

    private fun checkVictoryCondition() {
        val state = _uiState.value
        val allScrewsRemoved = state.screws.all { it.isRemoved }
        val allPiecesDetached = state.pieces.all { it.isDetached || it.isFallen }

        if (allScrewsRemoved || allPiecesDetached) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(400)
                soundEngine.playLevelComplete()
                val starsEarned = when (state.mistakesCount) {
                    0 -> 3
                    1 -> 2
                    else -> 1
                }
                val rewardCoins = 50 + state.currentLevelNumber * 5
                val newCoins = state.coins + rewardCoins

                repository.recordLevelCompleted(
                    levelNumber = state.currentLevelNumber,
                    stars = starsEarned,
                    timeSeconds = 45
                )

                _uiState.update {
                    it.copy(
                        isVictory = true,
                        victoryStars = starsEarned,
                        victoryCoinsReward = rewardCoins,
                        coins = newCoins,
                        highestUnlockedLevel = maxOf(it.highestUnlockedLevel, it.currentLevelNumber + 1)
                    )
                }
                saveCurrentProfile()
            }
        }
    }

    fun onPanelTapped(piece: PuzzlePiece) {
        if (_uiState.value.isHammerActive) {
            useHammerOnPiece(piece)
        }
    }

    fun onBoosterClicked(boosterType: BoosterType) {
        soundEngine.playBooster()
        val state = _uiState.value
        when (boosterType) {
            BoosterType.HAMMER -> {
                if (state.hammerCount > 0) {
                    _uiState.update { it.copy(isHammerActive = !it.isHammerActive) }
                }
            }
            BoosterType.MAGNET -> {
                if (state.magnetCount > 0) {
                    useMagnetBooster()
                }
            }
            BoosterType.UNDO -> {
                if (state.undoCount > 0 && state.moveHistory.isNotEmpty()) {
                    useUndoBooster()
                }
            }
            BoosterType.EXTRA_SLOT -> {
                if (state.extraSlotCount > 0) {
                    _uiState.update {
                        it.copy(
                            extraSlotCount = it.extraSlotCount - 1
                        )
                    }
                    saveCurrentProfile()
                }
            }
        }
    }

    private fun useHammerOnPiece(piece: PuzzlePiece) {
        soundEngine.playPieceFall()
        piece.isDetached = true
        piece.velocityY = 300f
        piece.angularVelocity = 0.5f
        _uiState.update {
            it.copy(
                hammerCount = it.hammerCount - 1,
                isHammerActive = false
            )
        }
        saveCurrentProfile()
        checkVictoryCondition()
    }

    private fun useHammerOnScrew(screw: Screw) {
        _uiState.update { it.copy(isHammerActive = false) }
        onScrewTapped(screw)
    }

    private fun useMagnetBooster() {
        val state = _uiState.value
        // Find first accessible screw that matches ANY open toolbox
        val openColors = state.toolboxes.filter { !it.isLocked && it.currentCount < it.capacity }.map { it.color }
        val matchingScrew = state.screws.firstOrNull { s ->
            !s.isRemoved && !s.isUnscrewing && openColors.contains(s.color) &&
                    !PhysicsSimulator.isScrewBlocked(s, state.pieces)
        }

        if (matchingScrew != null) {
            _uiState.update { it.copy(magnetCount = it.magnetCount - 1) }
            saveCurrentProfile()
            onScrewTapped(matchingScrew)
        } else {
            // Find any unblocked screw and pull it
            val anyAccessible = state.screws.firstOrNull { s ->
                !s.isRemoved && !s.isUnscrewing && !PhysicsSimulator.isScrewBlocked(s, state.pieces)
            }
            if (anyAccessible != null) {
                _uiState.update { it.copy(magnetCount = it.magnetCount - 1) }
                saveCurrentProfile()
                onScrewTapped(anyAccessible)
            }
        }
    }

    private fun useUndoBooster() {
        val state = _uiState.value
        val lastMove = state.moveHistory.lastOrNull() ?: return
        val screw = state.screws.firstOrNull { it.id == lastMove.screwId } ?: return

        screw.isRemoved = false
        screw.unscrewProgress = 0f

        // Re-attach pieces that were detached by this screw
        for (piece in state.pieces) {
            if (lastMove.wasDetachedPieceIds.contains(piece.id)) {
                piece.isDetached = false
                piece.isFallen = false
                piece.translationY = 0f
                piece.translationX = 0f
                piece.rotation = 0f
                piece.velocityY = 0f
                piece.opacity = 1f
                piece.supportingScrewIds.add(screw.id)
            }
        }

        // Decrement toolbox count
        val boxes = state.toolboxes.toMutableList()
        val box = boxes.getOrNull(lastMove.toolboxIndex)
        if (box != null && box.currentCount > 0) {
            box.currentCount--
        }

        _uiState.update {
            it.copy(
                undoCount = it.undoCount - 1,
                moveHistory = it.moveHistory.dropLast(1),
                toolboxes = boxes
            )
        }
        saveCurrentProfile()
    }

    fun continueWithRewardedAd(context: Context) {
        AdMobManager.showRewardedAd(
            context = context,
            onRewardEarned = {
                _uiState.update {
                    it.copy(
                        mistakesCount = 0,
                        isFailed = false
                    )
                }
            }
        )
    }

    fun onFrameUpdate(deltaSeconds: Float) {
        val state = _uiState.value
        if (state.isPaused) return

        val flyingList = state.flyingScrews.toMutableList()
        val particleList = state.particles.toMutableList()

        PhysicsSimulator.updatePhysics(
            pieces = state.pieces,
            flyingScrews = flyingList,
            particles = particleList,
            deltaSeconds = deltaSeconds,
            onPieceFellOff = { _ ->
                checkVictoryCondition()
            }
        )

        _uiState.update {
            it.copy(
                flyingScrews = flyingList,
                particles = particleList
            )
        }
    }

    private fun spawnBlockedSparks(x: Float, y: Float) {
        val newParticles = mutableListOf<VisualParticle>()
        for (i in 0 until 12) {
            val angle = (rng.nextFloat() * 2.0 * Math.PI).toFloat()
            val speed = rng.nextFloat() * 180f + 60f
            newParticles.add(
                VisualParticle(
                    x = x,
                    y = y,
                    vx = (cos(angle) * speed) / 60f,
                    vy = (sin(angle) * speed) / 60f,
                    color = Color(0xFFEF4444),
                    size = rng.nextFloat() * 6f + 3f,
                    decay = 0.05f
                )
            )
        }
        _uiState.update { it.copy(particles = it.particles + newParticles) }
    }

    private fun spawnToolboxSparks(boxIndex: Int) {
        val targetX = when (boxIndex) {
            0 -> 140f
            1 -> 380f
            2 -> 620f
            else -> 860f
        }
        val targetY = -60f

        val newParticles = mutableListOf<VisualParticle>()
        for (i in 0 until 16) {
            val angle = (rng.nextFloat() * 2.0 * Math.PI).toFloat()
            val speed = rng.nextFloat() * 240f + 80f
            newParticles.add(
                VisualParticle(
                    x = targetX,
                    y = targetY,
                    vx = (cos(angle) * speed) / 60f,
                    vy = (sin(angle) * speed) / 60f,
                    color = Color(0xFFFBBF24),
                    size = rng.nextFloat() * 8f + 4f,
                    decay = 0.04f
                )
            )
        }
        _uiState.update { it.copy(particles = it.particles + newParticles) }
    }

    private fun saveCurrentProfile() {
        val state = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveProfile(
                GameProfileEntity(
                    id = 1,
                    currentLevel = state.currentLevelNumber,
                    highestUnlockedLevel = state.highestUnlockedLevel,
                    coins = state.coins,
                    hammerCount = state.hammerCount,
                    magnetCount = state.magnetCount,
                    undoCount = state.undoCount,
                    extraSlotCount = state.extraSlotCount,
                    isSoundEnabled = state.isSoundEnabled,
                    isMusicEnabled = state.isMusicEnabled
                )
            )
        }
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
