# 🧶 Crochet Funny

Aplicación móvil nativa para Android desarrollada con **Jetpack Compose**, **Material Design 3** y **Room Database**. Diseñada como un asistente inteligente paso a paso para amantes del crochet y los amigurumis, permitiendo seguir patrones sin perder la cuenta de piezas, vueltas ni repeticiones, midiendo automáticamente el tiempo real invertido y guardando el progreso en todo momento.

---

## ✨ Características Principales

* **💾 Persistencia Inmediata en Tiempo Real**: Cada vuelta avanzada, repetición incrementada o cambio de pieza se guarda al instante en la base de datos local SQLite (Room). Si sales de la app o reinicias el teléfono, retomas exactamente donde te quedaste.
* **⏱️ Modo Tejido (Seguimiento Activo Ergónomico)**:
  * **Auto-Scroll Inteligente**: La lista se desplaza automáticamente manteniendo en foco la vuelta activa.
  * **Vuelta Activa Destacada**: Tarjeta con alto contraste, tipografía grande, indicación de puntos totales e instrucciones claras.
  * **Vueltas Previas y Futuras**: Visualiza fácilmente qué vueltas ya terminaste (con opción de regresar a ellas) y qué vueltas vienen a continuación.
  * **Sub-contador de Repeticiones**: Para instrucciones complejas como `(1 pb, 1 aum) x 6`, incluye un contador secundario interactivo (`0 / 6`) con botones `+` y `-`.
* **🔆 Pantalla Siempre Encendida (`FLAG_KEEP_SCREEN_ON`)**: Mientras estás en la pantalla de tejido, la pantalla no se apaga para que puedas tejer cómodamente con ambas manos libres.
* **🧩 Multiplicidad de Piezas e Instancias**: Soporte para piezas repetidas (ej. *Tentáculos x8* o *Brazos x2*) indicando con precisión la instancia actual (ej. *Tentáculo - Pieza 3 de 8 - Vuelta 1*).
* **⏳ Cronometraje y Cálculo Automático de Tiempo**:
  * El usuario no necesita ingresar el tiempo manualmente. La app mide el tiempo exacto transcurrido desde que se inicia el proyecto hasta que se presiona **"Finalizar Proyecto"**.
  * Al culminar, muestra un diálogo de felicitaciones con el tiempo exacto invertido (horas, minutos y segundos).
* **✏️ Creador y Editor de Patrones Dinámico**:
  * Permite dar de alta patrones propios o editar patrones existentes del catálogo.
  * Configuración de nombre, categoría, icono personalizado, aguja recomendada, tipo de hilo, notas de ensamblaje, agregando $N$ piezas y $M$ vueltas por pieza.
* **🏆 Logros, Ranking e Historial**:
  * **Top Más Realizados**: Ranking calculado automáticamente según la cantidad de veces que se ha tejido cada diseño.
  * **Historial Cronológico**: Registro de todos los proyectos finalizados con fecha, hora y duración exacta de tejido.
* **🎨 Diseño Cálido y Pastel**: Estilo visual amigable para tejedores, con paleta cálida (terracota, crema, ámbar y verde salvia) e interfaz ergonómica operable con una sola mano.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** [Kotlin](https://kotlinlang.org/) (2.4.10)
* **Framework UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) con **Material 3**
* **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture ligera
* **Persistencia Local:** [Room Database](https://developer.android.com/training/data-storage/room) (SQLite) con [KSP](https://kotlinlang.org/docs/ksp-overview.html)
* **Serialización:** [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) (para carga inicial desde JSON semilla y extras)
* **Navegación:** Jetpack Navigation Compose (Single Activity Architecture)
* **Reactividad y Asincronía:** Kotlin Coroutines & `StateFlow`
* **Compilación y Build:** Android Gradle Plugin (AGP) 9.5.1, `compileSdk` 37, `minSdk` 28

---

## 📂 Estructura del Proyecto

```
app/src/main/
├── assets/
│   └── patterns_seed.json              # Catálogo semilla inicial (Pulpo, Angelito, Rana)
└── java/com/nexdev/crochet/
    ├── MainActivity.kt                 # Punto de entrada único con inyección de repositorios
    ├── data/
    │   ├── local/
    │   │   ├── CrochetDatabase.kt      # Configuración de Room DB con callback de auto-seed
    │   │   ├── dao/
    │   │   │   ├── PatternDao.kt       # Consultas y transacciones de patrones, piezas y vueltas
    │   │   │   ├── ProgressDao.kt      # Guardado en vivo del progreso (WIPs)
    │   │   │   └── HistoryDao.kt       # Historial y conteo de proyectos terminados
    │   │   ├── entity/
    │   │   │   ├── PatternEntity.kt    # Entidad de catálogo
    │   │   │   ├── PartEntity.kt       # Piezas asociadas a un patrón (con ForeignKey y CASCADE)
    │   │   │   ├── RoundEntity.kt      # Vueltas asociadas a una pieza
    │   │   │   ├── ActiveProgressEntity.kt # Sesión activa en curso
    │   │   │   ├── CompletedProjectEntity.kt # Registro de proyectos terminados
    │   │   │   └── PatternRelations.kt # Relaciones relacionales de Room
    │   │   └── model/
    │   │       └── SeedModels.kt       # DTOs para deserializar patterns_seed.json
    │   └── repository/
    │       └── CrochetRepository.kt    # Capa de abstracción de datos para los ViewModels
    └── ui/
        ├── navigation/
        │   └── CrochetNavigation.kt    # NavHost, destinos y Bottom Navigation Bar
        ├── screens/
        │   ├── WelcomeScreen.kt        # Bienvenida y tarjeta para reanudar proyecto activo
        │   ├── CatalogScreen.kt        # Explorador con filtros de categoría y banner WIP
        │   ├── PatternDetailScreen.kt  # Vista previa técnica, desglose de piezas y botón editar
        │   ├── KnittingTrackerScreen.kt # Modo tejido con auto-scroll, repeticiones y Keep-Screen-On
        │   ├── StatsScreen.kt          # Ranking de favoritos e historial cronológico
        │   └── CreatePatternScreen.kt  # Formulario dinámico de creación y edición de patrones
        ├── theme/
        │   ├── Color.kt                # Paleta de colores cálidos, pastel y contrastes
        │   ├── Theme.kt                # Configuración de CrochetTheme (Material 3)
        │   └── Type.kt                 # Tipografías y jerarquía visual
        ├── util/
        │   └── ProgressCalculator.kt   # Cálculo porcentual y formateo de duraciones reales
        └── viewmodel/
            ├── CatalogViewModel.kt     # Estado del catálogo, filtros y creación de patrones
            ├── TrackerViewModel.kt     # Lógica de vueltas, piezas, repeticiones y finalización
            └── CrochetViewModelFactory.kt # Factory para inyección de dependencias
```

---

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos Previos
1. **Android Studio** Ladybug, Meerkat o superior.
2. **JDK 17** o **JDK 21** configurado en Gradle.
3. Dispositivo físico Android o Emulador con Android 9.0 (API 28) o superior.

### Pasos
1. Clona el repositorio o abre la carpeta del proyecto en Android Studio:
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   ```
2. Permite que Android Studio sincronice las dependencias de Gradle.
3. Compila el proyecto desde la terminal o mediante el botón **Run**:
   ```bash
   ./gradlew assembleDebug
   ```
4. Instala y ejecuta la aplicación en tu dispositivo o emulador.

---

## 📝 Licencia

Este proyecto está bajo la licencia [MIT](LICENSE). Desarrollado por **NexDev**.
