package com.salu.calendario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.util.Locale

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
    onMoveDown: (Int) -> Unit,
    onCheckDuplicateName: (String) -> ClientSearchResult? = { null }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newNameText by rememberSaveable { mutableStateOf("") }
    var whatsAppClientName by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    val duplicateMatch = remember(newNameText) {
        if (newNameText.trim().length >= 2) {
            onCheckDuplicateName(newNameText.trim())
        } else null
    }

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
                        placeholder = { Text("Ex: Maria") },
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

                if (duplicateMatch != null) {
                    val locationDesc = if (duplicateMatch.yearMonth == yearMonth) {
                        "dia ${duplicateMatch.day}"
                    } else {
                        "${CalendarEngine.getMonthDisplayName(duplicateMatch.yearMonth)}/${duplicateMatch.yearMonth.year} (dia ${duplicateMatch.day})"
                    }
                    val exampleFirstName = duplicateMatch.name.trim().split(" ").firstOrNull() ?: duplicateMatch.name
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF8E1),
                        border = BorderStroke(1.dp, Color(0xFFFFE082))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Já existe '${duplicateMatch.name}' agendada ($locationDesc). Sugestão: adicione sobrenome ou lembrete (ex: $exampleFirstName Silva - 14h).",
                                fontSize = 11.5.sp,
                                color = Color(0xFF7F4B00),
                                lineHeight = 15.sp
                            )
                        }
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

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Avisar no WhatsApp
                                    IconButton(
                                        onClick = { whatsAppClientName = name },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "Avisar no WhatsApp",
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

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

    // Modal WhatsApp Message Dialog
    whatsAppClientName?.let { clientName ->
        cell.dayNumber?.let { day ->
            WhatsAppMessageDialog(
                clientName = clientName,
                day = day,
                month = yearMonth.monthValue,
                year = yearMonth.year,
                onDismiss = { whatsAppClientName = null }
            )
        }
    }
}
