package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.DotsAndBoxesEngine
import com.example.engine.LineCoord
import com.example.engine.LineKind
import com.example.engine.PlayerSlot
import com.example.model.AppSettings
import com.example.model.FriendEntry
import com.example.model.FriendStatus
import com.example.model.PlayerIdentity
import com.example.model.SubscriptionState
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.DiamondIcon
import com.example.ui.components.DotsAndBoxesBoard
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.StudioTextButton
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.TeddyBiscuit
import com.example.ui.components.TeddyExpression
import com.example.ui.components.TeddyPose
import com.example.ui.components.WoodBackground
import com.example.ui.components.drawWoodTexture
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.LilitaOneFontFamily
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.ActiveRoomUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRoomScreen(
    roomState: ActiveRoomUiState,
    settings: AppSettings,
    subscription: SubscriptionState,
    friends: List<FriendEntry>,
    onLineTapped: (LineCoord) -> Unit,
    onDismissPassDevice: () -> Unit,
    onNextLevel: () -> Unit,
    onPlayAgain: () -> Unit,
    onLeaveConfirmed: () -> Unit,
    onSendChatMessage: (String) -> Unit,
    onToggleVoiceMute: () -> Unit,
    onToggleVoiceConnection: () -> Unit,
    onOpenPaywall: () -> Unit,
    onAddOpponentFriend: (PlayerIdentity) -> Unit,
    onInviteFriendInMatch: (FriendEntry) -> Unit,
    onWatchAdToDoubleDiamonds: () -> Unit = {},
) {
    val tokens = StudioTheme.tokens
    var showLeaveDialog by remember { mutableStateOf(false) }
    var activeSheetTab by remember { mutableStateOf<Int?>(null) }
    var sidePanelTab by remember { mutableIntStateOf(0) }
    var showRoomMenu by remember { mutableStateOf(false) }
    var showInviteFriendDialog by remember { mutableStateOf(false) }

    val match = roomState.matchState
    val isLive = !match.isMatchComplete

    fun handleBackAttempt() {
        if (isLive) showLeaveDialog = true else onLeaveConfirmed()
    }

    BackHandler(enabled = true) {
        handleBackAttempt()
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = tokens.paperCream,
            titleContentColor = tokens.ink,
            textContentColor = tokens.ink,
            title = {
                Text(
                    text = stringResource(R.string.leave_match_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                val isFriendMatch = roomState.isFriendMatch ||
                    (roomState.player2.playerId?.let { id -> friends.any { it.playerId == id } } ?: false)
                Text(
                    text = stringResource(
                        when {
                            roomState.isOnline && isFriendMatch -> R.string.leave_match_friend_body
                            roomState.isOnline -> R.string.leave_match_online_body
                            else -> R.string.leave_match_offline_body
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                StudioTextButton(
                    text = "Leave",
                    onClick = {
                        showLeaveDialog = false
                        onLeaveConfirmed()
                    },
                    contentColor = tokens.brandBerryDark,
                    testTag = "leave_match_confirm",
                )
            },
            dismissButton = {
                StudioTextButton(
                    text = "Stay",
                    onClick = { showLeaveDialog = false },
                    contentColor = tokens.ink,
                    testTag = "leave_match_cancel",
                )
            },
        )
    }

    if (showInviteFriendDialog && roomState.isOnline) {
        val onlineFriends = friends.filter { it.status == FriendStatus.ONLINE }
        AlertDialog(
            onDismissRequest = { showInviteFriendDialog = false },
            containerColor = tokens.paperCream,
            titleContentColor = tokens.ink,
            textContentColor = tokens.ink,
            title = { Text("Invite a friend", style = MaterialTheme.typography.headlineSmall) },
            text = {
                if (onlineFriends.isEmpty()) {
                    Text(
                        text = "No friends are currently online. Your match continues uninterrupted.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        onlineFriends.forEach { friend ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.paperShade)
                                    .border(2.dp, tokens.ink, RoundedCornerShape(14.dp))
                                    .clickable {
                                        showInviteFriendDialog = false
                                        onInviteFriendInMatch(friend)
                                    }
                                    .padding(10.dp),
                            ) {
                                TeddyAvatar(
                                    config = friend.avatarConfig,
                                    backdropColor = friend.color.color,
                                    size = 34.dp,
                                    equippedCosmetics = friend.equippedCosmetics,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = friend.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.ink,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "Invite",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = tokens.ink,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                StudioTextButton(text = "Close", onClick = { showInviteFriendDialog = false })
            },
        )
    }

    if (activeSheetTab != null && roomState.isOnline) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        // DRD §10.7: Paper bottom sheet with wood handle bar, 28dp top radius, 3dp ink outline
        ModalBottomSheet(
            onDismissRequest = { activeSheetTab = null },
            sheetState = sheetState,
            containerColor = tokens.paperCream,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(tokens.woodMid)
                            .border(2.dp, tokens.ink, RoundedCornerShape(999.dp)),
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 380.dp, max = 520.dp),
            ) {
                OnlinePanelTabBar(
                    selectedTab = activeSheetTab ?: 0,
                    voiceLocked = !subscription.isActive,
                    onSelectTab = { activeSheetTab = it },
                )
                HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                if (activeSheetTab == 0) {
                    RoomChatPanel(
                        messages = roomState.chatMessages,
                        onSendMessage = onSendChatMessage,
                    )
                } else {
                    RoomVoicePanel(
                        subscription = subscription,
                        participants = roomState.voiceParticipants,
                        isConnected = roomState.isVoiceConnected,
                        reducedMotion = settings.reducedMotion,
                        onToggleMute = onToggleVoiceMute,
                        onToggleConnect = onToggleVoiceConnection,
                        onOpenPaywall = {
                            activeSheetTab = null
                            onOpenPaywall()
                        },
                    )
                }
            }
        }
    }

    WoodBackground(
        showKnot = false,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            // Wood header bar with 3dp ink bottom edge (DRD §4.2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.woodMid)
                    .drawBehind {
                        drawWoodTexture(tokens.woodLight, tokens.woodMid, tokens.woodDark)
                    },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(tokens.paperCream)
                            .border(2.5.dp, tokens.ink, CircleShape)
                            .clickable { handleBackAttempt() }
                            .testTag("room_leave_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Leave room",
                            tint = tokens.ink,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (roomState.isOnline) {
                                "Room ${roomState.roomCode ?: ""}"
                            } else {
                                "Offline 1v1"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.paperCream,
                        )
                        Text(
                            text = "Level ${match.currentLevel.levelNumber}/3 · ${match.currentLevel.dotCount}×${match.currentLevel.dotCount} dots",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.paperCream,
                        )
                    }
                    StickerBadge(
                        text = "${match.totalP1Score + match.totalP2Score}/${DotsAndBoxesEngine.TOTAL_MATCH_BOXES}",
                        backgroundColor = tokens.brandHoney,
                        rotationDegrees = 2f,
                    )
                    if (roomState.isOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(tokens.paperCream)
                                    .border(2.dp, tokens.ink, CircleShape)
                                    .clickable { showRoomMenu = true }
                                    .testTag("room_social_menu_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PersonAdd,
                                    contentDescription = "Add or invite friend",
                                    tint = tokens.ink,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = showRoomMenu,
                                onDismissRequest = { showRoomMenu = false },
                                containerColor = tokens.paperCream,
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add ${roomState.player2.name} as friend", color = tokens.ink) },
                                    onClick = {
                                        showRoomMenu = false
                                        onAddOpponentFriend(roomState.player2)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Invite friend to match", color = tokens.ink) },
                                    onClick = {
                                        showRoomMenu = false
                                        showInviteFriendDialog = true
                                    },
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(tokens.ink),
                )
            }

            when {
                match.isMatchComplete -> {
                    MatchResultsPanel(
                        matchState = match,
                        player1 = roomState.player1,
                        player2 = roomState.player2,
                        completionSummary = roomState.completionSummary,
                        reducedMotion = settings.reducedMotion,
                        isOnline = roomState.isOnline,
                        isOpponentFriend = roomState.player2.playerId?.let { id -> friends.any { it.playerId == id } } ?: false,
                        onAddOpponentFriend = { onAddOpponentFriend(roomState.player2) },
                        onWatchAdToDoubleDiamonds = onWatchAdToDoubleDiamonds,
                        onPlayAgain = onPlayAgain,
                        onHome = onLeaveConfirmed,
                        modifier = Modifier.weight(1f),
                    )
                }

                match.isAwaitingNextLevel -> {
                    LevelTransitionPanel(
                        matchState = match,
                        player1 = roomState.player1,
                        player2 = roomState.player2,
                        reducedMotion = settings.reducedMotion,
                        onNextLevel = onNextLevel,
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val isWideLayout = maxWidth >= 680.dp

                        if (isWideLayout && roomState.isOnline) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                BoardStageColumn(
                                    roomState = roomState,
                                    settings = settings,
                                    onLineTapped = onLineTapped,
                                    onDismissPassDevice = onDismissPassDevice,
                                    modifier = Modifier
                                        .weight(1.25f)
                                        .fillMaxHeight(),
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .fillMaxHeight()
                                        .background(tokens.paperCream)
                                        .border(3.dp, tokens.ink),
                                ) {
                                    OnlinePanelTabBar(
                                        selectedTab = sidePanelTab,
                                        voiceLocked = !subscription.isActive,
                                        onSelectTab = { sidePanelTab = it },
                                    )
                                    HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                                    if (sidePanelTab == 0) {
                                        RoomChatPanel(
                                            messages = roomState.chatMessages,
                                            onSendMessage = onSendChatMessage,
                                        )
                                    } else {
                                        RoomVoicePanel(
                                            subscription = subscription,
                                            participants = roomState.voiceParticipants,
                                            isConnected = roomState.isVoiceConnected,
                                            reducedMotion = settings.reducedMotion,
                                            onToggleMute = onToggleVoiceMute,
                                            onToggleConnect = onToggleVoiceConnection,
                                            onOpenPaywall = onOpenPaywall,
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                BoardStageColumn(
                                    roomState = roomState,
                                    settings = settings,
                                    onLineTapped = onLineTapped,
                                    onDismissPassDevice = onDismissPassDevice,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                )
                                if (roomState.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(tokens.ink),
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(tokens.woodMid)
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                    ) {
                                        StudioSecondaryButton(
                                            text = "Chat (${roomState.chatMessages.size})",
                                            onClick = { activeSheetTab = 0 },
                                            modifier = Modifier.weight(1f),
                                            testTag = "room_open_chat_button",
                                        )
                                        if (subscription.isActive) {
                                            StudioSecondaryButton(
                                                text = "Voice",
                                                onClick = { activeSheetTab = 1 },
                                                modifier = Modifier.weight(1f),
                                                testTag = "room_open_voice_button",
                                            )
                                        } else {
                                            StudioPrimaryButton(
                                                text = "Voice · Locked",
                                                onClick = { activeSheetTab = 1 },
                                                modifier = Modifier.weight(1f),
                                                testTag = "room_open_voice_button",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardStageColumn(
    roomState: ActiveRoomUiState,
    settings: AppSettings,
    onLineTapped: (LineCoord) -> Unit,
    onDismissPassDevice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val match = roomState.matchState
    val level = match.currentLevel
    val activeSlot = level.activePlayer
    val activePlayer = if (activeSlot == PlayerSlot.PLAYER_1) roomState.player1 else roomState.player2

    Column(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        // Top-left & Top-right Score Chips (DRD §8.5)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ScorePillChip(
                identity = roomState.player1,
                levelScore = level.p1Score,
                matchScore = match.totalP1Score,
                diamondsEarned = match.totalP1Diamonds,
                isActive = activeSlot == PlayerSlot.PLAYER_1,
                modifier = Modifier.weight(1f),
                testTag = "score_card_p1",
            )
            ScorePillChip(
                identity = roomState.player2,
                levelScore = level.p2Score,
                matchScore = match.totalP2Score,
                diamondsEarned = match.totalP2Diamonds,
                isActive = activeSlot == PlayerSlot.PLAYER_2,
                modifier = Modifier.weight(1f),
                testTag = "score_card_p2",
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Turn Indicator Pill above board (DRD §8.5)
        val turnPillShape = RoundedCornerShape(999.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(turnPillShape)
                .background(activePlayer.color.color)
                .border(2.5.dp, tokens.ink, turnPillShape)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            TeddyAvatar(
                config = activePlayer.avatarConfig,
                backdropColor = tokens.paperCream,
                size = 24.dp,
                isCircle = true,
                equippedCosmetics = activePlayer.equippedCosmetics,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    roomState.isOnline && activeSlot == PlayerSlot.PLAYER_2 -> "${activePlayer.name} thinking..."
                    roomState.isOnline -> "Your turn"
                    else -> "${activePlayer.name}'s turn"
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (activePlayer.color.usesLightOutlineText) tokens.warmHighlight else tokens.ink,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Round label centered above board + little plain white fraction in top-right corner above board
        val isFinalRound = level.levelNumber >= 3
        val roundLabel = if (isFinalRound) "Final Round" else "Round ${level.levelNumber}"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
        ) {
            ChunkyDisplayText(
                text = roundLabel,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = LilitaOneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isFinalRound) 25.sp else 23.sp,
                ),
                fillColor = Color(0xFFFF6FA8),
                shadowColor = Color(0xFF6E0E38),
                outlineColor = Color(0xFF9C164F),
                strokeWidth = 1.6.dp,
                shadowOffset = 1.5.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("board_round_label"),
            )
            Text(
                text = "${level.levelNumber}/3",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .testTag("board_round_fraction"),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Board Centerpiece
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (roomState.showPassDeviceOverlay) {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        TeddyAvatar(
                            config = activePlayer.avatarConfig,
                            backdropColor = activePlayer.color.color,
                            size = 64.dp,
                            equippedCosmetics = activePlayer.equippedCosmetics,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Pass device to ${activePlayer.name}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = tokens.ink,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Board is hidden between turns.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink.copy(alpha = 0.78f),
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        StudioPrimaryButton(
                            text = "Ready",
                            onClick = onDismissPassDevice,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "pass_device_ready_button",
                        )
                    }
                }
            } else {
                val canTap = !roomState.isOnline || activeSlot == PlayerSlot.PLAYER_1
                DotsAndBoxesBoard(
                    levelState = level,
                    p1Initials = roomState.player1.initials,
                    p2Initials = roomState.player2.initials,
                    p1Color = roomState.player1.color.color,
                    p2Color = roomState.player2.color.color,
                    p1Pattern = roomState.player1.pattern,
                    p2Pattern = roomState.player2.pattern,
                    p1BoxSkin = roomState.player1.equippedCosmetics.boxSkin,
                    p2BoxSkin = roomState.player2.equippedCosmetics.boxSkin,
                    p1LetterFont = roomState.player1.equippedCosmetics.letterFont,
                    p2LetterFont = roomState.player2.equippedCosmetics.letterFont,
                    interactionEnabled = canTap,
                    reducedMotion = settings.reducedMotion,
                    hapticsEnabled = settings.hapticsEnabled,
                    onLineTapped = onLineTapped,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom tabletop corner: Biscuit reacting (DRD §7.6 & §8.5) + last move note
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val moveText = level.lastMove?.let { move ->
                val dir = if (move.kind == LineKind.HORIZONTAL) "H" else "V"
                "Last line: $dir (${move.row + 1}, ${move.col + 1})"
            } ?: "Tap any dotted line to draw"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(tokens.paperCream)
                    .border(2.dp, tokens.ink, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = moveText,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.ink,
                )
            }

            // DRD §7.6: Thinking during turn, Celebrating after capture!
            val biscuitExpression = if (roomState.justClaimedBoxes) {
                TeddyExpression.CELEBRATING
            } else {
                TeddyExpression.THINKING
            }
            TeddyBiscuit(
                pose = TeddyPose.PENCIL,
                expression = biscuitExpression,
                size = 64.dp,
                reducedMotion = settings.reducedMotion,
                bounceKey = level.lastClaimedBoxes,
            )
        }
    }
}

@Composable
private fun ScorePillChip(
    identity: PlayerIdentity,
    levelScore: Int,
    matchScore: Int,
    diamondsEarned: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    val tokens = StudioTheme.tokens
    val pillShape = RoundedCornerShape(999.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(pillShape)
            .background(if (isActive) tokens.paperCream else tokens.paperShade)
            .border(if (isActive) 3.dp else 2.dp, tokens.ink, pillShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
    ) {
        TeddyAvatar(
            config = identity.avatarConfig,
            backdropColor = identity.color.color,
            size = 34.dp,
            isCircle = true,
            equippedCosmetics = identity.equippedCosmetics,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = identity.initials,
                style = MaterialTheme.typography.labelLarge,
                color = tokens.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tot $matchScore",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.ink.copy(alpha = 0.72f),
                )
                if (diamondsEarned > 0) {
                    Spacer(modifier = Modifier.width(5.dp))
                    DiamondIcon(size = 11.dp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$diamondsEarned",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.diamondTealDark,
                    )
                }
            }
        }
        Text(
            text = "$levelScore",
            style = LilitaNumberStyle.copy(fontSize = 26.sp),
            color = tokens.ink,
        )
    }
}

@Composable
private fun OnlinePanelTabBar(
    selectedTab: Int,
    voiceLocked: Boolean,
    onSelectTab: (Int) -> Unit,
) {
    val tokens = StudioTheme.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(tokens.paperCream),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (selectedTab == 0) tokens.brandHoney else tokens.paperShade)
                .clickable { onSelectTab(0) }
                .testTag("panel_tab_chat"),
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = tokens.ink,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Chat",
                style = MaterialTheme.typography.labelLarge,
                color = tokens.ink,
            )
        }
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(tokens.ink),
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (selectedTab == 1) tokens.brandMint else tokens.paperShade)
                .clickable { onSelectTab(1) }
                .testTag("panel_tab_voice"),
        ) {
            Icon(
                imageVector = if (voiceLocked) Icons.Outlined.Lock else Icons.Outlined.Mic,
                contentDescription = null,
                tint = tokens.ink,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Voice",
                style = MaterialTheme.typography.labelLarge,
                color = tokens.ink,
            )
        }
    }
}
