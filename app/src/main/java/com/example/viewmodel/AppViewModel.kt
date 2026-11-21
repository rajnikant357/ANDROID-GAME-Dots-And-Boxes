package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FriendDataSnapshot
import com.example.data.GameService
import com.example.data.LeaderboardScope
import com.example.data.LocalPreferencesRepository
import com.example.data.MockGameService
import com.example.data.PersistedMatchSession
import com.example.engine.DotsAndBoxesEngine
import com.example.engine.LineCoord
import com.example.engine.MatchState
import com.example.engine.PlayerSlot
import com.example.model.AppSettings
import com.example.model.ChatMessage
import com.example.model.CosmeticCategory
import com.example.model.CosmeticItem
import com.example.model.DiamondPack
import com.example.model.DiamondTransaction
import com.example.model.EquippedCosmetics
import com.example.model.ExpConfig
import com.example.model.FriendEntry
import com.example.model.FriendRequest
import com.example.model.FriendStatus
import com.example.model.GenderOption
import com.example.model.IdCodeGenerator
import com.example.model.LeaderboardEntry
import com.example.model.MockDirectoryPlayer
import com.example.model.MockOnlinePlayer
import com.example.model.MockRoom
import com.example.model.OfflineMatchConfig
import com.example.model.PlayerColorOption
import com.example.model.PlayerIdentity
import com.example.model.PlayerPattern
import com.example.model.PurchaseRecord
import com.example.model.RoomVisibility
import com.example.model.SubscriptionState
import com.example.model.TeddyAvatarConfig
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.example.model.VoiceParticipant
import com.example.model.WaitingRoomState
import com.example.model.WalletState
import com.example.model.deriveInitialsFromName
import com.example.util.TactileSoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class MatchCompletionSummary(
    val isOnline: Boolean,
    val previousExp: Int,
    val newExp: Int,
    val expAwarded: Int,
    val previousLevel: Int,
    val newLevel: Int,
    val p1DiamondsEarned: Int = 0,
    val p2DiamondsEarned: Int = 0,
    val walletBeforeDiamonds: Int = 0,
    val walletAfterDiamonds: Int = 0,
    val adDoubledDiamonds: Boolean = false,
) {
    val leveledUp: Boolean get() = newLevel > previousLevel
}

data class ActiveRoomUiState(
    val isOnline: Boolean = false,
    val isFriendMatch: Boolean = false,
    val roomCode: String? = null,
    val player1: PlayerIdentity = PlayerIdentity("Player 1", "P1", PlayerColorOption.TOMATO, PlayerPattern.STRIPES),
    val player2: PlayerIdentity = PlayerIdentity("Player 2", "P2", PlayerColorOption.SKY, PlayerPattern.DOTS),
    val matchState: MatchState = DotsAndBoxesEngine.createMatch(),
    val showPassDeviceOverlay: Boolean = false,
    val isOpponentThinking: Boolean = false,
    val justClaimedBoxes: Boolean = false,
    val diamondsCredited: Boolean = false,
    val completionSummary: MatchCompletionSummary? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val voiceParticipants: List<VoiceParticipant> = emptyList(),
    val isVoiceConnected: Boolean = true,
)

sealed interface PlayerSearchResult {
    data object Idle : PlayerSearchResult
    data object SelfMatch : PlayerSearchResult
    data object NotFound : PlayerSearchResult
    data class Found(val player: MockDirectoryPlayer) : PlayerSearchResult
}

sealed interface CosmeticBuyResult {
    data class Success(val item: CosmeticItem) : CosmeticBuyResult
    data class InsufficientDiamonds(val needed: Int) : CosmeticBuyResult
    data object AlreadyOwned : CosmeticBuyResult
}

data class InviteBannerState(
    val friendName: String,
    val statusText: String,
    val acceptedRoomReady: Boolean = false,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = LocalPreferencesRepository(application.applicationContext)
    private val gameService: GameService = MockGameService()
    private val soundPlayer = TactileSoundPlayer()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repo.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val subscription: StateFlow<SubscriptionState> = repo.subscriptionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SubscriptionState())

    val settings: StateFlow<AppSettings> = repo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val offlineConfig: StateFlow<OfflineMatchConfig> = repo.offlineConfigFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, OfflineMatchConfig())

    val savedMatchSession: StateFlow<PersistedMatchSession?> = repo.activeMatchFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val wallet: StateFlow<WalletState> = repo.walletFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, WalletState(diamonds = 0))

    val ownedCosmetics: StateFlow<Set<String>> = repo.ownedCosmeticsFlow
        .combine(MutableStateFlow(gameService.getDefaultOwnedCosmeticIds())) { stored, defaults ->
            (stored ?: emptySet()) + defaults
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            gameService.getDefaultOwnedCosmeticIds(),
        )

    val transactions: StateFlow<List<DiamondTransaction>> = repo.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val purchaseHistory: StateFlow<List<PurchaseRecord>> = repo.purchaseHistoryFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val friendSnapshot: StateFlow<FriendDataSnapshot> = repo.friendDataFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, FriendDataSnapshot())

    val friends: StateFlow<List<FriendEntry>> = friendSnapshot
        .combine(MutableStateFlow(Unit)) { snap, _ ->
            snap.friends
                .filter { it.playerId !in snap.blockedIds }
                .sortedWith(
                    compareBy<FriendEntry> { it.status.sortOrder }
                        .thenBy { it.name.lowercase() }
                )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val incomingRequests: StateFlow<List<FriendRequest>> = friendSnapshot
        .combine(MutableStateFlow(Unit)) { snap, _ ->
            snap.requests.filter { it.isIncoming && it.playerId !in snap.blockedIds }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val outgoingRequests: StateFlow<List<FriendRequest>> = friendSnapshot
        .combine(MutableStateFlow(Unit)) { snap, _ ->
            snap.requests.filter { !it.isIncoming && it.playerId !in snap.blockedIds }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val blockedIds: StateFlow<Set<String>> = friendSnapshot
        .combine(MutableStateFlow(Unit)) { snap, _ -> snap.blockedIds }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val cosmeticCatalog: List<CosmeticItem> = gameService.getCosmeticCatalog()
    val diamondPacks: List<DiamondPack> = gameService.getDiamondPacks()

    private val _activeRoom = MutableStateFlow<ActiveRoomUiState?>(null)
    val activeRoom: StateFlow<ActiveRoomUiState?> = _activeRoom.asStateFlow()

    private val _waitingRoom = MutableStateFlow<WaitingRoomState?>(null)
    val waitingRoom: StateFlow<WaitingRoomState?> = _waitingRoom.asStateFlow()

    private val _roomsNotice = MutableStateFlow<String?>(null)
    val roomsNotice: StateFlow<String?> = _roomsNotice.asStateFlow()

    private val _leaderboardScope = MutableStateFlow(LeaderboardScope.ALL_TIME)
    val leaderboardScope: StateFlow<LeaderboardScope> = _leaderboardScope.asStateFlow()

    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> =
        combine(_leaderboardScope, userProfile) { scope, profile ->
            gameService.getLeaderboard(scope, profile)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            gameService.getLeaderboard(LeaderboardScope.ALL_TIME, null),
        )

    private val _openRooms = MutableStateFlow(gameService.getOpenRooms())
    val openRooms: StateFlow<List<MockRoom>> = _openRooms.asStateFlow()

    private val _onlinePlayers = MutableStateFlow(gameService.getOnlinePlayers())
    val onlinePlayers: StateFlow<List<MockOnlinePlayer>> = _onlinePlayers.asStateFlow()

    private val _isSearchingQuickMatch = MutableStateFlow(false)
    val isSearchingQuickMatch: StateFlow<Boolean> = _isSearchingQuickMatch.asStateFlow()

    private val _searchResult = MutableStateFlow<PlayerSearchResult>(PlayerSearchResult.Idle)
    val searchResult: StateFlow<PlayerSearchResult> = _searchResult.asStateFlow()

    private val _inviteBanner = MutableStateFlow<InviteBannerState?>(null)
    val inviteBanner: StateFlow<InviteBannerState?> = _inviteBanner.asStateFlow()

    private val _navigateToRoomSignal = MutableStateFlow(0)
    val navigateToRoomSignal: StateFlow<Int> = _navigateToRoomSignal.asStateFlow()

    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    private var botTurnJob: Job? = null
    private var voiceTickerJob: Job? = null
    private var chatReplyJob: Job? = null
    private var quickMatchJob: Job? = null
    private var inviteJob: Job? = null
    private var waitingGuestJob: Job? = null
    private var presenceTickerJob: Job? = null

    init {
        viewModelScope.launch {
            repo.userProfileFlow.first()
            repo.subscriptionFlow.first()
            repo.settingsFlow.first()
            repo.activeMatchFlow.first()
            repo.walletFlow.first()
            val storedOwned = repo.ownedCosmeticsFlow.first()
            if (storedOwned == null) {
                repo.saveOwnedCosmetics(gameService.getDefaultOwnedCosmeticIds())
            }
            val friendsSnap = repo.friendDataFlow.first()
            if (!friendsSnap.initialized) {
                repo.saveFriendData(
                    FriendDataSnapshot(
                        initialized = true,
                        friends = gameService.getInitialFriends(),
                        requests = gameService.getInitialIncomingRequests(),
                        blockedIds = emptySet(),
                    )
                )
            }
            _isLoaded.value = true
            startFriendPresenceTicker()
        }
    }

    fun clearUiNotice() {
        _uiNotice.value = null
    }

    fun clearRoomsNotice() {
        _roomsNotice.value = null
    }

    fun saveProfile(
        displayName: String,
        initials: String,
        gender: GenderOption,
        colorOption: PlayerColorOption,
        pattern: PlayerPattern,
        avatarConfig: TeddyAvatarConfig,
    ) {
        viewModelScope.launch {
            val existing = userProfile.value
            val trimmedName = displayName.trim()
            val autoInitial = deriveInitialsFromName(trimmedName)
                .ifEmpty { initials.trim().take(1).uppercase() }
                .ifEmpty { "P" }
            val updated = if (existing == null) {
                UserProfile(
                    playerId = IdCodeGenerator.generatePlayerId(),
                    displayName = trimmedName,
                    initials = autoInitial,
                    gender = gender,
                    colorOption = colorOption,
                    pattern = pattern,
                    avatarConfig = avatarConfig,
                )
            } else {
                existing.copy(
                    displayName = trimmedName,
                    initials = autoInitial,
                    gender = gender,
                    colorOption = colorOption,
                    pattern = pattern,
                    avatarConfig = avatarConfig,
                )
            }
            repo.saveProfile(updated)
        }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            repo.saveSettings(transform(settings.value))
        }
    }

    fun setLeaderboardScope(scope: LeaderboardScope) {
        _leaderboardScope.value = scope
    }

    fun formatRenewalDate(epochMillis: Long?): String {
        if (epochMillis == null) return ""
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date(epochMillis))
    }

    fun subscribeMonthly() {
        viewModelScope.launch {
            val thirtyDaysMs = 30L * 24L * 60L * 60L * 1000L
            val now = System.currentTimeMillis()
            repo.saveSubscription(
                SubscriptionState(
                    isActive = true,
                    renewalDateEpochMillis = now + thirtyDaysMs,
                )
            )
            val updatedHistory = listOf(
                PurchaseRecord(
                    id = "pur_${now}",
                    productId = "sub_voice_monthly",
                    title = "Voice Pass (Monthly)",
                    priceLabel = "₹9 / month",
                    verifiedAtMillis = now,
                )
            ) + purchaseHistory.value
            repo.savePurchaseHistory(updatedHistory)
            _uiNotice.value = "Voice Pass activated (prototype)."
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            repo.saveSubscription(SubscriptionState(isActive = false, renewalDateEpochMillis = null))
            _uiNotice.value = "Voice Pass cancelled."
        }
    }

    // --- Wallet & Store ---

    fun buyDiamondPack(pack: DiamondPack) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = wallet.value
            val newBalance = (current.diamonds + pack.diamonds).coerceAtLeast(0)
            repo.saveWallet(WalletState(diamonds = newBalance, updatedAtMillis = now))
            val tx = DiamondTransaction(
                id = "tx_${now}",
                type = TransactionType.PURCHASE,
                amount = pack.diamonds,
                reference = "${pack.name} (+${pack.diamonds})",
                createdAtMillis = now,
            )
            repo.saveTransactions(listOf(tx) + transactions.value)
            val pur = PurchaseRecord(
                id = "pur_${now}",
                productId = pack.id,
                title = "${pack.name} · ${pack.diamonds} Diamonds",
                priceLabel = "₹${pack.priceInr}",
                verifiedAtMillis = now,
            )
            repo.savePurchaseHistory(listOf(pur) + purchaseHistory.value)
            _uiNotice.value = "+${pack.diamonds} diamonds added to wallet."
        }
    }

    fun buyCosmetic(item: CosmeticItem): CosmeticBuyResult {
        val owned = ownedCosmetics.value
        if (item.id in owned || item.isDefault) {
            return CosmeticBuyResult.AlreadyOwned
        }
        val currentDiamonds = wallet.value.diamonds
        if (currentDiamonds < item.priceDiamonds) {
            return CosmeticBuyResult.InsufficientDiamonds(item.priceDiamonds - currentDiamonds)
        }
        val newBalance = (currentDiamonds - item.priceDiamonds).coerceAtLeast(0)
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repo.saveWallet(WalletState(diamonds = newBalance, updatedAtMillis = now))
            repo.saveOwnedCosmetics(owned + item.id)
            val tx = DiamondTransaction(
                id = "tx_${now}",
                type = TransactionType.SPEND,
                amount = -item.priceDiamonds,
                reference = item.name,
                createdAtMillis = now,
            )
            repo.saveTransactions(listOf(tx) + transactions.value)
        }
        equipCosmetic(item)
        _uiNotice.value = "${item.name} unlocked and equipped!"
        return CosmeticBuyResult.Success(item)
    }

    fun equipCosmetic(item: CosmeticItem) {
        val profile = userProfile.value ?: return
        if (item.id !in ownedCosmetics.value && !item.isDefault) return
        val current = profile.equippedCosmetics
        val updatedEquipped = when (item.category) {
            CosmeticCategory.BOX_SKIN -> current.copy(boxSkin = item.boxSkinId ?: current.boxSkin)
            CosmeticCategory.AVATAR_CLOTHES -> current.copy(avatarClothing = item.clothingId ?: current.avatarClothing)
            CosmeticCategory.AVATAR_SKIN -> current.copy(avatarSkin = item.avatarSkinId ?: current.avatarSkin)
            CosmeticCategory.LETTER_FONT -> current.copy(letterFont = item.letterFontId ?: current.letterFont)
        }
        viewModelScope.launch {
            repo.saveProfile(profile.copy(equippedCosmetics = updatedEquipped))
        }
    }

    private suspend fun creditDiamonds(amount: Int, reference: String): Pair<Int, Int> {
        val before = wallet.value.diamonds.coerceAtLeast(0)
        if (amount <= 0) return before to before
        val after = (before + amount).coerceAtLeast(0)
        val now = System.currentTimeMillis()
        repo.saveWallet(WalletState(diamonds = after, updatedAtMillis = now))
        val tx = DiamondTransaction(
            id = "tx_${now}_${Random.nextInt(100, 999)}",
            type = TransactionType.EARN,
            amount = amount,
            reference = reference,
            createdAtMillis = now,
        )
        repo.saveTransactions(listOf(tx) + transactions.value)
        return before to after
    }

    // --- Friends & Social ---

    fun searchPlayerById(rawQuery: String) {
        val cleaned = rawQuery.trim().uppercase()
        if (cleaned.isBlank()) {
            _searchResult.value = PlayerSearchResult.Idle
            return
        }
        val normalized = if (cleaned.startsWith("DB-")) cleaned else "DB-$cleaned"
        val localId = userProfile.value?.playerId
        if (localId != null && normalized.equals(localId, ignoreCase = true)) {
            _searchResult.value = PlayerSearchResult.SelfMatch
            return
        }
        val match = gameService.findPlayerById(normalized)
        if (match == null || match.playerId in blockedIds.value) {
            _searchResult.value = PlayerSearchResult.NotFound
        } else {
            _searchResult.value = PlayerSearchResult.Found(match)
        }
    }

    fun clearPlayerSearch() {
        _searchResult.value = PlayerSearchResult.Idle
    }

    fun sendFriendRequest(
        playerId: String,
        name: String,
        initials: String,
        gender: GenderOption,
        color: PlayerColorOption,
        pattern: PlayerPattern,
        avatarConfig: TeddyAvatarConfig,
        equippedCosmetics: EquippedCosmetics,
        level: Int,
    ): Boolean {
        val snap = friendSnapshot.value
        if (playerId == userProfile.value?.playerId) return false
        if (playerId in snap.blockedIds) return false
        if (snap.friends.any { it.playerId == playerId }) {
            _uiNotice.value = "$name is already your friend."
            return false
        }
        if (snap.friends.size >= IdCodeGenerator.MAX_FRIENDS_LIMIT) {
            _uiNotice.value = "Friend limit (${IdCodeGenerator.MAX_FRIENDS_LIMIT}) reached."
            return false
        }
        if (snap.requests.any { it.playerId == playerId }) {
            _uiNotice.value = "Request already pending with $name."
            return false
        }
        val req = FriendRequest(
            id = "req_out_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            playerId = playerId,
            name = name,
            initials = initials,
            gender = gender,
            color = color,
            pattern = pattern,
            avatarConfig = avatarConfig,
            equippedCosmetics = equippedCosmetics,
            level = level,
            isIncoming = false,
        )
        viewModelScope.launch {
            repo.saveFriendData(snap.copy(initialized = true, requests = listOf(req) + snap.requests))
        }
        _uiNotice.value = "Friend request sent to $name."
        return true
    }

    fun sendFriendRequest(player: MockDirectoryPlayer) {
        sendFriendRequest(
            playerId = player.playerId,
            name = player.name,
            initials = player.initials,
            gender = player.gender,
            color = player.color,
            pattern = player.pattern,
            avatarConfig = player.avatarConfig,
            equippedCosmetics = player.equippedCosmetics,
            level = player.level,
        )
    }

    fun sendFriendRequest(entry: LeaderboardEntry) {
        sendFriendRequest(
            playerId = entry.playerId,
            name = entry.name,
            initials = entry.initials,
            gender = entry.gender,
            color = entry.color,
            pattern = entry.pattern,
            avatarConfig = entry.avatarConfig,
            equippedCosmetics = entry.equippedCosmetics,
            level = entry.level,
        )
    }

    fun sendFriendRequestToIdentity(identity: PlayerIdentity) {
        val pid = identity.playerId ?: IdCodeGenerator.generatePlayerId()
        sendFriendRequest(
            playerId = pid,
            name = identity.name,
            initials = identity.initials,
            gender = identity.gender ?: GenderOption.PREFER_NOT_TO_SAY,
            color = identity.color,
            pattern = identity.pattern,
            avatarConfig = identity.avatarConfig,
            equippedCosmetics = identity.equippedCosmetics,
            level = identity.level ?: 1,
        )
    }

    fun acceptFriendRequest(requestId: String) {
        val snap = friendSnapshot.value
        val req = snap.requests.firstOrNull { it.id == requestId } ?: return
        if (snap.friends.size >= IdCodeGenerator.MAX_FRIENDS_LIMIT) {
            _uiNotice.value = "Friend limit (${IdCodeGenerator.MAX_FRIENDS_LIMIT}) reached."
            return
        }
        val newFriend = FriendEntry(
            playerId = req.playerId,
            name = req.name,
            initials = req.initials,
            gender = req.gender,
            color = req.color,
            pattern = req.pattern,
            avatarConfig = req.avatarConfig,
            equippedCosmetics = req.equippedCosmetics,
            level = req.level,
            status = FriendStatus.ONLINE,
            lastSeenLabel = "Just now",
        )
        val remaining = snap.requests.filterNot { it.id == requestId }
        val updatedFriends = (snap.friends.filterNot { it.playerId == req.playerId } + newFriend)
        viewModelScope.launch {
            repo.saveFriendData(snap.copy(initialized = true, friends = updatedFriends, requests = remaining))
        }
        _uiNotice.value = "${req.name} added to Friends!"
    }

    fun declineOrCancelRequest(requestId: String) {
        val snap = friendSnapshot.value
        viewModelScope.launch {
            repo.saveFriendData(
                snap.copy(
                    initialized = true,
                    requests = snap.requests.filterNot { it.id == requestId },
                )
            )
        }
    }

    fun removeFriend(playerId: String) {
        val snap = friendSnapshot.value
        viewModelScope.launch {
            repo.saveFriendData(
                snap.copy(
                    initialized = true,
                    friends = snap.friends.filterNot { it.playerId == playerId },
                )
            )
        }
        _uiNotice.value = "Friend removed."
    }

    fun blockPlayer(playerId: String) {
        val snap = friendSnapshot.value
        viewModelScope.launch {
            repo.saveFriendData(
                snap.copy(
                    initialized = true,
                    friends = snap.friends.filterNot { it.playerId == playerId },
                    requests = snap.requests.filterNot { it.playerId == playerId },
                    blockedIds = snap.blockedIds + playerId,
                )
            )
        }
        _uiNotice.value = "Player blocked."
    }

    fun inviteFriendToNewMatch(friend: FriendEntry) {
        if (friend.status != FriendStatus.ONLINE) return
        inviteJob?.cancel()
        _inviteBanner.value = InviteBannerState(
            friendName = friend.name,
            statusText = "Invite sent to ${friend.name}...",
            acceptedRoomReady = false,
        )
        inviteJob = viewModelScope.launch {
            delay(2000L)
            _inviteBanner.value = InviteBannerState(
                friendName = friend.name,
                statusText = "${friend.name} accepted your invite!",
                acceptedRoomReady = true,
            )
            startOnlineMatchWithFriend(friend, gameService.generateRoomCode())
            _navigateToRoomSignal.update { it + 1 }
            delay(2200L)
            _inviteBanner.value = null
        }
    }

    fun inviteFriendDuringActiveMatch(friend: FriendEntry) {
        _uiNotice.value = "Match invite sent to ${friend.name}."
    }

    fun dismissInviteBanner() {
        inviteJob?.cancel()
        _inviteBanner.value = null
    }

    // --- Lobby Quick Match ---

    fun startQuickMatchSearch() {
        quickMatchJob?.cancel()
        _isSearchingQuickMatch.value = true
        quickMatchJob = viewModelScope.launch {
            delay(2200L)
            if (_isSearchingQuickMatch.value) {
                _isSearchingQuickMatch.value = false
                startQuickOnlineMatch()
                _navigateToRoomSignal.update { it + 1 }
            }
        }
    }

    fun cancelQuickMatchSearch() {
        quickMatchJob?.cancel()
        quickMatchJob = null
        _isSearchingQuickMatch.value = false
    }

    // --- Rooms & Room Waiting ---

    fun createWaitingRoom(visibility: RoomVisibility) {
        waitingGuestJob?.cancel()
        val code = gameService.generateRoomCode()
        val profile = userProfile.value
        val hostName = profile?.displayName ?: "You"
        val host = PlayerIdentity(
            name = hostName,
            initials = deriveInitialsFromName(hostName).ifEmpty { "Y" },
            color = profile?.colorOption ?: PlayerColorOption.TOMATO,
            pattern = profile?.pattern ?: PlayerPattern.STRIPES,
            avatarConfig = profile?.avatarConfig ?: TeddyAvatarConfig(),
            equippedCosmetics = profile?.equippedCosmetics ?: EquippedCosmetics(),
            playerId = profile?.playerId,
            level = profile?.level ?: 1,
            gender = profile?.gender,
        )
        _waitingRoom.value = WaitingRoomState(
            code = code,
            visibility = visibility,
            hostPlayer = host,
            guestPlayer = null,
            isLocalHost = true,
        )
        _roomsNotice.value = null
    }

    fun joinWaitingRoomFromPublicList(room: MockRoom) {
        waitingGuestJob?.cancel()
        val profile = userProfile.value
        val localColor = profile?.colorOption ?: PlayerColorOption.TOMATO
        val resolvedGuestColor = if (localColor == room.hostColor) {
            PlayerColorOption.entries.first { it != room.hostColor }
        } else {
            localColor
        }
        val host = PlayerIdentity(
            name = room.hostName,
            initials = deriveInitialsFromName(room.hostName).ifEmpty { room.hostInitials.take(1).uppercase() },
            color = room.hostColor,
            pattern = room.hostPattern,
            avatarConfig = room.hostAvatar,
            equippedCosmetics = room.hostCosmetics,
            playerId = room.hostPlayerId,
            level = room.hostLevel,
            gender = room.hostGender,
        )
        val guestName = profile?.displayName ?: "You"
        val guest = PlayerIdentity(
            name = guestName,
            initials = deriveInitialsFromName(guestName).ifEmpty { "Y" },
            color = resolvedGuestColor,
            pattern = profile?.pattern ?: PlayerPattern.STRIPES,
            avatarConfig = profile?.avatarConfig ?: TeddyAvatarConfig(),
            equippedCosmetics = profile?.equippedCosmetics ?: EquippedCosmetics(),
            playerId = profile?.playerId,
            level = profile?.level ?: 1,
            gender = profile?.gender,
        )
        _waitingRoom.value = WaitingRoomState(
            code = room.code,
            visibility = room.visibility,
            hostPlayer = host,
            guestPlayer = guest,
            isLocalHost = false,
        )
        _roomsNotice.value = null
    }

    fun joinWaitingRoomByCode(rawCode: String): Boolean {
        val code = rawCode.trim().uppercase()
        if (code.isBlank()) return false
        val mockMatch = gameService.findRoomByCode(code)
        if (mockMatch != null) {
            joinWaitingRoomFromPublicList(mockMatch)
            return true
        }
        // Also allow joining via any valid 6-char mock code format if it matches a seeded friend code or current room
        val currentWaiting = _waitingRoom.value
        if (currentWaiting != null && currentWaiting.code.equals(code, ignoreCase = true)) {
            return true
        }
        return false
    }

    fun inviteFriendToWaitingRoom(friend: FriendEntry) {
        val waiting = _waitingRoom.value ?: return
        waitingGuestJob?.cancel()
        val resolvedColor = if (friend.color == waiting.hostPlayer.color) {
            PlayerColorOption.entries.first { it != waiting.hostPlayer.color }
        } else {
            friend.color
        }
        _waitingRoom.value = waiting.copy(isSimulatingGuestJoin = true)
        _uiNotice.value = if (friend.status == FriendStatus.ONLINE) {
            "Invite sent to ${friend.name}..."
        } else {
            "Room invite sent to ${friend.name} for when they're online."
        }
        waitingGuestJob = viewModelScope.launch {
            delay(1500L)
            _waitingRoom.update { cur ->
                if (cur == null) null
                else {
                    cur.copy(
                        isSimulatingGuestJoin = false,
                        guestPlayer = PlayerIdentity(
                            name = friend.name,
                            initials = deriveInitialsFromName(friend.name).ifEmpty { friend.initials.take(1).uppercase() },
                            color = resolvedColor,
                            pattern = friend.pattern,
                            avatarConfig = friend.avatarConfig,
                            equippedCosmetics = friend.equippedCosmetics,
                            playerId = friend.playerId,
                            level = friend.level,
                            gender = friend.gender,
                        )
                    )
                }
            }
            _uiNotice.value = "${friend.name} joined room ${waiting.code}!"
        }
    }

    fun simulateGuestJoinWaitingRoom() {
        val waiting = _waitingRoom.value ?: return
        if (waiting.guestPlayer != null) return
        val opp = gameService.pickQuickMatchOpponent(excludeColor = waiting.hostPlayer.color)
        _waitingRoom.value = waiting.copy(
            guestPlayer = PlayerIdentity(
                name = opp.hostName,
                initials = deriveInitialsFromName(opp.hostName).ifEmpty { opp.hostInitials.take(1).uppercase() },
                color = opp.hostColor,
                pattern = opp.hostPattern,
                avatarConfig = opp.hostAvatar,
                equippedCosmetics = opp.hostCosmetics,
                playerId = opp.hostPlayerId,
                level = opp.hostLevel,
                gender = opp.hostGender,
            ),
            isSimulatingGuestJoin = false,
        )
    }

    fun startMatchFromWaitingRoom(): Boolean {
        val waiting = _waitingRoom.value ?: return false
        val guest = waiting.guestPlayer ?: return false
        val opponent = if (waiting.isLocalHost) guest else waiting.hostPlayer
        waitingGuestJob?.cancel()
        _waitingRoom.value = null
        val isOpponentFriend = opponent.playerId != null && friends.value.any { it.playerId == opponent.playerId }
        startOnlineMatchInternal(
            roomCode = waiting.code,
            opponentId = opponent.playerId,
            opponentName = opponent.name,
            opponentInitials = opponent.initials,
            opponentColor = opponent.color,
            opponentPattern = opponent.pattern,
            opponentAvatar = opponent.avatarConfig,
            opponentCosmetics = opponent.equippedCosmetics,
            opponentLevel = opponent.level ?: 1,
            opponentGender = opponent.gender ?: GenderOption.PREFER_NOT_TO_SAY,
            isFriendMatch = isOpponentFriend,
        )
        return true
    }

    fun leaveWaitingRoom(hostClosedSimulation: Boolean = false) {
        waitingGuestJob?.cancel()
        val wasHost = _waitingRoom.value?.isLocalHost ?: true
        _waitingRoom.value = null
        if (hostClosedSimulation || !wasHost) {
            _roomsNotice.value = if (hostClosedSimulation) {
                "Room closed by host."
            } else {
                "Left waiting room."
            }
        } else {
            _roomsNotice.value = "Room closed."
        }
    }

    // --- Match Creation & Gameplay ---

    fun startOfflineMatch(config: OfflineMatchConfig) {
        cancelRoomCoroutines()
        viewModelScope.launch {
            repo.saveOfflineConfig(config)
        }
        val profile = userProfile.value
        val match = DotsAndBoxesEngine.createMatch(
            startingPlayer = PlayerSlot.PLAYER_1,
            alternateStarter = config.alternateStarter,
        )
        val p1Name = config.p1Name.ifBlank { "Player 1" }
        val p1 = PlayerIdentity(
            name = p1Name,
            initials = deriveInitialsFromName(p1Name).ifEmpty { "P" },
            color = config.p1Color,
            pattern = config.p1Pattern,
            avatarConfig = config.p1Avatar,
            equippedCosmetics = profile?.equippedCosmetics ?: EquippedCosmetics(),
            playerId = profile?.playerId,
        )
        val p2Name = config.p2Name.ifBlank { "Player 2" }
        val p2 = PlayerIdentity(
            name = p2Name,
            initials = deriveInitialsFromName(p2Name).ifEmpty { "P" },
            color = config.p2Color,
            pattern = config.p2Pattern,
            avatarConfig = config.p2Avatar,
            equippedCosmetics = EquippedCosmetics(),
        )
        val room = ActiveRoomUiState(
            isOnline = false,
            roomCode = null,
            player1 = p1,
            player2 = p2,
            matchState = match,
            showPassDeviceOverlay = false,
        )
        _activeRoom.value = room
        persistRoomSession(room)
    }

    fun startOnlineMatchFromRoom(room: MockRoom) {
        startOnlineMatchInternal(
            roomCode = room.code,
            opponentId = room.hostPlayerId,
            opponentName = room.hostName,
            opponentInitials = room.hostInitials,
            opponentColor = room.hostColor,
            opponentPattern = room.hostPattern,
            opponentAvatar = room.hostAvatar,
            opponentCosmetics = room.hostCosmetics,
            opponentLevel = room.hostLevel,
            opponentGender = room.hostGender,
        )
    }

    fun startQuickOnlineMatch() {
        val localColor = userProfile.value?.colorOption ?: PlayerColorOption.TOMATO
        val room = gameService.pickQuickMatchOpponent(excludeColor = localColor)
        startOnlineMatchFromRoom(room)
    }

    private fun startOnlineMatchWithFriend(friend: FriendEntry, roomCode: String) {
        startOnlineMatchInternal(
            roomCode = roomCode,
            opponentId = friend.playerId,
            opponentName = friend.name,
            opponentInitials = friend.initials,
            opponentColor = friend.color,
            opponentPattern = friend.pattern,
            opponentAvatar = friend.avatarConfig,
            opponentCosmetics = friend.equippedCosmetics,
            opponentLevel = friend.level,
            opponentGender = friend.gender,
            isFriendMatch = true,
        )
    }

    private fun startOnlineMatchInternal(
        roomCode: String,
        opponentId: String?,
        opponentName: String,
        opponentInitials: String,
        opponentColor: PlayerColorOption,
        opponentPattern: PlayerPattern,
        opponentAvatar: TeddyAvatarConfig,
        opponentCosmetics: EquippedCosmetics,
        opponentLevel: Int,
        opponentGender: GenderOption,
        isFriendMatch: Boolean = false,
    ) {
        cancelRoomCoroutines()
        val profile = userProfile.value
        val localColor = profile?.colorOption ?: PlayerColorOption.TOMATO
        val localPattern = profile?.pattern ?: PlayerPattern.STRIPES
        val resolvedOppColor = if (opponentColor == localColor) {
            PlayerColorOption.entries.first { it != localColor }
        } else {
            opponentColor
        }
        val resolvedOppPattern = if (opponentPattern == localPattern) {
            PlayerPattern.entries.first { it != localPattern }
        } else {
            opponentPattern
        }

        val localName = profile?.displayName ?: "You"
        val p1 = PlayerIdentity(
            name = localName,
            initials = deriveInitialsFromName(localName).ifEmpty { "Y" },
            color = localColor,
            pattern = localPattern,
            avatarConfig = profile?.avatarConfig ?: TeddyAvatarConfig(),
            equippedCosmetics = profile?.equippedCosmetics ?: EquippedCosmetics(),
            playerId = profile?.playerId,
            level = profile?.level ?: 1,
            gender = profile?.gender,
        )
        val p2 = PlayerIdentity(
            name = opponentName,
            initials = deriveInitialsFromName(opponentName).ifEmpty { opponentInitials.take(1).uppercase() },
            color = resolvedOppColor,
            pattern = resolvedOppPattern,
            avatarConfig = opponentAvatar,
            equippedCosmetics = opponentCosmetics,
            playerId = opponentId,
            level = opponentLevel,
            gender = opponentGender,
        )

        val match = DotsAndBoxesEngine.createMatch(
            startingPlayer = PlayerSlot.PLAYER_1,
            alternateStarter = true,
        )

        val initialGreeting = ChatMessage(
            id = "msg_welcome_${System.currentTimeMillis()}",
            senderName = p2.name,
            senderInitials = p2.initials,
            senderColor = p2.color,
            senderAvatar = p2.avatarConfig,
            senderCosmetics = p2.equippedCosmetics,
            text = "Room $roomCode ready. Good luck!",
            timestamp = currentTimeLabel(),
            isLocalUser = false,
        )

        val resolvedFriendMatch = isFriendMatch ||
            (opponentId != null && friends.value.any { it.playerId == opponentId })

        val room = ActiveRoomUiState(
            isOnline = true,
            isFriendMatch = resolvedFriendMatch,
            roomCode = roomCode,
            player1 = p1,
            player2 = p2,
            matchState = match,
            chatMessages = listOf(initialGreeting),
            voiceParticipants = gameService.createInitialVoiceParticipants(
                opponentName = p2.name,
                opponentInitials = p2.initials,
                opponentColor = p2.color,
                localUser = profile,
            ),
            isVoiceConnected = true,
        )
        _activeRoom.value = room
        persistRoomSession(room)
        startVoiceSimulation()
    }

    fun resumeSavedMatch(): Boolean {
        val saved = savedMatchSession.value ?: return false
        cancelRoomCoroutines()
        val profile = userProfile.value
        val chat = if (saved.isOnline) {
            listOf(
                ChatMessage(
                    id = "msg_resumed_${System.currentTimeMillis()}",
                    senderName = saved.player2.name,
                    senderInitials = saved.player2.initials,
                    senderColor = saved.player2.color,
                    senderAvatar = saved.player2.avatarConfig,
                    senderCosmetics = saved.player2.equippedCosmetics,
                    text = "Match restored.",
                    timestamp = currentTimeLabel(),
                    isLocalUser = false,
                )
            )
        } else {
            emptyList()
        }
        val voice = if (saved.isOnline) {
            gameService.createInitialVoiceParticipants(
                opponentName = saved.player2.name,
                opponentInitials = saved.player2.initials,
                opponentColor = saved.player2.color,
                localUser = profile,
            )
        } else {
            emptyList()
        }

        val room = ActiveRoomUiState(
            isOnline = saved.isOnline,
            isFriendMatch = saved.isFriendMatch,
            roomCode = saved.roomCode,
            player1 = saved.player1,
            player2 = saved.player2,
            matchState = saved.matchState,
            chatMessages = chat,
            voiceParticipants = voice,
            isVoiceConnected = saved.isOnline,
        )
        _activeRoom.value = room

        if (saved.isOnline) {
            startVoiceSimulation()
            if (!saved.matchState.isMatchComplete &&
                !saved.matchState.isAwaitingNextLevel &&
                saved.matchState.currentLevel.activePlayer == PlayerSlot.PLAYER_2
            ) {
                scheduleBotTurn()
            }
        }
        return true
    }

    fun onLineTapped(line: LineCoord) {
        val room = _activeRoom.value ?: return
        if (room.showPassDeviceOverlay) return
        if (room.matchState.isAwaitingNextLevel || room.matchState.isMatchComplete) return
        if (room.isOnline && room.matchState.currentLevel.activePlayer != PlayerSlot.PLAYER_1) return

        applyMove(line)
    }

    private fun applyMove(line: LineCoord) {
        val room = _activeRoom.value ?: return
        val outcome = DotsAndBoxesEngine.drawLine(room.matchState, line) ?: return
        val nextMatch = outcome.nextMatch
        val claimed = outcome.drawResult.claimedBoxes.isNotEmpty()

        if (settings.value.soundEnabled) {
            when {
                nextMatch.isMatchComplete -> soundPlayer.playMatchWin()
                outcome.drawResult.levelCompleted -> soundPlayer.playLevelComplete()
                claimed -> soundPlayer.playBoxClaim()
                else -> {
                    soundPlayer.playLineDraw()
                    soundPlayer.playTurnChange()
                }
            }
        }

        val shouldShowPassPrompt = !room.isOnline &&
            settings.value.passDevicePrompt &&
            !outcome.drawResult.extraTurnGranted &&
            !outcome.drawResult.levelCompleted &&
            !nextMatch.isMatchComplete

        val updatedRoom = room.copy(
            matchState = nextMatch,
            showPassDeviceOverlay = shouldShowPassPrompt,
            justClaimedBoxes = claimed,
            isOpponentThinking = room.isOnline &&
                !nextMatch.isAwaitingNextLevel &&
                !nextMatch.isMatchComplete &&
                nextMatch.currentLevel.activePlayer == PlayerSlot.PLAYER_2,
        )
        _activeRoom.value = updatedRoom

        if (nextMatch.isMatchComplete) {
            finalizeCompletedMatch(updatedRoom)
        } else {
            persistRoomSession(updatedRoom)
            if (updatedRoom.isOnline &&
                !nextMatch.isAwaitingNextLevel &&
                nextMatch.currentLevel.activePlayer == PlayerSlot.PLAYER_2
            ) {
                scheduleBotTurn()
            }
        }
    }

    fun dismissPassDeviceOverlay() {
        _activeRoom.update { it?.copy(showPassDeviceOverlay = false) }
    }

    fun proceedToNextLevel() {
        val room = _activeRoom.value ?: return
        if (!room.matchState.isAwaitingNextLevel) return
        val nextMatch = DotsAndBoxesEngine.advanceToNextLevel(room.matchState)
        val botStarts = room.isOnline && nextMatch.currentLevel.activePlayer == PlayerSlot.PLAYER_2
        val updated = room.copy(
            matchState = nextMatch,
            isOpponentThinking = botStarts,
            justClaimedBoxes = false,
            showPassDeviceOverlay = false,
        )
        _activeRoom.value = updated
        persistRoomSession(updated)
        if (botStarts) {
            scheduleBotTurn()
        }
    }

    fun playAgainInCurrentMode() {
        val room = _activeRoom.value ?: return
        if (room.isOnline) {
            startOnlineMatchInternal(
                roomCode = gameService.generateRoomCode(),
                opponentId = room.player2.playerId,
                opponentName = room.player2.name,
                opponentInitials = room.player2.initials,
                opponentColor = room.player2.color,
                opponentPattern = room.player2.pattern,
                opponentAvatar = room.player2.avatarConfig,
                opponentCosmetics = room.player2.equippedCosmetics,
                opponentLevel = room.player2.level ?: 5,
                opponentGender = room.player2.gender ?: GenderOption.PREFER_NOT_TO_SAY,
                isFriendMatch = room.isFriendMatch,
            )
        } else {
            startOfflineMatch(offlineConfig.value)
        }
    }

    fun leaveMatchConfirmed() {
        val room = _activeRoom.value
        cancelRoomCoroutines()
        if (room != null && !room.matchState.isMatchComplete) {
            val collectedByLocal = room.matchState.totalP1Diamonds
            val hadProfile = userProfile.value != null
            val isFriendOpponent = room.isFriendMatch ||
                (room.player2.playerId?.let { id -> friends.value.any { it.playerId == id } } ?: false)
            val applyPenalty = shouldDeductQuitPenalty(isOnline = room.isOnline, isFriendMatch = isFriendOpponent)
            viewModelScope.launch {
                if (!room.diamondsCredited && collectedByLocal > 0) {
                    creditDiamonds(collectedByLocal, if (room.isOnline) "Online match (forfeit)" else "Offline match (forfeit)")
                }
                if (applyPenalty) {
                    applyQuitMatchExpPenalty()
                }
            }
            if (applyPenalty && hadProfile) {
                _uiNotice.value = "Left ongoing match (-${ExpConfig.EXP_PENALTY_QUIT_MATCH} EXP)."
            }
        }
        _activeRoom.value = null
        viewModelScope.launch {
            repo.saveActiveMatch(null)
        }
    }

    fun sendChatMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank()) return
        val room = _activeRoom.value ?: return
        if (!room.isOnline) return

        if (settings.value.soundEnabled) {
            soundPlayer.playChatMessage()
        }

        val userMsg = ChatMessage(
            id = "msg_u_${System.currentTimeMillis()}",
            senderName = room.player1.name,
            senderInitials = room.player1.initials,
            senderColor = room.player1.color,
            senderAvatar = room.player1.avatarConfig,
            senderCosmetics = room.player1.equippedCosmetics,
            text = text,
            timestamp = currentTimeLabel(),
            isLocalUser = true,
        )
        _activeRoom.update { current ->
            current?.copy(chatMessages = current.chatMessages + userMsg)
        }

        chatReplyJob?.cancel()
        chatReplyJob = viewModelScope.launch {
            val delayMs = Random.nextLong(1000L, 2800L)
            delay(delayMs)
            val reply = gameService.pickRandomChatReply()
            if (settings.value.soundEnabled) {
                soundPlayer.playChatMessage()
            }
            _activeRoom.update { current ->
                if (current == null || !current.isOnline) current
                else {
                    val replyMsg = ChatMessage(
                        id = "msg_b_${System.currentTimeMillis()}",
                        senderName = current.player2.name,
                        senderInitials = current.player2.initials,
                        senderColor = current.player2.color,
                        senderAvatar = current.player2.avatarConfig,
                        senderCosmetics = current.player2.equippedCosmetics,
                        text = reply,
                        timestamp = currentTimeLabel(),
                        isLocalUser = false,
                    )
                    current.copy(chatMessages = current.chatMessages + replyMsg)
                }
            }
        }
    }

    fun toggleLocalVoiceMute() {
        _activeRoom.update { current ->
            if (current == null) null
            else {
                val updated = current.voiceParticipants.map { participant ->
                    if (participant.isLocalUser) {
                        participant.copy(
                            isMuted = !participant.isMuted,
                            isSpeaking = false,
                        )
                    } else {
                        participant
                    }
                }
                current.copy(voiceParticipants = updated)
            }
        }
    }

    fun toggleVoiceConnection() {
        _activeRoom.update { current ->
            if (current == null) null
            else current.copy(isVoiceConnected = !current.isVoiceConnected)
        }
    }

    private fun scheduleBotTurn() {
        botTurnJob?.cancel()
        botTurnJob = viewModelScope.launch {
            val delayMs = Random.nextLong(1500L, 2500L)
            delay(delayMs)
            val currentRoom = _activeRoom.value ?: return@launch
            if (!currentRoom.isOnline ||
                currentRoom.matchState.isMatchComplete ||
                currentRoom.matchState.isAwaitingNextLevel ||
                currentRoom.matchState.currentLevel.activePlayer != PlayerSlot.PLAYER_2
            ) {
                return@launch
            }
            val move = DotsAndBoxesEngine.chooseSmartBotMove(currentRoom.matchState.currentLevel)
            if (move != null) {
                applyMove(move)
            }
        }
    }

    private fun startVoiceSimulation() {
        voiceTickerJob?.cancel()
        voiceTickerJob = viewModelScope.launch {
            while (true) {
                delay(1600L)
                _activeRoom.update { current ->
                    if (current == null || !current.isOnline || !current.isVoiceConnected) current
                    else {
                        val updated = current.voiceParticipants.map { participant ->
                            if (participant.isMuted || participant.isLocalUser) {
                                participant.copy(isSpeaking = false)
                            } else {
                                participant.copy(isSpeaking = Random.nextFloat() < 0.48f)
                            }
                        }
                        current.copy(voiceParticipants = updated)
                    }
                }
            }
        }
    }

    private fun startFriendPresenceTicker() {
        presenceTickerJob?.cancel()
        presenceTickerJob = viewModelScope.launch {
            while (true) {
                delay(26_000L)
                val snap = friendSnapshot.value
                if (snap.friends.isEmpty()) continue
                val idx = Random.nextInt(snap.friends.size)
                val target = snap.friends[idx]
                val nextStatus = when (target.status) {
                    FriendStatus.ONLINE -> if (Random.nextBoolean()) FriendStatus.IN_MATCH else FriendStatus.OFFLINE
                    FriendStatus.IN_MATCH -> FriendStatus.ONLINE
                    FriendStatus.OFFLINE -> FriendStatus.ONLINE
                }
                val updatedList = snap.friends.toMutableList()
                updatedList[idx] = target.copy(
                    status = nextStatus,
                    lastSeenLabel = if (nextStatus == FriendStatus.OFFLINE) "Just now" else target.lastSeenLabel,
                )
                repo.saveFriendData(snap.copy(friends = updatedList))
            }
        }
    }

    private fun finalizeCompletedMatch(room: ActiveRoomUiState) {
        cancelRoomCoroutines()
        viewModelScope.launch {
            repo.saveActiveMatch(null)
            val p1Diamonds = room.matchState.totalP1Diamonds
            val p2Diamonds = room.matchState.totalP2Diamonds
            val (walletBefore, walletAfter) = creditDiamonds(
                amount = p1Diamonds,
                reference = if (room.isOnline) "Online match diamonds" else "Offline 1v1 diamonds",
            )

            if (room.isOnline) {
                val profile = userProfile.value
                val prevExp = profile?.totalExp ?: 0
                val prevLevel = profile?.level ?: 1
                val newExp = prevExp + ExpConfig.EXP_PER_ONLINE_MATCH
                val newLevel = ExpConfig.levelForExp(newExp)
                val won = room.matchState.matchWinner == PlayerSlot.PLAYER_1
                val p1TotalBoxes = room.matchState.totalP1Score

                if (newLevel > prevLevel && settings.value.soundEnabled) {
                    soundPlayer.playLevelUp()
                }

                if (profile != null) {
                    val updatedProfile = profile.copy(
                        totalExp = newExp,
                        onlineMatchesPlayed = profile.onlineMatchesPlayed + 1,
                        onlineMatchesWon = profile.onlineMatchesWon + if (won) 1 else 0,
                        totalBoxesClaimed = profile.totalBoxesClaimed + p1TotalBoxes,
                        bestMatchScore = maxOf(profile.bestMatchScore, p1TotalBoxes),
                    )
                    repo.saveProfile(updatedProfile)
                }

                _activeRoom.update { current ->
                    current?.copy(
                        isOpponentThinking = false,
                        diamondsCredited = true,
                        completionSummary = MatchCompletionSummary(
                            isOnline = true,
                            previousExp = prevExp,
                            newExp = newExp,
                            expAwarded = ExpConfig.EXP_PER_ONLINE_MATCH,
                            previousLevel = prevLevel,
                            newLevel = newLevel,
                            p1DiamondsEarned = p1Diamonds,
                            p2DiamondsEarned = p2Diamonds,
                            walletBeforeDiamonds = walletBefore,
                            walletAfterDiamonds = walletAfter,
                        ),
                    )
                }
            } else {
                _activeRoom.update { current ->
                    current?.copy(
                        isOpponentThinking = false,
                        diamondsCredited = true,
                        completionSummary = MatchCompletionSummary(
                            isOnline = false,
                            previousExp = 0,
                            newExp = 0,
                            expAwarded = 0,
                            previousLevel = 1,
                            newLevel = 1,
                            p1DiamondsEarned = p1Diamonds,
                            p2DiamondsEarned = p2Diamonds,
                            walletBeforeDiamonds = walletBefore,
                            walletAfterDiamonds = walletAfter,
                        ),
                    )
                }
            }
        }
    }

    /**
     * Doubles Player 1's match-earned diamonds after a rewarded AdMob video finishes.
     * Hook AdMob's RewardedAd OnUserEarnedRewardListener to call this method.
     */
    fun claimDoubleDiamondsReward() {
        val room = _activeRoom.value ?: return
        val summary = room.completionSummary ?: return
        if (summary.adDoubledDiamonds) return

        val bonus = summary.p1DiamondsEarned.coerceAtLeast(0)
        viewModelScope.launch {
            val (before, after) = if (bonus > 0) {
                creditDiamonds(
                    amount = bonus,
                    reference = if (room.isOnline) "2x Ad reward (online)" else "2x Ad reward (offline)",
                )
            } else {
                val cur = wallet.value.diamonds.coerceAtLeast(0)
                cur to cur
            }
            _activeRoom.update { current ->
                current?.copy(
                    completionSummary = current.completionSummary?.copy(
                        p1DiamondsEarned = summary.p1DiamondsEarned + bonus,
                        walletBeforeDiamonds = before,
                        walletAfterDiamonds = after,
                        adDoubledDiamonds = true,
                    )
                )
            }
            _uiNotice.value = if (bonus > 0) {
                "+$bonus bonus diamonds! (2x reward)"
            } else {
                "2x ad reward claimed!"
            }
        }
    }

    private suspend fun applyQuitMatchExpPenalty() {
        val profile = userProfile.value ?: return
        val updated = profile.copy(
            totalExp = ExpConfig.deductQuitPenalty(profile.totalExp),
        )
        repo.saveProfile(updated)
    }

    private fun persistRoomSession(room: ActiveRoomUiState) {
        viewModelScope.launch {
            repo.saveActiveMatch(
                PersistedMatchSession(
                    isOnline = room.isOnline,
                    isFriendMatch = room.isFriendMatch,
                    roomCode = room.roomCode,
                    player1 = room.player1,
                    player2 = room.player2,
                    matchState = room.matchState,
                )
            )
        }
    }

    private fun cancelRoomCoroutines() {
        botTurnJob?.cancel()
        botTurnJob = null
        voiceTickerJob?.cancel()
        voiceTickerJob = null
        chatReplyJob?.cancel()
        chatReplyJob = null
    }

    private fun currentTimeLabel(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        cancelRoomCoroutines()
        quickMatchJob?.cancel()
        inviteJob?.cancel()
        waitingGuestJob?.cancel()
        presenceTickerJob?.cancel()
    }

    companion object {
        fun shouldDeductQuitPenalty(isOnline: Boolean, isFriendMatch: Boolean): Boolean =
            isOnline && !isFriendMatch
    }
}
