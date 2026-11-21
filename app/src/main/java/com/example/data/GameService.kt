package com.example.data

import com.example.model.CosmeticItem
import com.example.model.DiamondPack
import com.example.model.FriendEntry
import com.example.model.FriendRequest
import com.example.model.LeaderboardEntry
import com.example.model.MockDirectoryPlayer
import com.example.model.MockOnlinePlayer
import com.example.model.MockRoom
import com.example.model.PlayerColorOption
import com.example.model.UserProfile
import com.example.model.VoiceParticipant

enum class LeaderboardScope {
    WEEKLY,
    ALL_TIME,
}

interface GameService {
    fun getOpenRooms(): List<MockRoom>
    fun getOnlinePlayers(): List<MockOnlinePlayer>
    fun getLeaderboard(scope: LeaderboardScope, currentUser: UserProfile?): List<LeaderboardEntry>
    fun getChatReplyPool(): List<String>
    fun pickRandomChatReply(): String
    fun createInitialVoiceParticipants(
        opponentName: String,
        opponentInitials: String,
        opponentColor: PlayerColorOption,
        localUser: UserProfile?,
    ): List<VoiceParticipant>
    fun generateRoomCode(): String
    fun pickQuickMatchOpponent(excludeColor: PlayerColorOption): MockRoom

    // Addendum v2 additions
    fun getCosmeticCatalog(): List<CosmeticItem>
    fun getDefaultOwnedCosmeticIds(): Set<String>
    fun getDiamondPacks(): List<DiamondPack>
    fun getDirectoryPlayers(): List<MockDirectoryPlayer>
    fun findPlayerById(rawId: String): MockDirectoryPlayer?
    fun findRoomByCode(rawCode: String): MockRoom?
    fun getInitialFriends(): List<FriendEntry>
    fun getInitialIncomingRequests(): List<FriendRequest>
}
