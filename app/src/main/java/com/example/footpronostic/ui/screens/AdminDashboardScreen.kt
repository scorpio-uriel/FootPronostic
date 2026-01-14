package com.example.footpronostic.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.footpronostic.data.model.UserProfile
import com.example.footpronostic.data.model.AvatarConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

/**
 * Écran d'administration pour gérer les utilisateurs et les points.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            users = snapshot?.documents?.mapNotNull { doc ->
                val avatarMap = doc.get("avatar") as? Map<*, *>
                UserProfile(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    points = doc.getLong("points")?.toInt() ?: 0,
                    role = doc.getString("role") ?: "USER",
                    avatar = AvatarConfig(
                        style = avatarMap?.get("style") as? String ?: "avataaars",
                        seed = avatarMap?.get("seed") as? String ?: "default"
                    )
                )
            } ?: emptyList()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Admin 🛡️") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    AdminUserCard(
                        user = user,
                        onUpdatePoints = { amount ->
                            db.collection("users").document(user.uid)
                                .update("points", FieldValue.increment(amount.toLong()))
                                .addOnSuccessListener { 
                                    Toast.makeText(context, "Points mis à jour", Toast.LENGTH_SHORT).show() 
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    onUpdatePoints: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (user.role == "ADMIN") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(user.email, fontWeight = FontWeight.Bold)
                    Text("Points actuels : ${user.points}", style = MaterialTheme.typography.bodySmall)
                }
                
                if (user.role == "ADMIN") {
                    Badge(containerColor = Color.Red, contentColor = Color.White) { Text("ADMIN") }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onUpdatePoints(10) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("+10", color = Color.White)
                }
                
                Button(
                    onClick = { onUpdatePoints(-10) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("-10", color = Color.White)
                }
            }
        }
    }
}
