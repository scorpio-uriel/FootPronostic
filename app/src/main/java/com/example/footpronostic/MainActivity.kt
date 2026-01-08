package com.example.footpronostic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.footpronostic.navigation.AppNavGraph
import com.example.footpronostic.navigation.Routes
import com.example.footpronostic.ui.theme.FootPronosticTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination =
            if (FirebaseAuth.getInstance().currentUser != null)
                Routes.Home.route
            else
                Routes.Login.route

        setContent {
            FootPronosticTheme {
                AppNavGraph(startDestination = startDestination)
            }
        }
    }
}
