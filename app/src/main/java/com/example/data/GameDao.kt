package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_profile WHERE id = 1 LIMIT 1")
    fun getGameProfile(): Flow<GameProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameProfile(profile: GameProfileEntity)

    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    fun getAllLevelRecords(): Flow<List<LevelRecordEntity>>

    @Query("SELECT * FROM level_records WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getLevelRecord(levelNumber: Int): LevelRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelRecord(record: LevelRecordEntity)

    @Query("UPDATE game_profile SET highestUnlockedLevel = :level WHERE id = 1 AND highestUnlockedLevel < :level")
    suspend fun unlockUpToLevel(level: Int)
}
