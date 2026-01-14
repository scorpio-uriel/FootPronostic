package com.example.footpronostic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.footpronostic.data.model.AvatarConfig
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.ui.viewmodel.MatchViewModel
import com.example.footpronostic.ui.viewmodel.PronosticViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran principal : liste des matchs disponibles pour faire les pronostics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    matchViewModel: MatchViewModel = viewModel(),
    pronosticViewModel: PronosticViewModel = viewModel(),
    onNavigateToPronostics: () -> Unit,
    onNavigateToCreatePronostic: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val matches by matchViewModel.matches.collectAsState()
    val isLoading by matchViewModel.isLoading.collectAsState()
    val errorMessage by matchViewModel.errorMessage.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var userAvatar by remember { mutableStateOf(AvatarConfig()) }
    var userPoints by remember { mutableStateOf(0) }
    var userRole by remember { mutableStateOf("USER") }

    // Liste des IDs des matchs déjà pariés
    val myPronostics by pronosticViewModel.pronostics.collectAsState()
    val betMatchIds = remember(myPronostics) { myPronostics.map { it.matchId }.toSet() }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            pronosticViewModel.loadUserPronostics(uid)
            db.collection("users").document(uid).addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    userPoints = doc.getLong("points")?.toInt() ?: 0
                    userRole = doc.getString("role") ?: "USER"
                    val map = doc.get("avatar") as? Map<*, *>
                    if (map != null) {
                        userAvatar = AvatarConfig(
                            style = map["style"] as? String ?: "avataaars",
                            seed = map["seed"] as? String ?: "default"
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "FootPronostic",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$userPoints pts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Bouton Admin Dashboard (Visible uniquement pour les ADMIN)
                    if (userRole == "ADMIN") {
                        IconButton(onClick = onNavigateToAdmin) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(Icons.Default.Leaderboard, "Classement", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { matchViewModel.refreshMatches() }) {
                        Icon(Icons.Default.Refresh, "Actualiser", tint = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onNavigateToPronostics() },
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarSmallPreview(userAvatar)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Mon Profil") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                onClick = { showMenu = false; onNavigateToProfile() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Déconnexion", color = Color.Red) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red) },
                                onClick = { showMenu = false; FirebaseAuth.getInstance().signOut(); onLogout() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(matches) { match ->
                        val hasBet = betMatchIds.contains(match.id)
                        MatchCard(
                            match = match, 
                            hasBet = hasBet,
                            onBetClick = { onNavigateToCreatePronostic(match.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarSmallPreview(avatar: AvatarConfig) {
    AsyncImage(
        model = AvatarUtils.getAvatarUrl(avatar),
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun MatchCard(match: SportMatch, hasBet: Boolean, onBetClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasBet) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDateTime(match.dateTime), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                if (hasBet) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("DÉJÀ PARIÉ", modifier = Modifier.padding(2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(match.teamA, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("VS", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp))
                Text(match.teamB, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            if (match.status == "finished") {
                Text("Score : ${match.scoreA} - ${match.scoreB}", color = MaterialTheme.colorScheme.primary)
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                if (hasBet) {
                    OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth(), enabled = false) {
                        Text("Pari enregistré")
                    }
                } else {
                    Button(onClick = onBetClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Parier")
                    }
                }
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE dd MMM · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
