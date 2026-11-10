package com.nexdev.crochet.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import com.nexdev.crochet.ui.theme.DangerRed
import com.nexdev.crochet.ui.theme.DangerRedBg
import com.nexdev.crochet.ui.theme.EspressoDark
import com.nexdev.crochet.ui.theme.SageSuccess
import com.nexdev.crochet.ui.theme.SageSuccessBg
import com.nexdev.crochet.ui.theme.SageSuccessText
import com.nexdev.crochet.ui.theme.TerracottaPrimary
import com.nexdev.crochet.ui.theme.TextSecondaryBrown
import com.nexdev.crochet.ui.theme.TextSubtleTaupe
import com.nexdev.crochet.ui.theme.WarmAccentSurface
import com.nexdev.crochet.ui.theme.WarmBorder
import com.nexdev.crochet.ui.theme.WarmCreamBg
import com.nexdev.crochet.ui.theme.WarmSurfaceSubtle
import com.nexdev.crochet.ui.viewmodel.CustomPartDraft
import com.nexdev.crochet.ui.viewmodel.CustomRoundDraft

private class EditableRoundState(
    instruccion: String,
    puntosTotales: String,
    repeticiones: String = "1"
) {
    var instruccion by mutableStateOf(instruccion)
    var puntosTotales by mutableStateOf(puntosTotales)
    var repeticiones by mutableStateOf(repeticiones)
}

private class EditablePartState(
    nombre: String,
    cantidadRequerida: String = "1",
    notasPieza: String = "",
    initialRounds: List<EditableRoundState>
) {
    var nombre by mutableStateOf(nombre)
    var cantidadRequerida by mutableStateOf(cantidadRequerida)
    var notasPieza by mutableStateOf(notasPieza)
    val vueltas = mutableStateListOf<EditableRoundState>().apply {
        addAll(initialRounds)
    }
}

@Composable
fun CreatePatternScreen(
    patternToEdit: PatternWithPartsAndRounds? = null,
    onSavePattern: (
        patternId: String?,
        nombre: String,
        categoria: String,
        agujaMm: Double,
        hiloRecomendado: String,
        icono: String,
        notasEnsamblaje: String,
        partsDraft: List<CustomPartDraft>
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditMode = patternToEdit != null

    var nombre by remember(patternToEdit) {
        mutableStateOf(patternToEdit?.pattern?.nombre ?: "")
    }
    var selectedCategory by remember(patternToEdit) {
        mutableStateOf(patternToEdit?.pattern?.categoria ?: "Apego")
    }
    var selectedIcon by remember(patternToEdit) {
        mutableStateOf(patternToEdit?.pattern?.icono ?: "🧶")
    }
    var agujaText by remember(patternToEdit) {
        mutableStateOf(patternToEdit?.pattern?.agujaMm?.toString() ?: "3.0")
    }
    var hiloText by remember(patternToEdit) {
        mutableStateOf(patternToEdit?.pattern?.hiloRecomendado ?: "Algodón grosor medio")
    }
    var notasEnsamblaje by remember(patternToEdit) {
        mutableStateOf(patternToEdit?.pattern?.notasEnsamblaje ?: "")
    }

    val availableCategories = listOf("Apego", "Animales", "Personajes", "Recuerdos")
    val availableIcons = listOf("🧶", "🐰", "🧸", "🐙", "🐸", "👼", "🦄", "🌸", "🦖")

    val parts = remember(patternToEdit) {
        mutableStateListOf<EditablePartState>().apply {
            if (patternToEdit != null && patternToEdit.sortedParts.isNotEmpty()) {
                patternToEdit.sortedParts.forEach { partWithRounds ->
                    add(
                        EditablePartState(
                            nombre = partWithRounds.part.nombre,
                            cantidadRequerida = partWithRounds.part.cantidadRequerida.toString(),
                            notasPieza = partWithRounds.part.notasPieza,
                            initialRounds = partWithRounds.sortedRounds.map { round ->
                                EditableRoundState(
                                    instruccion = round.instruccion,
                                    puntosTotales = round.puntosTotales.toString(),
                                    repeticiones = round.repeticiones.toString()
                                )
                            }
                        )
                    )
                }
            } else {
                add(
                    EditablePartState(
                        nombre = "Cuerpo Base",
                        cantidadRequerida = "1",
                        notasPieza = "Patrón creado por el usuario.",
                        initialRounds = listOf(
                            EditableRoundState("Anillo mágico 6 pb", "6", "1"),
                            EditableRoundState("6 aumentos", "12", "6"),
                            EditableRoundState("(1 pb, 1 aum) x 6", "18", "6")
                        )
                    )
                )
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TerracottaPrimary,
        unfocusedBorderColor = WarmBorder,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = EspressoDark,
        unfocusedTextColor = EspressoDark
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCreamBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = WarmAccentSurface
                ) {
                    Text(
                        text = if (isEditMode) "CATÁLOGO • EDITAR PATRÓN" else "NUEVO PROYECTO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TerracottaPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEditMode) "Editar Patrón" else "Nuevo Patrón",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EspressoDark
                )
                Text(
                    text = if (isEditMode) "Modifica las piezas, vueltas o materiales de este diseño."
                    else "Agrega tu propio diseño indicando piezas, vueltas y puntos.",
                    fontSize = 12.sp,
                    color = TextSubtleTaupe
                )
            }

            Surface(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Tarjeta Informativa sobre el Tiempo Automático
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SageSuccessBg,
            border = BorderStroke(1.dp, SageSuccess.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⏱️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "El tiempo de tejido no se ingresa a mano: la app cronometrará automáticamente desde que inicias el proyecto hasta que presionas 'Finalizar'.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SageSuccessText,
                    lineHeight = 16.sp
                )
            }
        }

        // Tarjeta 1: Datos Generales del Proyecto
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nombre del Proyecto",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Ej. Conejito Dormilón", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Icono
                Text(
                    text = "Icono del Proyecto",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableIcons) { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) TerracottaPrimary else WarmSurfaceSubtle)
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                        }
                    }
                }

                // Selector de Categoría
                Text(
                    text = "Categoría",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableCategories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = CircleShape,
                            color = if (isSelected) EspressoDark else WarmSurfaceSubtle,
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondaryBrown,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Aguja Sugerida
                Text(
                    text = "Aguja Recomendada (mm)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown
                )
                OutlinedTextField(
                    value = agujaText,
                    onValueChange = { agujaText = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Hilo Recomendado
                Text(
                    text = "Hilo Recomendado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown
                )
                OutlinedTextField(
                    value = hiloText,
                    onValueChange = { hiloText = it },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notas de Ensamblaje
                Text(
                    text = "Notas de Ensamblaje / Detalles (Opcional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown
                )
                OutlinedTextField(
                    value = notasEnsamblaje,
                    onValueChange = { notasEnsamblaje = it },
                    placeholder = { Text("Ej. Coser las orejas entre vueltas 8 y 9...", fontSize = 12.sp) },
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tarjetas Dinámicas de Piezas (N piezas, M vueltas)
        parts.forEachIndexed { pIndex, partState ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PIEZA #${pIndex + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TerracottaPrimary
                        )

                        if (parts.size > 1) {
                            Surface(
                                onClick = { parts.removeAt(pIndex) },
                                shape = RoundedCornerShape(8.dp),
                                color = DangerRedBg
                            ) {
                                Text(
                                    text = "Eliminar pieza",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Vueltas configurables",
                                fontSize = 11.sp,
                                color = TextSubtleTaupe
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(
                                text = "Nombre de Pieza",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondaryBrown
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = partState.nombre,
                                onValueChange = { partState.nombre = it },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cantidad",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondaryBrown
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = partState.cantidadRequerida,
                                onValueChange = { partState.cantidadRequerida = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Vueltas de la pieza
                    Text(
                        text = "Vueltas (Instrucción | Puntos | Reps)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryBrown
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = WarmSurfaceSubtle,
                        border = BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            partState.vueltas.forEachIndexed { rIndex, roundState ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "V${rIndex + 1}:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TerracottaPrimary,
                                        modifier = Modifier.width(28.dp)
                                    )

                                    OutlinedTextField(
                                        value = roundState.instruccion,
                                        onValueChange = { roundState.instruccion = it },
                                        placeholder = { Text("Instrucción", fontSize = 11.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = textFieldColors,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = roundState.puntosTotales,
                                        onValueChange = { roundState.puntosTotales = it },
                                        placeholder = { Text("Pts", fontSize = 11.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = textFieldColors,
                                        modifier = Modifier.width(62.dp)
                                    )

                                    OutlinedTextField(
                                        value = roundState.repeticiones,
                                        onValueChange = { roundState.repeticiones = it },
                                        placeholder = { Text("Rep", fontSize = 11.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = textFieldColors,
                                        modifier = Modifier.width(54.dp)
                                    )

                                    if (partState.vueltas.size > 1) {
                                        Surface(
                                            onClick = { partState.vueltas.removeAt(rIndex) },
                                            shape = CircleShape,
                                            color = DangerRedBg,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "×",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DangerRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val nextRoundNum = partState.vueltas.size + 1
                                    partState.vueltas.add(
                                        EditableRoundState(
                                            instruccion = "Tejer pb alrededor (V$nextRoundNum)",
                                            puntosTotales = "18",
                                            repeticiones = "1"
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, WarmBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = TerracottaPrimary
                                )
                            ) {
                                Text(
                                    text = "+ Agregar Vuelta a ${partState.nombre.ifBlank { "Pieza" }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Botón para agregar otra pieza al amigurumi
        OutlinedButton(
            onClick = {
                parts.add(
                    EditablePartState(
                        nombre = "Pieza ${parts.size + 1}",
                        cantidadRequerida = "2",
                        notasPieza = "",
                        initialRounds = listOf(
                            EditableRoundState("Anillo mágico 6 pb", "6", "1"),
                            EditableRoundState("6 pb alrededor", "6", "1")
                        )
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, TerracottaPrimary),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = TerracottaPrimary
            )
        ) {
            Text(
                text = "🧩 + Agregar Otra Pieza al Patrón",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Botón Principal Guardar Patrón en Room
        Button(
            onClick = {
                if (nombre.isBlank()) {
                    Toast.makeText(
                        context,
                        "Ingresa un nombre para tu proyecto",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val agujaMm = agujaText.toDoubleOrNull() ?: 3.0
                val drafts = parts.map { pState ->
                    CustomPartDraft(
                        nombre = pState.nombre.ifBlank { "Pieza" },
                        cantidadRequerida = pState.cantidadRequerida.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        notasPieza = pState.notasPieza,
                        vueltas = pState.vueltas.mapIndexed { idx, rState ->
                            CustomRoundDraft(
                                numero = idx + 1,
                                instruccion = rState.instruccion.ifBlank { "Tejer vuelta ${idx + 1}" },
                                puntosTotales = rState.puntosTotales.toIntOrNull()?.coerceAtLeast(1) ?: 6,
                                repeticiones = rState.repeticiones.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            )
                        }
                    )
                }

                onSavePattern(
                    patternToEdit?.pattern?.id,
                    nombre,
                    selectedCategory,
                    agujaMm,
                    hiloText.ifBlank { "Algodón grosor medio" },
                    selectedIcon,
                    notasEnsamblaje,
                    drafts
                )
                Toast.makeText(
                    context,
                    if (isEditMode) "✨ Cambios guardados en el patrón" else "✨ Patrón guardado en el catálogo",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SageSuccess,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = if (isEditMode) "💾 Guardar Cambios del Patrón" else "💾 Guardar Patrón en el Catálogo",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
