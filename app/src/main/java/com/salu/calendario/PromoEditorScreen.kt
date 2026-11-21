package com.salu.calendario

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoEditorScreen(
    uiState: CalendarUiState,
    snackbarHostState: SnackbarHostState? = null,
    onNavigateHome: () -> Unit,
    onSelectTemplate: (PromoTemplateType) -> Unit,
    onUpdateDraft: (PromoDraft) -> Unit,
    onResetToPreset: (PromoTemplateType) -> Unit,
    onOpenThemeGallery: () -> Unit,
    onOpenLogoSettings: () -> Unit,
    onSaveToGallery: (Context) -> Unit,
    onShare: (Context) -> Unit,
    onLogoTransform: ((zoomChange: Float, dx: Float, dy: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    var showEditTextsSheet by rememberSaveable { mutableStateOf(false) }
    var isAdjustingLogoMode by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = {
            snackbarHostState?.let {
                SnackbarHost(it)
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF0F5),
                    titleContentColor = Color(0xFF5E2326)
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color(0xFF5E2326)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Criar Promoção",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Molduras e fundos
                    IconButton(onClick = onOpenThemeGallery) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Molduras & Temas",
                            tint = Color(0xFF5E2326)
                        )
                    }
                    // Configuração da Logo
                    IconButton(onClick = onOpenLogoSettings) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Minha Logo",
                            tint = Color(0xFFB82B62)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFF3C7D4))
            ) {
                if (isAdjustingLogoMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arraste ou pinça na logo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5E2326),
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        OutlinedButton(
                            onClick = {
                                onUpdateDraft(uiState.promoDraft.copy(logoScale = 1.0f))
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFB82B62))
                        ) {
                            Text("100%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB82B62))
                        }
                        OutlinedButton(
                            onClick = {
                                onUpdateDraft(uiState.promoDraft.copy(logoOffsetX = 0f))
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFB82B62))
                        ) {
                            Text("Centralizar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB82B62))
                        }
                        Button(
                            onClick = {
                                isAdjustingLogoMode = false
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62))
                        ) {
                            Text("Concluir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão secundário / tonal "Editar"
                        OutlinedButton(
                            onClick = {
                                showEditTextsSheet = true
                            },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFB82B62)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB82B62))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Editar", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        }

                        // Botão Salvar PNG
                        OutlinedButton(
                            onClick = { onSaveToGallery(context) },
                            enabled = !uiState.isSavingGallery && !uiState.isSharing,
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFB82B62)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB82B62))
                        ) {
                            if (uiState.isSavingGallery) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFB82B62),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Salvar", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            }
                        }

                        // Botão Compartilhar
                        Button(
                            onClick = { onShare(context) },
                            enabled = !uiState.isSavingGallery && !uiState.isSharing,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isSharing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Enviar", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF9FA)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Horizontal Template Selector Chips (Vagas | Preços | Avisos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PromoTemplateType.values().forEach { templateType ->
                    val isSelected = uiState.promoDraft.templateType == templateType
                    Surface(
                        onClick = { onSelectTemplate(templateType) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFFB82B62) else Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFFB82B62) else Color(0xFFF3C7D4)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = templateType.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else Color(0xFF5E2326),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Central 9:16 WYSIWYG Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                PromoCanvasView(
                    promoDraft = uiState.promoDraft,
                    layoutConfig = uiState.layoutConfig,
                    isAdjustingLogoMode = isAdjustingLogoMode,
                    onTap = {
                        showEditTextsSheet = true
                    },
                    onTapLogo = {
                        showEditTextsSheet = false
                        isAdjustingLogoMode = true
                    },
                    onLogoTransform = { zoomChange, dx, dy ->
                        if (onLogoTransform != null) {
                            val current = uiState.promoDraft
                            val rawX = current.logoOffsetX + dx
                            val effectiveDx = if (abs(rawX) < 18f) -current.logoOffsetX else dx
                            onLogoTransform(zoomChange, effectiveDx, dy)
                        } else {
                            val current = uiState.promoDraft
                            val currentScale = (current.logoScale * zoomChange).coerceIn(0.3f, 5.0f)
                            val rawX = current.logoOffsetX + dx
                            val newX = if (abs(rawX) < 18f) 0f else rawX.coerceIn(-540f, 540f)
                            val newY = (current.logoOffsetY + dy).coerceIn(-600f, 1600f)
                            onUpdateDraft(
                                current.copy(
                                    logoScale = currentScale,
                                    logoOffsetX = newX,
                                    logoOffsetY = newY
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    // Modal BottomSheet for Unified Promo Editing
    if (showEditTextsSheet) {
        PromoEditBottomSheet(
            promoDraft = uiState.promoDraft,
            selectedThemeId = uiState.layoutConfig.selectedThemeId,
            onDismiss = { showEditTextsSheet = false },
            onUpdateDraft = onUpdateDraft,
            onResetToPreset = onResetToPreset
        )
    }
}

// 9:16 WYSIWYG Canvas for Promo Stories
@Composable
fun PromoCanvasView(
    promoDraft: PromoDraft,
    layoutConfig: LayoutConfig,
    isAdjustingLogoMode: Boolean = false,
    onTap: (initialTab: Int) -> Unit,
    onTapLogo: () -> Unit = {},
    onLogoTransform: (zoomChange: Float, panX: Float, panY: Float) -> Unit
) {
    val context = LocalContext.current

    val currentDraft by rememberUpdatedState(promoDraft)
    val currentAdjustingMode by rememberUpdatedState(isAdjustingLogoMode)
    val currentOnTransform by rememberUpdatedState(onLogoTransform)
    val currentOnTapLogo by rememberUpdatedState(onTapLogo)
    val currentOnTap by rememberUpdatedState(onTap)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxW = maxWidth
        val maxH = maxHeight

        val targetWidth = if (maxW * (16f / 9f) <= maxH) maxW else maxH * (9f / 16f)
        val targetHeight = targetWidth * (16f / 9f)

        Box(
            modifier = Modifier
                .size(targetWidth, targetHeight)
                .shadow(10.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val factor = PromoRenderer.VIRTUAL_WIDTH / size.width.toFloat().coerceAtLeast(1f)
                        val vx = down.position.x * factor
                        val vy = down.position.y * factor

                        val logoBounds = PromoRenderer.getLogoBounds(context, currentDraft)
                        val hitLogo = if (currentDraft.showLogo && logoBounds != null) {
                            val slop = 60f
                            (vx >= logoBounds.left - slop && vx <= logoBounds.right + slop &&
                             vy >= logoBounds.top - slop && vy <= logoBounds.bottom + slop)
                        } else false

                        val canTransform = currentAdjustingMode || hitLogo

                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop
                        var panAccumulated = Offset.Zero

                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (canceled) break

                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastTouchSlop) {
                                panAccumulated += panChange
                                val isMultiTouch = event.changes.size > 1
                                if (panAccumulated.getDistance() > touchSlop || isMultiTouch || abs(zoomChange - 1f) > 0.01f) {
                                    pastTouchSlop = true
                                    if (canTransform) {
                                        val dx = panAccumulated.x * factor
                                        val dy = panAccumulated.y * factor
                                        currentOnTransform(zoomChange, dx, dy)
                                        event.changes.forEach {
                                            if (it.positionChanged()) it.consume()
                                        }
                                    }
                                }
                            } else if (canTransform) {
                                val dx = panChange.x * factor
                                val dy = panChange.y * factor
                                if (dx != 0f || dy != 0f || abs(zoomChange - 1f) > 0.001f) {
                                    currentOnTransform(zoomChange, dx, dy)
                                }
                                event.changes.forEach {
                                    if (it.positionChanged()) it.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        if (!pastTouchSlop) {
                            if (hitLogo) {
                                currentOnTapLogo()
                            } else if (!currentAdjustingMode) {
                                currentOnTap(0) // Tab Texto
                            }
                        }
                    }
                }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    native.save()
                    val scaleX = size.width / PromoRenderer.VIRTUAL_WIDTH
                    val scaleY = size.height / PromoRenderer.VIRTUAL_HEIGHT
                    native.scale(scaleX, scaleY)

                    PromoRenderer.drawPromo(
                        canvas = native,
                        context = context,
                        promoDraft = promoDraft,
                        layoutConfig = layoutConfig
                    )

                    if (isAdjustingLogoMode && promoDraft.showLogo) {
                        val bounds = PromoRenderer.getLogoBounds(context, promoDraft)
                        if (bounds != null) {
                            val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = 3.5f
                                color = android.graphics.Color.parseColor("#B82B62")
                                pathEffect = android.graphics.DashPathEffect(floatArrayOf(14f, 8f), 0f)
                            }
                            native.drawRoundRect(
                                bounds.left - 4f,
                                bounds.top - 4f,
                                bounds.right + 4f,
                                bounds.bottom + 4f,
                                14f,
                                14f,
                                strokePaint
                            )
                        }

                        // Linha-guia vertical fina translúcida no centro quando centralizada (snap magnético)
                        if (abs(promoDraft.logoOffsetX) < 1f) {
                            val guidePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = 2.5f
                                color = android.graphics.Color.argb(130, 184, 43, 98)
                                pathEffect = android.graphics.DashPathEffect(floatArrayOf(16f, 12f), 0f)
                            }
                            native.drawLine(
                                PromoRenderer.VIRTUAL_WIDTH / 2f,
                                0f,
                                PromoRenderer.VIRTUAL_WIDTH / 2f,
                                PromoRenderer.VIRTUAL_HEIGHT,
                                guidePaint
                            )
                        }
                    }

                    native.restore()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromoEditBottomSheet(
    promoDraft: PromoDraft,
    selectedThemeId: String,
    onDismiss: () -> Unit,
    onUpdateDraft: (PromoDraft) -> Unit,
    onResetToPreset: (PromoTemplateType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var badgeText by remember(promoDraft) { mutableStateOf(promoDraft.badgeText) }
    var title by remember(promoDraft) { mutableStateOf(promoDraft.title) }
    var subtitle by remember(promoDraft) { mutableStateOf(promoDraft.subtitle) }
    var footerCta by remember(promoDraft) { mutableStateOf(promoDraft.footerCta) }
    var showLogo by remember(promoDraft) { mutableStateOf(promoDraft.showLogo) }
    var items by remember(promoDraft) { mutableStateOf(promoDraft.items) }
    var titleFont by remember(promoDraft) { mutableStateOf(promoDraft.titleFont) }
    var accentColorHex by remember(promoDraft) { mutableStateOf(promoDraft.accentColorHex) }

    fun commitChanges() {
        onUpdateDraft(
            promoDraft.copy(
                badgeText = badgeText,
                title = title,
                subtitle = subtitle,
                footerCta = footerCta,
                showLogo = showLogo,
                items = items,
                titleFont = titleFont,
                accentColorHex = accentColorHex
            )
        )
    }

    val cinzelFamily = remember {
        try { FontFamily(Font(R.font.cinzel_bold)) } catch (_: Exception) { FontFamily.Serif }
    }
    val playfairFamily = remember {
        try { FontFamily(Font(R.font.playfair_display_bold)) } catch (_: Exception) { FontFamily.Serif }
    }
    val fredokaFamily = remember {
        try { FontFamily(Font(R.font.fredoka_bold)) } catch (_: Exception) { FontFamily.SansSerif }
    }

    fun getTitleFontFamily(fontName: String): FontFamily {
        return when {
            fontName.equals("CINZEL_BOLD", ignoreCase = true) || fontName.equals("Cinzel", ignoreCase = true) -> cinzelFamily
            fontName.equals("FREDOKA_BOLD", ignoreCase = true) || fontName.equals("Fredoka", ignoreCase = true) -> fredokaFamily
            fontName.equals("SYSTEM_DEFAULT", ignoreCase = true) || fontName.equals("Default", ignoreCase = true) || fontName.equals("Padrão", ignoreCase = true) -> FontFamily.Default
            else -> playfairFamily
        }
    }

    val theme = remember(selectedThemeId) {
        ThemeCatalog.getThemeById(selectedThemeId) ?: ThemeCatalog.getThemes().first()
    }
    val isDark = theme.id == "black_friday_luxo" || theme.id == "black_friday_glam" ||
        (try { androidx.core.graphics.ColorUtils.calculateLuminance(android.graphics.Color.parseColor(theme.palette.backgroundBaseHex)) < 0.35f } catch (_: Exception) { false })

    val previewTitleColor = remember(accentColorHex, isDark, theme) {
        if (accentColorHex.isNotBlank()) {
            try { Color(android.graphics.Color.parseColor(accentColorHex)) } catch (_: Exception) { Color(0xFFB82B62) }
        } else {
            if (isDark) Color(0xFFFFD700)
            else try { Color(android.graphics.Color.parseColor(theme.palette.monthColorHex)) } catch (_: Exception) { Color(0xFF5E2326) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            commitChanges()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        scrimColor = Color.Black.copy(alpha = 0.22f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Editar Promoção",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E2326)
                    )
                    Text(
                        text = "Modelo: ${promoDraft.templateType.displayName}",
                        fontSize = 12.sp,
                        color = Color(0xFF8C5A60)
                    )
                }

                IconButton(
                    onClick = {
                        commitChanges()
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color(0xFF5E2326))
                }
            }

            // Scrollable fields in unified vertical layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 560.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Mini-Preview Ao Vivo do Título
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF1E1B18) else Color(0xFFFFF5F7),
                    border = BorderStroke(1.dp, if (isDark) Color(0x33D4AF37) else Color(0xFFF3C7D4))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "PRÉVIA AO VIVO DO TÍTULO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0x99FFE082) else Color(0xFF8C5A60),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (title.isNotBlank()) title else "(Digite o título abaixo)",
                            fontFamily = getTitleFontFamily(titleFont),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = previewTitleColor,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 2. Título Principal
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        commitChanges()
                    },
                    label = { Text("Título Principal") },
                    placeholder = { Text("Ex: Agenda Aberta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )

                // 3. Chips de Fonte com renderização no FontFamily real
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Estilo da Fonte",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF5E2326)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HeaderFontOption.values().forEach { fontOpt ->
                            val fontFam = when (fontOpt) {
                                HeaderFontOption.CINZEL_BOLD -> cinzelFamily
                                HeaderFontOption.PLAYFAIR_DISPLAY_BOLD -> playfairFamily
                                HeaderFontOption.FREDOKA_BOLD -> fredokaFamily
                                HeaderFontOption.SYSTEM_DEFAULT -> FontFamily.Default
                            }
                            val isSel = titleFont.equals(fontOpt.name, ignoreCase = true) ||
                                (fontOpt == HeaderFontOption.PLAYFAIR_DISPLAY_BOLD && (titleFont.isBlank() || titleFont.equals("Playfair", ignoreCase = true)))
                            Surface(
                                onClick = {
                                    titleFont = fontOpt.name
                                    commitChanges()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color(0xFFB82B62) else Color.White,
                                border = BorderStroke(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) Color(0xFFB82B62) else Color(0xFFE0C0C8)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = fontOpt.displayName,
                                        fontFamily = fontFam,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (isSel) Color.White else Color(0xFF5E2326),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Faixa compacta de Cor de Destaque
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Cor de Destaque",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF5E2326)
                        )
                        if (accentColorHex.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    accentColorHex = ""
                                    commitChanges()
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Usar Automático", fontSize = 11.sp, color = Color(0xFFB82B62))
                            }
                        }
                    }

                    val colorOptions = listOf(
                        "" to "Auto",
                        "#D4AF37" to "Ouro",
                        "#B82B62" to "Rosa",
                        "#5E2326" to "Vinho",
                        "#E89BAE" to "Rosé",
                        "#C86446" to "Terra",
                        "#2E6B53" to "Verde",
                        "#212121" to "Grafite"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorOptions.forEach { (hex, name) ->
                            val isSelected = (hex.isEmpty() && accentColorHex.isBlank()) ||
                                (hex.isNotEmpty() && accentColorHex.equals(hex, ignoreCase = true))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (hex.isEmpty()) Color(0xFFE0E0E0)
                                            else Color(android.graphics.Color.parseColor(hex))
                                        )
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFB82B62) else Color(0xFFCCCCCC),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            accentColorHex = hex
                                            commitChanges()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (hex == "#D4AF37" || hex == "#E89BAE" || hex.isEmpty()) Color.Black else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = name,
                                    fontSize = 9.5.sp,
                                    color = if (isSelected) Color(0xFFB82B62) else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 5. Selo Superior (Pill Badge)
                OutlinedTextField(
                    value = badgeText,
                    onValueChange = {
                        badgeText = it
                        commitChanges()
                    },
                    label = { Text("Selo Superior (opcional)") },
                    placeholder = { Text("Ex: ÚLTIMOS HORÁRIOS") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )

                // 6. Subtítulo
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = {
                        subtitle = it
                        commitChanges()
                    },
                    label = { Text("Subtítulo curto") },
                    placeholder = { Text("Ex: Garanta seu horário...") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )

                // 7. Lista Dinâmica de Itens
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Itens (${items.size}/${PromoDraft.MAX_ITEMS})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF5E2326)
                        )

                        if (items.size < PromoDraft.MAX_ITEMS) {
                            TextButton(
                                onClick = {
                                    val newLabel = when (promoDraft.templateType) {
                                        PromoTemplateType.AGENDA_ABERTA -> "Novo Dia"
                                        PromoTemplateType.TABELA_PRECOS -> "Novo Serviço"
                                        PromoTemplateType.AVISO_REGRAS -> "Nova Regra"
                                    }
                                    val newValue = when (promoDraft.templateType) {
                                        PromoTemplateType.AGENDA_ABERTA -> "10:00 • 14:00"
                                        PromoTemplateType.TABELA_PRECOS -> "R$ 50,00"
                                        PromoTemplateType.AVISO_REGRAS -> "Detalhe explicativo"
                                    }
                                    items = items + PromoLineItem(newLabel, newValue)
                                    commitChanges()
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFB82B62))
                                Spacer(Modifier.width(4.dp))
                                Text("Adicionar Item", fontSize = 12.sp, color = Color(0xFFB82B62), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            if (promoDraft.templateType == PromoTemplateType.TABELA_PRECOS) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.label,
                                        onValueChange = { newLabel ->
                                            items = items.toMutableList().also {
                                                it[index] = it[index].copy(label = newLabel)
                                            }
                                            commitChanges()
                                        },
                                        label = { Text("Serviço", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.8f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFB82B62)
                                        )
                                    )
                                    OutlinedTextField(
                                        value = item.value,
                                        onValueChange = { newVal ->
                                            items = items.toMutableList().also {
                                                it[index] = it[index].copy(value = newVal)
                                            }
                                            commitChanges()
                                        },
                                        label = { Text("Preço (R$)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFB82B62)
                                        )
                                    )
                                    if (items.size > 1) {
                                        IconButton(
                                            onClick = {
                                                items = items.toMutableList().also { it.removeAt(index) }
                                                commitChanges()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remover",
                                                tint = Color(0xFFC62828),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (promoDraft.templateType == PromoTemplateType.AGENDA_ABERTA) "Data / Horários #${index + 1}" else "Regra #${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF8C5A60)
                                        )
                                        if (items.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    items = items.toMutableList().also { it.removeAt(index) }
                                                    commitChanges()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Remover",
                                                    tint = Color(0xFFC62828),
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }
                                    val labelHint = if (promoDraft.templateType == PromoTemplateType.AGENDA_ABERTA) "Dia (Ex: Sexta • 28/02)" else "Título da Regra (Ex: Tolerância de 15 min)"
                                    val valueHint = if (promoDraft.templateType == PromoTemplateType.AGENDA_ABERTA) "Horários livres (Ex: 09:00 • 11:30 • 15:00)" else "Detalhe explicativo"

                                    OutlinedTextField(
                                        value = item.label,
                                        onValueChange = { newLabel ->
                                            items = items.toMutableList().also {
                                                it[index] = it[index].copy(label = newLabel)
                                            }
                                            commitChanges()
                                        },
                                        label = { Text(labelHint, fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFB82B62)
                                        )
                                    )
                                    OutlinedTextField(
                                        value = item.value,
                                        onValueChange = { newVal ->
                                            items = items.toMutableList().also {
                                                it[index] = it[index].copy(value = newVal)
                                            }
                                            commitChanges()
                                        },
                                        label = { Text(valueHint, fontSize = 11.sp) },
                                        singleLine = promoDraft.templateType == PromoTemplateType.AGENDA_ABERTA,
                                        maxLines = 2,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFB82B62)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Rodapé CTA
                OutlinedTextField(
                    value = footerCta,
                    onValueChange = {
                        footerCta = it
                        commitChanges()
                    },
                    label = { Text("Chamada do Rodapé (CTA)") },
                    placeholder = { Text("Ex: Agende seu horário pelo WhatsApp!") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )

                // 9. Switch discreto "Exibir logo no story"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF5F8), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exibir logo no story",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF5E2326)
                        )
                        Text(
                            text = "Toque e arraste a logo direto na tela",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = showLogo,
                        onCheckedChange = {
                            showLogo = it
                            commitChanges()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFB82B62)
                        )
                    )
                }

                // 10. Botão Restaurar modelo
                OutlinedButton(
                    onClick = {
                        onResetToPreset(promoDraft.templateType)
                        val reset = PromoDraft.getPreset(promoDraft.templateType)
                        badgeText = reset.badgeText
                        title = reset.title
                        subtitle = reset.subtitle
                        footerCta = reset.footerCta
                        showLogo = reset.showLogo
                        items = reset.items
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFB82B62))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFB82B62), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Restaurar modelo", color = Color(0xFFB82B62), fontSize = 13.sp)
                }
            }

            // 11. Ação Concluir
            Button(
                onClick = {
                    commitChanges()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Concluir", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
