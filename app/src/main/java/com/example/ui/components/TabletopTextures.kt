package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BoxSkinId
import com.example.model.PlayerPattern
import com.example.ui.theme.LilitaOneFontFamily
import com.example.ui.theme.StudioTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Warm paper background with subtle static fibers and optional faint crease lines (DRD §4.1).
 */
@Composable
fun PaperBackground(
    modifier: Modifier = Modifier,
    showCreaseLines: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = StudioTheme.tokens
    Box(
        modifier = modifier
            .background(tokens.paperCream)
            .drawBehind {
                drawPaperTexture(
                    lineColor = tokens.paperLine,
                    shadeColor = tokens.paperShade,
                    showCreaseLines = showCreaseLines,
                )
            },
        content = content,
    )
}

/**
 * Warm wood tabletop background with vertical wood grain planks and occasional knot (DRD §4.2).
 */
@Composable
fun WoodBackground(
    modifier: Modifier = Modifier,
    showKnot: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = StudioTheme.tokens
    Box(
        modifier = modifier
            .background(tokens.woodMid)
            .drawBehind {
                drawWoodTexture(
                    woodLight = tokens.woodLight,
                    woodMid = tokens.woodMid,
                    woodDark = tokens.woodDark,
                    showKnot = showKnot,
                )
            },
        content = content,
    )
}

/**
 * Felt green surface with fine static speckle grain (DRD §4.3).
 */
@Composable
fun FeltBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = StudioTheme.tokens
    Box(
        modifier = modifier
            .background(tokens.feltGreen)
            .drawBehind {
                val rng = Random(4207)
                val count = ((size.width * size.height) / 2600f).toInt().coerceIn(80, 420)
                for (i in 0 until count) {
                    val x = rng.nextFloat() * size.width
                    val y = rng.nextFloat() * size.height
                    val dark = i % 2 == 0
                    drawCircle(
                        color = if (dark) tokens.feltDark.copy(alpha = 0.24f) else tokens.warmHighlight.copy(alpha = 0.08f),
                        radius = (1f + rng.nextFloat() * 1.3f).dp.toPx(),
                        center = Offset(x, y),
                    )
                }
            },
        content = content,
    )
}

fun DrawScope.drawPaperTexture(
    lineColor: Color,
    shadeColor: Color,
    showCreaseLines: Boolean = false,
) {
    val rng = Random(1937)
    val fiberCount = ((size.width * size.height) / 4800f).toInt().coerceIn(45, 220)
    for (i in 0 until fiberCount) {
        val x = rng.nextFloat() * size.width
        val y = rng.nextFloat() * size.height
        val len = (4f + rng.nextFloat() * 10f).dp.toPx()
        val angle = rng.nextFloat() * 6.28f
        drawLine(
            color = lineColor.copy(alpha = 0.14f),
            start = Offset(x, y),
            end = Offset(x + cos(angle) * len, y + sin(angle) * len),
            strokeWidth = 0.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
    val speckCount = ((size.width * size.height) / 7000f).toInt().coerceIn(25, 130)
    for (i in 0 until speckCount) {
        drawCircle(
            color = shadeColor.copy(alpha = 0.55f),
            radius = (0.8f + rng.nextFloat() * 1.1f).dp.toPx(),
            center = Offset(rng.nextFloat() * size.width, rng.nextFloat() * size.height),
        )
    }
    if (showCreaseLines) {
        drawLine(
            color = lineColor.copy(alpha = 0.18f),
            start = Offset(0f, size.height * 0.34f),
            end = Offset(size.width, size.height * 0.36f),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = lineColor.copy(alpha = 0.14f),
            start = Offset(size.width * 0.52f, 0f),
            end = Offset(size.width * 0.50f, size.height),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

fun DrawScope.drawWoodTexture(
    woodLight: Color,
    woodMid: Color,
    woodDark: Color,
    showKnot: Boolean = false,
) {
    val plankWidth = 64.dp.toPx()
    var x = 0f
    var plankIdx = 0
    while (x < size.width) {
        val w = plankWidth.coerceAtMost(size.width - x)
        val tint = when (plankIdx % 3) {
            0 -> woodMid
            1 -> woodLight.copy(alpha = 0.28f)
            else -> woodDark.copy(alpha = 0.16f)
        }
        if (plankIdx % 3 != 0) {
            drawRect(color = tint, topLeft = Offset(x, 0f), size = Size(w, size.height))
        }
        // Plank seam
        if (x > 0f) {
            drawLine(
                color = woodDark.copy(alpha = 0.35f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        // Subtle vertical grain lines inside each plank
        val grainStep = 10.dp.toPx()
        var gx = x + 6.dp.toPx()
        var gIdx = 0
        while (gx < x + w - 4.dp.toPx()) {
            drawLine(
                color = woodDark.copy(alpha = if (gIdx % 2 == 0) 0.14f else 0.08f),
                start = Offset(gx, 0f),
                end = Offset(gx + (if (gIdx % 2 == 0) 2.dp.toPx() else (-1.5).dp.toPx()), size.height),
                strokeWidth = 1.dp.toPx(),
            )
            gx += grainStep
            gIdx++
        }
        x += plankWidth
        plankIdx++
    }

    if (showKnot && size.width > 180.dp.toPx() && size.height > 180.dp.toPx()) {
        val knotCenter = Offset(size.width * 0.82f, size.height * 0.24f)
        drawOval(
            color = woodDark.copy(alpha = 0.22f),
            topLeft = Offset(knotCenter.x - 14.dp.toPx(), knotCenter.y - 22.dp.toPx()),
            size = Size(28.dp.toPx(), 44.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawOval(
            color = woodDark.copy(alpha = 0.26f),
            topLeft = Offset(knotCenter.x - 6.dp.toPx(), knotCenter.y - 10.dp.toPx()),
            size = Size(12.dp.toPx(), 20.dp.toPx()),
        )
    }
}

/**
 * Draws the accessibility pattern inside a box or swatch (DRD §3.3 & §11).
 */
fun DrawScope.drawPlayerPattern(
    pattern: PlayerPattern,
    topLeft: Offset,
    boxSize: Size,
    tint: Color,
) {
    clipRect(
        left = topLeft.x,
        top = topLeft.y,
        right = topLeft.x + boxSize.width,
        bottom = topLeft.y + boxSize.height,
    ) {
        val patternColor = tint.copy(alpha = 0.26f)
        when (pattern) {
            PlayerPattern.STRIPES -> {
                val step = 8.dp.toPx()
                var offset = -boxSize.height
                while (offset < boxSize.width + boxSize.height) {
                    drawLine(
                        color = patternColor,
                        start = Offset(topLeft.x + offset, topLeft.y + boxSize.height),
                        end = Offset(topLeft.x + offset + boxSize.height, topLeft.y),
                        strokeWidth = 2.2.dp.toPx(),
                    )
                    offset += step
                }
            }
            PlayerPattern.DOTS -> {
                val step = 9.dp.toPx()
                var y = topLeft.y + 4.dp.toPx()
                var row = 0
                while (y < topLeft.y + boxSize.height) {
                    var x = topLeft.x + (if (row % 2 == 0) 4.dp.toPx() else 8.5.dp.toPx())
                    while (x < topLeft.x + boxSize.width) {
                        drawCircle(
                            color = patternColor,
                            radius = 1.6.dp.toPx(),
                            center = Offset(x, y),
                        )
                        x += step
                    }
                    y += step
                    row++
                }
            }
            PlayerPattern.STARS -> {
                val step = 13.dp.toPx()
                var y = topLeft.y + 6.dp.toPx()
                var row = 0
                while (y < topLeft.y + boxSize.height) {
                    var x = topLeft.x + (if (row % 2 == 0) 6.dp.toPx() else 12.dp.toPx())
                    while (x < topLeft.x + boxSize.width) {
                        drawMiniStar(Offset(x, y), 3.2.dp.toPx(), patternColor)
                        x += step
                    }
                    y += step
                    row++
                }
            }
            PlayerPattern.CHECKS -> {
                val cell = 8.dp.toPx()
                var y = topLeft.y
                var row = 0
                while (y < topLeft.y + boxSize.height) {
                    var x = topLeft.x
                    var col = 0
                    while (x < topLeft.x + boxSize.width) {
                        if ((row + col) % 2 == 0) {
                            drawRect(
                                color = patternColor,
                                topLeft = Offset(x, y),
                                size = Size(cell, cell),
                            )
                        }
                        x += cell
                        col++
                    }
                    y += cell
                    row++
                }
            }
        }
    }
}

private fun DrawScope.drawMiniStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    for (i in 0 until 8) {
        val r = if (i % 2 == 0) radius else radius * 0.42f
        val angle = (i * 45.0 - 90.0) * Math.PI / 180.0
        val px = center.x + (r * cos(angle)).toFloat()
        val py = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color = color)
}

/**
 * Chunky Lilita One title with ink stroke and hard 0-blur offset shadow (DRD §5.3).
 */
@Composable
fun ChunkyDisplayText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current.copy(fontFamily = LilitaOneFontFamily),
    fillColor: Color? = null,
    shadowColor: Color? = null,
    outlineColor: Color? = null,
    strokeWidth: Dp = 3.dp,
    shadowOffset: Dp = 3.dp,
) {
    val tokens = StudioTheme.tokens
    val effectiveFill = fillColor ?: tokens.paperCream
    val effectiveShadow = shadowColor ?: tokens.brandHoney
    val effectiveOutline = outlineColor ?: tokens.ink

    Box(modifier = modifier) {
        // Hard 0-blur offset shadow
        Text(
            text = text,
            style = style,
            color = effectiveShadow,
            modifier = Modifier
                .offset(x = shadowOffset, y = shadowOffset)
                .clearAndSetSemantics {},
        )
        // Outer stroke
        Text(
            text = text,
            style = style.copy(
                drawStyle = Stroke(
                    width = strokeWidth.value * 2.4f,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round,
                ),
            ),
            color = effectiveOutline,
            modifier = Modifier.clearAndSetSemantics {},
        )
        // Crisp inner fill
        Text(
            text = text,
            style = style,
            color = effectiveFill,
        )
    }
}

/**
 * Hard-offset shadow wrapper box for cards & pill containers (DRD §6.3).
 */
@Composable
fun HardShadowBox(
    cornerRadius: Dp = 24.dp,
    shadowOffset: Dp = 4.dp,
    borderWidth: Dp = 3.dp,
    fillColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = StudioTheme.tokens
    val shape = RoundedCornerShape(cornerRadius)
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(tokens.ink),
        )
        Box(
            modifier = Modifier
                .clip(shape)
                .background(fillColor)
                .border(borderWidth, tokens.ink, shape),
            content = content,
        )
    }
}

/**
 * Teal-blue cut-gem diamond Canvas icon (Addendum §3.1 — vector only, zero emoji).
 */
@Composable
fun DiamondIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
) {
    val tokens = StudioTheme.tokens
    Canvas(modifier = modifier.size(size)) {
        drawCutDiamond(
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            radius = this.size.minDimension * 0.44f,
            teal = tokens.diamondTeal,
            tealLight = tokens.diamondTealLight,
            tealDark = tokens.diamondTealDark,
            ink = tokens.ink,
            strokeWidth = (size.value * 0.09f).coerceIn(1.3f, 2.2f).dp.toPx(),
        )
    }
}

fun DrawScope.drawCutDiamond(
    center: Offset,
    radius: Float,
    teal: Color,
    tealLight: Color,
    tealDark: Color,
    ink: Color,
    strokeWidth: Float = 2.dp.toPx(),
) {
    val topY = center.y - radius * 0.85f
    val beltY = center.y - radius * 0.20f
    val bottomY = center.y + radius * 0.95f
    val topHalfW = radius * 0.62f
    val beltHalfW = radius * 1.02f

    val outer = Path().apply {
        moveTo(center.x - topHalfW, topY)
        lineTo(center.x + topHalfW, topY)
        lineTo(center.x + beltHalfW, beltY)
        lineTo(center.x, bottomY)
        lineTo(center.x - beltHalfW, beltY)
        close()
    }
    drawPath(outer, color = teal)

    // Top crown bright facet
    val crown = Path().apply {
        moveTo(center.x - topHalfW, topY)
        lineTo(center.x + topHalfW, topY)
        lineTo(center.x + beltHalfW * 0.55f, beltY)
        lineTo(center.x - beltHalfW * 0.55f, beltY)
        close()
    }
    drawPath(crown, color = tealLight)

    // Right lower shaded facet
    val rightFacet = Path().apply {
        moveTo(center.x + beltHalfW * 0.35f, beltY)
        lineTo(center.x + beltHalfW, beltY)
        lineTo(center.x, bottomY)
        close()
    }
    drawPath(rightFacet, color = tealDark)

    // Facet lines
    drawLine(ink.copy(alpha = 0.45f), Offset(center.x - beltHalfW, beltY), Offset(center.x + beltHalfW, beltY), strokeWidth * 0.6f)
    drawLine(ink.copy(alpha = 0.45f), Offset(center.x - topHalfW * 0.55f, beltY), Offset(center.x, bottomY), strokeWidth * 0.6f)
    drawLine(ink.copy(alpha = 0.45f), Offset(center.x + topHalfW * 0.55f, beltY), Offset(center.x, bottomY), strokeWidth * 0.6f)

    drawPath(outer, color = ink, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
}

/**
 * Draws a cosmetic box skin fill & border detail inside a claimed box or cosmetic preview (Addendum §5.1).
 */
fun DrawScope.drawBoxSkinDetail(
    boxSkin: BoxSkinId,
    topLeft: Offset,
    boxSize: Size,
    baseColor: Color,
    pattern: PlayerPattern,
    ink: Color,
    highlight: Color,
    honey: Color,
    woodDark: Color,
) {
    val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    drawRoundRect(
        color = baseColor,
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = corner,
    )
    clipRect(
        left = topLeft.x,
        top = topLeft.y,
        right = topLeft.x + boxSize.width,
        bottom = topLeft.y + boxSize.height,
    ) {
        when (boxSkin) {
            BoxSkinId.CLASSIC_PAPER -> {
                drawPlayerPattern(pattern, topLeft, boxSize, ink)
            }
            BoxSkinId.WOODEN_TILE -> {
                val plankStep = (boxSize.height / 3f).coerceAtLeast(4f)
                var y = topLeft.y + plankStep
                while (y < topLeft.y + boxSize.height - 2f) {
                    drawLine(
                        color = woodDark.copy(alpha = 0.45f),
                        start = Offset(topLeft.x, y),
                        end = Offset(topLeft.x + boxSize.width, y),
                        strokeWidth = 1.6.dp.toPx(),
                    )
                    y += plankStep
                }
                drawLine(
                    color = highlight.copy(alpha = 0.35f),
                    start = Offset(topLeft.x + 4.dp.toPx(), topLeft.y + 4.dp.toPx()),
                    end = Offset(topLeft.x + boxSize.width - 4.dp.toPx(), topLeft.y + 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            BoxSkinId.PAPER_STAMP -> {
                val inset = 3.5.dp.toPx()
                drawRect(
                    color = highlight.copy(alpha = 0.32f),
                    topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
                    size = Size((boxSize.width - inset * 2f).coerceAtLeast(1f), (boxSize.height - inset * 2f).coerceAtLeast(1f)),
                )
                val step = 5.dp.toPx()
                var px = topLeft.x + 4.dp.toPx()
                while (px < topLeft.x + boxSize.width - 2.dp.toPx()) {
                    drawCircle(highlight.copy(alpha = 0.85f), 1.4.dp.toPx(), Offset(px, topLeft.y + 1.5.dp.toPx()))
                    drawCircle(highlight.copy(alpha = 0.85f), 1.4.dp.toPx(), Offset(px, topLeft.y + boxSize.height - 1.5.dp.toPx()))
                    px += step
                }
            }
            BoxSkinId.WOVEN_MAT -> {
                val step = 7.dp.toPx()
                var x = topLeft.x + 3.dp.toPx()
                while (x < topLeft.x + boxSize.width) {
                    drawLine(
                        color = ink.copy(alpha = 0.22f),
                        start = Offset(x, topLeft.y),
                        end = Offset(x, topLeft.y + boxSize.height),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                    x += step
                }
                var y = topLeft.y + 3.dp.toPx()
                while (y < topLeft.y + boxSize.height) {
                    drawLine(
                        color = highlight.copy(alpha = 0.32f),
                        start = Offset(topLeft.x, y),
                        end = Offset(topLeft.x + boxSize.width, y),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                    y += step
                }
            }
            BoxSkinId.CANDY_WRAPPER -> {
                val step = 11.dp.toPx()
                var offset = -boxSize.height
                while (offset < boxSize.width + boxSize.height) {
                    drawLine(
                        color = highlight.copy(alpha = 0.42f),
                        start = Offset(topLeft.x + offset, topLeft.y + boxSize.height),
                        end = Offset(topLeft.x + offset + boxSize.height, topLeft.y),
                        strokeWidth = 4.dp.toPx(),
                    )
                    offset += step
                }
            }
            BoxSkinId.PIXEL_BLOCK -> {
                val bevel = 4.dp.toPx()
                drawRect(
                    color = highlight.copy(alpha = 0.48f),
                    topLeft = topLeft,
                    size = Size(boxSize.width, bevel),
                )
                drawRect(
                    color = highlight.copy(alpha = 0.48f),
                    topLeft = topLeft,
                    size = Size(bevel, boxSize.height),
                )
                drawRect(
                    color = ink.copy(alpha = 0.26f),
                    topLeft = Offset(topLeft.x, topLeft.y + boxSize.height - bevel),
                    size = Size(boxSize.width, bevel),
                )
                drawRect(
                    color = ink.copy(alpha = 0.26f),
                    topLeft = Offset(topLeft.x + boxSize.width - bevel, topLeft.y),
                    size = Size(bevel, boxSize.height),
                )
            }
            BoxSkinId.ROYAL_ENAMEL -> {
                val inset = 4.dp.toPx()
                drawRoundRect(
                    color = honey,
                    topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
                    size = Size((boxSize.width - inset * 2f).coerceAtLeast(1f), (boxSize.height - inset * 2f).coerceAtLeast(1f)),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = 2.4.dp.toPx()),
                )
                drawPlayerPattern(PlayerPattern.STARS, topLeft, boxSize, highlight)
            }
        }
    }
    drawRoundRect(
        color = ink,
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = corner,
        style = Stroke(width = if (boxSkin == BoxSkinId.ROYAL_ENAMEL) 2.5.dp.toPx() else 2.dp.toPx()),
    )
}

/**
 * Cute tabletop storefront icon with scalloped berry-and-cream awning, honey door, and teal window.
 */
@Composable
fun CuteStoreIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.085f).coerceAtLeast(1.8.dp.toPx())

        // Shop body
        val wallLeft = w * 0.16f
        val wallTop = h * 0.42f
        val wallW = w * 0.68f
        val wallH = h * 0.46f
        drawRoundRect(
            color = tokens.warmHighlight,
            topLeft = Offset(wallLeft, wallTop),
            size = Size(wallW, wallH),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
        )
        drawRoundRect(
            color = tokens.ink,
            topLeft = Offset(wallLeft, wallTop),
            size = Size(wallW, wallH),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(width = stroke),
        )

        // Honey door on left
        val doorW = wallW * 0.34f
        val doorH = wallH * 0.66f
        val doorLeft = wallLeft + wallW * 0.14f
        val doorTop = wallTop + wallH - doorH
        drawRoundRect(
            color = tokens.brandHoney,
            topLeft = Offset(doorLeft, doorTop),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
        )
        drawRoundRect(
            color = tokens.ink,
            topLeft = Offset(doorLeft, doorTop),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
            style = Stroke(width = stroke * 0.85f),
        )

        // Teal shop window on right
        val winSize = wallW * 0.28f
        val winLeft = wallLeft + wallW * 0.58f
        val winTop = wallTop + wallH * 0.26f
        drawRoundRect(
            color = tokens.diamondTealLight,
            topLeft = Offset(winLeft, winTop),
            size = Size(winSize, winSize),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
        )
        drawRoundRect(
            color = tokens.ink,
            topLeft = Offset(winLeft, winTop),
            size = Size(winSize, winSize),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
            style = Stroke(width = stroke * 0.8f),
        )

        // Slanted awning top
        val awningLeft = w * 0.08f
        val awningRight = w * 0.92f
        val awningTop = h * 0.14f
        val awningBottom = h * 0.44f
        val awningPath = Path().apply {
            moveTo(w * 0.16f, awningTop)
            lineTo(w * 0.84f, awningTop)
            lineTo(awningRight, awningBottom)
            lineTo(awningLeft, awningBottom)
            close()
        }
        drawPath(path = awningPath, color = tokens.brandBerry)

        // Alternating cream awning stripes + cute scallops
        val stripeCount = 4
        val awningSpan = awningRight - awningLeft
        val stripeW = awningSpan / stripeCount
        for (i in 0 until stripeCount) {
            val sx = awningLeft + i * stripeW
            if (i % 2 == 1) {
                val topLeftX = w * 0.16f + (w * 0.68f) * (i.toFloat() / stripeCount)
                val topRightX = w * 0.16f + (w * 0.68f) * ((i + 1).toFloat() / stripeCount)
                val stripePath = Path().apply {
                    moveTo(topLeftX, awningTop)
                    lineTo(topRightX, awningTop)
                    lineTo(sx + stripeW, awningBottom)
                    lineTo(sx, awningBottom)
                    close()
                }
                drawPath(path = stripePath, color = tokens.warmHighlight)
            }
            val scallopCenter = Offset(sx + stripeW / 2f, awningBottom)
            drawCircle(
                color = if (i % 2 == 0) tokens.brandBerry else tokens.warmHighlight,
                radius = stripeW * 0.48f,
                center = scallopCenter,
            )
            drawCircle(
                color = tokens.ink,
                radius = stripeW * 0.48f,
                center = scallopCenter,
                style = Stroke(width = stroke * 0.8f),
            )
        }
        drawPath(
            path = awningPath,
            color = tokens.ink,
            style = Stroke(width = stroke, join = StrokeJoin.Round),
        )
    }
}

/**
 * Cute cozy cottage icon for the Home navigation button, matching CuteStoreIcon's illustrated style.
 */
@Composable
fun CuteHomeIcon(
    size: Dp = 26.dp,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.085f).coerceAtLeast(1.6.dp.toPx())

        // Tiny chimney on right roof slope
        drawRoundRect(
            color = tokens.brandHoney,
            topLeft = Offset(w * 0.66f, h * 0.16f),
            size = Size(w * 0.14f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
        )
        drawRoundRect(
            color = tokens.ink,
            topLeft = Offset(w * 0.66f, h * 0.16f),
            size = Size(w * 0.14f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
            style = Stroke(width = stroke * 0.85f),
        )

        // Cream cottage body
        val bodyLeft = w * 0.20f
        val bodyTop = h * 0.44f
        val bodyW = w * 0.60f
        val bodyH = h * 0.42f
        drawRoundRect(
            color = tokens.warmHighlight,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
        )
        drawRoundRect(
            color = tokens.ink,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
            style = Stroke(width = stroke),
        )

        // Honey arched door
        val doorW = bodyW * 0.36f
        val doorH = bodyH * 0.60f
        val doorLeft = w * 0.50f - doorW * 0.5f
        val doorTop = bodyTop + bodyH - doorH
        drawRoundRect(
            color = tokens.brandHoney,
            topLeft = Offset(doorLeft, doorTop),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(doorW * 0.45f, doorW * 0.45f),
        )
        drawRoundRect(
            color = tokens.ink,
            topLeft = Offset(doorLeft, doorTop),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(doorW * 0.45f, doorW * 0.45f),
            style = Stroke(width = stroke * 0.85f),
        )

        // Berry pitched roof
        val roofPath = Path().apply {
            moveTo(w * 0.50f, h * 0.14f)
            lineTo(w * 0.88f, h * 0.46f)
            lineTo(w * 0.12f, h * 0.46f)
            close()
        }
        drawPath(path = roofPath, color = tokens.brandBerry)
        drawPath(
            path = roofPath,
            color = tokens.ink,
            style = Stroke(width = stroke, join = StrokeJoin.Round),
        )

        // Tiny sky attic window in roof gable
        drawCircle(
            color = tokens.diamondTealLight,
            radius = w * 0.075f,
            center = Offset(w * 0.50f, h * 0.35f),
        )
        drawCircle(
            color = tokens.ink,
            radius = w * 0.075f,
            center = Offset(w * 0.50f, h * 0.35f),
            style = Stroke(width = stroke * 0.75f),
        )
    }
}

/**
 * Cute twin teddy buddies icon for the Friends navigation button.
 */
@Composable
fun CuteFriendsIcon(
    size: Dp = 26.dp,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.08f).coerceAtLeast(1.5.dp.toPx())

        fun drawMiniBuddy(
            cx: Float,
            cy: Float,
            headR: Float,
            furColor: Color,
            cheekColor: Color,
        ) {
            val earR = headR * 0.42f
            listOf(
                Offset(cx - headR * 0.72f, cy - headR * 0.68f),
                Offset(cx + headR * 0.72f, cy - headR * 0.68f),
            ).forEach { ear ->
                drawCircle(color = furColor, radius = earR, center = ear)
                drawCircle(color = tokens.ink, radius = earR, center = ear, style = Stroke(width = stroke * 0.85f))
            }
            // Shoulders
            drawRoundRect(
                color = furColor,
                topLeft = Offset(cx - headR * 0.85f, cy + headR * 0.68f),
                size = Size(headR * 1.7f, headR * 0.72f),
                cornerRadius = CornerRadius(headR * 0.5f, headR * 0.5f),
            )
            drawRoundRect(
                color = tokens.ink,
                topLeft = Offset(cx - headR * 0.85f, cy + headR * 0.68f),
                size = Size(headR * 1.7f, headR * 0.72f),
                cornerRadius = CornerRadius(headR * 0.5f, headR * 0.5f),
                style = Stroke(width = stroke * 0.85f),
            )
            // Head
            drawCircle(color = furColor, radius = headR, center = Offset(cx, cy))
            drawCircle(color = tokens.ink, radius = headR, center = Offset(cx, cy), style = Stroke(width = stroke))
            // Muzzle + cheeks + eyes
            drawCircle(
                color = tokens.warmHighlight,
                radius = headR * 0.38f,
                center = Offset(cx, cy + headR * 0.24f),
            )
            drawCircle(
                color = tokens.ink,
                radius = headR * 0.13f,
                center = Offset(cx - headR * 0.34f, cy - headR * 0.10f),
            )
            drawCircle(
                color = tokens.ink,
                radius = headR * 0.13f,
                center = Offset(cx + headR * 0.34f, cy - headR * 0.10f),
            )
            drawCircle(
                color = cheekColor.copy(alpha = 0.65f),
                radius = headR * 0.16f,
                center = Offset(cx - headR * 0.54f, cy + headR * 0.20f),
            )
            drawCircle(
                color = cheekColor.copy(alpha = 0.65f),
                radius = headR * 0.16f,
                center = Offset(cx + headR * 0.54f, cy + headR * 0.20f),
            )
        }

        // Back buddy (mint)
        drawMiniBuddy(
            cx = w * 0.66f,
            cy = h * 0.44f,
            headR = w * 0.21f,
            furColor = tokens.brandMint,
            cheekColor = tokens.brandBerry,
        )
        // Front buddy (honey)
        drawMiniBuddy(
            cx = w * 0.35f,
            cy = h * 0.48f,
            headR = w * 0.23f,
            furColor = tokens.brandHoney,
            cheekColor = tokens.brandBerry,
        )
    }
}

/**
 * Cute golden trophy cup icon for the Ranks navigation button.
 */
@Composable
fun CuteRanksIcon(
    size: Dp = 26.dp,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.085f).coerceAtLeast(1.6.dp.toPx())

        // Side handles
        drawCircle(
            color = tokens.brandHoneyDark,
            radius = w * 0.15f,
            center = Offset(w * 0.21f, h * 0.35f),
            style = Stroke(width = stroke * 1.1f),
        )
        drawCircle(
            color = tokens.brandHoneyDark,
            radius = w * 0.15f,
            center = Offset(w * 0.79f, h * 0.35f),
            style = Stroke(width = stroke * 1.1f),
        )

        // Trophy stem + base
        drawRoundRect(
            color = tokens.brandHoneyDark,
            topLeft = Offset(w * 0.42f, h * 0.58f),
            size = Size(w * 0.16f, h * 0.18f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        )
        drawRoundRect(
            color = tokens.brandBerry,
            topLeft = Offset(w * 0.26f, h * 0.74f),
            size = Size(w * 0.48f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
        )
        drawRoundRect(
            color = tokens.ink,
            topLeft = Offset(w * 0.26f, h * 0.74f),
            size = Size(w * 0.48f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
            style = Stroke(width = stroke * 0.85f),
        )

        // Trophy cup body
        val cupPath = Path().apply {
            moveTo(w * 0.24f, h * 0.16f)
            lineTo(w * 0.76f, h * 0.16f)
            lineTo(w * 0.70f, h * 0.52f)
            quadraticTo(w * 0.50f, h * 0.66f, w * 0.30f, h * 0.52f)
            close()
        }
        drawPath(path = cupPath, color = tokens.brandHoney)
        drawPath(
            path = cupPath,
            color = tokens.ink,
            style = Stroke(width = stroke, join = StrokeJoin.Round),
        )

        // Sparkling badge gem on cup front
        drawCircle(
            color = tokens.warmHighlight,
            radius = w * 0.10f,
            center = Offset(w * 0.50f, h * 0.36f),
        )
        drawCircle(
            color = tokens.ink,
            radius = w * 0.10f,
            center = Offset(w * 0.50f, h * 0.36f),
            style = Stroke(width = stroke * 0.7f),
        )
    }
}

/**
 * Cute smiling teddy medallion icon for the Me navigation button.
 */
@Composable
fun CuteMeIcon(
    size: Dp = 26.dp,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.085f).coerceAtLeast(1.6.dp.toPx())
        val cx = w * 0.50f
        val cy = h * 0.54f
        val headR = w * 0.31f
        val earR = headR * 0.44f
        val furColor = Color(0xFFC98B53)

        // Ears
        listOf(
            Offset(cx - headR * 0.74f, cy - headR * 0.68f),
            Offset(cx + headR * 0.74f, cy - headR * 0.68f),
        ).forEach { ear ->
            drawCircle(color = furColor, radius = earR, center = ear)
            drawCircle(color = tokens.warmHighlight, radius = earR * 0.48f, center = ear)
            drawCircle(color = tokens.ink, radius = earR, center = ear, style = Stroke(width = stroke * 0.9f))
        }

        // Head
        drawCircle(color = furColor, radius = headR, center = Offset(cx, cy))
        drawCircle(color = tokens.ink, radius = headR, center = Offset(cx, cy), style = Stroke(width = stroke))

        // Cream muzzle
        drawOval(
            color = tokens.warmHighlight,
            topLeft = Offset(cx - headR * 0.44f, cy + headR * 0.02f),
            size = Size(headR * 0.88f, headR * 0.62f),
        )
        // Little ink nose
        drawOval(
            color = tokens.ink,
            topLeft = Offset(cx - headR * 0.17f, cy + headR * 0.10f),
            size = Size(headR * 0.34f, headR * 0.22f),
        )
        // Shiny dot eyes
        drawCircle(
            color = tokens.ink,
            radius = headR * 0.12f,
            center = Offset(cx - headR * 0.34f, cy - headR * 0.16f),
        )
        drawCircle(
            color = tokens.ink,
            radius = headR * 0.12f,
            center = Offset(cx + headR * 0.34f, cy - headR * 0.16f),
        )
        // Rosy cheeks
        drawCircle(
            color = tokens.brandBerry.copy(alpha = 0.65f),
            radius = headR * 0.15f,
            center = Offset(cx - headR * 0.56f, cy + headR * 0.14f),
        )
        drawCircle(
            color = tokens.brandBerry.copy(alpha = 0.65f),
            radius = headR * 0.15f,
            center = Offset(cx + headR * 0.56f, cy + headR * 0.14f),
        )
    }
}



