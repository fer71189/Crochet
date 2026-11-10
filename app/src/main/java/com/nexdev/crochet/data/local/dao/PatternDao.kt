package com.nexdev.crochet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nexdev.crochet.data.local.entity.PartEntity
import com.nexdev.crochet.data.local.entity.PatternEntity
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import com.nexdev.crochet.data.local.entity.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Transaction
    @Query("SELECT * FROM patterns")
    fun observeAllPatternsWithDetails(): Flow<List<PatternWithPartsAndRounds>>

    @Transaction
    @Query("SELECT * FROM patterns WHERE id = :patternId LIMIT 1")
    fun observePatternById(patternId: String): Flow<PatternWithPartsAndRounds?>

    @Transaction
    @Query("SELECT * FROM patterns WHERE id = :patternId LIMIT 1")
    suspend fun getPatternById(patternId: String): PatternWithPartsAndRounds?

    @Query("SELECT COUNT(*) FROM patterns")
    suspend fun getPatternCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<PatternEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: PatternEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParts(parts: List<PartEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRounds(rounds: List<RoundEntity>)

    @Query("DELETE FROM parts WHERE patternId = :patternId")
    suspend fun deletePartsByPatternId(patternId: String)

    @Transaction
    suspend fun insertFullPattern(
        pattern: PatternEntity,
        parts: List<PartEntity>,
        rounds: List<RoundEntity>
    ) {
        insertPattern(pattern)
        insertParts(parts)
        insertRounds(rounds)
    }

    @Transaction
    suspend fun updateFullPattern(
        pattern: PatternEntity,
        parts: List<PartEntity>,
        rounds: List<RoundEntity>
    ) {
        insertPattern(pattern)
        deletePartsByPatternId(pattern.id)
        insertParts(parts)
        insertRounds(rounds)
    }
}
