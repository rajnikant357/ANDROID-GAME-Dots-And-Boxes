package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// DRD §6.2: Cards/sheets 24dp, Inputs 16dp, Pills 999dp, Board 28dp
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(999.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
