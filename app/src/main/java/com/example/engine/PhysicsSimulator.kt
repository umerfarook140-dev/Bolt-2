package com.example.engine

import com.example.model.FlyingScrew
import com.example.model.OffsetFloat
import com.example.model.PuzzlePiece
import com.example.model.Screw
import com.example.model.VisualParticle
import kotlin.math.max

object PhysicsSimulator {

    // Point in polygon algorithm for accessibility & blocking checks
    fun isPointInsidePiece(px: Float, py: Float, piece: PuzzlePiece): Boolean {
        if (piece.isDetached || piece.isFallen) return false
        val pts = piece.points
        if (pts.size < 3) return false

        var inside = false
        var j = pts.size - 1
        for (i in pts.indices) {
            val xi = pts[i].x
            val yi = pts[i].y
            val xj = pts[j].x
            val yj = pts[j].y

            val intersect = ((yi > py) != (yj > py)) &&
                    (px < (xj - xi) * (py - yi) / (yj - yi) + xi)
            if (intersect) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    // Determine whether a screw is blocked by any piece in a higher layer
    fun isScrewBlocked(screw: Screw, pieces: List<PuzzlePiece>): Boolean {
        if (screw.isRemoved) return true
        for (piece in pieces) {
            if (piece.isDetached || piece.isFallen) continue
            // If piece is in a strictly higher layer and covers this screw
            if (piece.layer > screw.layer) {
                // Check if screw coordinate falls within this higher-layer piece
                if (isPointInsidePiece(screw.x, screw.y, piece)) {
                    // Unless this piece has this screw as its own supporting screw (in which case the hole is accessible)
                    if (!piece.supportingScrewIds.contains(screw.id)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    // Physics step for falling detached panels
    fun updatePhysics(
        pieces: List<PuzzlePiece>,
        flyingScrews: MutableList<FlyingScrew>,
        particles: MutableList<VisualParticle>,
        deltaSeconds: Float,
        onPieceFellOff: (Int) -> Unit
    ) {
        val gravity = 2400f // Downward gravity in screen space

        // 1. Update falling pieces
        for (piece in pieces) {
            if (piece.isDetached && !piece.isFallen) {
                // Downward acceleration
                piece.velocityY += gravity * deltaSeconds
                piece.translationY += piece.velocityY * deltaSeconds

                // Very minimal sideways movement for realistic drop
                piece.translationX += piece.velocityX * deltaSeconds

                // Subtle rotational momentum
                piece.rotation += piece.angularVelocity * deltaSeconds

                // Once dropped far below the screen (e.g. 1400f), mark as fallen & fade out
                if (piece.translationY > 800f) {
                    piece.opacity = max(0f, piece.opacity - deltaSeconds * 2.5f)
                    if (piece.opacity <= 0f) {
                        piece.isFallen = true
                        onPieceFellOff(piece.id)
                    }
                }
            }

            // Dampen jiggle effect
            if (piece.jiggleAmount > 0.01f) {
                piece.jiggleAmount = max(0f, piece.jiggleAmount - deltaSeconds * 8f)
            }
        }

        // 2. Update flying screws
        val flyingIter = flyingScrews.iterator()
        while (flyingIter.hasNext()) {
            val fly = flyingIter.next()
            fly.progress += deltaSeconds * 3.2f // ~0.3s flight duration
            if (fly.progress >= 1f) {
                fly.progress = 1f
                flyingIter.remove()
            }
        }

        // 3. Update particle effects
        val particleIter = particles.iterator()
        while (particleIter.hasNext()) {
            val p = particleIter.next()
            p.x += p.vx * deltaSeconds * 60f
            p.y += p.vy * deltaSeconds * 60f
            p.vy += 0.3f // slight gravity on sparks
            p.life -= p.decay
            p.alpha = max(0f, p.life)
            if (p.life <= 0f) {
                particleIter.remove()
            }
        }
    }

    // Bezier interpolation for flying screw
    fun evaluateBezier(
        startX: Float, startY: Float,
        controlX: Float, controlY: Float,
        targetX: Float, targetY: Float,
        t: Float
    ): OffsetFloat {
        val clampedT = t.coerceIn(0f, 1f)
        val oneMinusT = 1f - clampedT
        val x = (oneMinusT * oneMinusT * startX) + (2f * oneMinusT * clampedT * controlX) + (clampedT * clampedT * targetX)
        val y = (oneMinusT * oneMinusT * startY) + (2f * oneMinusT * clampedT * controlY) + (clampedT * clampedT * targetY)
        return OffsetFloat(x, y)
    }
}
