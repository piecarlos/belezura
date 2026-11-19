package com.salu.calendario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth

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
                        textStyle = TextStyle(
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

// Dialog de confirmação para restauração de backup
@Composable
fun ConfirmRestoreDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Restaurar Backup", fontWeight = FontWeight.Bold, color = Color(0xFF5E2326))
        },
        text = {
            Text("Isso substituirá todos os meses e o layout atuais pelos dados do backup. Continuar?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB82B62))
            ) {
                Text("Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
