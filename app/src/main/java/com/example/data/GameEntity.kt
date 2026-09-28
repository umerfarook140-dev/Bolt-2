package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profile")
data class GameProfileEntity(
    @PrimaryKey val id: Int = 1,
    val currentLevel: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val coins: Int = 150,
    val hammerCount: Int = 3,
    val magnetCount: Int = 3,
    val undoCount: Int = 5,
    val extraSlotCount: Int = 2,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val totalScrewsRemoved: Int = 0,
    val totalLevelsWon: Int = 0
)

@Entity(tableName = "level_records")
data class LevelRecordEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 0, // 0, 1, 2, 3
    val isCompleted: Boolean = false,
    val bestTimeSeconds: Int = 0,
    val completionTimestamp: Long = 0L
)
