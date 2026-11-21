package com.example.data

import com.example.model.AvatarClothingId
import com.example.model.AvatarExpression
import com.example.model.AvatarSkinId
import com.example.model.BoxSkinId
import com.example.model.CosmeticCategory
import com.example.model.CosmeticItem
import com.example.model.CosmeticRarity
import com.example.model.DiamondPack
import com.example.model.EquippedCosmetics
import com.example.model.ExpConfig
import com.example.model.FriendEntry
import com.example.model.FriendRequest
import com.example.model.FriendStatus
import com.example.model.GenderOption
import com.example.model.IdCodeGenerator
import com.example.model.LeaderboardEntry
import com.example.model.LetterFontId
import com.example.model.MockDirectoryPlayer
import com.example.model.MockOnlinePlayer
import com.example.model.MockRoom
import com.example.model.PlayerColorOption
import com.example.model.PlayerPattern
import com.example.model.RoomVisibility
import com.example.model.TeddyAccessory
import com.example.model.TeddyAvatarConfig
import com.example.model.TeddyBodyColor
import com.example.model.UserProfile
import com.example.model.VoiceParticipant
import kotlin.random.Random

class MockGameService : GameService {

    private fun mockAvatar(seed: Int): TeddyAvatarConfig {
        val bodies = TeddyBodyColor.entries
        val accessories = TeddyAccessory.entries
        return TeddyAvatarConfig(
            body = bodies[seed % bodies.size],
            accessory = accessories[(seed * 3) % accessories.size],
            expression = if (seed % 4 == 0) AvatarExpression.CELEBRATING else AvatarExpression.HAPPY,
        )
    }

    private fun mockPattern(seed: Int): PlayerPattern {
        val patterns = PlayerPattern.entries
        return patterns[seed % patterns.size]
    }

    private fun deterministicPlayerId(index: Int): String {
        val chars = IdCodeGenerator.ALLOWED_CHARS
        val rng = Random(7103 + index * 97)
        return "DB-" + buildString(6) {
            repeat(6) { append(chars[rng.nextInt(chars.length)]) }
        }
    }

    private val cosmeticCatalog: List<CosmeticItem> = listOf(
        // Box skins (7 items: 1 default + 6 purchasable)
        CosmeticItem(
            id = BoxSkinId.CLASSIC_PAPER.itemId,
            category = CosmeticCategory.BOX_SKIN,
            name = "Classic Paper",
            description = "Clean tabletop paper fill with crisp ink border.",
            priceDiamonds = 0,
            rarity = CosmeticRarity.DEFAULT,
            isDefault = true,
            boxSkinId = BoxSkinId.CLASSIC_PAPER,
        ),
        CosmeticItem(
            id = BoxSkinId.WOODEN_TILE.itemId,
            category = CosmeticCategory.BOX_SKIN,
            name = "Wooden Tile",
            description = "Warm inlaid plank grain carved inside each claimed box.",
            priceDiamonds = 80,
            rarity = CosmeticRarity.BASIC,
            boxSkinId = BoxSkinId.WOODEN_TILE,
        ),
        CosmeticItem(
            id = BoxSkinId.PAPER_STAMP.itemId,
            category = CosmeticCategory.BOX_SKIN,
            name = "Paper Stamp",
            description = "Perforated postage stamp edges with collector postmark.",
            priceDiamonds = 140,
            rarity = CosmeticRarity.BASIC,
            boxSkinId = BoxSkinId.PAPER_STAMP,
        ),
        CosmeticItem(
            id = BoxSkinId.WOVEN_MAT.itemId,
            category = CosmeticCategory.BOX_SKIN,
            name = "Woven Mat",
            description = "Hand-woven tabletop coaster crisscross texture.",
            priceDiamonds = 240,
            rarity = CosmeticRarity.MID,
            boxSkinId = BoxSkinId.WOVEN_MAT,
        ),
        CosmeticItem(
            id = BoxSkinId.CANDY_WRAPPER.itemId,
            category = CosmeticCategory.BOX_SKIN,
            name = "Candy Wrapper",
            description = "Sweet diagonal ribbon bands with crimped wax corners.",
            priceDiamonds = 340,
            rarity = CosmeticRarity.MID,
            boxSkinId = BoxSkinId.CANDY_WRAPPER,
        ),
        CosmeticItem(
            id = BoxSkinId.PIXEL_BLOCK.itemId,
            category = CosmeticCategory.BOX_SKIN,
            name = "Pixel Block",
            description = "Chunky 8-bit beveled pixel tile with bright corner glint.",
            priceDiamonds = 540,
            rarity = CosmeticRarity.RARE,
            boxSkinId = BoxSkinId.PIXEL_BLOCK,
        ),
        CosmeticItem(
            id = BoxSkinId.ROYAL_ENAMEL.itemId,
            category = CosmeticCategory.BOX_SKIN,
            name = "Royal Enamel",
            description = "Honey-gold inlaid double frame for master tacticians.",
            priceDiamonds = 720,
            rarity = CosmeticRarity.RARE,
            boxSkinId = BoxSkinId.ROYAL_ENAMEL,
        ),

        // Avatar clothes (7 items: 1 default + 6 purchasable)
        CosmeticItem(
            id = AvatarClothingId.NONE.itemId,
            category = CosmeticCategory.AVATAR_CLOTHES,
            name = "No Outfit",
            description = "Classic teddy plush with just your chosen accessory.",
            priceDiamonds = 0,
            rarity = CosmeticRarity.DEFAULT,
            isDefault = true,
            clothingId = AvatarClothingId.NONE,
        ),
        CosmeticItem(
            id = AvatarClothingId.BERRY_SCARF.itemId,
            category = CosmeticCategory.AVATAR_CLOTHES,
            name = "Cozy Scarf",
            description = "Wrapped berry knit scarf with cream winter stripe.",
            priceDiamonds = 70,
            rarity = CosmeticRarity.BASIC,
            clothingId = AvatarClothingId.BERRY_SCARF,
        ),
        CosmeticItem(
            id = AvatarClothingId.STRIPED_SWEATER.itemId,
            category = CosmeticCategory.AVATAR_CLOTHES,
            name = "Striped Sweater",
            description = "Soft sky-and-cream striped pullover for board nights.",
            priceDiamonds = 150,
            rarity = CosmeticRarity.BASIC,
            clothingId = AvatarClothingId.STRIPED_SWEATER,
        ),
        CosmeticItem(
            id = AvatarClothingId.VARSITY_JACKET.itemId,
            category = CosmeticCategory.AVATAR_CLOTHES,
            name = "Varsity Jacket",
            description = "Two-tone honey & berry jacket with cream sleeves.",
            priceDiamonds = 260,
            rarity = CosmeticRarity.MID,
            clothingId = AvatarClothingId.VARSITY_JACKET,
        ),
        CosmeticItem(
            id = AvatarClothingId.CRAFT_OVERALLS.itemId,
            category = CosmeticCategory.AVATAR_CLOTHES,
            name = "Craft Overalls",
            description = "Pocket overalls with brass honey buttons.",
            priceDiamonds = 360,
            rarity = CosmeticRarity.MID,
            clothingId = AvatarClothingId.CRAFT_OVERALLS,
        ),
        CosmeticItem(
            id = AvatarClothingId.STARRY_PAJAMAS.itemId,
            category = CosmeticCategory.AVATAR_CLOTHES,
            name = "Starry Pajamas",
            description = "Lilac bedtime collar dotted with tiny honey stars.",
            priceDiamonds = 520,
            rarity = CosmeticRarity.RARE,
            clothingId = AvatarClothingId.STARRY_PAJAMAS,
        ),
        CosmeticItem(
            id = AvatarClothingId.HERO_CAPE.itemId,
            category = CosmeticCategory.AVATAR_CLOTHES,
            name = "Honey Cape",
            description = "Swooping tabletop champion cape clasped with gold.",
            priceDiamonds = 760,
            rarity = CosmeticRarity.RARE,
            clothingId = AvatarClothingId.HERO_CAPE,
        ),

        // Avatar skins (7 items: 1 default + 6 purchasable)
        CosmeticItem(
            id = AvatarSkinId.CLASSIC_PLUSH.itemId,
            category = CosmeticCategory.AVATAR_SKIN,
            name = "Classic Plush",
            description = "Warm solid plush fur in your chosen teddy color.",
            priceDiamonds = 0,
            rarity = CosmeticRarity.DEFAULT,
            isDefault = true,
            avatarSkinId = AvatarSkinId.CLASSIC_PLUSH,
        ),
        CosmeticItem(
            id = AvatarSkinId.SPOTTED_COOKIE.itemId,
            category = CosmeticCategory.AVATAR_SKIN,
            name = "Spotted Cookie",
            description = "Toasted cream fur dotted with chocolate chip spots.",
            priceDiamonds = 90,
            rarity = CosmeticRarity.BASIC,
            avatarSkinId = AvatarSkinId.SPOTTED_COOKIE,
        ),
        CosmeticItem(
            id = AvatarSkinId.TIGER_STRIPED.itemId,
            category = CosmeticCategory.AVATAR_SKIN,
            name = "Tiger Striped",
            description = "Amber fur with bold espresso tabby stripes.",
            priceDiamonds = 150,
            rarity = CosmeticRarity.BASIC,
            avatarSkinId = AvatarSkinId.TIGER_STRIPED,
        ),
        CosmeticItem(
            id = AvatarSkinId.PATCHWORK_QUILT.itemId,
            category = CosmeticCategory.AVATAR_SKIN,
            name = "Patchwork Quilt",
            description = "Stitched mint, sky, and honey craft plush panels.",
            priceDiamonds = 280,
            rarity = CosmeticRarity.MID,
            avatarSkinId = AvatarSkinId.PATCHWORK_QUILT,
        ),
        CosmeticItem(
            id = AvatarSkinId.TWO_TONE_PANDA.itemId,
            category = CosmeticCategory.AVATAR_SKIN,
            name = "Two-Tone Panda",
            description = "Cream plush face with dark cocoa ears and eye patches.",
            priceDiamonds = 390,
            rarity = CosmeticRarity.MID,
            avatarSkinId = AvatarSkinId.TWO_TONE_PANDA,
        ),
        CosmeticItem(
            id = AvatarSkinId.FLUFFY_CLOUD.itemId,
            category = CosmeticCategory.AVATAR_SKIN,
            name = "Fluffy Cloud",
            description = "Poofy scalloped wool curls with rosy sky tuft.",
            priceDiamonds = 580,
            rarity = CosmeticRarity.RARE,
            avatarSkinId = AvatarSkinId.FLUFFY_CLOUD,
        ),
        CosmeticItem(
            id = AvatarSkinId.STARLIGHT_PLUSH.itemId,
            category = CosmeticCategory.AVATAR_SKIN,
            name = "Starlight Plush",
            description = "Twilight lilac fur sprinkled with tiny honey starlight.",
            priceDiamonds = 780,
            rarity = CosmeticRarity.RARE,
            avatarSkinId = AvatarSkinId.STARLIGHT_PLUSH,
        ),

        // Letter fonts (7 items: 1 default + 6 purchasable)
        CosmeticItem(
            id = LetterFontId.BOLD_BLOCK.itemId,
            category = CosmeticCategory.LETTER_FONT,
            name = "Bold Block",
            description = "Classic chunky display letter on claimed boxes.",
            priceDiamonds = 0,
            rarity = CosmeticRarity.DEFAULT,
            isDefault = true,
            letterFontId = LetterFontId.BOLD_BLOCK,
        ),
        CosmeticItem(
            id = LetterFontId.ROUNDED_BUBBLE.itemId,
            category = CosmeticCategory.LETTER_FONT,
            name = "Rounded Bubble",
            description = "Soft friendly rounded lettering for your initials.",
            priceDiamonds = 65,
            rarity = CosmeticRarity.BASIC,
            letterFontId = LetterFontId.ROUNDED_BUBBLE,
        ),
        CosmeticItem(
            id = LetterFontId.STICKER_OUTLINE.itemId,
            category = CosmeticCategory.LETTER_FONT,
            name = "Sticker Outline",
            description = "Thick paper-cut outer rim around every claimed letter.",
            priceDiamonds = 135,
            rarity = CosmeticRarity.BASIC,
            letterFontId = LetterFontId.STICKER_OUTLINE,
        ),
        CosmeticItem(
            id = LetterFontId.SHADOW_RIBBON.itemId,
            category = CosmeticCategory.LETTER_FONT,
            name = "Shadow Ribbon",
            description = "Crisp offset honey drop shadow behind box initials.",
            priceDiamonds = 250,
            rarity = CosmeticRarity.MID,
            letterFontId = LetterFontId.SHADOW_RIBBON,
        ),
        CosmeticItem(
            id = LetterFontId.HANDWRITTEN_MARKER.itemId,
            category = CosmeticCategory.LETTER_FONT,
            name = "Marker Script",
            description = "Lively slanted felt-tip marker initials.",
            priceDiamonds = 350,
            rarity = CosmeticRarity.MID,
            letterFontId = LetterFontId.HANDWRITTEN_MARKER,
        ),
        CosmeticItem(
            id = LetterFontId.WOODBLOCK_SERIF.itemId,
            category = CosmeticCategory.LETTER_FONT,
            name = "Woodblock Serif",
            description = "Carved board-game press serif monogram.",
            priceDiamonds = 550,
            rarity = CosmeticRarity.RARE,
            letterFontId = LetterFontId.WOODBLOCK_SERIF,
        ),
        CosmeticItem(
            id = LetterFontId.TYPEWRITER_STAMP.itemId,
            category = CosmeticCategory.LETTER_FONT,
            name = "Typewriter Stamp",
            description = "Crisp fixed-width ink stamp initials.",
            priceDiamonds = 680,
            rarity = CosmeticRarity.RARE,
            letterFontId = LetterFontId.TYPEWRITER_STAMP,
        ),
    )

    private val diamondPacks: List<DiamondPack> = listOf(
        DiamondPack(
            id = "pack_small",
            name = "Small Pouch",
            diamonds = 100,
            priceInr = 29,
            badge = null,
        ),
        DiamondPack(
            id = "pack_medium",
            name = "Medium Chest",
            diamonds = 500,
            priceInr = 99,
            badge = "Popular",
        ),
        DiamondPack(
            id = "pack_large",
            name = "Large Vault",
            diamonds = 1200,
            priceInr = 199,
            badge = "Best value",
        ),
    )

    private val mockLeaderboardPlayers: List<LeaderboardEntry> = listOf(
        LeaderboardEntry("p01", "DB-4K7Q2M", "Aarav Mehta", "AM", GenderOption.MALE, PlayerColorOption.TOMATO, mockPattern(0), mockAvatar(1), EquippedCosmetics(BoxSkinId.ROYAL_ENAMEL, AvatarClothingId.HERO_CAPE, AvatarSkinId.CLASSIC_PLUSH, LetterFontId.BOLD_BLOCK), 2145, 135, 294),
        LeaderboardEntry("p02", "DB-9W3P8N", "Nadia Lin", "NL", GenderOption.FEMALE, PlayerColorOption.SKY, mockPattern(1), mockAvatar(2), EquippedCosmetics(BoxSkinId.PIXEL_BLOCK, AvatarClothingId.VARSITY_JACKET, AvatarSkinId.TWO_TONE_PANDA, LetterFontId.ROUNDED_BUBBLE), 2060, 120, 279),
        LeaderboardEntry("p03", "DB-2X6R5T", "Soren Lindqvist", "SL", GenderOption.MALE, PlayerColorOption.MINT, mockPattern(2), mockAvatar(3), EquippedCosmetics(BoxSkinId.WOODEN_TILE, AvatarClothingId.STRIPED_SWEATER, AvatarSkinId.CLASSIC_PLUSH, LetterFontId.WOODBLOCK_SERIF), 1985, 110, 261),
        LeaderboardEntry("p04", "DB-7H8M4C", "Priya Nair", "PN", GenderOption.FEMALE, PlayerColorOption.LILAC, mockPattern(3), mockAvatar(4), EquippedCosmetics(BoxSkinId.CANDY_WRAPPER, AvatarClothingId.STARRY_PAJAMAS, AvatarSkinId.STARLIGHT_PLUSH, LetterFontId.STICKER_OUTLINE), 1890, 95, 248),
        LeaderboardEntry("p05", "DB-5J2V9F", "Mateo Alvarez", "MA", GenderOption.MALE, PlayerColorOption.HONEY, mockPattern(0), mockAvatar(5), EquippedCosmetics(BoxSkinId.WOVEN_MAT, AvatarClothingId.CRAFT_OVERALLS, AvatarSkinId.TIGER_STRIPED, LetterFontId.SHADOW_RIBBON), 1775, 115, 232),
        LeaderboardEntry("p06", "DB-3B9N7K", "Claire Moreau", "CM", GenderOption.FEMALE, PlayerColorOption.LIME, mockPattern(1), mockAvatar(6), EquippedCosmetics(BoxSkinId.PAPER_STAMP, AvatarClothingId.BERRY_SCARF, AvatarSkinId.SPOTTED_COOKIE, LetterFontId.HANDWRITTEN_MARKER), 1690, 85, 219),
        LeaderboardEntry("p07", "DB-8G4Y6Z", "Devon Brooks", "DB", GenderOption.NON_BINARY, PlayerColorOption.TOMATO, mockPattern(2), mockAvatar(7), EquippedCosmetics(BoxSkinId.PIXEL_BLOCK, AvatarClothingId.NONE, AvatarSkinId.PATCHWORK_QUILT, LetterFontId.TYPEWRITER_STAMP), 1580, 105, 204),
        LeaderboardEntry("p08", "DB-6D5L2W", "Kenji Takahashi", "KT", GenderOption.MALE, PlayerColorOption.SKY, mockPattern(3), mockAvatar(8), EquippedCosmetics(), 1495, 75, 192),
        LeaderboardEntry("p09", deterministicPlayerId(9), "Ananya Rao", "AR", GenderOption.FEMALE, PlayerColorOption.HONEY, mockPattern(0), mockAvatar(9), EquippedCosmetics(avatarClothing = AvatarClothingId.BERRY_SCARF), 1420, 90, 181),
        LeaderboardEntry("p10", deterministicPlayerId(10), "Lucas Weber", "LW", GenderOption.MALE, PlayerColorOption.TEDDY, mockPattern(1), mockAvatar(10), EquippedCosmetics(avatarSkin = AvatarSkinId.TIGER_STRIPED), 1345, 65, 174),
        LeaderboardEntry("p11", deterministicPlayerId(11), "Hannah Kim", "HK", GenderOption.FEMALE, PlayerColorOption.BERRY, mockPattern(2), mockAvatar(11), EquippedCosmetics(avatarClothing = AvatarClothingId.STRIPED_SWEATER), 1280, 80, 162),
        LeaderboardEntry("p12", deterministicPlayerId(12), "Rohan Kulkarni", "RK", GenderOption.MALE, PlayerColorOption.MINT, mockPattern(3), mockAvatar(12), EquippedCosmetics(), 1190, 70, 151),
        LeaderboardEntry("p13", deterministicPlayerId(13), "Elena Petrova", "EP", GenderOption.FEMALE, PlayerColorOption.TOMATO, mockPattern(0), mockAvatar(13), EquippedCosmetics(avatarSkin = AvatarSkinId.SPOTTED_COOKIE), 1125, 60, 144),
        LeaderboardEntry("p14", deterministicPlayerId(14), "Tariq Mansour", "TM", GenderOption.MALE, PlayerColorOption.SKY, mockPattern(1), mockAvatar(14), EquippedCosmetics(), 1050, 55, 136),
        LeaderboardEntry("p15", deterministicPlayerId(15), "Samira Haddad", "SH", GenderOption.FEMALE, PlayerColorOption.LILAC, mockPattern(2), mockAvatar(15), EquippedCosmetics(avatarClothing = AvatarClothingId.VARSITY_JACKET), 985, 85, 127),
        LeaderboardEntry("p16", deterministicPlayerId(16), "Noah Gallagher", "NG", GenderOption.MALE, PlayerColorOption.TEDDY, mockPattern(3), mockAvatar(16), EquippedCosmetics(), 920, 50, 118),
        LeaderboardEntry("p17", deterministicPlayerId(17), "Yuki Sato", "YS", GenderOption.PREFER_NOT_TO_SAY, PlayerColorOption.HONEY, mockPattern(0), mockAvatar(17), EquippedCosmetics(avatarSkin = AvatarSkinId.FLUFFY_CLOUD), 865, 65, 109),
        LeaderboardEntry("p18", deterministicPlayerId(18), "Maya Deshmukh", "MD", GenderOption.FEMALE, PlayerColorOption.LIME, mockPattern(1), mockAvatar(18), EquippedCosmetics(avatarClothing = AvatarClothingId.CRAFT_OVERALLS), 810, 45, 102),
        LeaderboardEntry("p19", deterministicPlayerId(19), "Julian Vance", "JV", GenderOption.MALE, PlayerColorOption.TOMATO, mockPattern(2), mockAvatar(19), EquippedCosmetics(), 745, 55, 95),
        LeaderboardEntry("p20", deterministicPlayerId(20), "Freya Nielsen", "FN", GenderOption.FEMALE, PlayerColorOption.SKY, mockPattern(3), mockAvatar(20), EquippedCosmetics(), 690, 40, 88),
        LeaderboardEntry("p21", deterministicPlayerId(21), "Kiran Joshi", "KJ", GenderOption.NON_BINARY, PlayerColorOption.LILAC, mockPattern(0), mockAvatar(21), EquippedCosmetics(avatarSkin = AvatarSkinId.STARLIGHT_PLUSH), 640, 70, 81),
        LeaderboardEntry("p22", deterministicPlayerId(22), "Owen Sterling", "OS", GenderOption.MALE, PlayerColorOption.TEDDY, mockPattern(1), mockAvatar(22), EquippedCosmetics(), 595, 35, 76),
        LeaderboardEntry("p23", deterministicPlayerId(23), "Leila Benali", "LB", GenderOption.FEMALE, PlayerColorOption.HONEY, mockPattern(2), mockAvatar(23), EquippedCosmetics(), 540, 50, 69),
        LeaderboardEntry("p24", deterministicPlayerId(24), "Vikram Kapoor", "VK", GenderOption.MALE, PlayerColorOption.MINT, mockPattern(3), mockAvatar(24), EquippedCosmetics(), 495, 45, 63),
        LeaderboardEntry("p25", deterministicPlayerId(25), "Tessa Clarke", "TC", GenderOption.FEMALE, PlayerColorOption.BERRY, mockPattern(0), mockAvatar(25), EquippedCosmetics(), 450, 30, 57),
        LeaderboardEntry("p26", deterministicPlayerId(26), "Darius Cole", "DC", GenderOption.MALE, PlayerColorOption.SKY, mockPattern(1), mockAvatar(26), EquippedCosmetics(), 405, 40, 51),
        LeaderboardEntry("p27", deterministicPlayerId(27), "Noor Al-Sayed", "NA", GenderOption.FEMALE, PlayerColorOption.LILAC, mockPattern(2), mockAvatar(27), EquippedCosmetics(), 360, 25, 45),
        LeaderboardEntry("p28", deterministicPlayerId(28), "Felix Wagner", "FW", GenderOption.MALE, PlayerColorOption.TEDDY, mockPattern(3), mockAvatar(28), EquippedCosmetics(), 325, 35, 41),
        LeaderboardEntry("p29", deterministicPlayerId(29), "Zara Fernandez", "ZF", GenderOption.FEMALE, PlayerColorOption.HONEY, mockPattern(0), mockAvatar(29), EquippedCosmetics(), 290, 30, 36),
        LeaderboardEntry("p30", deterministicPlayerId(30), "Arjun Bhat", "AB", GenderOption.MALE, PlayerColorOption.LIME, mockPattern(1), mockAvatar(30), EquippedCosmetics(), 255, 25, 31),
        LeaderboardEntry("p31", deterministicPlayerId(31), "Mina Park", "MP", GenderOption.FEMALE, PlayerColorOption.TOMATO, mockPattern(2), mockAvatar(31), EquippedCosmetics(), 220, 20, 27),
        LeaderboardEntry("p32", deterministicPlayerId(32), "Rowan Ellis", "RE", GenderOption.NON_BINARY, PlayerColorOption.SKY, mockPattern(3), mockAvatar(32), EquippedCosmetics(), 190, 25, 23),
        LeaderboardEntry("p33", deterministicPlayerId(33), "Siddharth Sen", "SS", GenderOption.MALE, PlayerColorOption.LILAC, mockPattern(0), mockAvatar(33), EquippedCosmetics(), 160, 15, 19),
        LeaderboardEntry("p34", deterministicPlayerId(34), "Camille Laurent", "CL", GenderOption.FEMALE, PlayerColorOption.MINT, mockPattern(1), mockAvatar(34), EquippedCosmetics(), 135, 20, 16),
        LeaderboardEntry("p35", deterministicPlayerId(35), "Elias Holm", "EH", GenderOption.PREFER_NOT_TO_SAY, PlayerColorOption.HONEY, mockPattern(2), mockAvatar(35), EquippedCosmetics(), 110, 15, 13),
        LeaderboardEntry("p36", deterministicPlayerId(36), "Diya Menon", "DM", GenderOption.FEMALE, PlayerColorOption.LIME, mockPattern(3), mockAvatar(36), EquippedCosmetics(), 85, 15, 10),
        LeaderboardEntry("p37", deterministicPlayerId(37), "Gavin Reed", "GR", GenderOption.MALE, PlayerColorOption.TOMATO, mockPattern(0), mockAvatar(37), EquippedCosmetics(), 60, 10, 7),
        LeaderboardEntry("p38", deterministicPlayerId(38), "Talia Cohen", "TC", GenderOption.FEMALE, PlayerColorOption.SKY, mockPattern(1), mockAvatar(38), EquippedCosmetics(), 40, 10, 5),
        LeaderboardEntry("p39", deterministicPlayerId(39), "Kabir Verma", "KV", GenderOption.MALE, PlayerColorOption.TEDDY, mockPattern(2), mockAvatar(39), EquippedCosmetics(), 25, 10, 3),
        LeaderboardEntry("p40", deterministicPlayerId(40), "Amina Okafor", "AO", GenderOption.FEMALE, PlayerColorOption.BERRY, mockPattern(3), mockAvatar(40), EquippedCosmetics(), 10, 5, 1),
    )

    private val directoryPlayers: List<MockDirectoryPlayer> = mockLeaderboardPlayers.take(32).mapIndexed { idx, entry ->
        val status = when {
            idx in listOf(1, 2, 4, 6, 8, 10, 12, 17, 20, 25) -> FriendStatus.ONLINE
            idx in listOf(0, 3, 7, 13, 19) -> FriendStatus.IN_MATCH
            else -> FriendStatus.OFFLINE
        }
        val lastSeen = when (idx % 5) {
            0 -> "12m ago"
            1 -> "1h ago"
            2 -> "3h ago"
            3 -> "Yesterday"
            else -> "2d ago"
        }
        MockDirectoryPlayer(
            playerId = entry.playerId,
            name = entry.name,
            initials = entry.initials,
            gender = entry.gender,
            color = entry.color,
            pattern = entry.pattern,
            avatarConfig = entry.avatarConfig,
            equippedCosmetics = entry.equippedCosmetics,
            level = ExpConfig.levelForExp(entry.totalExp),
            status = status,
            lastSeenLabel = lastSeen,
        )
    }

    private val openRooms: List<MockRoom> = listOf(
        MockRoom("r1", "K7QX2M", "DB-9W3P8N", "Nadia Lin", "NL", GenderOption.FEMALE, PlayerColorOption.SKY, PlayerPattern.DOTS, mockAvatar(2), EquippedCosmetics(BoxSkinId.PIXEL_BLOCK, AvatarClothingId.VARSITY_JACKET, AvatarSkinId.TWO_TONE_PANDA), 21, 2060, "Waiting for opponent", RoomVisibility.PUBLIC),
        MockRoom("r2", "M4RP8N", deterministicPlayerId(21), "Kiran Joshi", "KJ", GenderOption.NON_BINARY, PlayerColorOption.LILAC, PlayerPattern.STARS, mockAvatar(21), EquippedCosmetics(avatarSkin = AvatarSkinId.STARLIGHT_PLUSH), 7, 640, "Waiting for opponent", RoomVisibility.PUBLIC),
        MockRoom("r3", "T9WC3H", "DB-4K7Q2M", "Aarav Mehta", "AM", GenderOption.MALE, PlayerColorOption.TOMATO, PlayerPattern.STRIPES, mockAvatar(1), EquippedCosmetics(BoxSkinId.ROYAL_ENAMEL, AvatarClothingId.HERO_CAPE), 22, 2145, "Waiting for opponent", RoomVisibility.PUBLIC),
        MockRoom("r4", "B6YL5V", deterministicPlayerId(15), "Samira Haddad", "SH", GenderOption.FEMALE, PlayerColorOption.HONEY, PlayerPattern.CHECKS, mockAvatar(15), EquippedCosmetics(avatarClothing = AvatarClothingId.VARSITY_JACKET), 10, 985, "Waiting for opponent", RoomVisibility.PUBLIC),
        MockRoom("r5", "P2ZN7D", deterministicPlayerId(10), "Lucas Weber", "LW", GenderOption.MALE, PlayerColorOption.MINT, PlayerPattern.DOTS, mockAvatar(10), EquippedCosmetics(avatarSkin = AvatarSkinId.TIGER_STRIPED), 14, 1345, "Waiting for opponent", RoomVisibility.PUBLIC),
        MockRoom("r6", "J8FK4G", deterministicPlayerId(18), "Maya Deshmukh", "MD", GenderOption.FEMALE, PlayerColorOption.BERRY, PlayerPattern.STARS, mockAvatar(18), EquippedCosmetics(avatarClothing = AvatarClothingId.CRAFT_OVERALLS), 9, 810, "Waiting for opponent", RoomVisibility.PUBLIC),
    )

    private val onlinePlayers: List<MockOnlinePlayer> = listOf(
        MockOnlinePlayer("op1", "DB-9W3P8N", "Nadia Lin", "NL", GenderOption.FEMALE, PlayerColorOption.SKY, mockAvatar(2), EquippedCosmetics(avatarClothing = AvatarClothingId.VARSITY_JACKET, avatarSkin = AvatarSkinId.TWO_TONE_PANDA), 21, "In lobby"),
        MockOnlinePlayer("op2", deterministicPlayerId(21), "Kiran Joshi", "KJ", GenderOption.NON_BINARY, PlayerColorOption.LILAC, mockAvatar(21), EquippedCosmetics(avatarSkin = AvatarSkinId.STARLIGHT_PLUSH), 7, "In lobby"),
        MockOnlinePlayer("op3", "DB-4K7Q2M", "Aarav Mehta", "AM", GenderOption.MALE, PlayerColorOption.TOMATO, mockAvatar(1), EquippedCosmetics(avatarClothing = AvatarClothingId.HERO_CAPE), 22, "Hosting T9WC3H"),
        MockOnlinePlayer("op4", "DB-7H8M4C", "Priya Nair", "PN", GenderOption.FEMALE, PlayerColorOption.LILAC, mockAvatar(4), EquippedCosmetics(avatarClothing = AvatarClothingId.STARRY_PAJAMAS, avatarSkin = AvatarSkinId.STARLIGHT_PLUSH), 19, "In match"),
        MockOnlinePlayer("op5", "DB-2X6R5T", "Soren Lindqvist", "SL", GenderOption.MALE, PlayerColorOption.MINT, mockAvatar(3), EquippedCosmetics(avatarClothing = AvatarClothingId.STRIPED_SWEATER), 20, "In lobby"),
        MockOnlinePlayer("op6", deterministicPlayerId(17), "Yuki Sato", "YS", GenderOption.PREFER_NOT_TO_SAY, PlayerColorOption.HONEY, mockAvatar(17), EquippedCosmetics(avatarSkin = AvatarSkinId.FLUFFY_CLOUD), 9, "In lobby"),
        MockOnlinePlayer("op7", deterministicPlayerId(36), "Diya Menon", "DM", GenderOption.FEMALE, PlayerColorOption.LIME, mockAvatar(36), EquippedCosmetics(), 1, "In lobby"),
        MockOnlinePlayer("op8", deterministicPlayerId(14), "Tariq Mansour", "TM", GenderOption.MALE, PlayerColorOption.SKY, mockAvatar(14), EquippedCosmetics(), 11, "In match"),
    )

    private val chatReplies: List<String> = listOf(
        "Good line! Let me think a second.",
        "Careful around that corner.",
        "Nice setup for the double!",
        "Board 2 always flips the pace.",
        "That chain is going to be close.",
        "Well played!",
        "Almost missed that edge.",
        "Level 3 decides everything.",
        "Holding the center on this one.",
        "Good luck finding the hidden diamonds!",
        "Smart sacrifice there.",
        "Clean move!",
    )

    override fun getOpenRooms(): List<MockRoom> = openRooms

    override fun getOnlinePlayers(): List<MockOnlinePlayer> = onlinePlayers

    override fun getLeaderboard(
        scope: LeaderboardScope,
        currentUser: UserProfile?,
    ): List<LeaderboardEntry> {
        val entries = mockLeaderboardPlayers.toMutableList()
        if (currentUser != null) {
            val userWeeklyExp = currentUser.totalExp.coerceAtMost(140)
            entries.add(
                LeaderboardEntry(
                    id = "local_user",
                    playerId = currentUser.playerId,
                    name = currentUser.displayName,
                    initials = currentUser.initials,
                    gender = currentUser.gender,
                    color = currentUser.colorOption,
                    pattern = currentUser.pattern,
                    avatarConfig = currentUser.avatarConfig,
                    equippedCosmetics = currentUser.equippedCosmetics,
                    totalExp = currentUser.totalExp,
                    weeklyExp = userWeeklyExp,
                    matchesWon = currentUser.onlineMatchesWon,
                    isCurrentUser = true,
                )
            )
        }

        val comparator = Comparator<LeaderboardEntry> { a, b ->
            val expA = if (scope == LeaderboardScope.WEEKLY) a.weeklyExp else a.totalExp
            val expB = if (scope == LeaderboardScope.WEEKLY) b.weeklyExp else b.totalExp
            when {
                expA != expB -> expB.compareTo(expA)
                a.matchesWon != b.matchesWon -> b.matchesWon.compareTo(a.matchesWon)
                else -> a.name.lowercase().compareTo(b.name.lowercase())
            }
        }
        return entries.sortedWith(comparator)
    }

    override fun getChatReplyPool(): List<String> = chatReplies

    override fun pickRandomChatReply(): String = chatReplies[Random.nextInt(chatReplies.size)]

    override fun createInitialVoiceParticipants(
        opponentName: String,
        opponentInitials: String,
        opponentColor: PlayerColorOption,
        localUser: UserProfile?,
    ): List<VoiceParticipant> {
        val localName = localUser?.displayName ?: "You"
        val localInitials = localUser?.initials ?: "YO"
        val localColor = localUser?.colorOption ?: PlayerColorOption.TOMATO
        val localAvatar = localUser?.avatarConfig ?: TeddyAvatarConfig()
        val localCosmetics = localUser?.equippedCosmetics ?: EquippedCosmetics()
        return listOf(
            VoiceParticipant(
                id = "voice_local",
                name = localName,
                initials = localInitials,
                color = localColor,
                avatarConfig = localAvatar,
                equippedCosmetics = localCosmetics,
                isSpeaking = false,
                isMuted = false,
                isLocalUser = true,
            ),
            VoiceParticipant(
                id = "voice_opp",
                name = opponentName,
                initials = opponentInitials,
                color = opponentColor,
                avatarConfig = TeddyAvatarConfig(TeddyBodyColor.SKY, TeddyAccessory.HEADPHONES),
                isSpeaking = true,
                isMuted = false,
                isLocalUser = false,
            ),
            VoiceParticipant(
                id = "voice_spec",
                name = "Soren Lindqvist",
                initials = "SL",
                color = PlayerColorOption.MINT,
                avatarConfig = TeddyAvatarConfig(TeddyBodyColor.CREAM, TeddyAccessory.BEANIE),
                isSpeaking = false,
                isMuted = true,
                isLocalUser = false,
            ),
        )
    }

    override fun generateRoomCode(): String = IdCodeGenerator.generateRoomCode()

    override fun pickQuickMatchOpponent(excludeColor: PlayerColorOption): MockRoom {
        val filtered = openRooms.filter { it.hostColor != excludeColor }
        val pool = filtered.ifEmpty { openRooms }
        return pool[Random.nextInt(pool.size)]
    }

    override fun getCosmeticCatalog(): List<CosmeticItem> = cosmeticCatalog

    override fun getDefaultOwnedCosmeticIds(): Set<String> =
        cosmeticCatalog.filter { it.isDefault }.map { it.id }.toSet()

    override fun getDiamondPacks(): List<DiamondPack> = diamondPacks

    override fun getDirectoryPlayers(): List<MockDirectoryPlayer> = directoryPlayers

    override fun findPlayerById(rawId: String): MockDirectoryPlayer? {
        val cleaned = rawId.trim().uppercase()
        if (cleaned.isBlank()) return null
        val normalized = if (cleaned.startsWith("DB-")) cleaned else "DB-$cleaned"
        return directoryPlayers.firstOrNull { it.playerId.equals(normalized, ignoreCase = true) }
    }

    override fun findRoomByCode(rawCode: String): MockRoom? {
        val cleaned = rawCode.trim().uppercase()
        if (cleaned.isBlank()) return null
        return openRooms.firstOrNull { it.code.equals(cleaned, ignoreCase = true) }
    }

    override fun getInitialFriends(): List<FriendEntry> {
        return listOf(directoryPlayers[1], directoryPlayers[2], directoryPlayers[4], directoryPlayers[7], directoryPlayers[11]).map { p ->
            FriendEntry(
                playerId = p.playerId,
                name = p.name,
                initials = p.initials,
                gender = p.gender,
                color = p.color,
                pattern = p.pattern,
                avatarConfig = p.avatarConfig,
                equippedCosmetics = p.equippedCosmetics,
                level = p.level,
                status = p.status,
                lastSeenLabel = p.lastSeenLabel,
            )
        }
    }

    override fun getInitialIncomingRequests(): List<FriendRequest> {
        return listOf(directoryPlayers[5], directoryPlayers[8]).mapIndexed { i, p ->
            FriendRequest(
                id = "req_seed_$i",
                playerId = p.playerId,
                name = p.name,
                initials = p.initials,
                gender = p.gender,
                color = p.color,
                pattern = p.pattern,
                avatarConfig = p.avatarConfig,
                equippedCosmetics = p.equippedCosmetics,
                level = p.level,
                isIncoming = true,
            )
        }
    }
}
