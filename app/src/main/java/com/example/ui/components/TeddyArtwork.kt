package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AvatarClothingId
import com.example.model.AvatarExpression
import com.example.model.AvatarSkinId
import com.example.model.EquippedCosmetics
import com.example.model.TeddyAccessory
import com.example.model.TeddyAvatarConfig
import com.example.model.TeddyBodyColor
import com.example.ui.theme.StudioTheme

enum class TeddyPose {
    WAVING,
    PENCIL,
    TROPHY,
    COCOA,
    PEEKING,
}

enum class TeddyExpression {
    HAPPY,
    THINKING,
    SURPRISED,
    CELEBRATING,
    SLEEPY,
    SAD,
}

/**
 * Biscuit the brand mascot teddy character (DRD §7.1–7.4).
 * Flat vector shapes, 3px ink outline, two-tone fur, no gradients.
 */
@Composable
fun TeddyBiscuit(
    pose: TeddyPose,
    expression: TeddyExpression,
    modifier: Modifier = Modifier,
    size: Dp = 116.dp,
    reducedMotion: Boolean = false,
    bounceKey: Any? = Unit,
    overrideAccessory: TeddyAccessory = TeddyAccessory.SCARF,
) {
    val tokens = StudioTheme.tokens
    val bounceOffset = remember { Animatable(0f) }

    LaunchedEffect(bounceKey, reducedMotion) {
        if (!reducedMotion) {
            bounceOffset.snapTo(-10f)
            bounceOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        } else {
            bounceOffset.snapTo(0f)
        }
    }

    Canvas(
        modifier = modifier
            .size(size)
            .offset(y = bounceOffset.value.dp),
    ) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.8.dp.toPx()
        val fur = tokens.brandTeddy
        val furDark = tokens.brandTeddyDark
        val cream = tokens.paperCream
        val ink = tokens.ink
        val highlight = tokens.warmHighlight

        val headCx = w * 0.50f
        val headCy = if (pose == TeddyPose.PEEKING) h * 0.56f else h * 0.42f
        val headR = w * 0.27f

        // Body (hidden or peeking down for PEEKING pose)
        if (pose != TeddyPose.PEEKING) {
            // Torso
            drawOval(
                color = fur,
                topLeft = Offset(headCx - w * 0.24f, headCy + headR * 0.55f),
                size = Size(w * 0.48f, h * 0.36f),
            )
            drawOval(
                color = ink,
                topLeft = Offset(headCx - w * 0.24f, headCy + headR * 0.55f),
                size = Size(w * 0.48f, h * 0.36f),
                style = Stroke(width = stroke),
            )
            // Belly patch
            drawOval(
                color = cream,
                topLeft = Offset(headCx - w * 0.14f, headCy + headR * 0.72f),
                size = Size(w * 0.28f, h * 0.22f),
            )

            // Red & cream scarf (DRD §7.1)
            val scarfTop = headCy + headR * 0.65f
            drawRoundRect(
                color = tokens.brandBerry,
                topLeft = Offset(headCx - w * 0.20f, scarfTop),
                size = Size(w * 0.40f, h * 0.09f),
                cornerRadius = CornerRadius(12f, 12f),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(headCx - w * 0.20f, scarfTop),
                size = Size(w * 0.40f, h * 0.09f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = stroke * 0.85f),
            )
            // Cream stripe on scarf
            drawRect(
                color = cream,
                topLeft = Offset(headCx + w * 0.05f, scarfTop + h * 0.015f),
                size = Size(w * 0.06f, h * 0.06f),
            )
            // Scarf tail
            drawRoundRect(
                color = tokens.brandBerry,
                topLeft = Offset(headCx + w * 0.05f, scarfTop + h * 0.06f),
                size = Size(w * 0.10f, h * 0.14f),
                cornerRadius = CornerRadius(8f, 8f),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(headCx + w * 0.05f, scarfTop + h * 0.06f),
                size = Size(w * 0.10f, h * 0.14f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = stroke * 0.8f),
            )
        }

        // Ears
        val earR = headR * 0.38f
        val leftEar = Offset(headCx - headR * 0.78f, headCy - headR * 0.72f)
        val rightEar = Offset(headCx + headR * 0.78f, headCy - headR * 0.72f)
        listOf(leftEar, rightEar).forEach { ear ->
            drawCircle(color = fur, radius = earR, center = ear)
            drawCircle(color = ink, radius = earR, center = ear, style = Stroke(width = stroke))
            drawCircle(color = furDark, radius = earR * 0.54f, center = ear)
        }

        // Head
        drawCircle(color = fur, radius = headR, center = Offset(headCx, headCy))
        drawCircle(color = ink, radius = headR, center = Offset(headCx, headCy), style = Stroke(width = stroke))

        // Muzzle
        val muzzleW = headR * 0.92f
        val muzzleH = headR * 0.68f
        drawOval(
            color = cream,
            topLeft = Offset(headCx - muzzleW / 2f, headCy + headR * 0.04f),
            size = Size(muzzleW, muzzleH),
        )
        drawOval(
            color = ink,
            topLeft = Offset(headCx - muzzleW / 2f, headCy + headR * 0.04f),
            size = Size(muzzleW, muzzleH),
            style = Stroke(width = stroke * 0.6f),
        )

        // Nose
        drawOval(
            color = ink,
            topLeft = Offset(headCx - headR * 0.16f, headCy + headR * 0.12f),
            size = Size(headR * 0.32f, headR * 0.22f),
        )

        // Facial expression
        drawTeddyFace(
            cx = headCx,
            cy = headCy,
            headR = headR,
            expression = expression,
            ink = ink,
            highlight = highlight,
            cheekColor = tokens.playerTomato,
            stroke = stroke,
        )

        // Accessory override (e.g., headphones on Paywall)
        if (overrideAccessory == TeddyAccessory.HEADPHONES) {
            drawAccessory(
                accessory = TeddyAccessory.HEADPHONES,
                headCx = headCx,
                headCy = headCy,
                headR = headR,
                ink = ink,
                tokens = tokens,
                stroke = stroke,
            )
        }

        // Pose-specific paws & props
        when (pose) {
            TeddyPose.WAVING -> {
                // Raised left waving paw
                val pawCenter = Offset(headCx - w * 0.30f, headCy + headR * 0.30f)
                drawCircle(color = fur, radius = headR * 0.30f, center = pawCenter)
                drawCircle(color = ink, radius = headR * 0.30f, center = pawCenter, style = Stroke(width = stroke))
                drawCircle(color = cream, radius = headR * 0.14f, center = pawCenter)
                // Right paw resting
                val rightPaw = Offset(headCx + w * 0.24f, headCy + headR * 1.15f)
                drawCircle(color = fur, radius = headR * 0.28f, center = rightPaw)
                drawCircle(color = ink, radius = headR * 0.28f, center = rightPaw, style = Stroke(width = stroke))
            }
            TeddyPose.PENCIL -> {
                val pawCenter = Offset(headCx + w * 0.26f, headCy + headR * 0.90f)
                // Oversized wooden pencil
                rotate(degrees = -28f, pivot = pawCenter) {
                    drawRoundRect(
                        color = tokens.brandHoney,
                        topLeft = Offset(pawCenter.x - w * 0.04f, pawCenter.y - h * 0.26f),
                        size = Size(w * 0.08f, h * 0.34f),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                    drawRoundRect(
                        color = ink,
                        topLeft = Offset(pawCenter.x - w * 0.04f, pawCenter.y - h * 0.26f),
                        size = Size(w * 0.08f, h * 0.34f),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = stroke * 0.8f),
                    )
                    // Eraser
                    drawRoundRect(
                        color = tokens.brandBerry,
                        topLeft = Offset(pawCenter.x - w * 0.04f, pawCenter.y - h * 0.28f),
                        size = Size(w * 0.08f, h * 0.06f),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                    // Tip
                    val tip = Path().apply {
                        moveTo(pawCenter.x - w * 0.04f, pawCenter.y + h * 0.08f)
                        lineTo(pawCenter.x + w * 0.04f, pawCenter.y + h * 0.08f)
                        lineTo(pawCenter.x, pawCenter.y + h * 0.15f)
                        close()
                    }
                    drawPath(tip, color = cream)
                    drawPath(tip, color = ink, style = Stroke(width = stroke * 0.7f))
                }
                drawCircle(color = fur, radius = headR * 0.28f, center = pawCenter)
                drawCircle(color = ink, radius = headR * 0.28f, center = pawCenter, style = Stroke(width = stroke))
            }
            TeddyPose.TROPHY -> {
                val cupCenter = Offset(headCx + w * 0.26f, headCy + headR * 0.55f)
                // Honey trophy cup
                val cupPath = Path().apply {
                    moveTo(cupCenter.x - w * 0.11f, cupCenter.y - h * 0.12f)
                    lineTo(cupCenter.x + w * 0.11f, cupCenter.y - h * 0.12f)
                    lineTo(cupCenter.x + w * 0.07f, cupCenter.y + h * 0.03f)
                    lineTo(cupCenter.x - w * 0.07f, cupCenter.y + h * 0.03f)
                    close()
                }
                drawPath(cupPath, color = tokens.brandHoney)
                drawPath(cupPath, color = ink, style = Stroke(width = stroke * 0.85f, join = StrokeJoin.Round))
                drawRect(
                    color = tokens.brandHoneyDark,
                    topLeft = Offset(cupCenter.x - w * 0.03f, cupCenter.y + h * 0.03f),
                    size = Size(w * 0.06f, h * 0.08f),
                )
                drawRoundRect(
                    color = tokens.woodDark,
                    topLeft = Offset(cupCenter.x - w * 0.08f, cupCenter.y + h * 0.10f),
                    size = Size(w * 0.16f, h * 0.05f),
                    cornerRadius = CornerRadius(4f, 4f),
                )
                drawCircle(color = fur, radius = headR * 0.26f, center = Offset(cupCenter.x - w * 0.06f, cupCenter.y + h * 0.07f))
                drawCircle(color = ink, radius = headR * 0.26f, center = Offset(cupCenter.x - w * 0.06f, cupCenter.y + h * 0.07f), style = Stroke(width = stroke))
            }
            TeddyPose.COCOA -> {
                val mugCenter = Offset(headCx + w * 0.22f, headCy + headR * 1.05f)
                // Mug
                drawRoundRect(
                    color = cream,
                    topLeft = Offset(mugCenter.x - w * 0.09f, mugCenter.y - h * 0.08f),
                    size = Size(w * 0.18f, h * 0.16f),
                    cornerRadius = CornerRadius(8f, 8f),
                )
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(mugCenter.x - w * 0.09f, mugCenter.y - h * 0.08f),
                    size = Size(w * 0.18f, h * 0.16f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = stroke * 0.8f),
                )
                // Steam
                val steam = Path().apply {
                    moveTo(mugCenter.x - w * 0.02f, mugCenter.y - h * 0.11f)
                    quadraticTo(mugCenter.x - w * 0.05f, mugCenter.y - h * 0.16f, mugCenter.x, mugCenter.y - h * 0.20f)
                }
                drawPath(steam, color = tokens.paperLine, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(color = fur, radius = headR * 0.25f, center = Offset(mugCenter.x - w * 0.06f, mugCenter.y + h * 0.03f))
                drawCircle(color = ink, radius = headR * 0.25f, center = Offset(mugCenter.x - w * 0.06f, mugCenter.y + h * 0.03f), style = Stroke(width = stroke))
            }
            TeddyPose.PEEKING -> {
                // Two paws gripping bottom edge
                val leftPaw = Offset(headCx - headR * 0.65f, headCy + headR * 0.92f)
                val rightPaw = Offset(headCx + headR * 0.65f, headCy + headR * 0.92f)
                listOf(leftPaw, rightPaw).forEach { paw ->
                    drawCircle(color = fur, radius = headR * 0.26f, center = paw)
                    drawCircle(color = ink, radius = headR * 0.26f, center = paw, style = Stroke(width = stroke))
                }
            }
        }
    }
}

/**
 * Player teddy avatar in a rounded squircle/circle frame supporting 8 bodies, 12 accessories,
 * cosmetic Avatar Skins, and cosmetic Avatar Clothes (DRD §7.5 & Addendum §5).
 */
@Composable
fun TeddyAvatar(
    config: TeddyAvatarConfig,
    backdropColor: Color,
    size: Dp = 48.dp,
    isCircle: Boolean = false,
    equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val shape = if (isCircle) CircleShape else RoundedCornerShape((size.value * 0.30f).dp)
    val borderWidth = if (size >= 56.dp) 3.dp else 2.5.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backdropColor)
            .border(borderWidth, tokens.ink, shape),
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = (size.value * 0.055f).coerceIn(1.6f, 3.0f).dp.toPx()
            val headCx = w * 0.50f
            val headCy = h * 0.53f
            val headR = w * 0.29f

            val skin = equippedCosmetics.avatarSkin
            val fur = when (skin) {
                AvatarSkinId.CLASSIC_PLUSH -> config.body.fillColor
                AvatarSkinId.SPOTTED_COOKIE -> Color(0xFFF6DFB9)
                AvatarSkinId.TIGER_STRIPED -> Color(0xFFF7AE45)
                AvatarSkinId.PATCHWORK_QUILT -> tokens.brandSky.copy(alpha = 0.72f)
                AvatarSkinId.TWO_TONE_PANDA -> tokens.warmHighlight
                AvatarSkinId.FLUFFY_CLOUD -> Color(0xFFEAF6FF)
                AvatarSkinId.STARLIGHT_PLUSH -> Color(0xFF9C7CE0)
            }
            val furShade = when (skin) {
                AvatarSkinId.CLASSIC_PLUSH -> config.body.shadeColor
                AvatarSkinId.SPOTTED_COOKIE -> Color(0xFF7D4E2D)
                AvatarSkinId.TIGER_STRIPED -> Color(0xFF5C3419)
                AvatarSkinId.PATCHWORK_QUILT -> tokens.brandHoney
                AvatarSkinId.TWO_TONE_PANDA -> Color(0xFF3E271A)
                AvatarSkinId.FLUFFY_CLOUD -> tokens.brandSky
                AvatarSkinId.STARLIGHT_PLUSH -> tokens.brandHoney
            }
            val muzzleColor = if (skin == AvatarSkinId.TWO_TONE_PANDA || config.body == TeddyBodyColor.CREAM) {
                tokens.paperShade
            } else {
                tokens.paperCream
            }

            // Clothing shoulder silhouette behind head
            drawAvatarClothing(
                clothing = equippedCosmetics.avatarClothing,
                headCx = headCx,
                headCy = headCy,
                headR = headR,
                w = w,
                h = h,
                ink = tokens.ink,
                tokens = tokens,
                stroke = stroke,
            )

            // Ears
            val earR = headR * 0.40f
            val earFill = if (skin == AvatarSkinId.TWO_TONE_PANDA) furShade else fur
            listOf(
                Offset(headCx - headR * 0.76f, headCy - headR * 0.70f),
                Offset(headCx + headR * 0.76f, headCy - headR * 0.70f),
            ).forEach { ear ->
                drawCircle(color = earFill, radius = earR, center = ear)
                drawCircle(color = tokens.ink, radius = earR, center = ear, style = Stroke(width = stroke))
                drawCircle(color = furShade, radius = earR * 0.50f, center = ear)
            }

            // Head base
            drawCircle(color = fur, radius = headR, center = Offset(headCx, headCy))

            // Avatar Skin patterns clipped inside head
            val headClip = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        left = headCx - headR,
                        top = headCy - headR,
                        right = headCx + headR,
                        bottom = headCy + headR,
                    )
                )
            }
            clipPath(headClip) {
                when (skin) {
                    AvatarSkinId.CLASSIC_PLUSH -> Unit
                    AvatarSkinId.SPOTTED_COOKIE -> {
                        drawCircle(furShade, headR * 0.16f, Offset(headCx - headR * 0.52f, headCy - headR * 0.45f))
                        drawCircle(furShade, headR * 0.14f, Offset(headCx + headR * 0.48f, headCy - headR * 0.40f))
                        drawCircle(furShade, headR * 0.12f, Offset(headCx + headR * 0.10f, headCy - headR * 0.74f))
                    }
                    AvatarSkinId.TIGER_STRIPED -> {
                        drawLine(furShade, Offset(headCx, headCy - headR), Offset(headCx, headCy - headR * 0.55f), stroke * 1.1f, StrokeCap.Round)
                        drawLine(furShade, Offset(headCx - headR * 0.25f, headCy - headR * 0.90f), Offset(headCx - headR * 0.16f, headCy - headR * 0.56f), stroke * 0.9f, StrokeCap.Round)
                        drawLine(furShade, Offset(headCx + headR * 0.25f, headCy - headR * 0.90f), Offset(headCx + headR * 0.16f, headCy - headR * 0.56f), stroke * 0.9f, StrokeCap.Round)
                    }
                    AvatarSkinId.PATCHWORK_QUILT -> {
                        drawRect(tokens.brandMint.copy(alpha = 0.65f), Offset(headCx, headCy - headR), Size(headR, headR * 2f))
                        drawLine(tokens.ink, Offset(headCx, headCy - headR), Offset(headCx, headCy + headR), stroke * 0.55f)
                    }
                    AvatarSkinId.TWO_TONE_PANDA -> {
                        drawOval(furShade, Offset(headCx - headR * 0.58f, headCy - headR * 0.34f), Size(headR * 0.44f, headR * 0.44f))
                        drawOval(furShade, Offset(headCx + headR * 0.14f, headCy - headR * 0.34f), Size(headR * 0.44f, headR * 0.44f))
                    }
                    AvatarSkinId.FLUFFY_CLOUD -> {
                        drawCircle(tokens.warmHighlight, headR * 0.26f, Offset(headCx - headR * 0.28f, headCy - headR * 0.72f))
                        drawCircle(tokens.warmHighlight, headR * 0.28f, Offset(headCx + headR * 0.12f, headCy - headR * 0.78f))
                    }
                    AvatarSkinId.STARLIGHT_PLUSH -> {
                        drawCircle(tokens.brandHoney, headR * 0.10f, Offset(headCx - headR * 0.50f, headCy - headR * 0.50f))
                        drawCircle(tokens.brandHoney, headR * 0.08f, Offset(headCx + headR * 0.46f, headCy - headR * 0.56f))
                        drawCircle(tokens.warmHighlight, headR * 0.07f, Offset(headCx, headCy - headR * 0.75f))
                    }
                }
            }

            drawCircle(color = tokens.ink, radius = headR, center = Offset(headCx, headCy), style = Stroke(width = stroke))

            // Muzzle
            drawOval(
                color = muzzleColor,
                topLeft = Offset(headCx - headR * 0.44f, headCy + headR * 0.05f),
                size = Size(headR * 0.88f, headR * 0.64f),
            )
            drawOval(
                color = tokens.ink,
                topLeft = Offset(headCx - headR * 0.16f, headCy + headR * 0.12f),
                size = Size(headR * 0.32f, headR * 0.22f),
            )

            val expr = if (config.expression == AvatarExpression.CELEBRATING) {
                TeddyExpression.CELEBRATING
            } else {
                TeddyExpression.HAPPY
            }

            drawTeddyFace(
                cx = headCx,
                cy = headCy,
                headR = headR,
                expression = expr,
                ink = tokens.ink,
                highlight = tokens.warmHighlight,
                cheekColor = tokens.playerTomato,
                stroke = stroke,
            )

            drawAccessory(
                accessory = config.accessory,
                headCx = headCx,
                headCy = headCy,
                headR = headR,
                ink = tokens.ink,
                tokens = tokens,
                stroke = stroke,
            )
        }
    }
}

private fun DrawScope.drawAvatarClothing(
    clothing: AvatarClothingId,
    headCx: Float,
    headCy: Float,
    headR: Float,
    w: Float,
    h: Float,
    ink: Color,
    tokens: com.example.ui.theme.AppTokens,
    stroke: Float,
) {
    val shoulderTop = headCy + headR * 0.62f
    val shoulderW = w * 0.68f
    val shoulderH = h * 0.38f
    val shoulderLeft = headCx - shoulderW / 2f

    when (clothing) {
        AvatarClothingId.NONE -> Unit
        AvatarClothingId.BERRY_SCARF -> {
            drawRoundRect(
                color = tokens.brandBerry,
                topLeft = Offset(headCx - headR * 0.80f, shoulderTop),
                size = Size(headR * 1.60f, headR * 0.56f),
                cornerRadius = CornerRadius(12f, 12f),
            )
            drawRect(
                color = tokens.paperCream,
                topLeft = Offset(headCx + headR * 0.14f, shoulderTop + headR * 0.08f),
                size = Size(headR * 0.24f, headR * 0.40f),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(headCx - headR * 0.80f, shoulderTop),
                size = Size(headR * 1.60f, headR * 0.56f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = stroke * 0.8f),
            )
        }
        AvatarClothingId.STRIPED_SWEATER -> {
            drawRoundRect(
                color = tokens.brandSky,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(16f, 16f),
            )
            drawRect(
                color = tokens.paperCream,
                topLeft = Offset(shoulderLeft, shoulderTop + shoulderH * 0.35f),
                size = Size(shoulderW, shoulderH * 0.18f),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = stroke * 0.85f),
            )
        }
        AvatarClothingId.VARSITY_JACKET -> {
            drawRoundRect(
                color = tokens.brandBerry,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(16f, 16f),
            )
            drawRect(
                color = tokens.paperCream,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW * 0.24f, shoulderH),
            )
            drawRect(
                color = tokens.paperCream,
                topLeft = Offset(shoulderLeft + shoulderW * 0.76f, shoulderTop),
                size = Size(shoulderW * 0.24f, shoulderH),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = stroke * 0.85f),
            )
        }
        AvatarClothingId.CRAFT_OVERALLS -> {
            drawRoundRect(
                color = Color(0xFF477EC2),
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(14f, 14f),
            )
            drawCircle(tokens.brandHoney, headR * 0.11f, Offset(headCx - headR * 0.35f, shoulderTop + headR * 0.28f))
            drawCircle(tokens.brandHoney, headR * 0.11f, Offset(headCx + headR * 0.35f, shoulderTop + headR * 0.28f))
            drawRoundRect(
                color = ink,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(width = stroke * 0.85f),
            )
        }
        AvatarClothingId.STARRY_PAJAMAS -> {
            drawRoundRect(
                color = tokens.brandLilac,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(16f, 16f),
            )
            drawCircle(tokens.brandHoney, headR * 0.10f, Offset(headCx - headR * 0.42f, shoulderTop + headR * 0.36f))
            drawCircle(tokens.brandHoney, headR * 0.10f, Offset(headCx + headR * 0.42f, shoulderTop + headR * 0.36f))
            drawRoundRect(
                color = ink,
                topLeft = Offset(shoulderLeft, shoulderTop),
                size = Size(shoulderW, shoulderH),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = stroke * 0.85f),
            )
        }
        AvatarClothingId.HERO_CAPE -> {
            val cape = Path().apply {
                moveTo(headCx - headR * 0.90f, shoulderTop)
                lineTo(headCx + headR * 0.90f, shoulderTop)
                lineTo(headCx + shoulderW * 0.62f, shoulderTop + shoulderH)
                lineTo(headCx - shoulderW * 0.62f, shoulderTop + shoulderH)
                close()
            }
            drawPath(cape, color = tokens.brandHoney)
            drawPath(cape, color = ink, style = Stroke(width = stroke * 0.85f, join = StrokeJoin.Round))
            drawCircle(tokens.brandBerry, headR * 0.14f, Offset(headCx, shoulderTop + headR * 0.24f))
        }
    }
}

private fun DrawScope.drawTeddyFace(
    cx: Float,
    cy: Float,
    headR: Float,
    expression: TeddyExpression,
    ink: Color,
    highlight: Color,
    cheekColor: Color,
    stroke: Float,
) {
    val leftEye = Offset(cx - headR * 0.36f, cy - headR * 0.12f)
    val rightEye = Offset(cx + headR * 0.36f, cy - headR * 0.12f)
    val eyeR = headR * 0.11f

    // Soft cheek blush
    drawCircle(color = cheekColor.copy(alpha = 0.45f), radius = headR * 0.17f, center = Offset(cx - headR * 0.56f, cy + headR * 0.14f))
    drawCircle(color = cheekColor.copy(alpha = 0.45f), radius = headR * 0.17f, center = Offset(cx + headR * 0.56f, cy + headR * 0.14f))

    when (expression) {
        TeddyExpression.HAPPY, TeddyExpression.SURPRISED -> {
            val pupilOffset = 0f
            listOf(leftEye, rightEye).forEach { eye ->
                drawCircle(color = ink, radius = eyeR, center = Offset(eye.x + pupilOffset, eye.y))
                drawCircle(color = highlight, radius = eyeR * 0.35f, center = Offset(eye.x - eyeR * 0.3f, eye.y - eyeR * 0.3f))
            }
            if (expression == TeddyExpression.SURPRISED) {
                // Raised brows
                drawLine(ink, Offset(leftEye.x - eyeR, leftEye.y - eyeR * 2.2f), Offset(leftEye.x + eyeR, leftEye.y - eyeR * 2.5f), stroke * 0.7f, StrokeCap.Round)
                drawLine(ink, Offset(rightEye.x - eyeR, rightEye.y - eyeR * 2.5f), Offset(rightEye.x + eyeR, rightEye.y - eyeR * 2.2f), stroke * 0.7f, StrokeCap.Round)
                // Round O mouth
                drawCircle(color = ink, radius = headR * 0.11f, center = Offset(cx, cy + headR * 0.46f))
            } else {
                // Happy smile
                val mouth = Path().apply {
                    moveTo(cx - headR * 0.16f, cy + headR * 0.38f)
                    quadraticTo(cx, cy + headR * 0.54f, cx + headR * 0.16f, cy + headR * 0.38f)
                }
                drawPath(mouth, color = ink, style = Stroke(width = stroke * 0.75f, cap = StrokeCap.Round))
            }
        }
        TeddyExpression.THINKING -> {
            // Eyes shifted slightly to the side
            listOf(leftEye, rightEye).forEach { eye ->
                drawCircle(color = ink, radius = eyeR, center = Offset(eye.x + eyeR * 0.5f, eye.y - eyeR * 0.2f))
                drawCircle(color = highlight, radius = eyeR * 0.32f, center = Offset(eye.x + eyeR * 0.2f, eye.y - eyeR * 0.4f))
            }
            // Thoughtful tilted mouth
            drawLine(
                color = ink,
                start = Offset(cx - headR * 0.12f, cy + headR * 0.44f),
                end = Offset(cx + headR * 0.14f, cy + headR * 0.40f),
                strokeWidth = stroke * 0.75f,
                cap = StrokeCap.Round,
            )
        }
        TeddyExpression.CELEBRATING -> {
            // Happy closed crescent eyes
            listOf(leftEye, rightEye).forEach { eye ->
                val arc = Path().apply {
                    moveTo(eye.x - eyeR * 1.2f, eye.y + eyeR * 0.2f)
                    quadraticTo(eye.x, eye.y - eyeR * 1.1f, eye.x + eyeR * 1.2f, eye.y + eyeR * 0.2f)
                }
                drawPath(arc, color = ink, style = Stroke(width = stroke * 0.85f, cap = StrokeCap.Round))
            }
            val grin = Path().apply {
                moveTo(cx - headR * 0.20f, cy + headR * 0.36f)
                quadraticTo(cx, cy + headR * 0.58f, cx + headR * 0.20f, cy + headR * 0.36f)
            }
            drawPath(grin, color = ink, style = Stroke(width = stroke * 0.8f, cap = StrokeCap.Round))
        }
        TeddyExpression.SLEEPY -> {
            listOf(leftEye, rightEye).forEach { eye ->
                val arc = Path().apply {
                    moveTo(eye.x - eyeR * 1.1f, eye.y)
                    quadraticTo(eye.x, eye.y + eyeR * 0.8f, eye.x + eyeR * 1.1f, eye.y)
                }
                drawPath(arc, color = ink, style = Stroke(width = stroke * 0.8f, cap = StrokeCap.Round))
            }
            drawCircle(color = ink, radius = headR * 0.07f, center = Offset(cx, cy + headR * 0.44f))
        }
        TeddyExpression.SAD -> {
            listOf(leftEye, rightEye).forEach { eye ->
                drawCircle(color = ink, radius = eyeR * 0.9f, center = eye)
            }
            val frown = Path().apply {
                moveTo(cx - headR * 0.15f, cy + headR * 0.48f)
                quadraticTo(cx, cy + headR * 0.36f, cx + headR * 0.15f, cy + headR * 0.48f)
            }
            drawPath(frown, color = ink, style = Stroke(width = stroke * 0.75f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawAccessory(
    accessory: TeddyAccessory,
    headCx: Float,
    headCy: Float,
    headR: Float,
    ink: Color,
    tokens: com.example.ui.theme.AppTokens,
    stroke: Float,
) {
    when (accessory) {
        TeddyAccessory.NONE -> Unit
        TeddyAccessory.BOW -> {
            val bx = headCx + headR * 0.52f
            val by = headCy - headR * 0.78f
            val p1 = Path().apply {
                moveTo(bx, by)
                lineTo(bx - headR * 0.26f, by - headR * 0.18f)
                lineTo(bx - headR * 0.26f, by + headR * 0.18f)
                close()
            }
            val p2 = Path().apply {
                moveTo(bx, by)
                lineTo(bx + headR * 0.26f, by - headR * 0.18f)
                lineTo(bx + headR * 0.26f, by + headR * 0.18f)
                close()
            }
            drawPath(p1, color = tokens.brandBerry)
            drawPath(p1, color = ink, style = Stroke(width = stroke * 0.65f))
            drawPath(p2, color = tokens.brandBerry)
            drawPath(p2, color = ink, style = Stroke(width = stroke * 0.65f))
            drawCircle(color = tokens.brandHoney, radius = headR * 0.10f, center = Offset(bx, by))
        }
        TeddyAccessory.BEANIE -> {
            drawArc(
                color = tokens.brandSky,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCx - headR * 0.85f, headCy - headR * 1.15f),
                size = Size(headR * 1.70f, headR * 0.90f),
            )
            drawRoundRect(
                color = tokens.paperCream,
                topLeft = Offset(headCx - headR * 0.88f, headCy - headR * 0.78f),
                size = Size(headR * 1.76f, headR * 0.24f),
                cornerRadius = CornerRadius(10f, 10f),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(headCx - headR * 0.88f, headCy - headR * 0.78f),
                size = Size(headR * 1.76f, headR * 0.24f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = stroke * 0.65f),
            )
        }
        TeddyAccessory.CROWN -> {
            val topY = headCy - headR * 1.32f
            val baseY = headCy - headR * 0.82f
            val crown = Path().apply {
                moveTo(headCx - headR * 0.45f, baseY)
                lineTo(headCx - headR * 0.50f, topY + headR * 0.10f)
                lineTo(headCx - headR * 0.18f, baseY - headR * 0.22f)
                lineTo(headCx, topY)
                lineTo(headCx + headR * 0.18f, baseY - headR * 0.22f)
                lineTo(headCx + headR * 0.50f, topY + headR * 0.10f)
                lineTo(headCx + headR * 0.45f, baseY)
                close()
            }
            drawPath(crown, color = tokens.brandHoney)
            drawPath(crown, color = ink, style = Stroke(width = stroke * 0.7f, join = StrokeJoin.Round))
        }
        TeddyAccessory.GLASSES -> {
            val leftEye = Offset(headCx - headR * 0.36f, headCy - headR * 0.12f)
            val rightEye = Offset(headCx + headR * 0.36f, headCy - headR * 0.12f)
            val r = headR * 0.24f
            drawCircle(color = ink, radius = r, center = leftEye, style = Stroke(width = stroke * 0.8f))
            drawCircle(color = ink, radius = r, center = rightEye, style = Stroke(width = stroke * 0.8f))
            drawLine(ink, Offset(leftEye.x + r, leftEye.y), Offset(rightEye.x - r, rightEye.y), stroke * 0.8f)
        }
        TeddyAccessory.FLOWER -> {
            val fc = Offset(headCx - headR * 0.62f, headCy - headR * 0.75f)
            val petalR = headR * 0.13f
            for (i in 0 until 5) {
                val angle = i * 72.0 * Math.PI / 180.0
                val pc = Offset(fc.x + (petalR * 1.1f * kotlin.math.cos(angle)).toFloat(), fc.y + (petalR * 1.1f * kotlin.math.sin(angle)).toFloat())
                drawCircle(color = tokens.brandLilac, radius = petalR, center = pc)
                drawCircle(color = ink, radius = petalR, center = pc, style = Stroke(width = stroke * 0.45f))
            }
            drawCircle(color = tokens.brandHoney, radius = petalR * 0.85f, center = fc)
        }
        TeddyAccessory.BANDANA -> {
            val top = headCy - headR * 0.92f
            drawArc(
                color = tokens.brandBerry,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = true,
                topLeft = Offset(headCx - headR * 0.92f, top),
                size = Size(headR * 1.84f, headR * 0.80f),
            )
            drawArc(
                color = ink,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(headCx - headR * 0.92f, top),
                size = Size(headR * 1.84f, headR * 0.80f),
                style = Stroke(width = stroke * 0.65f),
            )
        }
        TeddyAccessory.SCARF -> {
            drawRoundRect(
                color = tokens.brandBerry,
                topLeft = Offset(headCx - headR * 0.65f, headCy + headR * 0.80f),
                size = Size(headR * 1.30f, headR * 0.34f),
                cornerRadius = CornerRadius(10f, 10f),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(headCx - headR * 0.65f, headCy + headR * 0.80f),
                size = Size(headR * 1.30f, headR * 0.34f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = stroke * 0.65f),
            )
        }
        TeddyAccessory.STAR_CLIP -> {
            val sc = Offset(headCx + headR * 0.54f, headCy - headR * 0.68f)
            val r = headR * 0.24f
            val path = Path()
            for (i in 0 until 10) {
                val rad = if (i % 2 == 0) r else r * 0.45f
                val ang = (i * 36.0 - 90.0) * Math.PI / 180.0
                val px = sc.x + (rad * kotlin.math.cos(ang)).toFloat()
                val py = sc.y + (rad * kotlin.math.sin(ang)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path, color = tokens.brandHoney)
            drawPath(path, color = ink, style = Stroke(width = stroke * 0.6f))
        }
        TeddyAccessory.HEADPHONES -> {
            // Band over head
            drawArc(
                color = ink,
                startAngle = 185f,
                sweepAngle = 170f,
                useCenter = false,
                topLeft = Offset(headCx - headR * 1.12f, headCy - headR * 1.15f),
                size = Size(headR * 2.24f, headR * 2.1f),
                style = Stroke(width = stroke * 1.2f, cap = StrokeCap.Round),
            )
            // Ear cups
            listOf(headCx - headR * 1.06f, headCx + headR * 0.82f).forEach { cx ->
                drawRoundRect(
                    color = tokens.brandMint,
                    topLeft = Offset(cx, headCy - headR * 0.30f),
                    size = Size(headR * 0.26f, headR * 0.62f),
                    cornerRadius = CornerRadius(10f, 10f),
                )
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(cx, headCy - headR * 0.30f),
                    size = Size(headR * 0.26f, headR * 0.62f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = stroke * 0.65f),
                )
            }
        }
        TeddyAccessory.CAP -> {
            drawArc(
                color = tokens.brandMint,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCx - headR * 0.84f, headCy - headR * 1.16f),
                size = Size(headR * 1.68f, headR * 0.84f),
            )
            // Brim
            drawRoundRect(
                color = tokens.brandMint,
                topLeft = Offset(headCx - headR * 0.45f, headCy - headR * 0.78f),
                size = Size(headR * 1.55f, headR * 0.20f),
                cornerRadius = CornerRadius(10f, 10f),
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(headCx - headR * 0.45f, headCy - headR * 0.78f),
                size = Size(headR * 1.55f, headR * 0.20f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = stroke * 0.6f),
            )
        }
        TeddyAccessory.BUNNY_EARS -> {
            listOf(-0.34f, 0.34f).forEach { xOff ->
                val ex = headCx + headR * xOff
                val ey = headCy - headR * 1.38f
                drawOval(
                    color = tokens.paperCream,
                    topLeft = Offset(ex - headR * 0.16f, ey),
                    size = Size(headR * 0.32f, headR * 0.64f),
                )
                drawOval(
                    color = ink,
                    topLeft = Offset(ex - headR * 0.16f, ey),
                    size = Size(headR * 0.32f, headR * 0.64f),
                    style = Stroke(width = stroke * 0.65f),
                )
            }
        }
        TeddyAccessory.HALO -> {
            drawOval(
                color = tokens.brandHoney,
                topLeft = Offset(headCx - headR * 0.54f, headCy - headR * 1.28f),
                size = Size(headR * 1.08f, headR * 0.28f),
                style = Stroke(width = stroke * 1.0f),
            )
        }
    }
}

/**
 * Crayon-style mode sticker illustrations for Home screen (DRD §13).
 */
@Composable
fun ModeStickerIcon(
    kind: HomeModeIconKind,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val tokens = StudioTheme.tokens
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.4.dp.toPx()
        val ink = tokens.ink

        when (kind) {
            HomeModeIconKind.OFFLINE_PENCILS -> {
                rotate(-32f, Offset(w * 0.5f, h * 0.5f)) {
                    drawRoundRect(
                        color = tokens.playerTomato,
                        topLeft = Offset(w * 0.42f, h * 0.14f),
                        size = Size(w * 0.16f, h * 0.72f),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                    drawRoundRect(
                        color = ink,
                        topLeft = Offset(w * 0.42f, h * 0.14f),
                        size = Size(w * 0.16f, h * 0.72f),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = stroke),
                    )
                }
                rotate(32f, Offset(w * 0.5f, h * 0.5f)) {
                    drawRoundRect(
                        color = tokens.brandSky,
                        topLeft = Offset(w * 0.42f, h * 0.14f),
                        size = Size(w * 0.16f, h * 0.72f),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                    drawRoundRect(
                        color = ink,
                        topLeft = Offset(w * 0.42f, h * 0.14f),
                        size = Size(w * 0.16f, h * 0.72f),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = stroke),
                    )
                }
            }
            HomeModeIconKind.ONLINE_TEDDIES -> {
                // Two cute teddy heads side by side
                listOf(w * 0.34f to tokens.brandTeddy, w * 0.66f to tokens.brandHoney).forEach { (cx, col) ->
                    val cy = h * 0.56f
                    val r = w * 0.20f
                    drawCircle(col, r * 0.42f, Offset(cx - r * 0.7f, cy - r * 0.7f))
                    drawCircle(ink, r * 0.42f, Offset(cx - r * 0.7f, cy - r * 0.7f), style = Stroke(stroke * 0.8f))
                    drawCircle(col, r * 0.42f, Offset(cx + r * 0.7f, cy - r * 0.7f))
                    drawCircle(ink, r * 0.42f, Offset(cx + r * 0.7f, cy - r * 0.7f), style = Stroke(stroke * 0.8f))
                    drawCircle(col, r, Offset(cx, cy))
                    drawCircle(ink, r, Offset(cx, cy), style = Stroke(stroke))
                    drawCircle(ink, 2.dp.toPx(), Offset(cx - r * 0.3f, cy - r * 0.1f))
                    drawCircle(ink, 2.dp.toPx(), Offset(cx + r * 0.3f, cy - r * 0.1f))
                }
            }
            HomeModeIconKind.LEADERBOARD_TROPHY -> {
                val cup = Path().apply {
                    moveTo(w * 0.26f, h * 0.22f)
                    lineTo(w * 0.74f, h * 0.22f)
                    lineTo(w * 0.64f, h * 0.58f)
                    lineTo(w * 0.36f, h * 0.58f)
                    close()
                }
                drawPath(cup, color = tokens.brandHoney)
                drawPath(cup, color = ink, style = Stroke(width = stroke, join = StrokeJoin.Round))
                drawRect(tokens.brandHoneyDark, Offset(w * 0.44f, h * 0.58f), Size(w * 0.12f, h * 0.16f))
                drawRect(ink, Offset(w * 0.44f, h * 0.58f), Size(w * 0.12f, h * 0.16f), style = Stroke(stroke * 0.8f))
                drawRoundRect(
                    color = tokens.woodDark,
                    topLeft = Offset(w * 0.30f, h * 0.74f),
                    size = Size(w * 0.40f, h * 0.12f),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(w * 0.30f, h * 0.74f),
                    size = Size(w * 0.40f, h * 0.12f),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = stroke),
                )
            }
        }
    }
}

enum class HomeModeIconKind {
    OFFLINE_PENCILS,
    ONLINE_TEDDIES,
    LEADERBOARD_TROPHY,
}
