package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class PanelMaterial {
    METAL,
    WOOD,
    ACRYLIC,
    GLASS,
    CARBON,
    GOLD_PLATED
}

enum class PieceShapeType {
    RECTANGLE,
    ROUNDED_BAR,
    L_SHAPE,
    T_SHAPE,
    DISK,
    CROSS,
    TRIANGLE_PLATE,
    CURVED_PLATE
}

data class OffsetFloat(val x: Float, val y: Float) {
    fun toOffset(): Offset = Offset(x, y)
}

data class Screw(
    val id: Int,
    var x: Float,
    var y: Float,
    val color: GameColor,
    val layer: Int,
    var isRemoved: Boolean = false,
    var isUnscrewing: Boolean = false,
    var unscrewProgress: Float = 0f,
    var rotationDegrees: Float = 0f,
    var blockedReason: String? = null
)

data class PuzzlePiece(
    val id: Int,
    val name: String,
    val material: PanelMaterial,
    val baseColor: Color,
    val borderColor: Color,
    val layer: Int,
    val shapeType: PieceShapeType,
    val points: List<OffsetFloat>, // Vertices in normalized board space (0..1000)
    val width: Float = 0f,
    val height: Float = 0f,
    val centerX: Float,
    val centerY: Float,
    val radius: Float = 0f,
    val supportingScrewIds: MutableSet<Int>,
    var isDetached: Boolean = false,
    var isFallen: Boolean = false,
    var velocityY: Float = 0f,
    var velocityX: Float = 0f,
    var angularVelocity: Float = 0f,
    var translationX: Float = 0f,
    var translationY: Float = 0f,
    var rotation: Float = 0f,
    var opacity: Float = 1f,
    var jiggleAmount: Float = 0f
)

data class Toolbox(
    val id: Int,
    val index: Int, // 0 to 3
    var color: GameColor,
    var currentCount: Int = 0,
    val capacity: Int = 3,
    var isLocked: Boolean = false,
    val unlockRequirement: Int = 0,
    var isCompleting: Boolean = false,
    var completionProgress: Float = 0f
)

data class FlyingScrew(
    val id: Long,
    val screwId: Int,
    val color: GameColor,
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val controlX: Float,
    val controlY: Float,
    var progress: Float = 0f,
    val targetToolboxIndex: Int,
    val isTempLocker: Boolean = false
)

data class VisualParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float = 1f,
    var life: Float = 1f,
    val decay: Float = 0.03f
)

enum class BoosterType(val title: String, val description: String, val iconName: String) {
    HAMMER("Hammer", "Break 1 blocking panel", "hammer"),
    MAGNET("Magnet", "Pull 1 matching screw", "magnet"),
    UNDO("Undo", "Undo last screw removal", "undo"),
    EXTRA_SLOT("Extra Slot", "Add 1 temporary bolt locker", "box")
}

data class LevelData(
    val levelNumber: Int,
    val title: String,
    val difficultyName: String,
    val pieces: List<PuzzlePiece>,
    val screws: List<Screw>,
    val initialToolboxes: List<Toolbox>,
    val extraColorQueue: List<GameColor>,
    val totalScrewsCount: Int,
    val targetStars: Int = 3
)

enum class ScreenState {
    HOME,
    LEVEL_SELECT,
    PLAYING
}

data class MoveHistory(
    val screwId: Int,
    val screwColor: GameColor,
    val toolboxIndex: Int,
    val wasDetachedPieceIds: List<Int>
)
