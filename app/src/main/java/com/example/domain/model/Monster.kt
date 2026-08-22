package com.example.domain.model

import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.random.Random

data class Monster(
    val id: String,
    val name: String,
    val level: Int,
    val maxHp: Long,
    var currentHp: Long,
    val minAttack: Long,
    val maxAttack: Long,
    val defense: Long,
    val critRate: Double = 5.0,
    val critDamage: Double = 150.0,
    val dodgeRate: Double = 3.0,
    val blockRate: Double = 2.0,
    val isBoss: Boolean = false,
    val expReward: Long,
    val goldReward: Long,
    val avatarEmoji: String = "👾"
)

object MonsterGenerator {
    private val normalMonsterTypes = listOf(
        "野狼" to "🐺",
        "狂暴野猪" to "🐗",
        "哥布林勇士" to "👺",
        "骷髅射手" to "💀",
        "幽灵暗刺" to "👻",
        "剧毒毒蛛" to "🕷️",
        "熔岩幼龙" to "🦎",
        "石像鬼守卫" to "🗿",
        "暗黑狂战傀儡" to "🗡️",
        "寒霜妖灵" to "❄️",
        "深渊噬魂怪" to "👿",
        "混沌巨兽" to "🧌",
        "天庭天兵" to "⚔️",
        "创世神仆" to "✨"
    )

    private val bossMonsterTypes = listOf(
        "嗜血狂狼王" to "🐺",
        "熔岩巨蝎领主" to "🦂",
        "远古不朽石皇" to "🗿",
        "极寒冰原巨龙" to "🐲",
        "太古龙王至尊" to "🐉",
        "虚空大魔神" to "🌌",
        "堕落炽天使长" to "🪽",
        "混沌毁灭魔尊" to "👹",
        "九霄天帝法身" to "🏯",
        "创世源初至尊" to "👑"
    )

    fun createMonster(dungeonLevel: Int, isBoss: Boolean = false): Monster {
        val lvl = dungeonLevel.coerceIn(1, 2000)
        val baseScale = lvl.toDouble().pow(1.28)

        val typeList = if (isBoss) bossMonsterTypes else normalMonsterTypes
        val index = if (isBoss) {
            val chapterIdx = ((lvl - 1) / 200).coerceIn(0, bossMonsterTypes.size - 1)
            chapterIdx
        } else {
            ((lvl / 12) % typeList.size)
        }
        val (baseName, emoji) = typeList[index]
        val prefix = if (isBoss) "【关底领主】" else ""
        val name = "$prefix$baseName (Lv.$lvl)"

        val hpMultiplier = if (isBoss) 6.5 else 1.0
        val atkMultiplier = if (isBoss) 2.4 else 1.0
        val defMultiplier = if (isBoss) 1.8 else 1.0

        val maxHp = ((100.0 + baseScale * 45.0) * hpMultiplier).roundToLong().coerceAtLeast(50L)
        val baseAtk = ((12.0 + baseScale * 7.5) * atkMultiplier).roundToLong().coerceAtLeast(5L)
        val minAtk = (baseAtk * 0.85).roundToLong().coerceAtLeast(1L)
        val maxAtk = (baseAtk * 1.15).roundToLong().coerceAtLeast(minAtk)
        val defense = ((4.0 + baseScale * 2.2) * defMultiplier).roundToLong().coerceAtLeast(1L)

        val exp = ((25.0 + baseScale * 14.0) * (if (isBoss) 5.0 else 1.0)).roundToLong()
        val gold = ((15.0 + baseScale * 10.0) * (if (isBoss) 6.0 else 1.0)).roundToLong()

        return Monster(
            id = "mon_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            name = name,
            level = lvl,
            maxHp = maxHp,
            currentHp = maxHp,
            minAttack = minAtk,
            maxAttack = maxAtk,
            defense = defense,
            critRate = if (isBoss) 12.0 else 5.0,
            critDamage = if (isBoss) 180.0 else 150.0,
            dodgeRate = if (isBoss) 6.0 else 2.0,
            blockRate = if (isBoss) 8.0 else 2.0,
            isBoss = isBoss,
            expReward = exp,
            goldReward = gold,
            avatarEmoji = emoji
        )
    }
}
