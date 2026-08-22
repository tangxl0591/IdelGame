package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.DamageType
import com.example.domain.model.DungeonCatalog
import com.example.domain.model.EquipmentQuality
import com.example.domain.model.LogType
import com.example.domain.model.Player
import com.example.domain.model.PlayerStats
import com.example.ui.components.formatNumber
import com.example.ui.theme.AccentBlue
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
import com.example.ui.viewmodel.BattleState

@Composable
fun BattleScreen(
    player: Player,
    stats: PlayerStats,
    battleState: BattleState,
    onSetBattleSpeed: (Double) -> Unit,
    onToggleAutoChallenge: () -> Unit,
    onSetAutoSellQuality: (EquipmentQuality?) -> Unit,
    onSetDungeonStage: (Int) -> Unit,
    onToggleEndlessMode: (Boolean) -> Unit,
    onSetEndlessFloor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAutoSellDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "combat_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp)
    ) {
        // Stage Info & Navigation Header
        val currentStage = if (battleState.isEndlessMode) player.endlessTowerFloor else player.currentDungeonStage
        val chapter = DungeonCatalog.getChapterForLevel(currentStage)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkSurfaceHighlight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chapter / Stage text
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (battleState.isEndlessMode) "🗼 镇妖古塔" else "${chapter.iconEmoji} ${chapter.name}",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldDark.copy(alpha = 0.3f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (battleState.isEndlessMode) "第 ${player.endlessTowerFloor} 层" else "第 ${player.currentDungeonStage} 关",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = if (battleState.isEndlessMode) "最高突破: 第${player.maxEndlessTowerFloor}层 / 2000" else "最高解锁: 第${player.maxDungeonStage}关 / 2000",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Stage Stepper Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (battleState.isEndlessMode) {
                                onSetEndlessFloor((player.endlessTowerFloor - 1).coerceAtLeast(1))
                            } else {
                                onSetDungeonStage((player.currentDungeonStage - 1).coerceAtLeast(1))
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        enabled = if (battleState.isEndlessMode) player.endlessTowerFloor > 1 else player.currentDungeonStage > 1
                    ) {
                        Text("◀", fontSize = 11.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = {
                            if (battleState.isEndlessMode) {
                                onSetEndlessFloor((player.endlessTowerFloor + 1).coerceAtMost(player.maxEndlessTowerFloor))
                            } else {
                                onSetDungeonStage((player.currentDungeonStage + 1).coerceAtMost(player.maxDungeonStage))
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        enabled = if (battleState.isEndlessMode) player.endlessTowerFloor < player.maxEndlessTowerFloor else player.currentDungeonStage < player.maxDungeonStage
                    ) {
                        Text("▶", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Wave Progress Indicator (Requirement #4)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = if (battleState.isBossWave) Color(0xFF3E1212) else DarkSurfaceElevated,
            border = BorderStroke(1.dp, if (battleState.isBossWave) AccentRed else DarkSurfaceHighlight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (battleState.isBossWave) "⚠️ 领主降临" else "⚔️ 推进波次",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (battleState.isBossWave) AccentRed else GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (battleState.isBossWave) "第 ${battleState.currentWave}/${battleState.totalWaves} 波 (关底领主·必爆装备与宝石!)"
                        else "第 ${battleState.currentWave}/${battleState.totalWaves} 波 (清剿小怪中)",
                        fontSize = 11.sp,
                        color = if (battleState.isBossWave) Color(0xFFFF8A80) else TextSecondary
                    )
                }

                // Wave Segment Dots
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (w in 1..battleState.totalWaves) {
                        val isCleared = w < battleState.currentWave
                        val isCurrent = w == battleState.currentWave
                        val isBoss = w == battleState.totalWaves
                        val dotColor = when {
                            isCurrent && isBoss -> AccentRed
                            isCurrent -> GoldPrimary
                            isCleared -> AccentGreen
                            else -> DarkSurfaceHighlight
                        }
                        Box(
                            modifier = Modifier
                                .size(if (isBoss) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Combat Arena Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GoldDark.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Battle Units (Player vs Monster)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player Card
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(1.5.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(player.characterClass.iconEmoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = player.name,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${player.characterClass.displayName} · 战力 ${formatNumber(stats.totalPower)}",
                            color = GoldPrimary,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        val playerHpProgress = (battleState.playerCurrentHp.toFloat() / stats.maxHp.coerceAtLeast(1L)).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { playerHpProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentGreen,
                            trackColor = DarkSurfaceHighlight
                        )
                        Text(
                            text = "${battleState.playerCurrentHp} / ${stats.maxHp}",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }

                    // VS Emblem + Floating Damage in middle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "⚔️",
                            fontSize = 22.sp,
                            modifier = Modifier.scale(pulseScale)
                        )
                        Text(
                            text = "VS",
                            color = AccentRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        // Floating Damage Render
                        battleState.floatingDamages.takeLast(2).forEach { dmg ->
                            val color = when (dmg.type) {
                                DamageType.CRIT -> GoldPrimary
                                DamageType.HEAL -> AccentGreen
                                DamageType.DODGE -> AccentCyan
                                DamageType.BLOCK -> AccentBlue
                                DamageType.NORMAL -> AccentRed
                            }
                            Text(
                                text = dmg.text,
                                color = color,
                                fontSize = if (dmg.type == DamageType.CRIT) 13.sp else 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Monster Card
                    val monster = battleState.currentMonster
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val monsterBorderColor = if (monster?.isBoss == true) AccentRed else AccentPurple
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(2.dp, monsterBorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(monster?.avatarEmoji ?: "👾", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = monster?.name ?: "搜寻魔物中...",
                            color = if (monster?.isBoss == true) AccentRed else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = if (monster?.isBoss == true) "👑 领主级首领" else "普通魔物",
                            color = if (monster?.isBoss == true) AccentRed else TextSecondary,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        val monsterMaxHp = monster?.maxHp?.coerceAtLeast(1L) ?: 100L
                        val monsterHpProgress = (battleState.monsterCurrentHp.toFloat() / monsterMaxHp).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { monsterHpProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (monster?.isBoss == true) AccentRed else AccentPurple,
                            trackColor = DarkSurfaceHighlight
                        )
                        Text(
                            text = "${battleState.monsterCurrentHp} / $monsterMaxHp",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DarkSurfaceHighlight)
                Spacer(modifier = Modifier.height(8.dp))

                // Combat Controls Row: Speed Multipliers, Auto Challenge, Auto Sell
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speed Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1.0, 2.0, 4.0, 8.0).forEach { speed ->
                            val isSelected = player.battleSpeed == speed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) GoldDark else DarkSurfaceElevated)
                                    .clickable { onSetBattleSpeed(speed) }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${speed.toInt()}X",
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Auto Sell Filter Shortcut
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { showAutoSellDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("自动熔炼: ", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = player.autoSellQuality?.displayName ?: "关闭",
                                color = player.autoSellQuality?.composeColor ?: GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Auto Challenge Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "自动推关",
                            color = if (player.autoChallenge) GoldPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = player.autoChallenge,
                            onCheckedChange = { onToggleAutoChallenge() },
                            modifier = Modifier.scale(0.7f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldDark
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Battle Log Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkSurfaceHighlight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📜 战斗历练日志", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("自动挂机历练中", color = AccentGreen, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = DarkSurfaceHighlight)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(battleState.battleLogs, key = { it.id }) { log ->
                        val logColor = when (log.type) {
                            LogType.VICTORY -> GoldPrimary
                            LogType.DEFEAT -> AccentRed
                            LogType.LOOT -> AccentCyan
                            LogType.LEVEL_UP -> GoldPrimary
                            LogType.PLAYER_ATTACK -> TextPrimary
                            LogType.MONSTER_ATTACK -> TextSecondary
                            LogType.INFO -> TextMuted
                        }
                        Text(
                            text = log.message,
                            color = logColor,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Auto Sell Quality Selection Dialog (7 tiers)
    if (showAutoSellDialog) {
        Dialog(onDismissRequest = { showAutoSellDialog = false }) {
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
                    Text("🔥 设置掉落自动熔炼", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("击败怪物掉落该品质及以下的装备时，将自动熔炼为金币与强化石", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    val options = listOf<Pair<String, EquipmentQuality?>>(
                        "不自动熔炼 (全部保留)" to null,
                        "熔炼【白色】及以下" to EquipmentQuality.WHITE,
                        "熔炼【绿色】及以下" to EquipmentQuality.GREEN,
                        "熔炼【蓝色】及以下" to EquipmentQuality.BLUE,
                        "熔炼【黄色】及以下" to EquipmentQuality.YELLOW,
                        "熔炼【红色】及以下" to EquipmentQuality.RED,
                        "熔炼【金色】及以下" to EquipmentQuality.GOLD
                    )

                    options.forEach { (label, quality) ->
                        val isSelected = player.autoSellQuality == quality
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldDark.copy(alpha = 0.3f) else DarkSurfaceElevated)
                                .clickable {
                                    onSetAutoSellQuality(quality)
                                    showAutoSellDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = quality?.composeColor ?: (if (isSelected) GoldPrimary else TextPrimary),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
