package com.salu.calendario

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.time.YearMonth

data class CalendarUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val currentYearMonth: YearMonth = CalendarEngine.DEFAULT_YEAR_MONTH,
    val cells: List<CellData> = emptyList(),
    val layoutConfig: LayoutConfig = LayoutConfig.DEFAULT,
    val isEditMode: Boolean = false,
    val selectedElement: EditableElement = EditableElement.NONE,
    val editingCell: CellData? = null,
    val hasCustomBackground: Boolean = false,
    val hasCustomLogo: Boolean = false,
    val proPreviewTheme: ThemeItem? = null,
    val snackbarMessage: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSavingGallery: Boolean = false,
    val isSharing: Boolean = false,
    val savedMonthsSummaries: List<MonthSummary> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<ClientSearchResult> = emptyList(),
    val lastBackupTimeFormatted: String? = null
)

object ClientSearchHelper {
    fun normalize(str: String): String {
        return Normalizer.normalize(str, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }

    fun search(allEntries: Map<String, *>, query: String): List<ClientSearchResult> {
        val cleanQuery = normalize(query.trim())
        if (cleanQuery.length < 2) return emptyList()

        val results = mutableListOf<ClientSearchResult>()
        for ((key, value) in allEntries) {
            if (key.startsWith("month_data_") && value is String) {
                val parts = key.removePrefix("month_data_").split("_")
                if (parts.size == 2) {
                    val y = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (y != null && m != null) {
                        val ym = YearMonth.of(y, m)
                        try {
                            val root = JSONObject(value)
                            val slotsArr = root.optJSONArray("slots") ?: continue
                            for (i in 0 until slotsArr.length()) {
                                val obj = slotsArr.getJSONObject(i)
                                val slotIndex = obj.getInt("slot")
                                val dayNum = if (obj.has("day") && !obj.isNull("day")) obj.getInt("day") else null
                                if (dayNum != null) {
                                    val namesArr = obj.optJSONArray("names") ?: continue
                                    for (j in 0 until namesArr.length()) {
                                        val rawName = namesArr.getString(j)
                                        if (normalize(rawName).contains(cleanQuery)) {
                                            results.add(ClientSearchResult(rawName, ym, dayNum, slotIndex))
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // Sort descending by YearMonth, then by day descending
        results.sortWith(
            compareByDescending<ClientSearchResult> { it.yearMonth }
                .thenByDescending { it.day }
        )
        return results
    }
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("salu_calendar_prefs", Context.MODE_PRIVATE)

    private val undoStack = ArrayDeque<List<CellData>>()
    private val redoStack = ArrayDeque<List<CellData>>()

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        val initialLayout = loadLayoutConfig()
        val initialYm = CalendarEngine.DEFAULT_YEAR_MONTH
        val savedCells = loadMonthCells(initialYm)
        val cells = CalendarEngine.generateCells(initialYm, savedCells)
        val hasCustom = BackgroundManager.hasCustomBackground(getApplication())
        val hasLogo = LogoManager.hasCustomLogo(getApplication())
        val lastBackup = BackupManager.getLastBackupTimeFormatted(getApplication())

        _uiState.update {
            it.copy(
                currentYearMonth = initialYm,
                layoutConfig = initialLayout,
                cells = cells,
                hasCustomBackground = hasCustom,
                hasCustomLogo = hasLogo,
                lastBackupTimeFormatted = lastBackup
            )
        }
        refreshSavedMonthsSummaries()
    }

    // Navigation
    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun openEditorForMonth(ym: YearMonth) {
        setYearMonth(ym)
        _uiState.update { it.copy(currentScreen = AppScreen.CALENDAR) }
    }

    fun openDayFromSearch(result: ClientSearchResult) {
        if (_uiState.value.currentYearMonth != result.yearMonth) {
            val savedCells = loadMonthCells(result.yearMonth)
            val cells = CalendarEngine.generateCells(result.yearMonth, savedCells)
            undoStack.clear()
            redoStack.clear()
            val targetCell = cells.firstOrNull { it.slotIndex == result.slotIndex || it.dayNumber == result.day }
            _uiState.update {
                it.copy(
                    currentYearMonth = result.yearMonth,
                    cells = cells,
                    currentScreen = AppScreen.CALENDAR,
                    editingCell = targetCell,
                    canUndo = false,
                    canRedo = false
                )
            }
        } else {
            val targetCell = _uiState.value.cells.firstOrNull { it.slotIndex == result.slotIndex || it.dayNumber == result.day }
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.CALENDAR,
                    editingCell = targetCell
                )
            }
        }
    }

    // Search
    fun onSearchQueryChanged(query: String) {
        val trimmed = query.trim()
        val results = if (trimmed.length >= 2) {
            searchClients(trimmed)
        } else {
            emptyList()
        }
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchResults = results
            )
        }
    }

    fun searchClients(query: String): List<ClientSearchResult> {
        return ClientSearchHelper.search(prefs.all, query)
    }

    // Refresh month summaries
    fun refreshSavedMonthsSummaries() {
        val allEntries = prefs.all
        val list = mutableListOf<MonthSummary>()

        for ((key, value) in allEntries) {
            if (key.startsWith("month_data_") && value is String) {
                val parts = key.removePrefix("month_data_").split("_")
                if (parts.size == 2) {
                    val y = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (y != null && m != null) {
                        val ym = YearMonth.of(y, m)
                        var namesCount = 0
                        var redXCount = 0
                        try {
                            val root = JSONObject(value)
                            val slotsArr = root.optJSONArray("slots") ?: continue
                            for (i in 0 until slotsArr.length()) {
                                val obj = slotsArr.getJSONObject(i)
                                if (obj.optBoolean("hasRedX", false)) {
                                    redXCount++
                                }
                                val namesArr = obj.optJSONArray("names")
                                if (namesArr != null) {
                                    namesCount += namesArr.length()
                                }
                            }
                        } catch (_: Exception) {}

                        if (namesCount > 0 || redXCount > 0) {
                            list.add(MonthSummary(ym, namesCount, redXCount))
                        }
                    }
                }
            }
        }

        // Chronological sort
        list.sortBy { it.yearMonth }

        val lastBackup = BackupManager.getLastBackupTimeFormatted(getApplication())

        _uiState.update {
            it.copy(
                savedMonthsSummaries = list,
                lastBackupTimeFormatted = lastBackup
            )
        }
    }

    // Month Navigation in Editor
    fun previousMonth() {
        setYearMonth(_uiState.value.currentYearMonth.minusMonths(1))
    }

    fun nextMonth() {
        setYearMonth(_uiState.value.currentYearMonth.plusMonths(1))
    }

    fun setYearMonth(newYm: YearMonth) {
        val savedCells = loadMonthCells(newYm)
        val cells = CalendarEngine.generateCells(newYm, savedCells)
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                currentYearMonth = newYm,
                cells = cells,
                editingCell = null,
                canUndo = false,
                canRedo = false
            )
        }
    }

    // Cell Editing
    fun openCellEditor(slotIndex: Int) {
        val cell = _uiState.value.cells.firstOrNull { it.slotIndex == slotIndex } ?: return
        _uiState.update { it.copy(editingCell = cell) }
    }

    fun closeCellEditor() {
        _uiState.update { it.copy(editingCell = null) }
    }

    fun toggleRedX(slotIndex: Int, enabled: Boolean) {
        updateCell(slotIndex) { it.copy(hasRedX = enabled) }
    }

    fun addNameToCell(slotIndex: Int, rawName: String) {
        val name = rawName.trim()
        if (name.isEmpty()) return
        updateCell(slotIndex) { it.copy(names = it.names + name) }
    }

    fun removeNameFromCell(slotIndex: Int, index: Int) {
        updateCell(slotIndex) {
            val list = it.names.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            it.copy(names = list)
        }
    }

    fun moveNameUp(slotIndex: Int, index: Int) {
        if (index <= 0) return
        updateCell(slotIndex) {
            val list = it.names.toMutableList()
            if (index in list.indices) {
                val item = list.removeAt(index)
                list.add(index - 1, item)
            }
            it.copy(names = list)
        }
    }

    fun moveNameDown(slotIndex: Int, index: Int) {
        updateCell(slotIndex) {
            val list = it.names.toMutableList()
            if (index in 0 until (list.size - 1)) {
                val item = list.removeAt(index)
                list.add(index + 1, item)
            }
            it.copy(names = list)
        }
    }

    private fun updateCell(slotIndex: Int, transform: (CellData) -> CellData) {
        val currentCells = _uiState.value.cells

        // Save snapshot to undo stack
        undoStack.addLast(currentCells)
        if (undoStack.size > 20) {
            undoStack.removeFirst()
        }
        redoStack.clear()

        val updatedCells = currentCells.map { cell ->
            if (cell.slotIndex == slotIndex) {
                val updated = transform(cell)
                if (_uiState.value.editingCell?.slotIndex == slotIndex) {
                    _uiState.update { s -> s.copy(editingCell = updated) }
                }
                updated
            } else {
                cell
            }
        }
        _uiState.update {
            it.copy(
                cells = updatedCells,
                canUndo = undoStack.isNotEmpty(),
                canRedo = false
            )
        }
        saveMonthCells(_uiState.value.currentYearMonth, updatedCells)
        refreshSavedMonthsSummaries()
    }

    // Undo / Redo
    fun undo() {
        if (undoStack.isEmpty()) return
        val currentCells = _uiState.value.cells
        redoStack.addLast(currentCells)
        val prevCells = undoStack.removeLast()

        val updatedEditing = _uiState.value.editingCell?.let { cur ->
            prevCells.firstOrNull { it.slotIndex == cur.slotIndex }
        }

        _uiState.update {
            it.copy(
                cells = prevCells,
                editingCell = updatedEditing,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        saveMonthCells(_uiState.value.currentYearMonth, prevCells)
        refreshSavedMonthsSummaries()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentCells = _uiState.value.cells
        undoStack.addLast(currentCells)
        val nextCells = redoStack.removeLast()

        val updatedEditing = _uiState.value.editingCell?.let { cur ->
            nextCells.firstOrNull { it.slotIndex == cur.slotIndex }
        }

        _uiState.update {
            it.copy(
                cells = nextCells,
                editingCell = updatedEditing,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        saveMonthCells(_uiState.value.currentYearMonth, nextCells)
        refreshSavedMonthsSummaries()
    }

    // Edit Mode & Layout Adjustments
    fun toggleEditMode() {
        _uiState.update {
            val nextState = !it.isEditMode
            it.copy(
                isEditMode = nextState,
                selectedElement = if (nextState) EditableElement.MONTH else EditableElement.NONE
            )
        }
    }

    fun selectElement(element: EditableElement) {
        _uiState.update { it.copy(selectedElement = element) }
    }

    fun openEditModeWithElement(element: EditableElement) {
        _uiState.update {
            it.copy(
                isEditMode = true,
                selectedElement = element
            )
        }
    }

    fun dragElement(element: EditableElement, deltaX: Float, deltaY: Float) {
        val cfg = _uiState.value.layoutConfig
        val updated = when (element) {
            EditableElement.MONTH -> {
                val newX = (cfg.monthX + deltaX).coerceIn(100f, 980f)
                val newY = (cfg.monthY + deltaY).coerceIn(30f, 600f)
                cfg.copy(
                    monthX = newX,
                    monthY = newY,
                    monthOffsetX = newX - 540f,
                    monthOffsetY = newY - 115f
                )
            }
            EditableElement.YEAR -> {
                val newX = (cfg.yearX + deltaX).coerceIn(100f, 980f)
                val newY = (cfg.yearY + deltaY).coerceIn(50f, 700f)
                cfg.copy(
                    yearX = newX,
                    yearY = newY,
                    yearOffsetX = newX - 540f,
                    yearOffsetY = newY - 225f
                )
            }
            EditableElement.GRID_CARD -> {
                val newOffX = (cfg.cardOffsetX + deltaX).coerceIn(-500f, 500f)
                val newOffY = (cfg.cardOffsetY + deltaY).coerceIn(-500f, 600f)
                cfg.copy(
                    cardOffsetX = newOffX,
                    cardOffsetY = newOffY
                )
            }
            EditableElement.LOGO -> {
                val newOffX = (cfg.logoOffsetX + deltaX).coerceIn(-500f, 500f)
                val newOffY = (cfg.logoOffsetY + deltaY).coerceIn(-500f, 250f)
                cfg.copy(
                    logoOffsetX = newOffX,
                    logoOffsetY = newOffY
                )
            }
            EditableElement.NONE -> cfg
        }
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateSelectedElementScale(scale: Float) {
        val element = _uiState.value.selectedElement
        val cfg = _uiState.value.layoutConfig
        val updated = when (element) {
            EditableElement.MONTH -> cfg.copy(monthScale = scale.coerceIn(0.6f, 1.6f))
            EditableElement.YEAR -> cfg.copy(yearScale = scale.coerceIn(0.6f, 1.6f))
            EditableElement.GRID_CARD -> cfg.copy(cardScale = scale.coerceIn(0.7f, 1.25f))
            EditableElement.LOGO -> cfg.copy(logoScale = scale.coerceIn(0.50f, 2.20f))
            EditableElement.NONE -> cfg
        }
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateMonthFont(font: HeaderFontOption) {
        val updated = _uiState.value.layoutConfig.copy(monthFont = font)
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateYearFont(font: HeaderFontOption) {
        val updated = _uiState.value.layoutConfig.copy(yearFont = font)
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    // Color updates
    fun updateMonthColor(color: Int) {
        val updated = _uiState.value.layoutConfig.copy(monthColor = color)
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateYearColor(color: Int) {
        val updated = _uiState.value.layoutConfig.copy(yearColor = color)
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateCardAccentColor(color: Int) {
        val updated = _uiState.value.layoutConfig.copy(cardAccentColor = color)
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    // Fine card position controls
    fun updateCardOffsetX(offsetX: Float) {
        val cfg = _uiState.value.layoutConfig
        val updated = cfg.copy(cardOffsetX = offsetX.coerceIn(-500f, 500f))
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateCardOffsetY(offsetY: Float) {
        val cfg = _uiState.value.layoutConfig
        val updated = cfg.copy(cardOffsetY = offsetY.coerceIn(-500f, 600f))
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun stepCardOffset(deltaX: Float, deltaY: Float) {
        stepElementOffset(EditableElement.GRID_CARD, deltaX, deltaY)
    }

    fun centerCardX() {
        centerElementX(EditableElement.GRID_CARD)
    }

    fun stepElementOffset(element: EditableElement, deltaX: Float, deltaY: Float) {
        val cfg = _uiState.value.layoutConfig
        val updated = when (element) {
            EditableElement.MONTH -> {
                val newX = (cfg.monthX + deltaX).coerceIn(100f, 980f)
                val newY = (cfg.monthY + deltaY).coerceIn(30f, 600f)
                cfg.copy(
                    monthX = newX,
                    monthY = newY,
                    monthOffsetX = newX - 540f,
                    monthOffsetY = newY - 115f
                )
            }
            EditableElement.YEAR -> {
                val newX = (cfg.yearX + deltaX).coerceIn(100f, 980f)
                val newY = (cfg.yearY + deltaY).coerceIn(50f, 700f)
                cfg.copy(
                    yearX = newX,
                    yearY = newY,
                    yearOffsetX = newX - 540f,
                    yearOffsetY = newY - 225f
                )
            }
            EditableElement.GRID_CARD -> {
                cfg.copy(
                    cardOffsetX = (cfg.cardOffsetX + deltaX).coerceIn(-500f, 500f),
                    cardOffsetY = (cfg.cardOffsetY + deltaY).coerceIn(-500f, 600f)
                )
            }
            EditableElement.LOGO -> {
                cfg.copy(
                    logoOffsetX = (cfg.logoOffsetX + deltaX).coerceIn(-500f, 500f),
                    logoOffsetY = (cfg.logoOffsetY + deltaY).coerceIn(-500f, 250f)
                )
            }
            EditableElement.NONE -> cfg
        }
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun centerElementX(element: EditableElement) {
        val cfg = _uiState.value.layoutConfig
        val (updated, message) = when (element) {
            EditableElement.MONTH -> {
                cfg.copy(monthX = 540f, monthOffsetX = 0f) to "Mês centralizado!"
            }
            EditableElement.YEAR -> {
                cfg.copy(yearX = 540f, yearOffsetX = 0f) to "Ano centralizado!"
            }
            EditableElement.GRID_CARD -> {
                cfg.copy(cardOffsetX = 0f) to "Calendário centralizado!"
            }
            EditableElement.LOGO -> {
                cfg.copy(logoOffsetX = 0f) to "Logomarca centralizada!"
            }
            EditableElement.NONE -> cfg to ""
        }
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
        if (message.isNotEmpty()) {
            showSnackbar(message)
        }
    }

    // Theme & Logo operations (Phase A)
    fun selectTheme(theme: ThemeItem) {
        if (theme.isPro && !ProAccessConfig.isProUnlocked) {
            _uiState.update { it.copy(proPreviewTheme = theme) }
            return
        }
        if (BackgroundManager.hasCustomBackground(getApplication())) {
            BackgroundManager.restoreDefaultBackground(getApplication())
            _uiState.update { it.copy(hasCustomBackground = false) }
        }
        val current = _uiState.value.layoutConfig
        val mColor = try {
            android.graphics.Color.parseColor(theme.palette.monthColorHex)
        } catch (_: Exception) { current.monthColor }
        val yColor = try {
            android.graphics.Color.parseColor(theme.palette.yearColorHex)
        } catch (_: Exception) { current.yearColor }
        val cColor = try {
            android.graphics.Color.parseColor(theme.palette.calendarAccentHex)
        } catch (_: Exception) { current.cardAccentColor }

        var updated = current.copy(
            selectedThemeId = theme.id,
            monthColor = mColor,
            yearColor = yColor,
            cardAccentColor = cColor,
            cardScale = theme.palette.defaultCalendarScale,
            cardOffsetX = 0f
        )
        theme.palette.suggestedMonthFont?.let { font ->
            updated = updated.copy(monthFont = font)
        }
        theme.palette.suggestedYearFont?.let { font ->
            updated = updated.copy(yearFont = font)
        }
        theme.palette.defaultMonthScale?.let { mScale ->
            updated = updated.copy(monthScale = mScale)
        }
        theme.palette.defaultYearScale?.let { yScale ->
            updated = updated.copy(yearScale = yScale)
        }

        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
        showSnackbar("Tema aplicado: ${theme.name}")
    }

    fun dismissProPreview() {
        _uiState.update { it.copy(proPreviewTheme = null) }
    }

    fun onLogoPicked(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = LogoManager.saveLogoFromUri(getApplication(), uri)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val updated = _uiState.value.layoutConfig.copy(logoEnabled = true)
                    _uiState.update {
                        it.copy(
                            hasCustomLogo = true,
                            layoutConfig = updated,
                            selectedElement = EditableElement.LOGO
                        )
                    }
                    saveLayoutConfig(updated)
                    showSnackbar("Logomarca importada com sucesso!")
                } else {
                    showSnackbar("Erro ao processar imagem da logomarca.")
                }
            }
        }
    }

    fun toggleLogoEnabled(enabled: Boolean) {
        val updated = _uiState.value.layoutConfig.copy(logoEnabled = enabled)
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateLogoMode(mode: String) {
        val updated = _uiState.value.layoutConfig.copy(logoMode = mode)
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun updateLogoScale(scale: Float) {
        val updated = _uiState.value.layoutConfig.copy(logoScale = scale.coerceIn(0.50f, 2.20f))
        _uiState.update { it.copy(layoutConfig = updated) }
        saveLayoutConfig(updated)
    }

    fun clearLogo() {
        LogoManager.clearLogo(getApplication())
        val updated = _uiState.value.layoutConfig.copy(logoEnabled = false)
        _uiState.update {
            it.copy(
                hasCustomLogo = false,
                layoutConfig = updated,
                selectedElement = if (it.selectedElement == EditableElement.LOGO) EditableElement.MONTH else it.selectedElement
            )
        }
        saveLayoutConfig(updated)
        showSnackbar("Logomarca removida.")
    }

    fun resetLayout() {
        val defaultCfg = LayoutConfig.DEFAULT
        _uiState.update { it.copy(layoutConfig = defaultCfg) }
        saveLayoutConfig(defaultCfg)
        showSnackbar("Layout e cores restaurados para o padrão original da v1!")
    }

    // Background Management
    fun onCustomBackgroundPicked(uri: Uri) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                BackgroundManager.setCustomBackground(getApplication(), uri)
            }
            result.onSuccess {
                _uiState.update { it.copy(hasCustomBackground = true) }
                showSnackbar("Imagem de fundo atualizada com sucesso!")
            }.onFailure { err ->
                val reason = err.localizedMessage ?: "Formato não suportado ou arquivo corrompido"
                showSnackbar("Erro ao processar imagem: $reason")
            }
        }
    }

    fun onRestoreDefaultBackground() {
        val success = BackgroundManager.restoreDefaultBackground(getApplication())
        if (success) {
            _uiState.update { it.copy(hasCustomBackground = false) }
            showSnackbar("Imagem de fundo padrão restaurada!")
        }
    }

    // Gallery & Share
    fun saveToGallery(context: Context) {
        if (_uiState.value.isSavingGallery || _uiState.value.isSharing) return
        _uiState.update { it.copy(isSavingGallery = true) }

        viewModelScope.launch {
            val res = withContext(Dispatchers.Default) {
                ExportHelper.saveToGallery(
                    context = context,
                    yearMonth = _uiState.value.currentYearMonth,
                    cells = _uiState.value.cells,
                    layoutConfig = _uiState.value.layoutConfig
                )
            }
            _uiState.update { it.copy(isSavingGallery = false) }
            res.onSuccess { msg ->
                showSnackbar(msg)
            }.onFailure { err ->
                showSnackbar("Erro ao salvar imagem: ${err.message}")
            }
        }
    }

    fun shareCalendar(context: Context) {
        if (_uiState.value.isSavingGallery || _uiState.value.isSharing) return
        _uiState.update { it.copy(isSharing = true) }

        viewModelScope.launch {
            val res = withContext(Dispatchers.Default) {
                ExportHelper.shareCalendar(
                    context = context,
                    yearMonth = _uiState.value.currentYearMonth,
                    cells = _uiState.value.cells,
                    layoutConfig = _uiState.value.layoutConfig
                )
            }
            _uiState.update { it.copy(isSharing = false) }
            res.onFailure { err ->
                showSnackbar("Erro ao compartilhar: ${err.message}")
            }
        }
    }

    fun shareCalendarFromHome(context: Context, yearMonth: YearMonth) {
        if (_uiState.value.isSavingGallery || _uiState.value.isSharing) return
        _uiState.update { it.copy(isSharing = true) }

        viewModelScope.launch {
            val savedCells = loadMonthCells(yearMonth)
            val cells = CalendarEngine.generateCells(yearMonth, savedCells)
            val res = withContext(Dispatchers.Default) {
                ExportHelper.shareCalendar(
                    context = context,
                    yearMonth = yearMonth,
                    cells = cells,
                    layoutConfig = _uiState.value.layoutConfig
                )
            }
            _uiState.update { it.copy(isSharing = false) }
            res.onFailure { err ->
                showSnackbar("Erro ao compartilhar: ${err.message}")
            }
        }
    }

    // Reload all data after Backup Restore
    fun reloadAllData() {
        val ym = _uiState.value.currentYearMonth
        val reloadedLayout = loadLayoutConfig()
        val savedCells = loadMonthCells(ym)
        val cells = CalendarEngine.generateCells(ym, savedCells)
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                layoutConfig = reloadedLayout,
                cells = cells,
                editingCell = null,
                canUndo = false,
                canRedo = false
            )
        }
        refreshSavedMonthsSummaries()
        showSnackbar("Dados e layout restaurados do backup!")
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Persistence with SharedPreferences + org.json
    private fun monthKey(yearMonth: YearMonth): String =
        "month_data_${yearMonth.year}_${String.format("%02d", yearMonth.monthValue)}"

    private fun loadMonthCells(yearMonth: YearMonth): Map<Int, CellData> {
        val jsonStr = prefs.getString(monthKey(yearMonth), null) ?: return emptyMap()
        return try {
            val root = JSONObject(jsonStr)
            val slotsArr = root.optJSONArray("slots") ?: return emptyMap()
            val map = mutableMapOf<Int, CellData>()
            for (i in 0 until slotsArr.length()) {
                val obj = slotsArr.getJSONObject(i)
                val slotIndex = obj.getInt("slot")
                val dayNum = if (obj.has("day") && !obj.isNull("day")) obj.getInt("day") else null
                val hasRedX = obj.optBoolean("hasRedX", false)
                val namesArr = obj.optJSONArray("names") ?: JSONArray()
                val names = mutableListOf<String>()
                for (j in 0 until namesArr.length()) {
                    names.add(namesArr.getString(j))
                }
                map[slotIndex] = CellData(slotIndex, dayNum, hasRedX, names)
            }
            map
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    private fun saveMonthCells(yearMonth: YearMonth, cells: List<CellData>) {
        try {
            val root = JSONObject()
            val slotsArr = JSONArray()
            for (c in cells) {
                if (c.hasRedX || c.names.isNotEmpty()) {
                    val obj = JSONObject()
                    obj.put("slot", c.slotIndex)
                    if (c.dayNumber != null) obj.put("day", c.dayNumber)
                    obj.put("hasRedX", c.hasRedX)
                    val namesArr = JSONArray()
                    c.names.forEach { namesArr.put(it) }
                    obj.put("names", namesArr)
                    slotsArr.put(obj)
                }
            }
            root.put("slots", slotsArr)
            prefs.edit().putString(monthKey(yearMonth), root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadLayoutConfig(): LayoutConfig {
        val jsonStr = prefs.getString("layout_config", null)
        return LayoutConfig.fromJson(jsonStr)
    }

    private fun saveLayoutConfig(config: LayoutConfig) {
        prefs.edit().putString("layout_config", config.toJson()).apply()
    }
}
