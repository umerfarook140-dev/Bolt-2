package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.model.GameColor
import com.example.model.LevelData
import com.example.model.OffsetFloat
import com.example.model.PanelMaterial
import com.example.model.PieceShapeType
import com.example.model.PuzzlePiece
import com.example.model.Screw
import com.example.model.Toolbox
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

object LevelGenerator {

    fun generateLevel(levelNumber: Int): LevelData {
        val clampedLevel = levelNumber.coerceIn(1, 500)
        val rng = Random(clampedLevel.toLong() * 9973L + 12345L)

        val (tierName, numColors, numPieces, layerCount, lockedCount) = when (clampedLevel) {
            in 1..5 -> LevelTierInfo("Tutorial", 2, 3, 2, 0)
            in 6..10 -> LevelTierInfo("Beginner", 2, 4, 2, 1)
            in 11..30 -> LevelTierInfo("Easy", 3, 5, 2, 1)
            in 31..60 -> LevelTierInfo("Easy-Medium", 3, 6, 3, 2)
            in 61..100 -> LevelTierInfo("Medium", 4, 7, 3, 2)
            in 101..200 -> LevelTierInfo("Hard", 4, 8, 3, 2)
            in 201..300 -> LevelTierInfo("Very Hard", 5, 9, 4, 2)
            in 301..400 -> LevelTierInfo("Expert", 5, 10, 4, 2)
            else -> LevelTierInfo("Master", 6, 12, 4, 2)
        }

        // Color selection for this level from the 12 rich colors
        val allColors = GameColor.entries.shuffled(rng)
        val levelColors = allColors.take(numColors)

        // Palette for piece materials and decorative colors
        val panelMaterials = listOf(
            PanelMaterial.ACRYLIC,
            PanelMaterial.WOOD,
            PanelMaterial.METAL,
            PanelMaterial.CARBON,
            PanelMaterial.GLASS,
            PanelMaterial.GOLD_PLATED
        )

        val pieceColors = listOf(
            Color(0xFF263238), // Dark slate
            Color(0xFF37474F), // Blue grey
            Color(0xFF5D4037), // Rich Walnut
            Color(0xFF00695C), // Deep Emerald
            Color(0xFF1565C0), // Royal Cobalt
            Color(0xFF6A1B9A), // Deep Amethyst
            Color(0xFFC2185B), // Ruby Plum
            Color(0xFFE65100), // Burnt Orange
            Color(0xFF455A64), // Titanium Grey
            Color(0xFF2E7D32)  // Forest Jade
        )

        val pieces = mutableListOf<PuzzlePiece>()
        val screws = mutableListOf<Screw>()
        var screwIdCounter = 1

        // Deterministic geometric layouts
        val gridRows = if (clampedLevel <= 5) 2 else if (clampedLevel <= 30) 3 else 4
        val gridCols = if (clampedLevel <= 5) 2 else if (clampedLevel <= 30) 3 else 3

        val centerX = 500f
        val centerY = 500f

        // Board margin and boundaries
        val minX = 160f
        val maxX = 840f
        val minY = 160f
        val maxY = 840f

        // Generate puzzle pieces in layered order
        for (i in 0 until numPieces) {
            val layer = i % layerCount
            val pieceId = i + 1
            val material = panelMaterials[(i + clampedLevel) % panelMaterials.size]
            val baseColor = pieceColors[(i + clampedLevel * 3) % pieceColors.size]
            val borderColor = Color.White.copy(alpha = 0.25f)

            // Shape type variation
            val shapeType = when ((i + clampedLevel) % 6) {
                0 -> PieceShapeType.ROUNDED_BAR
                1 -> PieceShapeType.RECTANGLE
                2 -> PieceShapeType.L_SHAPE
                3 -> PieceShapeType.T_SHAPE
                4 -> PieceShapeType.DISK
                else -> PieceShapeType.CROSS
            }

            // Piece center
            val px = minX + rng.nextFloat() * (maxX - minX - 200f) + 100f
            val py = minY + rng.nextFloat() * (maxY - minY - 200f) + 100f

            val pieceWidth = 140f + rng.nextFloat() * 120f
            val pieceHeight = 90f + rng.nextFloat() * 110f

            val points = createShapePoints(shapeType, px, py, pieceWidth, pieceHeight)
            val supportingScrewIds = mutableSetOf<Int>()

            // Number of screws holding this piece: 1 to 3 depending on level
            val screwCountForPiece = when {
                clampedLevel <= 3 -> if (i == 0) 1 else 2
                clampedLevel <= 10 -> if (rng.nextBoolean()) 2 else 3
                else -> if (i % 3 == 0) 3 else 2
            }

            // Place screws along the piece boundary/holes
            for (s in 0 until screwCountForPiece) {
                val fraction = if (screwCountForPiece == 1) 0.5f else (s + 0.5f) / screwCountForPiece
                val sx = when (shapeType) {
                    PieceShapeType.ROUNDED_BAR, PieceShapeType.RECTANGLE -> (px - pieceWidth / 2f + 28f) + (pieceWidth - 56f) * fraction
                    PieceShapeType.DISK -> px + (pieceWidth * 0.35f) * cos(s * (2.0 * Math.PI / screwCountForPiece)).toFloat()
                    else -> px - pieceWidth * 0.3f + s * (pieceWidth * 0.3f)
                }.coerceIn(120f, 880f)

                val sy = when (shapeType) {
                    PieceShapeType.ROUNDED_BAR, PieceShapeType.RECTANGLE -> py + (if (s % 2 == 0) -15f else 15f)
                    PieceShapeType.DISK -> py + (pieceHeight * 0.35f) * sin(s * (2.0 * Math.PI / screwCountForPiece)).toFloat()
                    else -> py - pieceHeight * 0.2f + s * 25f
                }.coerceIn(140f, 860f)

                // Assign a color ensuring balance of 3 per color
                val screwColor = levelColors[(screwIdCounter - 1) % levelColors.size]

                val screw = Screw(
                    id = screwIdCounter,
                    x = sx,
                    y = sy,
                    color = screwColor,
                    layer = layer,
                    isRemoved = false
                )
                screws.add(screw)
                supportingScrewIds.add(screwIdCounter)
                screwIdCounter++
            }

            pieces.add(
                PuzzlePiece(
                    id = pieceId,
                    name = "Panel $pieceId",
                    material = material,
                    baseColor = baseColor,
                    borderColor = borderColor,
                    layer = layer,
                    shapeType = shapeType,
                    points = points,
                    width = pieceWidth,
                    height = pieceHeight,
                    centerX = px,
                    centerY = py,
                    supportingScrewIds = supportingScrewIds
                )
            )
        }

        // Adjust total screws so each color has a multiple of 3 screws (since each toolbox takes exactly 3 screws)
        val colorCounts = mutableMapOf<GameColor, Int>()
        for (s in screws) {
            colorCounts[s.color] = (colorCounts[s.color] ?: 0) + 1
        }

        // Balance screw colors so each used color has exactly multiple of 3 (e.g. 3, 6, 9)
        var colorIdx = 0
        for (i in screws.indices) {
            val assignedColor = levelColors[colorIdx / 3 % levelColors.size]
            screws[i] = screws[i].copy(color = assignedColor)
            colorIdx++
        }

        // Ensure total screws is a multiple of 3 by trimming or padding if necessary
        val remainder = screws.size % 3
        if (remainder != 0) {
            val toRemove = remainder
            for (r in 0 until toRemove) {
                if (screws.isNotEmpty()) {
                    val removed = screws.removeAt(screws.size - 1)
                    for (p in pieces) {
                        p.supportingScrewIds.remove(removed.id)
                    }
                }
            }
        }

        // Ensure every piece has at least 1 supporting screw
        for (p in pieces) {
            if (p.supportingScrewIds.isEmpty() && screws.isNotEmpty()) {
                val available = screws.random(rng)
                p.supportingScrewIds.add(available.id)
            }
        }

        // Build toolbox color sequence
        val distinctColorsInScrews = screws.map { it.color }.distinct()
        val totalToolboxesNeeded = (screws.size / 3).coerceAtLeast(distinctColorsInScrews.size)

        val fullBoxColorSequence = mutableListOf<GameColor>()
        for (c in distinctColorsInScrews) {
            val countForColor = screws.count { it.color == c }
            val boxesForColor = (countForColor / 3).coerceAtLeast(1)
            repeat(boxesForColor) {
                fullBoxColorSequence.add(c)
            }
        }
        fullBoxColorSequence.shuffle(rng)

        // Setup the 4 Top Toolboxes
        val initialToolboxes = mutableListOf<Toolbox>()
        for (slotIndex in 0 until 4) {
            val isLocked = slotIndex >= (4 - lockedCount)
            val boxColor = if (slotIndex < fullBoxColorSequence.size) {
                fullBoxColorSequence[slotIndex]
            } else {
                levelColors[slotIndex % levelColors.size]
            }
            initialToolboxes.add(
                Toolbox(
                    id = slotIndex + 1,
                    index = slotIndex,
                    color = boxColor,
                    currentCount = 0,
                    capacity = 3,
                    isLocked = isLocked,
                    unlockRequirement = if (isLocked) (slotIndex * 3) else 0
                )
            )
        }

        val extraQueue = if (fullBoxColorSequence.size > 4) {
            fullBoxColorSequence.subList(4, fullBoxColorSequence.size).toList()
        } else {
            emptyList()
        }

        return LevelData(
            levelNumber = clampedLevel,
            title = "Level $clampedLevel",
            difficultyName = tierName,
            pieces = pieces,
            screws = screws,
            initialToolboxes = initialToolboxes,
            extraColorQueue = extraQueue,
            totalScrewsCount = screws.size
        )
    }

    private fun createShapePoints(
        shape: PieceShapeType,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float
    ): List<OffsetFloat> {
        val halfW = w / 2f
        val halfH = h / 2f
        return when (shape) {
            PieceShapeType.RECTANGLE, PieceShapeType.ROUNDED_BAR -> listOf(
                OffsetFloat(cx - halfW, cy - halfH),
                OffsetFloat(cx + halfW, cy - halfH),
                OffsetFloat(cx + halfW, cy + halfH),
                OffsetFloat(cx - halfW, cy + halfH)
            )
            PieceShapeType.L_SHAPE -> listOf(
                OffsetFloat(cx - halfW, cy - halfH),
                OffsetFloat(cx, cy - halfH),
                OffsetFloat(cx, cy),
                OffsetFloat(cx + halfW, cy),
                OffsetFloat(cx + halfW, cy + halfH),
                OffsetFloat(cx - halfW, cy + halfH)
            )
            PieceShapeType.T_SHAPE -> listOf(
                OffsetFloat(cx - halfW, cy - halfH),
                OffsetFloat(cx + halfW, cy - halfH),
                OffsetFloat(cx + halfW * 0.4f, cy - halfH),
                OffsetFloat(cx + halfW * 0.4f, cy + halfH),
                OffsetFloat(cx - halfW * 0.4f, cy + halfH),
                OffsetFloat(cx - halfW * 0.4f, cy - halfH)
            )
            PieceShapeType.DISK -> {
                val pts = mutableListOf<OffsetFloat>()
                val numSides = 16
                for (s in 0 until numSides) {
                    val angle = s * (2.0 * Math.PI / numSides)
                    pts.add(
                        OffsetFloat(
                            cx + (halfW * cos(angle)).toFloat(),
                            cy + (halfH * sin(angle)).toFloat()
                        )
                    )
                }
                pts
            }
            else -> listOf(
                OffsetFloat(cx - halfW, cy - halfH),
                OffsetFloat(cx + halfW, cy - halfH),
                OffsetFloat(cx + halfW, cy + halfH),
                OffsetFloat(cx - halfW, cy + halfH)
            )
        }
    }

    private data class LevelTierInfo(
        val tierName: String,
        val numColors: Int,
        val numPieces: Int,
        val layerCount: Int,
        val lockedCount: Int
    )
}
