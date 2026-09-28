package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val gameProfile: Flow<GameProfileEntity?> = gameDao.getGameProfile()
    val levelRecords: Flow<List<LevelRecordEntity>> = gameDao.getAllLevelRecords()

    suspend fun saveProfile(profile: GameProfileEntity) {
        gameDao.saveGameProfile(profile)
    }

    suspend fun getLevelRecord(levelNumber: Int): LevelRecordEntity? {
        return gameDao.getLevelRecord(levelNumber)
    }

    suspend fun recordLevelCompleted(levelNumber: Int, stars: Int, timeSeconds: Int) {
        val existing = gameDao.getLevelRecord(levelNumber)
        val bestStars = maxOf(existing?.stars ?: 0, stars)
        val bestTime = if (existing != null && existing.bestTimeSeconds > 0) {
            minOf(existing.bestTimeSeconds, timeSeconds)
        } else {
            timeSeconds
        }
        gameDao.saveLevelRecord(
            LevelRecordEntity(
                levelNumber = levelNumber,
                stars = bestStars,
                isCompleted = true,
                bestTimeSeconds = bestTime,
                completionTimestamp = System.currentTimeMillis()
            )
        )
        gameDao.unlockUpToLevel(levelNumber + 1)
    }

    suspend fun updateBoosters(
        hammer: Int,
        magnet: Int,
        undo: Int,
        extraSlot: Int,
        coins: Int
    ) {
        // Handled in profile update
    }
}
