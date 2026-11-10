package com.nexdev.crochet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = PartEntity::class,
            parentColumns = ["idPieza"],
            childColumns = ["partId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["partId"])]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val idVuelta: Long = 0,
    val partId: String,
    val numero: Int,
    val instruccion: String,
    val puntosTotales: Int,
    val repeticiones: Int = 1
)
