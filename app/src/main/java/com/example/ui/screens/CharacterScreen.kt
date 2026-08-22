package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CharacterClass
import com.example.domain.model.Equipment
import com.example.domain.model.EquipmentType
import com.example.domain.model.Player
import com.example.domain.model.PlayerStats
import com.example.ui.components.formatNumber
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
fun CharacterScreen(
    player: Player,
    stats: PlayerStats,
    equippedItems: List<Equipment>,
    onSelectEquipment: (Equipment) -> Unit,
    onOpenProfileManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Character Class & Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GoldDark.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(GoldDark.copy(alpha = 0.3f))
                                .border(1.5.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(player.characterClass.iconEmoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.name,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (player.characterClass) {
                                        CharacterClass.WARRIOR -> Color(0xFFC62828).copy(alpha = 0.4f)
                                        CharacterClass.MAGE -> Color(0xFF6A1B9A).copy(alpha = 0.4f)
                                        CharacterClass.ARCHER -> Color(0xFF2E7D32).copy(alpha = 0.4f)
                                    },
                                    border = BorderStroke(0.5.dp, GoldPrimary)
                                ) {
                                    Text(
                                        text = "${player.characterClass.displayName} · ${player.characterClass.title}",
                                        color = GoldPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "战斗体系: ${player.characterClass.combatStyle}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onOpenProfileManager,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("切换角色", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Total Combat Power Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("综合总战力", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = formatNumber(stats.totalPower),
                        color = GoldPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldDark.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (player.reincarnationCount > 0) "${player.reincarnationCount}转 · Lv.${player.level}" else "Lv.${player.level}",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Equipment Grid (6 Slots)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GoldDark.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚔️ 当前已穿戴装备", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("点击槽位查看属性与强化洗练", color = TextSecondary, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                val weapon = equippedItems.find { it.type == EquipmentType.WEAPON }
                val armor = equippedItems.find { it.type == EquipmentType.ARMOR }
                val helmet = equippedItems.find { it.type == EquipmentType.HELMET }
                val shoes = equippedItems.find { it.type == EquipmentType.SHOES }
                val ring = equippedItems.find { it.type == EquipmentType.RING }
                val necklace = equippedItems.find { it.type == EquipmentType.NECKLACE }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GearSlot(slotName = "武器", icon = "⚔️", equipment = weapon, onClick = { weapon?.let(onSelectEquipment) })
                    GearSlot(slotName = "头盔", icon = "🪖", equipment = helmet, onClick = { helmet?.let(onSelectEquipment) })
                    GearSlot(slotName = "项链", icon = "📿", equipment = necklace, onClick = { necklace?.let(onSelectEquipment) })
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GearSlot(slotName = "战甲", icon = "🛡️", equipment = armor, onClick = { armor?.let(onSelectEquipment) })
                    GearSlot(slotName = "战靴", icon = "👢", equipment = shoes, onClick = { shoes?.let(onSelectEquipment) })
                    GearSlot(slotName = "戒指", icon = "💍", equipment = ring, onClick = { ring?.let(onSelectEquipment) })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Character Attribute List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, DarkSurfaceHighlight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📊 基础属性与战斗词条", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = DarkSurfaceHighlight)

                AttributeRow("⚔️ 攻击区间", "${stats.minAttack} ~ ${stats.maxAttack}", GoldPrimary)
                AttributeRow("❤️ 生命上限", "${stats.maxHp}", AccentGreen)
                AttributeRow("🛡️ 防御抗性", "${stats.defense}", AccentCyan)
                AttributeRow("💥 暴击概率", "${String.format("%.1f", stats.critRate)}%", GoldPrimary)
                AttributeRow("⚡ 暴击倍率", "${String.format("%.1f", stats.critDamage)}%", GoldPrimary)
                AttributeRow("🌿 每秒生命回复", "${String.format("%.1f", stats.hpRegenPercent)}% / 秒", AccentGreen)
                AttributeRow("🧱 格挡概率", "${String.format("%.1f", stats.blockRate)}%", AccentCyan)
                AttributeRow("💨 闪避概率", "${String.format("%.1f", stats.dodgeRate)}%", AccentPurple)
                AttributeRow("🩸 生命吸取", "${String.format("%.1f", stats.lifeSteal)}%", AccentRed)
                AttributeRow("✨ 历练经验加成", "+${String.format("%.1f", stats.expBonus)}%", GoldPrimary)
                AttributeRow("💰 历练金币加成", "+${String.format("%.1f", stats.goldBonus)}%", GoldPrimary)
            }
        }
    }
}

@Composable
fun GearSlot(
    slotName: String,
    icon: String,
    equipment: Equipment?,
    onClick: () -> Unit
) {
    val qualityColor = equipment?.quality?.composeColor ?: DarkSurfaceHighlight

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = equipment != null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.5.dp, qualityColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (equipment != null) {
                Text(icon, fontSize = 26.sp)
                if (equipment.enhanceLevel > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(bottomStart = 6.dp))
                            .background(GoldPrimary)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "+${equipment.enhanceLevel}",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else {
                Text(icon, fontSize = 22.sp, color = TextMuted)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = equipment?.name ?: slotName,
            color = if (equipment != null) qualityColor else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (equipment != null) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun AttributeRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
