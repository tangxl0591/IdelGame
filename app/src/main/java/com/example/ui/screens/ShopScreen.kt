package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Equipment
import com.example.domain.model.Player
import com.example.domain.model.ShopItem
import com.example.domain.model.ShopItemType
import com.example.ui.components.formatNumber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
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
fun ShopScreen(
    player: Player,
    shopItems: List<ShopItem>,
    refreshCooldown: Int,
    onRefreshShop: (free: Boolean) -> Unit,
    onBuyItem: (ShopItem) -> Unit,
    onSelectEquipment: (Equipment) -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshCost = 500L + (player.level * 50L)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp)
    ) {
        // Shop Header & Refresh Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🏮 仙家万宝阁", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (refreshCooldown > 0) "免费刷新倒计时: ${refreshCooldown}s" else "可以免费刷新！",
                        color = if (refreshCooldown > 0) TextSecondary else AccentGreen,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { onRefreshShop(refreshCooldown <= 0) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (refreshCooldown <= 0) AccentGreen else GoldDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (refreshCooldown <= 0) "免费刷新" else "刷新 (💰$refreshCost)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Shop Items List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shopItems, key = { it.id }) { item ->
                val qualityColor = item.equipment?.quality?.composeColor ?: GoldPrimary

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isPurchased) DarkSurfaceElevated.copy(alpha = 0.5f) else DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isPurchased) DarkSurfaceHighlight else qualityColor.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Icon + Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, qualityColor, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.iconEmoji, fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = if (item.isPurchased) TextMuted else qualityColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                if (item.equipment != null && !item.isPurchased) {
                                    Text(
                                        text = "点击查看属性详情",
                                        color = AccentCyan,
                                        fontSize = 10.sp,
                                        modifier = Modifier.clickable { onSelectEquipment(item.equipment) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right: Price & Buy Button
                        if (item.isPurchased) {
                            Text("已售罄", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            val canAfford = player.gold >= item.goldCost
                            Button(
                                onClick = { onBuyItem(item) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) GoldDark else DarkSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("购买", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("💰${formatNumber(item.goldCost)}", fontSize = 10.sp, color = GoldPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
