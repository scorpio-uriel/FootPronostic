package com.example.footpronostic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.footpronostic.auth.LoginScreen
import com.example.footpronostic.auth.RegisterScreen
import com.example.footpronostic.ui.screens.MatchListScreen
import com.example.footpronostic.ui.screens.MyPronosticsScreen
import com.example.footpronostic.ui.screens.CreatePronosticScreen
import com.example.footpronostic.ui.screens.EditPronosticScreen
import com.example.footpronostic.ui.screens.ProfileScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // AUTH - Login
        composable(Routes.Login.route) {
            LoginScreen(
                onGoToRegister = {
                    navController.navigate(Routes.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.MatchList.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // AUTH - Register
        composable(Routes.Register.route) {
            RegisterScreen(
                onGoToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.MatchList.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // MATCHS - Liste des matchs disponibles
        composable(Routes.MatchList.route) {
            MatchListScreen(
                onNavigateToPronostics = {
                    navController.navigate(Routes.MyPronostics.route)
                },
                onNavigateToCreatePronostic = { matchId ->
                    navController.navigate(Routes.CreatePronostic.createRoute(matchId))
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // PROFIL - Gestion du profil utilisateur
        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // PRONOSTICS - Liste des pronostics de l'utilisateur
        composable(Routes.MyPronostics.route) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            MyPronosticsScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditPronostic = { pronosticId ->
                    navController.navigate(Routes.EditPronostic.createRoute(pronosticId))
                }
            )
        }

        // PRONOSTICS - Créer un pronostic
        composable(
            route = Routes.CreatePronostic.route,
            arguments = listOf(
                navArgument("matchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            CreatePronosticScreen(
                matchId = matchId,
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // PRONOSTICS - Éditer un pronostic
        composable(
            route = Routes.EditPronostic.route,
            arguments = listOf(
                navArgument("pronosticId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pronosticId = backStackEntry.arguments?.getString("pronosticId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            EditPronosticScreen(
                pronosticId = pronosticId,
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
