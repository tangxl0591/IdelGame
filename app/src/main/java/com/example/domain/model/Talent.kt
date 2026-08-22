package com.example.domain.model

enum class TalentType(
    val title: String,
    val description: String,
    val costPerLevel: Int,
    val iconEmoji: String,
    val maxLevel: Int = 100
) {
    ATTACK_BOOST("破军斩", "提升角色基础攻击力 +5%", 1, "⚔️"),
    HP_BOOST("不灭躯", "提升角色基础生命值 +6%", 1, "❤️"),
    DEFENSE_BOOST("玄武甲", "提升角色基础防御力 +5%", 1, "🛡️"),
    CRIT_MASTERY("天罚心眼", "提升暴击率 +0.8% 与暴击伤害 +5%", 2, "💥"),
    LIFESTEAL_MASTERY("嗜血狂宴", "提升吸血率 +0.6%", 2, "🩸"),
    LUCKY_LOOT("鸿运齐天", "极品装备掉落概率提升 +10%", 3, "✨"),
    GOLD_EXP_MASTERY("纳宝心决", "战斗金币与经验收益 +8%", 2, "💰")
}

data class TalentData(
    val attackLevel: Int = 0,
    val hpLevel: Int = 0,
    val defenseLevel: Int = 0,
    val critLevel: Int = 0,
    val lifestealLevel: Int = 0,
    val luckyLevel: Int = 0,
    val goldExpLevel: Int = 0
) {
    fun getLevel(type: TalentType): Int = when (type) {
        TalentType.ATTACK_BOOST -> attackLevel
        TalentType.HP_BOOST -> hpLevel
        TalentType.DEFENSE_BOOST -> defenseLevel
        TalentType.CRIT_MASTERY -> critLevel
        TalentType.LIFESTEAL_MASTERY -> lifestealLevel
        TalentType.LUCKY_LOOT -> luckyLevel
        TalentType.GOLD_EXP_MASTERY -> goldExpLevel
    }

    fun withUpgrade(type: TalentType): TalentData = when (type) {
        TalentType.ATTACK_BOOST -> copy(attackLevel = attackLevel + 1)
        TalentType.HP_BOOST -> copy(hpLevel = hpLevel + 1)
        TalentType.DEFENSE_BOOST -> copy(defenseLevel = defenseLevel + 1)
        TalentType.CRIT_MASTERY -> copy(critLevel = critLevel + 1)
        TalentType.LIFESTEAL_MASTERY -> copy(lifestealLevel = lifestealLevel + 1)
        TalentType.LUCKY_LOOT -> copy(luckyLevel = luckyLevel + 1)
        TalentType.GOLD_EXP_MASTERY -> copy(goldExpLevel = goldExpLevel + 1)
    }

    val totalSpentPoints: Int
        get() = (attackLevel * TalentType.ATTACK_BOOST.costPerLevel) +
                (hpLevel * TalentType.HP_BOOST.costPerLevel) +
                (defenseLevel * TalentType.DEFENSE_BOOST.costPerLevel) +
                (critLevel * TalentType.CRIT_MASTERY.costPerLevel) +
                (lifestealLevel * TalentType.LIFESTEAL_MASTERY.costPerLevel) +
                (luckyLevel * TalentType.LUCKY_LOOT.costPerLevel) +
                (goldExpLevel * TalentType.GOLD_EXP_MASTERY.costPerLevel)
}
