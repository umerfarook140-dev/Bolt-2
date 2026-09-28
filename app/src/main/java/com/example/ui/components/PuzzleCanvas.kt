package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.engine.PhysicsSimulator
import com.example.model.FlyingScrew
import com.example.model.GameColor
import com.example.model.PanelMaterial
import com.example.model.PieceShapeType
import com.example.model.PuzzlePiece
import com.example.model.Screw
import com.example.model.VisualParticle
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PuzzleCanvas(
    pieces: List<PuzzlePiece>,
    screws: List<Screw>,
    flyingScrews: List<FlyingScrew>,
    particles: List<VisualParticle>,
    onScrewTap: (Screw) -> Unit,
    onPanelTap: (PuzzlePiece) -> Unit,
    onFrameUpdate: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // 60 FPS animation loop
    LaunchedEffect(Unit) {
        var lastTimeNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastTimeNanos != 0L) {
                    val deltaSeconds = ((frameNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    onFrameUpdate(deltaSeconds)
                }
                lastTimeNanos = frameNanos
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("puzzle_canvas_area")
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        // Responsive board scaling ensuring everything fits inside visible mobile screen
        val boardDimension = min(containerWidth * 0.95f, containerHeight * 0.95f)
        val boardScale = boardDimension / 1000f
        val offsetX = (containerWidth - boardDimension) / 2f
        val offsetY = (containerHeight - boardDimension) / 2f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(screws, pieces) {
                    detectTapGestures { tapOffset ->
                        // Convert tap position to normalized board coordinates
                        val boardX = (tapOffset.x - offsetX) / boardScale
                        val boardY = (tapOffset.y - offsetY) / boardScale

                        // 1. Check for screw tap with large comfortable 48-56dp hit radius
                        val hitRadiusBoard = (48f * density) / boardScale
                        var tappedScrew: Screw? = null
                        var minDistanceSq = Float.MAX_VALUE

                        for (s in screws) {
                            if (s.isRemoved) continue
                            val dx = s.x - boardX
                            val dy = s.y - boardY
                            val distSq = dx * dx + dy * dy
                            if (distSq <= hitRadiusBoard * hitRadiusBoard && distSq < minDistanceSq) {
                                minDistanceSq = distSq
                                tappedScrew = s
                            }
                        }

                        if (tappedScrew != null) {
                            onScrewTap(tappedScrew)
                        } else {
                            // Check for panel tap (used for Hammer booster)
                            val tappedPiece = pieces
                                .filter { !it.isDetached && !it.isFallen }
                                .sortedByDescending { it.layer }
                                .firstOrNull { piece ->
                                    PhysicsSimulator.isPointInsidePiece(boardX, boardY, piece)
                                }
                            if (tappedPiece != null) {
                                onPanelTap(tappedPiece)
                            }
                        }
                    }
                }
        ) {
            // Draw background board backing plate with metallic fasteners and ambient occlusion
            drawBoardBackground(offsetX, offsetY, boardDimension)

            // Draw pieces ordered by layer (bottom to top), including falling pieces
            val sortedPieces = pieces.sortedBy { it.layer }
            for (piece in sortedPieces) {
                if (piece.isFallen) continue
                drawPuzzlePiece(piece, offsetX, offsetY, boardScale)
            }

            // Draw screw holes & socket recesses
            for (s in screws) {
                if (s.isRemoved) {
                    // Draw empty drilled socket hole
                    drawEmptyHole(s.x, s.y, offsetX, offsetY, boardScale)
                }
            }

            // Draw active screws in place
            val visibleScrews = screws.filter { !it.isRemoved }.sortedBy { it.layer }
            for (screw in visibleScrews) {
                drawPrecisionScrew(screw, offsetX, offsetY, boardScale)
            }

            // Draw flying screws traveling smoothly to top toolboxes
            for (fly in flyingScrews) {
                drawFlyingScrew(fly, offsetX, offsetY, boardScale)
            }

            // Draw visual particle bursts
            for (p in particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size * boardScale,
                    center = Offset(offsetX + p.x * boardScale, offsetY + p.y * boardScale)
                )
            }
        }
    }
}

// Background backing board with brushed gunmetal plate & subtle grid
private fun DrawScope.drawBoardBackground(
    ox: Float,
    oy: Float,
    dimension: Float
) {
    // Outer drop shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(ox + 4f, oy + 8f),
        size = Size(dimension, dimension),
        cornerRadius = CornerRadius(28f, 28f)
    )

    // Brushed metal backplate
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF242938),
                Color(0xFF181B26),
                Color(0xFF0F1118)
            ),
            center = Offset(ox + dimension * 0.5f, oy + dimension * 0.4f),
            radius = dimension * 0.7f
        ),
        topLeft = Offset(ox, oy),
        size = Size(dimension, dimension),
        cornerRadius = CornerRadius(28f, 28f)
    )

    // Metallic bevel border
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.6f)
            ),
            start = Offset(ox, oy),
            end = Offset(ox + dimension, oy + dimension)
        ),
        topLeft = Offset(ox, oy),
        size = Size(dimension, dimension),
        cornerRadius = CornerRadius(28f, 28f),
        style = Stroke(width = 3f)
    )

    // Subtle technical grid lines
    val gridStep = dimension / 8f
    for (i in 1..7) {
        drawLine(
            color = Color.White.copy(alpha = 0.03f),
            start = Offset(ox + i * gridStep, oy + 16f),
            end = Offset(ox + i * gridStep, oy + dimension - 16f),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.03f),
            start = Offset(ox + 16f, oy + i * gridStep),
            end = Offset(ox + dimension - 16f, oy + i * gridStep),
            strokeWidth = 1f
        )
    }
}

// Render individual mechanical puzzle piece
private fun DrawScope.drawPuzzlePiece(
    piece: PuzzlePiece,
    ox: Float,
    oy: Float,
    scale: Float
) {
    val alpha = piece.opacity
    if (alpha <= 0.01f) return

    val cx = ox + (piece.centerX + piece.translationX) * scale
    val cy = oy + (piece.centerY + piece.translationY) * scale

    val shadowDepth = (piece.layer + 1) * 6f * scale

    // Jiggle displacement on screw loosened
    val jiggleDx = (sin(piece.jiggleAmount * 20.0) * piece.jiggleAmount * 4f * scale).toFloat()

    translate(left = piece.translationX * scale + jiggleDx, top = piece.translationY * scale) {
        rotate(degrees = piece.rotation, pivot = Offset(cx, cy)) {
            // Convert points to canvas coordinates
            val path = Path()
            if (piece.points.isNotEmpty()) {
                path.moveTo(ox + piece.points[0].x * scale, oy + piece.points[0].y * scale)
                for (i in 1 until piece.points.size) {
                    path.lineTo(ox + piece.points[i].x * scale, oy + piece.points[i].y * scale)
                }
                path.close()
            }

            // Drop shadow
            drawPath(
                path = path,
                color = Color.Black.copy(alpha = 0.4f * alpha),
                style = Fill
            )

            // Panel material base fill
            val materialBrush = when (piece.material) {
                PanelMaterial.WOOD -> Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8D6E63).copy(alpha = alpha),
                        Color(0xFF5D4037).copy(alpha = alpha),
                        Color(0xFF4E342E).copy(alpha = alpha)
                    )
                )
                PanelMaterial.METAL -> Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF78909C).copy(alpha = alpha),
                        Color(0xFF455A64).copy(alpha = alpha),
                        Color(0xFF263238).copy(alpha = alpha)
                    )
                )
                PanelMaterial.ACRYLIC -> Brush.linearGradient(
                    colors = listOf(
                        piece.baseColor.copy(alpha = 0.95f * alpha),
                        piece.baseColor.copy(alpha = 0.8f * alpha)
                    )
                )
                PanelMaterial.GLASS -> Brush.linearGradient(
                    colors = listOf(
                        Color(0xAA80DEEA).copy(alpha = 0.6f * alpha),
                        Color(0xAA00ACC1).copy(alpha = 0.45f * alpha)
                    )
                )
                PanelMaterial.CARBON -> Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF212121).copy(alpha = alpha),
                        Color(0xFF141414).copy(alpha = alpha)
                    )
                )
                PanelMaterial.GOLD_PLATED -> Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD54F).copy(alpha = alpha),
                        Color(0xFFFFA000).copy(alpha = alpha),
                        Color(0xFFFF8F00).copy(alpha = alpha)
                    )
                )
            }

            drawPath(path = path, brush = materialBrush, style = Fill)

            // Panel bevel 3D highlights
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f * alpha),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.5f * alpha)
                    )
                ),
                style = Stroke(width = 3.5f * scale)
            )

            // Inner subtle border
            drawPath(
                path = path,
                color = piece.borderColor.copy(alpha = 0.2f * alpha),
                style = Stroke(width = 1.5f * scale)
            )
        }
    }
}

// Draw precision slim screw
private fun DrawScope.drawPrecisionScrew(
    screw: Screw,
    ox: Float,
    oy: Float,
    scale: Float
) {
    val sx = ox + screw.x * scale
    val sy = oy + screw.y * scale

    // Small slim screw proportion: 4-6% of puzzle board width
    val baseRadius = 26f * scale
    val radius = if (screw.isUnscrewing) {
        baseRadius * (1f + screw.unscrewProgress * 0.4f)
    } else {
        baseRadius
    }

    val rotation = screw.rotationDegrees + (if (screw.isUnscrewing) screw.unscrewProgress * 720f else 0f)
    val color = screw.color

    rotate(degrees = rotation, pivot = Offset(sx, sy)) {
        // Screw shadow (lifts upward during unscrew)
        val shadowOffset = if (screw.isUnscrewing) (12f * (1f + screw.unscrewProgress)) * scale else 4f * scale
        drawCircle(
            color = Color.Black.copy(alpha = 0.45f),
            radius = radius * 0.95f,
            center = Offset(sx + shadowOffset * 0.5f, sy + shadowOffset)
        )

        // Outer metallic bevel ring
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.8f),
                    color.primaryColor,
                    color.darkColor,
                    Color.White.copy(alpha = 0.9f),
                    color.darkColor,
                    color.primaryColor
                ),
                center = Offset(sx, sy)
            ),
            radius = radius,
            center = Offset(sx, sy)
        )

        // Colored core cap
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.metalSpecular,
                    color.lightColor,
                    color.primaryColor,
                    color.darkColor
                ),
                center = Offset(sx - radius * 0.25f, sy - radius * 0.25f),
                radius = radius * 0.85f
            ),
            radius = radius * 0.78f,
            center = Offset(sx, sy)
        )

        // Precision Mechanical Slot (Hex / Torx Star / Cross slot)
        val slotLength = radius * 0.45f
        val slotWidth = 3f * scale

        // Primary slot line
        drawLine(
            color = Color(0xFF0F172A),
            start = Offset(sx - slotLength, sy),
            end = Offset(sx + slotLength, sy),
            strokeWidth = slotWidth,
            cap = StrokeCap.Round
        )
        // Cross slot line
        drawLine(
            color = Color(0xFF0F172A),
            start = Offset(sx, sy - slotLength),
            end = Offset(sx, sy + slotLength),
            strokeWidth = slotWidth,
            cap = StrokeCap.Round
        )

        // Center hex indent
        drawCircle(
            color = Color(0xFF090D16),
            radius = radius * 0.22f,
            center = Offset(sx, sy)
        )

        // Highlight specular reflection spot
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = radius * 0.16f,
            center = Offset(sx - radius * 0.35f, sy - radius * 0.35f)
        )
    }
}

// Empty socket recess when screw is removed
private fun DrawScope.drawEmptyHole(
    boardX: Float,
    boardY: Float,
    ox: Float,
    oy: Float,
    scale: Float
) {
    val hx = ox + boardX * scale
    val hy = oy + boardY * scale
    val holeRadius = 22f * scale

    // Socket outer lip
    drawCircle(
        color = Color(0xFF0B0E14),
        radius = holeRadius,
        center = Offset(hx, hy)
    )
    // Deep hole shadow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black, Color(0xFF1E293B)),
            center = Offset(hx, hy),
            radius = holeRadius
        ),
        radius = holeRadius * 0.75f,
        center = Offset(hx, hy)
    )
    // Metallic thread rim
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = holeRadius,
        center = Offset(hx, hy),
        style = Stroke(width = 1.2f * scale)
    )
}

// Draw flying screw along bezier curve toward top toolbox
private fun DrawScope.drawFlyingScrew(
    fly: FlyingScrew,
    ox: Float,
    oy: Float,
    scale: Float
) {
    val currentPos = PhysicsSimulator.evaluateBezier(
        fly.startX, fly.startY,
        fly.controlX, fly.controlY,
        fly.targetX, fly.targetY,
        fly.progress
    )

    val currentX = ox + currentPos.x * scale
    val currentY = oy + currentPos.y * scale

    // Scale down smoothly as it enters toolbox
    val sizeFactor = (1f - fly.progress * 0.35f).coerceIn(0.6f, 1.2f)
    val radius = 24f * scale * sizeFactor
    val color = fly.color

    val spinAngle = fly.progress * 1080f

    rotate(degrees = spinAngle, pivot = Offset(currentX, currentY)) {
        // Glowing aura trail
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.lightColor.copy(alpha = 0.6f),
                    color.primaryColor.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(currentX, currentY),
                radius = radius * 2.2f
            ),
            radius = radius * 2.2f,
            center = Offset(currentX, currentY)
        )

        // Screw head
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.metalSpecular,
                    color.lightColor,
                    color.primaryColor,
                    color.darkColor
                ),
                center = Offset(currentX - radius * 0.3f, currentY - radius * 0.3f),
                radius = radius
            ),
            radius = radius,
            center = Offset(currentX, currentY)
        )

        // Center slot
        drawCircle(
            color = Color(0xFF0F172A),
            radius = radius * 0.25f,
            center = Offset(currentX, currentY)
        )
    }
}
