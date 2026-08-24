package com.example.domain.model

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToLong

enum class GemCategory(
    val displayName: String,
    val icon: String,
    val allowedSlotsDescription: String
) {
    OFFENSIVE("攻击类", "⚔️", "武器、戒指、项链"),
    DEFENSIVE("防守类", "🛡️", "战甲、头盔、战靴")
}

enum class GemType(
    val id: String,
    val displayName: String,
    val gemName: String,
    val iconEmoji: String,
    val category: GemCategory,
    val colorHex: Long,
    val description: String
) {
    // 攻击类宝石 (仅限武器、戒指、项链)
    RUBY_ATTACK("RUBY_ATTACK", "攻击宝石", "赤焰石", "🔴", GemCategory.OFFENSIVE, 0xFFFF5252, "强化物理基础攻击"),
    SAPPHIRE_MAGIC("SAPPHIRE_MAGIC", "法攻宝石", "星辉石", "🔷", GemCategory.OFFENSIVE, 0xFF448AFF, "强化法术伤害与暴伤"),
    AMETHYST_CRIT("AMETHYST_CRIT", "暴击宝石", "紫晶石", "🟣", GemCategory.OFFENSIVE, 0xFFE040FB, "强化致命一击暴击率"),
    BLOOD_LIFESTEAL("BLOOD_LIFESTEAL", "嗜血宝石", "血精石", "🩸", GemCategory.OFFENSIVE, 0xFFFF1744, "攻击命中时汲取生命"),

    // 防守类宝石 (仅限战甲、头盔、战靴)
    ONYX_DEFENSE("ONYX_DEFENSE", "防守宝石", "玄铁石", "⚫", GemCategory.DEFENSIVE, 0xFF78909C, "强化物理与法术护甲防御"),
    EMERALD_HP("EMERALD_HP", "生命宝石", "青木石", "🟢", GemCategory.DEFENSIVE, 0xFF4CAF50, "增加角色生命值上限"),
    TOPAZ_DODGE("TOPAZ_DODGE", "闪避宝石", "风灵石", "🟡", GemCategory.DEFENSIVE, 0xFFFFD700, "提升身法与躲避攻击几率"),
    DIAMOND_BLOCK("DIAMOND_BLOCK", "格挡宝石", "磐石", "💎", GemCategory.DEFENSIVE, 0xFF00E5FF, "提升磐石格挡与减伤几率");

    val composeColor: Color get() = Color(colorHex)

    companion object {
        fun fromId(id: String): GemType = entries.find { it.id == id || it.name == id } ?: RUBY_ATTACK
    }
}

data class Gem(
    val type: GemType,
    val level: Int = 1 // 1 ~ 10
) {
    val statValue: Double
        get() = when (type) {
            GemType.RUBY_ATTACK -> (level * 22.0 * (1.0 + level * 0.15))
            GemType.SAPPHIRE_MAGIC -> (level * 8.0 + (level * level * 1.5))
            GemType.AMETHYST_CRIT -> (level * 1.5)
            GemType.BLOOD_LIFESTEAL -> (level * 0.9)
            GemType.ONYX_DEFENSE -> (level * 12.0 * (1.0 + level * 0.12))
            GemType.EMERALD_HP -> (level * 150.0 * (1.0 + level * 0.16))
            GemType.TOPAZ_DODGE -> (level * 1.2)
            GemType.DIAMOND_BLOCK -> (level * 1.4)
        }

    val scoreValue: Long
        get() = (level * 150L + (statValue * 4.5).roundToLong())

    fun formatStatBonus(): String {
        return when (type) {
            GemType.RUBY_ATTACK -> "攻击力 +${statValue.toInt()}"
            GemType.SAPPHIRE_MAGIC -> "法术暴伤 +${String.format("%.1f", statValue)}%"
            GemType.AMETHYST_CRIT -> "暴击率 +${String.format("%.1f", statValue)}%"
            GemType.BLOOD_LIFESTEAL -> "生命吸取 +${String.format("%.1f", statValue)}%"
            GemType.ONYX_DEFENSE -> "防御力 +${statValue.toInt()}"
            GemType.EMERALD_HP -> "生命上限 +${statValue.toInt()}"
            GemType.TOPAZ_DODGE -> "闪避率 +${String.format("%.1f", statValue)}%"
            GemType.DIAMOND_BLOCK -> "格挡率 +${String.format("%.1f", statValue)}%"
        }
    }

    val fullName: String get() = "${level}级${type.gemName}"
    val iconEmoji: String get() = type.iconEmoji
    val description: String get() = formatStatBonus()
}

data class GemInventoryItem(
    val type: GemType,
    val level: Int,
    val count: Int
)
