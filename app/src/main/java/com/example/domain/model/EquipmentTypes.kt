package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class EquipmentType(val displayName: String, val slotIndex: Int) {
    WEAPON("武器", 0),
    ARMOR("战甲", 1),
    HELMET("头盔", 2),
    SHOES("战靴", 3),
    RING("戒指", 4),
    NECKLACE("项链", 5);

    val allowedGemCategory: GemCategory
        get() = when (this) {
            WEAPON, RING, NECKLACE -> GemCategory.OFFENSIVE
            ARMOR, HELMET, SHOES -> GemCategory.DEFENSIVE
        }
}

enum class EquipmentQuality(
    val displayName: String,
    val colorHex: Long,
    val maxAffixes: Int,
    val statMultiplier: Double,
    val sellPriceMultiplier: Int,
    val meltStoneRate: Int,
    val maxGemSockets: Int
) {
    WHITE("白色", 0xFFE0E0E0, 1, 0.8, 50, 1, 0),
    GREEN("绿色", 0xFF4CAF50, 2, 1.1, 120, 2, 0),
    BLUE("蓝色", 0xFF2196F3, 3, 1.5, 350, 3, 2),
    YELLOW("黄色", 0xFFFFD700, 4, 2.2, 1000, 5, 3),
    RED("红色", 0xFFFF5252, 5, 3.3, 3000, 8, 4),
    GOLD("金色", 0xFFFF9800, 6, 4.8, 8000, 15, 5),
    MYTHIC("神话", 0xFFE040FB, 7, 7.5, 25000, 30, 6);

    val composeColor: Color get() = Color(colorHex)
}

enum class AffixType(
    val displayName: String,
    val isPercentage: Boolean,
    val iconName: String
) {
    ATTACK_FLAT("攻击力", false, "⚔️"),
    ATTACK_PERCENT("攻击提升", true, "⚔️"),
    HP_FLAT("生命值", false, "❤️"),
    HP_PERCENT("生命提升", true, "❤️"),
    DEFENSE_FLAT("防御力", false, "🛡️"),
    DEFENSE_PERCENT("防御提升", true, "🛡️"),
    CRIT_RATE("暴击率", true, "💥"),
    CRIT_DAMAGE("暴击伤害", true, "⚡"),
    HP_REGEN("生命回复", true, "🌿"),
    BLOCK_RATE("格挡率", true, "🧱"),
    DODGE_RATE("闪避率", true, "💨"),
    LIFE_STEAL("生命吸取", true, "🩸"),
    EXP_BONUS("经验加成", true, "✨"),
    GOLD_BONUS("金币加成", true, "💰")
}

data class Affix(
    val type: AffixType,
    val value: Double
) {
    fun formatDisplay(): String {
        return if (type.isPercentage) {
            "${type.displayName} +${String.format("%.1f", value)}%"
        } else {
            "${type.displayName} +${value.toInt()}"
        }
    }
}
