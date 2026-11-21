package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.FriendEntry
import com.example.model.FriendStatus
import com.example.model.MockOnlinePlayer
import com.example.model.MockRoom
import com.example.model.SubscriptionState
import com.example.model.TeddyAccessory
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.FeltBackground
import com.example.ui.components.GenderBadge
import com.example.ui.components.LevelTag
import com.example.ui.components.PaperBackground
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioDangerButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.TeddyBiscuit
import com.example.ui.components.TeddyExpression
import com.example.ui.components.TeddyPose
import com.example.ui.components.drawWoodTexture
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.InviteBannerState

@Composable
fun LobbyScreen(
    openRooms: List<MockRoom>,
    onlinePlayers: List<MockOnlinePlayer>,
    friends: List<FriendEntry>,
    isSearchingQuickMatch: Boolean,
    inviteBanner: InviteBannerState?,
    onBack: () -> Unit,
    onStartQuickMatch: () -> Unit,
    onCancelQuickMatch: () -> Unit,
    onInviteFriend: (FriendEntry) -> Unit,
    onOpenRooms: () -> Unit,
    onJoinRoom: (MockRoom) -> Unit,
) {
    val tokens = StudioTheme.tokens
    val onlineFriends = friends.filter { it.status == FriendStatus.ONLINE }

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
                    // Wooden signboard header (DRD §9 & Addendum §8.2)
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
                                .testTag("lobby_back_button"),
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
                                    text = "Lobby",
                                    style = MaterialTheme.typography.displaySmall,
                                    fillColor = tokens.paperCream,
                                    shadowColor = tokens.brandHoney,
                                    strokeWidth = 2.dp,
                                    shadowOffset = 2.dp,
                                )
                                Text(
                                    text = "+5 EXP per match · Simulated rooms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.paperCream,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    if (isSearchingQuickMatch) {
                        StudioCard(
                            backgroundColor = tokens.paperCream,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lobby_searching_card"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                            ) {
                                TeddyBiscuit(
                                    pose = TeddyPose.PENCIL,
                                    expression = TeddyExpression.THINKING,
                                    size = 64.dp,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Searching for opponent...",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.ink,
                                    )
                                    Text(
                                        text = "Finding a live tabletop match",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.ink.copy(alpha = 0.78f),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StudioDangerButton(
                                    text = "Cancel",
                                    onClick = onCancelQuickMatch,
                                    modifier = Modifier.width(104.dp),
                                    testTag = "lobby_cancel_search_button",
                                )
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            StudioPrimaryButton(
                                text = "Quick match",
                                onClick = onStartQuickMatch,
                                modifier = Modifier.weight(1f),
                                testTag = "lobby_quick_match_button",
                            )
                            StudioSecondaryButton(
                                text = "Rooms",
                                onClick = onOpenRooms,
                                modifier = Modifier.weight(0.85f),
                                testTag = "lobby_open_rooms_button",
                            )
                        }
                    }
                }

                if (inviteBanner != null) {
                    item {
                        StudioCard(
                            backgroundColor = tokens.brandHoney.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = inviteBanner.statusText,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                color = tokens.ink,
                                modifier = Modifier.padding(14.dp),
                            )
                        }
                    }
                }

                // Friends online horizontal strip (Addendum §8.2)
                if (onlineFriends.isNotEmpty()) {
                    item {
                        StickerBadge(
                            text = "FRIENDS ONLINE (${onlineFriends.size})",
                            backgroundColor = tokens.brandMint,
                            rotationDegrees = -1f,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        ) {
                            onlineFriends.forEach { friend ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(86.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(tokens.paperCream)
                                        .border(2.5.dp, tokens.ink, RoundedCornerShape(16.dp))
                                        .clickable { onInviteFriend(friend) }
                                        .padding(8.dp)
                                        .testTag("lobby_invite_${friend.playerId}"),
                                ) {
                                    TeddyAvatar(
                                        config = friend.avatarConfig,
                                        backdropColor = friend.color.color,
                                        size = 44.dp,
                                        equippedCosmetics = friend.equippedCosmetics,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = friend.name.split(" ").first(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "Tap invite",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = tokens.feltDark,
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        StickerBadge(
                            text = "OPEN ROOMS (${openRooms.size})",
                            backgroundColor = tokens.paperCream,
                            rotationDegrees = -1f,
                        )
                        TeddyBiscuit(
                            pose = TeddyPose.PEEKING,
                            expression = if (openRooms.isEmpty()) TeddyExpression.SLEEPY else TeddyExpression.HAPPY,
                            size = 66.dp,
                        )
                    }
                }

                // Ticket-style room cards (DRD §10.3)
                items(openRooms, key = { it.id }) { room ->
                    TicketRoomCard(
                        room = room,
                        onJoin = { onJoinRoom(room) },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    StickerBadge(
                        text = "ONLINE PLAYERS (${onlinePlayers.size})",
                        backgroundColor = tokens.paperCream,
                        rotationDegrees = 1f,
                    )
                }

                items(onlinePlayers, key = { it.id }) { player ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.paperCream)
                            .border(2.5.dp, tokens.ink, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        TeddyAvatar(
                            config = player.avatarConfig,
                            backdropColor = player.color.color,
                            size = 34.dp,
                            isCircle = true,
                            equippedCosmetics = player.equippedCosmetics,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            color = tokens.ink,
                            modifier = Modifier.weight(1f),
                        )
                        GenderBadge(gender = player.gender)
                        Spacer(modifier = Modifier.width(6.dp))
                        LevelTag(level = player.level)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(tokens.brandMint)
                                .border(1.5.dp, tokens.ink, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = player.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.ink,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun TicketRoomCard(
    room: MockRoom,
    onJoin: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp, bottom = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(cardShape)
                .background(tokens.ink),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(tokens.paperCream)
                .border(3.dp, tokens.ink, cardShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(tokens.brandMint)
                                .border(1.5.dp, tokens.ink, CircleShape),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = room.statusLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink.copy(alpha = 0.78f),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Ticket dashed tear divider
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
                    style = LilitaNumberStyle.copy(fontSize = 24.sp),
                    color = tokens.ink,
                )
                StudioPrimaryButton(
                    text = "Join",
                    onClick = onJoin,
                    modifier = Modifier.width(116.dp),
                    testTag = "lobby_join_${room.id}",
                )
            }
        }
    }
}

@Composable
fun PaywallScreen(
    subscription: SubscriptionState,
    renewalDateLabel: String,
    onSubscribe: () -> Unit,
    onCancelSubscription: () -> Unit,
    onBack: () -> Unit,
) {
    val tokens = StudioTheme.tokens

    PaperBackground(
        showCreaseLines = true,
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
                    .widthIn(max = 560.dp)
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
                            .testTag("paywall_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = tokens.ink,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Voice pass",
                        style = MaterialTheme.typography.displaySmall,
                        color = tokens.ink,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ribbon Banner + Biscuit with Headphones + ₹9 / month in display.l (DRD §9 & §16 Step 8)
                StudioCard(
                    backgroundColor = if (subscription.isActive) tokens.brandMint.copy(alpha = 0.28f) else tokens.paperCream,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp),
                    ) {
                        StickerBadge(
                            text = if (subscription.isActive) "VOICE ACTIVE" else "TABLETOP VOICE",
                            backgroundColor = if (subscription.isActive) tokens.brandMint else tokens.brandHoney,
                            rotationDegrees = -2f,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TeddyBiscuit(
                            pose = TeddyPose.WAVING,
                            expression = TeddyExpression.HAPPY,
                            size = 112.dp,
                            overrideAccessory = TeddyAccessory.HEADPHONES,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ChunkyDisplayText(
                            text = "₹9 / month",
                            style = MaterialTheme.typography.displayMedium,
                            fillColor = tokens.brandHoney,
                            shadowColor = tokens.ink,
                            modifier = Modifier.testTag("paywall_price_text"),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (subscription.isActive && renewalDateLabel.isNotBlank()) {
                                "Active · renews $renewalDateLabel"
                            } else {
                                "Talk live with friends during online matches."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What voice includes",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PaywallBenefitRow(included = true, text = "Voice channel access in every online room")
                        Spacer(modifier = Modifier.height(8.dp))
                        PaywallBenefitRow(included = true, text = "Round participant stickers with speaking rings")
                        Spacer(modifier = Modifier.height(8.dp))
                        PaywallBenefitRow(included = true, text = "Instant mute and leave controls")

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "What it does not include",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PaywallBenefitRow(included = false, text = "Text chat is always free — no subscription needed")
                        Spacer(modifier = Modifier.height(8.dp))
                        PaywallBenefitRow(included = false, text = "No bonus EXP, hints, or gameplay advantage")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                StudioCard(
                    backgroundColor = tokens.paperShade,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.paywall_prototype_note),
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                            modifier = Modifier.testTag("paywall_prototype_note"),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Subscriptions auto-renew monthly at ₹9 and can be cancelled anytime. In this prototype, subscribing toggles a local state with a 30-day renewal date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink.copy(alpha = 0.82f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (subscription.isActive) {
                    StudioDangerButton(
                        text = "Cancel subscription",
                        onClick = onCancelSubscription,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "paywall_cancel_button",
                    )
                } else {
                    StudioPrimaryButton(
                        text = "Subscribe · ₹9 / month",
                        onClick = onSubscribe,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "paywall_subscribe_button",
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PaywallBenefitRow(
    included: Boolean,
    text: String,
) {
    val tokens = StudioTheme.tokens
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (included) tokens.brandMint else tokens.paperShade)
                .border(2.dp, tokens.ink, CircleShape),
        ) {
            Icon(
                imageVector = if (included) Icons.Outlined.Check else Icons.Outlined.Close,
                contentDescription = null,
                tint = tokens.ink,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.ink,
        )
    }
}
