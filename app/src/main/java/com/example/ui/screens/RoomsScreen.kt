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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FriendEntry
import com.example.model.FriendStatus
import com.example.model.MockRoom
import com.example.model.RoomVisibility
import com.example.model.WaitingRoomState
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.FeltBackground
import com.example.ui.components.GenderBadge
import com.example.ui.components.LevelTag
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioChip
import com.example.ui.components.StudioDangerButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.StudioTextButton
import com.example.ui.components.StudioTextField
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.TeddyBiscuit
import com.example.ui.components.TeddyExpression
import com.example.ui.components.TeddyPose
import com.example.ui.components.drawWoodTexture
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.StudioTheme

@Composable
fun RoomsScreen(
    publicRooms: List<MockRoom>,
    roomsNotice: String?,
    onBack: () -> Unit,
    onCreateRoom: (RoomVisibility) -> Unit,
    onJoinByCode: (String) -> Boolean,
    onJoinPublicRoom: (MockRoom) -> Unit,
    onDismissNotice: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    var selectedVisibility by remember { mutableStateOf(RoomVisibility.PRIVATE) }
    var codeInput by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    FeltBackground(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp),
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
                                .testTag("rooms_back_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = tokens.ink,
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(tokens.woodMid)
                                .drawBehind {
                                    drawWoodTexture(tokens.woodLight, tokens.woodMid, tokens.woodDark)
                                }
                                .border(3.dp, tokens.ink, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Column {
                                ChunkyDisplayText(
                                    text = "Rooms",
                                    style = MaterialTheme.typography.displaySmall,
                                    fillColor = tokens.paperCream,
                                    shadowColor = tokens.brandHoney,
                                    strokeWidth = 2.dp,
                                    shadowOffset = 2.dp,
                                )
                                Text(
                                    text = "Create a private table or join with a 6-character code.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.paperCream,
                                )
                            }
                        }
                    }
                }

                if (roomsNotice != null) {
                    item {
                        StudioCard(
                            backgroundColor = tokens.brandHoney.copy(alpha = 0.30f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    text = roomsNotice,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.ink,
                                    modifier = Modifier.weight(1f),
                                )
                                StudioTextButton(text = "OK", onClick = onDismissNotice)
                            }
                        }
                    }
                }

                // Create Room Card (Addendum §7.2)
                item {
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Create room",
                                style = MaterialTheme.typography.headlineSmall,
                                color = tokens.ink,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Private rooms are code-only. Public rooms appear in the list below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink.copy(alpha = 0.78f),
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                RoomVisibility.entries.forEach { vis ->
                                    StudioChip(
                                        label = vis.label,
                                        selected = selectedVisibility == vis,
                                        onClick = { selectedVisibility = vis },
                                        modifier = Modifier.weight(1f),
                                        testTag = "rooms_visibility_${vis.name.lowercase()}",
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            StudioPrimaryButton(
                                text = "Create ${selectedVisibility.label.lowercase()} room",
                                onClick = { onCreateRoom(selectedVisibility) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "rooms_create_button",
                            )
                        }
                    }
                }

                // Join by Code Card (Addendum §7.2)
                item {
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Join by code",
                                style = MaterialTheme.typography.headlineSmall,
                                color = tokens.ink,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                StudioTextField(
                                    value = codeInput,
                                    onValueChange = {
                                        codeInput = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(6)
                                        codeError = null
                                    },
                                    label = "",
                                    placeholder = "6-CHAR CODE",
                                    errorText = codeError,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Go,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onGo = {
                                            if (!onJoinByCode(codeInput)) {
                                                codeError = "Room not found."
                                            }
                                        },
                                    ),
                                    modifier = Modifier.weight(1f),
                                    testTag = "rooms_join_code_input",
                                )
                                StudioSecondaryButton(
                                    text = "Join",
                                    onClick = {
                                        if (!onJoinByCode(codeInput)) {
                                            codeError = "Room not found."
                                        }
                                    },
                                    modifier = Modifier.width(92.dp),
                                    testTag = "rooms_join_code_button",
                                )
                            }
                        }
                    }
                }

                item {
                    StickerBadge(
                        text = "PUBLIC ROOMS (${publicRooms.size})",
                        backgroundColor = tokens.paperCream,
                        rotationDegrees = -1f,
                    )
                }

                items(publicRooms, key = { it.id }) { room ->
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                TeddyAvatar(
                                    config = room.hostAvatar,
                                    backdropColor = room.hostColor.color,
                                    size = 44.dp,
                                    equippedCosmetics = room.hostCosmetics,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = room.hostName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = tokens.ink,
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        GenderBadge(gender = room.hostGender)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        LevelTag(level = room.hostLevel)
                                    }
                                    Text(
                                        text = room.statusLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.ink.copy(alpha = 0.78f),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                                drawLine(
                                    color = tokens.paperLine,
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width, size.height / 2f),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()), 0f),
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = room.code,
                                    style = LilitaNumberStyle.copy(fontSize = 22.sp),
                                    color = tokens.ink,
                                )
                                StudioPrimaryButton(
                                    text = "Join",
                                    onClick = { onJoinPublicRoom(room) },
                                    modifier = Modifier.width(110.dp),
                                    testTag = "rooms_join_${room.id}",
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun RoomWaitingScreen(
    waitingRoom: WaitingRoomState,
    friends: List<FriendEntry>,
    onCopyCode: (String) -> Unit,
    onInviteFriend: (FriendEntry) -> Unit,
    onSimulateGuestJoin: () -> Unit,
    onStartMatch: () -> Unit,
    onLeaveRoom: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    val clipboard = LocalClipboardManager.current
    var showInviteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        onLeaveRoom()
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            containerColor = tokens.paperCream,
            titleContentColor = tokens.ink,
            textContentColor = tokens.ink,
            title = {
                Text("Invite friend to ${waitingRoom.code}", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                if (friends.isEmpty()) {
                    Text(
                        text = "Add friends in the Friends tab to invite them here.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        friends.forEach { friend ->
                            val dotColor = when (friend.status) {
                                FriendStatus.ONLINE -> tokens.brandMint
                                FriendStatus.IN_MATCH -> tokens.brandHoney
                                FriendStatus.OFFLINE -> tokens.paperLine
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.paperShade)
                                    .border(2.dp, tokens.ink, RoundedCornerShape(14.dp))
                                    .clickable {
                                        showInviteDialog = false
                                        onInviteFriend(friend)
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = friend.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.ink,
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                                .border(1.dp, tokens.ink, CircleShape),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = friend.status.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.ink.copy(alpha = 0.75f),
                                        )
                                    }
                                }
                                Text(
                                    text = "Send",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = tokens.ink,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                StudioTextButton(text = "Close", onClick = { showInviteDialog = false })
            },
        )
    }

    FeltBackground(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                            .clickable(onClick = onLeaveRoom)
                            .testTag("waiting_room_back"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Leave room",
                            tint = tokens.ink,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    ChunkyDisplayText(
                        text = "Room ${waitingRoom.code}",
                        style = MaterialTheme.typography.displaySmall,
                        fillColor = tokens.paperCream,
                        shadowColor = tokens.brandHoney,
                        strokeWidth = 2.dp,
                        shadowOffset = 2.dp,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Room Code Ticket Card
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                    ) {
                        StickerBadge(
                            text = "${waitingRoom.visibility.label.uppercase()} ROOM",
                            backgroundColor = tokens.brandHoney,
                            rotationDegrees = -2f,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = waitingRoom.code,
                            style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 3.sp),
                            color = tokens.ink,
                            modifier = Modifier.testTag("waiting_room_code_text"),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        StudioSecondaryButton(
                            text = "Copy code",
                            onClick = {
                                clipboard.setText(AnnotatedString(waitingRoom.code))
                                onCopyCode(waitingRoom.code)
                            },
                            testTag = "waiting_room_copy_code",
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Host and Guest Slots (Addendum §7.3)
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Table seats",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                        )
                        // Host slot
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(tokens.paperShade)
                                .border(2.dp, tokens.ink, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                        ) {
                            TeddyAvatar(
                                config = waitingRoom.hostPlayer.avatarConfig,
                                backdropColor = waitingRoom.hostPlayer.color.color,
                                size = 46.dp,
                                equippedCosmetics = waitingRoom.hostPlayer.equippedCosmetics,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = waitingRoom.hostPlayer.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                )
                                Text(
                                    text = if (waitingRoom.isLocalHost) "Host · You" else "Host",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.ink.copy(alpha = 0.75f),
                                )
                            }
                            LevelTag(level = waitingRoom.hostPlayer.level ?: 1)
                        }

                        // Guest slot
                        val guest = waitingRoom.guestPlayer
                        if (guest != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(tokens.brandMint.copy(alpha = 0.22f))
                                    .border(2.dp, tokens.ink, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                            ) {
                                TeddyAvatar(
                                    config = guest.avatarConfig,
                                    backdropColor = guest.color.color,
                                    size = 46.dp,
                                    equippedCosmetics = guest.equippedCosmetics,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = guest.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.ink,
                                    )
                                    Text(
                                        text = if (waitingRoom.isLocalHost) "Guest · Ready" else "Guest · You",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink.copy(alpha = 0.78f),
                                    )
                                }
                                LevelTag(level = guest.level ?: 1)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(tokens.paperShade.copy(alpha = 0.6f))
                                    .border(2.dp, tokens.paperLine, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                            ) {
                                TeddyBiscuit(
                                    pose = TeddyPose.PEEKING,
                                    expression = TeddyExpression.THINKING,
                                    size = 48.dp,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (waitingRoom.isSimulatingGuestJoin) {
                                            "Joining room..."
                                        } else {
                                            "Waiting for friend..."
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                        color = tokens.ink,
                                    )
                                    Text(
                                        text = "Invite a friend or share the code above.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink.copy(alpha = 0.72f),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    StudioSecondaryButton(
                        text = "Invite friend",
                        onClick = { showInviteDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "waiting_room_invite_friend",
                    )
                    if (waitingRoom.guestPlayer == null && !waitingRoom.isSimulatingGuestJoin) {
                        StudioSecondaryButton(
                            text = "Test guest",
                            onClick = onSimulateGuestJoin,
                            modifier = Modifier.weight(0.9f),
                            testTag = "waiting_room_simulate_guest",
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (waitingRoom.isLocalHost) {
                    StudioPrimaryButton(
                        text = "Start match",
                        enabled = waitingRoom.guestPlayer != null,
                        onClick = onStartMatch,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "waiting_room_start_match",
                    )
                } else {
                    Text(
                        text = "Joined as guest. Ready to start!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.paperCream,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StudioPrimaryButton(
                        text = "Enter match",
                        onClick = onStartMatch,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "waiting_room_start_match",
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                StudioDangerButton(
                    text = "Leave room",
                    onClick = onLeaveRoom,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "waiting_room_leave_button",
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
