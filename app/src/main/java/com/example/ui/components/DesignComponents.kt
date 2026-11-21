package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpConfig
import com.example.model.GenderOption
import com.example.model.LetterFontId
import com.example.model.PlayerColorOption
import com.example.model.PlayerPattern
import com.example.model.TeddyAvatarConfig
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.LilitaOneFontFamily
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.StudioTheme

/**
 * Primary "bubble" button: Honey fill, ink text, 3dp ink outline, 4dp darker bottom lip, sinks 4dp when pressed (DRD §6.4 & §10.1).
 */
@Composable
fun StudioPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    BubbleButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        fillColor = StudioTheme.tokens.brandHoney,
        lipColor = StudioTheme.tokens.brandHoneyDark,
        textColor = StudioTheme.tokens.ink,
        modifier = modifier,
        testTag = testTag,
    )
}

@Composable
fun StudioDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    BubbleButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        fillColor = StudioTheme.tokens.brandBerry,
        lipColor = StudioTheme.tokens.brandBerryDark,
        textColor = StudioTheme.tokens.warmHighlight,
        modifier = modifier,
        testTag = testTag,
    )
}

@Composable
private fun BubbleButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    fillColor: Color,
    lipColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val tokens = StudioTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pillShape = RoundedCornerShape(999.dp)

    val activeFill = if (enabled) fillColor else tokens.paperShade
    val activeBorder = if (enabled) tokens.ink else tokens.paperLine
    val activeText = if (enabled) textColor else tokens.ink.copy(alpha = 0.45f)
    val pressOffset = if (isPressed && enabled) 4.dp else 0.dp

    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .padding(bottom = 4.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        // 4dp bottom lip
        if (enabled && !isPressed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 4.dp)
                    .clip(pillShape)
                    .background(lipColor)
                    .border(3.dp, tokens.ink, pillShape),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .offset(y = pressOffset)
                .clip(pillShape)
                .background(activeFill)
                .border(3.dp, activeBorder, pillShape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = activeText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Secondary button: Cream fill, ink text, 3dp ink outline, pill shape (DRD §10.1).
 */
@Composable
fun StudioSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color? = null,
    contentColor: Color? = null,
    testTag: String? = null,
) {
    val tokens = StudioTheme.tokens
    val pillShape = RoundedCornerShape(999.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .offset(y = if (isPressed && enabled) 2.dp else 0.dp)
            .clip(pillShape)
            .background(if (enabled) tokens.paperCream else tokens.paperShade)
            .border(
                width = 3.dp,
                color = if (enabled) (borderColor ?: tokens.ink) else tokens.paperLine,
                shape = pillShape,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) (contentColor ?: tokens.ink) else tokens.ink.copy(alpha = 0.45f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun StudioTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color? = null,
    testTag: String? = null,
) {
    val tokens = StudioTheme.tokens
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor ?: tokens.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Paper card with 3dp ink outline, 24dp radius, and hard 4dp offset ink shadow (DRD §6.3 & §10.3).
 */
@Composable
fun StudioCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val tokens = StudioTheme.tokens
    val shape = RoundedCornerShape(24.dp)
    val fill = backgroundColor ?: tokens.paperCream
    val outline = borderColor ?: tokens.ink

    Box(
        modifier = modifier.padding(end = 4.dp, bottom = 4.dp),
    ) {
        // Hard 4dp offset shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(shape)
                .background(tokens.ink),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(fill)
                .border(3.dp, outline, shape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        ) {
            content()
        }
    }
}

/**
 * Pill chip with 2dp ink outline (DRD §10.4).
 */
@Composable
fun StudioChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val tokens = StudioTheme.tokens
    val shape = RoundedCornerShape(999.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(if (selected) tokens.brandHoney else tokens.paperCream)
            .border(
                width = if (selected) 3.dp else 2.dp,
                color = tokens.ink,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            ),
            color = tokens.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Paper text input with 3dp ink outline (4dp honey on focus), 16dp radius (DRD §10.6).
 */
@Composable
fun StudioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String? = null,
) {
    val tokens = StudioTheme.tokens
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    val borderColor = when {
        errorText != null -> tokens.brandBerry
        isFocused -> tokens.brandHoney
        else -> tokens.ink
    }
    val borderWidth = if (isFocused || errorText != null) 4.dp else 3.dp

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = tokens.ink,
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .clip(shape)
                .background(tokens.paperCream)
                .border(borderWidth, borderColor, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tokens.ink.copy(alpha = 0.45f),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = tokens.ink),
                cursorBrush = SolidColor(tokens.ink),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
            )
        }
        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelMedium,
                color = tokens.brandBerryDark,
            )
        }
    }
}

/**
 * Paper sticker badge with cream border, ink outline, and slight rotation (DRD §6.5).
 */
@Composable
fun StickerBadge(
    text: String,
    backgroundColor: Color,
    rotationDegrees: Float = -3f,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val shape = RoundedCornerShape(999.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .rotate(rotationDegrees)
            .clip(shape)
            .background(tokens.warmHighlight)
            .border(2.dp, tokens.ink, shape)
            .padding(3.dp)
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = LilitaNumberStyle.copy(fontSize = 13.sp),
            color = tokens.ink,
        )
    }
}

/**
 * Monogram or TeddyAvatar wrapper used across profiles, leaderboard, and scoreboard.
 */
@Composable
fun MonogramAvatar(
    initials: String,
    colorOption: PlayerColorOption,
    size: Dp = 44.dp,
    avatarConfig: TeddyAvatarConfig? = null,
    modifier: Modifier = Modifier,
) {
    if (avatarConfig != null) {
        TeddyAvatar(
            config = avatarConfig,
            backdropColor = colorOption.color,
            size = size,
            modifier = modifier,
        )
        return
    }
    val tokens = StudioTheme.tokens
    val textColor = if (colorOption.usesLightOutlineText) tokens.warmHighlight else tokens.ink
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colorOption.color)
            .border(2.5.dp, tokens.ink, CircleShape),
    ) {
        Text(
            text = initials.take(3).uppercase(),
            style = LilitaNumberStyle.copy(fontSize = (size.value * 0.36f).sp),
            color = textColor,
            maxLines = 1,
        )
    }
}

@Composable
fun GenderBadge(
    gender: GenderOption,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(tokens.paperShade)
            .border(2.dp, tokens.ink, CircleShape),
    ) {
        Text(
            text = gender.mark,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (gender.mark.length > 1) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = tokens.ink,
        )
    }
}

@Composable
fun LevelTag(
    level: Int,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val shape = RoundedCornerShape(999.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(tokens.brandHoney)
            .border(2.dp, tokens.ink, shape)
            .padding(horizontal = 9.dp, vertical = 2.dp),
    ) {
        Text(
            text = "Lv $level",
            style = LilitaNumberStyle.copy(fontSize = 13.sp),
            color = tokens.ink,
        )
    }
}

/**
 * Candy-bar EXP bar with diagonal stripes on honey fill and Level sticker on the left (DRD §10.8).
 */
@Composable
fun ExpProgressBar(
    totalExp: Int,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    showHeader: Boolean = true,
) {
    val tokens = StudioTheme.tokens
    val level = ExpConfig.levelForExp(totalExp)
    val expInLevel = ExpConfig.expIntoLevel(totalExp)
    val targetProgress = ExpConfig.progressForExp(totalExp)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 280),
        label = "exp_progress",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LevelTag(level = level)
                Text(
                    text = "$expInLevel / ${ExpConfig.EXP_PER_LEVEL} EXP",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.ink,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        val pill = RoundedCornerShape(999.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(pill)
                .background(tokens.paperShade)
                .border(2.dp, tokens.ink, pill),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .height(16.dp),
            ) {
                drawRect(color = tokens.brandHoney, size = size)
                clipRect(0f, 0f, size.width, size.height) {
                    val step = 10.dp.toPx()
                    var x = -size.height
                    while (x < size.width + size.height) {
                        drawLine(
                            color = tokens.brandHoneyDark.copy(alpha = 0.45f),
                            start = Offset(x, size.height),
                            end = Offset(x + size.height, 0f),
                            strokeWidth = 4.dp.toPx(),
                        )
                        x += step
                    }
                }
            }
        }
    }
}

enum class BottomNavTab(
    val label: String,
    val icon: ImageVector,
    val testTag: String,
) {
    HOME("Home", Icons.Outlined.Home, "nav_home"),
    FRIENDS("Friends", Icons.Outlined.Group, "nav_friends"),
    RANKS("Ranks", Icons.Outlined.Leaderboard, "nav_leaderboard"),
    ME("Me", Icons.Outlined.Person, "nav_profile"),
}

/**
 * Frosted blur-glass bar surface layered over blurred warm tabletop wood grain with specular
 * glass sheen and crisp ink divider edges. Uses RenderEffect blur on Android 12+ (API 31+)
 * with translucent frosted tint fallback on earlier versions.
 */
@Composable
fun FrostedGlassBar(
    modifier: Modifier = Modifier,
    showTopEdge: Boolean = false,
    showBottomEdge: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = StudioTheme.tokens
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(16.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RectangleShape),
    ) {
        // Blurred tabletop wood + warm bokeh backdrop layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(blurModifier)
                .background(tokens.woodMid.copy(alpha = 0.72f))
                .drawBehind {
                    drawWoodTexture(
                        woodLight = tokens.woodLight,
                        woodMid = tokens.woodMid,
                        woodDark = tokens.woodDark,
                        showKnot = false,
                    )
                    drawCircle(
                        color = tokens.warmHighlight.copy(alpha = 0.24f),
                        radius = size.height * 0.85f,
                        center = Offset(size.width * 0.22f, size.height * 0.35f),
                    )
                    drawCircle(
                        color = tokens.brandHoney.copy(alpha = 0.22f),
                        radius = size.height * 0.75f,
                        center = Offset(size.width * 0.78f, size.height * 0.60f),
                    )
                },
        )

        // Frosted translucent glass glaze & diagonal sheen
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tokens.warmHighlight.copy(alpha = 0.24f),
                            tokens.woodMid.copy(alpha = 0.40f),
                            tokens.woodDark.copy(alpha = 0.50f),
                        ),
                    ),
                )
                .drawBehind {
                    drawLine(
                        color = tokens.warmHighlight.copy(alpha = 0.14f),
                        start = Offset(size.width * 0.10f, 0f),
                        end = Offset(size.width * 0.34f, size.height),
                        strokeWidth = 28.dp.toPx(),
                    )
                    drawLine(
                        color = tokens.warmHighlight.copy(alpha = 0.10f),
                        start = Offset(size.width * 0.62f, 0f),
                        end = Offset(size.width * 0.80f, size.height),
                        strokeWidth = 18.dp.toPx(),
                    )
                },
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            if (showTopEdge) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(tokens.ink),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(tokens.warmHighlight.copy(alpha = 0.46f)),
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                content = content,
            )
            if (showBottomEdge) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(tokens.warmHighlight.copy(alpha = 0.32f)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(tokens.ink),
                )
            }
        }
    }
}

@Composable
fun CuteNavIcon(
    tab: BottomNavTab,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.semantics { contentDescription = tab.label },
    ) {
        when (tab) {
            BottomNavTab.HOME -> CuteHomeIcon(size = size)
            BottomNavTab.FRIENDS -> CuteFriendsIcon(size = size)
            BottomNavTab.RANKS -> CuteRanksIcon(size = size)
            BottomNavTab.ME -> CuteMeIcon(size = size)
        }
    }
}

/**
 * Frosted blur-glass bottom navigation bar with 3dp ink top edge, floating paper sticker active circle,
 * cute illustrated nav icons, and optional berry badge on Friends (DRD §10.5 & Addendum §8).
 */
@Composable
fun StudioBottomNav(
    selectedTab: BottomNavTab,
    onSelectTab: (BottomNavTab) -> Unit,
    pendingFriendRequests: Int = 0,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    FrostedGlassBar(
        showTopEdge = true,
        modifier = modifier.testTag("studio_bottom_nav"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 6.dp)
                        .testTag(tab.testTag),
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(y = if (isSelected) (-6).dp else 0.dp)
                                .size(if (isSelected) 38.dp else 28.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .clip(CircleShape)
                                            .background(tokens.paperCream)
                                            .border(2.5.dp, tokens.ink, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                        ) {
                            CuteNavIcon(
                                tab = tab,
                                size = if (isSelected) 24.dp else 22.dp,
                            )
                        }
                        if (tab == BottomNavTab.FRIENDS && pendingFriendRequests > 0) {
                            val badgeText = if (pendingFriendRequests > 9) "9+" else "$pendingFriendRequests"
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .offset(x = 6.dp, y = if (isSelected) (-8).dp else (-4).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(tokens.brandBerry)
                                    .border(1.5.dp, tokens.ink, CircleShape)
                                    .testTag("friends_nav_badge"),
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = tokens.warmHighlight,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (isSelected) tokens.brandHoney else tokens.paperCream,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun DiamondWalletPill(
    diamonds: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val tokens = StudioTheme.tokens
    val pillShape = RoundedCornerShape(999.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(pillShape)
            .background(tokens.paperCream)
            .border(2.5.dp, tokens.ink, pillShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        DiamondIcon(size = 18.dp)
        Text(
            text = diamonds.coerceAtLeast(0).toString(),
            style = LilitaNumberStyle.copy(fontSize = 17.sp),
            color = tokens.ink,
            maxLines = 1,
        )
    }
}

fun letterFontTextStyle(font: LetterFontId, fontSize: TextUnit): TextStyle {
    return when (font) {
        LetterFontId.BOLD_BLOCK -> TextStyle(
            fontFamily = LilitaOneFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
        )
        LetterFontId.ROUNDED_BUBBLE -> TextStyle(
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
        )
        LetterFontId.STICKER_OUTLINE -> TextStyle(
            fontFamily = LilitaOneFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = 0.8.sp,
        )
        LetterFontId.SHADOW_RIBBON -> TextStyle(
            fontFamily = LilitaOneFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
        )
        LetterFontId.HANDWRITTEN_MARKER -> TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSize = fontSize,
        )
        LetterFontId.WOODBLOCK_SERIF -> TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
        )
        LetterFontId.TYPEWRITER_STAMP -> TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
        )
    }
}

@Composable
fun ColorSwatchRow(
    selected: PlayerColorOption,
    onSelect: (PlayerColorOption) -> Unit,
    modifier: Modifier = Modifier,
    disabledColor: PlayerColorOption? = null,
    testTagPrefix: String = "color_swatch",
) {
    val tokens = StudioTheme.tokens
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PlayerColorOption.entries.forEach { option ->
            val isSelected = option == selected
            val isDisabled = option == disabledColor
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(if (isDisabled) option.color.copy(alpha = 0.25f) else option.color)
                    .border(
                        width = if (isSelected) 4.dp else 2.5.dp,
                        color = if (isDisabled) tokens.paperLine else tokens.ink,
                        shape = CircleShape,
                    )
                    .clickable(enabled = !isDisabled) { onSelect(option) }
                    .testTag("${testTagPrefix}_${option.name.lowercase()}"),
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "${option.label} selected",
                        tint = if (option.usesLightOutlineText) tokens.warmHighlight else tokens.ink,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun PatternPickerRow(
    selected: PlayerPattern,
    colorOption: PlayerColorOption,
    onSelect: (PlayerPattern) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "pattern_chip",
) {
    val tokens = StudioTheme.tokens
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        PlayerPattern.entries.forEach { pattern ->
            val isSelected = pattern == selected
            val shape = RoundedCornerShape(14.dp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(if (isSelected) tokens.brandHoney else tokens.paperCream)
                    .border(if (isSelected) 3.dp else 2.dp, tokens.ink, shape)
                    .clickable { onSelect(pattern) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("${testTagPrefix}_${pattern.name.lowercase()}"),
            ) {
                Canvas(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorOption.color)
                        .border(1.5.dp, tokens.ink, RoundedCornerShape(6.dp)),
                ) {
                    drawPlayerPattern(
                        pattern = pattern,
                        topLeft = Offset.Zero,
                        boxSize = size,
                        tint = tokens.ink,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pattern.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.ink,
                    maxLines = 1,
                )
            }
        }
    }
}
