package com.nexdev.crochet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_projects",
    foreignKeys = [
        ForeignKey(
            entity = PatternEntity::class,
            parentColumns = ["id"],
            childColumns = ["patternId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patternId"])]
)
data class CompletedProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val idHistorial: Long = 0,
    val patternId: String,
    val startedAt: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val notasUsuario: String = ""
)
