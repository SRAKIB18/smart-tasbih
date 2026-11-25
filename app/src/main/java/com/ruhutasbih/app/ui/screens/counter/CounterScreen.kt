package com.ruhutasbih.app.ui.screens.counter

import android.app.Activity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhutasbih.app.R
import com.ruhutasbih.app.ui.theme.ArabicGoldText
import com.ruhutasbih.app.ui.theme.PendingBadgeOrange
import com.ruhutasbih.app.ui.theme.TargetProgressGreen
import com.ruhutasbih.app.ui.util.AudioReciter
import com.ruhutasbih.app.ui.util.HapticManager
import com.ruhutasbih.app.ui.util.SoundManager
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    viewModel: CounterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasbih by viewModel.tasbih.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()

    val soundManager = remember { SoundManager(context) }
    val hapticManager = remember { HapticManager(context) }
    val audioReciter = remember { AudioReciter(context) }
    var isSpeakingAudio by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
            audioReciter.release()
        }
    }

    // Manage Keep Screen Awake Flag
    DisposableEffect(keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showSkinDialog by remember { mutableStateOf(false) }
    var actionsMenuExpanded by remember { mutableStateOf(false) }

    val currentTasbih = tasbih ?: return

    val isTargetReached = currentTasbih.dailyTarget > 0 && currentTasbih.todayReading >= currentTasbih.dailyTarget

    val progress = if (currentTasbih.dailyTarget > 0) {
        (currentTasbih.todayReading.toFloat() / currentTasbih.dailyTarget.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "CounterProgress"
    )

    val currentSkinKey = userPrefs.counterSkin

    val skinConfig = when (currentSkinKey) {
        "WOODEN_BEADS" -> SkinConfig(
            key = "WOODEN_BEADS",
            name = stringResource(R.string.skin_wooden_beads),
            iconStr = "🪵",
            trackColor = Color(0xFF3E2723),
            arcStartColor = Color(0xFFFFB300),
            arcEndColor = Color(0xFFFF8F00),
            innerSurfaceColor = Color(0xFF2C1802),
            numberTextColor = Color(0xFFFFD54F),
            subtitleColor = Color(0xFFFFB300),
            auraGlowColor = Color(0xFFFFB300).copy(alpha = 0.25f),
            drawBeads = true
        )
        "CRYSTAL_BEADS" -> SkinConfig(
            key = "CRYSTAL_BEADS",
            name = stringResource(R.string.skin_crystal_beads),
            iconStr = "💎",
            trackColor = Color(0xFF004D40),
            arcStartColor = Color(0xFF00E5FF),
            arcEndColor = Color(0xFF1DE9B6),
            innerSurfaceColor = Color(0xFF00251A),
            numberTextColor = Color(0xFF1DE9B6),
            subtitleColor = Color(0xFF00E5FF),
            auraGlowColor = Color(0xFF1DE9B6).copy(alpha = 0.25f),
            drawBeads = true
        )
        "METALLIC_RING" -> SkinConfig(
            key = "METALLIC_RING",
            name = stringResource(R.string.skin_metallic_ring),
            iconStr = "⚙️",
            trackColor = Color(0xFF1A232A),
            arcStartColor = Color(0xFFFFD700),
            arcEndColor = Color(0xFFFFA000),
            innerSurfaceColor = Color(0xFF102A43),
            numberTextColor = Color(0xFFFFE082),
            subtitleColor = Color(0xFFFFD700),
            auraGlowColor = Color(0xFFFFD700).copy(alpha = 0.25f),
            drawBeads = true
        )
        "ISLAMIC_GOLD" -> SkinConfig(
            key = "ISLAMIC_GOLD",
            name = stringResource(R.string.skin_islamic_gold),
            iconStr = "🕌",
            trackColor = Color(0xFF0F172A),
            arcStartColor = Color(0xFFFFD700),
            arcEndColor = Color(0xFFFFB300),
            innerSurfaceColor = Color(0xFF1E1B4B),
            numberTextColor = Color(0xFFFFF176),
            subtitleColor = Color(0xFFFFD700),
            auraGlowColor = Color(0xFFFFD700).copy(alpha = 0.30f),
            drawBeads = true
        )
        "COSMIC_GALAXY" -> SkinConfig(
            key = "COSMIC_GALAXY",
            name = stringResource(R.string.skin_cosmic_galaxy),
            iconStr = "🌌",
            trackColor = Color(0xFF180E29),
            arcStartColor = Color(0xFFA855F7),
            arcEndColor = Color(0xFF38BDF8),
            innerSurfaceColor = Color(0xFF0B071A),
            numberTextColor = Color(0xFFE9D5FF),
            subtitleColor = Color(0xFFC084FC),
            auraGlowColor = Color(0xFFA855F7).copy(alpha = 0.25f),
            drawBeads = true
        )
        "NATURAL_JADE" -> SkinConfig(
            key = "NATURAL_JADE",
            name = stringResource(R.string.skin_natural_jade),
            iconStr = "🌿",
            trackColor = Color(0xFF064E3B),
            arcStartColor = Color(0xFF10B981),
            arcEndColor = Color(0xFF34D399),
            innerSurfaceColor = Color(0xFF022C22),
            numberTextColor = Color(0xFFA7F3D0),
            subtitleColor = Color(0xFF34D399),
            auraGlowColor = Color(0xFF34D399).copy(alpha = 0.25f),
            drawBeads = true
        )
        "MINIMALIST_ZEN" -> SkinConfig(
            key = "MINIMALIST_ZEN",
            name = stringResource(R.string.skin_minimalist_zen),
            iconStr = "🍃",
            trackColor = Color(0xFF1E293B),
            arcStartColor = Color(0xFF38BDF8),
            arcEndColor = Color(0xFF34D399),
            innerSurfaceColor = Color(0xFF0F172A),
            numberTextColor = Color(0xFFF8FAFC),
            subtitleColor = Color(0xFF38BDF8),
            auraGlowColor = Color.Transparent,
            drawBeads = false
        )
        else -> SkinConfig( // DIGITAL_LED
            key = "DIGITAL_LED",
            name = stringResource(R.string.skin_digital_led),
            iconStr = "⚡",
            trackColor = Color(0xFF1E293B),
            arcStartColor = if (isTargetReached) TargetProgressGreen else MaterialTheme.colorScheme.primary,
            arcEndColor = if (isTargetReached) TargetProgressGreen else Color(0xFF00E5FF),
            innerSurfaceColor = MaterialTheme.colorScheme.surface,
            numberTextColor = if (isTargetReached) TargetProgressGreen else MaterialTheme.colorScheme.onSurface,
            subtitleColor = if (isTargetReached) TargetProgressGreen else MaterialTheme.colorScheme.primary,
            auraGlowColor = (if (isTargetReached) TargetProgressGreen else MaterialTheme.colorScheme.primary).copy(alpha = 0.2f),
            drawBeads = false
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!isFullScreen) {
                TopAppBar(
                    title = {
                        Text(
                            text = currentTasbih.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // 1. Lock Counter Button
                        IconButton(onClick = {
                            viewModel.toggleLock()
                            val msg = if (!isLocked) "কাউন্টার লক করা হয়েছে" else "কাউন্টার আনলক করা হয়েছে"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock Counter",
                                tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 2. Select Skin Button
                        IconButton(onClick = { showSkinDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Select Skin",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 3. Overflow Menu
                        Box {
                            IconButton(onClick = { actionsMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options"
                                )
                            }
                            DropdownMenu(
                                expanded = actionsMenuExpanded,
                                onDismissRequest = { actionsMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (soundEnabled) "শব্দ বন্ধ করুন (Sound OFF)" else "শব্দ চালু করুন (Sound ON)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        actionsMenuExpanded = false
                                        viewModel.toggleSound()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (vibrationEnabled) "ভাইব্রেশন বন্ধ করুন" else "ভাইব্রেশন চালু করুন") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = null,
                                            tint = if (vibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {
                                        actionsMenuExpanded = false
                                        viewModel.toggleVibration()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (keepScreenAwake) "স্ক্রিন অন বন্ধ করুন" else "স্ক্রিন অন চালু করুন") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = if (keepScreenAwake) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {
                                        actionsMenuExpanded = false
                                        viewModel.toggleKeepScreenAwake()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isFullScreen) "ফুলস্ক্রিন থেকে বের হন" else "ফুলস্ক্রিন মোড") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        actionsMenuExpanded = false
                                        viewModel.toggleFullScreen()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreen) PaddingValues(16.dp) else innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Floating Toolbar when in FullScreen Mode
            if (isFullScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle Sound"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleVibration() }) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Toggle Vibration",
                            tint = if (vibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.toggleKeepScreenAwake() }) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Keep Screen Awake",
                            tint = if (keepScreenAwake) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showSkinDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Select Skin",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        viewModel.toggleLock()
                        val msg = if (!isLocked) "কাউন্টার লক করা হয়েছে" else "কাউন্টার আনলক করা হয়েছে"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock Counter",
                            tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFullScreen() }) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Full Screen"
                        )
                    }
                }
            }

            // Target Completion Pill Badge
            AnimatedVisibility(
                visible = isTargetReached,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TargetProgressGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, TargetProgressGreen.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Target Completed",
                            tint = TargetProgressGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.target_achieved_toast),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TargetProgressGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Arabic Text & Transliteration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentTasbih.arabicText.isNotBlank()) {
                        Text(
                            text = currentTasbih.arabicText,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = ArabicGoldText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    if (currentTasbih.transliteration.isNotBlank()) {
                        Text(
                            text = currentTasbih.transliteration,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    if (currentTasbih.meaning.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentTasbih.meaning,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    // Audio Recitation Play / Pause Button
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSpeakingAudio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSpeakingAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.clickable {
                            if (isSpeakingAudio) {
                                audioReciter.stop()
                                isSpeakingAudio = false
                            } else {
                                val textToRecite = currentTasbih.arabicText.ifBlank { currentTasbih.name }
                                audioReciter.speak(
                                    text = textToRecite,
                                    onStart = { isSpeakingAudio = true },
                                    onDone = { isSpeakingAudio = false }
                                )
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio Recitation",
                                tint = if (isSpeakingAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpeakingAudio) stringResource(R.string.stop_audio) else stringResource(R.string.play_audio),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSpeakingAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            // Central Animated Circular Counter Button
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed && !isLocked) 0.92f else 1.0f,
                animationSpec = tween(durationMillis = 100),
                label = "ButtonScale"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        if (isLocked) {
                            Toast.makeText(context, "গণনা করতে প্যাডলক আইকনে ট্যাপ করে আনলক করুন", Toast.LENGTH_SHORT).show()
                        } else {
                            if (soundEnabled) soundManager.playClickSound(userPrefs.clickSoundType)
                            if (vibrationEnabled) hapticManager.vibrateClick()
                            viewModel.incrementCount(1L)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Draw Outer Aura Glow
                    drawCircle(
                        color = skinConfig.auraGlowColor,
                        radius = radius + (strokeWidth / 1.5f),
                        center = center
                    )

                    // Draw Base Track Ring
                    drawCircle(
                        color = skinConfig.trackColor,
                        style = Stroke(width = strokeWidth)
                    )

                    // Draw Gradient Progress Arc
                    val arcBrush = Brush.sweepGradient(
                        colors = listOf(skinConfig.arcStartColor, skinConfig.arcEndColor, skinConfig.arcStartColor),
                        center = center
                    )
                    drawArc(
                        brush = arcBrush,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Draw 33 Traditional Bead Ticks for Physical Bead feel
                    if (skinConfig.drawBeads) {
                        val beadCount = 33
                        for (i in 0 until beadCount) {
                            val angleRad = Math.toRadians((i * (360.0 / beadCount)) - 90.0)
                            val bx = center.x + radius * cos(angleRad).toFloat()
                            val by = center.y + radius * sin(angleRad).toFloat()

                            val isPassedBead = (i.toFloat() / beadCount.toFloat()) <= animatedProgress
                            val isSeparatorBead = (i + 1) % 11 == 0
                            val beadRadius = if (isSeparatorBead) 6.dp.toPx() else 4.dp.toPx()
                            val beadColor = if (isPassedBead) skinConfig.arcStartColor else skinConfig.trackColor.copy(alpha = 0.6f)

                            drawCircle(
                                color = beadColor,
                                radius = beadRadius,
                                center = Offset(bx, by)
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isLocked) skinConfig.innerSurfaceColor.copy(alpha = 0.85f) else skinConfig.innerSurfaceColor,
                    border = BorderStroke(2.dp, skinConfig.arcStartColor.copy(alpha = 0.35f)),
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(225.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentTasbih.todayReading.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 56.sp,
                                    color = if (isLocked) skinConfig.numberTextColor.copy(alpha = 0.5f) else skinConfig.numberTextColor
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isLocked) stringResource(R.string.counter_locked) else stringResource(R.string.tap_to_count),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isLocked) MaterialTheme.colorScheme.error else skinConfig.subtitleColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Lock Badge Overlay
                        if (isLocked) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        viewModel.toggleLock()
                                        Toast.makeText(context, "কাউন্টার আনলক করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.counter_locked),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Breakdown Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMetricItem(
                            label = stringResource(R.string.daily_target),
                            value = currentTasbih.dailyTarget.toString()
                        )
                        StatMetricItem(
                            label = stringResource(R.string.previous_pending),
                            value = currentTasbih.previousPending.toString(),
                            color = if (currentTasbih.previousPending > 0) PendingBadgeOrange else MaterialTheme.colorScheme.onSurface
                        )
                        StatMetricItem(
                            label = stringResource(R.string.pending_remaining),
                            value = currentTasbih.previousPendingRemaining.toString(),
                            color = if (currentTasbih.previousPendingRemaining > 0) PendingBadgeOrange else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMetricItem(
                            label = stringResource(R.string.daily_count),
                            value = currentTasbih.dailyCount.toString(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatMetricItem(
                            label = stringResource(R.string.next_day_pending),
                            value = currentTasbih.nextDayPending.toString(),
                            color = if (currentTasbih.nextDayPending > 0) PendingBadgeOrange else MaterialTheme.colorScheme.onSurface
                        )
                        StatMetricItem(
                            label = stringResource(R.string.lifetime_count),
                            value = currentTasbih.lifetimeCount.toString()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Undo & Reset Today
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        if (isLocked) {
                            Toast.makeText(context, "গণনা করতে প্যাডলক আইকনে ট্যাপ করে আনলক করুন", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.undoCount()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_undo))
                }

                TextButton(
                    onClick = {
                        if (isLocked) {
                            Toast.makeText(context, "গণনা করতে প্যাডলক আইকনে ট্যাপ করে আনলক করুন", Toast.LENGTH_SHORT).show()
                        } else {
                            showResetDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_reset),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showSkinDialog) {
        AlertDialog(
            onDismissRequest = { showSkinDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.select_counter_skin),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val skins = listOf(
                        SkinConfig("DIGITAL_LED", stringResource(R.string.skin_digital_led), "⚡", Color(0xFF1E293B), Color(0xFF00E5FF), Color(0xFF10B981), Color(0xFF0F172A), Color(0xFF00E5FF), Color(0xFF10B981), Color.Unspecified, false),
                        SkinConfig("WOODEN_BEADS", stringResource(R.string.skin_wooden_beads), "🪵", Color(0xFF3E2723), Color(0xFFFFB300), Color(0xFFFF8F00), Color(0xFF2C1802), Color(0xFFFFD54F), Color(0xFFFFB300), Color.Unspecified, true),
                        SkinConfig("CRYSTAL_BEADS", stringResource(R.string.skin_crystal_beads), "💎", Color(0xFF004D40), Color(0xFF00E5FF), Color(0xFF1DE9B6), Color(0xFF00251A), Color(0xFF1DE9B6), Color(0xFF00E5FF), Color.Unspecified, true),
                        SkinConfig("METALLIC_RING", stringResource(R.string.skin_metallic_ring), "⚙️", Color(0xFF1A232A), Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFF102A43), Color(0xFFFFE082), Color(0xFFFFD700), Color.Unspecified, true),
                        SkinConfig("ISLAMIC_GOLD", stringResource(R.string.skin_islamic_gold), "🕌", Color(0xFF0F172A), Color(0xFFFFD700), Color(0xFFFFB300), Color(0xFF1E1B4B), Color(0xFFFFF176), Color(0xFFFFD700), Color.Unspecified, true),
                        SkinConfig("COSMIC_GALAXY", stringResource(R.string.skin_cosmic_galaxy), "🌌", Color(0xFF180E29), Color(0xFFA855F7), Color(0xFF38BDF8), Color(0xFF0B071A), Color(0xFFE9D5FF), Color(0xFFC084FC), Color.Unspecified, true),
                        SkinConfig("NATURAL_JADE", stringResource(R.string.skin_natural_jade), "🌿", Color(0xFF064E3B), Color(0xFF10B981), Color(0xFF34D399), Color(0xFF022C22), Color(0xFFA7F3D0), Color(0xFF34D399), Color.Unspecified, true),
                        SkinConfig("MINIMALIST_ZEN", stringResource(R.string.skin_minimalist_zen), "🍃", Color(0xFF1E293B), Color(0xFF34D399), Color(0xFF34D399), Color(0xFF0F172A), Color(0xFFF8FAFC), Color(0xFF94A3B8), Color.Unspecified, false)
                    )

                    skins.forEach { item ->
                        val isSelected = currentSkinKey == item.key
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCounterSkin(item.key)
                                    showSkinDialog = false
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.iconStr,
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setCounterSkin(item.key)
                                        showSkinDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSkinDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_confirmation_title)) },
            text = { Text(stringResource(R.string.reset_confirmation_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetTodayCount()
                    }
                ) {
                    Text(stringResource(R.string.action_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private data class SkinConfig(
    val key: String,
    val name: String,
    val iconStr: String,
    val trackColor: Color,
    val arcStartColor: Color,
    val arcEndColor: Color,
    val innerSurfaceColor: Color,
    val numberTextColor: Color,
    val subtitleColor: Color,
    val auraGlowColor: Color,
    val drawBeads: Boolean
)

@Composable
private fun StatMetricItem(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}