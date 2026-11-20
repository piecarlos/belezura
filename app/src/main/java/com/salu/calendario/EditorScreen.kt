package com.salu.calendario

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    uiState: CalendarUiState,
    onNavigateHome: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleEditMode: () -> Unit,
    onOpenBgDialog: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onRequestStoragePermission: () -> Unit,
    onSaveToGallery: (Context) -> Unit,
    onShare: (Context) -> Unit,
    onCellClick: (Int) -> Unit,
    onElementSelect: (EditableElement) -> Unit,
    onOpenEditModeWithElement: (EditableElement) -> Unit = {},
    onDrag: (EditableElement, Float, Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onMonthFontChange: (HeaderFontOption) -> Unit,
    onYearFontChange: (HeaderFontOption) -> Unit,
    onMonthColorChange: (Int) -> Unit,
    onYearColorChange: (Int) -> Unit,
    onCardAccentColorChange: (Int) -> Unit,
    onStepElementOffset: (EditableElement, Float, Float) -> Unit,
    onCenterElementX: (EditableElement) -> Unit,
    onResetLayout: () -> Unit,
    onUploadLogo: () -> Unit = {},
    onToggleLogoEnabled: (Boolean) -> Unit = {},
    onLogoModeChange: (String) -> Unit = {},
    onClearLogo: () -> Unit = {}
) {
    val context = LocalContext.current
    var showOptionsMenu by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF0F5),
                    titleContentColor = Color(0xFF5E2326)
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Início",
                            tint = Color(0xFF5E2326)
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Mês anterior",
                                tint = Color(0xFF5E2326),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        TextButton(
                            onClick = onOpenDatePicker,
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = CalendarEngine.getHeaderTitle(uiState.currentYearMonth),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E2326),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Próximo mês",
                                tint = Color(0xFF5E2326),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Undo button
                    IconButton(
                        onClick = onUndo,
                        enabled = uiState.canUndo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Desfazer",
                            tint = if (uiState.canUndo) Color(0xFF5E2326) else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Redo button
                    IconButton(
                        onClick = onRedo,
                        enabled = uiState.canRedo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Refazer",
                            tint = if (uiState.canRedo) Color(0xFF5E2326) else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Layout adjust button
                    IconButton(
                        onClick = onToggleEditMode,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (uiState.isEditMode) Color(0xFFB82B62) else Color.Transparent,
                            contentColor = if (uiState.isEditMode) Color.White else Color(0xFF5E2326)
                        )
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Ajustar Layout", modifier = Modifier.size(20.dp))
                    }

                    // Background settings button
                    IconButton(
                        onClick = onOpenBgDialog,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Fundo",
                            tint = if (uiState.hasCustomBackground) Color(0xFFB82B62) else Color(0xFF5E2326),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // More Options (Backup / Restore)
                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = Color(0xFF5E2326), modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exportar backup (.json)") },
                                onClick = {
                                    showOptionsMenu = false
                                    onExportBackup()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Restaurar backup (.json)") },
                                onClick = {
                                    showOptionsMenu = false
                                    onImportBackup()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!uiState.isEditMode) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        onRequestStoragePermission()
                                    } else {
                                        onSaveToGallery(context)
                                    }
                                } else {
                                    onSaveToGallery(context)
                                }
                            },
                            enabled = !uiState.isSavingGallery && !uiState.isSharing,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isSavingGallery) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Salvando...", fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Salvar PNG", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        OutlinedButton(
                            onClick = { onShare(context) },
                            enabled = !uiState.isSavingGallery && !uiState.isSharing,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isSharing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFFB82B62),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Gerando...", color = Color(0xFFB82B62), fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFB82B62))
                                Spacer(Modifier.width(8.dp))
                                Text("Compartilhar", color = Color(0xFFB82B62), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF9F5F6))
        ) {
            // Main Calendar Interactive View (locked 9:16 aspect ratio)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CalendarCanvasView(
                    uiState = uiState,
                    onCellClick = { slotIndex ->
                        if (!uiState.isEditMode) {
                            onCellClick(slotIndex)
                        }
                    },
                    onElementSelect = { element ->
                        if (uiState.isEditMode) {
                            onElementSelect(element)
                        }
                    },
                    onDrag = { element, dx, dy ->
                        if (uiState.isEditMode) {
                            onDrag(element, dx, dy)
                        }
                    },
                    onScaleChange = { newScale ->
                        if (uiState.isEditMode) {
                            onScaleChange(newScale)
                        }
                    },
                    onOpenEditModeWithElement = onOpenEditModeWithElement
                )
            }

            // Edit Mode Adjustment Bottom Panel
            AnimatedVisibility(
                visible = uiState.isEditMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                LayoutAdjustmentPanel(
                    uiState = uiState,
                    onSelectElement = onElementSelect,
                    onScaleChange = onScaleChange,
                    onMonthFontChange = onMonthFontChange,
                    onYearFontChange = onYearFontChange,
                    onMonthColorChange = onMonthColorChange,
                    onYearColorChange = onYearColorChange,
                    onCardAccentColorChange = onCardAccentColorChange,
                    onStepElementOffset = onStepElementOffset,
                    onCenterElementX = onCenterElementX,
                    onResetLayout = onResetLayout,
                    onCloseEditMode = onToggleEditMode,
                    onUploadLogo = onUploadLogo,
                    onToggleLogoEnabled = onToggleLogoEnabled,
                    onLogoModeChange = onLogoModeChange,
                    onClearLogo = onClearLogo
                )
            }
        }
    }
}

// 9:16 Canvas WYSIWYG View
@Composable
fun CalendarCanvasView(
    uiState: CalendarUiState,
    onCellClick: (Int) -> Unit,
    onElementSelect: (EditableElement) -> Unit,
    onDrag: (EditableElement, Float, Float) -> Unit,
    onScaleChange: (Float) -> Unit = {},
    onOpenEditModeWithElement: (EditableElement) -> Unit = {}
) {
    val context = LocalContext.current
    val rowCount = CalendarEngine.calculateRowCount(uiState.currentYearMonth)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxW = maxWidth
        val maxH = maxHeight

        // Compute aspect 9:16 box that fits available space
        val targetWidth = if (maxW * (16f / 9f) <= maxH) maxW else maxH * (9f / 16f)
        val targetHeight = targetWidth * (16f / 9f)

        Box(
            modifier = Modifier
                .size(targetWidth, targetHeight)
                .shadow(10.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(uiState.isEditMode, uiState.selectedElement, rowCount) {
                    if (uiState.isEditMode) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val vx = down.position.x * (CalendarRenderer.VIRTUAL_WIDTH / size.width.toFloat())
                            val vy = down.position.y * (CalendarRenderer.VIRTUAL_HEIGHT / size.height.toFloat())
                            val hit = CalendarRenderer.getHitElement(vx, vy, uiState.layoutConfig)
                            if (hit != EditableElement.NONE) {
                                onElementSelect(hit)
                            }

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
                                    }
                                }

                                if (pastTouchSlop) {
                                    val scaleFactor = CalendarRenderer.VIRTUAL_WIDTH / size.width.toFloat()
                                    val targetElement = if (hit != EditableElement.NONE) hit else uiState.selectedElement

                                    if (targetElement == EditableElement.LOGO) {
                                        if (zoomChange != 1f) {
                                            val currentScale = uiState.layoutConfig.logoScale
                                            val newScale = (currentScale * zoomChange).coerceIn(0.50f, 2.20f)
                                            onScaleChange(newScale)
                                        }
                                        if (panChange != Offset.Zero) {
                                            onDrag(EditableElement.LOGO, panChange.x * scaleFactor, panChange.y * scaleFactor)
                                        }
                                    } else if (targetElement != EditableElement.NONE) {
                                        if (panChange != Offset.Zero) {
                                            onDrag(targetElement, panChange.x * scaleFactor, panChange.y * scaleFactor)
                                        }
                                    }
                                    event.changes.forEach { it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    } else {
                        detectTapGestures { offset ->
                            val vx = offset.x * (CalendarRenderer.VIRTUAL_WIDTH / size.width.toFloat())
                            val vy = offset.y * (CalendarRenderer.VIRTUAL_HEIGHT / size.height.toFloat())
                            val hit = CalendarRenderer.getHitElement(vx, vy, uiState.layoutConfig)
                            if (hit == EditableElement.LOGO && uiState.layoutConfig.logoEnabled) {
                                onOpenEditModeWithElement(EditableElement.LOGO)
                            } else {
                                val slot = CalendarRenderer.getCellAt(vx, vy, uiState.layoutConfig, rowCount)
                                if (slot != null) {
                                    onCellClick(slot)
                                } else if (hit != EditableElement.NONE && hit != EditableElement.GRID_CARD) {
                                    onOpenEditModeWithElement(hit)
                                }
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    native.save()
                    val scaleX = size.width / CalendarRenderer.VIRTUAL_WIDTH
                    val scaleY = size.height / CalendarRenderer.VIRTUAL_HEIGHT
                    native.scale(scaleX, scaleY)

                    CalendarRenderer.drawCalendar(
                        canvas = native,
                        context = context,
                        yearMonth = uiState.currentYearMonth,
                        cells = uiState.cells,
                        layoutConfig = uiState.layoutConfig,
                        isEditMode = uiState.isEditMode,
                        selectedElement = uiState.selectedElement
                    )

                    native.restore()
                }
            }
        }
    }
}
