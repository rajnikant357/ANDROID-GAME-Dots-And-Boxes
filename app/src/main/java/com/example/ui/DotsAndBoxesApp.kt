package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.GenderOption
import com.example.model.PlayerColorOption
import com.example.model.PlayerPattern
import com.example.model.TeddyAvatarConfig
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CuteNavIcon
import com.example.ui.components.StudioBottomNav
import com.example.ui.screens.CreateProfileScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GameRoomScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.OfflineSetupScreen
import com.example.ui.screens.PaywallScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoomWaitingScreen
import com.example.ui.screens.RoomsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.DotsAndBoxesTheme
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.delay

object AppRoutes {
    const val ROUTE_SPLASH = "splash"
    const val ROUTE_WELCOME = "welcome"
    const val ROUTE_CREATE_PROFILE = "create_profile"
    const val ROUTE_EDIT_PROFILE = "edit_profile"
    const val ROUTE_HOME = "home"
    const val ROUTE_FRIENDS = "friends"
    const val ROUTE_STORE = "store"
    const val ROUTE_LEADERBOARD = "leaderboard"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_OFFLINE_SETUP = "offline_setup"
    const val ROUTE_ROOM = "room"
    const val ROUTE_LOBBY = "lobby"
    const val ROUTE_ROOMS = "rooms"
    const val ROUTE_ROOM_WAITING = "room_waiting"
    const val ROUTE_PAYWALL = "paywall"
    const val ROUTE_SETTINGS = "settings"
}

@Composable
fun DotsAndBoxesApp(
    viewModel: AppViewModel = viewModel(),
) {
    val isLoaded by viewModel.isLoaded.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val offlineConfig by viewModel.offlineConfig.collectAsStateWithLifecycle()
    val savedMatch by viewModel.savedMatchSession.collectAsStateWithLifecycle()
    val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
    val leaderboardScope by viewModel.leaderboardScope.collectAsStateWithLifecycle()
    val leaderboardEntries by viewModel.leaderboardEntries.collectAsStateWithLifecycle()
    val openRooms by viewModel.openRooms.collectAsStateWithLifecycle()
    val onlinePlayers by viewModel.onlinePlayers.collectAsStateWithLifecycle()

    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val ownedCosmetics by viewModel.ownedCosmetics.collectAsStateWithLifecycle()
    val purchaseHistory by viewModel.purchaseHistory.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val incomingRequests by viewModel.incomingRequests.collectAsStateWithLifecycle()
    val outgoingRequests by viewModel.outgoingRequests.collectAsStateWithLifecycle()
    val blockedIds by viewModel.blockedIds.collectAsStateWithLifecycle()
    val waitingRoom by viewModel.waitingRoom.collectAsStateWithLifecycle()
    val roomsNotice by viewModel.roomsNotice.collectAsStateWithLifecycle()
    val isSearchingQuickMatch by viewModel.isSearchingQuickMatch.collectAsStateWithLifecycle()
    val searchResult by viewModel.searchResult.collectAsStateWithLifecycle()
    val inviteBanner by viewModel.inviteBanner.collectAsStateWithLifecycle()
    val navigateToRoomSignal by viewModel.navigateToRoomSignal.collectAsStateWithLifecycle()
    val uiNotice by viewModel.uiNotice.collectAsStateWithLifecycle()

    DotsAndBoxesTheme(dyslexiaFriendly = settings.dyslexiaFont) {
        val tokens = StudioTheme.tokens
        val navController = rememberNavController()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        LaunchedEffect(navigateToRoomSignal) {
            if (navigateToRoomSignal > 0 && activeRoom != null) {
                navController.navigate(AppRoutes.ROUTE_ROOM) {
                    launchSingleTop = true
                }
            }
        }

        LaunchedEffect(uiNotice) {
            if (uiNotice != null) {
                delay(2600L)
                viewModel.clearUiNotice()
            }
        }

        val tabRoutes = setOf(
            AppRoutes.ROUTE_HOME,
            AppRoutes.ROUTE_FRIENDS,
            AppRoutes.ROUTE_LEADERBOARD,
            AppRoutes.ROUTE_PROFILE,
        )
        val showPrimaryNav = currentRoute in tabRoutes
        val selectedTab = when (currentRoute) {
            AppRoutes.ROUTE_FRIENDS -> BottomNavTab.FRIENDS
            AppRoutes.ROUTE_LEADERBOARD -> BottomNavTab.RANKS
            AppRoutes.ROUTE_PROFILE -> BottomNavTab.ME
            else -> BottomNavTab.HOME
        }

        fun navigateToTab(tab: BottomNavTab) {
            val target = when (tab) {
                BottomNavTab.HOME -> AppRoutes.ROUTE_HOME
                BottomNavTab.FRIENDS -> AppRoutes.ROUTE_FRIENDS
                BottomNavTab.RANKS -> AppRoutes.ROUTE_LEADERBOARD
                BottomNavTab.ME -> AppRoutes.ROUTE_PROFILE
            }
            navController.navigate(target) {
                popUpTo(AppRoutes.ROUTE_HOME) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.woodMid),
        ) {
            val isExpandedWidth = maxWidth >= 680.dp

            Scaffold(
                containerColor = tokens.paperCream,
                contentWindowInsets = if (showPrimaryNav) WindowInsets.safeDrawing else WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showPrimaryNav && !isExpandedWidth) {
                        StudioBottomNav(
                            selectedTab = selectedTab,
                            onSelectTab = ::navigateToTab,
                            pendingFriendRequests = incomingRequests.size,
                        )
                    }
                },
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (showPrimaryNav && isExpandedWidth) {
                        NavigationRail(
                            containerColor = tokens.woodMid,
                            modifier = Modifier.fillMaxHeight(),
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            BottomNavTab.entries.forEach { tab ->
                                val selected = tab == selectedTab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab) },
                                    icon = {
                                        if (tab == BottomNavTab.FRIENDS && incomingRequests.isNotEmpty()) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = tokens.brandBerry) {
                                                        Text("${incomingRequests.size}", color = tokens.warmHighlight)
                                                    }
                                                },
                                            ) {
                                                CuteNavIcon(tab = tab, size = 24.dp)
                                            }
                                        } else {
                                            CuteNavIcon(tab = tab, size = 24.dp)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = tokens.ink,
                                        selectedTextColor = tokens.brandHoney,
                                        indicatorColor = tokens.paperCream,
                                        unselectedIconColor = tokens.paperCream,
                                        unselectedTextColor = tokens.paperCream,
                                    ),
                                    modifier = Modifier.testTag("rail_${tab.testTag}"),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(tokens.ink),
                        )
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        NavHost(
                            navController = navController,
                            startDestination = AppRoutes.ROUTE_SPLASH,
                            enterTransition = {
                                if (settings.reducedMotion) EnterTransition.None
                                else fadeIn(tween(240))
                            },
                            exitTransition = {
                                if (settings.reducedMotion) ExitTransition.None
                                else fadeOut(tween(200))
                            },
                        ) {
                            composable(AppRoutes.ROUTE_SPLASH) {
                                SplashScreen(
                                    isLoaded = isLoaded,
                                    hasProfile = userProfile != null,
                                    reducedMotion = settings.reducedMotion,
                                    onFinished = { hasProfile ->
                                        val dest = if (hasProfile) AppRoutes.ROUTE_HOME else AppRoutes.ROUTE_WELCOME
                                        navController.navigate(dest) {
                                            popUpTo(AppRoutes.ROUTE_SPLASH) { inclusive = true }
                                        }
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_WELCOME) {
                                WelcomeScreen(
                                    onCreateProfile = {
                                        navController.navigate(AppRoutes.ROUTE_CREATE_PROFILE)
                                    },
                                    onPlayOfflineDirect = {
                                        navController.navigate(AppRoutes.ROUTE_HOME) {
                                            popUpTo(AppRoutes.ROUTE_WELCOME) { inclusive = true }
                                        }
                                        navController.navigate(AppRoutes.ROUTE_OFFLINE_SETUP)
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_CREATE_PROFILE) {
                                CreateProfileScreen(
                                    onBack = {
                                        if (!navController.popBackStack()) {
                                            navController.navigate(AppRoutes.ROUTE_WELCOME)
                                        }
                                    },
                                    onSaveProfile = { name, initials, gender, color, pattern, avatar ->
                                        viewModel.saveProfile(name, initials, gender, color, pattern, avatar)
                                        navController.navigate(AppRoutes.ROUTE_HOME) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_EDIT_PROFILE) {
                                val current = userProfile
                                CreateProfileScreen(
                                    initialName = current?.displayName ?: "",
                                    initialInitials = current?.initials ?: "",
                                    initialGender = current?.gender ?: GenderOption.PREFER_NOT_TO_SAY,
                                    initialColor = current?.colorOption ?: PlayerColorOption.TOMATO,
                                    initialPattern = current?.pattern ?: PlayerPattern.STRIPES,
                                    initialAvatar = current?.avatarConfig ?: TeddyAvatarConfig(),
                                    isEditMode = true,
                                    onBack = { navController.popBackStack() },
                                    onSaveProfile = { name, initials, gender, color, pattern, avatar ->
                                        viewModel.saveProfile(name, initials, gender, color, pattern, avatar)
                                        navController.popBackStack()
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_HOME) {
                                HomeScreen(
                                    userProfile = userProfile,
                                    wallet = wallet,
                                    openRoomsCount = openRooms.size,
                                    savedMatch = savedMatch,
                                    isSearchingOnline = isSearchingQuickMatch,
                                    onOpenOfflineSetup = {
                                        navController.navigate(AppRoutes.ROUTE_OFFLINE_SETUP)
                                    },
                                    onOpenLobby = {
                                        viewModel.startQuickMatchSearch()
                                    },
                                    onCancelOnlineSearch = {
                                        viewModel.cancelQuickMatchSearch()
                                    },
                                    onOpenRooms = {
                                        navController.navigate(AppRoutes.ROUTE_ROOMS)
                                    },
                                    onOpenStore = {
                                        navController.navigate(AppRoutes.ROUTE_STORE) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onOpenProfile = {
                                        navigateToTab(BottomNavTab.ME)
                                    },
                                    onOpenCreateProfile = {
                                        navController.navigate(AppRoutes.ROUTE_CREATE_PROFILE)
                                    },
                                    onResumeSavedMatch = {
                                        if (viewModel.resumeSavedMatch()) {
                                            navController.navigate(AppRoutes.ROUTE_ROOM)
                                        }
                                    },
                                    onDiscardSavedMatch = {
                                        viewModel.leaveMatchConfirmed()
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_FRIENDS) {
                                FriendsScreen(
                                    friends = friends,
                                    incomingRequests = incomingRequests,
                                    outgoingRequests = outgoingRequests,
                                    searchResult = searchResult,
                                    inviteBanner = inviteBanner,
                                    onSearchById = { viewModel.searchPlayerById(it) },
                                    onClearSearch = { viewModel.clearPlayerSearch() },
                                    onSendRequestToFound = { viewModel.sendFriendRequest(it) },
                                    onAcceptRequest = { viewModel.acceptFriendRequest(it) },
                                    onDeclineOrCancelRequest = { viewModel.declineOrCancelRequest(it) },
                                    onInviteFriend = { viewModel.inviteFriendToNewMatch(it) },
                                    onRemoveFriend = { viewModel.removeFriend(it) },
                                    onBlockPlayer = { viewModel.blockPlayer(it) },
                                    onDismissInviteBanner = { viewModel.dismissInviteBanner() },
                                )
                            }

                            composable(AppRoutes.ROUTE_STORE) {
                                StoreScreen(
                                    wallet = wallet,
                                    userProfile = userProfile,
                                    subscription = subscription,
                                    renewalDateLabel = viewModel.formatRenewalDate(subscription.renewalDateEpochMillis),
                                    diamondPacks = viewModel.diamondPacks,
                                    cosmeticCatalog = viewModel.cosmeticCatalog,
                                    ownedCosmetics = ownedCosmetics,
                                    purchaseHistory = purchaseHistory,
                                    onBuyDiamondPack = { viewModel.buyDiamondPack(it) },
                                    onBuyCosmetic = { viewModel.buyCosmetic(it) },
                                    onEquipCosmetic = { viewModel.equipCosmetic(it) },
                                    onSubscribeVoice = { viewModel.subscribeMonthly() },
                                    onCancelVoice = { viewModel.cancelSubscription() },
                                    onOpenVoiceDetails = { navController.navigate(AppRoutes.ROUTE_PAYWALL) },
                                    onBack = { navController.popBackStack() },
                                )
                            }

                            composable(AppRoutes.ROUTE_LEADERBOARD) {
                                LeaderboardScreen(
                                    scope = leaderboardScope,
                                    entries = leaderboardEntries,
                                    friends = friends,
                                    incomingRequests = incomingRequests,
                                    outgoingRequests = outgoingRequests,
                                    blockedIds = blockedIds,
                                    onSelectScope = { viewModel.setLeaderboardScope(it) },
                                    onAddFriend = { viewModel.sendFriendRequest(it) },
                                )
                            }

                            composable(AppRoutes.ROUTE_PROFILE) {
                                ProfileScreen(
                                    userProfile = userProfile,
                                    wallet = wallet,
                                    subscription = subscription,
                                    renewalDateLabel = viewModel.formatRenewalDate(subscription.renewalDateEpochMillis),
                                    cosmeticCatalog = viewModel.cosmeticCatalog,
                                    ownedCosmetics = ownedCosmetics,
                                    reducedMotion = settings.reducedMotion,
                                    onCreateProfile = {
                                        navController.navigate(AppRoutes.ROUTE_CREATE_PROFILE)
                                    },
                                    onEditProfile = {
                                        navController.navigate(AppRoutes.ROUTE_EDIT_PROFILE)
                                    },
                                    onEquipCosmetic = { viewModel.equipCosmetic(it) },
                                    onOpenStore = {
                                        navController.navigate(AppRoutes.ROUTE_STORE) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onCopyPlayerId = { id ->
                                        // Notice handled by user feedback or clipboard
                                    },
                                    onOpenPaywall = {
                                        navController.navigate(AppRoutes.ROUTE_PAYWALL)
                                    },
                                    onOpenSettings = {
                                        navController.navigate(AppRoutes.ROUTE_SETTINGS)
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_OFFLINE_SETUP) {
                                OfflineSetupScreen(
                                    initialConfig = offlineConfig,
                                    userProfile = userProfile,
                                    onBack = { navController.popBackStack() },
                                    onStartMatch = { config ->
                                        viewModel.startOfflineMatch(config)
                                        navController.navigate(AppRoutes.ROUTE_ROOM)
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_LOBBY) {
                                LobbyScreen(
                                    openRooms = openRooms,
                                    onlinePlayers = onlinePlayers,
                                    friends = friends,
                                    isSearchingQuickMatch = isSearchingQuickMatch,
                                    inviteBanner = inviteBanner,
                                    onBack = {
                                        viewModel.cancelQuickMatchSearch()
                                        navController.popBackStack()
                                    },
                                    onStartQuickMatch = {
                                        viewModel.startQuickMatchSearch()
                                    },
                                    onCancelQuickMatch = {
                                        viewModel.cancelQuickMatchSearch()
                                    },
                                    onInviteFriend = { friend ->
                                        viewModel.inviteFriendToNewMatch(friend)
                                    },
                                    onOpenRooms = {
                                        navController.navigate(AppRoutes.ROUTE_ROOMS)
                                    },
                                    onJoinRoom = { room ->
                                        viewModel.startOnlineMatchFromRoom(room)
                                        navController.navigate(AppRoutes.ROUTE_ROOM)
                                    },
                                )
                            }

                            composable(AppRoutes.ROUTE_ROOMS) {
                                RoomsScreen(
                                    publicRooms = openRooms,
                                    roomsNotice = roomsNotice,
                                    onBack = { navController.popBackStack() },
                                    onCreateRoom = { visibility ->
                                        viewModel.createWaitingRoom(visibility)
                                        navController.navigate(AppRoutes.ROUTE_ROOM_WAITING)
                                    },
                                    onJoinByCode = { code ->
                                        val ok = viewModel.joinWaitingRoomByCode(code)
                                        if (ok) {
                                            navController.navigate(AppRoutes.ROUTE_ROOM_WAITING)
                                        }
                                        ok
                                    },
                                    onJoinPublicRoom = { room ->
                                        viewModel.joinWaitingRoomFromPublicList(room)
                                        navController.navigate(AppRoutes.ROUTE_ROOM_WAITING)
                                    },
                                    onDismissNotice = { viewModel.clearRoomsNotice() },
                                )
                            }

                            composable(AppRoutes.ROUTE_ROOM_WAITING) {
                                val waiting = waitingRoom
                                if (waiting != null) {
                                    RoomWaitingScreen(
                                        waitingRoom = waiting,
                                        friends = friends,
                                        onCopyCode = {},
                                        onInviteFriend = { friend -> viewModel.inviteFriendToWaitingRoom(friend) },
                                        onSimulateGuestJoin = { viewModel.simulateGuestJoinWaitingRoom() },
                                        onStartMatch = {
                                            if (viewModel.startMatchFromWaitingRoom()) {
                                                navController.navigate(AppRoutes.ROUTE_ROOM) {
                                                    popUpTo(AppRoutes.ROUTE_ROOM_WAITING) { inclusive = true }
                                                }
                                            }
                                        },
                                        onLeaveRoom = {
                                            viewModel.leaveWaitingRoom()
                                            navController.popBackStack()
                                        },
                                    )
                                }
                            }

                            composable(AppRoutes.ROUTE_ROOM) {
                                val room = activeRoom
                                if (room != null) {
                                    GameRoomScreen(
                                        roomState = room,
                                        settings = settings,
                                        subscription = subscription,
                                        friends = friends,
                                        onLineTapped = { viewModel.onLineTapped(it) },
                                        onDismissPassDevice = { viewModel.dismissPassDeviceOverlay() },
                                        onNextLevel = { viewModel.proceedToNextLevel() },
                                        onPlayAgain = { viewModel.playAgainInCurrentMode() },
                                        onLeaveConfirmed = {
                                            viewModel.leaveMatchConfirmed()
                                            navController.popBackStack(AppRoutes.ROUTE_HOME, inclusive = false)
                                        },
                                        onSendChatMessage = { viewModel.sendChatMessage(it) },
                                        onToggleVoiceMute = { viewModel.toggleLocalVoiceMute() },
                                        onToggleVoiceConnection = { viewModel.toggleVoiceConnection() },
                                        onOpenPaywall = {
                                            navController.navigate(AppRoutes.ROUTE_PAYWALL)
                                        },
                                        onAddOpponentFriend = { opponent ->
                                            viewModel.sendFriendRequestToIdentity(opponent)
                                        },
                                        onInviteFriendInMatch = { friend ->
                                            viewModel.inviteFriendDuringActiveMatch(friend)
                                        },
                                        onWatchAdToDoubleDiamonds = {
                                            // TODO(AdMob): Show RewardedAd here and call claimDoubleDiamondsReward() inside OnUserEarnedRewardListener
                                            viewModel.claimDoubleDiamondsReward()
                                        },
                                    )
                                }
                            }

                            composable(AppRoutes.ROUTE_PAYWALL) {
                                PaywallScreen(
                                    subscription = subscription,
                                    renewalDateLabel = viewModel.formatRenewalDate(subscription.renewalDateEpochMillis),
                                    onSubscribe = { viewModel.subscribeMonthly() },
                                    onCancelSubscription = { viewModel.cancelSubscription() },
                                    onBack = { navController.popBackStack() },
                                )
                            }

                            composable(AppRoutes.ROUTE_SETTINGS) {
                                SettingsScreen(
                                    settings = settings,
                                    onUpdateSettings = { viewModel.updateSettings(it) },
                                    onBack = { navController.popBackStack() },
                                )
                            }
                        }

                        // Global subtle tabletop notice banner
                        androidx.compose.animation.AnimatedVisibility(
                            visible = uiNotice != null,
                            enter = fadeIn(tween(180)),
                            exit = fadeOut(tween(180)),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
                        ) {
                            val msg = uiNotice ?: ""
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(tokens.brandHoney)
                                    .border(2.5.dp, tokens.ink, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.ink,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
