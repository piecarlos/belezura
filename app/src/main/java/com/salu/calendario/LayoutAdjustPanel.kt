package com.salu.calendario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils

enum class LayoutToolTab {
    POSITION,
    FONT,
    COLOR,
    STYLE
}

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
    onCloseEditMode: () -> Unit,
    onUploadLogo: () -> Unit = {},
    onToggleLogoEnabled: (Boolean) -> Unit = {},
    onLogoModeChange: (String) -> Unit = {},
    onClearLogo: () -> Unit = {}
) {
    var activeTab by rememberSaveable { mutableStateOf(LayoutToolTab.POSITION) }

    val currentElement = if (uiState.selectedElement == EditableElement.NONE) {
        EditableElement.MONTH
    } else {
        uiState.selectedElement
    }

    // Auto-fallback based on selected element
    val effectiveTab = when {
        currentElement == EditableElement.LOGO && (activeTab == LayoutToolTab.FONT || activeTab == LayoutToolTab.COLOR) -> {
            LayoutToolTab.STYLE
        }
        currentElement == EditableElement.GRID_CARD && activeTab == LayoutToolTab.FONT -> {
            LayoutToolTab.POSITION
        }
        currentElement != EditableElement.LOGO && activeTab == LayoutToolTab.STYLE -> {
            LayoutToolTab.POSITION
        }
        else -> activeTab
    }

    val currentElementColor = when (currentElement) {
        EditableElement.MONTH -> uiState.layoutConfig.monthColor
        EditableElement.YEAR -> uiState.layoutConfig.yearColor
        EditableElement.GRID_CARD -> uiState.layoutConfig.cardAccentColor
        EditableElement.LOGO -> uiState.layoutConfig.cardAccentColor
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
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
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

                    FilterChip(
                        selected = currentElement == EditableElement.LOGO,
                        onClick = { onSelectElement(EditableElement.LOGO) },
                        label = { Text("Logo", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB82B62),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentElement == EditableElement.LOGO)
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

            // Logo without file uploaded yet: friendly inline banner
            if (currentElement == EditableElement.LOGO && !uiState.hasCustomLogo) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0F4)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Adicionar Logomarca",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF5E2326)
                            )
                            Text(
                                "Aparece com destaque no rodapé",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Button(
                            onClick = onUploadLogo,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Enviar Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Row 2: Tool Sub-Tabs (Posição | Fonte/Estilo | Cor)
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

                    if (currentElement == EditableElement.LOGO) {
                        // Tab Estilo for Logo
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (effectiveTab == LayoutToolTab.STYLE) Color(0xFFB82B62) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .clickable { activeTab = LayoutToolTab.STYLE }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Estilo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (effectiveTab == LayoutToolTab.STYLE) Color.White else Color(0xFF5E2326)
                                )
                            }
                        }
                    } else {
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
                            EditableElement.LOGO -> uiState.layoutConfig.logoScale
                            EditableElement.NONE -> 1f
                        }

                        val scaleRange = when (currentElement) {
                            EditableElement.GRID_CARD -> 0.70f..1.25f
                            EditableElement.LOGO -> 0.50f..2.20f
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
                                        EditableElement.LOGO -> {}
                                        EditableElement.NONE -> {}
                                    }
                                }
                            )
                        }
                    }

                    LayoutToolTab.STYLE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (uiState.layoutConfig.logoEnabled) "Exibindo no rodapé" else "Oculto no rodapé",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF5E2326)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    androidx.compose.material3.Switch(
                                        checked = uiState.layoutConfig.logoEnabled,
                                        onCheckedChange = onToggleLogoEnabled,
                                        modifier = Modifier.height(24.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = onUploadLogo,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Trocar", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = onClearLogo,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Remover", fontSize = 11.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val modes = listOf(
                                    "ORIGINAL" to "Original",
                                    "REMOVE_WHITE" to "Sem fundo branco",
                                    "CIRCLE_BADGE" to "Selo redondo"
                                )
                                modes.forEach { (modeKey, modeTitle) ->
                                    FilterChip(
                                        selected = uiState.layoutConfig.logoMode == modeKey,
                                        onClick = { onLogoModeChange(modeKey) },
                                        label = { Text(modeTitle, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFB82B62),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
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
