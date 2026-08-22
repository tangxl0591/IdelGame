package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipment",
    indices = [Index("playerId")]
)
data class EquipmentEntity(
    @PrimaryKey val id: String,
    val playerId: String = "",
    val name: String,
    val type: String,
    val quality: String,
    val level: Int,
    val enhanceLevel: Int = 0,
    val baseAttack: Int = 0,
    val baseHp: Int = 0,
    val baseDefense: Int = 0,
    val affixesJson: String = "",
    val gemsJson: String = "",
    val isEquipped: Boolean = false,
    val isLocked: Boolean = false,
    val orderTimestamp: Long = System.currentTimeMillis()
)
