package com.example.footpronostic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.footpronostic.ui.screens.MatchListScreen
import com.example.footpronostic.ui.theme.FootPronosticTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FootPronosticTheme {
                MatchListScreen()
            }
        }
    }
}
