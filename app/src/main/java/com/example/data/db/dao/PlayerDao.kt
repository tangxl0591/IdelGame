package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.db.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player WHERE isActive = 1 LIMIT 1")
    fun getActivePlayerFlow(): Flow<PlayerEntity?>

    @Query("SELECT * FROM player WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlayerDirect(): PlayerEntity?

    @Query("SELECT * FROM player ORDER BY createdAt DESC")
    fun getAllPlayersFlow(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM player ORDER BY createdAt DESC")
    suspend fun getAllPlayersDirect(): List<PlayerEntity>

    @Query("SELECT * FROM player WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(player: PlayerEntity)

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("UPDATE player SET isActive = CASE WHEN id = :activeId THEN 1 ELSE 0 END")
    suspend fun setActivePlayer(activeId: String)

    @Query("DELETE FROM player WHERE id = :id")
    suspend fun deletePlayer(id: String)
}
