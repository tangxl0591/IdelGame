package com.example.data.db

import com.example.data.db.entity.EquipmentEntity
import com.example.data.db.entity.PlayerEntity
import com.example.domain.model.Affix
import com.example.domain.model.AffixType
import com.example.domain.model.CharacterClass
import com.example.domain.model.Equipment
import com.example.domain.model.EquipmentQuality
import com.example.domain.model.EquipmentType
import com.example.domain.model.Gem
import com.example.domain.model.GemType
import com.example.domain.model.Player
import com.example.domain.model.TalentData

object Converters {
    fun serializeAffixes(affixes: List<Affix>): String {
        return affixes.joinToString(";") { "${it.type.name}:${it.value}" }
    }

    fun deserializeAffixes(raw: String): List<Affix> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val type = try { AffixType.valueOf(parts[0]) } catch (e: Exception) { null }
                val value = parts[1].toDoubleOrNull()
                if (type != null && value != null) {
                    Affix(type, value)
                } else null
            } else null
        }
    }

    fun serializeGems(gems: List<Gem>): String {
        return gems.joinToString(";") { "${it.type.id}:${it.level}" }
    }

    fun deserializeGems(raw: String): List<Gem> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val type = GemType.fromId(parts[0])
                val level = parts[1].toIntOrNull() ?: 1
                Gem(type, level)
            } else null
        }
    }

    fun serializeGemInventory(gemMap: Map<String, Int>): String {
        return gemMap.entries.filter { it.value > 0 }.joinToString(";") { "${it.key}:${it.value}" }
    }

    fun deserializeGemInventory(raw: String): Map<String, Int> {
        if (raw.isBlank()) return emptyMap()
        val map = mutableMapOf<String, Int>()
        raw.split(";").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val key = parts[0]
                val count = parts[1].toIntOrNull() ?: 0
                if (count > 0) map[key] = count
            }
        }
        return map
    }

    fun toDomain(entity: EquipmentEntity): Equipment {
        return Equipment(
            id = entity.id,
            playerId = entity.playerId,
            name = entity.name,
            type = try { EquipmentType.valueOf(entity.type) } catch (e: Exception) { EquipmentType.WEAPON },
            quality = try { EquipmentQuality.valueOf(entity.quality) } catch (e: Exception) { EquipmentQuality.WHITE },
            level = entity.level,
            enhanceLevel = entity.enhanceLevel,
            baseAttack = entity.baseAttack,
            baseHp = entity.baseHp,
            baseDefense = entity.baseDefense,
            affixes = deserializeAffixes(entity.affixesJson),
            gems = deserializeGems(entity.gemsJson),
            isEquipped = entity.isEquipped,
            isLocked = entity.isLocked
        )
    }

    fun toEntity(domain: Equipment): EquipmentEntity {
        return EquipmentEntity(
            id = domain.id,
            playerId = domain.playerId,
            name = domain.name,
            type = domain.type.name,
            quality = domain.quality.name,
            level = domain.level,
            enhanceLevel = domain.enhanceLevel,
            baseAttack = domain.baseAttack,
            baseHp = domain.baseHp,
            baseDefense = domain.baseDefense,
            affixesJson = serializeAffixes(domain.affixes),
            gemsJson = serializeGems(domain.gems),
            isEquipped = domain.isEquipped,
            isLocked = domain.isLocked
        )
    }

    fun toDomain(entity: PlayerEntity): Player {
        return Player(
            id = entity.id,
            name = entity.name,
            characterClass = CharacterClass.fromId(entity.characterClass),
            level = entity.level,
            currentExp = entity.currentExp,
            gold = entity.gold,
            diamonds = entity.diamonds,
            enhanceStones = entity.enhanceStones,
            reincarnationCount = entity.reincarnationCount,
            reincarnationPoints = entity.reincarnationPoints,
            talentData = TalentData(
                attackLevel = entity.attackTalentLevel,
                hpLevel = entity.hpTalentLevel,
                defenseLevel = entity.defenseTalentLevel,
                critLevel = entity.critTalentLevel,
                lifestealLevel = entity.lifestealTalentLevel,
                luckyTalentLevel = entity.luckyTalentLevel,
                goldExpLevel = entity.goldExpTalentLevel
            ),
            currentDungeonStage = entity.currentDungeonStage,
            maxDungeonStage = entity.maxDungeonStage,
            endlessTowerFloor = entity.endlessTowerFloor,
            maxEndlessTowerFloor = entity.maxEndlessTowerFloor,
            autoChallenge = entity.autoChallenge,
            autoSellQuality = entity.autoSellQuality?.let {
                try { EquipmentQuality.valueOf(it) } catch (e: Exception) { null }
            },
            battleSpeed = entity.battleSpeed,
            gemInventory = deserializeGemInventory(entity.gemsInventoryJson),
            lastActiveTimestamp = entity.lastActiveTimestamp,
            createdAt = entity.createdAt,
            isActive = entity.isActive
        )
    }

    fun toEntity(domain: Player): PlayerEntity {
        return PlayerEntity(
            id = domain.id,
            name = domain.name,
            characterClass = domain.characterClass.id,
            level = domain.level,
            currentExp = domain.currentExp,
            gold = domain.gold,
            diamonds = domain.diamonds,
            enhanceStones = domain.enhanceStones,
            reincarnationCount = domain.reincarnationCount,
            reincarnationPoints = domain.reincarnationPoints,
            attackTalentLevel = domain.talentData.attackLevel,
            hpTalentLevel = domain.talentData.hpLevel,
            defenseTalentLevel = domain.talentData.defenseLevel,
            critTalentLevel = domain.talentData.critLevel,
            lifestealTalentLevel = domain.talentData.lifestealLevel,
            luckyTalentLevel = domain.talentData.luckyLevel,
            goldExpTalentLevel = domain.talentData.goldExpLevel,
            currentDungeonStage = domain.currentDungeonStage,
            maxDungeonStage = domain.maxDungeonStage,
            endlessTowerFloor = domain.endlessTowerFloor,
            maxEndlessTowerFloor = domain.maxEndlessTowerFloor,
            autoChallenge = domain.autoChallenge,
            autoSellQuality = domain.autoSellQuality?.name,
            battleSpeed = domain.battleSpeed,
            gemsInventoryJson = serializeGemInventory(domain.gemInventory),
            lastActiveTimestamp = domain.lastActiveTimestamp,
            createdAt = domain.createdAt,
            isActive = domain.isActive
        )
    }
}
