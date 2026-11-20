package com.salu.calendario

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeGallerySheet(
    currentThemeId: String,
    hasCustomBackground: Boolean,
    hasCustomLogo: Boolean,
    initialCategory: ThemeCategory = ThemeCategory.ALL,
    onSelectTheme: (ThemeItem) -> Unit,
    onPickCustomBackground: () -> Unit,
    onResetToDefaultBackground: () -> Unit,
    onOpenLogoSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    val seasonalHighlight = remember { SeasonalRadar.getActiveCampaign() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val themes = remember(selectedCategory) {
        ThemeCatalog.getThemesByCategory(selectedCategory)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDFD),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Galeria de Molduras",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E2326)
                    )
                    Text(
                        text = "Escolha o visual do seu calendário",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color(0xFF5E2326)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Seasonal Radar Banner
            if (seasonalHighlight != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE64886)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = seasonalHighlight.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF5E2326)
                                )
                                if (seasonalHighlight.daysRemaining > 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "em ${seasonalHighlight.daysRemaining} dias",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE64886)
                                    )
                                }
                            }
                            Text(
                                text = seasonalHighlight.subtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF6B4345),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Quick Actions: Minha Foto & Minha Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPickCustomBackground,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF5E2326)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFB82B62)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (hasCustomBackground) "Trocar Foto" else "Minha Foto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onOpenLogoSettings,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF5E2326)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.BrandingWatermark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFB82B62)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (hasCustomLogo) "Editar Logo" else "Minha Logo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (hasCustomBackground) {
                    IconButton(
                        onClick = onResetToDefaultBackground,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Voltar ao padrão",
                            tint = Color(0xFFB82B62)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThemeCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB82B62),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // 9:16 Theme Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(themes, key = { it.id }) { theme ->
                    val isSelected = (!hasCustomBackground && currentThemeId == theme.id)
                    ThemeCard(
                        theme = theme,
                        isSelected = isSelected,
                        onClick = { onSelectTheme(theme) }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: ThemeItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = remember(theme.accentHex) {
        try {
            Color(android.graphics.Color.parseColor(theme.accentHex))
        } catch (_: Exception) {
            Color(0xFFB82B62)
        }
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.5.dp, Color(0xFFE64886), RoundedCornerShape(10.dp))
                } else {
                    Modifier.border(1.dp, Color(0xFFE89BAE).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                }
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background preview: real image for builtin/drawable themes or faithful gradient for others
            if (theme.drawableResId != null) {
                Image(
                    painter = painterResource(id = theme.drawableResId),
                    contentDescription = theme.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (theme.id == "floral_default") {
                Image(
                    painter = painterResource(id = R.drawable.bg_base),
                    contentDescription = theme.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val bgBrush = remember(theme.id) {
                    when (theme.id) {
                        "black_friday_glam" -> Brush.verticalGradient(
                            listOf(Color(0xFF141314), Color(0xFF282226))
                        )
                        "rose_editorial" -> Brush.verticalGradient(
                            listOf(Color(0xFFFFF2F5), Color(0xFFFDF7F0))
                        )
                        "mothers_day_floral" -> Brush.verticalGradient(
                            listOf(Color(0xFFFFF4F7), Color(0xFFFBE8EE))
                        )
                        "valentines_romance" -> Brush.verticalGradient(
                            listOf(Color(0xFFFFF1F3), Color(0xFFFCE4E8))
                        )
                        "pink_october" -> Brush.verticalGradient(
                            listOf(Color(0xFFFFF0F5), Color(0xFFFCE0EA))
                        )
                        "christmas_gold" -> Brush.verticalGradient(
                            listOf(Color(0xFFFAF7EF), Color(0xFFF5EEDD))
                        )
                        "summer_tropical" -> Brush.verticalGradient(
                            listOf(Color(0xFFF8FBF8), Color(0xFFEBF3EE))
                        )
                        "minimal_cream" -> Brush.verticalGradient(
                            listOf(Color(0xFFFAF6F2), Color(0xFFF3ECE4))
                        )
                        "luxury_gold" -> Brush.verticalGradient(
                            listOf(Color(0xFFFDFBF7), Color(0xFFF7F2E7))
                        )
                        else -> Brush.verticalGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5))
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgBrush)
                )

                // Subtle inner frame border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .border(
                            width = if (theme.id == "luxury_gold" || theme.id == "black_friday_glam") 1.dp else 0.6.dp,
                            color = accentColor.copy(alpha = if (theme.id == "black_friday_glam") 0.6f else 0.4f),
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            }

            // Subtle calendar grid mini-sketch in center
            val isDark = (theme.id == "black_friday_glam")
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(0.72f)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (isDark) Color(0x3DFFFFFF) else Color(0x8AFFFFFF)
                    )
                    .border(
                        0.5.dp,
                        accentColor.copy(alpha = if (isDark) 0.7f else 0.45f),
                        RoundedCornerShape(5.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini header: Month/Year simulated indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(accentColor.copy(alpha = 0.85f))
                        )
                    }

                    // Mini calendar grid dots: 4 rows of 5 dots
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        repeat(4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(5) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) accentColor.copy(alpha = 0.75f)
                                                else accentColor.copy(alpha = 0.45f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // PRO Diamond Badge
            if (theme.isPro) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF5E2326))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Diamond,
                            contentDescription = "PRO",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "PRO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Selected Checkmark Badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE64886)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selecionado",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Bottom Label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.94f))
                    .padding(horizontal = 4.dp, vertical = 5.dp)
            ) {
                Text(
                    text = theme.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5E2326),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
