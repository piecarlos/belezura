package com.salu.calendario

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: CalendarUiState,
    onOpenEditorForMonth: (YearMonth) -> Unit,
    onOpenDayFromSearch: (ClientSearchResult) -> Unit,
    onShareMonthFromHome: (YearMonth) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCustomBackgroundPicked: (android.net.Uri) -> Unit,
    onRestoreDefaultBackground: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onOpenQuickDatePicker: () -> Unit,
    onOpenThemeGallery: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showBgDialog by rememberSaveable { mutableStateOf(false) }
    var showBackupMenu by rememberSaveable { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onCustomBackgroundPicked(uri)
        }
    }

    // Active month summary calculation
    val activeYm = uiState.currentYearMonth
    val activeSummary = uiState.savedMonthsSummaries.firstOrNull { it.yearMonth == activeYm }
    val activeNamesCount = activeSummary?.namesCount ?: uiState.cells.sumOf { it.names.size }
    val activeRedXCount = activeSummary?.redXCount ?: uiState.cells.count { it.hasRedX }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFFFFFDFD)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Header Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Agenda Salú",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E2326)
                    )
                    Text(
                        text = "Painel rápido e gerador de agendas",
                        fontSize = 13.sp,
                        color = Color(0xFF8C5A60)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFFCEBF1), CircleShape)
                        .border(1.5.dp, Color(0xFFE89BAE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Logo",
                        tint = Color(0xFFB82B62),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 2. Active Month Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFE89BAE), Color(0xFFFCEBF1))))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MÊS ATIVO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB82B62),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = CalendarEngine.getHeaderTitle(activeYm),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E2326)
                            )
                        }

                        // Status badges
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$activeNamesCount agendamento${if (activeNamesCount == 1) "" else "s"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E6B53)
                            )
                            if (activeRedXCount > 0) {
                                Text(
                                    text = "$activeRedXCount dia${if (activeRedXCount == 1) "" else "s"} com 'X'",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }

                    // Action buttons side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenEditorForMonth(activeYm) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Abrir Agenda", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = { onShareMonthFromHome(activeYm) },
                            enabled = !uiState.isSharing,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFB82B62), Color(0xFFE89BAE))))
                        ) {
                            if (uiState.isSharing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFFB82B62),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Gerando...", fontSize = 12.sp, color = Color(0xFFB82B62))
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFB82B62), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("PNG", fontWeight = FontWeight.SemiBold, color = Color(0xFFB82B62), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 3. Client Search Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5F7)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Buscar Cliente (Todos os meses)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF5E2326),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: Jessica, Glória, Milka...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF8C5A60)) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpar", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Search Results List
                    AnimatedVisibility(visible = uiState.searchQuery.trim().length >= 2) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (uiState.searchResults.isEmpty()) {
                                Text(
                                    text = "Nenhum agendamento encontrado para '${uiState.searchQuery}'.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            } else {
                                Text(
                                    text = "${uiState.searchResults.size} resultado${if (uiState.searchResults.size == 1) "" else "s"} encontrado${if (uiState.searchResults.size == 1) "" else "s"}:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF8C5A60)
                                )

                                uiState.searchResults.take(8).forEach { result ->
                                    val monthName = CalendarEngine.getMonthDisplayName(result.yearMonth)
                                        .lowercase()
                                        .replaceFirstChar { it.titlecase(Locale("pt", "BR")) }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .clickable { onOpenDayFromSearch(result) }
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = result.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1A1A1A)
                                        )

                                        Text(
                                            text = "${result.day} de $monthName ${result.yearMonth.year}",
                                            fontSize = 12.sp,
                                            color = Color(0xFFB82B62),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. "Meus Meses" Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Meus Meses (${uiState.savedMonthsSummaries.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E2326)
                    )

                    TextButton(onClick = onOpenQuickDatePicker) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFB82B62), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Outro mês", color = Color(0xFFB82B62), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (uiState.savedMonthsSummaries.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5F7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Nenhum outro mês salvo ainda. Toque em 'Abrir Agenda' acima para começar a preencher!",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.savedMonthsSummaries) { summary ->
                            val isSelected = summary.yearMonth == activeYm
                            val shortMonth = CalendarEngine.getMonthDisplayName(summary.yearMonth).take(3)

                            Card(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable { onOpenEditorForMonth(summary.yearMonth) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFFCEBF1) else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) {
                                    CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFB82B62), Color(0xFFE89BAE))))
                                } else {
                                    CardDefaults.outlinedCardBorder()
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "$shortMonth ${summary.yearMonth.year}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF5E2326)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${summary.namesCount} nomes",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E6B53),
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (summary.redXCount > 0) {
                                        Text(
                                            text = "${summary.redXCount} com X",
                                            fontSize = 11.sp,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Maintenance Shortcuts (Footer)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aparência e Backup",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5E2326),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Backup Card
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBackupMenu = true },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFFB82B62), modifier = Modifier.size(18.dp))
                                    Text("Backup (.json)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF5E2326))
                                }
                                Spacer(Modifier.height(4.dp))
                                val backupSubtitle = uiState.lastBackupTimeFormatted?.let { "Último: $it" } ?: "Nenhum backup ainda"
                                Text(text = backupSubtitle, fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        DropdownMenu(
                            expanded = showBackupMenu,
                            onDismissRequest = { showBackupMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exportar backup (.json)") },
                                leadingIcon = { Icon(Icons.Default.CloudUpload, null) },
                                onClick = {
                                    showBackupMenu = false
                                    onExportBackup()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Restaurar backup (.json)") },
                                leadingIcon = { Icon(Icons.Default.CloudDownload, null) },
                                onClick = {
                                    showBackupMenu = false
                                    onImportBackup()
                                }
                            )
                        }
                    }

                    // Background & Themes Card
                    Box(modifier = Modifier.weight(1f)) {
                        val currentThemeName = remember(uiState.layoutConfig.selectedThemeId) {
                            ThemeCatalog.getThemeById(uiState.layoutConfig.selectedThemeId)?.name ?: "Floral Clássico"
                        }
                        val themeSubtitle = if (uiState.hasCustomBackground) "Foto personalizada" else currentThemeName

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenThemeGallery() },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFB82B62), modifier = Modifier.size(18.dp))
                                    Text("Molduras e Temas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF5E2326))
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = themeSubtitle,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}
