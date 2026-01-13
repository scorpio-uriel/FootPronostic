package com.example.footpronostic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footpronostic.data.model.AvatarConfig
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.ui.viewmodel.MatchViewModel
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
    onNavigateToPronostics: () -> Unit,
    onNavigateToCreatePronostic: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val matches by matchViewModel.matches.collectAsState()
    val isLoading by matchViewModel.isLoading.collectAsState()
    val errorMessage by matchViewModel.errorMessage.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var userAvatar by remember { mutableStateOf(AvatarConfig()) }

    // Charger l'avatar de l'utilisateur connecté
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val map = doc.get("avatar") as? Map<*, *>
                    if (map != null) {
                        userAvatar = AvatarConfig(
                            skin = map["skin"] as? String ?: "light",
                            hair = map["hair"] as? String ?: "short",
                            eyes = map["eyes"] as? String ?: "default",
                            mouth = map["mouth"] as? String ?: "smile",
                            outfit = map["outfit"] as? String ?: "hoodie"
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
                    Text(
                        "Matchs du jour",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Bouton Actualiser
                    IconButton(onClick = { matchViewModel.refreshMatches() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Avatar de l'utilisateur cliquable vers Mes Pronostics
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onNavigateToPronostics() },
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarSmallPreview(userAvatar)
                    }

                    // Menu avec déconnexion et profil
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Email de l'utilisateur
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Connecté en tant que :",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = currentUser?.email ?: "Utilisateur",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                },
                                onClick = { }
                            )

                            HorizontalDivider()

                            // Bouton Profil
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mon Profil")
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToProfile()
                                }
                            )

                            // Bouton Déconnexion
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Déconnexion",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌ Erreur",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Erreur inconnue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { matchViewModel.refreshMatches() }) {
                            Text("Réessayer")
                        }
                    }
                }

                matches.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚽ Aucun match",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aucun match disponible 😭",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(matches) { match ->
                            MatchCard(
                                match = match,
                                onBetClick = { onNavigateToCreatePronostic(match.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarSmallPreview(avatar: AvatarConfig) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(AvatarUtils.getSkinRes(avatar.skin)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getHairRes(avatar.hair)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getEyesRes(avatar.eyes)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getMouthRes(avatar.mouth)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getOutfitRes(avatar.outfit)), null, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun MatchCard(
    match: SportMatch,
    onBetClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête avec date/heure
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDateTime(match.dateTime),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                Surface(
                    color = if (match.status == "finished")
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (match.status == "finished") "TERMINÉ" else "À VENIR",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Match
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.teamA,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Text(
                    text = match.teamB,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Score si le match est terminé
            if (match.status == "finished") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Score final : ${match.scoreA} - ${match.scoreB}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton parier (uniquement si le match n'a pas commencé)
            if (match.status != "finished" && System.currentTimeMillis() < match.dateTime) {
                Button(
                    onClick = onBetClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("🎯 Parier sur ce match")
                }
            } else if (System.currentTimeMillis() >= match.dateTime && match.status != "finished") {
                Text(
                    text = "⚠️ Match en cours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE dd MMM · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
