package com.salu.calendario

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
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

@Composable
fun CalendarAppScreen(viewModel: CalendarViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeGallerySheet by rememberSaveable { mutableStateOf(false) }
    var showLogoSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // SAF Launchers
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onCustomBackgroundPicked(uri)
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onLogoPicked(uri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.saveToGallery(context)
        } else {
            viewModel.showSnackbar("Permissão de armazenamento necessária para salvar na galeria.")
        }
    }

    val launchExportBackup = {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        exportBackupLauncher.launch("AgendaSalu_Backup_$timestamp.json")
    }

    val launchImportBackup = {
        importBackupLauncher.launch(arrayOf("application/json", "*/*"))
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
                    onOpenEditorForMonth = { ym -> viewModel.openEditorForMonth(ym) },
                    onOpenDayFromSearch = { result -> viewModel.openDayFromSearch(result) },
                    onShareMonthFromHome = { ym -> viewModel.shareCalendarFromHome(context, ym) },
                    onSearchQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
                    onCustomBackgroundPicked = { uri -> viewModel.onCustomBackgroundPicked(uri) },
                    onRestoreDefaultBackground = { viewModel.onRestoreDefaultBackground() },
                    onExportBackup = launchExportBackup,
                    onImportBackup = launchImportBackup,
                    onOpenQuickDatePicker = { showDatePickerDialog = true },
                    onOpenThemeGallery = { showThemeGallerySheet = true }
                )
            }
            AppScreen.CALENDAR -> {
                EditorScreen(
                    uiState = uiState,
                    onNavigateHome = {
                        if (uiState.isEditMode) {
                            viewModel.toggleEditMode()
                        }
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onOpenDatePicker = { showDatePickerDialog = true },
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onToggleEditMode = { viewModel.toggleEditMode() },
                    onOpenBgDialog = { showThemeGallerySheet = true },
                    onExportBackup = launchExportBackup,
                    onImportBackup = launchImportBackup,
                    onRequestStoragePermission = {
                        permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    },
                    onSaveToGallery = { ctx -> viewModel.saveToGallery(ctx) },
                    onShare = { ctx -> viewModel.shareCalendar(ctx) },
                    onCellClick = { slotIndex -> viewModel.openCellEditor(slotIndex) },
                    onElementSelect = { element -> viewModel.selectElement(element) },
                    onDrag = { element, dx, dy -> viewModel.dragElement(element, dx, dy) },
                    onScaleChange = { scale -> viewModel.updateSelectedElementScale(scale) },
                    onMonthFontChange = { font -> viewModel.updateMonthFont(font) },
                    onYearFontChange = { font -> viewModel.updateYearFont(font) },
                    onMonthColorChange = { color -> viewModel.updateMonthColor(color) },
                    onYearColorChange = { color -> viewModel.updateYearColor(color) },
                    onCardAccentColorChange = { color -> viewModel.updateCardAccentColor(color) },
                    onStepElementOffset = { element, dx, dy -> viewModel.stepElementOffset(element, dx, dy) },
                    onCenterElementX = { element -> viewModel.centerElementX(element) },
                    onResetLayout = { viewModel.resetLayout() },
                    onUploadLogo = {
                        logoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onToggleLogoEnabled = { viewModel.toggleLogoEnabled(it) },
                    onLogoModeChange = { viewModel.updateLogoMode(it) },
                    onClearLogo = { viewModel.clearLogo() }
                )
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
        ConfirmRestoreDialog(
            onDismiss = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            },
            onConfirm = {
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
            }
        )
    }

    // Theme Gallery BottomSheet (Phase A)
    if (showThemeGallerySheet) {
        ThemeGallerySheet(
            currentThemeId = uiState.layoutConfig.selectedThemeId,
            hasCustomBackground = uiState.hasCustomBackground,
            hasCustomLogo = uiState.hasCustomLogo,
            onSelectTheme = { theme ->
                viewModel.selectTheme(theme)
            },
            onPickCustomBackground = {
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onResetToDefaultBackground = {
                viewModel.onRestoreDefaultBackground()
            },
            onOpenLogoSettings = {
                showThemeGallerySheet = false
                showLogoSettingsDialog = true
            },
            onDismiss = { showThemeGallerySheet = false }
        )
    }

    // Logo Settings Dialog (Phase A)
    if (showLogoSettingsDialog) {
        LogoSettingsDialog(
            hasCustomLogo = uiState.hasCustomLogo,
            logoEnabled = uiState.layoutConfig.logoEnabled,
            logoMode = uiState.layoutConfig.logoMode,
            onUploadClick = {
                logoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onToggleEnabled = { viewModel.toggleLogoEnabled(it) },
            onModeChange = { viewModel.updateLogoMode(it) },
            onClearClick = { viewModel.clearLogo() },
            onDismiss = { showLogoSettingsDialog = false }
        )
    }

    // Honest PRO Preview Transparency Dialog
    uiState.proPreviewTheme?.let { proTheme ->
        ProPreviewDialog(
            theme = proTheme,
            onDismiss = { viewModel.dismissProPreview() }
        )
    }
}
