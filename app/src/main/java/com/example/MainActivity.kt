package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presentation.ui.*
import com.example.presentation.viewmodel.GenerateViewModel
import com.example.presentation.viewmodel.LibraryViewModel
import com.example.presentation.viewmodel.ResultViewModel
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge drawing
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Get pre-initialized local service locator
                val appContainer = (application as MainApplication).container
                val navController = rememberNavController()

                // Instants ViewModels using Custom Factory providers
                val generateViewModel: GenerateViewModel = viewModel(
                    factory = GenerateViewModel.Factory(appContainer.projectRepository)
                )

                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.Factory(appContainer.projectRepository)
                )

                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(appContainer.projectRepository)
                )

                // Sync generator configurations instantly if settings are loaded
                val defaultLanguage by settingsViewModel.defaultLanguage.collectAsState()
                val defaultStyle by settingsViewModel.defaultStyle.collectAsState()

                LaunchedEffect(defaultLanguage, defaultStyle) {
                    generateViewModel.setDefaults(defaultLanguage, defaultStyle)
                }

                NavHost(
                    navController = navController,
                    startDestination = "generate",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("generate") {
                        GenerateScreen(
                            viewModel = generateViewModel,
                            onNavigateToResult = { projectId ->
                                navController.navigate("result/$projectId")
                            },
                            onNavigateToLibrary = {
                                navController.navigate("library")
                            },
                            onNavigateToManageData = {
                                navController.navigate("manage_data")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable(
                        route = "result/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
                        
                        // Use unique key so separate results do not cross-talk
                        val resultViewModel: ResultViewModel = viewModel(
                            key = "result_viewmodel_$projectId",
                            factory = ResultViewModel.Factory(appContainer.projectRepository, projectId)
                        )

                        ResultScreen(
                            viewModel = resultViewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("library") {
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onProjectSelect = { projectId ->
                                navController.navigate("result/$projectId")
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("manage_data") {
                        ManageDataScreen(
                            viewModel = generateViewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
