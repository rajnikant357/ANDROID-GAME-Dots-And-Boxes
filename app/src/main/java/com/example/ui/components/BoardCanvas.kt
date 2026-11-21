package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LevelState
import com.example.engine.LineCoord
import com.example.engine.LineKind
import com.example.engine.PlayerSlot
import com.example.model.BoxSkinId
import com.example.model.LetterFontId
import com.example.model.PlayerPattern
import com.example.ui.theme.StudioTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Tactile DRD Board (DRD §8):
 * - Paper sheet on wood tabletop with 4dp ink outline & corner masking tape
 * - Light pencil guide grid
 * - Seeded hand-drawn wobble on ink lines + 150ms draw-in + honey midpoint dot on last move
 * - Claimed boxes with full player color, 2dp inner outline, accessibility pattern, Lilita One initial, & stamp pop
 * - Wooden peg dots with ink outline & upper-left highlight
 */
@Composable
fun DotsAndBoxesBoard(
    levelState: LevelState,
    p1Initials: String,
    p2Initials: String,
    p1Color: Color,
    p2Color: Color,
    p1Pattern: PlayerPattern,
    p2Pattern: PlayerPattern,
    p1BoxSkin: BoxSkinId = BoxSkinId.CLASSIC_PAPER,
    p2BoxSkin: BoxSkinId = BoxSkinId.CLASSIC_PAPER,
    p1LetterFont: LetterFontId = LetterFontId.BOLD_BLOCK,
    p2LetterFont: LetterFontId = LetterFontId.BOLD_BLOCK,
    interactionEnabled: Boolean,
    reducedMotion: Boolean,
    hapticsEnabled: Boolean,
    onLineTapped: (LineCoord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val haptic = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()

    val currentLevel by rememberUpdatedState(levelState)
    val currentEnabled by rememberUpdatedState(interactionEnabled)
    val currentOnTap by rememberUpdatedState(onLineTapped)
    val currentHaptics by rememberUpdatedState(hapticsEnabled)

    val drawProgress = remember { Animatable(1f) }
    val stampScale = remember { Animatable(1f) }
    val sparkleProgress = remember { Animatable(1f) }

    LaunchedEffect(levelState.lastMove, levelState.levelNumber) {
        if (levelState.lastMove != null && !reducedMotion) {
            drawProgress.snapTo(0f)
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
            )
        } else {
            drawProgress.snapTo(1f)
        }
    }

    LaunchedEffect(levelState.lastClaimedBoxes, levelState.levelNumber) {
        if (levelState.lastClaimedBoxes.isNotEmpty() && !reducedMotion) {
            stampScale.snapTo(1.06f)
            stampScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        } else {
            stampScale.snapTo(1f)
        }
    }

    LaunchedEffect(levelState.lastRevealedDiamonds, levelState.levelNumber) {
        if (levelState.lastRevealedDiamonds.isNotEmpty() && !reducedMotion) {
            sparkleProgress.snapTo(0f)
            sparkleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            )
        } else {
            sparkleProgress.snapTo(1f)
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        val side = min(maxWidth.value, maxHeight.value).dp
        val boardShape = RoundedCornerShape(28.dp)

        Box(
            modifier = Modifier
                .size(side)
                .aspectRatio(1f),
        ) {
            // Hard offset tabletop shadow beneath paper sheet
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .clip(boardShape)
                    .background(tokens.ink),
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(boardShape)
                    .background(tokens.paperCream)
                    .border(4.dp, tokens.ink, boardShape)
                    .padding(16.dp)
                    .testTag("game_board_canvas")
                    .semantics {
                        contentDescription = "Dots and Boxes ${levelState.dotCount} by ${levelState.dotCount} board"
                    }
                    .pointerInput(levelState.levelNumber, levelState.dotCount) {
                        val gridPadPx = 10.dp.toPx()
                        detectTapGestures { tap ->
                            if (!currentEnabled) return@detectTapGestures
                            val found = findClosestUndrawnLine(
                                tap = tap,
                                canvasWidth = size.width.toFloat(),
                                canvasHeight = size.height.toFloat(),
                                gridPad = gridPadPx,
                                level = currentLevel,
                            )
                            if (found != null) {
                                if (currentHaptics) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                currentOnTap(found)
                            }
                        }
                    },
            ) {
                // Static paper fibers
                drawPaperTexture(
                    lineColor = tokens.paperLine,
                    shadeColor = tokens.paperShade,
                    showCreaseLines = false,
                )

                val dotCount = levelState.dotCount
                val boxesPerSide = dotCount - 1
                val gridPad = 10.dp.toPx()
                val usableSize = (min(size.width, size.height) - gridPad * 2f).coerceAtLeast(1f)
                val step = usableSize / boxesPerSide.toFloat()

                fun dotOffset(row: Int, col: Int): Offset = Offset(
                    x = gridPad + col * step,
                    y = gridPad + row * step,
                )

                // Scale peg & pencil weights per DRD §8.6
                val pegDiameterDp = when (dotCount) {
                    4 -> 11.dp
                    6 -> 9.dp
                    else -> 7.5.dp
                }
                val pencilWidthDp = when (dotCount) {
                    4 -> 6.dp
                    6 -> 5.dp
                    else -> 4.5.dp
                }

                // 1. Faint pencil guide grid (30% opacity)
                for (r in 0 until dotCount) {
                    drawLine(
                        color = tokens.paperLine.copy(alpha = 0.28f),
                        start = dotOffset(r, 0),
                        end = dotOffset(r, boxesPerSide),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                for (c in 0 until dotCount) {
                    drawLine(
                        color = tokens.paperLine.copy(alpha = 0.28f),
                        start = dotOffset(0, c),
                        end = dotOffset(boxesPerSide, c),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                // 2. Claimed boxes (skin + pattern + 2dp inner outline + letter font + revealed diamond + sparkle pop)
                val initialFontSize = when (dotCount) {
                    4 -> 26.sp
                    6 -> 18.sp
                    else -> 13.sp
                }
                val boxInset = 3.dp.toPx()
                val lastClaimedSet = levelState.lastClaimedBoxes.toSet()
                val lastRevealedSet = levelState.lastRevealedDiamonds.toSet()

                for ((box, owner) in levelState.boxOwners) {
                    val topLeft = dotOffset(box.row, box.col)
                    val boxColor = if (owner == PlayerSlot.PLAYER_1) p1Color else p2Color
                    val pattern = if (owner == PlayerSlot.PLAYER_1) p1Pattern else p2Pattern
                    val boxSkin = if (owner == PlayerSlot.PLAYER_1) p1BoxSkin else p2BoxSkin
                    val letterFont = if (owner == PlayerSlot.PLAYER_1) p1LetterFont else p2LetterFont
                    val boxDrawTopLeft = Offset(topLeft.x + boxInset, topLeft.y + boxInset)
                    val boxDrawSize = Size(
                        width = (step - boxInset * 2f).coerceAtLeast(1f),
                        height = (step - boxInset * 2f).coerceAtLeast(1f),
                    )
                    val currentScale = if (box in lastClaimedSet) stampScale.value else 1f
                    val center = Offset(
                        boxDrawTopLeft.x + boxDrawSize.width / 2f,
                        boxDrawTopLeft.y + boxDrawSize.height / 2f,
                    )

                    scale(scale = currentScale, pivot = center) {
                        drawBoxSkinDetail(
                            boxSkin = boxSkin,
                            topLeft = boxDrawTopLeft,
                            boxSize = boxDrawSize,
                            baseColor = boxColor,
                            pattern = pattern,
                            ink = tokens.ink,
                            highlight = tokens.warmHighlight,
                            honey = tokens.brandHoney,
                            woodDark = tokens.woodDark,
                        )

                        val initials = (if (owner == PlayerSlot.PLAYER_1) p1Initials else p2Initials)
                            .take(1)
                            .uppercase()
                        val baseFontStyle = letterFontTextStyle(letterFont, initialFontSize)
                        val strokeWidthPx = when (letterFont) {
                            LetterFontId.STICKER_OUTLINE -> 5.5.dp.toPx()
                            else -> 4.dp.toPx()
                        }
                        val strokeLayout = textMeasurer.measure(
                            text = initials,
                            style = baseFontStyle.copy(
                                color = tokens.ink,
                                drawStyle = Stroke(width = strokeWidthPx, join = StrokeJoin.Round),
                            ),
                        )
                        val fillLayout = textMeasurer.measure(
                            text = initials,
                            style = baseFontStyle.copy(
                                color = tokens.warmHighlight,
                            ),
                        )
                        val textTopLeft = Offset(
                            x = topLeft.x + (step - fillLayout.size.width) / 2f,
                            y = topLeft.y + (step - fillLayout.size.height) / 2f,
                        )
                        if (letterFont == LetterFontId.SHADOW_RIBBON) {
                            val ribbonOffset = Offset(2.5.dp.toPx(), 2.5.dp.toPx())
                            drawText(
                                textLayoutResult = textMeasurer.measure(
                                    text = initials,
                                    style = baseFontStyle.copy(color = tokens.brandHoney),
                                ),
                                topLeft = textTopLeft + ribbonOffset,
                            )
                        }
                        drawText(textLayoutResult = strokeLayout, topLeft = textTopLeft)
                        drawText(textLayoutResult = fillLayout, topLeft = textTopLeft)

                        // Revealed hidden diamond icon in bottom-right corner of claimed box (Addendum §2.2)
                        if (box in levelState.revealedDiamonds) {
                            val diaRadius = (boxDrawSize.minDimension * 0.16f).coerceIn(5.dp.toPx(), 10.dp.toPx())
                            val diaCenter = Offset(
                                x = boxDrawTopLeft.x + boxDrawSize.width - diaRadius * 1.45f,
                                y = boxDrawTopLeft.y + boxDrawSize.height - diaRadius * 1.45f,
                            )
                            drawCutDiamond(
                                center = diaCenter,
                                radius = diaRadius,
                                teal = tokens.diamondTeal,
                                tealLight = tokens.diamondTealLight,
                                tealDark = tokens.diamondTealDark,
                                ink = tokens.ink,
                                strokeWidth = 1.4.dp.toPx(),
                            )
                        }

                        // 200ms 3-star sparkle pop (Addendum §2.2 / DRD §8.4 — 3 crisp 4-point stars, no particles)
                        if (box in lastRevealedSet && !reducedMotion && sparkleProgress.value < 1f) {
                            drawThreeStarSparklePop(
                                center = center,
                                spread = boxDrawSize.minDimension * 0.42f * sparkleProgress.value,
                                alpha = (1f - sparkleProgress.value * 0.65f).coerceIn(0f, 1f),
                                teal = tokens.diamondTealLight,
                                honey = tokens.brandHoney,
                                ink = tokens.ink,
                            )
                        } else if (box in lastClaimedSet && !reducedMotion) {
                            drawThreeStarAccent(
                                topRight = Offset(boxDrawTopLeft.x + boxDrawSize.width, boxDrawTopLeft.y),
                                color = tokens.brandHoney,
                                ink = tokens.ink,
                            )
                        }
                    }
                }

                // 3. Undrawn lines as faint dotted pencil marks
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 6.dp.toPx()), 0f)
                val idleStroke = 1.6.dp.toPx()
                for (r in 0 until dotCount) {
                    for (c in 0 until boxesPerSide) {
                        val line = LineCoord(LineKind.HORIZONTAL, r, c)
                        if (!levelState.isLineDrawn(line)) {
                            drawLine(
                                color = tokens.paperLine.copy(alpha = 0.72f),
                                start = dotOffset(r, c),
                                end = dotOffset(r, c + 1),
                                strokeWidth = idleStroke,
                                pathEffect = dashEffect,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }
                for (r in 0 until boxesPerSide) {
                    for (c in 0 until dotCount) {
                        val line = LineCoord(LineKind.VERTICAL, r, c)
                        if (!levelState.isLineDrawn(line)) {
                            drawLine(
                                color = tokens.paperLine.copy(alpha = 0.72f),
                                start = dotOffset(r, c),
                                end = dotOffset(r + 1, c),
                                strokeWidth = idleStroke,
                                pathEffect = dashEffect,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }

                // 4. Drawn pencil lines with seeded hand-drawn wobble + 150ms draw-in (DRD §8.3)
                val drawnStroke = pencilWidthDp.toPx()
                val wobblePx = 0.8.dp.toPx()
                val lastMove = levelState.lastMove

                for (pos in levelState.horizontalLines) {
                    val coord = LineCoord(LineKind.HORIZONTAL, pos.row, pos.col)
                    val start = dotOffset(pos.row, pos.col)
                    val end = dotOffset(pos.row, pos.col + 1)
                    val progress = if (coord == lastMove) drawProgress.value else 1f
                    drawWobbledPencilLine(
                        start = start,
                        end = end,
                        progress = progress,
                        seed = coord.hashCode(),
                        wobblePx = wobblePx,
                        strokeWidth = drawnStroke,
                        color = tokens.ink,
                    )
                }
                for (pos in levelState.verticalLines) {
                    val coord = LineCoord(LineKind.VERTICAL, pos.row, pos.col)
                    val start = dotOffset(pos.row, pos.col)
                    val end = dotOffset(pos.row + 1, pos.col)
                    val progress = if (coord == lastMove) drawProgress.value else 1f
                    drawWobbledPencilLine(
                        start = start,
                        end = end,
                        progress = progress,
                        seed = coord.hashCode(),
                        wobblePx = wobblePx,
                        strokeWidth = drawnStroke,
                        color = tokens.ink,
                    )
                }

                // Last line midpoint highlight dot in brand.honey (DRD §8.3)
                if (lastMove != null && drawProgress.value > 0.85f) {
                    val mid = when (lastMove.kind) {
                        LineKind.HORIZONTAL -> {
                            val a = dotOffset(lastMove.row, lastMove.col)
                            val b = dotOffset(lastMove.row, lastMove.col + 1)
                            Offset((a.x + b.x) / 2f, a.y)
                        }
                        LineKind.VERTICAL -> {
                            val a = dotOffset(lastMove.row, lastMove.col)
                            val b = dotOffset(lastMove.row + 1, lastMove.col)
                            Offset(a.x, (a.y + b.y) / 2f)
                        }
                    }
                    drawCircle(color = tokens.brandHoney, radius = 4.5.dp.toPx(), center = mid)
                    drawCircle(color = tokens.ink, radius = 4.5.dp.toPx(), center = mid, style = Stroke(1.8.dp.toPx()))
                }

                // 5. Wooden peg dots rendered above lines (DRD §8.2)
                val pegRadius = (pegDiameterDp / 2f).toPx()
                val pegOutline = 2.dp.toPx()
                for (r in 0 until dotCount) {
                    for (c in 0 until dotCount) {
                        val center = dotOffset(r, c)
                        drawCircle(
                            color = tokens.woodDark,
                            radius = pegRadius,
                            center = center,
                        )
                        drawCircle(
                            color = tokens.ink,
                            radius = pegRadius,
                            center = center,
                            style = Stroke(width = pegOutline),
                        )
                        drawCircle(
                            color = tokens.warmHighlight.copy(alpha = 0.85f),
                            radius = pegRadius * 0.28f,
                            center = Offset(center.x - pegRadius * 0.30f, center.y - pegRadius * 0.30f),
                        )
                    }
                }
            }

            // Corner masking tape pieces for "pinned paper" feel (DRD §8.1)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-6).dp, y = 10.dp)
                    .rotate(-35f)
                    .size(width = 34.dp, height = 12.dp)
                    .background(tokens.paperShade.copy(alpha = 0.92f))
                    .border(1.5.dp, tokens.ink.copy(alpha = 0.65f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = (-10).dp)
                    .rotate(-35f)
                    .size(width = 34.dp, height = 12.dp)
                    .background(tokens.paperShade.copy(alpha = 0.92f))
                    .border(1.5.dp, tokens.ink.copy(alpha = 0.65f)),
            )
        }
    }
}

private fun DrawScope.drawWobbledPencilLine(
    start: Offset,
    end: Offset,
    progress: Float,
    seed: Int,
    wobblePx: Float,
    strokeWidth: Float,
    color: Color,
) {
    val steps = 8
    val maxSteps = (steps * progress).coerceAtLeast(0.1f)
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
    val nx = -dy / len
    val ny = dx / len
    val phase = ((seed % 17) / 17f) * (2f * PI.toFloat())

    val path = Path().apply {
        moveTo(start.x, start.y)
        val whole = maxSteps.toInt()
        for (i in 1..whole) {
            val t = i / steps.toFloat()
            val envelope = sin(t * PI.toFloat())
            val offset = sin(t * 2f * PI.toFloat() + phase) * wobblePx * envelope
            lineTo(start.x + dx * t + nx * offset, start.y + dy * t + ny * offset)
        }
        if (progress > whole / steps.toFloat()) {
            val t = progress
            val envelope = sin(t * PI.toFloat())
            val offset = sin(t * 2f * PI.toFloat() + phase) * wobblePx * envelope
            lineTo(start.x + dx * t + nx * offset, start.y + dy * t + ny * offset)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

private fun DrawScope.drawThreeStarAccent(
    topRight: Offset,
    color: Color,
    ink: Color,
) {
    val points = listOf(
        Offset(topRight.x - 4.dp.toPx(), topRight.y + 4.dp.toPx()) to 4.dp.toPx(),
        Offset(topRight.x - 12.dp.toPx(), topRight.y + 7.dp.toPx()) to 2.8.dp.toPx(),
        Offset(topRight.x - 7.dp.toPx(), topRight.y + 13.dp.toPx()) to 2.5.dp.toPx(),
    )
    points.forEach { (center, r) ->
        drawCircle(color = color, radius = r, center = center)
        drawCircle(color = ink, radius = r, center = center, style = Stroke(1.2.dp.toPx()))
    }
}

private fun DrawScope.drawThreeStarSparklePop(
    center: Offset,
    spread: Float,
    alpha: Float,
    teal: Color,
    honey: Color,
    ink: Color,
) {
    val stars = listOf(
        Triple(Offset(center.x - spread * 0.72f, center.y - spread * 0.62f), 6.5.dp.toPx(), teal),
        Triple(Offset(center.x + spread * 0.76f, center.y - spread * 0.45f), 5.5.dp.toPx(), honey),
        Triple(Offset(center.x + spread * 0.15f, center.y + spread * 0.72f), 5.0.dp.toPx(), teal),
    )
    stars.forEach { (pt, r, fill) ->
        val star = Path().apply {
            for (i in 0 until 8) {
                val rad = if (i % 2 == 0) r else r * 0.34f
                val ang = (i * 45.0 - 90.0) * PI / 180.0
                val px = pt.x + (rad * cos(ang)).toFloat()
                val py = pt.y + (rad * sin(ang)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(star, color = fill.copy(alpha = alpha))
        drawPath(star, color = ink.copy(alpha = alpha), style = Stroke(1.3.dp.toPx(), join = StrokeJoin.Round))
    }
}

private fun findClosestUndrawnLine(
    tap: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    gridPad: Float,
    level: LevelState,
): LineCoord? {
    val dotCount = level.dotCount
    val boxesPerSide = dotCount - 1
    val side = min(canvasWidth, canvasHeight)
    val usable = (side - gridPad * 2f).coerceAtLeast(1f)
    val step = usable / boxesPerSide.toFloat()
    val maxHitDistance = maxOf(step * 0.45f, 18f)

    var bestLine: LineCoord? = null
    var bestDistance = Float.MAX_VALUE

    for (r in 0 until dotCount) {
        for (c in 0 until boxesPerSide) {
            val candidate = LineCoord(LineKind.HORIZONTAL, r, c)
            if (level.isLineDrawn(candidate)) continue
            val y = gridPad + r * step
            val x1 = gridPad + c * step
            val x2 = gridPad + (c + 1) * step
            val dist = distancePointToSegment(tap, Offset(x1, y), Offset(x2, y))
            if (dist < maxHitDistance && dist < bestDistance) {
                bestDistance = dist
                bestLine = candidate
            }
        }
    }

    for (r in 0 until boxesPerSide) {
        for (c in 0 until dotCount) {
            val candidate = LineCoord(LineKind.VERTICAL, r, c)
            if (level.isLineDrawn(candidate)) continue
            val x = gridPad + c * step
            val y1 = gridPad + r * step
            val y2 = gridPad + (r + 1) * step
            val dist = distancePointToSegment(tap, Offset(x, y1), Offset(x, y2))
            if (dist < maxHitDistance && dist < bestDistance) {
                bestDistance = dist
                bestLine = candidate
            }
        }
    }

    return bestLine
}

private fun distancePointToSegment(p: Offset, a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return abs(p.x - a.x) + abs(p.y - a.y)
    val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq).coerceIn(0f, 1f)
    val projX = a.x + t * dx
    val projY = a.y + t * dy
    val diffX = p.x - projX
    val diffY = p.y - projY
    return kotlin.math.sqrt(diffX * diffX + diffY * diffY)
}
