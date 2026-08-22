package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String = "修仙剑客",
    val characterClass: String = "WARRIOR",
    val level: Int = 1,
    val currentExp: Long = 0L,
    val gold: Long = 1000L,
    val diamonds: Long = 50L,
    val enhanceStones: Int = 15,
    val reincarnationCount: Int = 0,
    val reincarnationPoints: Int = 0,
    val attackTalentLevel: Int = 0,
    val hpTalentLevel: Int = 0,
    val defenseTalentLevel: Int = 0,
    val critTalentLevel: Int = 0,
    val lifestealTalentLevel: Int = 0,
    val luckyTalentLevel: Int = 0,
    val goldExpTalentLevel: Int = 0,
    val currentDungeonStage: Int = 1,
    val maxDungeonStage: Int = 1,
    val endlessTowerFloor: Int = 1,
    val maxEndlessTowerFloor: Int = 1,
    val autoChallenge: Boolean = true,
    val autoSellQuality: String? = null,
    val battleSpeed: Double = 1.0,
    val gemsInventoryJson: String = "",
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
