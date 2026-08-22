package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Equipment
import com.example.domain.model.EquipmentType
import com.example.domain.model.Player
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
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
fun EquipmentDetailDialog(
    equipment: Equipment,
    player: Player?,
    onDismiss: () -> Unit,
    onEquip: (Equipment) -> Unit,
    onUnequip: (Equipment) -> Unit,
    onEnhance: (Equipment) -> Unit,
    onReforge: (Equipment) -> Unit,
    onToggleLock: (Equipment) -> Unit,
    onSell: (Equipment) -> Unit
) {
    val qualityColor = equipment.quality.composeColor
    val slotIcon = when (equipment.type) {
        EquipmentType.WEAPON -> "⚔️"
        EquipmentType.ARMOR -> "🛡️"
        EquipmentType.HELMET -> "🪖"
        EquipmentType.SHOES -> "👢"
        EquipmentType.RING -> "💍"
        EquipmentType.NECKLACE -> "📿"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, qualityColor.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header: Icon, Name, Level, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.5.dp, qualityColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = slotIcon, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = equipment.name,
                                color = qualityColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (equipment.enhanceLevel > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${equipment.enhanceLevel}",
                                    color = GoldPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "【${equipment.quality.displayName}】${equipment.type.displayName}",
                                color = qualityColor,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lv.${equipment.level}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { onToggleLock(equipment) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (equipment.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock",
                            tint = if (equipment.isLocked) GoldPrimary else TextMuted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkSurfaceHighlight)
                Spacer(modifier = Modifier.height(10.dp))

                // Power Score Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚔️ 战力评级", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = "${equipment.powerScore}",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Base Attributes
                Text("基础属性:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceHighlight.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (equipment.baseAttack > 0) {
                        StatRow("⚔️ 攻击力", "+${equipment.totalAttack}", "(强化+${equipment.totalAttack - equipment.baseAttack})", GoldPrimary)
                    }
                    if (equipment.baseHp > 0) {
                        StatRow("❤️ 生命值", "+${equipment.totalHp}", "(强化+${equipment.totalHp - equipment.baseHp})", AccentGreen)
                    }
                    if (equipment.baseDefense > 0) {
                        StatRow("🛡️ 防御力", "+${equipment.totalDefense}", "(强化+${equipment.totalDefense - equipment.baseDefense})", AccentCyan)
                    }
                }

                // Random Affixes
                if (equipment.affixes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("附加词条 (${equipment.affixes.size}/${equipment.quality.maxAffixes}):", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceHighlight.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (affix in equipment.affixes) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${affix.type.iconName} ${affix.type.displayName}", color = TextPrimary, fontSize = 12.sp)
                                Text(affix.formatDisplay().substringAfter(" "), color = qualityColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkSurfaceHighlight)
                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Equip / Unequip
                    if (equipment.isEquipped) {
                        Button(
                            onClick = { onUnequip(equipment) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight)
                        ) {
                            Text("卸下装备", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onEquip(equipment) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
                        ) {
                            Text("立即穿戴", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Enhance & Reforge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Enhance Button
                        Button(
                            onClick = { onEnhance(equipment) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("强化 +${equipment.enhanceLevel + 1}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("成功率:${equipment.enhanceSuccessRate}% 💰${equipment.enhanceCost}", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        // Reforge Button
                        Button(
                            onClick = { onReforge(equipment) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("洗练词条", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("消耗 💎10 灵石", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Sell Button
                    if (!equipment.isEquipped) {
                        OutlinedButton(
                            onClick = { onSell(equipment) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (equipment.isLocked) "已锁定 (无法出售)" else "出售装备 (获得 💰${equipment.sellPrice} 金币)",
                                fontSize = 12.sp,
                                color = if (equipment.isLocked) TextMuted else AccentRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, extra: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (extra != "(强化+0)") {
                Spacer(modifier = Modifier.width(4.dp))
                Text(extra, color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}
