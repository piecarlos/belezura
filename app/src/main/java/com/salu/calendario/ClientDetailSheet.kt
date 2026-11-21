package com.salu.calendario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailSheet(
    clientName: String,
    day: Int,
    month: Int,
    year: Int,
    initialDetail: AppointmentDetail?,
    studioFinanceConfig: StudioFinanceConfig,
    onSave: (AppointmentDetail) -> Unit,
    onSaveAndNotifyWhatsApp: (AppointmentDetail) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var status by rememberSaveable { mutableStateOf(initialDetail?.status ?: AppointmentStatus.SCHEDULED) }
    var serviceText by rememberSaveable { mutableStateOf(initialDetail?.service ?: "") }
    var timeText by rememberSaveable { mutableStateOf(initialDetail?.time ?: "") }
    var phoneText by rememberSaveable { mutableStateOf(initialDetail?.phone ?: "") }
    var priceText by rememberSaveable {
        mutableStateOf(
            if (initialDetail != null && initialDetail.priceCents > 0L) {
                val r = initialDetail.priceCents / 100
                val c = initialDetail.priceCents % 100
                "$r,${String.format("%02d", c)}"
            } else ""
        )
    }
    var depositText by rememberSaveable {
        mutableStateOf(
            if (initialDetail != null && initialDetail.depositCents > 0L) {
                val r = initialDetail.depositCents / 100
                val c = initialDetail.depositCents % 100
                "$r,${String.format("%02d", c)}"
            } else ""
        )
    }
    var paymentMethod by rememberSaveable { mutableStateOf(initialDetail?.paymentMethod ?: PaymentMethod.PIX) }
    var notesText by rememberSaveable { mutableStateOf(initialDetail?.notes ?: "") }

    val formattedDate = remember(day, month, year) {
        WhatsAppHelper.formatClientDate(day, month, year)
    }

    val priceCents = remember(priceText) { parseBrlToCents(priceText) }
    val depositCents = remember(depositText) { parseBrlToCents(depositText) }
    val remainingCents = remember(priceCents, depositCents) { (priceCents - depositCents).coerceAtLeast(0L) }

    fun buildCurrentDetail(): AppointmentDetail {
        return AppointmentDetail(
            service = serviceText.trim(),
            priceCents = priceCents,
            depositCents = depositCents,
            time = timeText.trim(),
            phone = phoneText.trim(),
            paymentMethod = paymentMethod,
            status = status,
            notes = notesText.trim()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clientName.trim(),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E2326)
                    )
                    Text(
                        text = "Ficha de Atendimento • $formattedDate",
                        fontSize = 12.sp,
                        color = Color(0xFF8C5A60),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color(0xFF5E2326)
                    )
                }
            }

            // Status Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppointmentStatus.values().forEach { st ->
                    val isSelected = status == st
                    val activeBg = when (st) {
                        AppointmentStatus.SCHEDULED -> Color(0xFF5E2326)
                        AppointmentStatus.CONFIRMED -> Color(0xFF1565C0)
                        AppointmentStatus.DONE -> Color(0xFF00796B)
                        AppointmentStatus.PAID -> Color(0xFF2E7D32)
                    }
                    Surface(
                        onClick = { status = st },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) activeBg else Color(0xFFF9F5F6),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) activeBg else Color(0xFFE8D7DC)
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = st.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF5E2326),
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Quick Service Presets
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sugestões de Procedimento:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5E2326),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val presetScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(presetScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ServicePreset.DEFAULT_PRESETS.forEach { preset ->
                        Surface(
                            onClick = {
                                serviceText = preset.name
                                if (priceCents == 0L) {
                                    val r = preset.defaultPriceCents / 100
                                    val c = preset.defaultPriceCents % 100
                                    priceText = "$r,${String.format("%02d", c)}"
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF2F5),
                            border = BorderStroke(1.dp, Color(0xFFF3C7D4))
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 11.5.sp,
                                color = Color(0xFFB82B62),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Service & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = serviceText,
                    onValueChange = { serviceText = it },
                    label = { Text("Procedimento / Serviço", fontSize = 12.sp) },
                    placeholder = { Text("Ex: Maquiagem Social", fontSize = 12.sp) },
                    modifier = Modifier.weight(1.3f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Horário", fontSize = 12.sp) },
                    placeholder = { Text("Ex: 14:00", fontSize = 12.sp) },
                    modifier = Modifier.weight(0.7f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )
            }

            // WhatsApp / Phone
            OutlinedTextField(
                value = phoneText,
                onValueChange = { phoneText = it },
                label = { Text("WhatsApp / Celular da Cliente", fontSize = 12.sp) },
                placeholder = { Text("Ex: 11999998888", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB82B62),
                    focusedLabelColor = Color(0xFFB82B62)
                )
            )

            // Price & Deposit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Valor Total (R$)", fontSize = 12.sp) },
                    placeholder = { Text("0,00", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )

                OutlinedTextField(
                    value = depositText,
                    onValueChange = { depositText = it },
                    label = { Text("Sinal / Adiantado (R$)", fontSize = 12.sp) },
                    placeholder = { Text("0,00", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB82B62),
                        focusedLabelColor = Color(0xFFB82B62)
                    )
                )
            }

            // Remaining Banner
            if (priceCents > 0L) {
                val isFullyPaid = status == AppointmentStatus.PAID || (depositCents >= priceCents && priceCents > 0L)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFullyPaid) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                    border = BorderStroke(1.dp, if (isFullyPaid) Color(0xFFA5D6A7) else Color(0xFFFFE082))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isFullyPaid) "✅ Quitado integralmente" else "💵 Restante a receber:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFullyPaid) Color(0xFF1B5E20) else Color(0xFF7F4B00)
                        )
                        Text(
                            text = if (isFullyPaid) formatBrl(priceCents) else formatBrl(remainingCents),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFullyPaid) Color(0xFF1B5E20) else Color(0xFFB82B62)
                        )
                    }
                }
            }

            // Payment Method
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Forma de Pagamento:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5E2326),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        val isSelected = paymentMethod == method
                        Surface(
                            onClick = { paymentMethod = method },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFB82B62) else Color.White,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFB82B62) else Color(0xFFE8D7DC)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (method == PaymentMethod.PIX) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_pix),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isSelected) Color.White else Color(0xFF00BDAE)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    text = method.label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF5E2326)
                                )
                            }
                        }
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Anotações Internas (alergias, preferências, etc.)", fontSize = 12.sp) },
                placeholder = { Text("Ex: Pele sensível, prefere tons terrosos", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB82B62),
                    focusedLabelColor = Color(0xFFB82B62)
                )
            )

            // Privacy Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF3F3F7)
            ) {
                Text(
                    text = "🔒 100% Privado — Valores, sinais, contatos e observações NUNCA aparecem nos Stories ou imagens exportadas.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    lineHeight = 14.sp
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val detail = buildCurrentDetail()
                        onSave(detail)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFB82B62)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB82B62))
                ) {
                    Text("Salvar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        val detail = buildCurrentDetail()
                        onSave(detail)
                        onSaveAndNotifyWhatsApp(detail)
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_whatsapp),
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Salvar e Avisar", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }
}
