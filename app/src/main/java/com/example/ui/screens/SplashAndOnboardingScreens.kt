package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AvatarExpression
import com.example.model.GenderOption
import com.example.model.PlayerColorOption
import com.example.model.PlayerPattern
import com.example.model.TeddyAccessory
import com.example.model.TeddyAvatarConfig
import com.example.model.TeddyBodyColor
import com.example.model.deriveInitialsFromName
import com.example.ui.components.ChunkyDisplayText
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.GenderBadge
import com.example.ui.components.PaperBackground
import com.example.ui.components.PatternPickerRow
import com.example.ui.components.StickerBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioChip
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.StudioTextField
import com.example.ui.components.TeddyAvatar
import com.example.ui.components.TeddyBiscuit
import com.example.ui.components.TeddyExpression
import com.example.ui.components.TeddyPose
import com.example.ui.components.WoodBackground
import com.example.ui.components.drawPlayerPattern
import com.example.ui.theme.LilitaNumberStyle
import com.example.ui.theme.StudioTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoaded: Boolean,
    hasProfile: Boolean,
    reducedMotion: Boolean,
    onFinished: (hasProfile: Boolean) -> Unit,
) {
    val tokens = StudioTheme.tokens

    LaunchedEffect(isLoaded, reducedMotion) {
        if (isLoaded) {
            delay(if (reducedMotion) 250L else 1350L)
            onFinished(hasProfile)
        }
    }

    WoodBackground(
        showKnot = true,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .rotate(-2f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(tokens.paperCream)
                        .border(4.dp, tokens.ink, RoundedCornerShape(24.dp))
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TeddyBiscuit(
                            pose = TeddyPose.WAVING,
                            expression = TeddyExpression.HAPPY,
                            size = 132.dp,
                            reducedMotion = reducedMotion,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ChunkyDisplayText(
                            text = "Dots & Boxes",
                            style = MaterialTheme.typography.displayMedium,
                            fillColor = tokens.paperCream,
                            shadowColor = tokens.brandHoney,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StickerBadge(
                            text = "TABLETOP EDITION",
                            backgroundColor = tokens.brandMint,
                            rotationDegrees = 2f,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onCreateProfile: () -> Unit,
    onPlayOfflineDirect: () -> Unit,
) {
    val tokens = StudioTheme.tokens
    PaperBackground(
        showCreaseLines = true,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(20.dp),
        ) {
            // Pencil doodle dashed border around welcome sheet (DRD §9)
            Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                drawRoundRect(
                    color = tokens.paperLine,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx()), 0f),
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                StickerBadge(
                    text = "HELLO FRIEND",
                    backgroundColor = tokens.brandHoney,
                    rotationDegrees = -3f,
                )
                Spacer(modifier = Modifier.height(10.dp))
                TeddyBiscuit(
                    pose = TeddyPose.WAVING,
                    expression = TeddyExpression.HAPPY,
                    size = 128.dp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                ChunkyDisplayText(
                    text = stringResource(R.string.welcome_headline),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                    fillColor = tokens.paperCream,
                    shadowColor = tokens.brandHoney,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.welcome_subtitle_line1),
                    style = MaterialTheme.typography.headlineSmall,
                    color = tokens.ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.welcome_subtitle_line2),
                    style = MaterialTheme.typography.bodyLarge,
                    color = tokens.ink.copy(alpha = 0.80f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                StudioPrimaryButton(
                    text = stringResource(R.string.create_profile_cta),
                    onClick = onCreateProfile,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "welcome_create_profile_button",
                )
                Spacer(modifier = Modifier.height(12.dp))
                StudioSecondaryButton(
                    text = stringResource(R.string.offline_direct_cta),
                    onClick = onPlayOfflineDirect,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "welcome_offline_direct_button",
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProfileScreen(
    initialName: String = "",
    initialInitials: String = "",
    initialGender: GenderOption = GenderOption.PREFER_NOT_TO_SAY,
    initialColor: PlayerColorOption = PlayerColorOption.TOMATO,
    initialPattern: PlayerPattern = PlayerPattern.STRIPES,
    initialAvatar: TeddyAvatarConfig = TeddyAvatarConfig(),
    isEditMode: Boolean = false,
    onBack: (() -> Unit)? = null,
    onSaveProfile: (
        name: String,
        initials: String,
        gender: GenderOption,
        color: PlayerColorOption,
        pattern: PlayerPattern,
        avatar: TeddyAvatarConfig,
    ) -> Unit,
) {
    val tokens = StudioTheme.tokens
    var displayName by remember { mutableStateOf(initialName) }
    var gender by remember { mutableStateOf(initialGender) }
    var colorOption by remember { mutableStateOf(initialColor) }
    var pattern by remember { mutableStateOf(initialPattern) }
    var avatarBody by remember { mutableStateOf(initialAvatar.body) }
    var avatarAccessory by remember { mutableStateOf(initialAvatar.accessory) }
    var avatarExpression by remember { mutableStateOf(initialAvatar.expression) }
    var showValidation by remember { mutableStateOf(false) }

    val trimmedName = displayName.trim()
    val derivedInitial = deriveInitialsFromName(trimmedName)
    val nameError = when {
        !showValidation && displayName.isEmpty() -> null
        trimmedName.length < 3 -> "Enter 3 to 16 characters."
        trimmedName.length > 16 -> "Keep display name under 16 characters."
        derivedInitial.isEmpty() -> "Include at least one letter or number."
        else -> null
    }
    val isFormValid = trimmedName.length in 3..16 && derivedInitial.isNotEmpty()
    val currentAvatar = TeddyAvatarConfig(avatarBody, avatarAccessory, avatarExpression)

    // DRD §16 Step 5: Biscuit reacts to typing (thinking while typing/incomplete, happy when valid)
    val biscuitExpression = if (isFormValid) TeddyExpression.HAPPY else TeddyExpression.THINKING

    PaperBackground(
        showCreaseLines = false,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (onBack != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(tokens.paperCream)
                                .border(2.5.dp, tokens.ink, CircleShape)
                                .clickable(onClick = onBack)
                                .testTag("profile_form_back"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = tokens.ink,
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEditMode) "Edit profile" else "Create profile",
                            style = MaterialTheme.typography.displaySmall,
                            color = tokens.ink,
                        )
                        Text(
                            text = if (isFormValid) "Looking sharp!" else "Pick your teddy & mark.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink.copy(alpha = 0.78f),
                        )
                    }
                    TeddyBiscuit(
                        pose = TeddyPose.PENCIL,
                        expression = biscuitExpression,
                        size = 76.dp,
                        bounceKey = isFormValid,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Profile + Board Box Preview Card
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp),
                    ) {
                        TeddyAvatar(
                            config = currentAvatar,
                            backdropColor = colorOption.color,
                            size = 58.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = trimmedName.ifBlank { "Your name" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (trimmedName.isBlank()) tokens.ink.copy(alpha = 0.45f) else tokens.ink,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                GenderBadge(gender = gender)
                            }
                            Text(
                                text = "${colorOption.label} · ${pattern.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink.copy(alpha = 0.75f),
                            )
                        }
                        // Claimed box preview
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorOption.color)
                                .border(2.5.dp, tokens.ink, RoundedCornerShape(10.dp)),
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawPlayerPattern(pattern, Offset.Zero, size, tokens.ink)
                            }
                            Text(
                                text = derivedInitial.ifBlank { "—" },
                                style = LilitaNumberStyle.copy(fontSize = 20.sp),
                                color = if (colorOption.usesLightOutlineText) tokens.warmHighlight else tokens.ink,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                StudioTextField(
                    value = displayName,
                    onValueChange = { input ->
                        if (input.length <= 16) {
                            displayName = input
                        }
                    },
                    label = "Display name (3–16 characters)",
                    placeholder = "e.g. Aarav Mehta",
                    errorText = nameError,
                    testTag = "profile_name_input",
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (derivedInitial.isNotEmpty()) {
                        "Box mark: \"$derivedInitial\" (automatic first letter of your name)"
                    } else {
                        "Box mark automatically uses the first letter of your name"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.ink.copy(alpha = 0.72f),
                    modifier = Modifier.testTag("profile_auto_initial_note"),
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Teddy Avatar Carousel: 8 base bodies
                Text(
                    text = "Teddy fur (8 styles)",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.ink,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(TeddyBodyColor.entries) { bodyOpt ->
                        val selected = avatarBody == bodyOpt
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) tokens.brandHoney else tokens.paperShade)
                                .border(if (selected) 3.dp else 2.dp, tokens.ink, RoundedCornerShape(18.dp))
                                .clickable { avatarBody = bodyOpt }
                                .padding(6.dp)
                                .testTag("teddy_body_${bodyOpt.name.lowercase()}"),
                        ) {
                            TeddyAvatar(
                                config = currentAvatar.copy(body = bodyOpt),
                                backdropColor = colorOption.color,
                                size = 50.dp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 12 accessories + None
                Text(
                    text = "Accessory (12 options)",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.ink,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(TeddyAccessory.entries) { acc ->
                        StudioChip(
                            label = acc.label,
                            selected = avatarAccessory == acc,
                            onClick = { avatarAccessory = acc },
                            testTag = "teddy_acc_${acc.name.lowercase()}",
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Expression (Happy / Celebrating)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AvatarExpression.entries.forEach { expr ->
                        StudioChip(
                            label = "Mood: ${expr.label}",
                            selected = avatarExpression == expr,
                            onClick = { avatarExpression = expr },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Gender (separate from teddy avatar)",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.ink,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GenderOption.entries.forEach { option ->
                        StudioChip(
                            label = option.label,
                            selected = gender == option,
                            onClick = { gender = option },
                            testTag = "gender_chip_${option.name.lowercase()}",
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Board color",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.ink,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorSwatchRow(
                    selected = colorOption,
                    onSelect = { colorOption = it },
                    testTagPrefix = "profile_color",
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Box pattern",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.ink,
                )
                Spacer(modifier = Modifier.height(8.dp))
                PatternPickerRow(
                    selected = pattern,
                    colorOption = colorOption,
                    onSelect = { pattern = it },
                    testTagPrefix = "profile_pattern",
                )

                Spacer(modifier = Modifier.height(26.dp))

                StudioPrimaryButton(
                    text = if (isEditMode) "Save changes" else "Ready",
                    onClick = {
                        showValidation = true
                        if (isFormValid) {
                            onSaveProfile(trimmedName, derivedInitial, gender, colorOption, pattern, currentAvatar)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "profile_save_button",
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
