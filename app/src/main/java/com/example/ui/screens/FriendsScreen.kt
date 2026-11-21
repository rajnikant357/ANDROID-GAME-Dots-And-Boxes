package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FriendEntry
import com.example.model.FriendRequest
import com.example.model.FriendStatus
import com.example.model.IdCodeGenerator
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.FrostedGlassBar
import com.example.ui.components.LevelTag
import com.example.ui.components.PaperBackground
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.StudioTextButton
import com.example.ui.components.StudioTextField
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.TeddyBiscuit
import com.example.ui.components.TeddyExpression
import com.example.ui.components.TeddyPose
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.InviteBannerState
import com.example.viewmodel.PlayerSearchResult

@Composable
fun FriendsScreen(
    friends: List<FriendEntry>,
    incomingRequests: List<FriendRequest>,
    outgoingRequests: List<FriendRequest>,
    searchResult: PlayerSearchResult,
    inviteBanner: InviteBannerState?,
    onSearchById: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSendRequestToFound: (com.example.model.MockDirectoryPlayer) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeclineOrCancelRequest: (String) -> Unit,
    onInviteFriend: (FriendEntry) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onBlockPlayer: (String) -> Unit,
    onDismissInviteBanner: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    var searchQuery by remember { mutableStateOf("") }

    PaperBackground(
        showCreaseLines = false,
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
                    .verticalScroll(rememberScrollState()),
            ) {
                // Frosted Blur-Glass Header
                FrostedGlassBar(showBottomEdge = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        ChunkyDisplayText(
                            text = "Friends",
                            style = MaterialTheme.typography.displaySmall,
                            fillColor = tokens.paperCream,
                            shadowColor = tokens.brandHoney,
                            strokeWidth = 2.5.dp,
                            shadowOffset = 2.dp,
                        )
                        StickerBadge(
                            text = "${friends.size} / ${IdCodeGenerator.MAX_FRIENDS_LIMIT}",
                            backgroundColor = tokens.brandHoney,
                            rotationDegrees = 2f,
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    if (inviteBanner != null) {
                        StudioCard(
                            backgroundColor = if (inviteBanner.acceptedRoomReady) tokens.brandMint.copy(alpha = 0.25f) else tokens.brandHoney.copy(alpha = 0.25f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("friends_invite_banner"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    text = inviteBanner.statusText,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                    color = tokens.ink,
                                    modifier = Modifier.weight(1f),
                                )
                                StudioTextButton(
                                    text = "Dismiss",
                                    onClick = onDismissInviteBanner,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Search by ID card (Addendum §6.2)
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Add friend by Player ID",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Example IDs: DB-K9M4P2 or DB-A2B3C4",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.ink.copy(alpha = 0.72f),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                StudioTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it.uppercase().take(10)
                                        if (it.isBlank()) onClearSearch()
                                    },
                                    label = "",
                                    placeholder = "DB-XXXXXX",
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Search,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = { onSearchById(searchQuery) },
                                    ),
                                    modifier = Modifier.weight(1f),
                                    testTag = "friends_search_input",
                                )
                                StudioPrimaryButton(
                                    text = "Find",
                                    onClick = { onSearchById(searchQuery) },
                                    modifier = Modifier.width(96.dp),
                                    testTag = "friends_search_button",
                                )
                            }

                            when (searchResult) {
                                PlayerSearchResult.Idle -> Unit
                                PlayerSearchResult.SelfMatch -> {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "That's you!",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.brandHoneyDark,
                                        modifier = Modifier.testTag("friends_search_self"),
                                    )
                                }
                                PlayerSearchResult.NotFound -> {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No player found with that ID.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.brandBerryDark,
                                        modifier = Modifier.testTag("friends_search_not_found"),
                                    )
                                }
                                is PlayerSearchResult.Found -> {
                                    val found = searchResult.player
                                    val alreadyFriend = friends.any { it.playerId == found.playerId }
                                    val alreadyPending = incomingRequests.any { it.playerId == found.playerId } ||
                                        outgoingRequests.any { it.playerId == found.playerId }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(tokens.paperShade)
                                            .border(2.dp, tokens.ink, RoundedCornerShape(16.dp))
                                            .padding(10.dp),
                                    ) {
                                        TeddyAvatar(
                                            config = found.avatarConfig,
                                            backdropColor = found.color.color,
                                            size = 42.dp,
                                            equippedCosmetics = found.equippedCosmetics,
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = found.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                                color = tokens.ink,
                                            )
                                            Text(
                                                text = "${found.playerId} · Lv ${found.level}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.ink.copy(alpha = 0.78f),
                                            )
                                        }
                                        when {
                                            alreadyFriend -> StickerBadge("FRIEND", tokens.brandMint, 0f)
                                            alreadyPending -> StickerBadge("PENDING", tokens.paperCream, 0f)
                                            else -> StudioPrimaryButton(
                                                text = "Add",
                                                onClick = { onSendRequestToFound(found) },
                                                modifier = Modifier.width(88.dp),
                                                testTag = "friends_search_send_request",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Incoming requests (Addendum §6.3)
                    if (incomingRequests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Friend requests (${incomingRequests.size})",
                            style = MaterialTheme.typography.headlineSmall,
                            color = tokens.ink,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        incomingRequests.forEach { req ->
                            StudioCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                ) {
                                    TeddyAvatar(
                                        config = req.avatarConfig,
                                        backdropColor = req.color.color,
                                        size = 42.dp,
                                        equippedCosmetics = req.equippedCosmetics,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = req.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                            color = tokens.ink,
                                        )
                                        Text(
                                            text = "${req.playerId} · Lv ${req.level}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.ink.copy(alpha = 0.75f),
                                        )
                                    }
                                    StudioPrimaryButton(
                                        text = "Accept",
                                        onClick = { onAcceptRequest(req.id) },
                                        modifier = Modifier.width(96.dp),
                                        testTag = "accept_request_${req.playerId}",
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StudioTextButton(
                                        text = "Decline",
                                        onClick = { onDeclineOrCancelRequest(req.id) },
                                        contentColor = tokens.brandBerryDark,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Outgoing requests
                    if (outgoingRequests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sent requests (${outgoingRequests.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        outgoingRequests.forEach { req ->
                            StudioCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    TeddyAvatar(
                                        config = req.avatarConfig,
                                        backdropColor = req.color.color,
                                        size = 36.dp,
                                        equippedCosmetics = req.equippedCosmetics,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = req.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = tokens.ink,
                                        )
                                        Text(
                                            text = "${req.playerId} · Pending",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.ink.copy(alpha = 0.7f),
                                        )
                                    }
                                    StudioTextButton(
                                        text = "Cancel",
                                        onClick = { onDeclineOrCancelRequest(req.id) },
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Friends list sorted Online -> In a match -> Offline (Addendum §6.4)
                    Text(
                        text = "Your friends",
                        style = MaterialTheme.typography.headlineSmall,
                        color = tokens.ink,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (friends.isEmpty()) {
                        StudioCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                            ) {
                                TeddyBiscuit(
                                    pose = TeddyPose.PEEKING,
                                    expression = TeddyExpression.HAPPY,
                                    size = 86.dp,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No friends added yet. Search by ID or tap Add on Ranks!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.ink.copy(alpha = 0.8f),
                                )
                            }
                        }
                    } else {
                        friends.forEach { friend ->
                            FriendRowCard(
                                friend = friend,
                                onInvite = { onInviteFriend(friend) },
                                onRemove = { onRemoveFriend(friend.playerId) },
                                onBlock = { onBlockPlayer(friend.playerId) },
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun FriendRowCard(
    friend: FriendEntry,
    onInvite: () -> Unit,
    onRemove: () -> Unit,
    onBlock: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    var menuExpanded by remember { mutableStateOf(false) }

    val statusDotColor = when (friend.status) {
        FriendStatus.ONLINE -> tokens.brandMint
        FriendStatus.IN_MATCH -> tokens.brandHoney
        FriendStatus.OFFLINE -> tokens.paperLine
    }
    val statusText = when (friend.status) {
        FriendStatus.ONLINE -> "Online"
        FriendStatus.IN_MATCH -> "In a match"
        FriendStatus.OFFLINE -> "Offline · ${friend.lastSeenLabel}"
    }

    StudioCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_row_${friend.playerId}"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            TeddyAvatar(
                config = friend.avatarConfig,
                backdropColor = friend.color.color,
                size = 44.dp,
                equippedCosmetics = friend.equippedCosmetics,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        color = tokens.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LevelTag(level = friend.level)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                            .border(1.5.dp, tokens.ink, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.ink.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            val canInvite = friend.status == FriendStatus.ONLINE
            StudioSecondaryButton(
                text = when (friend.status) {
                    FriendStatus.ONLINE -> "Invite"
                    FriendStatus.IN_MATCH -> "Busy"
                    FriendStatus.OFFLINE -> "Offline"
                },
                enabled = canInvite,
                onClick = onInvite,
                testTag = "friend_invite_${friend.playerId}",
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Options for ${friend.name}",
                        tint = tokens.ink,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = tokens.paperCream,
                ) {
                    DropdownMenuItem(
                        text = { Text("Remove friend", color = tokens.ink) },
                        onClick = {
                            menuExpanded = false
                            onRemove()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Block", color = tokens.brandBerryDark) },
                        onClick = {
                            menuExpanded = false
                            onBlock()
                        },
                    )
                }
            }
        }
    }
}
