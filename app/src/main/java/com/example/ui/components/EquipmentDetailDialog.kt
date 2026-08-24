package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.example.domain.model.EquipmentType
import com.example.domain.model.Gem
import com.example.domain.model.GemCategory
import com.example.domain.model.GemType
import com.example.domain.model.Player
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
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
    equippedComparison: Equipment? = null,
    player: Player?,
    onDismiss: () -> Unit,
    onEquip: (Equipment) -> Unit,
    onUnequip: (Equipment) -> Unit,
    onEnhance: (Equipment) -> Unit,
    onReforge: (Equipment) -> Unit,
    onToggleLock: (Equipment) -> Unit,
    onSell: (Equipment) -> Unit,
    onSocketGem: ((Equipment, GemType, Int) -> Unit)? = null,
    onUnsocketGem: ((Equipment, Int) -> Unit)? = null
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

    var showGemPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, qualityColor.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header: Icon, Name, Level, Quality, Lock, Close
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
                            if (equipment.isEquipped) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = AccentGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, AccentGreen)
                                ) {
                                    Text(
                                        text = "已穿戴",
                                        color = AccentGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
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

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DarkSurfaceHighlight)
                Spacer(modifier = Modifier.height(10.dp))

                // Power Score & Tier Badge (Requirement #2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚔️ 装备战力评分", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (equipment.ratingTier) {
                                "SSS" -> AccentPurple.copy(alpha = 0.3f)
                                "SS" -> GoldPrimary.copy(alpha = 0.3f)
                                "S" -> AccentRed.copy(alpha = 0.3f)
                                "A" -> AccentCyan.copy(alpha = 0.3f)
                                else -> TextMuted.copy(alpha = 0.3f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when (equipment.ratingTier) {
                                    "SSS" -> AccentPurple
                                    "SS" -> GoldPrimary
                                    "S" -> AccentRed
                                    "A" -> AccentCyan
                                    else -> TextMuted
                                }
                            )
                        ) {
                            Text(
                                text = equipment.ratingTier,
                                color = when (equipment.ratingTier) {
                                    "SSS" -> AccentPurple
                                    "SS" -> GoldPrimary
                                    "S" -> AccentRed
                                    "A" -> AccentCyan
                                    else -> TextPrimary
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${equipment.powerScore}",
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!equipment.isEquipped && equippedComparison != null) {
                            val scoreDiff = equipment.powerScore - equippedComparison.powerScore
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (scoreDiff >= 0) "(+$scoreDiff)" else "($scoreDiff)",
                                color = if (scoreDiff >= 0) AccentGreen else AccentRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Equipment Comparison View (Requirement #1)
                if (!equipment.isEquipped && equippedComparison != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GoldDark.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔄 穿戴对比 (对比当前已装备)",
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "【${equippedComparison.quality.displayName}】${equippedComparison.name} (+${equippedComparison.enhanceLevel})",
                                    color = equippedComparison.quality.composeColor,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Comparison Stat Matrix
                            ComparisonStatRow(
                                label = "攻击力",
                                selectedVal = equipment.totalAttack.toLong(),
                                equippedVal = equippedComparison.totalAttack.toLong()
                            )
                            ComparisonStatRow(
                                label = "生命值",
                                selectedVal = equipment.totalHp.toLong(),
                                equippedVal = equippedComparison.totalHp.toLong()
                            )
                            ComparisonStatRow(
                                label = "防御力",
                                selectedVal = equipment.totalDefense.toLong(),
                                equippedVal = equippedComparison.totalDefense.toLong()
                            )
                            ComparisonStatRow(
                                label = "宝石孔位",
                                selectedVal = equipment.maxSockets.toLong(),
                                equippedVal = equippedComparison.maxSockets.toLong()
                            )
                        }
                    }
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

                // Random Affixes (Requirement #2)
                if (equipment.affixes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "附加战斗词条 (${equipment.affixes.size}/${equipment.quality.maxAffixes}):",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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

                // Gem Sockets Section (Requirements #3, #4, #5)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💎 宝石镶嵌 (${equipment.gems.size}/${equipment.maxSockets})",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (equipment.type.allowedGemCategory == GemCategory.OFFENSIVE) "⚔️ 仅限攻击类宝石" else "🛡️ 仅限防守类宝石",
                        color = if (equipment.type.allowedGemCategory == GemCategory.OFFENSIVE) AccentRed else AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (equipment.maxSockets > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceHighlight.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until equipment.maxSockets) {
                            if (i < equipment.gems.size) {
                                val gem = equipment.gems[i]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceElevated)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(gem.iconEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = gem.fullName,
                                                color = GoldPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = gem.description,
                                                color = AccentGreen,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onUnsocketGem?.invoke(equipment, i) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.2f)),
                                        border = BorderStroke(0.5.dp, AccentRed),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("卸下", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceElevated.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💠", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("【空孔位】可镶嵌${equipment.type.allowedGemCategory.displayName}宝石", color = TextMuted, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { showGemPicker = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("镶嵌", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceHighlight.copy(alpha = 0.3f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "白色/绿色装备无孔位。蓝色品质装备开启 2 个孔位，每升一品质增加 1 颗孔位（最高神话 6 孔）",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
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
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
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
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f))
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
                            border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (equipment.isLocked) "已锁定 (无法出售/熔炼)" else "熔炼装备 (获得 💰${equipment.sellPrice} 金币 & 🔮${equipment.meltStonesGained} 强化石)",
                                fontSize = 11.sp,
                                color = if (equipment.isLocked) TextMuted else AccentRed
                            )
                        }
                    }
                }
            }
        }
    }

    // Gem Picker Pop-up Dialog
    if (showGemPicker && player != null) {
        val targetCategory = equipment.type.allowedGemCategory
        val availableGems = player.getAllGemsList().filter {
            it.count > 0 && it.type.category == targetCategory
        }

        Dialog(onDismissRequest = { showGemPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GoldPrimary)
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
                        Text(
                            text = "💎 选择镶嵌宝石 (${targetCategory.displayName})",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showGemPicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "关闭", tint = TextMuted)
                        }
                    }

                    Text(
                        text = "此装备为【${equipment.type.displayName}】，仅可镶嵌【${targetCategory.displayName}】宝石（${if (targetCategory == GemCategory.OFFENSIVE) "武器、戒指、项链" else "衣服、头盔、战靴"}）",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (availableGems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💠", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("宝石囊中没有可用的${targetCategory.displayName}宝石", color = TextSecondary, fontSize = 12.sp)
                                Text("击败关底领主或在万宝阁中可获得珍稀宝石！", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(availableGems) { item ->
                                val gem = Gem(item.type, item.level)
                                val count = item.count
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
                                        Text(gem.iconEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(gem.fullName, color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("拥有: $count 颗", color = TextSecondary, fontSize = 10.sp)
                                            }
                                            Text(gem.description, color = AccentGreen, fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onSocketGem?.invoke(equipment, gem.type, gem.level)
                                            showGemPicker = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("镶入", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonStatRow(
    label: String,
    selectedVal: Long,
    equippedVal: Long
) {
    val diff = selectedVal - equippedVal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "+$selectedVal",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = " (原:+$equippedVal)",
                color = TextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            if (diff != 0L) {
                Text(
                    text = if (diff > 0) "▲ +$diff" else "▼ $diff",
                    color = if (diff > 0) AccentGreen else AccentRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text("持平", color = TextMuted, fontSize = 10.sp)
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
