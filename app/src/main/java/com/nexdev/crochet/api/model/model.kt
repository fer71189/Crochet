package com.nexdev.crochet.api.model

import com.google.gson.annotations.SerializedName

data class CatalogoResponse(
    @SerializedName("version_catalogo") val versionCatalogo: Int,
    val patrones: List<Patron>
)

data class Patron(
    val id: String,
    val nombre: String,
    val icono: String,
    @SerializedName("color_bg") val colorBg: String = "#F4E6DD",
    val categoria: String,
    val dificultad: String,
    @SerializedName("tiempo_estimado_min") val tiempoEstimadoMin: Int,
    @SerializedName("hilo_recomendado") val hiloRecomendado: String,
    @SerializedName("aguja_mm") val agujaMm: Double,
    val extras: List<String> = emptyList(),
    @SerializedName("notas_ensamblaje") val notasEnsamblaje: String = "",
    val piezas: List<Pieza> = emptyList()
)

data class Pieza(
    @SerializedName("id_pieza") val idPieza: String,
    val nombre: String,
    @SerializedName("cantidad_requerida") val cantidadRequerida: Int,
    @SerializedName("notas_pieza") val notasPieza: String = "",
    val vueltas: List<Vuelta> = emptyList()
)

data class Vuelta(
    val numero: Int,
    val instruccion: String,
    @SerializedName("puntos_totales") val puntosTotales: Int,
    val repeticiones: Int = 1
)
