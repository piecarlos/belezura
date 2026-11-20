package com.salu.calendario

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoEditorScreen(
    uiState: CalendarUiState,
    onNavigateHome: () -> Unit,
    onSelectTemplate: (PromoTemplateType) -> Unit,
    onUpdateDraft: (PromoDraft) -> Unit,
    onResetToPreset: (PromoTemplateType) -> Unit,
    onOpenThemeGallery: () -> Unit,
    onOpenLogoSettings: () -> Unit,
    onSaveToGallery: (Context) -> Unit,
    onShare: (Context) -> Unit
) {
    val context = LocalContext.current
    var showEditTextsSheet by rememberSaveable { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão secundário / tonal "Editar Textos"
                    OutlinedButton(
                        onClick = { showEditTextsSheet = true },
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
                        Text("Textos", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
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
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectTemplate(templateType) },
                        label = {
                            Text(
                                text = templateType.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB82B62),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF5E2326)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFFF3C7D4),
                            selectedBorderColor = Color(0xFFB82B62),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        )
                    )
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
                    onTap = { showEditTextsSheet = true }
                )
            }
        }
    }

    // Modal BottomSheet for Text and Item Editing
    if (showEditTextsSheet) {
        PromoEditBottomSheet(
            promoDraft = uiState.promoDraft,
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
    onTap: () -> Unit
) {
    val context = LocalContext.current

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
                    detectTapGestures(onTap = { onTap() })
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

    fun commitChanges() {
        onUpdateDraft(
            promoDraft.copy(
                badgeText = badgeText,
                title = title,
                subtitle = subtitle,
                footerCta = footerCta,
                showLogo = showLogo,
                items = items
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            commitChanges()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDFD),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Editar Conteúdo",
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

            // Scrollable fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selo Superior (Pill Badge)
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

                // Título Principal
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

                // Subtítulo
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = {
                        subtitle = it
                        commitChanges()
                    },
                    label = { Text("Subtítulo curto") },
                    placeholder = { Text("Ex: Garanta seu horário...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )

                // Toggle Logomarca
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF5F8), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Exibir minha logomarca",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF5E2326)
                        )
                        Text(
                            text = "Renderiza sua logo cadastrada no topo",
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
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFB82B62))
                    )
                }

                // Lista Dinâmica de Itens
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
                            fontSize = 14.sp,
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Label Field
                                val labelHint = when (promoDraft.templateType) {
                                    PromoTemplateType.AGENDA_ABERTA -> "Dia (Ex: Sexta)"
                                    PromoTemplateType.TABELA_PRECOS -> "Serviço"
                                    PromoTemplateType.AVISO_REGRAS -> "Regra / Tópico"
                                }
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
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFB82B62)
                                    )
                                )

                                // Value Field
                                val valueHint = when (promoDraft.templateType) {
                                    PromoTemplateType.AGENDA_ABERTA -> "Horários"
                                    PromoTemplateType.TABELA_PRECOS -> "Preço (R$)"
                                    PromoTemplateType.AVISO_REGRAS -> "Detalhe"
                                }
                                OutlinedTextField(
                                    value = item.value,
                                    onValueChange = { newVal ->
                                        items = items.toMutableList().also {
                                            it[index] = it[index].copy(value = newVal)
                                        }
                                        commitChanges()
                                    },
                                    label = { Text(valueHint, fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFB82B62)
                                    )
                                )

                                // Remove Button (se houver mais de 1 item)
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
                        }
                    }
                }

                // Rodapé CTA
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

                // Botão Restaurar texto modelo
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
                    Text("Restaurar texto modelo original", color = Color(0xFFB82B62), fontSize = 13.sp)
                }
            }

            // Ação Concluir
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
                Text("Concluir Edição", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
