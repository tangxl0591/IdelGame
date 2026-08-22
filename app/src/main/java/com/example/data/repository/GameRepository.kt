package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.Converters
import com.example.domain.model.Affix
import com.example.domain.model.AffixType
import com.example.domain.model.CharacterClass
import com.example.domain.model.Equipment
import com.example.domain.model.EquipmentGenerator
import com.example.domain.model.EquipmentQuality
import com.example.domain.model.EquipmentType
import com.example.domain.model.MonsterGenerator
import com.example.domain.model.OfflineGainSummary
import com.example.domain.model.Player
import com.example.domain.model.TalentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepository(private val database: AppDatabase) {
    private val playerDao = database.playerDao()
    private val equipmentDao = database.equipmentDao()

    val activePlayerFlow: Flow<Player?> = playerDao.getActivePlayerFlow().map { entity ->
        entity?.let { Converters.toDomain(it) }
    }

    val allPlayersFlow: Flow<List<Player>> = playerDao.getAllPlayersFlow().map { list ->
        list.map { Converters.toDomain(it) }
    }

    val equippedItemsFlow: Flow<List<Equipment>> = activePlayerFlow.flatMapLatest { player ->
        if (player == null) flowOf(emptyList())
        else equipmentDao.getEquippedItemsFlow(player.id).map { list ->
            list.map { Converters.toDomain(it) }
        }
    }

    val inventoryItemsFlow: Flow<List<Equipment>> = activePlayerFlow.flatMapLatest { player ->
        if (player == null) flowOf(emptyList())
        else equipmentDao.getInventoryItemsFlow(player.id).map { list ->
            list.map { Converters.toDomain(it) }
        }
    }

    suspend fun getActivePlayerDirect(): Player? = withContext(Dispatchers.IO) {
        playerDao.getActivePlayerDirect()?.let { Converters.toDomain(it) }
    }

    suspend fun getAllPlayersDirect(): List<Player> = withContext(Dispatchers.IO) {
        playerDao.getAllPlayersDirect().map { Converters.toDomain(it) }
    }

    suspend fun getEquippedItemsDirect(playerId: String): List<Equipment> = withContext(Dispatchers.IO) {
        equipmentDao.getEquippedItemsDirect(playerId).map { Converters.toDomain(it) }
    }

    suspend fun getInventoryItemsDirect(playerId: String): List<Equipment> = withContext(Dispatchers.IO) {
        equipmentDao.getInventoryItemsDirect(playerId).map { Converters.toDomain(it) }
    }

    suspend fun initializeDefaultProfilesIfNeeded(): Player = withContext(Dispatchers.IO) {
        val existingActive = playerDao.getActivePlayerDirect()
        if (existingActive != null) {
            return@withContext Converters.toDomain(existingActive)
        }

        val all = playerDao.getAllPlayersDirect()
        if (all.isNotEmpty()) {
            playerDao.setActivePlayer(all.first().id)
            return@withContext Converters.toDomain(all.first())
        }

        // Create 3 diverse starter profiles for user to pick from right away
        val warriorProfile = createNewPlayer("凌云剑圣", CharacterClass.WARRIOR, makeActive = true)
        createNewPlayer("星月魔灵", CharacterClass.MAGE, makeActive = false)
        createNewPlayer("追风神影", CharacterClass.ARCHER, makeActive = false)

        return@withContext warriorProfile
    }

    suspend fun createNewPlayer(
        name: String,
        characterClass: CharacterClass,
        makeActive: Boolean = true
    ): Player = withContext(Dispatchers.IO) {
        val playerId = UUID.randomUUID().toString()
        val starterGems = mapOf(
            "RUBY_ATTACK_1" to 3,
            "SAPPHIRE_MAGIC_1" to 2,
            "AMETHYST_CRIT_1" to 1,
            "ONYX_DEFENSE_1" to 3,
            "EMERALD_HP_1" to 3,
            "TOPAZ_DODGE_1" to 1
        )
        val newPlayer = Player(
            id = playerId,
            name = name.ifBlank { characterClass.title },
            characterClass = characterClass,
            level = 1,
            gold = 2000L,
            diamonds = 150L,
            enhanceStones = 25,
            currentDungeonStage = 1,
            maxDungeonStage = 1,
            gemInventory = starterGems,
            lastActiveTimestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            isActive = makeActive
        )

        playerDao.insertOrUpdate(Converters.toEntity(newPlayer))
        if (makeActive) {
            playerDao.setActivePlayer(playerId)
        }

        // Generate class-specific starter gear
        val weaponName = when (characterClass) {
            CharacterClass.WARRIOR -> "玄铁长剑"
            CharacterClass.MAGE -> "青木法杖"
            CharacterClass.ARCHER -> "疾风短弓"
        }

        val starterGear = listOf(
            Equipment(
                playerId = playerId,
                name = weaponName,
                type = EquipmentType.WEAPON,
                quality = EquipmentQuality.BLUE, // Blue quality with 2 sockets
                level = 1,
                baseAttack = 25,
                baseHp = 40,
                affixes = listOf(Affix(AffixType.ATTACK_FLAT, 8.0), Affix(AffixType.ATTACK_PERCENT, 4.0), Affix(AffixType.CRIT_RATE, 2.0)),
                gems = listOf(Gem(GemType.RUBY_ATTACK, 1)),
                isEquipped = true
            ),
            Equipment(
                playerId = playerId,
                name = "粗布道袍",
                type = EquipmentType.ARMOR,
                quality = EquipmentQuality.BLUE, // Blue quality with 2 sockets
                level = 1,
                baseHp = 120,
                baseDefense = 15,
                affixes = listOf(Affix(AffixType.HP_FLAT, 45.0), Affix(AffixType.DEFENSE_FLAT, 6.0)),
                gems = listOf(Gem(GemType.ONYX_DEFENSE, 1)),
                isEquipped = true
            ),
            Equipment(
                playerId = playerId,
                name = "束发头冠",
                type = EquipmentType.HELMET,
                quality = EquipmentQuality.WHITE,
                level = 1,
                baseHp = 45,
                baseDefense = 5,
                isEquipped = true
            ),
            Equipment(
                playerId = playerId,
                name = "青云布履",
                type = EquipmentType.SHOES,
                quality = EquipmentQuality.WHITE,
                level = 1,
                baseHp = 35,
                baseDefense = 4,
                isEquipped = true
            ),
            Equipment(
                playerId = playerId,
                name = "黄铜扳指",
                type = EquipmentType.RING,
                quality = EquipmentQuality.WHITE,
                level = 1,
                baseAttack = 10,
                isEquipped = true
            ),
            Equipment(
                playerId = playerId,
                name = "护身符石",
                type = EquipmentType.NECKLACE,
                quality = EquipmentQuality.WHITE,
                level = 1,
                baseHp = 60,
                isEquipped = true
            )
        )

        equipmentDao.insertAll(starterGear.map { Converters.toEntity(it) })
        return@withContext newPlayer
    }

    suspend fun switchActivePlayer(playerId: String) = withContext(Dispatchers.IO) {
        playerDao.setActivePlayer(playerId)
    }

    suspend fun deletePlayerProfile(playerId: String) = withContext(Dispatchers.IO) {
        equipmentDao.deleteAllByPlayerId(playerId)
        playerDao.deletePlayer(playerId)

        val remaining = playerDao.getAllPlayersDirect()
        if (remaining.isNotEmpty()) {
            playerDao.setActivePlayer(remaining.first().id)
        }
    }

    suspend fun savePlayer(player: Player) = withContext(Dispatchers.IO) {
        val updated = player.copy(lastActiveTimestamp = System.currentTimeMillis())
        playerDao.insertOrUpdate(Converters.toEntity(updated))
    }

    suspend fun equipItem(item: Equipment, playerId: String) = withContext(Dispatchers.IO) {
        // Unequip currently equipped item of the same slot
        val equipped = equipmentDao.getEquippedItemsDirect(playerId).map { Converters.toDomain(it) }
        val currentSlotItem = equipped.find { it.type == item.type }
        if (currentSlotItem != null) {
            equipmentDao.update(Converters.toEntity(currentSlotItem.copy(isEquipped = false)))
        }
        equipmentDao.update(Converters.toEntity(item.copy(isEquipped = true, playerId = playerId)))
    }

    suspend fun unequipItem(item: Equipment) = withContext(Dispatchers.IO) {
        equipmentDao.update(Converters.toEntity(item.copy(isEquipped = false)))
    }

    suspend fun insertEquipment(item: Equipment, playerId: String) = withContext(Dispatchers.IO) {
        equipmentDao.insert(Converters.toEntity(item.copy(playerId = playerId)))
    }

    suspend fun deleteEquipment(item: Equipment) = withContext(Dispatchers.IO) {
        equipmentDao.deleteById(item.id)
    }

    suspend fun toggleLockEquipment(item: Equipment) = withContext(Dispatchers.IO) {
        val updated = item.copy(isLocked = !item.isLocked)
        equipmentDao.update(Converters.toEntity(updated))
    }

    suspend fun enhanceEquipment(item: Equipment, player: Player): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val costGold = item.enhanceCost
        val costStones = item.enhanceStoneCost
        if (player.gold < costGold) {
            return@withContext Pair(false, "金币不足！需要 $costGold 金币")
        }
        if (player.enhanceStones < costStones) {
            return@withContext Pair(false, "强化石不足！需要 $costStones 颗强化石")
        }

        val successRate = item.enhanceSuccessRate
        val isSuccess = Random.nextInt(100) < successRate

        val newPlayer = player.copy(
            gold = player.gold - costGold,
            enhanceStones = player.enhanceStones - costStones
        )
        savePlayer(newPlayer)

        if (isSuccess) {
            val enhanced = item.copy(enhanceLevel = item.enhanceLevel + 1)
            equipmentDao.update(Converters.toEntity(enhanced))
            return@withContext Pair(true, "强化成功！【${item.name}】提升至 +${enhanced.enhanceLevel}")
        } else {
            return@withContext Pair(false, "强化失败！未改变装备等级。")
        }
    }

    suspend fun reforgeAffixes(item: Equipment, player: Player): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val reforgeCostDiamonds = 10L
        if (player.diamonds < reforgeCostDiamonds) {
            return@withContext Pair(false, "灵石不足！重铸词条需要 $reforgeCostDiamonds 灵石")
        }

        val newPlayer = player.copy(diamonds = player.diamonds - reforgeCostDiamonds)
        savePlayer(newPlayer)

        val newAffixes = EquipmentGenerator.generateAffixes(item.quality.maxAffixes, item.level, item.quality)
        val updated = item.copy(affixes = newAffixes)
        equipmentDao.update(Converters.toEntity(updated))
        return@withContext Pair(true, "洗练成功！获得全新的极品词条组合！")
    }

    suspend fun sellEquipment(item: Equipment, player: Player): Pair<Long, Int> = withContext(Dispatchers.IO) {
        val price = item.sellPrice
        val stones = item.meltStonesGained
        equipmentDao.deleteById(item.id)
        val updatedPlayer = player.copy(
            gold = player.gold + price,
            enhanceStones = player.enhanceStones + stones
        )
        savePlayer(updatedPlayer)
        Pair(price, stones)
    }

    suspend fun batchSellInventory(
        filterQuality: EquipmentQuality?,
        player: Player
    ): Triple<Int, Long, Int> = withContext(Dispatchers.IO) {
        val inventory = equipmentDao.getInventoryItemsDirect(player.id).map { Converters.toDomain(it) }
        val toSell = inventory.filter { item ->
            !item.isLocked && (filterQuality == null || item.quality.ordinal <= filterQuality.ordinal)
        }

        if (toSell.isEmpty()) return@withContext Triple(0, 0L, 0)

        var totalGold = 0L
        var totalStones = 0
        toSell.forEach {
            totalGold += it.sellPrice
            totalStones += it.meltStonesGained
        }

        equipmentDao.deleteByIds(toSell.map { it.id })
        val updatedPlayer = player.copy(
            gold = player.gold + totalGold,
            enhanceStones = player.enhanceStones + totalStones
        )
        savePlayer(updatedPlayer)

        Triple(toSell.size, totalGold, totalStones)
    }

    suspend fun calculateAndApplyOfflineProgress(player: Player): OfflineGainSummary? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val elapsedMillis = now - player.lastActiveTimestamp
        val elapsedSeconds = elapsedMillis / 1000L

        if (elapsedSeconds < 15L) {
            savePlayer(player.copy(lastActiveTimestamp = now))
            return@withContext null
        }

        val cappedSeconds = min(elapsedSeconds, 86400L)
        val battleDuration = 3.0 / player.battleSpeed.coerceIn(1.0, 4.0)
        val monstersCount = (cappedSeconds / battleDuration).toInt().coerceAtLeast(1)

        val monster = MonsterGenerator.createMonster(player.currentDungeonStage, false)
        val equipped = equipmentDao.getEquippedItemsDirect(player.id).map { Converters.toDomain(it) }
        val stats = player.calculateStats(equipped)

        val expPerMonster = (monster.expReward * (1.0 + stats.expBonus / 100.0)).roundToLong()
        val goldPerMonster = (monster.goldReward * (1.0 + stats.goldBonus / 100.0)).roundToLong()

        val totalExp = expPerMonster * monstersCount
        val totalGold = (goldPerMonster * monstersCount * 0.8).roundToLong()
        val totalStones = (monstersCount * 0.15).toInt()

        // Equipment drops simulation
        val droppedItems = mutableListOf<Equipment>()
        val dropCount = (monstersCount * 0.10).toInt().coerceAtMost(35)
        for (i in 0 until dropCount) {
            val eq = EquipmentGenerator.generateEquipment(
                level = player.currentDungeonStage,
                characterClass = player.characterClass,
                playerId = player.id
            )
            droppedItems.add(eq)
        }

        // Apply auto-sell if configured
        val autoSellFilter = player.autoSellQuality
        var autoSoldGold = 0L
        var autoSoldStones = 0
        var autoSoldCount = 0
        val keptItems = mutableListOf<Equipment>()

        for (eq in droppedItems) {
            if (autoSellFilter != null && eq.quality.ordinal <= autoSellFilter.ordinal) {
                autoSoldGold += eq.sellPrice
                autoSoldStones += eq.meltStonesGained
                autoSoldCount++
            } else {
                keptItems.add(eq)
            }
        }

        // Limit kept items to fit bag space (40 slots)
        val currentInventory = equipmentDao.getInventoryItemsDirect(player.id)
        val spaceLeft = (40 - currentInventory.size).coerceAtLeast(0)
        val finalKept = keptItems.take(spaceLeft)
        val overflow = keptItems.drop(spaceLeft)
        overflow.forEach {
            autoSoldGold += it.sellPrice
            autoSoldStones += it.meltStonesGained
            autoSoldCount++
        }

        // Level up calculations
        var newLevel = player.level
        var newExp = player.currentExp + totalExp
        var levelUps = 0

        while (true) {
            val reqExp = (newLevel.toDouble().let { it * it * 60.0 }).roundToLong() + 50L
            if (newExp >= reqExp) {
                newExp -= reqExp
                newLevel++
                levelUps++
            } else {
                break
            }
        }

        val finalGold = player.gold + totalGold + autoSoldGold
        val finalDiamonds = player.diamonds + (monstersCount / 40).coerceAtLeast(0)
        val finalStones = player.enhanceStones + totalStones + autoSoldStones

        val updatedPlayer = player.copy(
            level = newLevel,
            currentExp = newExp,
            gold = finalGold,
            diamonds = finalDiamonds,
            enhanceStones = finalStones,
            lastActiveTimestamp = now
        )

        savePlayer(updatedPlayer)
        if (finalKept.isNotEmpty()) {
            equipmentDao.insertAll(finalKept.map { Converters.toEntity(it) })
        }

        return@withContext OfflineGainSummary(
            offlineSeconds = cappedSeconds,
            monstersDefeated = monstersCount,
            expGained = totalExp,
            goldGained = totalGold + autoSoldGold,
            enhanceStonesGained = totalStones + autoSoldStones,
            itemsGained = finalKept,
            autoSoldItemsCount = autoSoldCount,
            autoSoldGoldGained = autoSoldGold,
            levelUps = levelUps
        )
    }

    suspend fun reincarnate(player: Player): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val minReincarnateLvl = 50 + (player.reincarnationCount * 25)
        if (player.level < minReincarnateLvl) {
            return@withContext Pair(false, "转生境界不足！当前需要达到 Lv.$minReincarnateLvl 方可逆天转生")
        }

        val gainedPoints = (player.level / 10) + (player.reincarnationCount * 3) + 5
        val updatedPlayer = player.copy(
            level = 1,
            currentExp = 0L,
            reincarnationCount = player.reincarnationCount + 1,
            reincarnationPoints = player.reincarnationPoints + gainedPoints,
            currentDungeonStage = 1
        )
        savePlayer(updatedPlayer)
        return@withContext Pair(true, "转生成功！重铸仙躯，获得 $gainedPoints 点转生潜能点！")
    }

    suspend fun upgradeTalent(type: TalentType, player: Player): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cost = type.costPerLevel
        if (player.reincarnationPoints < cost) {
            return@withContext Pair(false, "转生潜能点不足！需要 $cost 点")
        }
        val currentLevel = player.talentData.getLevel(type)
        if (currentLevel >= type.maxLevel) {
            return@withContext Pair(false, "该天赋已修炼至圆满境！")
        }

        val updatedTalent = player.talentData.withUpgrade(type)
        val updatedPlayer = player.copy(
            reincarnationPoints = player.reincarnationPoints - cost,
            talentData = updatedTalent
        )
        savePlayer(updatedPlayer)
        return@withContext Pair(true, "修炼成功！【${type.title}】提升至 Lv.${currentLevel + 1}")
    }

    suspend fun resetTalents(player: Player): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val spent = player.talentData.totalSpentPoints
        if (spent <= 0) {
            return@withContext Pair(false, "当前未分配任何转生点数")
        }
        val updatedPlayer = player.copy(
            reincarnationPoints = player.reincarnationPoints + spent,
            talentData = com.example.domain.model.TalentData()
        )
        savePlayer(updatedPlayer)
        return@withContext Pair(true, "重置成功！已返还 $spent 点转生潜能点")
    }

    // Gem System Operations (Requirements #3, #4, #5)
    suspend fun socketGem(
        item: Equipment,
        gemType: com.example.domain.model.GemType,
        level: Int,
        player: Player
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (item.gems.size >= item.maxSockets) {
            return@withContext Pair(false, "镶嵌孔位已满！${item.quality.displayName}品质装备最多镶嵌 ${item.maxSockets} 颗宝石")
        }
        if (gemType.category != item.type.allowedGemCategory) {
            val allowedDesc = if (item.type.allowedGemCategory == com.example.domain.model.GemCategory.OFFENSIVE) {
                "攻击类宝石（赤焰石、星辉石、紫晶石、血精石）"
            } else {
                "防守类宝石（玄铁石、青木石、风灵石、磐石）"
            }
            return@withContext Pair(false, "${item.type.displayName} 仅可镶嵌 $allowedDesc！")
        }
        val count = player.getGemCount(gemType, level)
        if (count <= 0) {
            return@withContext Pair(false, "背包中没有可用的 ${level}级${gemType.gemName}")
        }
        val updatedPlayer = player.removeGem(gemType, level, 1)
            ?: return@withContext Pair(false, "宝石扣除失败")

        val newGem = com.example.domain.model.Gem(gemType, level)
        val updatedGems = item.gems + newGem
        val updatedItem = item.copy(gems = updatedGems)

        equipmentDao.update(Converters.toEntity(updatedItem))
        savePlayer(updatedPlayer)

        Pair(true, "镶嵌成功！【${item.name}】已镶嵌【${newGem.fullName}】")
    }

    suspend fun unsocketGem(
        item: Equipment,
        gemIndex: Int,
        player: Player
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (gemIndex !in item.gems.indices) {
            return@withContext Pair(false, "宝石孔位无效")
        }
        val targetGem = item.gems[gemIndex]
        val updatedGems = item.gems.toMutableList().apply { removeAt(gemIndex) }
        val updatedItem = item.copy(gems = updatedGems)
        val updatedPlayer = player.addGem(targetGem.type, targetGem.level, 1)

        equipmentDao.update(Converters.toEntity(updatedItem))
        savePlayer(updatedPlayer)

        Pair(true, "已成功卸下【${targetGem.fullName}】并收回宝石囊")
    }

    suspend fun synthesizeGem(
        gemType: com.example.domain.model.GemType,
        level: Int,
        player: Player
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (level >= 10) {
            return@withContext Pair(false, "已达最高宝石等级 Lv.10")
        }
        val currentCount = player.getGemCount(gemType, level)
        if (currentCount < 3) {
            return@withContext Pair(false, "合成需要消耗 3 颗 ${level}级${gemType.gemName}，当前仅有 $currentCount 颗")
        }
        val updatedPlayer = player.synthesizeGem(gemType, level)
            ?: return@withContext Pair(false, "合成失败")

        savePlayer(updatedPlayer)
        Pair(true, "合成成功！获得 1 颗【${level + 1}级${gemType.gemName}】")
    }

    suspend fun synthesizeAllGems(player: Player): Pair<Int, String> = withContext(Dispatchers.IO) {
        val (updatedPlayer, totalCount) = player.synthesizeAllGems()
        if (totalCount > 0) {
            savePlayer(updatedPlayer)
            Pair(totalCount, "一键合成完毕！共合成进化 $totalCount 颗更高阶宝石")
        } else {
            Pair(0, "当前没有满足 3 颗同级条件的宝石可供合成")
        }
    }

    suspend fun buyGem(
        gemType: com.example.domain.model.GemType,
        level: Int,
        costGold: Long,
        costDiamonds: Long,
        player: Player
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (player.gold < costGold) {
            return@withContext Pair(false, "金币不足！需要 $costGold 金币")
        }
        if (player.diamonds < costDiamonds) {
            return@withContext Pair(false, "灵石不足！需要 $costDiamonds 灵石")
        }
        val updatedPlayer = player.copy(
            gold = player.gold - costGold,
            diamonds = player.diamonds - costDiamonds
        ).addGem(gemType, level, 1)

        savePlayer(updatedPlayer)
        Pair(true, "购买成功！获得【${level}级${gemType.gemName}】")
    }
}
