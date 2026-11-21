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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BoxSkinId
import com.example.model.CosmeticCategory
import com.example.model.CosmeticItem
import com.example.model.CosmeticRarity
import com.example.model.DiamondPack
import com.example.model.EquippedCosmetics
import com.example.model.LetterFontId
import com.example.model.PlayerColorOption
import com.example.model.PlayerPattern
import com.example.model.PurchaseRecord
import com.example.model.SubscriptionState
import com.example.model.TeddyAvatarConfig
import com.example.model.UserProfile
import com.example.model.WalletState
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.DiamondIcon
import com.example.ui.components.DiamondWalletPill
import com.example.ui.components.FrostedGlassBar
import com.example.ui.components.PaperBackground
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioChip
import com.example.ui.components.StudioDangerButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.StudioTextButton
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.drawBoxSkinDetail
import com.example.ui.components.drawWoodTexture
import com.example.ui.components.letterFontTextStyle
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.CosmeticBuyResult
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(
    wallet: WalletState,
    userProfile: UserProfile?,
    subscription: SubscriptionState,
    renewalDateLabel: String,
    diamondPacks: List<DiamondPack>,
    cosmeticCatalog: List<CosmeticItem>,
    ownedCosmetics: Set<String>,
    purchaseHistory: List<PurchaseRecord>,
    onBuyDiamondPack: (DiamondPack) -> Unit,
    onBuyCosmetic: (CosmeticItem) -> CosmeticBuyResult,
    onEquipCosmetic: (CosmeticItem) -> Unit,
    onSubscribeVoice: () -> Unit,
    onCancelVoice: () -> Unit,
    onOpenVoiceDetails: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(CosmeticCategory.BOX_SKIN) }
    var pendingPack by remember { mutableStateOf<DiamondPack?>(null) }
    var showVoiceConfirmDialog by remember { mutableStateOf(false) }
    var insufficientNeedAmount by remember { mutableStateOf<Int?>(null) }

    val equipped = userProfile?.equippedCosmetics ?: EquippedCosmetics()
    val previewColor = userProfile?.colorOption ?: PlayerColorOption.TOMATO
    val previewPattern = userProfile?.pattern ?: PlayerPattern.STRIPES
    val previewInitials = userProfile?.displayName?.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
        ?: userProfile?.initials?.take(1)?.uppercase()?.ifBlank { "Y" }
        ?: "Y"
    val previewAvatar = userProfile?.avatarConfig ?: TeddyAvatarConfig()

    // Mock Diamond Pack confirmation dialog
    val packToConfirm = pendingPack
    if (packToConfirm != null) {
        AlertDialog(
            onDismissRequest = { pendingPack = null },
            containerColor = tokens.paperCream,
            titleContentColor = tokens.ink,
            textContentColor = tokens.ink,
            title = {
                Text(
                    text = "Confirm ${packToConfirm.name}",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DiamondIcon(size = 20.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+${packToConfirm.diamonds} Diamonds for ₹${packToConfirm.priceInr}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.paywall_prototype_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.ink.copy(alpha = 0.8f),
                    )
                }
            },
            confirmButton = {
                StudioTextButton(
                    text = "Add diamonds",
                    onClick = {
                        onBuyDiamondPack(packToConfirm)
                        pendingPack = null
                    },
                    contentColor = tokens.brandHoneyDark,
                    testTag = "store_confirm_pack_buy",
                )
            },
            dismissButton = {
                StudioTextButton(
                    text = "Cancel",
                    onClick = { pendingPack = null },
                    contentColor = tokens.ink,
                )
            },
        )
    }

    // Mock Voice Pass confirmation dialog
    if (showVoiceConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceConfirmDialog = false },
            containerColor = tokens.paperCream,
            titleContentColor = tokens.ink,
            textContentColor = tokens.ink,
            title = {
                Text(
                    text = "Activate Voice Pass?",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = "₹9 / month for live voice chat in online rooms. ${stringResource(R.string.paywall_prototype_note)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                StudioTextButton(
                    text = "Subscribe",
                    onClick = {
                        onSubscribeVoice()
                        showVoiceConfirmDialog = false
                    },
                    contentColor = tokens.brandHoneyDark,
                    testTag = "store_confirm_voice_buy",
                )
            },
            dismissButton = {
                StudioTextButton(
                    text = "Cancel",
                    onClick = { showVoiceConfirmDialog = false },
                    contentColor = tokens.ink,
                )
            },
        )
    }

    // Insufficient diamonds dialog -> jumps back to top Diamond Packs
    val shortage = insufficientNeedAmount
    if (shortage != null) {
        AlertDialog(
            onDismissRequest = { insufficientNeedAmount = null },
            containerColor = tokens.paperCream,
            titleContentColor = tokens.ink,
            textContentColor = tokens.ink,
            title = {
                Text(
                    text = "Not enough diamonds",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = "You need $shortage more diamonds for this item. Earn them from boxes in matches or pick up a diamond pack.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                StudioTextButton(
                    text = "Get more diamonds",
                    onClick = {
                        insufficientNeedAmount = null
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    contentColor = tokens.diamondTealDark,
                    testTag = "store_get_more_diamonds_button",
                )
            },
            dismissButton = {
                StudioTextButton(
                    text = "Close",
                    onClick = { insufficientNeedAmount = null },
                    contentColor = tokens.ink,
                )
            },
        )
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
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState),
            ) {
                // Frosted Blur-Glass Header with Back button + Wallet Pill
                FrostedGlassBar(showBottomEdge = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (onBack != null) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(tokens.paperCream)
                                        .border(2.5.dp, tokens.ink, CircleShape)
                                        .clickable(onClick = onBack)
                                        .testTag("store_back_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = "Back",
                                        tint = tokens.ink,
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            ChunkyDisplayText(
                                text = "Store",
                                style = MaterialTheme.typography.displaySmall,
                                fillColor = tokens.paperCream,
                                shadowColor = tokens.brandHoney,
                                strokeWidth = 2.5.dp,
                                shadowOffset = 2.dp,
                            )
                        }
                        DiamondWalletPill(
                            diamonds = wallet.diamonds,
                            testTag = "store_wallet_pill",
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    // Section A: Diamond Packs (Addendum §4.1 & §4.2)
                    Text(
                        text = "Diamond packs",
                        style = MaterialTheme.typography.headlineSmall,
                        color = tokens.ink,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlock box skins, teddy outfits, and letter styles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.ink.copy(alpha = 0.78f),
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    diamondPacks.forEach { pack ->
                        DiamondPackCard(
                            pack = pack,
                            onBuyClick = { pendingPack = pack },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Section B: Voice Subscription (Addendum §4.1 & §4.3)
                    Text(
                        text = "Subscriptions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = tokens.ink,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    StudioCard(
                        backgroundColor = if (subscription.isActive) tokens.brandMint.copy(alpha = 0.24f) else tokens.paperCream,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Voice Pass",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.ink,
                                    )
                                    Text(
                                        text = "₹9 / month · Live room voice channels",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.ink.copy(alpha = 0.8f),
                                    )
                                }
                                StickerBadge(
                                    text = if (subscription.isActive) "ACTIVE" else "₹9 / MO",
                                    backgroundColor = if (subscription.isActive) tokens.brandMint else tokens.brandHoney,
                                    rotationDegrees = -2f,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (subscription.isActive && renewalDateLabel.isNotBlank()) {
                                    "Renews $renewalDateLabel · ${stringResource(R.string.paywall_prototype_note)}"
                                } else {
                                    "Talk with friends during matches. Text chat is always free. ${stringResource(R.string.paywall_prototype_note)}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink.copy(alpha = 0.78f),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (subscription.isActive) {
                                    StudioDangerButton(
                                        text = "Cancel pass",
                                        onClick = onCancelVoice,
                                        modifier = Modifier.weight(1f),
                                        testTag = "store_voice_cancel_button",
                                    )
                                } else {
                                    StudioPrimaryButton(
                                        text = "Subscribe · ₹9",
                                        onClick = { showVoiceConfirmDialog = true },
                                        modifier = Modifier.weight(1f),
                                        testTag = "store_voice_subscribe_button",
                                    )
                                }
                                StudioSecondaryButton(
                                    text = "Details",
                                    onClick = onOpenVoiceDetails,
                                    modifier = Modifier.weight(0.8f),
                                    testTag = "store_voice_details_button",
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section C: Cosmetics Shop (Addendum §4.4 & §5)
                    Text(
                        text = "Cosmetics shop",
                        style = MaterialTheme.typography.headlineSmall,
                        color = tokens.ink,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        CosmeticCategory.entries.forEach { category ->
                            StudioChip(
                                label = category.label,
                                selected = category == selectedCategory,
                                onClick = { selectedCategory = category },
                                testTag = "store_category_${category.name.lowercase()}",
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val itemsInCategory = remember(selectedCategory, cosmeticCatalog) {
                        cosmeticCatalog.filter { it.category == selectedCategory }
                    }

                    itemsInCategory.forEach { item ->
                        val isOwned = item.isDefault || item.id in ownedCosmetics
                        val isEquipped = equipped.isItemEquipped(item.id)
                        CosmeticStoreRow(
                            item = item,
                            isOwned = isOwned,
                            isEquipped = isEquipped,
                            previewAvatar = previewAvatar,
                            previewColor = previewColor,
                            previewPattern = previewPattern,
                            previewInitials = previewInitials,
                            previewEquipped = equipped,
                            onAction = {
                                if (isOwned) {
                                    onEquipCosmetic(item)
                                } else {
                                    when (val res = onBuyCosmetic(item)) {
                                        is CosmeticBuyResult.InsufficientDiamonds -> {
                                            insufficientNeedAmount = res.needed
                                        }
                                        CosmeticBuyResult.AlreadyOwned,
                                        is CosmeticBuyResult.Success -> Unit
                                    }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (purchaseHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Recent purchases",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StudioCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                purchaseHistory.take(5).forEachIndexed { idx, rec ->
                                    if (idx > 0) {
                                        HorizontalDivider(
                                            thickness = 1.5.dp,
                                            color = tokens.paperLine,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = rec.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = tokens.ink,
                                            )
                                            Text(
                                                text = "${rec.platform} · ${rec.status}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.ink.copy(alpha = 0.65f),
                                            )
                                        }
                                        Text(
                                            text = rec.priceLabel,
                                            style = LilitaNumberStyle.copy(fontSize = 16.sp),
                                            color = tokens.ink,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp).navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun DiamondPackCard(
    pack: DiamondPack,
    onBuyClick: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    StudioCard(
        backgroundColor = if (pack.badge != null) tokens.warmHighlight else tokens.paperCream,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("store_pack_${pack.id}"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.diamondTealLight.copy(alpha = 0.35f))
                    .border(2.dp, tokens.ink, RoundedCornerShape(14.dp)),
            ) {
                DiamondIcon(size = 28.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${pack.diamonds} Diamonds",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink,
                    )
                    if (pack.badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(tokens.brandHoney)
                                .border(1.5.dp, tokens.ink, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = pack.badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.ink,
                            )
                        }
                    }
                }
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.ink.copy(alpha = 0.75f),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StudioPrimaryButton(
                text = "₹${pack.priceInr}",
                onClick = onBuyClick,
                modifier = Modifier.width(98.dp),
                testTag = "store_buy_${pack.id}",
            )
        }
    }
}

@Composable
private fun CosmeticStoreRow(
    item: CosmeticItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    previewAvatar: TeddyAvatarConfig,
    previewColor: PlayerColorOption,
    previewPattern: PlayerPattern,
    previewInitials: String,
    previewEquipped: EquippedCosmetics,
    onAction: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    StudioCard(
        backgroundColor = if (isEquipped) tokens.brandHoney.copy(alpha = 0.22f) else tokens.paperCream,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("store_cosmetic_${item.id}"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            CosmeticPreviewBadge(
                item = item,
                previewAvatar = previewAvatar,
                previewColor = previewColor,
                previewPattern = previewPattern,
                previewInitials = previewInitials,
                previewEquipped = previewEquipped,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = tokens.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (item.rarity != CosmeticRarity.DEFAULT) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.rarity.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.ink.copy(alpha = 0.65f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.ink.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            when {
                isEquipped -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(tokens.brandMint)
                            .border(2.dp, tokens.ink, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Equipped",
                                tint = tokens.ink,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Equipped",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.ink,
                            )
                        }
                    }
                }
                isOwned -> {
                    StudioSecondaryButton(
                        text = "Equip",
                        onClick = onAction,
                        testTag = "equip_${item.id}",
                    )
                }
                else -> {
                    val pillShape = RoundedCornerShape(999.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(44.dp)
                            .clip(pillShape)
                            .background(tokens.brandHoney)
                            .border(2.5.dp, tokens.ink, pillShape)
                            .clickable(onClick = onAction)
                            .padding(horizontal = 12.dp)
                            .testTag("buy_${item.id}"),
                    ) {
                        DiamondIcon(size = 16.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${item.priceDiamonds}",
                            style = LilitaNumberStyle.copy(fontSize = 16.sp),
                            color = tokens.ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CosmeticPreviewBadge(
    item: CosmeticItem,
    previewAvatar: TeddyAvatarConfig,
    previewColor: PlayerColorOption,
    previewPattern: PlayerPattern,
    previewInitials: String,
    previewEquipped: EquippedCosmetics,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    when (item.category) {
        CosmeticCategory.BOX_SKIN -> {
            val skin = item.boxSkinId ?: BoxSkinId.CLASSIC_PAPER
            Canvas(
                modifier = modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                drawBoxSkinDetail(
                    boxSkin = skin,
                    topLeft = Offset.Zero,
                    boxSize = size,
                    baseColor = previewColor.color,
                    pattern = previewPattern,
                    ink = tokens.ink,
                    highlight = tokens.warmHighlight,
                    honey = tokens.brandHoney,
                    woodDark = tokens.woodDark,
                )
            }
        }
        CosmeticCategory.AVATAR_CLOTHES -> {
            val cos = previewEquipped.copy(
                avatarClothing = item.clothingId ?: previewEquipped.avatarClothing,
            )
            TeddyAvatar(
                config = previewAvatar,
                backdropColor = previewColor.color,
                size = 52.dp,
                equippedCosmetics = cos,
                modifier = modifier,
            )
        }
        CosmeticCategory.AVATAR_SKIN -> {
            val cos = previewEquipped.copy(
                avatarSkin = item.avatarSkinId ?: previewEquipped.avatarSkin,
            )
            TeddyAvatar(
                config = previewAvatar,
                backdropColor = previewColor.color,
                size = 52.dp,
                equippedCosmetics = cos,
                modifier = modifier,
            )
        }
        CosmeticCategory.LETTER_FONT -> {
            val font = item.letterFontId ?: LetterFontId.BOLD_BLOCK
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(previewColor.color)
                    .border(2.5.dp, tokens.ink, RoundedCornerShape(12.dp)),
            ) {
                Text(
                    text = previewInitials.take(1).uppercase(),
                    style = letterFontTextStyle(font, 24.sp),
                    color = tokens.warmHighlight,
                )
            }
        }
    }
}
