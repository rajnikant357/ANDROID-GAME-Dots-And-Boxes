package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.DotsAndBoxesEngine
import com.example.engine.MatchState
import com.example.engine.PlayerSlot
import com.example.model.ChatMessage
import com.example.model.PlayerIdentity
import com.example.model.SubscriptionState
import com.example.model.VoiceParticipant
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.DiamondIcon
import com.example.ui.components.ExpProgressBar
import com.example.ui.components.FeltBackground
import com.example.ui.components.PaperBackground
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
import com.example.ui.components.drawWoodTexture
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.LilitaOneFontFamily
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.MatchCompletionSummary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LevelTransitionPanel(
    matchState: MatchState,
    player1: PlayerIdentity,
    player2: PlayerIdentity,
    reducedMotion: Boolean,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val lastSummary = matchState.completedLevels.lastOrNull() ?: return
    val nextLevelNumber = matchState.currentLevelIndex + 2
    val nextDots = DotsAndBoxesEngine.LEVEL_DOT_COUNTS.getOrNull(matchState.currentLevelIndex + 1) ?: 8
    val nextBoxes = (nextDots - 1) * (nextDots - 1)

    val winnerLabel = when (lastSummary.winner) {
        PlayerSlot.PLAYER_1 -> "Congrats ${player1.name}! Won Round ${lastSummary.levelNumber}"
        PlayerSlot.PLAYER_2 -> "Congrats ${player2.name}! Won Round ${lastSummary.levelNumber}"
        null -> "Congrats! Round ${lastSummary.levelNumber} tied"
    }

    var secondsRemaining by remember(matchState.currentLevelIndex) { mutableIntStateOf(3) }
    LaunchedEffect(matchState.currentLevelIndex) {
        for (sec in 3 downTo 1) {
            secondsRemaining = sec
            delay(1000L)
        }
        onNextLevel()
    }

    PaperBackground(
        showCreaseLines = true,
        modifier = modifier
            .fillMaxSize()
            .testTag("level_transition_panel"),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            StudioCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(18.dp),
                ) {
                    // Confetti-free star ring + Biscuit celebrating (DRD §9 & §16 Step 7)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(104.dp),
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension * 0.44f
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            for (i in 0 until 8) {
                                val ang = (i * 45.0) * Math.PI / 180.0
                                val sx = cx + (r * cos(ang)).toFloat()
                                val sy = cy + (r * sin(ang)).toFloat()
                                drawCircle(
                                    color = if (i % 2 == 0) tokens.brandHoney else tokens.brandMint,
                                    radius = 5.dp.toPx(),
                                    center = Offset(sx, sy),
                                )
                                drawCircle(
                                    color = tokens.ink,
                                    radius = 5.dp.toPx(),
                                    center = Offset(sx, sy),
                                    style = Stroke(1.5.dp.toPx()),
                                )
                            }
                        }
                        TeddyBiscuit(
                            pose = TeddyPose.WAVING,
                            expression = TeddyExpression.CELEBRATING,
                            size = 82.dp,
                            reducedMotion = reducedMotion,
                        )
                    }

                    StickerBadge(
                        text = "ROUND ${lastSummary.levelNumber} COMPLETE",
                        backgroundColor = tokens.brandHoney,
                        rotationDegrees = -2f,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = winnerLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = tokens.ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TeddyAvatar(
                                config = player1.avatarConfig,
                                backdropColor = player1.color.color,
                                size = 48.dp,
                                equippedCosmetics = player1.equippedCosmetics,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${lastSummary.p1Boxes}",
                                style = LilitaNumberStyle.copy(fontSize = 32.sp),
                                color = tokens.ink,
                            )
                            Text(
                                text = player1.initials,
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.ink.copy(alpha = 0.78f),
                            )
                            if (lastSummary.p1Diamonds > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    DiamondIcon(size = 14.dp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+${lastSummary.p1Diamonds}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.diamondTealDark,
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TeddyAvatar(
                                config = player2.avatarConfig,
                                backdropColor = player2.color.color,
                                size = 48.dp,
                                equippedCosmetics = player2.equippedCosmetics,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${lastSummary.p2Boxes}",
                                style = LilitaNumberStyle.copy(fontSize = 32.sp),
                                color = tokens.ink,
                            )
                            Text(
                                text = player2.initials,
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.ink.copy(alpha = 0.78f),
                            )
                            if (lastSummary.p2Diamonds > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    DiamondIcon(size = 14.dp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+${lastSummary.p2Diamonds}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.diamondTealDark,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Match total: ${matchState.totalP1Score} – ${matchState.totalP2Score}",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (nextLevelNumber >= 3) {
                            "Up next: Final Round ($nextDots×$nextDots dots · $nextBoxes boxes)"
                        } else {
                            "Up next: Round $nextLevelNumber ($nextDots×$nextDots dots · $nextBoxes boxes)"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.ink.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(999.dp))
                            .background(tokens.brandHoney)
                            .border(2.5.dp, tokens.ink, RoundedCornerShape(999.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("level_transition_countdown"),
                    ) {
                        Text(
                            text = "Starting next round in ${secondsRemaining}s...",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = tokens.ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatchResultsPanel(
    matchState: MatchState,
    player1: PlayerIdentity,
    player2: PlayerIdentity,
    completionSummary: MatchCompletionSummary?,
    reducedMotion: Boolean,
    isOnline: Boolean = false,
    isOpponentFriend: Boolean = false,
    onAddOpponentFriend: (() -> Unit)? = null,
    onWatchAdToDoubleDiamonds: () -> Unit = {},
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val winnerSlot = matchState.matchWinner
    val winner = if (winnerSlot == PlayerSlot.PLAYER_2) player2 else player1
    val runnerUp = if (winnerSlot == PlayerSlot.PLAYER_2) player1 else player2
    val winnerScore = if (winnerSlot == PlayerSlot.PLAYER_2) matchState.totalP2Score else matchState.totalP1Score
    val runnerScore = if (winnerSlot == PlayerSlot.PLAYER_2) matchState.totalP1Score else matchState.totalP2Score

    val avatarScale = remember { Animatable(if (reducedMotion) 1f else 0.92f) }
    var adOfferDismissed by remember(matchState) { mutableStateOf(false) }
    val adRewardClaimed = completionSummary?.adDoubledDiamonds == true
    var displayedExp by remember(completionSummary) {
        mutableIntStateOf(completionSummary?.previousExp ?: 0)
    }
    var targetWallet by remember(completionSummary) {
        mutableIntStateOf(completionSummary?.walletBeforeDiamonds ?: 0)
    }
    val animatedWallet by animateIntAsState(
        targetValue = targetWallet,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 600),
        label = "wallet_count_up",
    )

    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            avatarScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }

    LaunchedEffect(completionSummary) {
        if (completionSummary != null) {
            if (!reducedMotion) delay(240L)
            if (completionSummary.isOnline) {
                displayedExp = completionSummary.newExp
            }
            targetWallet = completionSummary.walletAfterDiamonds
        }
    }

    PaperBackground(
        showCreaseLines = true,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 540.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                StickerBadge(
                    text = "MATCH COMPLETE",
                    backgroundColor = tokens.brandHoney,
                    rotationDegrees = -2f,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Winner avatar + Biscuit holding trophy on wood podium (DRD §9 & §16 Step 7)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Runner up step
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(110.dp),
                    ) {
                        TeddyAvatar(
                            config = runnerUp.avatarConfig,
                            backdropColor = runnerUp.color.color,
                            size = 46.dp,
                            equippedCosmetics = runnerUp.equippedCosmetics,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(tokens.woodLight)
                                .drawBehind {
                                    drawWoodTexture(tokens.woodLight, tokens.woodMid, tokens.woodDark)
                                }
                                .border(2.5.dp, tokens.ink, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        ) {
                            Text(
                                text = "$runnerScore",
                                style = LilitaNumberStyle.copy(fontSize = 22.sp),
                                color = tokens.paperCream,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Winner step
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(136.dp),
                    ) {
                        Box(modifier = Modifier.scale(avatarScale.value)) {
                            TeddyAvatar(
                                config = winner.avatarConfig,
                                backdropColor = winner.color.color,
                                size = 64.dp,
                                equippedCosmetics = winner.equippedCosmetics,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                .background(tokens.woodMid)
                                .drawBehind {
                                    drawWoodTexture(tokens.woodLight, tokens.woodMid, tokens.woodDark)
                                }
                                .border(3.dp, tokens.ink, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                        ) {
                            Text(
                                text = "$winnerScore",
                                style = LilitaNumberStyle.copy(fontSize = 30.sp),
                                color = tokens.brandHoney,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    TeddyBiscuit(
                        pose = TeddyPose.TROPHY,
                        expression = TeddyExpression.CELEBRATING,
                        size = 86.dp,
                        reducedMotion = reducedMotion,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                ChunkyDisplayText(
                    text = "${winner.name} wins!",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 36.sp),
                    fillColor = winner.color.color,
                    shadowColor = tokens.ink,
                    modifier = Modifier.testTag("match_winner_title"),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${matchState.totalP1Score} to ${matchState.totalP2Score} across 83 boxes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.ink,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Paper Stat Table Card
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Round",
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.ink,
                                modifier = Modifier.weight(1.3f),
                            )
                            Text(
                                text = player1.initials,
                                style = LilitaNumberStyle.copy(fontSize = 18.sp),
                                color = tokens.ink,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = player2.initials,
                                style = LilitaNumberStyle.copy(fontSize = 18.sp),
                                color = tokens.ink,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 2.dp, color = tokens.ink)

                        matchState.completedLevels.forEach { level ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Level ${level.levelNumber} (${level.dotCount}×${level.dotCount})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.ink,
                                    modifier = Modifier.weight(1.3f),
                                )
                                Text(
                                    text = "${level.p1Boxes}",
                                    style = LilitaNumberStyle.copy(fontSize = 20.sp),
                                    color = tokens.ink,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${level.p2Boxes}",
                                    style = LilitaNumberStyle.copy(fontSize = 20.sp),
                                    color = tokens.ink,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            HorizontalDivider(thickness = 1.5.dp, color = tokens.paperLine)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                modifier = Modifier.weight(1.3f),
                            )
                            Text(
                                text = "${matchState.totalP1Score}",
                                style = LilitaNumberStyle.copy(fontSize = 24.sp),
                                color = tokens.ink,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${matchState.totalP2Score}",
                                style = LilitaNumberStyle.copy(fontSize = 24.sp),
                                color = tokens.ink,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.3f),
                            ) {
                                DiamondIcon(size = 16.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Diamonds",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.diamondTealDark,
                                )
                            }
                            Text(
                                text = "${matchState.totalP1Diamonds}",
                                style = LilitaNumberStyle.copy(fontSize = 20.sp),
                                color = tokens.diamondTealDark,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${matchState.totalP2Diamonds}",
                                style = LilitaNumberStyle.copy(fontSize = 20.sp),
                                color = tokens.diamondTealDark,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Diamond Wallet Earnings Card with 600ms count-up animation & AdMob 2x Watch Ad CTA
                StudioCard(
                    backgroundColor = tokens.warmHighlight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("results_diamonds_card"),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DiamondIcon(size = 26.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Diamonds collected",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.ink,
                                    )
                                    Text(
                                        text = "+${completionSummary?.p1DiamondsEarned ?: matchState.totalP1Diamonds} added to your wallet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.ink.copy(alpha = 0.78f),
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$animatedWallet",
                                    style = LilitaNumberStyle.copy(fontSize = 26.sp),
                                    color = tokens.diamondTealDark,
                                    modifier = Modifier.testTag("results_wallet_balance"),
                                )
                                Text(
                                    text = "Balance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.ink.copy(alpha = 0.72f),
                                )
                            }
                        }

                        if (adRewardClaimed) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.brandMint.copy(alpha = 0.30f))
                                    .border(2.dp, tokens.ink, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("results_ad_claimed_badge"),
                            ) {
                                Text(
                                    text = "2x Diamonds Claimed!",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.ink,
                                )
                            }
                        } else if (!adOfferDismissed) {
                            Spacer(modifier = Modifier.height(14.dp))
                            WatchAdDoubleDiamondsButton(
                                onClick = onWatchAdToDoubleDiamonds,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "No thanks",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                                    color = tokens.ink.copy(alpha = 0.72f),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .clickable { adOfferDismissed = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("results_ad_no_thanks_link"),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // EXP Reward Card
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (completionSummary != null && completionSummary.isOnline) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Online match reward",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                )
                                StickerBadge(
                                    text = "+${completionSummary.expAwarded} EXP",
                                    backgroundColor = tokens.brandHoney,
                                    rotationDegrees = 2f,
                                    modifier = Modifier.testTag("results_exp_awarded"),
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            ExpProgressBar(
                                totalExp = displayedExp,
                                reducedMotion = reducedMotion,
                            )
                            AnimatedVisibility(
                                visible = completionSummary.leveledUp,
                                enter = fadeIn(tween(if (reducedMotion) 0 else 280)),
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(thickness = 2.dp, color = tokens.ink)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    StickerBadge(
                                        text = "LEVEL ${completionSummary.newLevel}!",
                                        backgroundColor = tokens.brandLilac,
                                        rotationDegrees = -2f,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "You advanced to a new profile level!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.ink,
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.offline_exp_note),
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink,
                                modifier = Modifier.testTag("results_offline_exp_note"),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isOnline && !isOpponentFriend && onAddOpponentFriend != null) {
                    StudioSecondaryButton(
                        text = "Add ${player2.name} as friend",
                        onClick = onAddOpponentFriend,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "results_add_friend_button",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    StudioPrimaryButton(
                        text = "Play again",
                        onClick = onPlayAgain,
                        modifier = Modifier.weight(1f),
                        testTag = "results_play_again_button",
                    )
                    StudioSecondaryButton(
                        text = "Home",
                        onClick = onHome,
                        modifier = Modifier.weight(1f),
                        testTag = "results_home_button",
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RoomChatPanel(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    PaperBackground(
        showCreaseLines = false,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (messages.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    TeddyBiscuit(
                        pose = TeddyPose.COCOA,
                        expression = TeddyExpression.HAPPY,
                        size = 104.dp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Say hello to start chatting!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.ink,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleItem(msg = msg)
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = tokens.ink)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.paperShade)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                StudioTextField(
                    value = draft,
                    onValueChange = { if (it.length <= 140) draft = it },
                    label = "",
                    placeholder = "Write a note...",
                    modifier = Modifier.weight(1f),
                    testTag = "chat_message_input",
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            onSendMessage(draft)
                            draft = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (draft.isNotBlank()) tokens.brandHoney else tokens.paperCream)
                        .border(2.5.dp, tokens.ink, CircleShape)
                        .testTag("chat_send_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send message",
                        tint = tokens.ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(msg: ChatMessage) {
    val tokens = StudioTheme.tokens
    // DRD §10.2: Other bubble = paper.cream with 2px ink outline; My bubble = sky/player color with ink text
    val bubbleShape = if (msg.isLocalUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }
    val bubbleFill = if (msg.isLocalUser) tokens.brandSky.copy(alpha = 0.35f) else tokens.paperCream

    Row(
        horizontalArrangement = if (msg.isLocalUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (!msg.isLocalUser) {
            TeddyAvatar(
                config = msg.senderAvatar,
                backdropColor = msg.senderColor.color,
                size = 32.dp,
                isCircle = true,
                equippedCosmetics = msg.senderCosmetics,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            horizontalAlignment = if (msg.isLocalUser) Alignment.End else Alignment.Start,
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(bubbleShape)
                .background(bubbleFill)
                .border(2.dp, tokens.ink, bubbleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = msg.senderName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.ink,
                )
                Text(
                    text = msg.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.ink.copy(alpha = 0.65f),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.ink,
            )
        }
        if (msg.isLocalUser) {
            Spacer(modifier = Modifier.width(8.dp))
            TeddyAvatar(
                config = msg.senderAvatar,
                backdropColor = msg.senderColor.color,
                size = 32.dp,
                isCircle = true,
                equippedCosmetics = msg.senderCosmetics,
            )
        }
    }
}

@Composable
fun RoomVoicePanel(
    subscription: SubscriptionState,
    participants: List<VoiceParticipant>,
    isConnected: Boolean,
    reducedMotion: Boolean,
    onToggleMute: () -> Unit,
    onToggleConnect: () -> Unit,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens

    FeltBackground(modifier = modifier.fillMaxSize()) {
        if (!subscription.isActive) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp),
                    ) {
                        StickerBadge(
                            text = "LOCKED",
                            backgroundColor = tokens.brandBerry,
                            rotationDegrees = -3f,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TeddyBiscuit(
                            pose = TeddyPose.WAVING,
                            expression = TeddyExpression.SURPRISED,
                            size = 96.dp,
                            reducedMotion = reducedMotion,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Voice channel is locked",
                            style = MaterialTheme.typography.headlineSmall,
                            color = tokens.ink,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Text chat is free for everyone. Subscribe for ₹9 / month to talk in match rooms.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StudioPrimaryButton(
                            text = "View voice plan · ₹9 / mo",
                            onClick = onOpenPaywall,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "voice_locked_paywall_button",
                        )
                    }
                }
            }
            return@FeltBackground
        }

        val localUser = participants.firstOrNull { it.isLocalUser }
        val isLocalMuted = localUser?.isMuted ?: false

        // DRD §12.1: honey pulsing ring 1.2s cycle, paused when muted or reduced motion
        val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (reducedMotion) 1.0f else 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse_scale",
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StickerBadge(
                    text = if (isConnected) "VOICE LIVE" else "PAUSED",
                    backgroundColor = if (isConnected) tokens.brandMint else tokens.paperShade,
                    rotationDegrees = -2f,
                )
                Text(
                    text = "Simulated audio",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.paperCream,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                participants.forEach { participant ->
                    val isSpeakingNow = isConnected && participant.isSpeaking && !participant.isMuted
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSpeakingNow) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(tokens.brandHoney)
                                            .border(2.dp, tokens.ink, CircleShape),
                                    )
                                }
                                TeddyAvatar(
                                    config = participant.avatarConfig,
                                    backdropColor = participant.color.color,
                                    size = 42.dp,
                                    isCircle = true,
                                    equippedCosmetics = participant.equippedCosmetics,
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (participant.isLocalUser) "${participant.name} (You)" else participant.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                )
                                Text(
                                    text = when {
                                        !isConnected -> "Paused"
                                        participant.isMuted -> "Muted"
                                        isSpeakingNow -> "Speaking"
                                        else -> "Listening"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSpeakingNow) tokens.brandHoneyDark else tokens.ink.copy(alpha = 0.75f),
                                )
                            }
                            Icon(
                                imageVector = if (participant.isMuted) Icons.Outlined.MicOff else Icons.Outlined.Mic,
                                contentDescription = if (participant.isMuted) "Muted" else "Mic active",
                                tint = tokens.ink,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StudioSecondaryButton(
                    text = if (isLocalMuted) "Unmute" else "Mute",
                    onClick = onToggleMute,
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    testTag = "voice_mute_button",
                )
                if (isConnected) {
                    StudioDangerButton(
                        text = "Leave voice",
                        onClick = onToggleConnect,
                        modifier = Modifier.weight(1f),
                        testTag = "voice_leave_button",
                    )
                } else {
                    StudioPrimaryButton(
                        text = "Rejoin",
                        onClick = onToggleConnect,
                        modifier = Modifier.weight(1f),
                        testTag = "voice_leave_button",
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchAdDoubleDiamondsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = StudioTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val lipDepth = 4.dp
    val pressOffsetPx = if (pressed) with(androidx.compose.ui.platform.LocalDensity.current) { lipDepth.toPx().roundToInt() } else 0
    val shape = RoundedCornerShape(18.dp)
    val flashyPinkBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF247E),
            Color(0xFFFF68B8),
            Color(0xFFFF1E78),
        ),
    )
    val deepPinkLip = Color(0xFFB31056)
    val darkPinkOutline = Color(0xFF8C0C44)

    Box(
        modifier = modifier
            .padding(bottom = lipDepth)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .testTag("results_watch_ad_button"),
    ) {
        // Hard bottom lip
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = lipDepth)
                .clip(shape)
                .background(deepPinkLip)
                .border(3.dp, tokens.ink, shape),
        )
        // Main flashy pink face
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, pressOffsetPx) }
                .clip(shape)
                .background(flashyPinkBrush)
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.28f),
                        start = Offset(0f, 3.dp.toPx()),
                        end = Offset(size.width, 3.dp.toPx()),
                        strokeWidth = 3.dp.toPx(),
                    )
                }
                .border(3.dp, tokens.ink, shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    DiamondIcon(size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    ChunkyDisplayText(
                        text = "2x Watch Ad",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = LilitaOneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        ),
                        fillColor = Color.White,
                        shadowColor = darkPinkOutline,
                        outlineColor = darkPinkOutline,
                        strokeWidth = 2.dp,
                        shadowOffset = 1.5.dp,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Watch ad to double your diamonds",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("results_watch_ad_subtag"),
                )
            }
        }
    }
}

