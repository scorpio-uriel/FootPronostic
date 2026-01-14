package com.example.footpronostic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PendingActions
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
import com.example.footpronostic.data.model.Pronostic
import com.example.footpronostic.ui.viewmodel.PronosticViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran affichant tous les pronostics de l'utilisateur avec onglets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPronosticsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onEditPronostic: (String) -> Unit,
    pronosticViewModel: PronosticViewModel = viewModel()
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pronosticToDelete by remember { mutableStateOf<Pronostic?>(null) }
    val pronostics by pronosticViewModel.pronostics.collectAsState()
    val isLoading by pronosticViewModel.isLoading.collectAsState()

    val db = FirebaseFirestore.getInstance()
    var userAvatar by remember { mutableStateOf(AvatarConfig()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Charger l'avatar et les données
    LaunchedEffect(userId) {
        pronosticViewModel.loadUserPronostics(userId)
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
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

    // Filtrage des listes
    val pendingPronos = pronostics.filter { !it.isValidated }
    val historyPronos = pronostics.filter { it.isValidated }

    // Dialogue de confirmation de suppression
    if (showDeleteConfirm && pronosticToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                pronosticToDelete = null
            },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce pronostic ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pronosticToDelete?.let { pronosticViewModel.deletePronostic(it.id) }
                        showDeleteConfirm = false
                        pronosticToDelete = null
                    }
                ) {
                    Text("Supprimer", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    pronosticToDelete = null
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes pronostics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("En cours (${pendingPronos.size})") },
                    icon = { Icon(Icons.Default.PendingActions, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Terminés (${historyPronos.size})") },
                    icon = { Icon(Icons.Default.History, null) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    val currentList = if (selectedTab == 0) pendingPronos else historyPronos

                    if (currentList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (selectedTab == 0) "🎯 Aucun pari en attente" else "📜 Historique vide",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = if (selectedTab == 0) "Il est temps de parier !" else "Vos résultats s'afficheront ici.",
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentList) { pronostic ->
                                PronosticCard(
                                    pronostic = pronostic,
                                    userAvatar = userAvatar,
                                    onEdit = { onEditPronostic(pronostic.id) },
                                    onDelete = {
                                        pronosticToDelete = pronostic
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PronosticCard(
    pronostic: Pronostic,
    userAvatar: AvatarConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // On ne peut modifier/supprimer que si le pronostic n'est pas validé
    val canModify = !pronostic.isValidated

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pronostic.isValidated)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AvatarMediumPreview(userAvatar)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatDateTime(pronostic.matchDateTime),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    if (pronostic.isValidated) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            val sign = if (pronostic.pointsGained >= 0) "+" else ""
                            Text(
                                "$sign${pronostic.pointsGained} pts",
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pronostic.matchTeamA} vs ${pronostic.matchTeamB}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Votre prono : ${pronostic.predictedScoreA} - ${pronostic.predictedScoreB}",
                    color = MaterialTheme.colorScheme.primary
                )

                if (canModify) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Supprimer", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarMediumPreview(avatar: AvatarConfig) {
    AsyncImage(
        model = AvatarUtils.getAvatarUrl(avatar),
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE dd MMM · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
