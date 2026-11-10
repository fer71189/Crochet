package com.nexdev.crochet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patterns")
data class PatternEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val icono: String = "🧶",
    val colorBg: String = "#F4E6DD",
    val categoria: String,
    val dificultad: String,
    val tiempoEstimadoMin: Int,
    val hiloRecomendado: String,
    val agujaMm: Double,
    val extrasJson: String = "[]",
    val notasEnsamblaje: String = "",
    val esPersonalizado: Boolean = false
)
