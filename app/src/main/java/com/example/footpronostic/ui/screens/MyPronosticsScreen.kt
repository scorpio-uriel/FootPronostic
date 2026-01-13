package com.example.footpronostic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.footpronostic.data.model.Pronostic
import com.example.footpronostic.ui.viewmodel.PronosticViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran affichant tous les pronostics de l'utilisateur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPronosticsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onEditPronostic: (String) -> Unit,
    pronosticViewModel: PronosticViewModel = viewModel()
) {
    val pronostics by pronosticViewModel.pronostics.collectAsState()
    val isLoading by pronosticViewModel.isLoading.collectAsState()
    
    val db = FirebaseFirestore.getInstance()
    var userAvatar by remember { mutableStateOf(AvatarConfig()) }

    // Charger l'avatar de l'utilisateur
    LaunchedEffect(userId) {
        pronosticViewModel.loadUserPronostics(userId)
        
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes pronostics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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

                pronostics.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 Aucun pronostic",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vous n'avez pas encore de pronostics.",
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
                        items(pronostics) { pronostic ->
                            PronosticCard(
                                pronostic = pronostic,
                                userAvatar = userAvatar,
                                onEdit = { onEditPronostic(pronostic.id) },
                                onDelete = { pronosticViewModel.deletePronostic(pronostic.id) }
                            )
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
    // Suppression de la condition temporelle pour permettre la modification/suppression à tout moment pour le test
    // Vous pourrez la remettre plus tard : val canEdit = System.currentTimeMillis() < pronostic.matchDateTime
    val canEdit = true 

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pronostic.isValidated)
                MaterialTheme.colorScheme.tertiaryContainer
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
            // Affichage de l'avatar à gauche
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
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDateTime(pronostic.matchDateTime),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )

                    if (pronostic.isValidated) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "✓ ${pronostic.points} pts",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Match
                Text(
                    text = "${pronostic.matchTeamA} vs ${pronostic.matchTeamB}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Pronostic
                Text(
                    text = "Votre prono : ${pronostic.predictedScoreA} - ${pronostic.predictedScoreB}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Actions
                if (canEdit && !pronostic.isValidated) {
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
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Modifier",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Supprimer", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else if (!canEdit) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Match commencé",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarMediumPreview(avatar: AvatarConfig) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(AvatarUtils.getSkinRes(avatar.skin)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getHairRes(avatar.hair)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getEyesRes(avatar.eyes)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getMouthRes(avatar.mouth)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getOutfitRes(avatar.outfit)), null, modifier = Modifier.fillMaxSize())
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE dd MMM · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
