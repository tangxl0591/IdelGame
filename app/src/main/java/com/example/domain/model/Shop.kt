package com.example.domain.model

import java.util.UUID

enum class ShopItemType {
    EQUIPMENT,
    ENHANCE_STONE_PACK,
    SPIRIT_CHEST,
    REFORGE_STONE,
    GEM_POUCH,
    GEM
}

data class ShopItem(
    val id: String = UUID.randomUUID().toString(),
    val type: ShopItemType,
    val name: String,
    val description: String,
    val goldCost: Long,
    val diamondCost: Long = 0,
    val equipment: Equipment? = null,
    val gemType: GemType? = null,
    val gemLevel: Int = 1,
    val quantity: Int = 1,
    val iconEmoji: String = "🎁",
    var isPurchased: Boolean = false
)

object ShopGenerator {
    fun generateDailyShop(
        playerLevel: Int,
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        playerId: String = ""
    ): List<ShopItem> {
        val items = mutableListOf<ShopItem>()
        val lvl = playerLevel.coerceAtLeast(1)

        // 1. Guaranteed Rare, Epic, or Mythic Equipment
        val qualities = listOf(
            EquipmentQuality.YELLOW,
            EquipmentQuality.RED,
            if (playerLevel >= 10) EquipmentQuality.GOLD else EquipmentQuality.YELLOW,
            if (playerLevel >= 25) EquipmentQuality.MYTHIC else EquipmentQuality.RED
        )
        val selectedQuality = qualities.random()
        val eq1 = EquipmentGenerator.generateEquipment(
            level = lvl,
            forcedQuality = selectedQuality,
            characterClass = characterClass,
            playerId = playerId
        )
        items.add(
            ShopItem(
                type = ShopItemType.EQUIPMENT,
                name = eq1.name,
                description = "【${eq1.quality.displayName}】品质 Lv.${eq1.level} 装备 (孔位: ${eq1.maxSockets}个, 评分: ${eq1.powerScore})",
                goldCost = eq1.sellPrice * 2L + 500L,
                equipment = eq1,
                iconEmoji = when (eq1.type) {
                    EquipmentType.WEAPON -> "⚔️"
                    EquipmentType.ARMOR -> "🛡️"
                    EquipmentType.HELMET -> "🪖"
                    EquipmentType.SHOES -> "👢"
                    EquipmentType.RING -> "💍"
                    EquipmentType.NECKLACE -> "📿"
                }
            )
        )

        // 2. Gem Offerings (Offensive Gem and Defensive Gem)
        val offensiveGem = listOf(GemType.RUBY_ATTACK, GemType.SAPPHIRE_MAGIC, GemType.AMETHYST_CRIT, GemType.BLOOD_LIFESTEAL).random()
        val gemLvl1 = (1 + (lvl / 30)).coerceIn(1, 4)
        items.add(
            ShopItem(
                type = ShopItemType.GEM,
                name = "${gemLvl1}级${offensiveGem.gemName} (${offensiveGem.displayName})",
                description = "【${offensiveGem.category.displayName}】${offensiveGem.description}。仅可镶嵌于武器、戒指、项链。",
                goldCost = 3000L + (gemLvl1 * 2500L),
                gemType = offensiveGem,
                gemLevel = gemLvl1,
                quantity = 1,
                iconEmoji = offensiveGem.iconEmoji
            )
        )

        val defensiveGem = listOf(GemType.ONYX_DEFENSE, GemType.EMERALD_HP, GemType.TOPAZ_DODGE, GemType.DIAMOND_BLOCK).random()
        val gemLvl2 = (1 + (lvl / 30)).coerceIn(1, 4)
        items.add(
            ShopItem(
                type = ShopItemType.GEM,
                name = "${gemLvl2}级${defensiveGem.gemName} (${defensiveGem.displayName})",
                description = "【${defensiveGem.category.displayName}】${defensiveGem.description}。仅可镶嵌于战甲、头盔、战靴。",
                goldCost = 2800L + (gemLvl2 * 2200L),
                gemType = defensiveGem,
                gemLevel = gemLvl2,
                quantity = 1,
                iconEmoji = defensiveGem.iconEmoji
            )
        )

        // 3. Enhance Stone Packs
        val stoneQty1 = 15 + (playerLevel * 3)
        items.add(
            ShopItem(
                type = ShopItemType.ENHANCE_STONE_PACK,
                name = "强化石宝匣 ($stoneQty1 颗)",
                description = "用于强化装备基础属性，大幅提高人物战力",
                goldCost = 1500L + (playerLevel * 120L),
                quantity = stoneQty1,
                iconEmoji = "🔮"
            )
        )

        // 4. Spirit Chest / Diamonds
        items.add(
            ShopItem(
                type = ShopItemType.SPIRIT_CHEST,
                name = "天机灵石礼袋 (60灵石)",
                description = "精纯灵石，用于重铸词条与高阶转生修炼",
                goldCost = 8000L + (playerLevel * 700L),
                quantity = 60,
                iconEmoji = "💠"
            )
        )

        return items
    }
}
