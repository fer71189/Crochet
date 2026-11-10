package com.nexdev.crochet.data.local.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeedCatalogDto(
    @SerialName("version_catalogo")
    val versionCatalogo: Int = 1,
    val patrones: List<SeedPatternDto> = emptyList()
)

@Serializable
data class SeedPatternDto(
    val id: String,
    val nombre: String,
    val icono: String = "🧶",
    @SerialName("color_bg")
    val colorBg: String = "#F4E6DD",
    val categoria: String,
    val dificultad: String,
    @SerialName("tiempo_estimado_min")
    val tiempoEstimadoMin: Int,
    @SerialName("hilo_recomendado")
    val hiloRecomendado: String,
    @SerialName("aguja_mm")
    val agujaMm: Double,
    val extras: List<String> = emptyList(),
    @SerialName("notas_ensamblaje")
    val notasEnsamblaje: String = "",
    val piezas: List<SeedPartDto> = emptyList()
)

@Serializable
data class SeedPartDto(
    @SerialName("id_pieza")
    val idPieza: String,
    val nombre: String,
    @SerialName("cantidad_requerida")
    val cantidadRequerida: Int,
    @SerialName("notas_pieza")
    val notasPieza: String = "",
    val vueltas: List<SeedRoundDto> = emptyList()
)

@Serializable
data class SeedRoundDto(
    val numero: Int,
    val instruccion: String,
    @SerialName("puntos_totales")
    val puntosTotales: Int,
    val repeticiones: Int = 1
)
