package com.example.domain.model

enum class DamageType {
    NORMAL,
    CRIT,
    HEAL,
    DODGE,
    BLOCK
}

data class FloatingDamage(
    val id: Long = System.currentTimeMillis() + (0..9999).random(),
    val text: String,
    val type: DamageType,
    val isMonsterTarget: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class BattleLogEntry(
    val id: Long = System.currentTimeMillis() + (0..9999).random(),
    val message: String,
    val type: LogType = LogType.INFO,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogType {
    INFO,
    PLAYER_ATTACK,
    MONSTER_ATTACK,
    VICTORY,
    DEFEAT,
    LOOT,
    LEVEL_UP
}
