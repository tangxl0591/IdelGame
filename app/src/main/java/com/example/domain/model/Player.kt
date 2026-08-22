package com.example.domain.model

import java.util.UUID
import kotlin.math.pow
import kotlin.math.roundToLong

data class PlayerStats(
    val maxHp: Long,
    val minAttack: Long,
    val maxAttack: Long,
    val defense: Long,
    val critRate: Double,
    val critDamage: Double,
    val hpRegenPercent: Double,
    val blockRate: Double,
    val dodgeRate: Double,
    val lifeSteal: Double,
    val expBonus: Double,
    val goldBonus: Double,
    val attackSpeed: Double,
    val totalPower: Long
)

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "修仙剑客",
    val characterClass: CharacterClass = CharacterClass.WARRIOR,
    val level: Int = 1,
    val currentExp: Long = 0,
    val gold: Long = 1000,
    val diamonds: Long = 50,
    val enhanceStones: Int = 15,
    val reincarnationCount: Int = 0,
    val reincarnationPoints: Int = 0,
    val talentData: TalentData = TalentData(),
    val currentDungeonStage: Int = 1,
    val maxDungeonStage: Int = 1,
    val endlessTowerFloor: Int = 1,
    val maxEndlessTowerFloor: Int = 1,
    val autoChallenge: Boolean = true,
    val autoSellQuality: EquipmentQuality? = null, // Auto melt items below or equal to this
    val battleSpeed: Double = 1.0,
    val gemInventory: Map<String, Int> = emptyMap(), // Key: "${gemType.id}_${level}", Value: count
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    val maxExp: Long
        get() = (level.toDouble().pow(1.55) * 60.0).roundToLong() + 50L

    fun getGemCount(type: GemType, level: Int): Int {
        val key = "${type.id}_$level"
        return gemInventory[key] ?: 0
    }

    fun addGem(type: GemType, level: Int, count: Int = 1): Player {
        val key = "${type.id}_$level"
        val current = gemInventory[key] ?: 0
        val updatedMap = gemInventory.toMutableMap()
        updatedMap[key] = current + count
        return copy(gemInventory = updatedMap)
    }

    fun removeGem(type: GemType, level: Int, count: Int = 1): Player? {
        val key = "${type.id}_$level"
        val current = gemInventory[key] ?: 0
        if (current < count) return null
        val updatedMap = gemInventory.toMutableMap()
        if (current - count <= 0) {
            updatedMap.remove(key)
        } else {
            updatedMap[key] = current - count
        }
        return copy(gemInventory = updatedMap)
    }

    fun synthesizeGem(type: GemType, level: Int): Player? {
        if (level >= 10) return null
        val current = getGemCount(type, level)
        if (current < 3) return null
        val afterRemove = removeGem(type, level, 3) ?: return null
        return afterRemove.addGem(type, level + 1, 1)
    }

    fun synthesizeAllGems(): Pair<Player, Int> {
        var currentPlayer = this
        var totalSynthesized = 0
        var changed = true
        while (changed) {
            changed = false
            for (type in GemType.entries) {
                for (lvl in 1..9) {
                    val count = currentPlayer.getGemCount(type, lvl)
                    if (count >= 3) {
                        val numSynth = count / 3
                        val rem = currentPlayer.removeGem(type, lvl, numSynth * 3)
                        if (rem != null) {
                            currentPlayer = rem.addGem(type, lvl + 1, numSynth)
                            totalSynthesized += numSynth
                            changed = true
                        }
                    }
                }
            }
        }
        return Pair(currentPlayer, totalSynthesized)
    }

    fun getAllGemsList(): List<GemInventoryItem> {
        val list = mutableListOf<GemInventoryItem>()
        gemInventory.forEach { (key, count) ->
            if (count > 0) {
                val parts = key.split("_")
                if (parts.size >= 2) {
                    val typeStr = parts.dropLast(1).joinToString("_")
                    val lvl = parts.last().toIntOrNull() ?: 1
                    val type = GemType.fromId(typeStr)
                    list.add(GemInventoryItem(type, lvl, count))
                }
            }
        }
        return list.sortedWith(compareBy<GemInventoryItem> { it.type.category.ordinal }.thenBy { it.type.ordinal }.thenByDescending { it.level })
    }

    fun calculateStats(equippedItems: List<Equipment>): PlayerStats {
        // Base character scaling with level and character class modifiers
        val baseLvl = level.toDouble()
        val rawBaseHp = (100.0 + (baseLvl * 35.0) + (baseLvl.pow(1.2) * 10.0)) * characterClass.hpScale
        val rawBaseAtk = (15.0 + (baseLvl * 7.0) + (baseLvl.pow(1.15) * 2.5)) * characterClass.atkScale
        val rawBaseDef = (5.0 + (baseLvl * 3.5) + (baseLvl.pow(1.1) * 1.2)) * characterClass.defScale

        // Talent bonuses
        val talentAtkBonusPct = talentData.attackLevel * 5.0
        val talentHpBonusPct = talentData.hpLevel * 6.0
        val talentDefBonusPct = talentData.defenseLevel * 5.0
        val talentCritRateBonus = talentData.critLevel * 0.8
        val talentCritDmgBonus = talentData.critLevel * 5.0
        val talentLifeStealBonus = talentData.lifestealLevel * 0.6
        val talentGoldExpBonus = talentData.goldExpLevel * 8.0

        // Equipment sums
        var equipAtkFlat = 0L
        var equipHpFlat = 0L
        var equipDefFlat = 0L

        var equipAtkPct = 0.0
        var equipHpPct = 0.0
        var equipDefPct = 0.0
        var equipCritRate = 0.0
        var equipCritDmg = 0.0
        var equipHpRegen = 0.0
        var equipBlockRate = 0.0
        var equipDodgeRate = 0.0
        var equipLifeSteal = 0.0
        var equipExpBonus = 0.0
        var equipGoldBonus = 0.0

        for (item in equippedItems) {
            equipAtkFlat += item.totalAttack
            equipHpFlat += item.totalHp
            equipDefFlat += item.totalDefense

            for (affix in item.affixes) {
                when (affix.type) {
                    AffixType.ATTACK_FLAT -> {}
                    AffixType.ATTACK_PERCENT -> equipAtkPct += affix.value
                    AffixType.HP_FLAT -> {}
                    AffixType.HP_PERCENT -> equipHpPct += affix.value
                    AffixType.DEFENSE_FLAT -> {}
                    AffixType.DEFENSE_PERCENT -> equipDefPct += affix.value
                    AffixType.CRIT_RATE -> equipCritRate += affix.value
                    AffixType.CRIT_DAMAGE -> equipCritDmg += affix.value
                    AffixType.HP_REGEN -> equipHpRegen += affix.value
                    AffixType.BLOCK_RATE -> equipBlockRate += affix.value
                    AffixType.DODGE_RATE -> equipDodgeRate += affix.value
                    AffixType.LIFE_STEAL -> equipLifeSteal += affix.value
                    AffixType.EXP_BONUS -> equipExpBonus += affix.value
                    AffixType.GOLD_BONUS -> equipGoldBonus += affix.value
                }
            }

            // Socketed Gem bonuses on equipped items
            for (gem in item.gems) {
                when (gem.type) {
                    GemType.RUBY_ATTACK -> {} // already included in item.totalAttack
                    GemType.SAPPHIRE_MAGIC -> equipCritDmg += gem.statValue
                    GemType.AMETHYST_CRIT -> equipCritRate += gem.statValue
                    GemType.BLOOD_LIFESTEAL -> equipLifeSteal += gem.statValue
                    GemType.ONYX_DEFENSE -> {} // already included in item.totalDefense
                    GemType.EMERALD_HP -> {} // already included in item.totalHp
                    GemType.TOPAZ_DODGE -> equipDodgeRate += gem.statValue
                    GemType.DIAMOND_BLOCK -> equipBlockRate += gem.statValue
                }
            }
        }

        // Reincarnation stat bonus (each rebirth gives +10% all base attributes)
        val rebirthMultiplier = 1.0 + (reincarnationCount * 0.10)

        // Final Calculations
        val totalHpMultiplier = (1.0 + ((talentHpBonusPct + equipHpPct) / 100.0)) * rebirthMultiplier
        val finalMaxHp = ((rawBaseHp + equipHpFlat) * totalHpMultiplier).roundToLong()

        val totalAtkMultiplier = (1.0 + ((talentAtkBonusPct + equipAtkPct) / 100.0)) * rebirthMultiplier
        val finalBaseAtk = ((rawBaseAtk + equipAtkFlat) * totalAtkMultiplier).roundToLong()
        val minAtk = (finalBaseAtk * 0.9).roundToLong().coerceAtLeast(1L)
        val maxAtk = (finalBaseAtk * 1.1).roundToLong().coerceAtLeast(minAtk)

        val totalDefMultiplier = (1.0 + ((talentDefBonusPct + equipDefPct) / 100.0)) * rebirthMultiplier
        val finalDef = ((rawBaseDef + equipDefFlat) * totalDefMultiplier).roundToLong()

        val finalCritRate = (5.0 + characterClass.critRateBonus + talentCritRateBonus + equipCritRate).coerceIn(5.0, 95.0)
        val finalCritDamage = 150.0 + characterClass.critDmgBonus + talentCritDmgBonus + equipCritDmg
        val finalHpRegen = 2.0 + equipHpRegen
        val finalBlockRate = (2.0 + characterClass.blockBonus + equipBlockRate).coerceIn(2.0, 80.0)
        val finalDodgeRate = (3.0 + characterClass.dodgeBonus + equipDodgeRate).coerceIn(3.0, 80.0)
        val finalLifeSteal = (characterClass.lifeStealBonus + talentLifeStealBonus + equipLifeSteal).coerceIn(0.0, 80.0)
        val finalExpBonus = talentGoldExpBonus + equipExpBonus
        val finalGoldBonus = talentGoldExpBonus + equipGoldBonus
        val finalAttackSpeed = characterClass.attackSpeedBonus

        val power = (finalBaseAtk * 3L) + (finalMaxHp / 4L) + (finalDef * 4L) +
                ((finalCritRate * 50).toLong()) + ((finalCritDamage * 10).toLong()) +
                ((finalBlockRate * 30).toLong()) + ((finalDodgeRate * 30).toLong())

        return PlayerStats(
            maxHp = finalMaxHp,
            minAttack = minAtk,
            maxAttack = maxAtk,
            defense = finalDef,
            critRate = finalCritRate,
            critDamage = finalCritDamage,
            hpRegenPercent = finalHpRegen,
            blockRate = finalBlockRate,
            dodgeRate = finalDodgeRate,
            lifeSteal = finalLifeSteal,
            expBonus = finalExpBonus,
            goldBonus = finalGoldBonus,
            attackSpeed = finalAttackSpeed,
            totalPower = power
        )
    }
}
