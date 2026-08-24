package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.repository.GameRepository
import com.example.domain.model.BattleLogEntry
import com.example.domain.model.CharacterClass
import com.example.domain.model.DamageType
import com.example.domain.model.Equipment
import com.example.domain.model.EquipmentGenerator
import com.example.domain.model.EquipmentQuality
import com.example.domain.model.FloatingDamage
import com.example.domain.model.LogType
import com.example.domain.model.Monster
import com.example.domain.model.MonsterGenerator
import com.example.domain.model.OfflineGainSummary
import com.example.domain.model.Player
import com.example.domain.model.PlayerStats
import com.example.domain.model.ShopGenerator
import com.example.domain.model.ShopItem
import com.example.domain.model.ShopItemType
import com.example.domain.model.TalentType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.random.Random

data class BattleState(
    val currentMonster: Monster? = null,
    val currentWave: Int = 1,
    val totalWaves: Int = 3,
    val isBossWave: Boolean = false,
    val playerCurrentHp: Long = 100L,
    val monsterCurrentHp: Long = 100L,
    val floatingDamages: List<FloatingDamage> = emptyList(),
    val battleLogs: List<BattleLogEntry> = emptyList(),
    val isFighting: Boolean = true,
    val isEndlessMode: Boolean = false,
    val isPlayerAttacking: Boolean = false,
    val isMonsterAttacking: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(AppDatabase.getInstance(application))

    val player: StateFlow<Player?> = repository.activePlayerFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val allPlayers: StateFlow<List<Player>> = repository.allPlayersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val equippedItems: StateFlow<List<Equipment>> = repository.equippedItemsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val inventoryItems: StateFlow<List<Equipment>> = repository.inventoryItemsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _battleState = MutableStateFlow(BattleState())
    val battleState: StateFlow<BattleState> = _battleState.asStateFlow()

    private val _offlineSummary = MutableStateFlow<OfflineGainSummary?>(null)
    val offlineSummary: StateFlow<OfflineGainSummary?> = _offlineSummary.asStateFlow()

    private val _shopItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val shopItems: StateFlow<List<ShopItem>> = _shopItems.asStateFlow()

    private val _shopRefreshCooldown = MutableStateFlow(0)
    val shopRefreshCooldown: StateFlow<Int> = _shopRefreshCooldown.asStateFlow()

    private val _selectedEquipment = MutableStateFlow<Equipment?>(null)
    val selectedEquipment: StateFlow<Equipment?> = _selectedEquipment.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var battleJob: Job? = null
    private var hpRegenJob: Job? = null
    private var cooldownJob: Job? = null

    init {
        viewModelScope.launch {
            val p = repository.initializeDefaultProfilesIfNeeded()
            val summary = repository.calculateAndApplyOfflineProgress(p)
            if (summary != null && summary.offlineSeconds >= 20) {
                _offlineSummary.value = summary
            }

            refreshShop(free = true)
            startBattleLoop()
            startHpRegenLoop()
            startCooldownTimer()
        }
    }

    fun dismissOfflineDialog() {
        _offlineSummary.value = null
    }

    fun selectEquipment(equipment: Equipment?) {
        _selectedEquipment.value = equipment
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Profile Management
    fun createCharacter(name: String, characterClass: CharacterClass) {
        viewModelScope.launch {
            val newPlayer = repository.createNewPlayer(name, characterClass, makeActive = true)
            _battleState.value = BattleState()
            refreshShop(free = true)
            showMessage("成功创建角色【${newPlayer.name}】(${characterClass.displayName})！")
        }
    }

    fun switchCharacter(playerId: String) {
        viewModelScope.launch {
            repository.switchActivePlayer(playerId)
            _battleState.value = BattleState()
            val active = repository.getActivePlayerDirect()
            if (active != null) {
                val summary = repository.calculateAndApplyOfflineProgress(active)
                if (summary != null && summary.offlineSeconds >= 20) {
                    _offlineSummary.value = summary
                }
                refreshShop(free = true)
                showMessage("已切换至角色【${active.name}】！")
            }
        }
    }

    fun deleteCharacter(playerId: String) {
        viewModelScope.launch {
            repository.deletePlayerProfile(playerId)
            _battleState.value = BattleState()
            showMessage("角色已删除")
        }
    }

    private fun startCooldownTimer() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_shopRefreshCooldown.value > 0) {
                    _shopRefreshCooldown.value -= 1
                }
            }
        }
    }

    private fun startHpRegenLoop() {
        hpRegenJob?.cancel()
        hpRegenJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val curPlayer = player.value ?: continue
                val stats = curPlayer.calculateStats(equippedItems.value)
                val curHp = _battleState.value.playerCurrentHp
                if (curHp < stats.maxHp && curHp > 0) {
                    val regenAmount = (stats.maxHp * (stats.hpRegenPercent / 100.0)).roundToLong().coerceAtLeast(1L)
                    val newHp = (curHp + regenAmount).coerceAtMost(stats.maxHp)
                    _battleState.value = _battleState.value.copy(playerCurrentHp = newHp)
                }
            }
        }
    }

    private fun calculateTotalWaves(stageLevel: Int, isEndless: Boolean): Int {
        if (isEndless) return 1 // Endless tower is 1 boss/floor per level
        return when {
            stageLevel <= 30 -> 3    // 2 mob waves + 1 boss wave (total 3)
            stageLevel <= 100 -> 4   // 3 mob waves + 1 boss wave
            stageLevel <= 300 -> 5   // 4 mob waves + 1 boss wave
            stageLevel <= 600 -> 6   // 5 mob waves + 1 boss wave
            stageLevel <= 1000 -> 7  // 6 mob waves + 1 boss wave
            stageLevel <= 1500 -> 8  // 7 mob waves + 1 boss wave
            else -> 9               // 8 mob waves + 1 boss wave
        }
    }

    private fun startBattleLoop() {
        battleJob?.cancel()
        battleJob = viewModelScope.launch {
            while (true) {
                val curPlayer = player.value
                if (curPlayer == null) {
                    delay(300)
                    continue
                }

                val stats = curPlayer.calculateStats(equippedItems.value)
                if (_battleState.value.currentMonster == null) {
                    spawnMonsterForCurrentWave(curPlayer)
                    _battleState.value = _battleState.value.copy(
                        playerCurrentHp = stats.maxHp,
                        monsterCurrentHp = _battleState.value.currentMonster?.maxHp ?: 100L
                    )
                }

                val speed = (curPlayer.battleSpeed * stats.attackSpeed).coerceIn(1.0, 10.0)
                val attackInterval = (750L / speed).toLong().coerceAtLeast(60L)

                // Player Turn
                performPlayerAttack(curPlayer, stats)
                delay(attackInterval / 2)

                // Check monster death
                if (_battleState.value.monsterCurrentHp <= 0) {
                    onMonsterDefeated(curPlayer, stats)
                    delay(attackInterval)
                    continue
                }

                // Monster Turn
                performMonsterAttack(curPlayer, stats)
                delay(attackInterval / 2)

                // Check player death
                if (_battleState.value.playerCurrentHp <= 0) {
                    onPlayerDefeated(curPlayer, stats)
                    delay(attackInterval * 2)
                    continue
                }

                // Clean old floating damages
                cleanFloatingDamages()
            }
        }
    }

    private fun spawnMonsterForCurrentWave(p: Player) {
        val isEndless = _battleState.value.isEndlessMode
        val stageLevel = if (isEndless) p.endlessTowerFloor else p.currentDungeonStage
        val totalWaves = calculateTotalWaves(stageLevel, isEndless)
        val curWave = _battleState.value.currentWave.coerceIn(1, totalWaves)
        val isBoss = (curWave == totalWaves)

        val monster = MonsterGenerator.createMonster(stageLevel, isBoss)
        _battleState.value = _battleState.value.copy(
            currentMonster = monster,
            currentWave = curWave,
            totalWaves = totalWaves,
            isBossWave = isBoss,
            monsterCurrentHp = monster.maxHp
        )
        val waveDesc = if (isBoss) "⚠️ 领主降临 (第 $curWave/$totalWaves 波)" else "遭遇魔物 (第 $curWave/$totalWaves 波)"
        addBattleLog("$waveDesc：${monster.name}！", if (isBoss) LogType.LEVEL_UP else LogType.INFO)
    }

    private fun performPlayerAttack(p: Player, stats: PlayerStats) {
        val monster = _battleState.value.currentMonster ?: return

        // Check dodge
        if (Random.nextDouble(100.0) < monster.dodgeRate) {
            addFloatingDamage("闪避!", DamageType.DODGE, isMonsterTarget = true)
            addBattleLog("${monster.name} 灵巧地闪避了你的攻击！", LogType.PLAYER_ATTACK)
            return
        }

        // Base damage with class scaling
        val rawDmg = Random.nextLong(stats.minAttack, stats.maxAttack + 1)
        val isCrit = Random.nextDouble(100.0) < stats.critRate
        var dmg = if (isCrit) (rawDmg * (stats.critDamage / 100.0)).roundToLong() else rawDmg

        // Monster defense
        val defReduction = 100.0 / (100.0 + monster.defense)
        dmg = (dmg * defReduction).roundToLong().coerceAtLeast(1L)

        // Monster block
        val isBlocked = Random.nextDouble(100.0) < monster.blockRate
        if (isBlocked) {
            dmg = (dmg * 0.6).roundToLong().coerceAtLeast(1L)
        }

        val newMonsterHp = max(0L, _battleState.value.monsterCurrentHp - dmg)
        _battleState.value = _battleState.value.copy(
            monsterCurrentHp = newMonsterHp,
            isPlayerAttacking = true
        )

        // Damage display & class flavor
        val dmgText = if (isCrit) "💥 $dmg (暴击!)" else if (isBlocked) "🛡️ $dmg (格挡)" else "-$dmg"
        addFloatingDamage(dmgText, if (isCrit) DamageType.CRIT else if (isBlocked) DamageType.BLOCK else DamageType.NORMAL, isMonsterTarget = true)

        // Life Steal
        if (stats.lifeSteal > 0 && dmg > 0) {
            val healAmount = (dmg * (stats.lifeSteal / 100.0)).roundToLong().coerceAtLeast(1L)
            val curHp = _battleState.value.playerCurrentHp
            val healedHp = (curHp + healAmount).coerceAtMost(stats.maxHp)
            _battleState.value = _battleState.value.copy(playerCurrentHp = healedHp)
            addFloatingDamage("+$healAmount", DamageType.HEAL, isMonsterTarget = false)
        }

        val attackVerb = when (p.characterClass) {
            CharacterClass.WARRIOR -> "挥舞巨刃斩击"
            CharacterClass.MAGE -> "吟唱禁咒法术轰击"
            CharacterClass.ARCHER -> "射出疾风贯日箭穿透"
        }
        addBattleLog("你${attackVerb} ${monster.name}，造成 $dmg 点伤害${if (isCrit) " [暴击!]" else ""}", LogType.PLAYER_ATTACK)
    }

    private fun performMonsterAttack(p: Player, stats: PlayerStats) {
        val monster = _battleState.value.currentMonster ?: return

        // Check player dodge
        if (Random.nextDouble(100.0) < stats.dodgeRate) {
            addFloatingDamage("💨 闪避!", DamageType.DODGE, isMonsterTarget = false)
            addBattleLog("你身形化作残影，敏捷地闪避了 ${monster.name} 的猛攻！", LogType.MONSTER_ATTACK)
            return
        }

        // Base monster damage
        val rawDmg = Random.nextLong(monster.minAttack, monster.maxAttack + 1)
        val isCrit = Random.nextDouble(100.0) < monster.critRate
        var dmg = if (isCrit) (rawDmg * (monster.critDamage / 100.0)).roundToLong() else rawDmg

        // Player defense
        val defReduction = 100.0 / (100.0 + stats.defense)
        dmg = (dmg * defReduction).roundToLong().coerceAtLeast(1L)

        // Player block
        val isBlocked = Random.nextDouble(100.0) < stats.blockRate
        if (isBlocked) {
            dmg = (dmg * 0.50).roundToLong().coerceAtLeast(1L)
        }

        val newPlayerHp = max(0L, _battleState.value.playerCurrentHp - dmg)
        _battleState.value = _battleState.value.copy(
            playerCurrentHp = newPlayerHp,
            isMonsterAttacking = true
        )

        val dmgText = if (isCrit) "💥 $dmg (重击!)" else if (isBlocked) "🧱 $dmg (格挡)" else "-$dmg"
        addFloatingDamage(dmgText, if (isCrit) DamageType.CRIT else if (isBlocked) DamageType.BLOCK else DamageType.NORMAL, isMonsterTarget = false)

        addBattleLog("${monster.name} 对你造成了 $dmg 点伤害！", LogType.MONSTER_ATTACK)
    }

    private suspend fun onMonsterDefeated(p: Player, stats: PlayerStats) {
        val monster = _battleState.value.currentMonster ?: return
        val isBoss = monster.isBoss
        val curWave = _battleState.value.currentWave
        val totalWaves = _battleState.value.totalWaves

        addBattleLog("⚔️ 成功击败 ${monster.name}！", LogType.VICTORY)

        val expGained = (monster.expReward * (1.0 + stats.expBonus / 100.0)).roundToLong()

        // Drop Rule (Requirement #5):
        // Normal mob: probability of gold (75%), exp (100%), small chance of stone (15%), small gear (8%)
        // Boss: guaranteed gold (100%), diamonds (80%), enhance stones (100%), high-quality equipment (100%)
        val goldRoll = Random.nextDouble(100.0)
        val shouldDropGold = isBoss || (goldRoll < 75.0)
        val goldGained = if (shouldDropGold) (monster.goldReward * (1.0 + stats.goldBonus / 100.0)).roundToLong() else 0L

        var newGold = p.gold + goldGained
        var newDiamonds = p.diamonds
        var newStones = p.enhanceStones

        if (goldGained > 0) {
            addBattleLog("💰 获得金币 +$goldGained", LogType.LOOT)
        }

        // Enhance Stones Drop Logic
        val stoneGained = if (isBoss) {
            Random.nextInt(3, 8)
        } else {
            if (Random.nextDouble(100.0) < 15.0) 1 else 0
        }
        if (stoneGained > 0) {
            newStones += stoneGained
            addBattleLog("🔮 击落强化石 x$stoneGained", LogType.LOOT)
        }

        // Diamond / Gem Drop on Boss (Requirement #5: Boss drops gold, equip, gems & rare resources)
        var updatedPlayerState = p
        val isEndless = _battleState.value.isEndlessMode
        val stageLevel = if (isEndless) p.endlessTowerFloor else p.currentDungeonStage

        if (isBoss) {
            val diamondsGained = Random.nextLong(6, 20)
            newDiamonds += diamondsGained
            addBattleLog("💎 领主掉落极品灵石 x$diamondsGained", LogType.LOOT)

            // Boss guaranteed 1~2 gems drop
            val gemCount = Random.nextInt(1, 3)
            for (i in 0 until gemCount) {
                val droppedGemType = com.example.domain.model.GemType.entries.random()
                val gemLvl = (1 + (stageLevel / 35)).coerceIn(1, 5)
                updatedPlayerState = updatedPlayerState.addGem(droppedGemType, gemLvl, 1)
                addBattleLog("💠 领主爆出稀世宝石：【${gemLvl}级${droppedGemType.gemName}】(${droppedGemType.displayName})！", LogType.LOOT)
            }
        } else {
            // Normal monster 12% chance to drop 1 Lv.1 gem
            if (Random.nextDouble(100.0) < 12.0) {
                val droppedGemType = com.example.domain.model.GemType.entries.random()
                updatedPlayerState = updatedPlayerState.addGem(droppedGemType, 1, 1)
                addBattleLog("💠 击落宝石碎片：【1级${droppedGemType.gemName}】！", LogType.LOOT)
            }
        }

        // Equipment Drop Logic
        val dropChance = if (isBoss) 100.0 else 8.0

        if (Random.nextDouble(100.0) < dropChance) {
            val forcedQ = if (isBoss) EquipmentGenerator.rollBossQuality() else null
            val droppedEq = EquipmentGenerator.generateEquipment(
                level = stageLevel,
                forcedQuality = forcedQ,
                characterClass = p.characterClass,
                playerId = p.id
            )
            val autoSellFilter = p.autoSellQuality

            if (autoSellFilter != null && droppedEq.quality.ordinal <= autoSellFilter.ordinal) {
                newGold += droppedEq.sellPrice
                newStones += droppedEq.meltStonesGained
                addBattleLog("🔥 自动熔炼了【${droppedEq.quality.displayName}·${droppedEq.name}】，获得 ${droppedEq.sellPrice} 金币 & ${droppedEq.meltStonesGained} 强化石", LogType.LOOT)
            } else {
                val currentInventory = repository.getInventoryItemsDirect(p.id)
                if (currentInventory.size < 40) {
                    repository.insertEquipment(droppedEq, p.id)
                    addBattleLog("🎁 爆出装备：【${droppedEq.quality.displayName}·${droppedEq.name}】(评分: ${droppedEq.powerScore})！", LogType.LOOT)
                } else {
                    newGold += droppedEq.sellPrice
                    newStones += droppedEq.meltStonesGained
                    addBattleLog("⚠️ 背包已满，【${droppedEq.name}】已自动熔炼为 ${droppedEq.sellPrice} 金币 & ${droppedEq.meltStonesGained} 强化石", LogType.LOOT)
                }
            }
        }

        // Level Up check
        var newLevel = p.level
        var newExp = p.currentExp + expGained
        while (true) {
            val maxExp = (newLevel.toDouble().let { it * it * 60.0 }).roundToLong() + 50L
            if (newExp >= maxExp) {
                newExp -= maxExp
                newLevel++
                addBattleLog("🎉 境界突破！角色等级提升至 Lv.$newLevel ！", LogType.LEVEL_UP)
            } else {
                break
            }
        }

        // Wave & Stage Progression (Requirement #4)
        var nextStage = p.currentDungeonStage
        var maxStage = p.maxDungeonStage
        var nextTower = p.endlessTowerFloor
        var maxTower = p.maxEndlessTowerFloor
        var nextWave = curWave

        if (isBoss) {
            // Defeated boss -> Advance to next map level!
            if (p.autoChallenge) {
                if (isEndless) {
                    nextTower = (p.endlessTowerFloor + 1).coerceAtMost(2000)
                    maxTower = max(maxTower, nextTower)
                } else {
                    nextStage = (p.currentDungeonStage + 1).coerceAtMost(2000)
                    maxStage = max(maxStage, nextStage)
                }
            }
            nextWave = 1 // Reset to wave 1 on next stage
            addBattleLog("🏆 恭喜通关第 $stageLevel 关！前往下一关历练！", LogType.LEVEL_UP)
        } else {
            // Advance wave
            nextWave = curWave + 1
        }

        val updatedPlayer = updatedPlayerState.copy(
            level = newLevel,
            currentExp = newExp,
            gold = newGold,
            diamonds = newDiamonds,
            enhanceStones = newStones,
            currentDungeonStage = nextStage,
            maxDungeonStage = maxStage,
            endlessTowerFloor = nextTower,
            maxEndlessTowerFloor = maxTower
        )
        repository.savePlayer(updatedPlayer)

        // Spawn next wave or next stage
        val newTotalWaves = calculateTotalWaves(if (isEndless) nextTower else nextStage, isEndless)
        val newIsBoss = (nextWave == newTotalWaves)
        val nextMonster = MonsterGenerator.createMonster(if (isEndless) nextTower else nextStage, newIsBoss)

        _battleState.value = _battleState.value.copy(
            currentMonster = nextMonster,
            currentWave = nextWave,
            totalWaves = newTotalWaves,
            isBossWave = newIsBoss,
            monsterCurrentHp = nextMonster.maxHp,
            playerCurrentHp = stats.maxHp
        )
        val waveDesc = if (newIsBoss) "⚠️ 领主降临 (第 $nextWave/$newTotalWaves 波)" else "遭遇魔物 (第 $nextWave/$newTotalWaves 波)"
        addBattleLog("$waveDesc：${nextMonster.name}！", if (newIsBoss) LogType.LEVEL_UP else LogType.INFO)
    }

    private fun onPlayerDefeated(p: Player, stats: PlayerStats) {
        val monster = _battleState.value.currentMonster
        addBattleLog("💀 战斗失利！你不敌 ${monster?.name}，打坐调息中...", LogType.DEFEAT)

        // Reset wave to 1 on defeat to let player retry from mob wave
        val isEndless = _battleState.value.isEndlessMode
        val stageLevel = if (isEndless) p.endlessTowerFloor else p.currentDungeonStage
        val totalWaves = calculateTotalWaves(stageLevel, isEndless)
        val resetMonster = MonsterGenerator.createMonster(stageLevel, totalWaves == 1)

        _battleState.value = _battleState.value.copy(
            currentMonster = resetMonster,
            currentWave = 1,
            totalWaves = totalWaves,
            isBossWave = (totalWaves == 1),
            playerCurrentHp = stats.maxHp,
            monsterCurrentHp = resetMonster.maxHp
        )
    }

    // Equipment Actions
    fun equip(equipment: Equipment) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            repository.equipItem(equipment, curPlayer.id)
            selectEquipment(null)
            showMessage("已穿戴【${equipment.name}】")
        }
    }

    fun unequip(equipment: Equipment) {
        viewModelScope.launch {
            repository.unequipItem(equipment)
            selectEquipment(null)
            showMessage("已卸下【${equipment.name}】")
        }
    }

    fun enhance(equipment: Equipment) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.enhanceEquipment(equipment, curPlayer)
            showMessage(msg)
            if (success) {
                val updated = repository.getEquippedItemsDirect(curPlayer.id).find { it.id == equipment.id }
                    ?: repository.getInventoryItemsDirect(curPlayer.id).find { it.id == equipment.id }
                _selectedEquipment.value = updated
            }
        }
    }

    fun reforge(equipment: Equipment) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.reforgeAffixes(equipment, curPlayer)
            showMessage(msg)
            if (success) {
                val updated = repository.getEquippedItemsDirect(curPlayer.id).find { it.id == equipment.id }
                    ?: repository.getInventoryItemsDirect(curPlayer.id).find { it.id == equipment.id }
                _selectedEquipment.value = updated
            }
        }
    }

    fun toggleLock(equipment: Equipment) {
        viewModelScope.launch {
            repository.toggleLockEquipment(equipment)
            val updated = equipment.copy(isLocked = !equipment.isLocked)
            _selectedEquipment.value = updated
        }
    }

    fun sell(equipment: Equipment) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (price, stones) = repository.sellEquipment(equipment, curPlayer)
            selectEquipment(null)
            showMessage("熔炼获得 $price 金币 & $stones 强化石")
        }
    }

    fun batchSell(quality: EquipmentQuality?) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (count, gold, stones) = repository.batchSellInventory(quality, curPlayer)
            if (count > 0) {
                showMessage("一键熔炼 $count 件装备，获得 $gold 金币 & $stones 强化石！")
            } else {
                showMessage("没有符合熔炼条件的未锁定装备")
            }
        }
    }

    fun setBattleSpeed(speed: Double) {
        val curPlayer = player.value ?: return
        val updated = curPlayer.copy(battleSpeed = speed)
        viewModelScope.launch {
            repository.savePlayer(updated)
        }
    }

    fun toggleAutoChallenge() {
        val curPlayer = player.value ?: return
        val updated = curPlayer.copy(autoChallenge = !curPlayer.autoChallenge)
        viewModelScope.launch {
            repository.savePlayer(updated)
            showMessage(if (updated.autoChallenge) "已开启自动推关" else "已锁定当前关卡挂机")
        }
    }

    fun setAutoSellQuality(quality: EquipmentQuality?) {
        val curPlayer = player.value ?: return
        val updated = curPlayer.copy(autoSellQuality = quality)
        viewModelScope.launch {
            repository.savePlayer(updated)
            showMessage(if (quality != null) "已设置自动熔炼【${quality.displayName}】及以下装备" else "已关闭自动熔炼")
        }
    }

    fun setDungeonStage(stage: Int) {
        val curPlayer = player.value ?: return
        val updated = curPlayer.copy(currentDungeonStage = stage.coerceIn(1, 2000))
        viewModelScope.launch {
            repository.savePlayer(updated)
            _battleState.value = _battleState.value.copy(isEndlessMode = false, currentWave = 1)
            spawnMonsterForCurrentWave(updated)
        }
    }

    fun toggleEndlessMode(isEndless: Boolean) {
        _battleState.value = _battleState.value.copy(isEndlessMode = isEndless, currentWave = 1)
        player.value?.let { p ->
            spawnMonsterForCurrentWave(p)
        }
    }

    fun setEndlessFloor(floor: Int) {
        val curPlayer = player.value ?: return
        val updated = curPlayer.copy(endlessTowerFloor = floor.coerceIn(1, 2000))
        viewModelScope.launch {
            repository.savePlayer(updated)
            _battleState.value = _battleState.value.copy(isEndlessMode = true, currentWave = 1)
            spawnMonsterForCurrentWave(updated)
        }
    }

    // Shop Actions
    fun refreshShop(free: Boolean = false) {
        val curPlayer = player.value ?: return
        if (!free) {
            val cost = 500L + (curPlayer.level * 50L)
            if (curPlayer.gold < cost) {
                showMessage("金币不足！刷新万宝阁需要 $cost 金币")
                return
            }
            viewModelScope.launch {
                val updated = curPlayer.copy(gold = curPlayer.gold - cost)
                repository.savePlayer(updated)
            }
        }
        _shopItems.value = ShopGenerator.generateDailyShop(
            playerLevel = curPlayer.level,
            characterClass = curPlayer.characterClass,
            playerId = curPlayer.id
        )
        _shopRefreshCooldown.value = 60
    }

    fun buyShopItem(item: ShopItem) {
        val curPlayer = player.value ?: return
        if (item.isPurchased) {
            showMessage("该宝物已被购买")
            return
        }

        if (curPlayer.gold < item.goldCost) {
            showMessage("金币不足！")
            return
        }

        viewModelScope.launch {
            var newGold = curPlayer.gold - item.goldCost
            var newDiamonds = curPlayer.diamonds
            var newStones = curPlayer.enhanceStones

            when (item.type) {
                ShopItemType.EQUIPMENT -> {
                    item.equipment?.let { eq ->
                        val inventory = repository.getInventoryItemsDirect(curPlayer.id)
                        if (inventory.size >= 40) {
                            showMessage("背包已满，无法容纳新装备！")
                            return@launch
                        }
                        repository.insertEquipment(eq, curPlayer.id)
                    }
                }
                ShopItemType.ENHANCE_STONE_PACK -> {
                    newStones += item.quantity
                }
                ShopItemType.SPIRIT_CHEST -> {
                    newDiamonds += item.quantity
                }
                ShopItemType.REFORGE_STONE -> {
                    newDiamonds += item.quantity
                }
                ShopItemType.GEM, ShopItemType.GEM_POUCH -> {
                    item.gemType?.let { gt ->
                        val updatedWithGem = curPlayer.copy(
                            gold = newGold,
                            diamonds = newDiamonds,
                            enhanceStones = newStones
                        ).addGem(gt, item.gemLevel, item.quantity)
                        repository.savePlayer(updatedWithGem)
                        _shopItems.value = _shopItems.value.map {
                            if (it.id == item.id) it.copy(isPurchased = true) else it
                        }
                        showMessage("购买成功！获得了【${item.name}】并存入宝石囊")
                        return@launch
                    }
                }
            }

            val updatedPlayer = curPlayer.copy(
                gold = newGold,
                diamonds = newDiamonds,
                enhanceStones = newStones
            )
            repository.savePlayer(updatedPlayer)

            _shopItems.value = _shopItems.value.map {
                if (it.id == item.id) it.copy(isPurchased = true) else it
            }
            showMessage("购买成功！获得了【${item.name}】")
        }
    }

    // Gem Operations (Requirements #3, #4, #5)
    fun socketGem(item: Equipment, gemType: com.example.domain.model.GemType, level: Int) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.socketGem(item, gemType, level, curPlayer)
            showMessage(msg)
            if (success) {
                // Refresh selected equipment dialog
                val updatedEq = repository.getEquipmentByIdDirect(item.id)
                _selectedEquipment.value = updatedEq
            }
        }
    }

    fun unsocketGem(item: Equipment, gemIndex: Int) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.unsocketGem(item, gemIndex, curPlayer)
            showMessage(msg)
            if (success) {
                // Refresh selected equipment dialog
                val updatedEq = repository.getEquipmentByIdDirect(item.id)
                _selectedEquipment.value = updatedEq
            }
        }
    }

    fun synthesizeGem(gemType: com.example.domain.model.GemType, level: Int) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.synthesizeGem(gemType, level, curPlayer)
            showMessage(msg)
        }
    }

    fun synthesizeAllGems() {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (count, msg) = repository.synthesizeAllGems(curPlayer)
            showMessage(msg)
        }
    }

    // Reincarnation & Talents
    fun reincarnate() {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.reincarnate(curPlayer)
            showMessage(msg)
            if (success) {
                _battleState.value = _battleState.value.copy(currentWave = 1)
            }
        }
    }

    fun upgradeTalent(type: TalentType) {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.upgradeTalent(type, curPlayer)
            showMessage(msg)
        }
    }

    fun resetTalents() {
        val curPlayer = player.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.resetTalents(curPlayer)
            showMessage(msg)
        }
    }

    // Profile Management (Requirements #7 & #8)
    fun createProfile(name: String, characterClass: CharacterClass) {
        viewModelScope.launch {
            val newPlayer = repository.createNewPlayer(name, characterClass, makeActive = true)
            showMessage("成功创建角色【${newPlayer.name}】(${characterClass.displayName})！")
            _battleState.value = BattleState(currentWave = 1, totalWaves = calculateTotalWaves(1, false))
            refreshShop(free = true)
            startBattleLoop()
        }
    }

    fun switchProfile(playerId: String) {
        viewModelScope.launch {
            repository.switchActivePlayer(playerId)
            showMessage("已切换至当前角色")
            _battleState.value = BattleState(currentWave = 1, totalWaves = calculateTotalWaves(player.value?.currentDungeonStage ?: 1, false))
            val active = repository.getActivePlayerDirect()
            if (active != null) {
                val summary = repository.calculateAndApplyOfflineProgress(active)
                if (summary != null && summary.offlineSeconds >= 20) {
                    _offlineSummary.value = summary
                }
            }
            refreshShop(free = true)
            startBattleLoop()
        }
    }

    fun deleteProfile(playerId: String) {
        viewModelScope.launch {
            repository.deletePlayerProfile(playerId)
            showMessage("角色档案已删除")
            startBattleLoop()
        }
    }

    private fun addFloatingDamage(text: String, type: DamageType, isMonsterTarget: Boolean) {
        val item = FloatingDamage(
            text = text,
            type = type,
            isMonsterTarget = isMonsterTarget
        )
        val current = _battleState.value.floatingDamages
        _battleState.value = _battleState.value.copy(
            floatingDamages = (current + item).takeLast(6)
        )
    }

    private fun cleanFloatingDamages() {
        val now = System.currentTimeMillis()
        val filtered = _battleState.value.floatingDamages.filter { now - it.timestamp < 1200 }
        _battleState.value = _battleState.value.copy(floatingDamages = filtered)
    }

    private fun addBattleLog(message: String, type: LogType) {
        val entry = BattleLogEntry(message = message, type = type)
        val current = _battleState.value.battleLogs
        _battleState.value = _battleState.value.copy(
            battleLogs = (listOf(entry) + current).take(50)
        )
    }
}
