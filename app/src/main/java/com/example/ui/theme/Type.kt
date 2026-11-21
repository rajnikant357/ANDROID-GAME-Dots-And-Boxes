package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val LilitaOneFontFamily = FontFamily(
    Font(R.font.lilita_one, FontWeight.Normal),
)

val FredokaFontFamily = FontFamily(
    Font(R.font.fredoka, FontWeight.Normal),
    Font(R.font.fredoka, FontWeight.SemiBold),
    Font(R.font.fredoka, FontWeight.Bold),
)

val NunitoFontFamily = FontFamily(
    Font(R.font.nunito, FontWeight.Normal),
    Font(R.font.nunito, FontWeight.Medium),
    Font(R.font.nunito, FontWeight.SemiBold),
    Font(R.font.nunito, FontWeight.Bold),
)

val LexendFontFamily = FontFamily(
    Font(R.font.lexend, FontWeight.Normal),
    Font(R.font.lexend, FontWeight.Medium),
    Font(R.font.lexend, FontWeight.SemiBold),
    Font(R.font.lexend, FontWeight.Bold),
)

// Type scale strictly matching DRD §5.2: 56 / 40 / 28 / 22 / 17 / 15 / 12 sp
fun buildAppTypography(dyslexiaFriendly: Boolean = false): Typography {
    val bodyFamily = if (dyslexiaFriendly) LexendFontFamily else NunitoFontFamily
    val bodyLargeLineHeight = if (dyslexiaFriendly) 27.sp else 23.sp
    val bodyMediumLineHeight = if (dyslexiaFriendly) 24.sp else 21.sp

    return Typography(
        displayLarge = TextStyle(
            fontFamily = LilitaOneFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 56.sp,
            lineHeight = 60.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = LilitaOneFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 40.sp,
            lineHeight = 44.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = LilitaOneFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 32.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            lineHeight = bodyLargeLineHeight,
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = bodyMediumLineHeight,
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 19.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
    )
}

val LilitaNumberStyle = TextStyle(
    fontFamily = LilitaOneFontFamily,
    fontWeight = FontWeight.Normal,
)
