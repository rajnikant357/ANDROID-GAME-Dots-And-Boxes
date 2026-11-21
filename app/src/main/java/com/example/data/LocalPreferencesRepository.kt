package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.engine.BoxCoord
import com.example.engine.GridPos
import com.example.engine.LevelState
import com.example.engine.LevelSummary
import com.example.engine.LineCoord
import com.example.engine.LineKind
import com.example.engine.MatchState
import com.example.engine.PlayerSlot
import com.example.model.AppSettings
import com.example.model.AvatarClothingId
import com.example.model.AvatarExpression
import com.example.model.AvatarSkinId
import com.example.model.BoxSkinId
import com.example.model.DiamondTransaction
import com.example.model.EquippedCosmetics
import com.example.model.FriendEntry
import com.example.model.FriendRequest
import com.example.model.FriendStatus
import com.example.model.GenderOption
import com.example.model.IdCodeGenerator
import com.example.model.LetterFontId
import com.example.model.OfflineMatchConfig
import com.example.model.PlayerColorOption
import com.example.model.PlayerIdentity
import com.example.model.PlayerPattern
import com.example.model.PurchaseRecord
import com.example.model.SubscriptionState
import com.example.model.TeddyAccessory
import com.example.model.TeddyAvatarConfig
import com.example.model.TeddyBodyColor
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.example.model.WalletState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dots_boxes_prefs")

data class PersistedMatchSession(
    val isOnline: Boolean,
    val isFriendMatch: Boolean = false,
    val roomCode: String?,
    val player1: PlayerIdentity,
    val player2: PlayerIdentity,
    val matchState: MatchState,
)

data class FriendDataSnapshot(
    val initialized: Boolean = false,
    val friends: List<FriendEntry> = emptyList(),
    val requests: List<FriendRequest> = emptyList(),
    val blockedIds: Set<String> = emptySet(),
)

class LocalPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_PROFILE = stringPreferencesKey("user_profile_json")
        val SUBSCRIPTION = stringPreferencesKey("subscription_json")
        val SETTINGS = stringPreferencesKey("settings_json")
        val OFFLINE_CONFIG = stringPreferencesKey("offline_config_json")
        val ACTIVE_MATCH = stringPreferencesKey("active_match_json")
        val WALLET = stringPreferencesKey("wallet")
        val INVENTORY = stringPreferencesKey("owned_cosmetics_json")
        val TRANSACTIONS = stringPreferencesKey("diamond_transactions_json")
        val PURCHASE_HISTORY = stringPreferencesKey("purchase_history_json")
        val FRIENDS_DATA = stringPreferencesKey("friends_data_json")
    }

    val userProfileFlow: Flow<UserProfile?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_PROFILE]?.let { parseProfile(it) }
    }

    val subscriptionFlow: Flow<SubscriptionState> = context.dataStore.data.map { prefs ->
        prefs[Keys.SUBSCRIPTION]?.let { parseSubscription(it) } ?: SubscriptionState()
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        prefs[Keys.SETTINGS]?.let { parseSettings(it) } ?: AppSettings()
    }

    val offlineConfigFlow: Flow<OfflineMatchConfig> = context.dataStore.data.map { prefs ->
        prefs[Keys.OFFLINE_CONFIG]?.let { parseOfflineConfig(it) } ?: OfflineMatchConfig()
    }

    val activeMatchFlow: Flow<PersistedMatchSession?> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_MATCH]?.let { parsePersistedMatch(it) }
    }

    val walletFlow: Flow<WalletState> = context.dataStore.data.map { prefs ->
        prefs[Keys.WALLET]?.let { parseWallet(it) } ?: WalletState(diamonds = 0)
    }

    val ownedCosmeticsFlow: Flow<Set<String>?> = context.dataStore.data.map { prefs ->
        prefs[Keys.INVENTORY]?.let { parseStringSet(it) }
    }

    val transactionsFlow: Flow<List<DiamondTransaction>> = context.dataStore.data.map { prefs ->
        prefs[Keys.TRANSACTIONS]?.let { parseTransactions(it) } ?: emptyList()
    }

    val purchaseHistoryFlow: Flow<List<PurchaseRecord>> = context.dataStore.data.map { prefs ->
        prefs[Keys.PURCHASE_HISTORY]?.let { parsePurchases(it) } ?: emptyList()
    }

    val friendDataFlow: Flow<FriendDataSnapshot> = context.dataStore.data.map { prefs ->
        prefs[Keys.FRIENDS_DATA]?.let { parseFriendData(it) } ?: FriendDataSnapshot()
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_PROFILE] = encodeProfile(profile)
        }
    }

    suspend fun saveSubscription(subscription: SubscriptionState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SUBSCRIPTION] = encodeSubscription(subscription)
        }
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SETTINGS] = encodeSettings(settings)
        }
    }

    suspend fun saveOfflineConfig(config: OfflineMatchConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OFFLINE_CONFIG] = encodeOfflineConfig(config)
        }
    }

    suspend fun saveActiveMatch(session: PersistedMatchSession?) {
        context.dataStore.edit { prefs ->
            if (session == null) {
                prefs.remove(Keys.ACTIVE_MATCH)
            } else {
                prefs[Keys.ACTIVE_MATCH] = encodePersistedMatch(session)
            }
        }
    }

    suspend fun saveWallet(wallet: WalletState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WALLET] = encodeWallet(wallet.copy(diamonds = wallet.diamonds.coerceAtLeast(0)))
        }
    }

    suspend fun saveOwnedCosmetics(ownedIds: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.INVENTORY] = JSONArray().apply {
                ownedIds.forEach { put(it) }
            }.toString()
        }
    }

    suspend fun saveTransactions(items: List<DiamondTransaction>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TRANSACTIONS] = encodeTransactions(items.take(80))
        }
    }

    suspend fun savePurchaseHistory(items: List<PurchaseRecord>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PURCHASE_HISTORY] = encodePurchases(items.take(60))
        }
    }

    suspend fun saveFriendData(snapshot: FriendDataSnapshot) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FRIENDS_DATA] = encodeFriendData(snapshot)
        }
    }

    private fun encodeAvatar(a: TeddyAvatarConfig): JSONObject = JSONObject().apply {
        put("body", a.body.name)
        put("accessory", a.accessory.name)
        put("expression", a.expression.name)
    }

    private fun parseAvatar(obj: JSONObject?): TeddyAvatarConfig {
        if (obj == null) return TeddyAvatarConfig()
        return TeddyAvatarConfig(
            body = TeddyBodyColor.fromName(obj.optString("body")),
            accessory = TeddyAccessory.fromName(obj.optString("accessory")),
            expression = AvatarExpression.fromName(obj.optString("expression")),
        )
    }

    private fun encodeEquipped(e: EquippedCosmetics): JSONObject = JSONObject().apply {
        put("boxSkin", e.boxSkin.name)
        put("avatarClothing", e.avatarClothing.name)
        put("avatarSkin", e.avatarSkin.name)
        put("letterFont", e.letterFont.name)
    }

    private fun parseEquipped(obj: JSONObject?): EquippedCosmetics {
        if (obj == null) return EquippedCosmetics()
        return EquippedCosmetics(
            boxSkin = BoxSkinId.fromName(obj.optString("boxSkin")),
            avatarClothing = AvatarClothingId.fromName(obj.optString("avatarClothing")),
            avatarSkin = AvatarSkinId.fromName(obj.optString("avatarSkin")),
            letterFont = LetterFontId.fromName(obj.optString("letterFont")),
        )
    }

    private fun encodeProfile(p: UserProfile): String = JSONObject().apply {
        put("playerId", p.playerId)
        put("displayName", p.displayName)
        put("initials", p.initials)
        put("gender", p.gender.name)
        put("colorOption", p.colorOption.name)
        put("pattern", p.pattern.name)
        put("avatarConfig", encodeAvatar(p.avatarConfig))
        put("equippedCosmetics", encodeEquipped(p.equippedCosmetics))
        put("totalExp", p.totalExp)
        put("onlineMatchesPlayed", p.onlineMatchesPlayed)
        put("onlineMatchesWon", p.onlineMatchesWon)
        put("totalBoxesClaimed", p.totalBoxesClaimed)
        put("bestMatchScore", p.bestMatchScore)
    }.toString()

    private fun parseProfile(raw: String): UserProfile? = runCatching {
        val obj = JSONObject(raw)
        val rawId = obj.optString("playerId", "")
        val resolvedId = if (IdCodeGenerator.isValidPlayerId(rawId)) {
            rawId
        } else {
            IdCodeGenerator.generatePlayerId()
        }
        UserProfile(
            playerId = resolvedId,
            displayName = obj.getString("displayName"),
            initials = obj.getString("initials"),
            gender = GenderOption.fromName(obj.optString("gender")),
            colorOption = PlayerColorOption.fromName(obj.optString("colorOption")),
            pattern = PlayerPattern.fromName(obj.optString("pattern")),
            avatarConfig = parseAvatar(obj.optJSONObject("avatarConfig")),
            equippedCosmetics = parseEquipped(obj.optJSONObject("equippedCosmetics")),
            totalExp = obj.optInt("totalExp", 0),
            onlineMatchesPlayed = obj.optInt("onlineMatchesPlayed", 0),
            onlineMatchesWon = obj.optInt("onlineMatchesWon", 0),
            totalBoxesClaimed = obj.optInt("totalBoxesClaimed", 0),
            bestMatchScore = obj.optInt("bestMatchScore", 0),
        )
    }.getOrNull()

    private fun encodeSubscription(s: SubscriptionState): String = JSONObject().apply {
        put("isActive", s.isActive)
        if (s.renewalDateEpochMillis != null) {
            put("renewalDateEpochMillis", s.renewalDateEpochMillis)
        }
    }.toString()

    private fun parseSubscription(raw: String): SubscriptionState = runCatching {
        val obj = JSONObject(raw)
        SubscriptionState(
            isActive = obj.optBoolean("isActive", false),
            renewalDateEpochMillis = if (obj.has("renewalDateEpochMillis")) {
                obj.getLong("renewalDateEpochMillis")
            } else {
                null
            },
        )
    }.getOrDefault(SubscriptionState())

    private fun encodeSettings(s: AppSettings): String = JSONObject().apply {
        put("soundEnabled", s.soundEnabled)
        put("hapticsEnabled", s.hapticsEnabled)
        put("passDevicePrompt", s.passDevicePrompt)
        put("reducedMotion", s.reducedMotion)
        put("dyslexiaFont", s.dyslexiaFont)
    }.toString()

    private fun parseSettings(raw: String): AppSettings = runCatching {
        val obj = JSONObject(raw)
        AppSettings(
            soundEnabled = obj.optBoolean("soundEnabled", true),
            hapticsEnabled = obj.optBoolean("hapticsEnabled", true),
            passDevicePrompt = obj.optBoolean("passDevicePrompt", false),
            reducedMotion = obj.optBoolean("reducedMotion", false),
            dyslexiaFont = obj.optBoolean("dyslexiaFont", false),
        )
    }.getOrDefault(AppSettings())

    private fun encodeOfflineConfig(c: OfflineMatchConfig): String = JSONObject().apply {
        put("p1Name", c.p1Name)
        put("p1Initials", c.p1Initials)
        put("p1Color", c.p1Color.name)
        put("p1Pattern", c.p1Pattern.name)
        put("p1Avatar", encodeAvatar(c.p1Avatar))
        put("p2Name", c.p2Name)
        put("p2Initials", c.p2Initials)
        put("p2Color", c.p2Color.name)
        put("p2Pattern", c.p2Pattern.name)
        put("p2Avatar", encodeAvatar(c.p2Avatar))
        put("alternateStarter", c.alternateStarter)
    }.toString()

    private fun parseOfflineConfig(raw: String): OfflineMatchConfig = runCatching {
        val obj = JSONObject(raw)
        OfflineMatchConfig(
            p1Name = obj.optString("p1Name", "Player 1"),
            p1Initials = obj.optString("p1Initials", "P1"),
            p1Color = PlayerColorOption.fromName(obj.optString("p1Color")),
            p1Pattern = PlayerPattern.fromName(obj.optString("p1Pattern", PlayerPattern.STRIPES.name)),
            p1Avatar = parseAvatar(obj.optJSONObject("p1Avatar")),
            p2Name = obj.optString("p2Name", "Player 2"),
            p2Initials = obj.optString("p2Initials", "P2"),
            p2Color = PlayerColorOption.fromName(obj.optString("p2Color", PlayerColorOption.SKY.name)),
            p2Pattern = PlayerPattern.fromName(obj.optString("p2Pattern", PlayerPattern.DOTS.name)),
            p2Avatar = parseAvatar(obj.optJSONObject("p2Avatar")),
            alternateStarter = obj.optBoolean("alternateStarter", true),
        )
    }.getOrDefault(OfflineMatchConfig())

    private fun encodeIdentity(id: PlayerIdentity): JSONObject = JSONObject().apply {
        put("name", id.name)
        put("initials", id.initials)
        put("color", id.color.name)
        put("pattern", id.pattern.name)
        put("avatarConfig", encodeAvatar(id.avatarConfig))
        put("equippedCosmetics", encodeEquipped(id.equippedCosmetics))
        if (id.playerId != null) put("playerId", id.playerId)
        if (id.level != null) put("level", id.level)
        if (id.gender != null) put("gender", id.gender.name)
    }

    private fun parseIdentity(obj: JSONObject): PlayerIdentity = PlayerIdentity(
        name = obj.getString("name"),
        initials = obj.getString("initials"),
        color = PlayerColorOption.fromName(obj.optString("color")),
        pattern = PlayerPattern.fromName(obj.optString("pattern")),
        avatarConfig = parseAvatar(obj.optJSONObject("avatarConfig")),
        equippedCosmetics = parseEquipped(obj.optJSONObject("equippedCosmetics")),
        playerId = if (obj.has("playerId")) obj.getString("playerId") else null,
        level = if (obj.has("level")) obj.getInt("level") else null,
        gender = if (obj.has("gender")) GenderOption.fromName(obj.optString("gender")) else null,
    )

    private fun encodePersistedMatch(session: PersistedMatchSession): String {
        val m = session.matchState
        val level = m.currentLevel
        val levelJson = JSONObject().apply {
            put("levelNumber", level.levelNumber)
            put("dotCount", level.dotCount)
            put("startingPlayer", level.startingPlayer.name)
            put("activePlayer", level.activePlayer.name)
            put("p1Diamonds", level.p1Diamonds)
            put("p2Diamonds", level.p2Diamonds)
            put("horizontal", JSONArray().apply {
                level.horizontalLines.forEach { put("${it.row},${it.col}") }
            })
            put("vertical", JSONArray().apply {
                level.verticalLines.forEach { put("${it.row},${it.col}") }
            })
            put("boxOwners", JSONArray().apply {
                level.boxOwners.forEach { (box, slot) ->
                    put("${box.row},${box.col},${slot.name}")
                }
            })
            put("diamondBoxes", JSONArray().apply {
                level.diamondBoxes.forEach { put("${it.row},${it.col}") }
            })
            put("revealedDiamonds", JSONArray().apply {
                level.revealedDiamonds.forEach { put("${it.row},${it.col}") }
            })
            if (level.lastMove != null) {
                put("lastMove", "${level.lastMove.kind.name},${level.lastMove.row},${level.lastMove.col}")
            }
        }
        val completedJson = JSONArray().apply {
            m.completedLevels.forEach { summary ->
                put(JSONObject().apply {
                    put("levelNumber", summary.levelNumber)
                    put("dotCount", summary.dotCount)
                    put("p1Boxes", summary.p1Boxes)
                    put("p2Boxes", summary.p2Boxes)
                    put("p1Diamonds", summary.p1Diamonds)
                    put("p2Diamonds", summary.p2Diamonds)
                    if (summary.winner != null) put("winner", summary.winner.name)
                })
            }
        }
        return JSONObject().apply {
            put("isOnline", session.isOnline)
            put("isFriendMatch", session.isFriendMatch)
            if (session.roomCode != null) put("roomCode", session.roomCode)
            put("player1", encodeIdentity(session.player1))
            put("player2", encodeIdentity(session.player2))
            put("currentLevelIndex", m.currentLevelIndex)
            put("alternateStarter", m.alternateStarter)
            put("isAwaitingNextLevel", m.isAwaitingNextLevel)
            put("isMatchComplete", m.isMatchComplete)
            put("currentLevel", levelJson)
            put("completedLevels", completedJson)
        }.toString()
    }

    private fun parsePersistedMatch(raw: String): PersistedMatchSession? = runCatching {
        val obj = JSONObject(raw)
        val levelObj = obj.getJSONObject("currentLevel")
        val hSet = buildSet {
            val arr = levelObj.getJSONArray("horizontal")
            for (i in 0 until arr.length()) {
                val parts = arr.getString(i).split(",")
                add(GridPos(parts[0].toInt(), parts[1].toInt()))
            }
        }
        val vSet = buildSet {
            val arr = levelObj.getJSONArray("vertical")
            for (i in 0 until arr.length()) {
                val parts = arr.getString(i).split(",")
                add(GridPos(parts[0].toInt(), parts[1].toInt()))
            }
        }
        val owners = buildMap {
            val arr = levelObj.getJSONArray("boxOwners")
            for (i in 0 until arr.length()) {
                val parts = arr.getString(i).split(",")
                put(BoxCoord(parts[0].toInt(), parts[1].toInt()), PlayerSlot.valueOf(parts[2]))
            }
        }
        val diamonds = buildSet {
            val arr = levelObj.optJSONArray("diamondBoxes")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val parts = arr.getString(i).split(",")
                    add(BoxCoord(parts[0].toInt(), parts[1].toInt()))
                }
            }
        }
        val revealed = buildSet {
            val arr = levelObj.optJSONArray("revealedDiamonds")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val parts = arr.getString(i).split(",")
                    add(BoxCoord(parts[0].toInt(), parts[1].toInt()))
                }
            }
        }
        val lastMove = if (levelObj.has("lastMove")) {
            val parts = levelObj.getString("lastMove").split(",")
            LineCoord(LineKind.valueOf(parts[0]), parts[1].toInt(), parts[2].toInt())
        } else {
            null
        }
        val level = LevelState(
            levelNumber = levelObj.getInt("levelNumber"),
            dotCount = levelObj.getInt("dotCount"),
            horizontalLines = hSet,
            verticalLines = vSet,
            boxOwners = owners,
            startingPlayer = PlayerSlot.valueOf(levelObj.getString("startingPlayer")),
            activePlayer = PlayerSlot.valueOf(levelObj.getString("activePlayer")),
            lastMove = lastMove,
            diamondBoxes = diamonds,
            revealedDiamonds = revealed,
            p1Diamonds = levelObj.optInt("p1Diamonds", 0),
            p2Diamonds = levelObj.optInt("p2Diamonds", 0),
        )
        val completed = buildList {
            val arr = obj.getJSONArray("completedLevels")
            for (i in 0 until arr.length()) {
                val sObj = arr.getJSONObject(i)
                add(
                    LevelSummary(
                        levelNumber = sObj.getInt("levelNumber"),
                        dotCount = sObj.getInt("dotCount"),
                        p1Boxes = sObj.getInt("p1Boxes"),
                        p2Boxes = sObj.getInt("p2Boxes"),
                        winner = if (sObj.has("winner")) PlayerSlot.valueOf(sObj.getString("winner")) else null,
                        p1Diamonds = sObj.optInt("p1Diamonds", 0),
                        p2Diamonds = sObj.optInt("p2Diamonds", 0),
                    )
                )
            }
        }
        val matchState = MatchState(
            currentLevelIndex = obj.getInt("currentLevelIndex"),
            currentLevel = level,
            completedLevels = completed,
            alternateStarter = obj.optBoolean("alternateStarter", true),
            isAwaitingNextLevel = obj.optBoolean("isAwaitingNextLevel", false),
            isMatchComplete = obj.optBoolean("isMatchComplete", false),
        )
        PersistedMatchSession(
            isOnline = obj.getBoolean("isOnline"),
            isFriendMatch = obj.optBoolean("isFriendMatch", false),
            roomCode = if (obj.has("roomCode")) obj.getString("roomCode") else null,
            player1 = parseIdentity(obj.getJSONObject("player1")),
            player2 = parseIdentity(obj.getJSONObject("player2")),
            matchState = matchState,
        )
    }.getOrNull()

    private fun encodeWallet(w: WalletState): String = JSONObject().apply {
        put("diamonds", w.diamonds.coerceAtLeast(0))
        put("updatedAtMillis", w.updatedAtMillis)
    }.toString()

    private fun parseWallet(raw: String): WalletState = runCatching {
        val obj = JSONObject(raw)
        WalletState(
            diamonds = obj.optInt("diamonds", 0).coerceAtLeast(0),
            updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis()),
        )
    }.getOrDefault(WalletState(diamonds = 0))

    private fun parseStringSet(raw: String): Set<String> = runCatching {
        val arr = JSONArray(raw)
        buildSet {
            for (i in 0 until arr.length()) add(arr.getString(i))
        }
    }.getOrDefault(emptySet())

    private fun encodeTransactions(list: List<DiamondTransaction>): String = JSONArray().apply {
        list.forEach { t ->
            put(JSONObject().apply {
                put("id", t.id)
                put("type", t.type.name)
                put("amount", t.amount)
                put("reference", t.reference)
                put("createdAtMillis", t.createdAtMillis)
            })
        }
    }.toString()

    private fun parseTransactions(raw: String): List<DiamondTransaction> = runCatching {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    DiamondTransaction(
                        id = obj.getString("id"),
                        type = TransactionType.valueOf(obj.getString("type")),
                        amount = obj.getInt("amount"),
                        reference = obj.getString("reference"),
                        createdAtMillis = obj.getLong("createdAtMillis"),
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun encodePurchases(list: List<PurchaseRecord>): String = JSONArray().apply {
        list.forEach { p ->
            put(JSONObject().apply {
                put("id", p.id)
                put("productId", p.productId)
                put("title", p.title)
                put("priceLabel", p.priceLabel)
                put("platform", p.platform)
                put("status", p.status)
                put("verifiedAtMillis", p.verifiedAtMillis)
            })
        }
    }.toString()

    private fun parsePurchases(raw: String): List<PurchaseRecord> = runCatching {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    PurchaseRecord(
                        id = obj.getString("id"),
                        productId = obj.getString("productId"),
                        title = obj.getString("title"),
                        priceLabel = obj.getString("priceLabel"),
                        platform = obj.optString("platform", "prototype"),
                        status = obj.optString("status", "completed"),
                        verifiedAtMillis = obj.getLong("verifiedAtMillis"),
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun encodeFriendData(snapshot: FriendDataSnapshot): String = JSONObject().apply {
        put("initialized", snapshot.initialized)
        put("friends", JSONArray().apply {
            snapshot.friends.forEach { f ->
                put(JSONObject().apply {
                    put("playerId", f.playerId)
                    put("name", f.name)
                    put("initials", f.initials)
                    put("gender", f.gender.name)
                    put("color", f.color.name)
                    put("pattern", f.pattern.name)
                    put("avatarConfig", encodeAvatar(f.avatarConfig))
                    put("equippedCosmetics", encodeEquipped(f.equippedCosmetics))
                    put("level", f.level)
                    put("status", f.status.name)
                    put("lastSeenLabel", f.lastSeenLabel)
                })
            }
        })
        put("requests", JSONArray().apply {
            snapshot.requests.forEach { r ->
                put(JSONObject().apply {
                    put("id", r.id)
                    put("playerId", r.playerId)
                    put("name", r.name)
                    put("initials", r.initials)
                    put("gender", r.gender.name)
                    put("color", r.color.name)
                    put("pattern", r.pattern.name)
                    put("avatarConfig", encodeAvatar(r.avatarConfig))
                    put("equippedCosmetics", encodeEquipped(r.equippedCosmetics))
                    put("level", r.level)
                    put("isIncoming", r.isIncoming)
                    put("createdAtMillis", r.createdAtMillis)
                })
            }
        })
        put("blockedIds", JSONArray().apply {
            snapshot.blockedIds.forEach { put(it) }
        })
    }.toString()

    private fun parseFriendData(raw: String): FriendDataSnapshot = runCatching {
        val obj = JSONObject(raw)
        val friends = buildList {
            val arr = obj.optJSONArray("friends")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val f = arr.getJSONObject(i)
                    add(
                        FriendEntry(
                            playerId = f.getString("playerId"),
                            name = f.getString("name"),
                            initials = f.getString("initials"),
                            gender = GenderOption.fromName(f.optString("gender")),
                            color = PlayerColorOption.fromName(f.optString("color")),
                            pattern = PlayerPattern.fromName(f.optString("pattern")),
                            avatarConfig = parseAvatar(f.optJSONObject("avatarConfig")),
                            equippedCosmetics = parseEquipped(f.optJSONObject("equippedCosmetics")),
                            level = f.optInt("level", 1),
                            status = FriendStatus.fromName(f.optString("status")),
                            lastSeenLabel = f.optString("lastSeenLabel", "Recently"),
                        )
                    )
                }
            }
        }
        val requests = buildList {
            val arr = obj.optJSONArray("requests")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val r = arr.getJSONObject(i)
                    add(
                        FriendRequest(
                            id = r.getString("id"),
                            playerId = r.getString("playerId"),
                            name = r.getString("name"),
                            initials = r.getString("initials"),
                            gender = GenderOption.fromName(r.optString("gender")),
                            color = PlayerColorOption.fromName(r.optString("color")),
                            pattern = PlayerPattern.fromName(r.optString("pattern")),
                            avatarConfig = parseAvatar(r.optJSONObject("avatarConfig")),
                            equippedCosmetics = parseEquipped(r.optJSONObject("equippedCosmetics")),
                            level = r.optInt("level", 1),
                            isIncoming = r.optBoolean("isIncoming", true),
                            createdAtMillis = r.optLong("createdAtMillis", System.currentTimeMillis()),
                        )
                    )
                }
            }
        }
        val blocked = buildSet {
            val arr = obj.optJSONArray("blockedIds")
            if (arr != null) {
                for (i in 0 until arr.length()) add(arr.getString(i))
            }
        }
        FriendDataSnapshot(
            initialized = obj.optBoolean("initialized", false),
            friends = friends,
            requests = requests,
            blockedIds = blocked,
        )
    }.getOrDefault(FriendDataSnapshot())
}
