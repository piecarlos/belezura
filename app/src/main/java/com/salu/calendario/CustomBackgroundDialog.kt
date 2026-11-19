package com.salu.calendario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomBackgroundDialog(
    hasCustomBackground: Boolean,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onRestoreDefault: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Personalizar moldura",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF5E2326)
            )
        },
        text = {
            Text(
                text = "Para melhores resultados, use uma imagem vertical (9:16). Imagens em outros formatos (JPG, PNG, WebP) também funcionam e serão ajustadas automaticamente com zoom central.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF4A4A4A)
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onPickImage()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64886))
                ) {
                    Text(
                        text = "Escolher imagem",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasCustomBackground) {
                        TextButton(
                            onClick = {
                                onDismiss()
                                onRestoreDefault()
                            }
                        ) {
                            Text(
                                text = "Restaurar padrão floral",
                                color = Color(0xFF8B4A5E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancelar",
                            color = Color(0xFF7A7A7A)
                        )
                    }
                }
            }
        },
        dismissButton = null
    )
}
