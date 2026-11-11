package com.nexdev.crochet.ui.navigation

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexdev.crochet.ui.screens.CatalogScreen
import com.nexdev.crochet.ui.screens.CreatePatternScreen
import com.nexdev.crochet.ui.screens.KnittingTrackerScreen
import com.nexdev.crochet.ui.screens.PatternDetailScreen
import com.nexdev.crochet.ui.screens.StatsScreen
import com.nexdev.crochet.ui.screens.WelcomeScreen
import com.nexdev.crochet.ui.theme.TerracottaPrimary
import com.nexdev.crochet.ui.theme.TextSubtleTaupe
import com.nexdev.crochet.ui.theme.WarmBorder
import com.nexdev.crochet.ui.theme.WarmCreamBg
import com.nexdev.crochet.ui.viewmodel.CatalogViewModel
import com.nexdev.crochet.ui.viewmodel.TrackerViewModel

object CrochetDestinations {
    const val WELCOME = "welcome"
    const val CATALOG = "catalog"
    const val DETAIL = "detail/{patternId}"
    const val TRACKER = "tracker"
    const val STATS = "stats"
    const val CREATOR = "creator"
    const val EDIT_PATTERN = "edit_pattern/{patternId}"

    fun detailRoute(patternId: String) = "detail/$patternId"
    fun editPatternRoute(patternId: String) = "edit_pattern/$patternId"
}

@Composable
fun CrochetApp(
    catalogViewModel: CatalogViewModel,
    trackerViewModel: TrackerViewModel,
    navController: NavHostController = rememberNavController()
) {
    val catalogState by catalogViewModel.uiState.collectAsStateWithLifecycle()
    val trackerState by trackerViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: CrochetDestinations.WELCOME

    val hasActiveProject = catalogState.latestResumeSummary != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WarmCreamBg,
        bottomBar = {
            CrochetBottomBar(
                currentRoute = currentRoute,
                hasActiveProject = hasActiveProject,
                onNavigate = { route ->
                    if (route == CrochetDestinations.TRACKER && trackerState.patternWithDetails == null) {
                        catalogState.latestResumeSummary?.patternWithDetails?.pattern?.id?.let { latestId ->
                            trackerViewModel.loadPatternForTracking(latestId, startFresh = false)
                        }
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CrochetDestinations.WELCOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CrochetDestinations.WELCOME) {
                WelcomeScreen(
                    resumeSummary = catalogState.latestResumeSummary,
                    completedCount = catalogState.totalCompletedCount,
                    onExploreCatalog = {
                        navController.navigate(CrochetDestinations.CATALOG)
                    },
                    onResumeProject = { patternId ->
                        trackerViewModel.loadPatternForTracking(patternId, startFresh = false)
                        navController.navigate(CrochetDestinations.TRACKER)
                    },
                    onOpenStats = {
                        navController.navigate(CrochetDestinations.STATS)
                    },
                    onOpenCreator = {
                        navController.navigate(CrochetDestinations.CREATOR)
                    }
                )
            }

            composable(CrochetDestinations.CATALOG) {
                CatalogScreen(
                    uiState = catalogState,
                    onSelectCategory = { cat -> catalogViewModel.selectCategory(cat) },
                    onOpenPatternDetail = { patternId ->
                        navController.navigate(CrochetDestinations.detailRoute(patternId))
                    },
                    onResumeActiveProject = { patternId ->
                        trackerViewModel.loadPatternForTracking(patternId, startFresh = false)
                        navController.navigate(CrochetDestinations.TRACKER)
                    },
                    onNavigateHome = {
                        navController.navigate(CrochetDestinations.WELCOME) {
                            popUpTo(CrochetDestinations.WELCOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onOpenCreator = {
                        navController.navigate(CrochetDestinations.CREATOR)
                    }
                )
            }

            composable(
                route = CrochetDestinations.DETAIL,
                arguments = listOf(navArgument("patternId") { type = NavType.StringType })
            ) { backStackEntry ->
                val patternId = backStackEntry.arguments?.getString("patternId") ?: ""
                val patternWithDetails = catalogState.patterns.find { it.pattern.id == patternId }
                val isAlreadyActive = catalogState.activeProgressByPatternId.containsKey(patternId)
                val context = LocalContext.current

                PatternDetailScreen(
                    patternWithDetails = patternWithDetails,
                    isAlreadyActive = isAlreadyActive,
                    onBackToCatalog = {
                        navController.popBackStack()
                    },
                    onEditPattern = { id ->
                        navController.navigate(CrochetDestinations.editPatternRoute(id))
                    },
                    onSharePattern = { id ->
                        catalogViewModel.compartirPatron(id) { exito ->
                            if (exito) {
                                Toast.makeText(context, "¡Gracias! El patrón fue enviado a revisión.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al enviar. Revisa tu conexión a internet.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onStartNewProject = { id ->
                        catalogViewModel.startNewProject(id) {
                            trackerViewModel.loadPatternForTracking(id, startFresh = false)
                            navController.navigate(CrochetDestinations.TRACKER)
                        }
                    },
                    onContinueProject = { id ->
                        catalogViewModel.resumeOrStartProject(id) {
                            trackerViewModel.loadPatternForTracking(id, startFresh = false)
                            navController.navigate(CrochetDestinations.TRACKER)
                        }
                    }
                )
            }

            composable(CrochetDestinations.TRACKER) {
                KnittingTrackerScreen(
                    uiState = trackerState,
                    onPauseAndExit = {
                        navController.navigate(CrochetDestinations.CATALOG) {
                            launchSingleTop = true
                        }
                    },
                    onExploreCatalog = {
                        navController.navigate(CrochetDestinations.CATALOG) {
                            launchSingleTop = true
                        }
                    },
                    onResetPart = { trackerViewModel.resetCurrentPart() },
                    onSelectPart = { idx -> trackerViewModel.selectPart(idx) },
                    onJumpToRound = { rIdx -> trackerViewModel.jumpToRound(rIdx) },
                    onChangeRepetition = { delta -> trackerViewModel.changeRepetition(delta) },
                    onPrevRound = { trackerViewModel.previousRound() },
                    onNextRoundOrFinish = { onMsg, onFinish ->
                        trackerViewModel.nextRoundOrFinish(
                            onMessage = onMsg,
                            onProjectFinished = onFinish
                        )
                    },
                    onProjectCompletedNavigate = {
                        trackerViewModel.loadPatternForTracking(null)
                        navController.navigate(CrochetDestinations.STATS) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(CrochetDestinations.STATS) {
                StatsScreen(
                    uiState = catalogState,
                    onOpenPatternDetail = { patternId ->
                        navController.navigate(CrochetDestinations.detailRoute(patternId))
                    },
                    onExploreCatalog = {
                        navController.navigate(CrochetDestinations.CATALOG) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(CrochetDestinations.CREATOR) {
                CreatePatternScreen(
                    patternToEdit = null,
                    onSavePattern = { _, nombre, categoria, agujaMm, hilo, icono, notas, partsDraft ->
                        catalogViewModel.createCustomPattern(
                            nombre = nombre,
                            categoria = categoria,
                            hiloRecomendado = hilo,
                            agujaMm = agujaMm,
                            icono = icono,
                            notasEnsamblaje = notas,
                            partsDraft = partsDraft
                        ) { newPatternId ->
                            navController.navigate(CrochetDestinations.detailRoute(newPatternId)) {
                                popUpTo(CrochetDestinations.CATALOG) { inclusive = false }
                            }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = CrochetDestinations.EDIT_PATTERN,
                arguments = listOf(navArgument("patternId") { type = NavType.StringType })
            ) { backStackEntry ->
                val patternId = backStackEntry.arguments?.getString("patternId") ?: ""
                val patternWithDetails = catalogState.patterns.find { it.pattern.id == patternId }

                CreatePatternScreen(
                    patternToEdit = patternWithDetails,
                    onSavePattern = { id, nombre, categoria, agujaMm, hilo, icono, notas, partsDraft ->
                        val targetId = id ?: patternId
                        catalogViewModel.updateExistingPattern(
                            patternId = targetId,
                            nombre = nombre,
                            categoria = categoria,
                            hiloRecomendado = hilo,
                            agujaMm = agujaMm,
                            icono = icono,
                            colorBg = patternWithDetails?.pattern?.colorBg ?: "#F4E6DD",
                            notasEnsamblaje = notas,
                            partsDraft = partsDraft
                        ) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun CrochetBottomBar(
    currentRoute: String,
    hasActiveProject: Boolean,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, WarmBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = "🏠",
                label = "Inicio",
                selected = currentRoute == CrochetDestinations.WELCOME,
                onClick = { onNavigate(CrochetDestinations.WELCOME) }
            )
            BottomNavItem(
                icon = "🧶",
                label = "Catálogo",
                selected = currentRoute == CrochetDestinations.CATALOG || currentRoute.startsWith("detail/"),
                onClick = { onNavigate(CrochetDestinations.CATALOG) }
            )
            BottomNavItem(
                icon = "⏱️",
                label = "Tejiendo",
                selected = currentRoute == CrochetDestinations.TRACKER,
                showBadgeDot = hasActiveProject,
                onClick = { onNavigate(CrochetDestinations.TRACKER) }
            )
            BottomNavItem(
                icon = "🏆",
                label = "Logros",
                selected = currentRoute == CrochetDestinations.STATS,
                onClick = { onNavigate(CrochetDestinations.STATS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    selected: Boolean,
    showBadgeDot: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (selected) TerracottaPrimary else TextSubtleTaupe
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box {
                Text(text = icon, fontSize = 18.sp)
                if (showBadgeDot) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = 12.dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimary)
                    )
                }
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
