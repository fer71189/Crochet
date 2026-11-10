package com.nexdev.crochet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexdev.crochet.data.local.entity.CompletedProjectCount
import com.nexdev.crochet.data.local.entity.CompletedProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM completed_projects ORDER BY completedAt DESC")
    fun observeAllCompletedProjects(): Flow<List<CompletedProjectEntity>>

    @Query("SELECT patternId, COUNT(*) as count FROM completed_projects GROUP BY patternId ORDER BY count DESC")
    fun observeCompletedCountsByPattern(): Flow<List<CompletedProjectCount>>

    @Query("SELECT COUNT(*) FROM completed_projects")
    suspend fun getCompletedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedProject(completedProject: CompletedProjectEntity)
}
