package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.MockGameService
import com.example.engine.DotsAndBoxesEngine
import com.example.engine.LevelSummary
import com.example.engine.PlayerSlot
import com.example.model.AppSettings
import com.example.model.BoxSkinId
import com.example.model.ExpConfig
import com.example.model.GenderOption
import com.example.model.PlayerColorOption
import com.example.model.PlayerIdentity
import com.example.model.RoomVisibility
import com.example.model.SubscriptionState
import com.example.model.UserProfile
import com.example.model.WalletState
import com.example.ui.components.BottomNavTab
import com.example.ui.components.StudioBottomNav
import com.example.ui.screens.CreateProfileScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GameRoomScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelTransitionPanel
import com.example.ui.screens.MatchResultsPanel
import com.example.ui.screens.PaywallScreen
import com.example.ui.screens.RoomsScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.DotsAndBoxesTheme
import com.example.viewmodel.ActiveRoomUiState
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.CosmeticBuyResult
import com.example.viewmodel.MatchCompletionSummary
import com.example.viewmodel.PlayerSearchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Dots & Boxes", appName)
    }

    @Test
    fun `welcome screen displays primary and offline direct actions`() {
        var createClicked = false
        var offlineClicked = false
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                WelcomeScreen(
                    onCreateProfile = { createClicked = true },
                    onPlayOfflineDirect = { offlineClicked = true },
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("welcome_create_profile_button").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("welcome_offline_direct_button").performScrollTo().assertIsDisplayed().performClick()
        assertTrue(createClicked)
        assertTrue(offlineClicked)
    }

    @Test
    fun `paywall screen displays prototype note and price`() {
        var subscribed = false
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                PaywallScreen(
                    subscription = SubscriptionState(isActive = false),
                    renewalDateLabel = "",
                    onSubscribe = { subscribed = true },
                    onCancelSubscription = {},
                    onBack = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("₹9 / month").assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_prototype_note").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_subscribe_button").performScrollTo().performClick()
        assertTrue(subscribed)
    }

    @Test
    fun `home screen shows player header under logo, top-right store button without settings, and 3 bulky buttons`() {
        var startedOnline = false
        var startedOffline = false
        var openedRooms = false
        var openedStore = false
        var openedProfile = false
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                HomeScreen(
                    userProfile = UserProfile(
                        displayName = "Aria",
                        initials = "A",
                        gender = GenderOption.FEMALE,
                        colorOption = PlayerColorOption.TOMATO,
                        totalExp = 125,
                    ),
                    wallet = WalletState(diamonds = 180),
                    openRoomsCount = 4,
                    savedMatch = null,
                    onOpenOfflineSetup = { startedOffline = true },
                    onOpenLobby = { startedOnline = true },
                    onOpenRooms = { openedRooms = true },
                    onOpenStore = { openedStore = true },
                    onOpenProfile = { openedProfile = true },
                    onOpenCreateProfile = {},
                    onResumeSavedMatch = {},
                    onDiscardSavedMatch = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_header_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_wallet_pill").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_store_button").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("home_settings_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_profile_avatar").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_bottom_strip_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_player_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_player_avatar", useUnmergedTree = true).assertIsDisplayed()
        val avatarBounds = composeTestRule.onNodeWithTag("home_player_avatar", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val nameBounds = composeTestRule.onNodeWithTag("home_player_name", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(avatarBounds.left < nameBounds.left)
        composeTestRule.onNodeWithText("Lv 2", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("25/100 EXP", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_player_header").performClick()
        composeTestRule.onNodeWithTag("home_start_online_button").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("home_offline_mode").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("home_rooms_button").performScrollTo().assertIsDisplayed().performClick()
        assertTrue(openedStore)
        assertTrue(openedProfile)
        assertTrue(startedOnline)
        assertTrue(startedOffline)
        assertTrue(openedRooms)
    }

    @Test
    fun `home screen displays matchmaking overlay and cancel action when searching online`() {
        var cancelled = false
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                HomeScreen(
                    userProfile = UserProfile(
                        displayName = "Aria",
                        initials = "A",
                        gender = GenderOption.FEMALE,
                        colorOption = PlayerColorOption.TOMATO,
                    ),
                    wallet = WalletState(diamonds = 180),
                    openRoomsCount = 4,
                    savedMatch = null,
                    isSearchingOnline = true,
                    onOpenOfflineSetup = {},
                    onOpenLobby = {},
                    onCancelOnlineSearch = { cancelled = true },
                    onOpenRooms = {},
                    onOpenStore = {},
                    onOpenProfile = {},
                    onOpenCreateProfile = {},
                    onResumeSavedMatch = {},
                    onDiscardSavedMatch = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_matchmaking_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_cancel_matchmaking_button").assertIsDisplayed().performClick()
        assertTrue(cancelled)
    }

    @Test
    fun `profile form places name above teddy fur and automatically derives single first letter initial`() {
        var savedInitial = ""
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                CreateProfileScreen(
                    isEditMode = false,
                    onBack = null,
                    onSaveProfile = { _, initials, _, _, _, _ -> savedInitial = initials },
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_initials_input").assertDoesNotExist()
        val nameBounds = composeTestRule.onNodeWithTag("profile_name_input").fetchSemanticsNode().boundsInRoot
        val furLabelBounds = composeTestRule.onNodeWithText("Teddy fur (8 styles)").fetchSemanticsNode().boundsInRoot
        assertTrue(nameBounds.top < furLabelBounds.top)
        composeTestRule.onNodeWithTag("profile_name_input").performScrollTo().performTextInput("Karan Rao")
        composeTestRule.onNodeWithTag("profile_auto_initial_note").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_save_button").performScrollTo().performClick()
        assertEquals("K", savedInitial)
    }

    @Test
    fun `bottom nav renders four tabs without store and toggles friends request badge`() {
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                StudioBottomNav(
                    selectedTab = BottomNavTab.HOME,
                    onSelectTab = {},
                    pendingFriendRequests = 2,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("studio_bottom_nav").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_friends").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_store").assertDoesNotExist()
        composeTestRule.onNodeWithTag("nav_leaderboard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_profile").assertIsDisplayed()
        composeTestRule.onNodeWithTag("friends_nav_badge", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `store screen displays diamond packs, voice pass, and cosmetic category chips`() {
        val service = MockGameService()
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                StoreScreen(
                    wallet = WalletState(diamonds = 120),
                    userProfile = UserProfile(
                        displayName = "Aria",
                        initials = "AR",
                        gender = GenderOption.FEMALE,
                        colorOption = PlayerColorOption.TOMATO,
                    ),
                    subscription = SubscriptionState(isActive = false),
                    renewalDateLabel = "",
                    diamondPacks = service.getDiamondPacks(),
                    cosmeticCatalog = service.getCosmeticCatalog(),
                    ownedCosmetics = emptySet(),
                    purchaseHistory = emptyList(),
                    onBuyDiamondPack = {},
                    onBuyCosmetic = { CosmeticBuyResult.AlreadyOwned },
                    onEquipCosmetic = {},
                    onSubscribeVoice = {},
                    onCancelVoice = {},
                    onOpenVoiceDetails = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("store_pack_pack_small").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_pack_pack_medium").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_pack_pack_large").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_voice_subscribe_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_cosmetic_${BoxSkinId.CLASSIC_PAPER.itemId}").performScrollTo()
        composeTestRule.onNodeWithTag("store_category_box_skin").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_category_avatar_clothes").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_category_avatar_skin").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_category_letter_font").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `friends screen renders search feedback and friends list`() {
        val service = MockGameService()
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                FriendsScreen(
                    friends = service.getInitialFriends(),
                    incomingRequests = service.getInitialIncomingRequests(),
                    outgoingRequests = emptyList(),
                    searchResult = PlayerSearchResult.SelfMatch,
                    inviteBanner = null,
                    onSearchById = {},
                    onClearSearch = {},
                    onSendRequestToFound = {},
                    onAcceptRequest = {},
                    onDeclineOrCancelRequest = {},
                    onInviteFriend = {},
                    onRemoveFriend = {},
                    onBlockPlayer = {},
                    onDismissInviteBanner = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("friends_search_self").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("That's you!").assertIsDisplayed()
    }

    @Test
    fun `rooms screen switches visibility and shows error when room code is missing`() {
        var createdVisibility: RoomVisibility? = null
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                RoomsScreen(
                    publicRooms = MockGameService().getOpenRooms(),
                    roomsNotice = null,
                    onBack = {},
                    onCreateRoom = { createdVisibility = it },
                    onJoinByCode = { false },
                    onJoinPublicRoom = {},
                    onDismissNotice = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("rooms_visibility_public").performClick()
        composeTestRule.onNodeWithTag("rooms_create_button").performClick()
        assertEquals(RoomVisibility.PUBLIC, createdVisibility)

        composeTestRule.onNodeWithTag("rooms_join_code_input").performScrollTo().performTextInput("ZZZZZZ")
        composeTestRule.onNodeWithTag("rooms_join_code_button").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Room not found.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `quitting ongoing non-friend online match deducts 5 exp while friend and offline matches do not`() {
        assertEquals(20, ExpConfig.deductQuitPenalty(25))
        assertEquals(0, ExpConfig.deductQuitPenalty(3))
        assertEquals(0, ExpConfig.deductQuitPenalty(0))

        assertTrue(AppViewModel.shouldDeductQuitPenalty(isOnline = true, isFriendMatch = false))
        assertFalse(AppViewModel.shouldDeductQuitPenalty(isOnline = true, isFriendMatch = true))
        assertFalse(AppViewModel.shouldDeductQuitPenalty(isOnline = false, isFriendMatch = false))
        assertFalse(AppViewModel.shouldDeductQuitPenalty(isOnline = false, isFriendMatch = true))
    }

    @Test
    fun `leaving friend match dialog confirms no EXP penalty`() {
        val friendRoom = ActiveRoomUiState(
            isOnline = true,
            isFriendMatch = true,
            roomCode = "FRD999",
            player1 = PlayerIdentity(name = "Aria", initials = "A", color = PlayerColorOption.TOMATO),
            player2 = PlayerIdentity(name = "Leo", initials = "L", color = PlayerColorOption.SKY, playerId = "DB-FRIEND"),
            matchState = DotsAndBoxesEngine.createMatch(),
        )
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                GameRoomScreen(
                    roomState = friendRoom,
                    settings = AppSettings(),
                    subscription = SubscriptionState(),
                    friends = emptyList(),
                    onLineTapped = {},
                    onDismissPassDevice = {},
                    onNextLevel = {},
                    onPlayAgain = {},
                    onLeaveConfirmed = {},
                    onSendChatMessage = {},
                    onToggleVoiceMute = {},
                    onToggleVoiceConnection = {},
                    onOpenPaywall = {},
                    onAddOpponentFriend = {},
                    onInviteFriendInMatch = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("room_leave_button").performClick()
        composeTestRule.onNodeWithText("Your match with a friend will end with no EXP penalty. Any diamonds already collected are kept.").assertIsDisplayed()
    }

    @Test
    fun `game room displays Round 1 with 1_3 and Final Round with 3_3 above board`() {
        val baseMatch = DotsAndBoxesEngine.createMatch()
        val roomRound1 = ActiveRoomUiState(
            isOnline = false,
            roomCode = null,
            player1 = PlayerIdentity(name = "Aria", initials = "A", color = PlayerColorOption.TOMATO),
            player2 = PlayerIdentity(name = "Leo", initials = "L", color = PlayerColorOption.SKY),
            matchState = baseMatch,
        )
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                GameRoomScreen(
                    roomState = roomRound1,
                    settings = AppSettings(),
                    subscription = SubscriptionState(),
                    friends = emptyList(),
                    onLineTapped = {},
                    onDismissPassDevice = {},
                    onNextLevel = {},
                    onPlayAgain = {},
                    onLeaveConfirmed = {},
                    onSendChatMessage = {},
                    onToggleVoiceMute = {},
                    onToggleVoiceConnection = {},
                    onOpenPaywall = {},
                    onAddOpponentFriend = {},
                    onInviteFriendInMatch = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("board_round_label").assertIsDisplayed()
        composeTestRule.onNodeWithText("Round 1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("board_round_fraction").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/3").assertIsDisplayed()
    }

    @Test
    fun `final round displays Final Round and 3_3 above board`() {
        val level2Match = DotsAndBoxesEngine.advanceToNextLevel(
            DotsAndBoxesEngine.createMatch().copy(isAwaitingNextLevel = true)
        )
        val finalRoundMatch = DotsAndBoxesEngine.advanceToNextLevel(
            level2Match.copy(isAwaitingNextLevel = true)
        )
        val roomRound3 = ActiveRoomUiState(
            isOnline = false,
            roomCode = null,
            player1 = PlayerIdentity(name = "Aria", initials = "A", color = PlayerColorOption.TOMATO),
            player2 = PlayerIdentity(name = "Leo", initials = "L", color = PlayerColorOption.SKY),
            matchState = finalRoundMatch,
        )
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                GameRoomScreen(
                    roomState = roomRound3,
                    settings = AppSettings(),
                    subscription = SubscriptionState(),
                    friends = emptyList(),
                    onLineTapped = {},
                    onDismissPassDevice = {},
                    onNextLevel = {},
                    onPlayAgain = {},
                    onLeaveConfirmed = {},
                    onSendChatMessage = {},
                    onToggleVoiceMute = {},
                    onToggleVoiceConnection = {},
                    onOpenPaywall = {},
                    onAddOpponentFriend = {},
                    onInviteFriendInMatch = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Final Round").assertIsDisplayed()
        composeTestRule.onNodeWithText("3/3").assertIsDisplayed()
    }

    @Test
    fun `level transition panel shows congrats score and auto-starts next round after 3 seconds`() {
        var advanced = false
        val awaitingMatch = DotsAndBoxesEngine.createMatch().copy(
            isAwaitingNextLevel = true,
            completedLevels = listOf(
                LevelSummary(
                    levelNumber = 1,
                    dotCount = 4,
                    p1Boxes = 6,
                    p2Boxes = 3,
                    winner = PlayerSlot.PLAYER_1,
                    p1Diamonds = 1,
                    p2Diamonds = 0,
                )
            ),
        )
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                LevelTransitionPanel(
                    matchState = awaitingMatch,
                    player1 = PlayerIdentity(name = "Aria", initials = "A", color = PlayerColorOption.TOMATO),
                    player2 = PlayerIdentity(name = "Leo", initials = "L", color = PlayerColorOption.SKY),
                    reducedMotion = true,
                    onNextLevel = { advanced = true },
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("level_transition_panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("ROUND 1 COMPLETE").assertIsDisplayed()
        composeTestRule.onNodeWithTag("level_transition_countdown").performScrollTo().assertIsDisplayed()
        assertFalse(advanced)

        composeTestRule.mainClock.advanceTimeBy(3100L)
        composeTestRule.waitForIdle()
        assertTrue(advanced)
    }

    @Test
    fun `match results card displays 2x watch ad button, subtag, and no thanks text link underneath`() {
        var watchAdClicked = false
        val completedMatch = DotsAndBoxesEngine.createMatch().copy(
            isMatchComplete = true,
            completedLevels = listOf(
                LevelSummary(levelNumber = 1, dotCount = 4, p1Boxes = 6, p2Boxes = 3, winner = PlayerSlot.PLAYER_1, p1Diamonds = 2, p2Diamonds = 0),
                LevelSummary(levelNumber = 2, dotCount = 6, p1Boxes = 15, p2Boxes = 10, winner = PlayerSlot.PLAYER_1, p1Diamonds = 1, p2Diamonds = 1),
                LevelSummary(levelNumber = 3, dotCount = 8, p1Boxes = 28, p2Boxes = 21, winner = PlayerSlot.PLAYER_1, p1Diamonds = 2, p2Diamonds = 1),
            ),
        )
        val summary = MatchCompletionSummary(
            isOnline = true,
            previousExp = 20,
            newExp = 25,
            expAwarded = 5,
            previousLevel = 1,
            newLevel = 1,
            p1DiamondsEarned = 5,
            p2DiamondsEarned = 2,
            walletBeforeDiamonds = 10,
            walletAfterDiamonds = 15,
        )
        composeTestRule.setContent {
            DotsAndBoxesTheme {
                MatchResultsPanel(
                    matchState = completedMatch,
                    player1 = PlayerIdentity(name = "Aria", initials = "A", color = PlayerColorOption.TOMATO),
                    player2 = PlayerIdentity(name = "Leo", initials = "L", color = PlayerColorOption.SKY),
                    completionSummary = summary,
                    reducedMotion = true,
                    isOnline = true,
                    onWatchAdToDoubleDiamonds = { watchAdClicked = true },
                    onPlayAgain = {},
                    onHome = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("results_watch_ad_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("2x Watch Ad", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Watch ad to double your diamonds", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("results_ad_no_thanks_link").performScrollTo().assertIsDisplayed()

        val watchAdBounds = composeTestRule.onNodeWithTag("results_watch_ad_button").fetchSemanticsNode().boundsInRoot
        val noThanksBounds = composeTestRule.onNodeWithTag("results_ad_no_thanks_link").fetchSemanticsNode().boundsInRoot
        assertTrue(noThanksBounds.top >= watchAdBounds.bottom - 1f)

        composeTestRule.onNodeWithTag("results_watch_ad_button").performScrollTo().performClick()
        assertTrue(watchAdClicked)

        composeTestRule.onNodeWithTag("results_ad_no_thanks_link").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("results_watch_ad_button").assertDoesNotExist()
    }
}
