package com.nexdev.crochet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "active_progress",
    foreignKeys = [
        ForeignKey(
            entity = PatternEntity::class,
            parentColumns = ["id"],
            childColumns = ["patternId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patternId"], unique = true)]
)
data class ActiveProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val idSesion: Long = 0,
    val patternId: String,
    val currentPartId: String,
    val currentPartInstance: Int = 1,
    val currentRoundNumber: Int = 1,
    val currentRepetitionCount: Int = 0,
    val completedPartsMask: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
