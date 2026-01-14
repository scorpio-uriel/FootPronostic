package com.example.footpronostic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.footpronostic.navigation.AppNavGraph
import com.example.footpronostic.navigation.Routes
import com.example.footpronostic.ui.theme.FootPronosticTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
            Routes.MatchList.route
        } else {
            Routes.Login.route
        }

        setContent {
            // Forcez darkTheme = true pour le look Stadium Night
            FootPronosticTheme(darkTheme = true) {
                // Ajoutez une Surface pour s'assurer que le fond couvre tout l'écran
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
