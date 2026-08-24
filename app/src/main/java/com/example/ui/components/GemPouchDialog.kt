package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.example.domain.model.Gem
import com.example.domain.model.GemCategory
import com.example.domain.model.GemType
import com.example.domain.model.Player
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GemPouchDialog(
    player: Player,
    onDismiss: () -> Unit,
    onSynthesizeGem: (GemType, Int) -> Unit,
    onSynthesizeAllGems: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<GemCategory?>(null) }

    val allGems = player.getAllGemsList().filter {
        selectedCategoryFilter == null || it.type.category == selectedCategoryFilter
    }

    val totalGemCount = player.gemInventory.values.sum()
    val canSynthesizeAny = player.getAllGemsList().any { it.count >= 3 && it.level < 10 }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎 乾坤宝石囊", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("($totalGemCount 颗)", color = TextSecondary, fontSize = 12.sp)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = "【3合1规则】消耗 3 颗同类型同等级宝石，可进阶合成 1 颗高 1 级宝石！",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Actions and Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("全部", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldDark,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceElevated,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                        items(GemCategory.entries) { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (cat == GemCategory.OFFENSIVE) AccentRed.copy(alpha = 0.5f) else AccentCyan.copy(alpha = 0.5f),
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceElevated,
                                    labelColor = if (cat == GemCategory.OFFENSIVE) AccentRed else AccentCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onSynthesizeAllGems,
                        enabled = canSynthesizeAny,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("一键合成", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DarkSurfaceHighlight)
                Spacer(modifier = Modifier.height(10.dp))

                // Gem List
                if (allGems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💠", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("宝石囊暂无该类宝石", color = TextSecondary, fontSize = 13.sp)
                            Text("通关领主或在万宝阁中可获得极品宝石！", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allGems) { item ->
                            val gem = Gem(item.type, item.level)
                            val count = item.count
                            val canSynthesize = count >= 3 && gem.level < 10

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkSurfaceHighlight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(gem.iconEmoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = gem.fullName,
                                                color = GoldPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (gem.type.category == GemCategory.OFFENSIVE) AccentRed.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(3.dp),
                                                border = BorderStroke(0.5.dp, if (gem.type.category == GemCategory.OFFENSIVE) AccentRed else AccentCyan)
                                            ) {
                                                Text(
                                                    text = gem.type.category.displayName,
                                                    color = if (gem.type.category == GemCategory.OFFENSIVE) AccentRed else AccentCyan,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${gem.description} (拥有: $count 颗)",
                                            color = if (canSynthesize) AccentGreen else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (gem.level < 10) {
                                    Button(
                                        onClick = { onSynthesizeGem(gem.type, gem.level) },
                                        enabled = canSynthesize,
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = if (canSynthesize) "合成 Lv.${gem.level + 1}" else "$count/3",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canSynthesize) Color.White else TextMuted
                                        )
                                    }
                                } else {
                                    Text("已满级", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
