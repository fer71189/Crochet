package com.nexdev.crochet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexdev.crochet.data.local.entity.ActiveProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM active_progress ORDER BY lastUpdatedTimestamp DESC LIMIT 1")
    fun observeLatestActiveProgress(): Flow<ActiveProgressEntity?>

    @Query("SELECT * FROM active_progress ORDER BY lastUpdatedTimestamp DESC")
    fun observeAllActiveProgress(): Flow<List<ActiveProgressEntity>>

    @Query("SELECT * FROM active_progress WHERE patternId = :patternId LIMIT 1")
    fun observeProgressForPattern(patternId: String): Flow<ActiveProgressEntity?>

    @Query("SELECT * FROM active_progress WHERE patternId = :patternId LIMIT 1")
    suspend fun getProgressForPattern(patternId: String): ActiveProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ActiveProgressEntity): Long

    @Query("DELETE FROM active_progress WHERE patternId = :patternId")
    suspend fun deleteProgressByPatternId(patternId: String)
}
