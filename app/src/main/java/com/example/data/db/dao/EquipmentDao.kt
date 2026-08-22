package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment WHERE playerId = :playerId ORDER BY isEquipped DESC, orderTimestamp DESC")
    fun getAllEquipmentFlow(playerId: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE playerId = :playerId AND isEquipped = 1")
    fun getEquippedItemsFlow(playerId: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE playerId = :playerId AND isEquipped = 0 ORDER BY orderTimestamp DESC")
    fun getInventoryItemsFlow(playerId: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE playerId = :playerId AND isEquipped = 1")
    suspend fun getEquippedItemsDirect(playerId: String): List<EquipmentEntity>

    @Query("SELECT * FROM equipment WHERE playerId = :playerId AND isEquipped = 0 ORDER BY orderTimestamp DESC")
    suspend fun getInventoryItemsDirect(playerId: String): List<EquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: EquipmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(equipmentList: List<EquipmentEntity>)

    @Update
    suspend fun update(equipment: EquipmentEntity)

    @Delete
    suspend fun delete(equipment: EquipmentEntity)

    @Query("DELETE FROM equipment WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM equipment WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM equipment WHERE playerId = :playerId")
    suspend fun deleteAllByPlayerId(playerId: String)
}
