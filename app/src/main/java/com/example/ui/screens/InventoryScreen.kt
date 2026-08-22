package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Equipment
import com.example.domain.model.EquipmentQuality
import com.example.domain.model.EquipmentType
import com.example.domain.model.Player
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InventoryScreen(
    player: Player,
    inventoryItems: List<Equipment>,
    onSelectEquipment: (Equipment) -> Unit,
    onBatchSell: (EquipmentQuality?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeFilter by remember { mutableStateOf<EquipmentType?>(null) }
    var selectedQualityFilter by remember { mutableStateOf<EquipmentQuality?>(null) }
    var showBatchSellDialog by remember { mutableStateOf(false) }

    val filteredItems = inventoryItems.filter { item ->
        (selectedTypeFilter == null || item.type == selectedTypeFilter) &&
        (selectedQualityFilter == null || item.quality == selectedQualityFilter)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp)
    ) {
        // Top Info & Batch Sell Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkSurfaceHighlight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎒 储物行囊", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${inventoryItems.size}/40)",
                            color = if (inventoryItems.size >= 35) AccentRed else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("点击装备查看属性、强化、洗练词条与熔炼", color = TextMuted, fontSize = 10.sp)
                }

                Button(
                    onClick = { showBatchSellDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("🔥 一键熔炼", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("全部位", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldDark,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }

            items(EquipmentType.entries) { type ->
                FilterChip(
                    selected = selectedTypeFilter == type,
                    onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                    label = { Text(type.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldDark,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quality Filter Chips Row (7 tiers)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedQualityFilter == null,
                    onClick = { selectedQualityFilter = null },
                    label = { Text("全部质", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldDark,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }

            items(EquipmentQuality.entries) { quality ->
                FilterChip(
                    selected = selectedQualityFilter == quality,
                    onClick = { selectedQualityFilter = if (selectedQualityFilter == quality) null else quality },
                    label = {
                        Text(
                            text = quality.displayName,
                            fontSize = 11.sp,
                            color = if (selectedQualityFilter == quality) Color.White else quality.composeColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = quality.composeColor.copy(alpha = 0.5f),
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = quality.composeColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of Inventory Items (or empty state)
        if (inventoryItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("行囊空空如也", color = TextSecondary, fontSize = 14.sp)
                    Text("前往历练打怪与击杀关底领主，获取强力神装！", color = TextMuted, fontSize = 11.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val qualityColor = item.quality.composeColor
                    val icon = when (item.type) {
                        EquipmentType.WEAPON -> "⚔️"
                        EquipmentType.ARMOR -> "🛡️"
                        EquipmentType.HELMET -> "🪖"
                        EquipmentType.SHOES -> "👢"
                        EquipmentType.RING -> "💍"
                        EquipmentType.NECKLACE -> "📿"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectEquipment(item) },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, qualityColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 20.sp)
                                if (item.isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = GoldPrimary,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .align(Alignment.TopStart)
                                    )
                                }
                                if (item.enhanceLevel > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(bottomStart = 4.dp))
                                            .background(GoldPrimary)
                                            .padding(horizontal = 2.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "+${item.enhanceLevel}",
                                            color = Color.Black,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.name,
                                color = qualityColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Lv.${item.level}",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Batch Sell Dialog with Quality Selection
    if (showBatchSellDialog) {
        Dialog(onDismissRequest = { showBatchSellDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, AccentRed)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥 一键熔炼行囊装备", color = AccentRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showBatchSellDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "关闭", tint = TextMuted)
                        }
                    }
                    Text("熔炼装备可获得金币与大量【强化石】。已锁定的装备不会被熔炼！", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    val batchOptions = listOf(
                        "熔炼【白色】及以下" to EquipmentQuality.WHITE,
                        "熔炼【绿色】及以下" to EquipmentQuality.GREEN,
                        "熔炼【蓝色】及以下" to EquipmentQuality.BLUE,
                        "熔炼【黄色】及以下" to EquipmentQuality.YELLOW,
                        "熔炼【红色】及以下" to EquipmentQuality.RED,
                        "熔炼【金色】及以下" to EquipmentQuality.GOLD,
                        "熔炼全部未锁定装备" to null
                    )

                    batchOptions.forEach { (label, quality) ->
                        val count = inventoryItems.count {
                            !it.isLocked && (quality == null || it.quality.ordinal <= quality.ordinal)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .clickable(enabled = count > 0) {
                                    onBatchSell(quality)
                                    showBatchSellDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = if (count > 0) (quality?.composeColor ?: TextPrimary) else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "共 $count 件",
                                    color = if (count > 0) GoldPrimary else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
