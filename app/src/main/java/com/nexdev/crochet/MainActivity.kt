package com.nexdev.crochet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.nexdev.crochet.data.local.CrochetDatabase
import com.nexdev.crochet.data.repository.CrochetRepository
import com.nexdev.crochet.ui.navigation.CrochetApp
import com.nexdev.crochet.ui.theme.CrochetTheme
import com.nexdev.crochet.ui.viewmodel.CatalogViewModel
import com.nexdev.crochet.ui.viewmodel.CrochetViewModelFactory
import com.nexdev.crochet.ui.viewmodel.TrackerViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy {
        CrochetDatabase.getDatabase(applicationContext)
    }

    private val repository by lazy {
        CrochetRepository(
            patternDao = database.patternDao(),
            progressDao = database.progressDao(),
            historyDao = database.historyDao()
        )
    }

    private val viewModelFactory by lazy {
        CrochetViewModelFactory(repository)
    }

    private val catalogViewModel: CatalogViewModel by viewModels { viewModelFactory }
    private val trackerViewModel: TrackerViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrochetTheme {
                CrochetApp(
                    catalogViewModel = catalogViewModel,
                    trackerViewModel = trackerViewModel
                )
            }
        }
    }
}
