package com.salu.calendario

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaluCalendarTheme {
                CalendarAppScreen(viewModel = viewModel)
            }
        }
    }
}

// Visual Theme for Agenda Salu
@Composable
fun SaluCalendarTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFFB82B62),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFCEBF1),
        onPrimaryContainer = Color(0xFF5E2326),
        secondary = Color(0xFFE56B6F),
        onSecondary = Color.White,
        background = Color(0xFFFFF9FA),
        surface = Color.White,
        onSurface = Color(0xFF2C2426)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarAppScreen(viewModel: CalendarViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showBgDialog by rememberSaveable { mutableStateOf(false) }
    var showOptionsMenu by rememberSaveable { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // Backup launchers
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val res = BackupManager.exportToUri(context, uri)
            res.onSuccess {
                viewModel.showSnackbar("Backup exportado com sucesso!")
            }.onFailure { err ->
                viewModel.showSnackbar("Erro ao exportar backup: ${err.message}")
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirmDialog = true
        }
    }

    // Gallery Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onCustomBackgroundPicked(uri)
        }
    }

    // Permission launcher for API <= 28 if needed for saving
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.saveToGallery(context)
        } else {
            viewModel.showSnackbar("Permissão de armazenamento necessária para salvar na galeria.")
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    BackHandler(enabled = uiState.currentScreen == AppScreen.CALENDAR) {
        if (uiState.isEditMode) {
            viewModel.toggleEditMode()
        } else {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    uiState = uiState,
                    onOpenEditorForMonth = { ym ->
                        viewModel.openEditorForMonth(ym)
                    },
                    onOpenDayFromSearch = { result ->
                        viewModel.openDayFromSearch(result)
                    },
                    onShareMonthFromHome = { ym ->
                        viewModel.shareCalendarFromHome(context, ym)
                    },
                    onSearchQueryChange = { query ->
                        viewModel.onSearchQueryChanged(query)
                    },
                    onCustomBackgroundPicked = { uri ->
                        viewModel.onCustomBackgroundPicked(uri)
                    },
                    onRestoreDefaultBackground = {
                        viewModel.onRestoreDefaultBackground()
                    },
                    onExportBackup = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        exportBackupLauncher.launch("AgendaSalu_Backup_$timestamp.json")
                    },
                    onImportBackup = {
                        importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    onOpenQuickDatePicker = {
                        showDatePickerDialog = true
                    }
                )
            }
            AppScreen.CALENDAR -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFFFFF0F5),
                                titleContentColor = Color(0xFF5E2326)
                            ),
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (uiState.isEditMode) {
                                        viewModel.toggleEditMode()
                                    }
                                    viewModel.navigateTo(AppScreen.HOME)
                                }) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = "Início",
                                        tint = Color(0xFF5E2326)
                                    )
                                }
                            },
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.previousMonth() },
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
                                        onClick = { showDatePickerDialog = true },
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
                                        onClick = { viewModel.nextMonth() },
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
                                    onClick = { viewModel.undo() },
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
                                    onClick = { viewModel.redo() },
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
                                    onClick = { viewModel.toggleEditMode() },
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
                                    onClick = { showBgDialog = true },
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
                                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    exportBackupLauncher.launch("AgendaSalu_Backup_$timestamp.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Restaurar backup (.json)") },
                                onClick = {
                                    showOptionsMenu = false
                                    importBackupLauncher.launch(arrayOf("application/json", "*/*"))
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
                                        permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    } else {
                                        viewModel.saveToGallery(context)
                                    }
                                } else {
                                    viewModel.saveToGallery(context)
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
                            onClick = { viewModel.shareCalendar(context) },
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
                            viewModel.openCellEditor(slotIndex)
                        }
                    },
                    onElementSelect = { element ->
                        if (uiState.isEditMode) {
                            viewModel.selectElement(element)
                        }
                    },
                    onDrag = { element, dx, dy ->
                        if (uiState.isEditMode) {
                            viewModel.dragElement(element, dx, dy)
                        }
                    }
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
                    onSelectElement = { viewModel.selectElement(it) },
                    onScaleChange = { viewModel.updateSelectedElementScale(it) },
                    onMonthFontChange = { viewModel.updateMonthFont(it) },
                    onYearFontChange = { viewModel.updateYearFont(it) },
                    onMonthColorChange = { viewModel.updateMonthColor(it) },
                    onYearColorChange = { viewModel.updateYearColor(it) },
                    onCardAccentColorChange = { viewModel.updateCardAccentColor(it) },
                    onStepElementOffset = { element, dx, dy -> viewModel.stepElementOffset(element, dx, dy) },
                    onCenterElementX = { element -> viewModel.centerElementX(element) },
                    onResetLayout = { viewModel.resetLayout() },
                    onCloseEditMode = { viewModel.toggleEditMode() }
                )
            }
        }
    }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (uiState.currentScreen == AppScreen.CALENDAR && !uiState.isEditMode) 80.dp else 16.dp)
        )
    }

    // Modal BottomSheet for Cell Editing
    uiState.editingCell?.let { cell ->
        CellEditBottomSheet(
            cell = cell,
            yearMonth = uiState.currentYearMonth,
            onDismiss = { viewModel.closeCellEditor() },
            onToggleRedX = { viewModel.toggleRedX(cell.slotIndex, it) },
            onAddName = { viewModel.addNameToCell(cell.slotIndex, it) },
            onRemoveName = { viewModel.removeNameFromCell(cell.slotIndex, it) },
            onMoveUp = { viewModel.moveNameUp(cell.slotIndex, it) },
            onMoveDown = { viewModel.moveNameDown(cell.slotIndex, it) }
        )
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
        MonthYearPickerDialog(
            currentYearMonth = uiState.currentYearMonth,
            onDismiss = { showDatePickerDialog = false },
            onConfirm = { newYm ->
                showDatePickerDialog = false
                viewModel.openEditorForMonth(newYm)
            }
        )
    }

    // Confirm Backup Restore Dialog
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            },
            title = {
                Text("Restaurar Backup", fontWeight = FontWeight.Bold, color = Color(0xFF5E2326))
            },
            text = {
                Text("Isso substituirá todos os meses e o layout atuais pelos dados do backup. Continuar?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingRestoreUri
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                        if (uri != null) {
                            val res = BackupManager.importFromUri(context, uri)
                            res.onSuccess {
                                viewModel.reloadAllData()
                            }.onFailure { err ->
                                viewModel.showSnackbar("Falha ao restaurar: ${err.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62))
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de orientação antes do upload da moldura personalizada
    if (showBgDialog) {
        CustomBackgroundDialog(
            hasCustomBackground = uiState.hasCustomBackground,
            onDismiss = { showBgDialog = false },
            onPickImage = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRestoreDefault = {
                viewModel.onRestoreDefaultBackground()
            }
        )
    }
}

// 9:16 Canvas WYSIWYG View
@Composable
fun CalendarCanvasView(
    uiState: CalendarUiState,
    onCellClick: (Int) -> Unit,
    onElementSelect: (EditableElement) -> Unit,
    onDrag: (EditableElement, Float, Float) -> Unit
) {
    val context = LocalContext.current
    val rowCount = CalendarEngine.calculateRowCount(uiState.currentYearMonth)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
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
                        detectDragGestures(
                            onDragStart = { offset ->
                                val vx = offset.x * (CalendarRenderer.VIRTUAL_WIDTH / size.width.toFloat())
                                val vy = offset.y * (CalendarRenderer.VIRTUAL_HEIGHT / size.height.toFloat())
                                val hit = CalendarRenderer.getHitElement(vx, vy, uiState.layoutConfig)
                                if (hit != EditableElement.NONE) {
                                    onElementSelect(hit)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val scale = CalendarRenderer.VIRTUAL_WIDTH / size.width.toFloat()
                                val dx = dragAmount.x * scale
                                val dy = dragAmount.y * scale
                                onDrag(uiState.selectedElement, dx, dy)
                            }
                        )
                    } else {
                        detectTapGestures { offset ->
                            val vx = offset.x * (CalendarRenderer.VIRTUAL_WIDTH / size.width.toFloat())
                            val vy = offset.y * (CalendarRenderer.VIRTUAL_HEIGHT / size.height.toFloat())
                            val slot = CalendarRenderer.getCellAt(vx, vy, uiState.layoutConfig, rowCount)
                            if (slot != null) {
                                onCellClick(slot)
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

// Preset colors for the color selector
val PRESET_COLORS = listOf(
    0xFF5E2326.toInt(), // Vinho
    0xFFE64886.toInt(), // Rosa
    0xFFE89BAE.toInt(), // Rosa Suave
    0xFFC62828.toInt(), // Vermelho
    0xFFC9972B.toInt(), // Dourado
    0xFFB5533C.toInt(), // Terracota
    0xFF7E4EAC.toInt(), // Lilás
    0xFF1E3A5F.toInt(), // Azul Marinho
    0xFF2E6B53.toInt(), // Verde Esmeralda
    0xFF222222.toInt(), // Grafite
    0xFFFFFFFF.toInt()  // Branco
)

enum class LayoutToolTab {
    POSITION,
    FONT,
    COLOR
}

// Layout Adjustment Bottom Panel (Zero Rolagem)
@Composable
fun LayoutAdjustmentPanel(
    uiState: CalendarUiState,
    onSelectElement: (EditableElement) -> Unit,
    onScaleChange: (Float) -> Unit,
    onMonthFontChange: (HeaderFontOption) -> Unit,
    onYearFontChange: (HeaderFontOption) -> Unit,
    onMonthColorChange: (Int) -> Unit,
    onYearColorChange: (Int) -> Unit,
    onCardAccentColorChange: (Int) -> Unit,
    onStepElementOffset: (EditableElement, Float, Float) -> Unit,
    onCenterElementX: (EditableElement) -> Unit,
    onResetLayout: () -> Unit,
    onCloseEditMode: () -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf(LayoutToolTab.POSITION) }

    val currentElement = if (uiState.selectedElement == EditableElement.NONE) {
        EditableElement.MONTH
    } else {
        uiState.selectedElement
    }

    // Auto-fallback from FONT to POSITION when Calendário is selected
    val effectiveTab = if (currentElement == EditableElement.GRID_CARD && activeTab == LayoutToolTab.FONT) {
        LayoutToolTab.POSITION
    } else {
        activeTab
    }

    val currentElementColor = when (currentElement) {
        EditableElement.MONTH -> uiState.layoutConfig.monthColor
        EditableElement.YEAR -> uiState.layoutConfig.yearColor
        EditableElement.GRID_CARD -> uiState.layoutConfig.cardAccentColor
        EditableElement.NONE -> uiState.layoutConfig.monthColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Row 1: Element Selector Chips (Mês, Ano, Calendário) + Action Icons (Resetar, Concluir)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = currentElement == EditableElement.MONTH,
                        onClick = { onSelectElement(EditableElement.MONTH) },
                        label = { Text("Mês", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB82B62),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentElement == EditableElement.MONTH)
                    )

                    FilterChip(
                        selected = currentElement == EditableElement.YEAR,
                        onClick = { onSelectElement(EditableElement.YEAR) },
                        label = { Text("Ano", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB82B62),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentElement == EditableElement.YEAR)
                    )

                    FilterChip(
                        selected = currentElement == EditableElement.GRID_CARD,
                        onClick = { onSelectElement(EditableElement.GRID_CARD) },
                        label = { Text("Calendário", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB82B62),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentElement == EditableElement.GRID_CARD)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onResetLayout, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Resetar padrão",
                            tint = Color(0xFFB82B62),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onCloseEditMode, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Concluir",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Row 2: Tool Sub-Tabs (Posição | Fonte | Cor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9EEF2))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab Posição
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (effectiveTab == LayoutToolTab.POSITION) Color(0xFFB82B62) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clickable { activeTab = LayoutToolTab.POSITION }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Posição",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (effectiveTab == LayoutToolTab.POSITION) Color.White else Color(0xFF5E2326)
                        )
                    }
                }

                // Tab Fonte (Only for Mês and Ano)
                if (currentElement != EditableElement.GRID_CARD) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (effectiveTab == LayoutToolTab.FONT) Color(0xFFB82B62) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clickable { activeTab = LayoutToolTab.FONT }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Fonte",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (effectiveTab == LayoutToolTab.FONT) Color.White else Color(0xFF5E2326)
                            )
                        }
                    }
                }

                // Tab Cor (with colored indicator circle)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (effectiveTab == LayoutToolTab.COLOR) Color(0xFFB82B62) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clickable { activeTab = LayoutToolTab.COLOR }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Cor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (effectiveTab == LayoutToolTab.COLOR) Color.White else Color(0xFF5E2326)
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(Color(currentElementColor))
                                .border(
                                    width = 1.dp,
                                    color = if (effectiveTab == LayoutToolTab.COLOR) Color.White else Color(0xFFB82B62),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Sub-tab content area
            when (effectiveTab) {
                LayoutToolTab.POSITION -> {
                    // Scale slider line
                    val currentScale = when (currentElement) {
                        EditableElement.MONTH -> uiState.layoutConfig.monthScale
                        EditableElement.YEAR -> uiState.layoutConfig.yearScale
                        EditableElement.GRID_CARD -> uiState.layoutConfig.cardScale
                        EditableElement.NONE -> 1f
                    }

                    val scaleRange = when (currentElement) {
                        EditableElement.GRID_CARD -> 0.70f..1.25f
                        else -> 0.60f..1.60f
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tamanho: ${(currentScale * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5E2326),
                            modifier = Modifier.width(92.dp)
                        )
                        Slider(
                            value = currentScale,
                            onValueChange = onScaleChange,
                            valueRange = scaleRange,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFB82B62),
                                activeTrackColor = Color(0xFFB82B62)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fine adjustment and Center X buttons line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Ajuste fino:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            IconButton(
                                onClick = { onStepElementOffset(currentElement, -10f, 0f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Esquerda",
                                    tint = Color(0xFFB82B62),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onStepElementOffset(currentElement, 0f, -10f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = "Cima",
                                    tint = Color(0xFFB82B62),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onStepElementOffset(currentElement, 0f, 10f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = "Baixo",
                                    tint = Color(0xFFB82B62),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onStepElementOffset(currentElement, 10f, 0f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Direita",
                                    tint = Color(0xFFB82B62),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onCenterElementX(currentElement) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Centralizar X", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB82B62))
                        }
                    }
                }

                LayoutToolTab.FONT -> {
                    val currentFont = if (currentElement == EditableElement.MONTH) {
                        uiState.layoutConfig.monthFont
                    } else {
                        uiState.layoutConfig.yearFont
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HeaderFontOption.values().forEach { option ->
                            FilterChip(
                                selected = currentFont == option,
                                onClick = {
                                    if (currentElement == EditableElement.MONTH) {
                                        onMonthFontChange(option)
                                    } else {
                                        onYearFontChange(option)
                                    }
                                },
                                label = { Text(option.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFB82B62),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                LayoutToolTab.COLOR -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        ColorRowSelector(
                            selectedColor = currentElementColor,
                            onColorSelected = { newColor ->
                                when (currentElement) {
                                    EditableElement.MONTH -> onMonthColorChange(newColor)
                                    EditableElement.YEAR -> onYearColorChange(newColor)
                                    EditableElement.GRID_CARD -> onCardAccentColorChange(newColor)
                                    EditableElement.NONE -> {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Color Selector Row with Presets + Custom Dialog button
@Composable
fun ColorRowSelector(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PRESET_COLORS.forEach { colorInt ->
            val isSelected = selectedColor == colorInt
            val composeColor = Color(colorInt)
            val isLight = ColorUtils.calculateLuminance(colorInt) > 0.75

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(composeColor)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color(0xFFB82B62) else if (isLight) Color.LightGray else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorInt) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selecionado",
                        tint = if (isLight) Color(0xFF5E2326) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Custom color "+" button
        IconButton(
            onClick = { showCustomDialog = true },
            modifier = Modifier
                .size(34.dp)
                .background(Color(0xFFF3E5EC), CircleShape)
                .border(1.dp, Color(0xFFE89BAE), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Cor customizada",
                tint = Color(0xFFB82B62),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showCustomDialog) {
        CustomColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { showCustomDialog = false },
            onColorChosen = { newColor ->
                onColorSelected(newColor)
                showCustomDialog = false
            }
        )
    }
}

// Dialog with HSV sliders + Hex input + preview
@Composable
fun CustomColorPickerDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onColorChosen: (Int) -> Unit
) {
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor, hsv)
        hsv
    }

    var hue by remember { mutableStateOf(initialHsv[0]) }
    var saturation by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }
    var hexText by remember {
        mutableStateOf(String.format("#%06X", 0xFFFFFF and initialColor))
    }

    val currentColor = remember(hue, saturation, value) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Escolher Cor Customizada", fontWeight = FontWeight.Bold, color = Color(0xFF5E2326))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Color Preview Box and Hex Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(currentColor))
                            .border(1.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    )

                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { input ->
                            hexText = input
                            if (input.startsWith("#") && (input.length == 7 || input.length == 9)) {
                                try {
                                    val parsed = android.graphics.Color.parseColor(input)
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(parsed, hsv)
                                    hue = hsv[0]
                                    saturation = hsv[1]
                                    value = hsv[2]
                                } catch (_: Exception) {}
                            }
                        },
                        label = { Text("Hex (#RRGGBB)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Hue Slider
                Column {
                    Text("Matiz (Tom): ${hue.toInt()}°", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            val c = android.graphics.Color.HSVToColor(floatArrayOf(it, saturation, value))
                            hexText = String.format("#%06X", 0xFFFFFF and c)
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(currentColor),
                            activeTrackColor = Color(0xFFB82B62)
                        )
                    )
                }

                // Saturation Slider
                Column {
                    Text("Saturação: ${(saturation * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            val c = android.graphics.Color.HSVToColor(floatArrayOf(hue, it, value))
                            hexText = String.format("#%06X", 0xFFFFFF and c)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(currentColor),
                            activeTrackColor = Color(0xFFB82B62)
                        )
                    )
                }

                // Brightness / Value Slider
                Column {
                    Text("Brilho: ${(value * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = value,
                        onValueChange = {
                            value = it
                            val c = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, it))
                            hexText = String.format("#%06X", 0xFFFFFF and c)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(currentColor),
                            activeTrackColor = Color(0xFFB82B62)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorChosen(currentColor) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62))
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Modal BottomSheet for Day Editing
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellEditBottomSheet(
    cell: CellData,
    yearMonth: YearMonth,
    onDismiss: () -> Unit,
    onToggleRedX: (Boolean) -> Unit,
    onAddName: (String) -> Unit,
    onRemoveName: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newNameText by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Sheet Title
            val title = if (cell.dayNumber != null) {
                "Dia ${cell.dayNumber} de ${CalendarEngine.getMonthDisplayName(yearMonth).lowercase().replaceFirstChar { it.titlecase(Locale("pt", "BR")) }} de ${yearMonth.year}"
            } else {
                "Célula vazia (fora do mês)"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5E2326)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Switch Red X
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (cell.hasRedX) Color(0xFFFDE8E9) else Color(0xFFF9F9F9)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Marcar com X vermelho",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (cell.hasRedX) Color(0xFFC2185B) else Color(0xFF333333)
                        )
                        Text(
                            text = "Indica dia indisponível ou fechado",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = cell.hasRedX,
                        onCheckedChange = onToggleRedX,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFE56B6F),
                            checkedTrackColor = Color(0xFFFFCDD2)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // If day is valid, show name list and input field
            if (cell.dayNumber != null) {
                Text(
                    text = "Nomes / Atendimentos (${cell.names.size}):",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF5E2326)
                )

                Spacer(Modifier.height(8.dp))

                // Input field + add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newNameText,
                        onValueChange = { newNameText = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Ex: Milka Noiva") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newNameText.isNotBlank()) {
                                    onAddName(newNameText)
                                    newNameText = ""
                                    focusRequester.requestFocus()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (newNameText.isNotBlank()) {
                                onAddName(newNameText)
                                newNameText = ""
                                focusRequester.requestFocus()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // List of names with reorder / remove
                if (cell.names.isEmpty()) {
                    Text(
                        text = "Nenhum nome cadastrado neste dia.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        itemsIndexed(cell.names) { index, name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFFCEBF1), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1A1A1A),
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    // Move Up
                                    IconButton(
                                        onClick = { onMoveUp(index) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = "Subir",
                                            tint = if (index > 0) Color(0xFFB82B62) else Color.LightGray
                                        )
                                    }

                                    // Move Down
                                    IconButton(
                                        onClick = { onMoveDown(index) },
                                        enabled = index < cell.names.size - 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = "Descer",
                                            tint = if (index < cell.names.size - 1) Color(0xFFB82B62) else Color.LightGray
                                        )
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = { onRemoveName(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remover",
                                            tint = Color(0xFFD32F2F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Quick Month + Year Picker Dialog
@Composable
fun MonthYearPickerDialog(
    currentYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var selectedMonth by rememberSaveable { mutableStateOf(currentYearMonth.monthValue) }
    var yearText by rememberSaveable { mutableStateOf(currentYearMonth.year.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Selecionar Mês e Ano", fontWeight = FontWeight.Bold, color = Color(0xFF5E2326))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Year field with - / + buttons
                Text("Ano:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            val cur = yearText.toIntOrNull() ?: 2026
                            yearText = (cur - 1).coerceAtLeast(1970).toString()
                        }
                    ) {
                        Text("-1", fontWeight = FontWeight.Bold, color = Color(0xFFB82B62))
                    }

                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                yearText = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )

                    IconButton(
                        onClick = {
                            val cur = yearText.toIntOrNull() ?: 2026
                            yearText = (cur + 1).coerceAtMost(2100).toString()
                        }
                    ) {
                        Text("+1", fontWeight = FontWeight.Bold, color = Color(0xFFB82B62))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 12 Months Grid
                Text("Mês:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))

                val monthsShort = listOf(
                    "JAN", "FEV", "MAR", "ABR",
                    "MAI", "JUN", "JUL", "AGO",
                    "SET", "OUT", "NOV", "DEZ"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0 until 3) {
                                val monthIndex = row * 3 + col
                                val monthNum = monthIndex + 1
                                val isSelected = selectedMonth == monthNum

                                Button(
                                    onClick = { selectedMonth = monthNum },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFFB82B62) else Color(0xFFF3E5EC),
                                        contentColor = if (isSelected) Color.White else Color(0xFF5E2326)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(monthsShort[monthIndex], fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = yearText.toIntOrNull() ?: currentYearMonth.year
                    val ym = YearMonth.of(year.coerceIn(1970, 2100), selectedMonth)
                    onConfirm(ym)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62))
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
