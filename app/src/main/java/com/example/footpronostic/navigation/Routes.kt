package com.example.footpronostic.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")

    object MatchList : Routes("match_list")
    object MyPronostics : Routes("my_pronostics")
    object Profile : Routes("profile")

    // Route avec paramètre pour créer un pronostic
    object CreatePronostic : Routes("create_pronostic/{matchId}") {
        fun createRoute(matchId: String) = "create_pronostic/$matchId"
    }

    // Route avec paramètre pour éditer un pronostic
    object EditPronostic : Routes("edit_pronostic/{pronosticId}") {
        fun createRoute(pronosticId: String) = "edit_pronostic/$pronosticId"
    }
}
