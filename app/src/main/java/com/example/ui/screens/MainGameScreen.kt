package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CurrencyHeader
import com.example.ui.components.EquipmentDetailDialog
import com.example.ui.components.OfflineGainDialog
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GameViewModel

sealed class GameTab(val title: String, val icon: String) {
    object Battle : GameTab("历练", "⚔️")
    object Character : GameTab("角色", "🧙‍♂️")
    object Inventory : GameTab("背包", "🎒")
    object Dungeon : GameTab("地图", "🗺️")
    object Shop : GameTab("万宝阁", "🏮")
    object Reincarnation : GameTab("转生", "🌌")
}

@Composable
fun MainGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val player by viewModel.player.collectAsStateWithLifecycle()
    val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle()
    val equippedItems by viewModel.equippedItems.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val battleState by viewModel.battleState.collectAsStateWithLifecycle()
    val offlineSummary by viewModel.offlineSummary.collectAsStateWithLifecycle()
    val shopItems by viewModel.shopItems.collectAsStateWithLifecycle()
    val shopCooldown by viewModel.shopRefreshCooldown.collectAsStateWithLifecycle()
    val selectedEquipment by viewModel.selectedEquipment.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableIntStateOf(0) }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val currentPlayer = player

    if (currentPlayer == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        )
        return
    }

    val stats = remember(currentPlayer, equippedItems) {
        currentPlayer.calculateStats(equippedItems)
    }

    val tabs = listOf(
        GameTab.Battle,
        GameTab.Character,
        GameTab.Inventory,
        GameTab.Dungeon,
        GameTab.Shop,
        GameTab.Reincarnation
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CurrencyHeader(
                player = currentPlayer,
                currentHp = battleState.playerCurrentHp,
                maxHp = stats.maxHp,
                onProfileClick = { showProfileDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Text(tab.icon, fontSize = 18.sp)
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldDark.copy(alpha = 0.25f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> BattleScreen(
                    player = currentPlayer,
                    stats = stats,
                    battleState = battleState,
                    onSetBattleSpeed = viewModel::setBattleSpeed,
                    onToggleAutoChallenge = viewModel::toggleAutoChallenge,
                    onSetAutoSellQuality = viewModel::setAutoSellQuality,
                    onSetDungeonStage = viewModel::setDungeonStage,
                    onToggleEndlessMode = viewModel::toggleEndlessMode,
                    onSetEndlessFloor = viewModel::setEndlessFloor
                )
                1 -> CharacterScreen(
                    player = currentPlayer,
                    stats = stats,
                    equippedItems = equippedItems,
                    onSelectEquipment = viewModel::selectEquipment,
                    onOpenProfileManager = { showProfileDialog = true },
                    onSynthesizeGem = viewModel::synthesizeGem,
                    onSynthesizeAllGems = viewModel::synthesizeAllGems
                )
                2 -> InventoryScreen(
                    player = currentPlayer,
                    inventoryItems = inventoryItems,
                    equippedItems = equippedItems,
                    onSelectEquipment = viewModel::selectEquipment,
                    onBatchSell = viewModel::batchSell,
                    onSynthesizeGem = viewModel::synthesizeGem,
                    onSynthesizeAllGems = viewModel::synthesizeAllGems
                )
                3 -> DungeonScreen(
                    player = currentPlayer,
                    onSelectStage = {
                        viewModel.setDungeonStage(it)
                        currentTab = 0 // Return to battle screen
                    },
                    onToggleEndlessMode = {
                        viewModel.toggleEndlessMode(it)
                        currentTab = 0
                    },
                    onSetEndlessFloor = {
                        viewModel.setEndlessFloor(it)
                        currentTab = 0
                    }
                )
                4 -> ShopScreen(
                    player = currentPlayer,
                    shopItems = shopItems,
                    refreshCooldown = shopCooldown,
                    onRefreshShop = viewModel::refreshShop,
                    onBuyItem = viewModel::buyShopItem,
                    onSelectEquipment = viewModel::selectEquipment
                )
                5 -> TalentReincarnationScreen(
                    player = currentPlayer,
                    onReincarnate = viewModel::reincarnate,
                    onUpgradeTalent = viewModel::upgradeTalent,
                    onResetTalents = viewModel::resetTalents
                )
            }
        }
    }

    // User Profile Switch / Create Dialog (Requirement #7 & #8)
    if (showProfileDialog) {
        UserProfileDialog(
            currentPlayer = currentPlayer,
            allPlayers = allPlayers,
            onDismiss = { showProfileDialog = false },
            onSwitchPlayer = { playerId ->
                viewModel.switchProfile(playerId)
            },
            onCreatePlayer = { name, characterClass ->
                viewModel.createProfile(name, characterClass)
            },
            onDeletePlayer = { playerId ->
                viewModel.deleteProfile(playerId)
            }
        )
    }

    // Equipment Detail Dialog
    selectedEquipment?.let { eq ->
        val equippedComparison = if (!eq.isEquipped) equippedItems.find { it.type == eq.type } else null
        EquipmentDetailDialog(
            equipment = eq,
            equippedComparison = equippedComparison,
            player = currentPlayer,
            onDismiss = { viewModel.selectEquipment(null) },
            onEquip = viewModel::equip,
            onUnequip = viewModel::unequip,
            onEnhance = viewModel::enhance,
            onReforge = viewModel::reforge,
            onToggleLock = viewModel::toggleLock,
            onSell = viewModel::sell,
            onSocketGem = viewModel::socketGem,
            onUnsocketGem = viewModel::unsocketGem
        )
    }

    // Offline Gains Dialog
    offlineSummary?.let { summary ->
        OfflineGainDialog(
            summary = summary,
            onClaim = viewModel::dismissOfflineDialog
        )
    }
}
