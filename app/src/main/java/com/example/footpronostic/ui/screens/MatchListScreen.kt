package com.example.footpronostic.ui.screens

import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.footpronostic.data.model.AvatarConfig
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.ui.theme.DeepStadium
import com.example.footpronostic.ui.theme.PitchGreen
import com.example.footpronostic.ui.theme.StadiumGrey
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
                    // 1. Bouton Admin (Visible uniquement pour les ADMIN)
                    if (userRole == "ADMIN") {
                        IconButton(onClick = onNavigateToAdmin) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // 2. Bouton Classement
                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(
                            Icons.Default.Leaderboard,
                            "Classement",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 3. Bouton Actualiser
                    IconButton(onClick = { matchViewModel.refreshMatches() }) {
                        Icon(
                            Icons.Default.Refresh,
                            "Actualiser",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 4. Nouveau bouton direct pour Mes Pronostics
                    IconButton(onClick = onNavigateToPronostics) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = "Mes Pronostics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 5. L'Avatar qui remplace le menu "Trois points"
                    Box {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp, start = 4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showMenu = true }, // Ouvre le menu au clic
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarSmallPreview(userAvatar)
                        }

                        // Menu déroulant attaché à l'avatar
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mon Profil") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToProfile()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Déconnexion", color = Color.Red) },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ExitToApp,
                                        null,
                                        tint = Color.Red
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDateTime(match.dateTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = StadiumGrey
                    )
                    if (hasBet) {
                        Surface(
                            color = PitchGreen.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                "DÉJÀ PARIÉ",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Équipes avec VS parfaitement centré
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.teamA,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "VS",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = PitchGreen
                    )

                    Text(
                        text = match.teamB,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (match.status != "finished") {
                    Button(
                        onClick = onBetClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !hasBet,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasBet) StadiumGrey.copy(alpha = 0.2f) else PitchGreen,
                            contentColor = DeepStadium
                        )
                    ) {
                        Text(
                            if (hasBet) "Pari enregistré" else "PARIER",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Text(
                        "Score final : ${match.scoreA} - ${match.scoreB}",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.titleMedium,
                        color = PitchGreen
                    )
                }
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE dd MMM · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
