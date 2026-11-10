# Crochet Funny - Documento de Especificación y Guía para Agente IA

## 1. Visión General del Proyecto
**Nombre de la App:** Crochet Funny  
**Plataforma:** Android Nativo (Android Studio)  
**Objetivo:** Aplicación móvil para la gestión interactiva y seguimiento paso a paso de proyectos de crochet (amigurumis). Permite explorar patrones precargados, visualizar requisitos técnicos, llevar el control exacto de piezas, repeticiones y vueltas con persistencia automática en tiempo real, registrar estadísticas de proyectos finalizados y, en una fase posterior, crear patrones propios desde el dispositivo.

---

## 2. Stack Tecnológico Recomendado (Instrucciones para el Agente)
Para garantizar escalabilidad, código limpio y facilidad de mantenimiento, el agente de desarrollo debe apegarse a la siguiente arquitectura:
* **Lenguaje:** Kotlin.
* **UI Framework:** Jetpack Compose (Material Design 3) con paleta de colores cálida/pastel.
* **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture ligera (Capa de Datos y Capa de UI).
* **Base de Datos Local:** Room Database (SQLite) para tanto el catálogo como el progreso del usuario.
* **Serialización JSON:** `kotlinx.serialization` o `Gson` para la carga inicial (seed) desde `assets/patterns_seed.json`.
* **Navegación:** Jetpack Navigation Compose (Single Activity Architecture).
* **Asincronía y Estado:** Kotlin Coroutines y `StateFlow`.

---

## 3. Reglas de Negocio y UX Críticas
1. **Persistencia Inmediata:** Cada vez que el usuario avance/retroceda una vuelta, cambie de pieza o incremente el contador de repeticiones, el cambio debe guardarse inmediatamente en Room. Si la app se cierra, entra en suspensión o el teléfono se reinicia, al volver a entrar debe retomar exactamente donde se quedó.
2. **Multiplicidad de Piezas:** Muchos patrones tienen piezas repetidas (ej. "Tentáculos x8" o "Brazos x2"). El modelo de datos NO debe duplicar las vueltas en el JSON; debe usar `cantidad_requerida: 8` y el rastreador de progreso debe indicar: *"Tentáculo (Pieza 3 de 8) - Vuelta 5"*.
3. **Pantalla Siempre Encendida (`Keep Screen On`):** Exclusivamente en la pantalla de **Modo Tejido (Seguimiento Activo)**, se debe activar el flag `FLAG_KEEP_SCREEN_ON` en la ventana actual para evitar que el teléfono se bloquee mientras el usuario tiene las manos ocupadas tejiendo.
4. **Múltiples Proyectos en Curso (WIPs):** La base de datos debe permitir pausar un proyecto e iniciar otro sin perder el progreso del primero, mostrando en el inicio el último proyecto activo.
5. **Contador Secundario de Repeticiones:** Dentro de la vuelta activa (ej. `(1 pb, 1 aum) x 6`), el usuario debe tener un sub-contador rápido (`0 / 6`) para no perder la cuenta de las repeticiones dentro de la misma vuelta.

---

## 4. Estructura del Modelo de Datos

### 4.1 Estructura del JSON Semilla (`assets/patterns_seed.json`)
Al iniciar la app por primera vez (`RoomDatabase.Callback`), se debe leer este JSON e insertarlo en las tablas de Room:

```json
{
  "version_catalogo": 1,
  "patrones": [
    {
      "id": "pat_pulpo_01",
      "nombre": "Pulpo de Apego",
      "categoria": "Apego",
      "dificultad": "Básico",
      "tiempo_estimado_min": 180,
      "hilo_recomendado": "Algodón peinado 100% (Grosor Medio / 4 ply)",
      "aguja_mm": 2.5,
      "extras": ["Ojos de seguridad 8mm", "Relleno sintético", "Marcador de puntos"],
      "notas_ensamblaje": "Coser los 8 tentáculos en la base entre las vueltas 14 y 15. Bordar detalles con hilo negro si es para recién nacido.",
      "piezas": [
        {
          "id_pieza": "pulpo_p1",
          "nombre": "Cabeza y Cuerpo",
          "cantidad_requerida": 1,
          "notas_pieza": "Rellenar firmemente antes de cerrar la vuelta 16.",
          "vueltas": [
            { "numero": 1, "instruccion": "Anillo mágico de 6 pb", "puntos_totales": 6, "repeticiones": 1 },
            { "numero": 2, "instruccion": "6 aum", "puntos_totales": 12, "repeticiones": 6 },
            { "numero": 3, "instruccion": "(1 pb, 1 aum) x 6", "puntos_totales": 18, "repeticiones": 6 },
            { "numero": 4, "instruccion": "(2 pb, 1 aum) x 6", "puntos_totales": 24, "repeticiones": 6 }
          ]
        },
        {
          "id_pieza": "pulpo_p2",
          "nombre": "Tentáculos",
          "cantidad_requerida": 8,
          "notas_pieza": "No requiere relleno. Se enrollan solos al tejer.",
          "vueltas": [
            { "numero": 1, "instruccion": "Tejer 40 cadenas al aire", "puntos_totales": 40, "repeticiones": 1 },
            { "numero": 2, "instruccion": "Desde la 2da cad, hacer 2 pb en cada cadena (x39)", "puntos_totales": 78, "repeticiones": 39 }
          ]
        }
      ]
    }
  ]
}
```

### 4.2 Entidades de Room Database
El agente debe diseñar las siguientes tablas relacionales:
1. **`PatternEntity`**: Datos generales del patrón (`id`, `nombre`, `categoria`, `dificultad`, `tiempoEstimadoMin`, `hiloRecomendado`, `agujaMm`, `extrasJson`, `notasEnsamblaje`, `esPersonalizado`).
2. **`PartEntity`**: Piezas asociadas a un patrón (`idPieza`, `patternId`, `orden`, `nombre`, `cantidadRequerida`, `notasPieza`).
3. **`RoundEntity`**: Vueltas asociadas a una pieza (`idVuelta`, `partId`, `numero`, `instruccion`, `puntosTotales`, `repeticiones`).
4. **`ActiveProgressEntity`**: Estado en vivo de un proyecto iniciado (`idSesion`, `patternId`, `currentPartId`, `currentPartInstance`, `currentRoundNumber`, `currentRepetitionCount`, `completedPartsMask`, `lastUpdatedTimestamp`).
5. **`CompletedProjectEntity`**: Historial de proyectos terminados (`idHistorial`, `patternId`, `startedAt`, `completedAt`, `notasUsuario`).

---

## 5. Diseño de Pantallas y Flujo de Navegación

### Pantalla 1: Bienvenida (`WelcomeScreen`)
* **Header:** Logo e ilustración amigable con el título **"Crochet Funny"** y subtítulo motivador.
* **Tarjeta Dinámica "Continuar Tejiendo":** Si existe un registro en `ActiveProgressEntity`, muestra una tarjeta destacada con el nombre del proyecto, pieza actual, instancia y vuelta (ej. *"Pulpo de Apego • Tentáculos (2/8) • Vuelta 1"*), barra de progreso porcentual y botón **"Reanudar"**.
* **Acción Principal:** Botón para ir al **"Catálogo de Proyectos"**.

### Pantalla 2: Catálogo e Historial (`CatalogScreen`)
* **Filtros por Categoría (Chips):** `Todos`, `Apego`, `Animales`, `Personajes`, `Recuerdos`.
* **Sección "Más Realizados":** Carrusel horizontal o insignias calculadas mediante un `COUNT(*)` en `CompletedProjectEntity`.
* **Lista de Patrones:** Tarjetas con icono/imagen, nombre, categoría, tiempo estimado y número de piezas.
* **Pestaña Inferior / Top Tab:** Alternar entre **"Explorar Catálogo"**, **"En Curso / Finalizados"** y **"Crear Patrón (Fase 2)"**.

### Pantalla 3: Vista Previa del Proyecto (`PatternDetailScreen`)
* **Resumen Técnico (Grid 2x2):**
  * Aguja recomendada (ej. `2.5 mm`)
  * Hilo sugerido (ej. `Algodón 4 ply`)
  * Tiempo aprox. (ej. `3 horas`)
  * Volumen (ej. `2 piezas / 6 vueltas tipo`)
* **Acordeón o Lista de Piezas:** Muestra qué piezas componen el amigurumi y cuántas repeticiones requiere cada una (ej. `Cabeza x1`, `Tentáculos x8`).
* **Materiales Extra y Notas:** Lista de accesorios (ojos de seguridad, relleno).
* **Botón Sticky Inferior:** **"Empezar Proyecto"** (o **"Continuar donde me quedé"** si ya está iniciado).

### Pantalla 4: Modo Tejido / Seguimiento Activo (`KnittingTrackerScreen`)
* **Barra Superior:** Nombre del patrón, porcentaje global de avance y botón de **"Reiniciar pieza"**.
* **Selector de Pieza Activa (Chips Horizontales):** Permite ver y cambiar entre las piezas del diseño (ej. `[Cabeza 1/1 ✓]`, `[Tentáculos 3/8 ●]`).
* **Selector de Instancia de Pieza:** Si la pieza tiene `cantidad_requerida > 1`, muestra un indicador claro: `Pieza 3 de 8` con botón para marcar la instancia actual como terminada al llegar a su última vuelta.
* **Lista de Vueltas con Auto-Scroll:**
  * **Vueltas Completadas:** Atenuadas con un ícono de check (`✓`).
  * **Vuelta Actual (Foco Principal):** Tarjeta expandida y resaltada con alto contraste, texto grande con la instrucción (ej. `(1 pb, 1 aum) x 6`), insignia de puntos totales (`18 pts`) y el sub-contador interactivo de repeticiones (`Repetición: 2 / 6 [+]`).
  * **Vueltas Siguientes:** En vista previa compacta para que el tejedor sepa qué viene después.
* **Barra de Control Inferior (Ergonómica para una mano):**
  * Botón secundario `- Vuelta Anterior`.
  * Botón gigante principal `+ Siguiente Vuelta` (o `Siguiente Pieza` si está en la última vuelta).
  * Cuando todas las piezas e instancias llegan al 100%, el botón cambia a **"🎉 Finalizar Proyecto"**.

---

## 6. Plan de Trabajo por Fases para el Agente (Paso a Paso)

> **Instrucción para el Agente IA:** No intentes generar todo el proyecto en una sola respuesta. Desarrolla el proyecto siguiendo estrictamente estas fases, esperando confirmación del usuario al finalizar cada una antes de pasar a la siguiente.

### 🔹 Fase 1: Configuración Base, JSON Semilla y Room Database
1. Configurar dependencias en `build.gradle.kts` (Room, Navigation Compose, Kotlinx Serialization, Material Icons Extended).
2. Crear el archivo `app/src/main/assets/patterns_seed.json` incluyendo al menos 3 proyectos completos: **Pulpo de Apego**, **Angelito Protector** y **Rana Saltarina**.
3. Crear las entidades de Room, los `DAO`s (`PatternDao`, `ProgressDao`, `HistoryDao`) y la clase `CrochetDatabase` con pre-carga automática del JSON semilla.

### 🔹 Fase 2: Repositorio y ViewModels
1. Crear `CrochetRepository` que exponga `Flow`s para el catálogo, progreso activo y estadísticas de proyectos más realizados.
2. Implementar la lógica de cálculo de avance en `TrackerViewModel`: avanzar vuelta, retroceder vuelta, incrementar instancia de pieza (ej. pasar del Tentáculo 1 al Tentáculo 2 y reiniciar la vuelta a la 1), y finalizar proyecto (mover de `ActiveProgressEntity` a `CompletedProjectEntity`).

### 🔹 Fase 3: Pantallas de Bienvenida, Catálogo y Vista Previa
1. Implementar el tema visual (`Theme.kt`) con colores amigables para tejido.
2. Construir `WelcomeScreen`, `CatalogScreen` (con filtros de categoría) y `PatternDetailScreen`.
3. Conectar la navegación hasta la vista previa del patrón.

### 🔹 Fase 4: Pantalla de Modo Tejido (`KnittingTrackerScreen`) y Persistencia
1. Construir la interfaz de seguimiento activo con auto-scroll hacia la vuelta actual (`LazyListState.animateScrollToItem`).
2. Agregar el contador secundario de repeticiones y el flag `KEEP_SCREEN_ON`.
3. Verificar que al cerrar la app y reabrirla desde `WelcomeScreen`, el estado persista exactamente en la pieza, instancia y vuelta correspondientes.

### 🔹 Fase 5: Historial, Estadísticas y Creador de Patrones (Escalabilidad)
1. Implementar la vista de **"Proyectos Finalizados y Más Realizados"**.
2. Crear la pantalla `CreatePatternScreen` con un formulario dinámico para que el usuario pueda dar de alta un nuevo proyecto desde su teléfono (Nombre, Categoría, Aguja, Hilo, agregar $N$ piezas y agregar $M$ vueltas por pieza) guardándolo directamente en Room.