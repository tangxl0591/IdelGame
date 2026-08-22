package com.example.domain.model

import java.util.UUID
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

data class Equipment(
    val id: String = UUID.randomUUID().toString(),
    val playerId: String = "",
    val name: String,
    val type: EquipmentType,
    val quality: EquipmentQuality,
    val level: Int,
    val enhanceLevel: Int = 0,
    val baseAttack: Int = 0,
    val baseHp: Int = 0,
    val baseDefense: Int = 0,
    val affixes: List<Affix> = emptyList(),
    val gems: List<Gem> = emptyList(),
    val isEquipped: Boolean = false,
    val isLocked: Boolean = false
) {
    val maxSockets: Int get() = quality.maxGemSockets

    val totalAttack: Int
        get() {
            val mult = 1.0 + (enhanceLevel * 0.12)
            var flat = (baseAttack * mult).roundToInt()
            affixes.forEach {
                if (it.type == AffixType.ATTACK_FLAT) flat += it.value.toInt()
            }
            gems.forEach {
                if (it.type == GemType.RUBY_ATTACK) flat += it.statValue.toInt()
            }
            return flat
        }

    val totalHp: Int
        get() {
            val mult = 1.0 + (enhanceLevel * 0.12)
            var flat = (baseHp * mult).roundToInt()
            affixes.forEach {
                if (it.type == AffixType.HP_FLAT) flat += it.value.toInt()
            }
            gems.forEach {
                if (it.type == GemType.EMERALD_HP) flat += it.statValue.toInt()
            }
            return flat
        }

    val totalDefense: Int
        get() {
            val mult = 1.0 + (enhanceLevel * 0.12)
            var flat = (baseDefense * mult).roundToInt()
            affixes.forEach {
                if (it.type == AffixType.DEFENSE_FLAT) flat += it.value.toInt()
            }
            gems.forEach {
                if (it.type == GemType.ONYX_DEFENSE) flat += it.statValue.toInt()
            }
            return flat
        }

    val sellPrice: Long
        get() {
            val base = quality.sellPriceMultiplier.toLong() * level
            val enhanceBonus = (enhanceLevel * 250L * level)
            return base + enhanceBonus
        }

    val meltStonesGained: Int
        get() {
            val base = quality.meltStoneRate
            val enhanceBonus = enhanceLevel * 2
            return base + enhanceBonus
        }

    val enhanceCost: Long
        get() {
            return ((enhanceLevel + 1) * 350L * (level.coerceAtLeast(1) / 2 + 1))
        }

    val enhanceStoneCost: Int
        get() {
            return (enhanceLevel / 2) + 1
        }

    val enhanceSuccessRate: Int
        get() = when {
            enhanceLevel < 5 -> 100
            enhanceLevel < 10 -> 85
            enhanceLevel < 15 -> 65
            enhanceLevel < 20 -> 45
            enhanceLevel < 25 -> 30
            else -> 15
        }

    // Comprehensive Equipment Power Score (Requirement #2 & #3 & #4)
    val powerScore: Long
        get() {
            var score = (totalAttack * 3.6) + (totalHp * 0.45) + (totalDefense * 4.0)
            affixes.forEach { affix ->
                val weight = when (affix.type) {
                    AffixType.ATTACK_FLAT -> affix.value * 3.6
                    AffixType.ATTACK_PERCENT -> affix.value * 38.0
                    AffixType.HP_FLAT -> affix.value * 0.45
                    AffixType.HP_PERCENT -> affix.value * 32.0
                    AffixType.DEFENSE_FLAT -> affix.value * 4.0
                    AffixType.DEFENSE_PERCENT -> affix.value * 35.0
                    AffixType.CRIT_RATE -> affix.value * 85.0
                    AffixType.CRIT_DAMAGE -> affix.value * 18.0
                    AffixType.HP_REGEN -> affix.value * 50.0
                    AffixType.BLOCK_RATE -> affix.value * 65.0
                    AffixType.DODGE_RATE -> affix.value * 65.0
                    AffixType.LIFE_STEAL -> affix.value * 80.0
                    AffixType.EXP_BONUS -> affix.value * 25.0
                    AffixType.GOLD_BONUS -> affix.value * 25.0
                }
                score += weight
            }
            gems.forEach { gem ->
                score += gem.scoreValue
            }
            if (maxSockets > 0) {
                score += (maxSockets * 45L)
            }
            return (score * (1.0 + enhanceLevel * 0.08)).roundToInt().toLong().coerceAtLeast(10L)
        }

    val ratingTier: String
        get() = when {
            powerScore >= 120_000 -> "SSS"
            powerScore >= 50_000 -> "SS"
            powerScore >= 20_000 -> "S"
            powerScore >= 8_000 -> "A"
            powerScore >= 3_000 -> "B"
            else -> "C"
        }
}

object EquipmentGenerator {
    private val prefixes = listOf(
        "粗制", "精良", "百炼", "无双", "狂暴", "破晓", "修罗", "幽冥", "斩魂", "灭世",
        "神圣", "烈焰", "霜语", "星辰", "混沌", "太虚", "造化", "万界", "鸿蒙", "不朽"
    )

    private val warriorWeapons = listOf(
        "精铁剑", "青铜阔剑", "龙泉重刃", "天罡巨剑", "开山战斧", "方天画戟",
        "破天狂刃", "斩魔重剑", "不灭战斧", "混沌巨刃", "太虚龙雀剑", "诛仙古刃"
    )

    private val mageWeapons = listOf(
        "木质法杖", "灵木法杖", "碧玉法球", "星辉灵杖", "炽炎魔杖", "奥术秘典",
        "极冰权杖", "虚空法球", "万灵唤魔杖", "造化玉碟", "太初混沌杖", "创世神典"
    )

    private val archerWeapons = listOf(
        "猎人之弓", "铁木短弓", "疾风战弩", "逐日连弩", "追风神弓", "穿云劲弩",
        "绝影神弓", "暗夜猎魄弩", "星辰神臂弓", "碎空流影弓", "万象诛天弩", "极速天煞弓"
    )

    private val armorNames = listOf("布衣", "皮质软甲", "秘银战甲", "圣灵长袍", "龙鳞锁子甲", "幽冥暗铠", "天蚕宝衣", "混沌神甲", "不灭金甲", "鸿蒙帝袍")
    private val helmetNames = listOf("布帽", "皮革头盔", "精钢战盔", "幻影法帽", "龙骨战盔", "修罗面具", "乾坤冠", "诸神王冠", "天启圣冠", "至尊神冕")
    private val shoesNames = listOf("草鞋", "皮革履", "疾风履", "追云战靴", "影舞短靴", "踏云宝靴", "缩地神履", "万界极速战靴", "御风天灵履", "登仙踏虚靴")
    private val ringNames = listOf("铜戒", "铁环戒", "灵蛇戒", "翡翠扳指", "吸血鬼之戒", "虚空之戒", "至尊神戒", "永恒契约", "太初乾坤戒", "万古不灭戒")
    private val necklaceNames = listOf("麻绳坠", "玛瑙项链", "祝福吊坠", "龙心挂坠", "星月神链", "苍穹之泪", "造化挂坠", "诸神之眼", "九天凤血链", "万象源生链")

    fun generateEquipment(
        level: Int,
        forcedQuality: EquipmentQuality? = null,
        type: EquipmentType = EquipmentType.entries.random(),
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        playerId: String = ""
    ): Equipment {
        val lvl = level.coerceAtLeast(1)
        val quality = forcedQuality ?: rollQuality()
        val name = generateName(type, quality, lvl, characterClass)

        var baseAtk = 0
        var baseHp = 0
        var baseDef = 0

        val statBase = (lvl.toDouble().pow(1.18) * 8.5).roundToInt().coerceAtLeast(10)
        val qualityMult = quality.statMultiplier

        when (type) {
            EquipmentType.WEAPON -> {
                baseAtk = (statBase * 1.6 * qualityMult).roundToInt().coerceAtLeast(8)
                baseHp = (statBase * 0.4 * qualityMult).roundToInt()
            }
            EquipmentType.ARMOR -> {
                baseHp = (statBase * 2.8 * qualityMult).roundToInt().coerceAtLeast(30)
                baseDef = (statBase * 0.9 * qualityMult).roundToInt().coerceAtLeast(4)
            }
            EquipmentType.HELMET -> {
                baseHp = (statBase * 1.8 * qualityMult).roundToInt().coerceAtLeast(20)
                baseDef = (statBase * 0.6 * qualityMult).roundToInt().coerceAtLeast(3)
            }
            EquipmentType.SHOES -> {
                baseDef = (statBase * 0.7 * qualityMult).roundToInt().coerceAtLeast(3)
                baseHp = (statBase * 1.0 * qualityMult).roundToInt().coerceAtLeast(15)
            }
            EquipmentType.RING -> {
                baseAtk = (statBase * 1.2 * qualityMult).roundToInt().coerceAtLeast(5)
                baseHp = (statBase * 0.8 * qualityMult).roundToInt().coerceAtLeast(10)
            }
            EquipmentType.NECKLACE -> {
                baseHp = (statBase * 2.0 * qualityMult).roundToInt().coerceAtLeast(25)
                baseAtk = (statBase * 0.6 * qualityMult).roundToInt().coerceAtLeast(4)
            }
        }

        val affixCount = when (quality) {
            EquipmentQuality.WHITE -> if (Random.nextDouble() < 0.3) 1 else 0
            EquipmentQuality.GREEN -> 1 + if (Random.nextDouble() < 0.5) 1 else 0
            EquipmentQuality.BLUE -> 2 + if (Random.nextDouble() < 0.6) 1 else 0
            EquipmentQuality.YELLOW -> 3 + if (Random.nextDouble() < 0.7) 1 else 0
            EquipmentQuality.RED -> 4 + if (Random.nextDouble() < 0.8) 1 else 0
            EquipmentQuality.GOLD -> 5 + if (Random.nextDouble() < 0.9) 1 else 0
            EquipmentQuality.MYTHIC -> 7
        }

        val affixes = generateAffixes(affixCount, lvl, quality)

        return Equipment(
            playerId = playerId,
            name = name,
            type = type,
            quality = quality,
            level = lvl,
            baseAttack = baseAtk,
            baseHp = baseHp,
            baseDefense = baseDef,
            affixes = affixes
        )
    }

    private fun rollQuality(): EquipmentQuality {
        val roll = Random.nextDouble(100.0)
        return when {
            roll < 1.5 -> EquipmentQuality.MYTHIC  // 1.5%
            roll < 5.5 -> EquipmentQuality.GOLD    // 4.0%
            roll < 13.0 -> EquipmentQuality.RED    // 7.5%
            roll < 26.0 -> EquipmentQuality.YELLOW // 13.0%
            roll < 46.0 -> EquipmentQuality.BLUE   // 20.0%
            roll < 72.0 -> EquipmentQuality.GREEN  // 26.0%
            else -> EquipmentQuality.WHITE         // 28.0%
        }
    }

    fun rollBossQuality(): EquipmentQuality {
        val roll = Random.nextDouble(100.0)
        return when {
            roll < 6.0 -> EquipmentQuality.MYTHIC   // 6.0% Boss mythic chance
            roll < 18.0 -> EquipmentQuality.GOLD    // 12.0%
            roll < 38.0 -> EquipmentQuality.RED     // 20.0%
            roll < 65.0 -> EquipmentQuality.YELLOW  // 27.0%
            roll < 90.0 -> EquipmentQuality.BLUE    // 25.0%
            else -> EquipmentQuality.GREEN          // 10.0%
        }
    }

    private fun generateName(
        type: EquipmentType,
        quality: EquipmentQuality,
        level: Int,
        characterClass: CharacterClass
    ): String {
        val prefixIndex = (quality.ordinal * 2 + Random.nextInt(2)).coerceIn(0, prefixes.size - 1)
        val prefix = if (quality != EquipmentQuality.WHITE) "${prefixes[prefixIndex]}·" else ""

        val baseList = when (type) {
            EquipmentType.WEAPON -> when (characterClass) {
                CharacterClass.WARRIOR -> warriorWeapons
                CharacterClass.MAGE -> mageWeapons
                CharacterClass.ARCHER -> archerWeapons
            }
            EquipmentType.ARMOR -> armorNames
            EquipmentType.HELMET -> helmetNames
            EquipmentType.SHOES -> shoesNames
            EquipmentType.RING -> ringNames
            EquipmentType.NECKLACE -> necklaceNames
        }
        val nameIndex = ((level / 80).coerceAtMost(baseList.size - 1) + Random.nextInt(2)).coerceIn(0, baseList.size - 1)
        val baseName = baseList[nameIndex]
        return "$prefix$baseName"
    }

    fun generateAffixes(count: Int, level: Int, quality: EquipmentQuality): List<Affix> {
        val pool = AffixType.entries.toMutableList()
        pool.shuffle()

        val list = mutableListOf<Affix>()
        val selectedTypes = pool.take(count)

        for (type in selectedTypes) {
            val value = calculateAffixValue(type, level, quality)
            list.add(Affix(type, value))
        }
        return list
    }

    private fun calculateAffixValue(type: AffixType, level: Int, quality: EquipmentQuality): Double {
        val qMult = 1.0 + (quality.ordinal * 0.3)
        return when (type) {
            AffixType.ATTACK_FLAT -> (level * 4.0 * qMult * (0.85 + Random.nextDouble() * 0.35)).coerceAtLeast(3.0)
            AffixType.ATTACK_PERCENT -> (1.5 + (level * 0.08) + Random.nextDouble() * 3.0) * qMult
            AffixType.HP_FLAT -> (level * 25.0 * qMult * (0.85 + Random.nextDouble() * 0.35)).coerceAtLeast(15.0)
            AffixType.HP_PERCENT -> (2.0 + (level * 0.10) + Random.nextDouble() * 4.0) * qMult
            AffixType.DEFENSE_FLAT -> (level * 2.5 * qMult * (0.85 + Random.nextDouble() * 0.35)).coerceAtLeast(2.0)
            AffixType.DEFENSE_PERCENT -> (1.5 + (level * 0.06) + Random.nextDouble() * 2.5) * qMult
            AffixType.CRIT_RATE -> (1.2 + Random.nextDouble() * 3.5) * (1.0 + quality.ordinal * 0.18)
            AffixType.CRIT_DAMAGE -> (12.0 + Random.nextDouble() * 25.0) * (1.0 + quality.ordinal * 0.20)
            AffixType.HP_REGEN -> (0.3 + Random.nextDouble() * 1.2) * (1.0 + quality.ordinal * 0.15)
            AffixType.BLOCK_RATE -> (1.5 + Random.nextDouble() * 4.0) * (1.0 + quality.ordinal * 0.18)
            AffixType.DODGE_RATE -> (1.2 + Random.nextDouble() * 3.5) * (1.0 + quality.ordinal * 0.18)
            AffixType.LIFE_STEAL -> (0.8 + Random.nextDouble() * 2.5) * (1.0 + quality.ordinal * 0.18)
            AffixType.EXP_BONUS -> (3.0 + Random.nextDouble() * 6.0) * (1.0 + quality.ordinal * 0.18)
            AffixType.GOLD_BONUS -> (3.0 + Random.nextDouble() * 6.0) * (1.0 + quality.ordinal * 0.18)
        }
    }
}
