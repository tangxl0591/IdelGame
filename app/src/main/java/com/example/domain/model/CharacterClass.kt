package com.example.domain.model

enum class CharacterClass(
    val id: String,
    val displayName: String,
    val title: String,
    val iconEmoji: String,
    val description: String,
    val combatStyle: String,
    val weaponCategory: String,
    val hpScale: Double,
    val defScale: Double,
    val atkScale: Double,
    val critRateBonus: Double,
    val critDmgBonus: Double,
    val blockBonus: Double,
    val dodgeBonus: Double,
    val lifeStealBonus: Double,
    val attackSpeedBonus: Double
) {
    WARRIOR(
        id = "WARRIOR",
        displayName = "战士",
        title = "狂暴战神",
        iconEmoji = "🛡️",
        description = "拥有坚不可摧的体魄与无坚不摧的近战重击，格挡与生命成长极高，是战场上的铜墙铁壁。",
        combatStyle = "近战物理攻击 · 巨力格挡",
        weaponCategory = "巨剑 / 战斧 / 战刃",
        hpScale = 1.35,
        defScale = 1.35,
        atkScale = 1.05,
        critRateBonus = 0.0,
        critDmgBonus = 0.0,
        blockBonus = 12.0,
        dodgeBonus = 0.0,
        lifeStealBonus = 0.0,
        attackSpeedBonus = 1.0
    ),
    MAGE(
        id = "MAGE",
        displayName = "法师",
        title = "元素法皇",
        iconEmoji = "🔮",
        description = "沟通天地奥术与烈焰雷霆，拥有毁天灭地的法术爆发伤害与强力法力吸血。",
        combatStyle = "远程法术攻击 · 禁咒吸血",
        weaponCategory = "法杖 / 法球 / 秘典",
        hpScale = 0.85,
        defScale = 0.80,
        atkScale = 1.45,
        critRateBonus = 5.0,
        critDmgBonus = 40.0,
        blockBonus = 0.0,
        dodgeBonus = 3.0,
        lifeStealBonus = 8.0,
        attackSpeedBonus = 1.05
    ),
    ARCHER(
        id = "ARCHER",
        displayName = "弓箭手",
        title = "追风神射",
        iconEmoji = "🏹",
        description = "身法如风，百步穿杨。拥有极致的敏捷身法、高频连击与致命暴击，极难被敌人命中。",
        combatStyle = "敏捷物理穿透 · 灵动闪避",
        weaponCategory = "长弓 / 强弩 / 连弩",
        hpScale = 0.95,
        defScale = 0.90,
        atkScale = 1.20,
        critRateBonus = 10.0,
        critDmgBonus = 20.0,
        blockBonus = 0.0,
        dodgeBonus = 15.0,
        lifeStealBonus = 2.0,
        attackSpeedBonus = 1.25
    );

    companion object {
        fun fromId(id: String?): CharacterClass {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: WARRIOR
        }
    }
}
