package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object ExpConfig {
    const val EXP_PER_LEVEL = 100
    const val EXP_PER_ONLINE_MATCH = 5
    const val EXP_PENALTY_QUIT_MATCH = 5

    fun levelForExp(totalExp: Int): Int = (totalExp.coerceAtLeast(0) / EXP_PER_LEVEL) + 1
    fun expIntoLevel(totalExp: Int): Int = totalExp.coerceAtLeast(0) % EXP_PER_LEVEL
    fun progressForExp(totalExp: Int): Float =
        expIntoLevel(totalExp).toFloat() / EXP_PER_LEVEL.toFloat()

    fun deductQuitPenalty(totalExp: Int): Int =
        (totalExp - EXP_PENALTY_QUIT_MATCH).coerceAtLeast(0)
}

object IdCodeGenerator {
    // Letters A-Z excluding O and I; digits 2-9 excluding 0 and 1 (Addendum §6.1 & §7.2)
    const val ALLOWED_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    const val MAX_FRIENDS_LIMIT = 200

    fun generateSixChars(random: Random = Random.Default): String {
        return buildString(6) {
            repeat(6) {
                append(ALLOWED_CHARS[random.nextInt(ALLOWED_CHARS.length)])
            }
        }
    }

    fun generatePlayerId(random: Random = Random.Default): String =
        "DB-${generateSixChars(random)}"

    fun generateRoomCode(random: Random = Random.Default): String =
        generateSixChars(random)

    fun isValidCodeChars(code: String): Boolean =
        code.length == 6 && code.all { it in ALLOWED_CHARS }

    fun isValidPlayerId(id: String): Boolean =
        id.startsWith("DB-") && isValidCodeChars(id.removePrefix("DB-"))
}

enum class GenderOption(val label: String, val mark: String) {
    MALE("Male", "M"),
    FEMALE("Female", "F"),
    NON_BINARY("Non-binary", "NB"),
    PREFER_NOT_TO_SAY("Prefer not to say", "—");

    companion object {
        fun fromName(name: String?): GenderOption =
            entries.firstOrNull { it.name == name } ?: PREFER_NOT_TO_SAY
    }
}

// DRD §3.3: 8 playful profile colors on paper
enum class PlayerColorOption(
    val label: String,
    val colorValue: Long,
    val usesLightOutlineText: Boolean = false,
) {
    TOMATO("Tomato", 0xFFFF7A5C, true),
    SKY("Sky", 0xFF4AA8F0, false),
    MINT("Mint", 0xFF4FC08D, false),
    HONEY("Honey", 0xFFF7B73A, false),
    BERRY("Berry", 0xFFEF5B6B, true),
    LILAC("Lilac", 0xFFA989E8, true),
    TEDDY("Teddy", 0xFFC98B53, true),
    LIME("Lime", 0xFF9BD35A, false);

    val color: Color get() = Color(colorValue)

    companion object {
        fun fromName(name: String?): PlayerColorOption = when (name) {
            "TERRACOTTA" -> TOMATO
            "MUTED_TEAL" -> SKY
            "OLIVE" -> MINT
            "SLATE" -> SKY
            "PLUM" -> LILAC
            "OCHRE" -> HONEY
            else -> entries.firstOrNull { it.name == name } ?: TOMATO
        }
    }
}

// DRD §3.3 & §11: Patterns so color is never the only signal
enum class PlayerPattern(val label: String) {
    STRIPES("Stripes"),
    DOTS("Dots"),
    STARS("Stars"),
    CHECKS("Checks");

    companion object {
        fun fromName(name: String?): PlayerPattern =
            entries.firstOrNull { it.name == name } ?: STRIPES
    }
}

// DRD §7.5: 8 Teddy base body colors
enum class TeddyBodyColor(val label: String, val fillHex: Long, val shadeHex: Long) {
    TEDDY_BROWN("Teddy Brown", 0xFFC98B53, 0xFF9A6034),
    HONEY("Honey", 0xFFF7B73A, 0xFFC98B1E),
    CREAM("Cream", 0xFFFFF4DE, 0xFFD9C39A),
    DARK_CHOCOLATE("Dark Chocolate", 0xFF7D4E2D, 0xFF543118),
    GREY("Grey", 0xFFB0B4BC, 0xFF7E838C),
    PINK("Pink", 0xFFF6A5B8, 0xFFCC758B),
    SKY("Sky", 0xFF7DC3F7, 0xFF4893CC),
    MINT("Mint", 0xFF7FD9B0, 0xFF4CA87F);

    val fillColor: Color get() = Color(fillHex)
    val shadeColor: Color get() = Color(shadeHex)

    companion object {
        fun fromName(name: String?): TeddyBodyColor =
            entries.firstOrNull { it.name == name } ?: TEDDY_BROWN
    }
}

// DRD §7.5: 12 optional accessories + None
enum class TeddyAccessory(val label: String) {
    NONE("None"),
    BOW("Bow"),
    BEANIE("Beanie"),
    CROWN("Crown"),
    GLASSES("Glasses"),
    FLOWER("Flower"),
    BANDANA("Bandana"),
    SCARF("Scarf"),
    STAR_CLIP("Star clip"),
    HEADPHONES("Headphones"),
    CAP("Cap"),
    BUNNY_EARS("Bunny ears"),
    HALO("Halo");

    companion object {
        fun fromName(name: String?): TeddyAccessory =
            entries.firstOrNull { it.name == name } ?: SCARF
    }
}

// DRD §7.5: Happy or Celebrating for avatar selection
enum class AvatarExpression(val label: String) {
    HAPPY("Happy"),
    CELEBRATING("Celebrating");

    companion object {
        fun fromName(name: String?): AvatarExpression =
            entries.firstOrNull { it.name == name } ?: HAPPY
    }
}

// Addendum §5: Cosmetic categories & IDs
enum class CosmeticCategory(val label: String) {
    BOX_SKIN("Box skins"),
    AVATAR_CLOTHES("Avatar clothes"),
    AVATAR_SKIN("Avatar skins"),
    LETTER_FONT("Letter fonts"),
}

enum class CosmeticRarity(val label: String) {
    DEFAULT("Default"),
    BASIC("Basic"),
    MID("Mid"),
    RARE("Rare"),
}

enum class BoxSkinId(val label: String, val itemId: String) {
    CLASSIC_PAPER("Classic Paper", "box_classic_paper"),
    WOODEN_TILE("Wooden Tile", "box_wooden_tile"),
    PAPER_STAMP("Paper Stamp", "box_paper_stamp"),
    WOVEN_MAT("Woven Mat", "box_woven_mat"),
    CANDY_WRAPPER("Candy Wrapper", "box_candy_wrapper"),
    PIXEL_BLOCK("Pixel Block", "box_pixel_block"),
    ROYAL_ENAMEL("Royal Enamel", "box_royal_enamel");

    companion object {
        fun fromName(name: String?): BoxSkinId =
            entries.firstOrNull { it.name == name } ?: CLASSIC_PAPER
    }
}

enum class AvatarClothingId(val label: String, val itemId: String) {
    NONE("No Outfit", "cloth_none"),
    BERRY_SCARF("Cozy Scarf", "cloth_scarf"),
    STRIPED_SWEATER("Striped Sweater", "cloth_sweater"),
    VARSITY_JACKET("Varsity Jacket", "cloth_jacket"),
    CRAFT_OVERALLS("Craft Overalls", "cloth_overalls"),
    STARRY_PAJAMAS("Starry Pajamas", "cloth_pajamas"),
    HERO_CAPE("Honey Cape", "cloth_cape");

    companion object {
        fun fromName(name: String?): AvatarClothingId =
            entries.firstOrNull { it.name == name } ?: NONE
    }
}

enum class AvatarSkinId(val label: String, val itemId: String) {
    CLASSIC_PLUSH("Classic Plush", "skin_classic_plush"),
    SPOTTED_COOKIE("Spotted Cookie", "skin_spotted_cookie"),
    TIGER_STRIPED("Tiger Striped", "skin_tiger_striped"),
    PATCHWORK_QUILT("Patchwork Quilt", "skin_patchwork_quilt"),
    TWO_TONE_PANDA("Two-Tone Panda", "skin_two_tone_panda"),
    FLUFFY_CLOUD("Fluffy Cloud", "skin_fluffy_cloud"),
    STARLIGHT_PLUSH("Starlight Plush", "skin_starlight_plush");

    companion object {
        fun fromName(name: String?): AvatarSkinId =
            entries.firstOrNull { it.name == name } ?: CLASSIC_PLUSH
    }
}

enum class LetterFontId(val label: String, val itemId: String) {
    BOLD_BLOCK("Bold Block", "font_bold_block"),
    ROUNDED_BUBBLE("Rounded Bubble", "font_rounded_bubble"),
    STICKER_OUTLINE("Sticker Outline", "font_sticker_outline"),
    SHADOW_RIBBON("Shadow Ribbon", "font_shadow_ribbon"),
    HANDWRITTEN_MARKER("Marker Script", "font_marker_script"),
    WOODBLOCK_SERIF("Woodblock Serif", "font_woodblock_serif"),
    TYPEWRITER_STAMP("Typewriter Stamp", "font_typewriter_stamp");

    companion object {
        fun fromName(name: String?): LetterFontId =
            entries.firstOrNull { it.name == name } ?: BOLD_BLOCK
    }
}

data class EquippedCosmetics(
    val boxSkin: BoxSkinId = BoxSkinId.CLASSIC_PAPER,
    val avatarClothing: AvatarClothingId = AvatarClothingId.NONE,
    val avatarSkin: AvatarSkinId = AvatarSkinId.CLASSIC_PLUSH,
    val letterFont: LetterFontId = LetterFontId.BOLD_BLOCK,
) {
    fun isItemEquipped(itemId: String): Boolean =
        boxSkin.itemId == itemId ||
            avatarClothing.itemId == itemId ||
            avatarSkin.itemId == itemId ||
            letterFont.itemId == itemId
}

data class CosmeticItem(
    val id: String,
    val category: CosmeticCategory,
    val name: String,
    val description: String,
    val priceDiamonds: Int,
    val rarity: CosmeticRarity,
    val isDefault: Boolean = false,
    val boxSkinId: BoxSkinId? = null,
    val clothingId: AvatarClothingId? = null,
    val avatarSkinId: AvatarSkinId? = null,
    val letterFontId: LetterFontId? = null,
)

data class DiamondPack(
    val id: String,
    val name: String,
    val diamonds: Int,
    val priceInr: Int,
    val badge: String? = null,
)

data class WalletState(
    val diamonds: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

enum class TransactionType {
    EARN,
    PURCHASE,
    SPEND,
}

data class DiamondTransaction(
    val id: String,
    val type: TransactionType,
    val amount: Int,
    val reference: String,
    val createdAtMillis: Long,
)

data class PurchaseRecord(
    val id: String,
    val productId: String,
    val title: String,
    val priceLabel: String,
    val platform: String = "prototype",
    val status: String = "completed",
    val verifiedAtMillis: Long,
)

data class TeddyAvatarConfig(
    val body: TeddyBodyColor = TeddyBodyColor.TEDDY_BROWN,
    val accessory: TeddyAccessory = TeddyAccessory.SCARF,
    val expression: AvatarExpression = AvatarExpression.HAPPY,
)

data class UserProfile(
    val playerId: String = IdCodeGenerator.generatePlayerId(),
    val displayName: String,
    val initials: String,
    val gender: GenderOption,
    val colorOption: PlayerColorOption,
    val pattern: PlayerPattern = PlayerPattern.STRIPES,
    val avatarConfig: TeddyAvatarConfig = TeddyAvatarConfig(),
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val totalExp: Int = 0,
    val onlineMatchesPlayed: Int = 0,
    val onlineMatchesWon: Int = 0,
    val totalBoxesClaimed: Int = 0,
    val bestMatchScore: Int = 0,
) {
    val level: Int get() = ExpConfig.levelForExp(totalExp)
    val expIntoLevel: Int get() = ExpConfig.expIntoLevel(totalExp)
    val levelProgress: Float get() = ExpConfig.progressForExp(totalExp)
}

data class SubscriptionState(
    val isActive: Boolean = false,
    val renewalDateEpochMillis: Long? = null,
)

data class AppSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val passDevicePrompt: Boolean = false,
    val reducedMotion: Boolean = false,
    val dyslexiaFont: Boolean = false,
)

data class OfflineMatchConfig(
    val p1Name: String = "Player 1",
    val p1Initials: String = "P",
    val p1Color: PlayerColorOption = PlayerColorOption.TOMATO,
    val p1Pattern: PlayerPattern = PlayerPattern.STRIPES,
    val p1Avatar: TeddyAvatarConfig = TeddyAvatarConfig(TeddyBodyColor.TEDDY_BROWN, TeddyAccessory.SCARF),
    val p2Name: String = "Player 2",
    val p2Initials: String = "P",
    val p2Color: PlayerColorOption = PlayerColorOption.SKY,
    val p2Pattern: PlayerPattern = PlayerPattern.DOTS,
    val p2Avatar: TeddyAvatarConfig = TeddyAvatarConfig(TeddyBodyColor.SKY, TeddyAccessory.BEANIE),
    val alternateStarter: Boolean = true,
)

data class PlayerIdentity(
    val name: String,
    val initials: String,
    val color: PlayerColorOption,
    val pattern: PlayerPattern = PlayerPattern.STRIPES,
    val avatarConfig: TeddyAvatarConfig = TeddyAvatarConfig(),
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val playerId: String? = null,
    val level: Int? = null,
    val gender: GenderOption? = null,
)

data class LeaderboardEntry(
    val id: String,
    val playerId: String,
    val name: String,
    val initials: String,
    val gender: GenderOption,
    val color: PlayerColorOption,
    val pattern: PlayerPattern,
    val avatarConfig: TeddyAvatarConfig,
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val totalExp: Int,
    val weeklyExp: Int,
    val matchesWon: Int,
    val isCurrentUser: Boolean = false,
) {
    val level: Int get() = ExpConfig.levelForExp(totalExp)
}

enum class RoomVisibility(val label: String) {
    PRIVATE("Private"),
    PUBLIC("Public");

    companion object {
        fun fromName(name: String?): RoomVisibility =
            entries.firstOrNull { it.name == name } ?: PRIVATE
    }
}

data class MockRoom(
    val id: String,
    val code: String,
    val hostPlayerId: String,
    val hostName: String,
    val hostInitials: String,
    val hostGender: GenderOption,
    val hostColor: PlayerColorOption,
    val hostPattern: PlayerPattern,
    val hostAvatar: TeddyAvatarConfig,
    val hostCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val hostLevel: Int,
    val hostExp: Int,
    val statusLabel: String,
    val visibility: RoomVisibility = RoomVisibility.PUBLIC,
)

data class WaitingRoomState(
    val code: String,
    val visibility: RoomVisibility,
    val hostPlayer: PlayerIdentity,
    val guestPlayer: PlayerIdentity? = null,
    val isLocalHost: Boolean = true,
    val isSimulatingGuestJoin: Boolean = false,
)

enum class FriendStatus(val label: String, val sortOrder: Int) {
    ONLINE("Online", 0),
    IN_MATCH("In a match", 1),
    OFFLINE("Offline", 2);

    companion object {
        fun fromName(name: String?): FriendStatus =
            entries.firstOrNull { it.name == name } ?: OFFLINE
    }
}

data class FriendEntry(
    val playerId: String,
    val name: String,
    val initials: String,
    val gender: GenderOption,
    val color: PlayerColorOption,
    val pattern: PlayerPattern,
    val avatarConfig: TeddyAvatarConfig,
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val level: Int,
    val status: FriendStatus,
    val lastSeenLabel: String = "Just now",
)

data class FriendRequest(
    val id: String,
    val playerId: String,
    val name: String,
    val initials: String,
    val gender: GenderOption,
    val color: PlayerColorOption,
    val pattern: PlayerPattern,
    val avatarConfig: TeddyAvatarConfig,
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val level: Int,
    val isIncoming: Boolean,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class MockDirectoryPlayer(
    val playerId: String,
    val name: String,
    val initials: String,
    val gender: GenderOption,
    val color: PlayerColorOption,
    val pattern: PlayerPattern,
    val avatarConfig: TeddyAvatarConfig,
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val level: Int,
    val status: FriendStatus,
    val lastSeenLabel: String,
)

data class MockOnlinePlayer(
    val id: String,
    val playerId: String,
    val name: String,
    val initials: String,
    val gender: GenderOption,
    val color: PlayerColorOption,
    val avatarConfig: TeddyAvatarConfig,
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val level: Int,
    val status: String,
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderInitials: String,
    val senderColor: PlayerColorOption,
    val senderAvatar: TeddyAvatarConfig,
    val senderCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val text: String,
    val timestamp: String,
    val isLocalUser: Boolean,
)

data class VoiceParticipant(
    val id: String,
    val name: String,
    val initials: String,
    val color: PlayerColorOption,
    val avatarConfig: TeddyAvatarConfig,
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    val isSpeaking: Boolean,
    val isMuted: Boolean,
    val isLocalUser: Boolean = false,
)

fun deriveInitialsFromName(name: String): String {
    val first = name.trim().firstOrNull { it.isLetterOrDigit() } ?: return ""
    return first.uppercaseChar().toString()
}
