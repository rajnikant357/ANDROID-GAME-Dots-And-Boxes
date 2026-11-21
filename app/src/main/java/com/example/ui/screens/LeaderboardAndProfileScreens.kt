package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LeaderboardScope
import com.example.model.CosmeticCategory
import com.example.model.CosmeticItem
import com.example.model.FriendEntry
import com.example.model.FriendRequest
import com.example.model.LeaderboardEntry
import com.example.model.SubscriptionState
import com.example.model.UserProfile
import com.example.model.WalletState
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.DiamondWalletPill
import com.example.ui.components.ExpProgressBar
import com.example.ui.components.FrostedGlassBar
import com.example.ui.components.GenderBadge
import com.example.ui.components.HomeModeIconKind
import com.example.ui.components.LevelTag
import com.example.ui.components.ModeStickerIcon
import com.example.ui.components.PaperBackground
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioChip
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.TeddyBiscuit
import com.example.ui.components.TeddyExpression
import com.example.ui.components.TeddyPose
import com.example.ui.components.drawWoodTexture
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.StudioTheme

@Composable
fun LeaderboardScreen(
    scope: LeaderboardScope,
    entries: List<LeaderboardEntry>,
    friends: List<FriendEntry> = emptyList(),
    incomingRequests: List<FriendRequest> = emptyList(),
    outgoingRequests: List<FriendRequest> = emptyList(),
    blockedIds: Set<String> = emptySet(),
    onSelectScope: (LeaderboardScope) -> Unit,
    onAddFriend: (LeaderboardEntry) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val listState = rememberLazyListState()
    val userIndex = remember(entries) { entries.indexOfFirst { it.isCurrentUser } }
    val topThree = remember(entries) { entries.take(3) }
    val friendIds = remember(friends) { friends.map { it.playerId }.toSet() }
    val pendingIds = remember(incomingRequests, outgoingRequests) {
        (incomingRequests.map { it.playerId } + outgoingRequests.map { it.playerId }).toSet()
    }

    val isUserRowVisible by remember(userIndex) {
        derivedStateOf {
            if (userIndex < 0) true
            else listState.layoutInfo.visibleItemsInfo.any { it.index == userIndex + 1 }
        }
    }

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
                    .widthIn(max = 600.dp),
            ) {
                // Frosted Blur-Glass header & Podium (DRD §9 & §16 Step 7)
                FrostedGlassBar(showBottomEdge = true) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ChunkyDisplayText(
                                text = "Leaderboard",
                                style = MaterialTheme.typography.displaySmall,
                                fillColor = tokens.paperCream,
                                shadowColor = tokens.brandHoney,
                                strokeWidth = 2.5.dp,
                                shadowOffset = 2.dp,
                            )
                            ModeStickerIcon(kind = HomeModeIconKind.LEADERBOARD_TROPHY, size = 38.dp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StudioChip(
                                label = "This week",
                                selected = scope == LeaderboardScope.WEEKLY,
                                onClick = { onSelectScope(LeaderboardScope.WEEKLY) },
                                modifier = Modifier.weight(1f),
                                testTag = "leaderboard_tab_weekly",
                            )
                            StudioChip(
                                label = "All time",
                                selected = scope == LeaderboardScope.ALL_TIME,
                                onClick = { onSelectScope(LeaderboardScope.ALL_TIME) },
                                modifier = Modifier.weight(1f),
                                testTag = "leaderboard_tab_all_time",
                            )
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    if (topThree.size == 3) {
                        item(key = "podium_header") {
                            LeaderboardWoodPodium(
                                first = topThree[0],
                                second = topThree[1],
                                third = topThree[2],
                                scope = scope,
                            )
                            HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                        }
                    }

                    itemsIndexed(entries, key = { _, item -> item.id }) { index, entry ->
                        LeaderboardRow(
                            rank = index + 1,
                            entry = entry,
                            scope = scope,
                            isFriend = entry.playerId in friendIds,
                            isPending = entry.playerId in pendingIds,
                            isBlocked = entry.playerId in blockedIds,
                            onAddFriend = { onAddFriend(entry) },
                        )
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                    }
                }

                // Pinned current user row when outside visible range
                if (userIndex >= 0 && !isUserRowVisible) {
                    val userEntry = entries[userIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tokens.brandHoney)
                            .testTag("leaderboard_pinned_user_row"),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(tokens.ink),
                        )
                        LeaderboardRow(
                            rank = userIndex + 1,
                            entry = userEntry,
                            scope = scope,
                            isFriend = false,
                            isPending = false,
                            isBlocked = false,
                            onAddFriend = {},
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardWoodPodium(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry,
    scope: LeaderboardScope,
) {
    val tokens = StudioTheme.tokens
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.paperShade)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        PodiumColumn(
            rank = 2,
            entry = second,
            scope = scope,
            stepHeight = 46.dp,
            badgeColor = tokens.silverSticker,
            modifier = Modifier.weight(1f),
        )
        PodiumColumn(
            rank = 1,
            entry = first,
            scope = scope,
            stepHeight = 68.dp,
            badgeColor = tokens.goldSticker,
            modifier = Modifier.weight(1.1f),
        )
        PodiumColumn(
            rank = 3,
            entry = third,
            scope = scope,
            stepHeight = 36.dp,
            badgeColor = tokens.bronzeSticker,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PodiumColumn(
    rank: Int,
    entry: LeaderboardEntry,
    scope: LeaderboardScope,
    stepHeight: Dp,
    badgeColor: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val expValue = if (scope == LeaderboardScope.WEEKLY) entry.weeklyExp else entry.totalExp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        StickerBadge(
            text = "#$rank",
            backgroundColor = badgeColor,
            rotationDegrees = if (rank == 1) -2f else 2f,
        )
        Spacer(modifier = Modifier.height(6.dp))
        TeddyAvatar(
            config = entry.avatarConfig,
            backdropColor = entry.color.color,
            size = if (rank == 1) 54.dp else 44.dp,
            equippedCosmetics = entry.equippedCosmetics,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.name.split(" ").first(),
            style = MaterialTheme.typography.labelLarge,
            color = tokens.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(stepHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(tokens.woodMid)
                .drawBehind {
                    drawWoodTexture(tokens.woodLight, tokens.woodMid, tokens.woodDark)
                }
                .border(2.5.dp, tokens.ink, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        ) {
            Text(
                text = "$expValue",
                style = LilitaNumberStyle.copy(fontSize = if (rank == 1) 22.sp else 18.sp),
                color = tokens.paperCream,
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
    scope: LeaderboardScope,
    isFriend: Boolean,
    isPending: Boolean,
    isBlocked: Boolean,
    onAddFriend: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    val expValue = if (scope == LeaderboardScope.WEEKLY) entry.weeklyExp else entry.totalExp

    // DRD §10.9: Top 3 gold/silver/bronze stickers; user row has honey highlight band + 3px ink outline
    val rankBadgeColor = when (rank) {
        1 -> tokens.goldSticker
        2 -> tokens.silverSticker
        3 -> tokens.bronzeSticker
        else -> null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (entry.isCurrentUser) tokens.brandHoney else Color.Transparent)
            .then(if (entry.isCurrentUser) Modifier.border(3.dp, tokens.ink) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .then(if (entry.isCurrentUser) Modifier.testTag("leaderboard_current_user_row") else Modifier),
    ) {
        if (rankBadgeColor != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(rankBadgeColor)
                    .border(2.dp, tokens.ink, CircleShape),
            ) {
                Text(
                    text = "$rank",
                    style = LilitaNumberStyle.copy(fontSize = 16.sp),
                    color = tokens.ink,
                )
            }
        } else {
            Text(
                text = "$rank",
                style = LilitaNumberStyle.copy(fontSize = 20.sp),
                color = tokens.ink,
                modifier = Modifier.width(32.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        TeddyAvatar(
            config = entry.avatarConfig,
            backdropColor = entry.color.color,
            size = 40.dp,
            isCircle = true,
            equippedCosmetics = entry.equippedCosmetics,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (entry.isCurrentUser) "${entry.name} (You)" else entry.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = tokens.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(6.dp))
                GenderBadge(gender = entry.gender)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelTag(level = entry.level)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${entry.matchesWon} wins",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.ink.copy(alpha = 0.78f),
                )
            }
        }
        if (!entry.isCurrentUser && !isBlocked) {
            Spacer(modifier = Modifier.width(6.dp))
            when {
                isFriend -> Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(tokens.brandMint.copy(alpha = 0.35f))
                        .border(1.5.dp, tokens.ink, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Friend",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.ink,
                    )
                }
                isPending -> Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(tokens.paperShade)
                        .border(1.5.dp, tokens.ink, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.ink,
                    )
                }
                else -> Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(tokens.brandHoney)
                        .border(2.dp, tokens.ink, RoundedCornerShape(999.dp))
                        .clickable(onClick = onAddFriend)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("leaderboard_add_${entry.playerId}"),
                ) {
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.ink,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$expValue",
                style = LilitaNumberStyle.copy(fontSize = 22.sp),
                color = tokens.ink,
            )
            Text(
                text = "EXP",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.ink.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    wallet: WalletState,
    subscription: SubscriptionState,
    renewalDateLabel: String,
    cosmeticCatalog: List<CosmeticItem>,
    ownedCosmetics: Set<String>,
    reducedMotion: Boolean,
    onCreateProfile: () -> Unit,
    onEditProfile: () -> Unit,
    onEquipCosmetic: (CosmeticItem) -> Unit,
    onOpenStore: () -> Unit,
    onCopyPlayerId: (String) -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var wardrobeCategory by remember { mutableStateOf(CosmeticCategory.BOX_SKIN) }

    PaperBackground(
        showCreaseLines = false,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (userProfile == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 480.dp)
                        .padding(24.dp),
                ) {
                    TeddyBiscuit(
                        pose = TeddyPose.PENCIL,
                        expression = TeddyExpression.HAPPY,
                        size = 120.dp,
                        reducedMotion = reducedMotion,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No profile yet",
                        style = MaterialTheme.typography.displaySmall,
                        color = tokens.ink,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create a profile to customize your teddy, earn EXP, and join the global leaderboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.ink.copy(alpha = 0.8f),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    StudioPrimaryButton(
                        text = "Create profile",
                        onClick = onCreateProfile,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "profile_empty_create_button",
                    )
                }
                return@PaperBackground
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Frosted Blur-Glass Header (DRD §9)
                FrostedGlassBar(showBottomEdge = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        ChunkyDisplayText(
                            text = "Profile",
                            style = MaterialTheme.typography.displaySmall,
                            fillColor = tokens.paperCream,
                            shadowColor = tokens.brandHoney,
                            strokeWidth = 2.5.dp,
                            shadowOffset = 2.dp,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DiamondWalletPill(
                                diamonds = wallet.diamonds,
                                onClick = onOpenStore,
                                testTag = "profile_wallet_pill",
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(tokens.paperCream)
                                    .border(2.5.dp, tokens.ink, CircleShape)
                                    .clickable(onClick = onEditProfile)
                                    .testTag("profile_edit_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit profile",
                                    tint = tokens.ink,
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(tokens.paperCream)
                                    .border(2.5.dp, tokens.ink, CircleShape)
                                    .clickable(onClick = onOpenSettings)
                                    .testTag("profile_settings_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = tokens.ink,
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Large TeddyAvatar with overlapping Level sticker (DRD §9)
                                Box(modifier = Modifier.size(80.dp)) {
                                    TeddyAvatar(
                                        config = userProfile.avatarConfig,
                                        backdropColor = userProfile.colorOption.color,
                                        size = 72.dp,
                                        equippedCosmetics = userProfile.equippedCosmetics,
                                    )
                                    StickerBadge(
                                        text = "LV ${userProfile.level}",
                                        backgroundColor = tokens.brandHoney,
                                        rotationDegrees = -6f,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 6.dp, y = 4.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userProfile.displayName,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = tokens.ink,
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        GenderBadge(gender = userProfile.gender)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${userProfile.initials} · ${userProfile.colorOption.label} · ${userProfile.pattern.label}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.ink.copy(alpha = 0.78f),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${userProfile.totalExp} total EXP",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = tokens.ink,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Unique Player ID row with Copy & Share (Addendum §6.1)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.paperShade)
                                    .border(2.dp, tokens.ink, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Column {
                                    Text(
                                        text = "Player ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink.copy(alpha = 0.7f),
                                    )
                                    Text(
                                        text = userProfile.playerId,
                                        style = LilitaNumberStyle.copy(fontSize = 18.sp),
                                        color = tokens.ink,
                                        modifier = Modifier.testTag("profile_player_id_text"),
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(tokens.paperCream)
                                            .border(2.dp, tokens.ink, CircleShape)
                                            .clickable {
                                                clipboard.setText(AnnotatedString(userProfile.playerId))
                                                onCopyPlayerId(userProfile.playerId)
                                            }
                                            .testTag("profile_copy_id_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy Player ID",
                                            tint = tokens.ink,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(tokens.paperCream)
                                            .border(2.dp, tokens.ink, CircleShape)
                                            .clickable {
                                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Add me on Dots & Boxes! My Player ID is ${userProfile.playerId}",
                                                    )
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Share Player ID"))
                                            }
                                            .testTag("profile_share_id_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "Share Player ID",
                                            tint = tokens.ink,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            ExpProgressBar(
                                totalExp = userProfile.totalExp,
                                reducedMotion = reducedMotion,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wardrobe Section (Addendum §5.2 & §5.3)
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Wardrobe",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = tokens.ink,
                                )
                                StudioSecondaryButton(
                                    text = "Shop",
                                    onClick = onOpenStore,
                                    testTag = "profile_open_store_button",
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                CosmeticCategory.entries.forEach { category ->
                                    StudioChip(
                                        label = category.label,
                                        selected = wardrobeCategory == category,
                                        onClick = { wardrobeCategory = category },
                                        testTag = "wardrobe_tab_${category.name.lowercase()}",
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val ownedInCategory = remember(wardrobeCategory, cosmeticCatalog, ownedCosmetics) {
                                cosmeticCatalog.filter {
                                    it.category == wardrobeCategory && (it.isDefault || it.id in ownedCosmetics)
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                ownedInCategory.forEach { item ->
                                    val isEquipped = userProfile.equippedCosmetics.isItemEquipped(item.id)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(104.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isEquipped) tokens.brandHoney.copy(alpha = 0.28f) else tokens.paperShade)
                                            .border(if (isEquipped) 3.dp else 2.dp, tokens.ink, RoundedCornerShape(16.dp))
                                            .clickable { onEquipCosmetic(item) }
                                            .padding(10.dp)
                                            .testTag("wardrobe_item_${item.id}"),
                                    ) {
                                        CosmeticPreviewBadge(
                                            item = item,
                                            previewAvatar = userProfile.avatarConfig,
                                            previewColor = userProfile.colorOption,
                                            previewPattern = userProfile.pattern,
                                            previewInitials = userProfile.initials,
                                            previewEquipped = userProfile.equippedCosmetics,
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = tokens.ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (isEquipped) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = "Equipped",
                                                    tint = tokens.feltDark,
                                                    modifier = Modifier.size(12.dp),
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "Equipped",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = tokens.feltDark,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        StatCard(
                            label = "Online matches",
                            value = "${userProfile.onlineMatchesPlayed}",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Matches won",
                            value = "${userProfile.onlineMatchesWon}",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        StatCard(
                            label = "Boxes claimed",
                            value = "${userProfile.totalBoxesClaimed}",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Best match",
                            value = "${userProfile.bestMatchScore}",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    StudioCard(
                        onClick = onOpenPaywall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_subscription_row"),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Voice subscription",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (subscription.isActive && renewalDateLabel.isNotBlank()) {
                                        "Active · renews $renewalDateLabel"
                                    } else {
                                        "Not subscribed"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.ink.copy(alpha = 0.82f),
                                    modifier = Modifier.testTag("profile_subscription_status"),
                                )
                            }
                            StudioSecondaryButton(
                                text = if (subscription.isActive) "Manage" else "₹9 / mo",
                                onClick = onOpenPaywall,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StudioCard(
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Text(
                                text = "Preferences & About",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = "Open settings",
                                tint = tokens.ink,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    StudioCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = LilitaNumberStyle.copy(fontSize = 32.sp),
                color = tokens.ink,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.ink.copy(alpha = 0.78f),
            )
        }
    }
}
