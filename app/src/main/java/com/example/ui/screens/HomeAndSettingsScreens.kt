package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PersistedMatchSession
import com.example.model.AppSettings
import com.example.model.BoxSkinId
import com.example.model.EquippedCosmetics
import com.example.model.ExpConfig
import com.example.model.OfflineMatchConfig
import com.example.model.PlayerColorOption
import com.example.model.PlayerPattern
import com.example.model.TeddyAvatarConfig
import com.example.model.TeddyBodyColor
import com.example.model.UserProfile
import com.example.model.WalletState
import com.example.model.deriveInitialsFromName
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.CuteStoreIcon
import com.example.ui.components.DiamondIcon
import com.example.ui.components.DiamondWalletPill
import com.example.ui.components.ExpProgressBar
import com.example.ui.components.FrostedGlassBar
import com.example.ui.components.LevelTag
import com.example.ui.components.PaperBackground
import com.example.ui.components.PatternPickerRow
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioDangerButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.StudioTextField
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.TeddyBiscuit
import com.example.ui.components.TeddyExpression
import com.example.ui.components.TeddyPose
import com.example.ui.components.WoodBackground
import com.example.ui.components.drawBoxSkinDetail
import com.example.ui.components.drawWoodTexture
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.LilitaOneFontFamily
import com.example.ui.theme.StudioTheme
import kotlin.math.roundToInt

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    wallet: WalletState,
    openRoomsCount: Int,
    savedMatch: PersistedMatchSession?,
    isSearchingOnline: Boolean = false,
    onOpenOfflineSetup: () -> Unit,
    onOpenLobby: () -> Unit,
    onCancelOnlineSearch: () -> Unit = {},
    onOpenRooms: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCreateProfile: () -> Unit,
    onResumeSavedMatch: () -> Unit,
    onDiscardSavedMatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val density = LocalDensity.current
    var headerHeightDp by remember { mutableStateOf(78.dp) }

    WoodBackground(
        showKnot = true,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Spacer(modifier = Modifier.height(headerHeightDp))

                // Teddy avatar on the left of bold outlined Player Name + LV rank + EXP bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = if (userProfile != null) onOpenProfile else onOpenCreateProfile)
                        .padding(vertical = 4.dp)
                        .testTag("home_player_header"),
                ) {
                    TeddyAvatar(
                        config = userProfile?.avatarConfig ?: TeddyAvatarConfig(),
                        backdropColor = (userProfile?.colorOption ?: PlayerColorOption.HONEY).color,
                        size = 56.dp,
                        equippedCosmetics = userProfile?.equippedCosmetics ?: EquippedCosmetics(),
                        modifier = Modifier.testTag("home_player_avatar"),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ChunkyDisplayText(
                                text = userProfile?.displayName ?: "Guest",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = LilitaOneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                ),
                                fillColor = tokens.brandHoney,
                                shadowColor = tokens.ink,
                                strokeWidth = 2.2.dp,
                                shadowOffset = 2.dp,
                                modifier = Modifier.testTag("home_player_name"),
                            )
                            LevelTag(level = userProfile?.level ?: 1)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ExpProgressBar(
                                totalExp = userProfile?.totalExp ?: 0,
                                showHeader = false,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${userProfile?.expIntoLevel ?: 0}/${ExpConfig.EXP_PER_LEVEL} EXP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = tokens.paperCream,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resume active match card if saved
                if (savedMatch != null && !savedMatch.matchState.isMatchComplete) {
                    StudioCard(
                        borderColor = tokens.ink,
                        backgroundColor = tokens.paperCream,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                StickerBadge(
                                    text = if (savedMatch.isOnline) "ACTIVE ONLINE" else "ACTIVE OFFLINE",
                                    backgroundColor = tokens.brandHoney,
                                    rotationDegrees = -2f,
                                )
                                Text(
                                    text = "Level ${savedMatch.matchState.currentLevel.levelNumber}/3",
                                    style = LilitaNumberStyle.copy(fontSize = 18.sp),
                                    color = tokens.ink,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${savedMatch.player1.name} vs ${savedMatch.player2.name}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = tokens.ink,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Score ${savedMatch.matchState.totalP1Score} – ${savedMatch.matchState.totalP2Score}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink.copy(alpha = 0.8f),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                StudioPrimaryButton(
                                    text = "Resume",
                                    onClick = onResumeSavedMatch,
                                    modifier = Modifier.weight(1.3f),
                                    testTag = "home_resume_match_button",
                                )
                                StudioDangerButton(
                                    text = "Discard",
                                    onClick = onDiscardSavedMatch,
                                    modifier = Modifier.weight(1f),
                                    testTag = "home_discard_match_button",
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3 separate big, bulky, bold-font mode buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HomeBigModeButton(
                        title = "Start Online",
                        subtitle = "Quick match & online friends",
                        backgroundColor = Color(0xFFF5C166),
                        hasBrightWoodTexture = true,
                        onClick = {
                            if (userProfile == null) onOpenCreateProfile() else onOpenLobby()
                        },
                        testTag = "home_start_online_button",
                    )
                    HomeBigModeButton(
                        title = "Play 1 v 1 Offline",
                        subtitle = "Pass & play on this device",
                        backgroundColor = Color.White,
                        onClick = onOpenOfflineSetup,
                        testTag = "home_offline_mode",
                    )
                    HomeBigModeButton(
                        title = "Rooms",
                        subtitle = "$openRoomsCount open · Create or join by code",
                        backgroundColor = Color.White,
                        onClick = {
                            if (userProfile == null) onOpenCreateProfile() else onOpenRooms()
                        },
                        testTag = "home_rooms_button",
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Frosted blur-glass top header bar pinned above scrollable Home content
            FrostedGlassBar(
                showBottomEdge = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onSizeChanged {
                        val measured = with(density) { it.height.toDp() }
                        if (measured > 0.dp) headerHeightDp = measured
                    }
                    .testTag("home_header_bar"),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            ChunkyDisplayText(
                                text = "Dots & Boxes",
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp),
                                fillColor = tokens.paperCream,
                                shadowColor = tokens.brandHoney,
                                strokeWidth = 2.5.dp,
                                shadowOffset = 2.5.dp,
                            )
                        }
                        DiamondWalletPill(
                            diamonds = wallet.diamonds,
                            onClick = onOpenStore,
                            testTag = "home_wallet_pill",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(tokens.brandHoney)
                                .border(2.5.dp, tokens.ink, CircleShape)
                                .clickable(onClick = onOpenStore)
                                .semantics { contentDescription = "Store" }
                                .testTag("home_store_button"),
                        ) {
                            CuteStoreIcon(size = 30.dp)
                        }
                    }
                }
            }

            if (isSearchingOnline) {
                BackHandler(onBack = onCancelOnlineSearch)

                var dotCount by remember { mutableStateOf(1) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(420L)
                        dotCount = (dotCount % 3) + 1
                    }
                }
                val dots = ".".repeat(dotCount)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_matchmaking_overlay"),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tokens.ink.copy(alpha = 0.56f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                    )
                    StudioCard(
                        backgroundColor = tokens.paperCream,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .widthIn(max = 380.dp)
                            .fillMaxWidth(),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 24.dp),
                        ) {
                            TeddyBiscuit(
                                pose = TeddyPose.PENCIL,
                                expression = TeddyExpression.THINKING,
                                size = 86.dp,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Searching for opponent$dots",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = LilitaOneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = tokens.ink,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Matching you into a 3-board online game.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink.copy(alpha = 0.78f),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            StudioDangerButton(
                                text = "Cancel",
                                onClick = onCancelOnlineSearch,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "home_cancel_matchmaking_button",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeBigModeButton(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    hasBrightWoodTexture: Boolean = false,
) {
    val tokens = StudioTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowDp = 5.dp
    val pressShift = if (isPressed) 3.dp else 0.dp
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = shadowDp, end = shadowDp)
            .drawBehind {
                val offsetPx = (shadowDp - pressShift).toPx().coerceAtLeast(1f)
                drawRoundRect(
                    color = tokens.ink,
                    topLeft = Offset(offsetPx, offsetPx),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                )
            }
            .offset {
                val px = pressShift.toPx().roundToInt()
                IntOffset(px, px)
            }
            .heightIn(min = 78.dp)
            .clip(shape)
            .background(backgroundColor)
            .drawBehind {
                if (hasBrightWoodTexture) {
                    drawWoodTexture(
                        woodLight = Color(0xFFFFE1A3),
                        woodMid = Color(0xFFF5C166),
                        woodDark = Color(0xFFC9862A),
                        showKnot = false,
                    )
                    // Subtle warm horizontal plank highlight for a sunlit oak sheen
                    drawLine(
                        color = Color.White.copy(alpha = 0.28f),
                        start = Offset(0f, 3.dp.toPx()),
                        end = Offset(size.width, 3.dp.toPx()),
                        strokeWidth = 3.dp.toPx(),
                    )
                }
            }
            .border(3.5.dp, tokens.ink, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag(testTag),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = LilitaOneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    ),
                    color = tokens.ink,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.ink.copy(alpha = 0.78f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = tokens.ink,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
fun OfflineSetupScreen(
    initialConfig: OfflineMatchConfig,
    userProfile: UserProfile?,
    onBack: () -> Unit,
    onStartMatch: (OfflineMatchConfig) -> Unit,
) {
    val tokens = StudioTheme.tokens
    var p1Name by remember {
        mutableStateOf(
            if (initialConfig.p1Name == "Player 1" && userProfile != null) userProfile.displayName else initialConfig.p1Name
        )
    }
    val p1Initial = deriveInitialsFromName(p1Name).ifEmpty { "P" }
    var p1Color by remember { mutableStateOf(userProfile?.colorOption ?: initialConfig.p1Color) }
    var p1Pattern by remember { mutableStateOf(userProfile?.pattern ?: initialConfig.p1Pattern) }
    val p1Avatar = userProfile?.avatarConfig ?: initialConfig.p1Avatar

    var p2Name by remember { mutableStateOf(initialConfig.p2Name) }
    val p2Initial = deriveInitialsFromName(p2Name).ifEmpty { "P" }
    var p2Color by remember {
        mutableStateOf(
            if (initialConfig.p2Color == p1Color) {
                PlayerColorOption.entries.first { it != p1Color }
            } else {
                initialConfig.p2Color
            }
        )
    }
    var p2Pattern by remember {
        mutableStateOf(
            if (initialConfig.p2Pattern == p1Pattern) {
                PlayerPattern.entries.first { it != p1Pattern }
            } else {
                initialConfig.p2Pattern
            }
        )
    }
    var p2Body by remember { mutableStateOf(initialConfig.p2Avatar.body) }
    var alternateStarter by remember { mutableStateOf(initialConfig.alternateStarter) }

    PaperBackground(
        showCreaseLines = false,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(tokens.paperCream)
                            .border(2.5.dp, tokens.ink, CircleShape)
                            .clickable(onClick = onBack)
                            .testTag("offline_setup_back"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = tokens.ink,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Offline 1v1",
                            style = MaterialTheme.typography.displaySmall,
                            color = tokens.ink,
                        )
                        Text(
                            text = "Set names, colors & box patterns.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink.copy(alpha = 0.78f),
                        )
                    }
                    TeddyBiscuit(
                        pose = TeddyPose.PENCIL,
                        expression = TeddyExpression.HAPPY,
                        size = 70.dp,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                PlayerSetupCard(
                    header = "PLAYER 1 · STARTS LEVEL 1",
                    name = p1Name,
                    onNameChange = {
                        if (it.length <= 16) p1Name = it
                    },
                    boxInitial = p1Initial,
                    selectedColor = p1Color,
                    disabledColor = p2Color,
                    onSelectColor = { p1Color = it },
                    selectedPattern = p1Pattern,
                    onSelectPattern = { p1Pattern = it },
                    avatarConfig = p1Avatar,
                    equippedCosmetics = userProfile?.equippedCosmetics ?: EquippedCosmetics(),
                    onCycleTeddy = null,
                    tagPrefix = "p1",
                )

                Spacer(modifier = Modifier.height(14.dp))

                PlayerSetupCard(
                    header = "PLAYER 2 (TAP TEDDY TO SWAP FUR)",
                    name = p2Name,
                    onNameChange = {
                        if (it.length <= 16) p2Name = it
                    },
                    boxInitial = p2Initial,
                    selectedColor = p2Color,
                    disabledColor = p1Color,
                    onSelectColor = { p2Color = it },
                    selectedPattern = p2Pattern,
                    onSelectPattern = { p2Pattern = it },
                    avatarConfig = initialConfig.p2Avatar.copy(body = p2Body),
                    equippedCosmetics = EquippedCosmetics(),
                    onCycleTeddy = {
                        val bodies = TeddyBodyColor.entries
                        p2Body = bodies[(bodies.indexOf(p2Body) + 1) % bodies.size]
                    },
                    tagPrefix = "p2",
                )

                Spacer(modifier = Modifier.height(14.dp))

                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alternate first turn",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                            )
                            Text(
                                text = "Who starts alternates on Level 2 and Level 3.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink.copy(alpha = 0.78f),
                            )
                        }
                        Switch(
                            checked = alternateStarter,
                            onCheckedChange = { alternateStarter = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = tokens.paperCream,
                                checkedTrackColor = tokens.brandMint,
                                checkedBorderColor = tokens.ink,
                                uncheckedThumbColor = tokens.ink,
                                uncheckedTrackColor = tokens.paperShade,
                                uncheckedBorderColor = tokens.ink,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                StudioPrimaryButton(
                    text = "Start match",
                    onClick = {
                        onStartMatch(
                            OfflineMatchConfig(
                                p1Name = p1Name.trim().ifBlank { "Player 1" },
                                p1Initials = p1Initial,
                                p1Color = p1Color,
                                p1Pattern = p1Pattern,
                                p1Avatar = p1Avatar,
                                p2Name = p2Name.trim().ifBlank { "Player 2" },
                                p2Initials = p2Initial,
                                p2Color = p2Color,
                                p2Pattern = p2Pattern,
                                p2Avatar = initialConfig.p2Avatar.copy(body = p2Body),
                                alternateStarter = alternateStarter,
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "offline_start_match_button",
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PlayerSetupCard(
    header: String,
    name: String,
    onNameChange: (String) -> Unit,
    boxInitial: String,
    selectedColor: PlayerColorOption,
    disabledColor: PlayerColorOption,
    onSelectColor: (PlayerColorOption) -> Unit,
    selectedPattern: PlayerPattern,
    onSelectPattern: (PlayerPattern) -> Unit,
    avatarConfig: com.example.model.TeddyAvatarConfig,
    equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    onCycleTeddy: (() -> Unit)?,
    tagPrefix: String,
) {
    val tokens = StudioTheme.tokens
    StudioCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.ink.copy(alpha = 0.78f),
                )
                Box(
                    modifier = Modifier.then(
                        if (onCycleTeddy != null) Modifier.clickable(onClick = onCycleTeddy) else Modifier
                    ),
                ) {
                    TeddyAvatar(
                        config = avatarConfig,
                        backdropColor = selectedColor.color,
                        size = 44.dp,
                        equippedCosmetics = equippedCosmetics,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StudioTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Name (box mark uses 1st letter)",
                    modifier = Modifier.weight(1f),
                    testTag = "${tagPrefix}_name_input",
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("${tagPrefix}_auto_initial_box"),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawBoxSkinDetail(
                            boxSkin = equippedCosmetics.boxSkin,
                            topLeft = Offset.Zero,
                            boxSize = size,
                            baseColor = selectedColor.color,
                            pattern = selectedPattern,
                            ink = tokens.ink,
                            highlight = tokens.warmHighlight,
                            honey = tokens.brandHoney,
                            woodDark = tokens.woodDark,
                        )
                    }
                    Text(
                        text = boxInitial.take(1).uppercase(),
                        style = LilitaNumberStyle.copy(fontSize = 20.sp),
                        color = if (selectedColor.usesLightOutlineText) tokens.warmHighlight else tokens.ink,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelMedium,
                color = tokens.ink,
            )
            Spacer(modifier = Modifier.height(6.dp))
            ColorSwatchRow(
                selected = selectedColor,
                onSelect = onSelectColor,
                disabledColor = disabledColor,
                testTagPrefix = "${tagPrefix}_color",
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pattern",
                style = MaterialTheme.typography.labelMedium,
                color = tokens.ink,
            )
            Spacer(modifier = Modifier.height(6.dp))
            PatternPickerRow(
                selected = selectedPattern,
                colorOption = selectedColor,
                onSelect = onSelectPattern,
                testTagPrefix = "${tagPrefix}_pattern",
            )
        }
    }
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onBack: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    PaperBackground(
        showCreaseLines = false,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(tokens.paperCream)
                            .border(2.5.dp, tokens.ink, CircleShape)
                            .clickable(onClick = onBack)
                            .testTag("settings_back"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = tokens.ink,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displaySmall,
                        color = tokens.ink,
                        modifier = Modifier.weight(1f),
                    )
                    TeddyBiscuit(
                        pose = TeddyPose.COCOA,
                        expression = TeddyExpression.HAPPY,
                        size = 74.dp,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingToggleRow(
                            title = "Sound",
                            subtitle = "Pencil scratch, wooden tock, chimes & jingles.",
                            checked = settings.soundEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(soundEnabled = v) } },
                            testTag = "setting_sound_toggle",
                        )
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                        SettingToggleRow(
                            title = "Haptics",
                            subtitle = "Gentle tap vibration when drawing a line.",
                            checked = settings.hapticsEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(hapticsEnabled = v) } },
                            testTag = "setting_haptics_toggle",
                        )
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                        SettingToggleRow(
                            title = "Pass-device prompt",
                            subtitle = "Hide board with a handoff card between Offline turns.",
                            checked = settings.passDevicePrompt,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(passDevicePrompt = v) } },
                            testTag = "setting_pass_device_toggle",
                        )
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                        SettingToggleRow(
                            title = "Reduced motion",
                            subtitle = "Disable bounce, line draw-in, stamp pop & teddy motion.",
                            checked = settings.reducedMotion,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(reducedMotion = v) } },
                            testTag = "setting_reduced_motion_toggle",
                        )
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                        SettingToggleRow(
                            title = "Dyslexia-friendly font",
                            subtitle = "Use Lexend with 1.6× line spacing for body text.",
                            checked = settings.dyslexiaFont,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(dyslexiaFont = v) } },
                            testTag = "setting_dyslexia_toggle",
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Tabletop style",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                            )
                            Text(
                                text = "Paper & Wood · v1.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink.copy(alpha = 0.78f),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dots & Boxes Prototype. Three-level strategy matches (4×4, 6×6, 8×8 dots = 83 boxes). Online rooms, chat, voice, and ₹9/month subscription are local simulations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink.copy(alpha = 0.80f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    val tokens = StudioTheme.tokens
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = tokens.ink,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.ink.copy(alpha = 0.78f),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = tokens.paperCream,
                checkedTrackColor = tokens.brandMint,
                checkedBorderColor = tokens.ink,
                uncheckedThumbColor = tokens.ink,
                uncheckedTrackColor = tokens.paperShade,
                uncheckedBorderColor = tokens.ink,
            ),
            modifier = Modifier.testTag(testTag),
        )
    }
}
