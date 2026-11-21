package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppTokens(
    // Base materials (DRD §3.1)
    val paperCream: Color = Color(0xFFFFF4DE),
    val paperShade: Color = Color(0xFFF3E2C0),
    val paperLine: Color = Color(0xFFC9B48A),
    val woodLight: Color = Color(0xFFD9A36A),
    val woodMid: Color = Color(0xFFB97D45),
    val woodDark: Color = Color(0xFF8A5628),
    val ink: Color = Color(0xFF2B1A10),
    val feltGreen: Color = Color(0xFF5FA776),
    val feltDark: Color = Color(0xFF48865C),

    // Brand & UI colors (DRD §3.2)
    val brandHoney: Color = Color(0xFFF7B73A),
    val brandHoneyDark: Color = Color(0xFFCC8E1D),
    val brandBerry: Color = Color(0xFFEF5B6B),
    val brandBerryDark: Color = Color(0xFFC13949),
    val brandMint: Color = Color(0xFF4FC08D),
    val brandSky: Color = Color(0xFF4AA8F0),
    val brandLilac: Color = Color(0xFFA989E8),
    val brandTeddy: Color = Color(0xFFC98B53),
    val brandTeddyDark: Color = Color(0xFF9A6034),
    val brandLime: Color = Color(0xFF9BD35A),
    val playerTomato: Color = Color(0xFFFF7A5C),

    // Soft warm highlights (never pure #FFFFFF or #000000 for text)
    val warmHighlight: Color = Color(0xFFFFFAF0),
    val goldSticker: Color = Color(0xFFF7C948),
    val silverSticker: Color = Color(0xFFDDE2E8),
    val bronzeSticker: Color = Color(0xFFE09F67),

    // Cut-gem Diamond currency palette (teal-blue family, Addendum §3.1)
    val diamondTeal: Color = Color(0xFF2EC4C7),
    val diamondTealLight: Color = Color(0xFF8EF3F5),
    val diamondTealDark: Color = Color(0xFF178D96),
)

val DefaultAppTokens = AppTokens()

val LocalAppTokens = staticCompositionLocalOf { DefaultAppTokens }
