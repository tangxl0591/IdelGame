package com.example.domain.model

data class OfflineGainSummary(
    val offlineSeconds: Long,
    val monstersDefeated: Int,
    val expGained: Long,
    val goldGained: Long,
    val enhanceStonesGained: Int,
    val itemsGained: List<Equipment>,
    val autoSoldItemsCount: Int,
    val autoSoldGoldGained: Long,
    val levelUps: Int
)
