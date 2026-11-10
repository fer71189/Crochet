package com.nexdev.crochet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parts",
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
data class PartEntity(
    @PrimaryKey
    val idPieza: String,
    val patternId: String,
    val orden: Int,
    val nombre: String,
    val cantidadRequerida: Int,
    val notasPieza: String = ""
)
